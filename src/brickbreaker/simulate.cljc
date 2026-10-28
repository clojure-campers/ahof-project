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

(defn score
  [state]
  (let []
    {:percent-of-bricks-broken
     (/ (:state/bricks-broken state)
        (+ (count (:state/bricks-alive state))
           (:state/bricks-broken state)))
     :bricks-per-tick
     (/ (:state/bricks-broken state)
        (:state/game-tick state))}))

#_(let [states (play-game {:state (game/new-game {:seed 42})
                           :decide brickbreaker.bots.simple/follow-ball
                           :max-ticks 100000})
        final (peek states)]
    (score final))

(defn play-many
  [{:keys [decide max-ticks game-count]}]
  (->> (range game-count)
       (#?(:clj pmap
                :cljs map)
        (fn [n]
          (play-game {:state (game/new-game {:seed n})
                      :decide decide
                      :max-ticks max-ticks})))
       (take game-count)
       (map (fn [states]
              (score (peek states))))))

(defn average-score
  [scores]
  {:percent-of-bricks-broken
   (double (/ (apply + (map :percent-of-bricks-broken scores))
              (count scores)))
   :bricks-per-tick
   (double (/ (apply + (map :bricks-per-tick scores))
              (count scores)))})

(defn failed-game-seeds
  [{:keys [decide max-ticks game-count]}]
  (->> (range game-count)
       (#?(:clj pmap
                :cljs map)
        (fn [n]
          {:seed n
           :states (play-game {:state (game/new-game {:seed n})
                               :decide decide
                               :max-ticks max-ticks})}))
       (take game-count)
       (remove (fn [{:keys [states]}]
                 (= :status/won
                    (:state/game-status (peek states)))))
       (map :seed)))


#_(average-score (play-many {:decide brickbreaker.bots.simple/follow-ball
                             :max-ticks 100000
                             :game-count 200}))
#_(average-score (play-many {:decide
                             brickbreaker.bots.simple/avoid-center
                             :max-ticks 100000
                             :game-count 500}))

#_(failed-game-seeds {:decide
                      brickbreaker.bots.simple/avoid-center
                      :max-ticks 100000
                      :game-count 5000})
