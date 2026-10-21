(ns brickbreaker.game-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [brickbreaker.game :as game]
    [brickbreaker.schema :as schema]
    [brickbreaker.simulate :as simulate]))

(defn play-with-bot [seed]
  (simulate/play-game {:state (game/new-game {:seed seed})
                       :decide simulate/follow-ball
                       :max-ticks 50000}))

(deftest bot-wins-test
  (doseq [seed [1 42 99]]
    (testing (str "seed " seed)
      (let [states (play-with-bot seed)
            first-state (first states)
            final-state (peek states)]
        (is (= :status/won (:state/game-status final-state)))
        (is (empty? (:state/bricks-alive final-state)))
        (is (= (count (:state/bricks-alive first-state))
               (game/score final-state)))))))

(deftest states-match-schema-test
  (is (every? schema/valid-state? (play-with-bot 42))))

(deftest deterministic-test
  (is (= (play-with-bot 5)
         (play-with-bot 5))))

(deftest no-input-loses-test
  (let [final-state (peek (simulate/play-game {:state (game/new-game {:seed 42})
                                               :decide (fn [_state] :action/none)
                                               :max-ticks 50000}))]
    (is (= :status/lost (:state/game-status final-state)))))

(deftest step-after-game-over-test
  (let [lost-state (assoc (game/new-game {:seed 42}) :state/game-status :status/lost)]
    (is (= lost-state (game/step lost-state :action/left)))))

(deftest paddle-stays-in-world-test
  (let [final-state (peek (simulate/play-game {:state (game/new-game {:seed 42})
                                               :decide (fn [_state] :action/left)
                                               :max-ticks 100}))]
    (is (<= 0 (get-in final-state [:state/paddle-location :location/x])))))
