(ns brickbreaker.physics)

(defn clamp [value low high]
  (max low (min high value)))

(defn move-paddle [state action]
  (let [direction (case action
                    :action/left -1
                    :action/right 1
                    :action/none 0)
        half-width (/ (:paddle/width state) 2)]
    (update state :paddle/x
            (fn [x]
              (clamp (+ x (* direction (:paddle/speed state)))
                     half-width
                     (- (:world/width state) half-width))))))

(defn brick-at
  "Returns [column row] of the alive brick containing point, or nil."
  [state x y]
  (let [column (int (Math/floor (/ x (:bricks/width state))))
        row (int (Math/floor (/ (- y (:bricks/top state)) (:bricks/height state))))
        cell [column row]]
    (when (contains? (:bricks/alive state) cell)
      cell)))

(defn break-brick [state cell]
  (-> state
      (update :bricks/alive disj cell)
      (update :game/bricks-broken inc)))

(defn sign [value]
  (if (neg? value)
    -1
    1))

(defn move-ball-x [state]
  (let [{:ball/keys [x y vx radius]} state
        next-x (+ x vx)
        cell (brick-at state (+ next-x (* (sign vx) radius)) y)]
    (cond
      cell
      (-> state
          (break-brick cell)
          (update :ball/vx -))

      (< (- next-x radius) 0)
      (assoc state
             :ball/x radius
             :ball/vx (Math/abs (double vx)))

      (> (+ next-x radius) (:world/width state))
      (assoc state
             :ball/x (- (:world/width state) radius)
             :ball/vx (- (Math/abs (double vx))))

      :else
      (assoc state :ball/x next-x))))

(defn hits-paddle? [state next-y]
  (let [{:ball/keys [x y vy radius]} state
        {:paddle/keys [width]} state
        paddle-top (- (:paddle/y state) (/ (:paddle/height state) 2))
        half-width (/ width 2)]
    (and
      (pos? vy)
      (<= (+ y radius) paddle-top)
      (> (+ next-y radius) paddle-top)
      (<= (- (:paddle/x state) half-width) x (+ (:paddle/x state) half-width)))))

(defn bounce-off-paddle [state]
  (let [offset (/ (- (:ball/x state) (:paddle/x state))
                  (/ (:paddle/width state) 2))
        paddle-top (- (:paddle/y state) (/ (:paddle/height state) 2))]
    (assoc state
           :ball/y (- paddle-top (:ball/radius state))
           :ball/vy (- (Math/abs (double (:ball/vy state))))
           :ball/vx (* offset (:ball/max-vx state)))))

(defn move-ball-y [state]
  (let [{:ball/keys [x y vy radius]} state
        next-y (+ y vy)
        cell (brick-at state x (+ next-y (* (sign vy) radius)))]
    (cond
      cell
      (-> state
          (break-brick cell)
          (update :ball/vy -))

      (hits-paddle? state next-y)
      (bounce-off-paddle state)

      (< (- next-y radius) 0)
      (assoc state
             :ball/y radius
             :ball/vy (Math/abs (double vy)))

      :else
      (assoc state :ball/y next-y))))

(defn move-ball [state]
  (-> state
      move-ball-x
      move-ball-y))
