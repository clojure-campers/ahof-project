(ns brickbreaker.ui.play
  (:require
    [reagent.core :as r]
    [brickbreaker.game :as game]
    [brickbreaker.ui.board :as board]))

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

(defn on-key-down! [event]
  (if (= " " (.-key event))
    (reset! game-state (game/new-game {:seed (rand-int 1000000)}))
    (swap! held-keys conj (.-key event))))

(defn on-key-up! [event]
  (swap! held-keys disj (.-key event)))

(defn tick! []
  (swap! game-state game/step (current-action)))

(defn play-view []
  (let [{:state/keys [game-tick game-status] :as state} @game-state]
    [:div
     [:div "Score: " (game/score state)
      " | Tick: " game-tick
      " | " (name game-status)]
     [board/board-view {:state state
                        :width "min(90vw, 80vh)"}]
     [:div "Arrow keys move. Space starts a new game."]]))
