(ns brickbreaker.ui
  (:require
    [reagent.core :as r]
    [reagent.dom.client :as rdc]
    [brickbreaker.constants :as constants]
    [brickbreaker.game :as game]))

(defonce game-state
  (r/atom (game/new-game {:seed (rand-int 1000000)})))

(defonce held-keys
  (atom #{}))

(defn current-action []
  (let [keys @held-keys]
    (cond
      (and (keys "ArrowLeft") (not (keys "ArrowRight"))) :action/left
      (and (keys "ArrowRight") (not (keys "ArrowLeft"))) :action/right
      :else :action/none)))

(defn bricks-view [{:state/keys [bricks-alive]}]
  (let [{:constant/keys [brick-width brick-height bricks-top]} constants/constants]
    [:g
     (for [[column row] bricks-alive]
       ^{:key (str column "-" row)}
       [:rect {:x (* column brick-width)
               :y (+ bricks-top (* row brick-height))
               :width brick-width
               :height brick-height
               :fill (str "hsl(" (* row 36) ", 70%, 55%)")
               :stroke "#111"
               :stroke-width 0.3}])]))

(defn game-view []
  (let [{:state/keys [game-tick game-status paddle-location ball-location] :as state} @game-state
        {:constant/keys [world-width world-height paddle-width paddle-height ball-radius]} constants/constants]
    [:div {:style {:font-family "sans-serif"
                   :color "#eee"
                   :background "#111"
                   :min-height "100vh"
                   :padding 16}}
     [:div "Score: " (game/score state)
      " | Tick: " game-tick
      " | " (name game-status)]
     [:svg {:view-box (str "0 0 " world-width " " world-height)
            :style {:width "min(90vw, 80vh)"
                    :background "#222"
                    :display "block"}}
      [bricks-view state]
      [:rect {:x (- (:location/x paddle-location) (/ paddle-width 2))
              :y (- (:location/y paddle-location) (/ paddle-height 2))
              :width paddle-width
              :height paddle-height
              :fill "#eee"}]
      [:circle {:cx (:location/x ball-location)
                :cy (:location/y ball-location)
                :r ball-radius
                :fill "#fff"}]]
     [:div "Arrow keys move. Space starts a new game."]]))

(defn on-key-down! [event]
  (if (= " " (.-key event))
    (reset! game-state (game/new-game {:seed (rand-int 1000000)}))
    (swap! held-keys conj (.-key event))))

(defn on-key-up! [event]
  (swap! held-keys disj (.-key event)))

(defn tick! []
  (swap! game-state game/step (current-action))
  (js/requestAnimationFrame tick!))

(defonce root
  (rdc/create-root (js/document.getElementById "app")))

(defn ^:dev/after-load render! []
  (rdc/render root [game-view]))

(defonce started
  (do
    (js/window.addEventListener "keydown" on-key-down!)
    (js/window.addEventListener "keyup" on-key-up!)
    (js/requestAnimationFrame tick!)
    true))

(defn init! []
  (render!))
