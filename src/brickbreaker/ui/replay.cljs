(ns brickbreaker.ui.replay
  (:require
    [clojure.string :as string]
    [reagent.core :as r]
    [brickbreaker.bots :as bots]
    [brickbreaker.game :as game]
    [brickbreaker.simulate :as simulate]
    [brickbreaker.ui.board :as board]))

(def max-ticks 50000)

(def speeds [1 2 4 8 16 32])

(defn simulate-bots [seed]
  (vec
    (for [bot bots/bots]
      {:run/bot bot
       :run/states (simulate/play-game {:state (game/new-game {:seed seed})
                                        :decide (:bot/decide bot)
                                        :max-ticks max-ticks})})))

(defn new-replay [seed]
  {:replay/seed seed
   :replay/runs (simulate-bots seed)
   :replay/tick 0
   :replay/playing? false
   :replay/speed 1})

(defonce replay-state
  (r/atom (new-replay 42)))

(defn last-tick [{:replay/keys [runs]}]
  (->> runs
       (map (fn [run] (dec (count (:run/states run)))))
       (apply max 0)))

(defn set-tick! [tick]
  (swap! replay-state
         (fn [replay]
           (assoc replay :replay/tick (-> tick
                                          (max 0)
                                          (min (last-tick replay)))))))

(defn step-tick! [delta]
  (set-tick! (+ (:replay/tick @replay-state) delta)))

(defn toggle-playing! []
  (swap! replay-state
         (fn [replay]
           (if (and (not (:replay/playing? replay))
                    (= (:replay/tick replay) (last-tick replay)))
             (assoc replay
               :replay/tick 0
               :replay/playing? true)
             (update replay :replay/playing? not)))))

(defn reseed! [seed]
  (reset! replay-state (new-replay seed)))

(defn tick! []
  (let [{:replay/keys [playing? tick speed] :as replay} @replay-state]
    (when playing?
      (if (>= tick (last-tick replay))
        (swap! replay-state assoc :replay/playing? false)
        (set-tick! (+ tick speed))))))

(defn on-key-down! [event]
  (let [step (if (.-shiftKey event) 10 1)
        handle! (fn [action!]
                  ;; stops focused buttons and the slider from also reacting
                  (.preventDefault event)
                  (action!))]
    (case (.-key event)
      " " (handle! toggle-playing!)
      "ArrowLeft" (handle! (fn [] (step-tick! (- step))))
      "ArrowRight" (handle! (fn [] (step-tick! step)))
      nil)))

(defn format-state [state]
  (str "{"
       (->> (sort-by key state)
            (map (fn [[state-key value]]
                   (str (pr-str state-key) " " (pr-str value))))
            (string/join "\n "))
       "}"))

(defn run-view [{:keys [run tick]}]
  (let [{:run/keys [bot states]} run
        state (get states (min tick (dec (count states))))]
    [:div
     [:div {:style {:font-weight "bold"}}
      (:bot/name bot)]
     [:div "Score: " (game/score state)
      " | Tick: " (:state/game-tick state)
      " | " (name (:state/game-status state))]
     [:div {:style {:display "flex"
                    :flex-wrap "wrap"
                    :gap 8}}
      [board/board-view {:state state
                         :width "min(90vw, 40vh)"}]
      [:div {:style {:font-family "monospace"
                     :font-size 12
                     :max-width 320}}
       [:pre {:style {:margin 0
                      :white-space "pre-wrap"
                      :word-break "break-all"}}
        (format-state state)]
       [:div {:style {:margin-top 8}}
        "Bot action: "
        [:span {:style {:color "#ff0"}}
         (pr-str ((:bot/decide bot) state))]]]]]))

(defn controls-view []
  (let [{:replay/keys [seed tick playing? speed] :as replay} @replay-state
        final-tick (last-tick replay)]
    [:div {:style {:display "flex"
                   :flex-wrap "wrap"
                   :gap 8
                   :align-items "center"
                   :margin-bottom 16}}
     [:button {:on-click (fn [_] (set-tick! 0))} "⏮"]
     [:button {:on-click (fn [_] (step-tick! -1))} "◀"]
     [:button {:on-click (fn [_] (toggle-playing!))
               :style {:width 60}}
      (if playing? "Pause" "Play")]
     [:button {:on-click (fn [_] (step-tick! 1))} "▶"]
     [:button {:on-click (fn [_] (set-tick! final-tick))} "⏭"]
     [:input {:type "range"
              :min 0
              :max final-tick
              :value tick
              :style {:flex 1
                      :min-width 200}
              :on-change (fn [event]
                           (set-tick! (js/parseInt (.. event -target -value))))}]
     [:span tick " / " final-tick]
     [:label "Speed "
      [:select {:value speed
                :on-change (fn [event]
                             (swap! replay-state assoc :replay/speed (js/parseInt (.. event -target -value))))}
       (for [option speeds]
         ^{:key option}
         [:option {:value option} (str option "x")])]]
     [:label "Seed "
      [:input {:type "number"
               :default-value seed
               :style {:width 80}
               :on-key-down (fn [event]
                              (.stopPropagation event))
               :on-blur (fn [event]
                          (let [new-seed (js/parseInt (.. event -target -value))]
                            (when (and (not (js/isNaN new-seed))
                                       (not= new-seed seed))
                              (reseed! new-seed))))}]]
     [:button {:on-click (fn [_] (reseed! (rand-int 1000000)))} "Random seed"]]))

(defn replay-view []
  (let [{:replay/keys [runs tick seed]} @replay-state]
    [:div
     ^{:key seed} [controls-view]
     [:div {:style {:display "flex"
                    :flex-wrap "wrap"
                    :gap 16}}
      (for [run runs]
        ^{:key (:bot/id (:run/bot run))}
        [run-view {:run run
                   :tick tick}])]
     [:div {:style {:margin-top 16}}
      "Space play/pause. Arrows step (Shift for 10)."]]))
