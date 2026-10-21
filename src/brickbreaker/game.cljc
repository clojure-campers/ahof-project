(ns brickbreaker.core
  (:require
    [brickbreaker.physics :as physics]
    [brickbreaker.random :as random]))

(def actions
  #{:action/left :action/right :action/none})

(defn new-game
  [{:keys [seed fill-percent]
    :or {seed 42
         fill-percent 60}}]
  {:game/tick 0
   :game/status :status/playing
   :game/seed seed ;; fixed
   :game/bricks-broken 0

   :world/width 100 ;; fixed
   :world/height 100 ;; fixed

   :paddle/x 50
   :paddle/y 95
   :paddle/width 16 ;; fixed
   :paddle/height 2 ;; fixed
   :paddle/speed 2

   :ball/x 50
   :ball/y 90
   :ball/vx 0.7
   :ball/vy -1
   :ball/max-vx 1.5 ;; fixed
   :ball/radius 1  ;; fixed

   :bricks/columns 10 ;; fixed
   :bricks/rows 10  ;; fixed
   :bricks/width 10  ;; fixed
   :bricks/height 4 ;; fixed
   :bricks/top 10  ;; fixed
   :bricks/alive (random/random-bricks {:seed seed
                                        :columns 10
                                        :rows 10
                                        :fill-percent fill-percent})})

(defn game-over? [state]
  (not= :status/playing (:game/status state)))

(defn update-status [state]
  (cond
    (empty? (:bricks/alive state))
    (assoc state :game/status :status/won)

    (> (- (:ball/y state) (:ball/radius state)) (:world/height state))
    (assoc state :game/status :status/lost)

    :else
    state))

(defn step [state action]
  (if (game-over? state)
    state
    (-> state
        (physics/move-paddle action)
        physics/move-ball
        (update :game/tick inc)
        update-status)))

(defn score [state]
  (:game/bricks-broken state))

#_(step (new-game {:seed 1}) :action/left)
