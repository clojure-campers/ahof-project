(ns brickbreaker.simulate
  (:require
    [brickbreaker.core :as core]))

(defn play-game
  "Returns the vector of states, from start state to game over (or max-ticks)."
  [{:keys [state decide max-ticks]}]
  (loop [states [state]]
    (let [current (peek states)]
      (if (or
            (core/game-over? current)
            (>= (:game/tick current) max-ticks))
        states
        (recur (conj states (core/step current (decide current))))))))

(defn follow-ball [state]
  (let [difference (- (:ball/x state) (:paddle/x state))]
    (cond
      (< difference -1) :action/left
      (> difference 1) :action/right
      :else :action/none)))

(comment
  (let [states (play-game {:state (core/new-game {:seed 42})
                           :decide follow-ball
                           :max-ticks 100000})
        final (peek states)]
    (select-keys final [:game/tick :game/status :game/bricks-broken])))
