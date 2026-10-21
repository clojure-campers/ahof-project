(ns brickbreaker.simulate
  (:require
    [brickbreaker.game :as game]))

(defn play-game
  "Returns the vector of states, from start state to game over (or max-ticks)."
  [{:keys [state decide max-ticks]}]
  (loop [states [state]]
    (let [current (peek states)]
      (if (or
            (game/game-over? current)
            (>= (:state/game-tick current) max-ticks))
        states
        (recur (conj states (game/step current (decide current))))))))

(defn follow-ball [{:state/keys [ball-location paddle-location]}]
  (let [difference (- (:location/x ball-location) (:location/x paddle-location))]
    (cond
      (< difference -1) :action/left
      (> difference 1) :action/right
      :else :action/none)))

(comment
  (let [states (play-game {:state (game/new-game {:seed 42})
                           :decide follow-ball
                           :max-ticks 100000})
        final (peek states)]
    (select-keys final [:state/game-tick :state/game-status :state/bricks-broken])))
