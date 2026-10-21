(ns brickbreaker.physics
  (:require
    [brickbreaker.constants :as constants]))

(defn clamp [value low high]
  (max low (min high value)))

(defn sign [value]
  (if (neg? value)
    -1
    1))

(defn absolute [value]
  (Math/abs (double value)))

(defn move-paddle [state action]
  (let [{:constant/keys [paddle-width paddle-speed world-width]} constants/constants
        direction (case action
                    :action/left -1
                    :action/right 1
                    :action/none 0)
        half-width (/ paddle-width 2)]
    (update-in state [:state/paddle-location :location/x]
               (fn [x]
                 (clamp (+ x (* direction paddle-speed))
                        half-width
                        (- world-width half-width))))))

(defn brick-at
  "Returns [column row] of the alive brick containing the point, or nil."
  [{:state/keys [bricks-alive]} {:location/keys [x y]}]
  (let [{:constant/keys [brick-width brick-height bricks-top]} constants/constants
        cell [(int (Math/floor (/ x brick-width)))
              (int (Math/floor (/ (- y bricks-top) brick-height)))]]
    (when (contains? bricks-alive cell)
      cell)))

(defn break-brick [state cell]
  (-> state
      (update :state/bricks-alive disj cell)
      (update :state/bricks-broken inc)))

(defn move-ball-x [{:state/keys [ball-location ball-velocity] :as state}]
  (let [{:constant/keys [ball-radius world-width]} constants/constants
        {:location/keys [x y]} ball-location
        velocity-x (:velocity/x ball-velocity)
        next-x (+ x velocity-x)
        cell (brick-at state {:location/x (+ next-x (* (sign velocity-x) ball-radius))
                              :location/y y})]
    (cond
      cell
      (-> state
          (break-brick cell)
          (update-in [:state/ball-velocity :velocity/x] -))

      (< (- next-x ball-radius) 0)
      (-> state
          (assoc-in [:state/ball-location :location/x] ball-radius)
          (assoc-in [:state/ball-velocity :velocity/x] (absolute velocity-x)))

      (> (+ next-x ball-radius) world-width)
      (-> state
          (assoc-in [:state/ball-location :location/x] (- world-width ball-radius))
          (assoc-in [:state/ball-velocity :velocity/x] (- (absolute velocity-x))))

      :else
      (assoc-in state [:state/ball-location :location/x] next-x))))

(defn paddle-top [{:state/keys [paddle-location]}]
  (- (:location/y paddle-location)
     (/ (:constant/paddle-height constants/constants) 2)))

(defn hits-paddle? [{:state/keys [ball-location ball-velocity paddle-location] :as state} next-y]
  (let [{:constant/keys [ball-radius paddle-width]} constants/constants
        {:location/keys [x y]} ball-location
        top (paddle-top state)
        half-width (/ paddle-width 2)
        paddle-x (:location/x paddle-location)]
    (and
      (pos? (:velocity/y ball-velocity))
      (<= (+ y ball-radius) top)
      (> (+ next-y ball-radius) top)
      (<= (- paddle-x half-width) x (+ paddle-x half-width)))))

(defn bounce-off-paddle [{:state/keys [ball-location ball-velocity paddle-location] :as state}]
  (let [{:constant/keys [ball-radius paddle-width ball-max-velocity-x]} constants/constants
        offset (/ (- (:location/x ball-location) (:location/x paddle-location))
                  (/ paddle-width 2))]
    (-> state
        (assoc-in [:state/ball-location :location/y] (- (paddle-top state) ball-radius))
        (assoc :state/ball-velocity {:velocity/x (* offset ball-max-velocity-x)
                                     :velocity/y (- (absolute (:velocity/y ball-velocity)))}))))

(defn move-ball-y [{:state/keys [ball-location ball-velocity] :as state}]
  (let [{:constant/keys [ball-radius]} constants/constants
        {:location/keys [x y]} ball-location
        velocity-y (:velocity/y ball-velocity)
        next-y (+ y velocity-y)
        cell (brick-at state {:location/x x
                              :location/y (+ next-y (* (sign velocity-y) ball-radius))})]
    (cond
      cell
      (-> state
          (break-brick cell)
          (update-in [:state/ball-velocity :velocity/y] -))

      (hits-paddle? state next-y)
      (bounce-off-paddle state)

      (< (- next-y ball-radius) 0)
      (-> state
          (assoc-in [:state/ball-location :location/y] ball-radius)
          (assoc-in [:state/ball-velocity :velocity/y] (absolute velocity-y)))

      :else
      (assoc-in state [:state/ball-location :location/y] next-y))))

(defn move-ball [state]
  (-> state
      move-ball-x
      move-ball-y))
