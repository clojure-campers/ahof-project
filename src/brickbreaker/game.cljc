(ns brickbreaker.game
  (:require
    [brickbreaker.constants :as constants]
    [brickbreaker.physics :as physics]
    [brickbreaker.random :as random]))

(def actions
  #{:action/left :action/right :action/none})

(defn new-game
  [{:keys [seed fill-percent]
    :or {seed 42
         fill-percent 60}}]
  (let [{:constant/keys [brick-columns brick-rows]} constants/constants]
    {:state/game-tick 0
     :state/game-status :status/playing
     :state/bricks-broken 0
     :state/paddle-location {:location/x 50
                             :location/y 95}
     :state/ball-location {:location/x 50
                           :location/y 90}
     :state/ball-velocity {:velocity/x 0.7
                           :velocity/y -1}
     :state/bricks-alive (random/random-bricks {:seed seed
                                                :columns brick-columns
                                                :rows brick-rows
                                                :fill-percent fill-percent})}))

(defn game-over? [{:state/keys [game-status]}]
  (not= :status/playing game-status))

(defn update-status [{:state/keys [bricks-alive ball-location] :as state}]
  (let [{:constant/keys [ball-radius world-height]} constants/constants]
    (cond
      (empty? bricks-alive)
      (assoc state :state/game-status :status/won)

      (> (- (:location/y ball-location) ball-radius) world-height)
      (assoc state :state/game-status :status/lost)

      :else
      state)))

(defn step [state action]
  (if (game-over? state)
    state
    (-> state
        (physics/move-paddle action)
        physics/move-ball
        (update :state/game-tick inc)
        update-status)))

(defn score [{:state/keys [bricks-broken]}]
  bricks-broken)

#_(step (new-game {:seed 1}) :action/left)
