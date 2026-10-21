(ns brickbreaker.ui
  (:require
    [reagent.core :as r]
    [reagent.dom.client :as rdc]
    [brickbreaker.core :as core]))

(defonce game-state
  (r/atom (core/new-game {:seed (rand-int 1000000)})))

(defonce held-keys
  (atom #{}))

(defn current-action []
  (let [keys @held-keys]
    (cond
      (and (keys "ArrowLeft") (not (keys "ArrowRight"))) :action/left
      (and (keys "ArrowRight") (not (keys "ArrowLeft"))) :action/right
      :else :action/none)))

(defn bricks-view [state]
  [:g
   (for [[column row] (:bricks/alive state)]
     ^{:key (str column "-" row)}
     [:rect {:x (* column (:bricks/width state))
             :y (+ (:bricks/top state) (* row (:bricks/height state)))
             :width (:bricks/width state)
             :height (:bricks/height state)
             :fill (str "hsl(" (* row 36) ", 70%, 55%)")
             :stroke "#111"
             :stroke-width 0.3}])])

(defn game-view []
  (let [state @game-state]
    [:div {:style {:font-family "sans-serif"
                   :color "#eee"
                   :background "#111"
                   :min-height "100vh"
                   :padding 16}}
     [:div "Score: " (core/score state)
      " | Tick: " (:game/tick state)
      " | " (name (:game/status state))]
     [:svg {:view-box (str "0 0 " (:world/width state) " " (:world/height state))
            :style {:width "min(90vw, 80vh)"
                    :background "#222"
                    :display "block"}}
      [bricks-view state]
      [:rect {:x (- (:paddle/x state) (/ (:paddle/width state) 2))
              :y (- (:paddle/y state) (/ (:paddle/height state) 2))
              :width (:paddle/width state)
              :height (:paddle/height state)
              :fill "#eee"}]
      [:circle {:cx (:ball/x state)
                :cy (:ball/y state)
                :r (:ball/radius state)
                :fill "#fff"}]]
     [:div "Arrow keys move. Space starts a new game."]]))

(defn on-key-down! [event]
  (if (= " " (.-key event))
    (reset! game-state (core/new-game {:seed (rand-int 1000000)}))
    (swap! held-keys conj (.-key event))))

(defn on-key-up! [event]
  (swap! held-keys disj (.-key event)))

(defn tick! []
  (swap! game-state core/step (current-action))
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
