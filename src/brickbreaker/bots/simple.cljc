(ns brickbreaker.bots.simple)

(defn bot
  "Move towards the ball"
  [{:state/keys [paddle-location ball-location]}]
  (cond
    (< (:location/x paddle-location)
       (:location/x ball-location))
    :action/right
    (< (:location/x ball-location)
       (:location/x paddle-location))
    :action/left
    :else
    :action/none))

#_(bot (brickbreaker.game/new-game {:seed 1}))
#_(step (new-game {:seed 1}) :action/left)

(defn follow-ball
  [{:state/keys [ball-location paddle-location]}]
  (let [difference (- (:location/x ball-location)
                      (:location/x paddle-location))]
    (cond
      (< difference -1) :action/left
      (> difference 1) :action/right
      :else :action/none)))


(defn avoid-center
  "Move towards the ball"
  [{:state/keys [paddle-location ball-location]}]
  (let [difference (- (:location/x ball-location)
                      (:location/x paddle-location))]
    (cond
      (< difference -2) :action/left
      (> difference 2) :action/right
      :else :action/none)))


;; for one game
;;   1          63           -123
;; [win/loss bricks-broken elapsed-time]

;; for many games:
;;    0.92         62.3             -125.34
;; [win-ratio avg-bricks-broken avg-elapsed-time]


;; game
;;   won or lost          1 / 0
;;   # of bricks broken   0 - ~100
;;   % of bricks broken   0.0 - 1.0
;;   time taken           1 - infinity
;;   bricks-per-tick      0-100
;;   ticks-per-brick      100-infinity

;; %-of-bricks , bricks-per-tick

;;                    bad     middling         perfect
;;  slow             0.01       50.01           100.01
;;  middling         0.33       50.33           100.33
;;  fast             0.55       50.55           100.55


;; for many games:

;; median %-of-bricks, median bricks-per-tick
;; or
;; avg %-of-bricks, median bricks-per-tick



