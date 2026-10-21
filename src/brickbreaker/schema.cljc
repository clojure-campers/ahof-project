(ns brickbreaker.schema
  (:require
    [malli.core :as m]))

(def Location
  [:map
   [:location/x number?]
   [:location/y number?]])

(def Velocity
  [:map
   [:velocity/x number?]
   [:velocity/y number?]])

(def Brick
  [:tuple nat-int? nat-int?])

(def Action
  [:enum :action/left :action/right :action/none])

(def State
  [:map
   [:state/game-tick nat-int?]
   [:state/game-status [:enum :status/playing :status/won :status/lost]]
   [:state/bricks-broken nat-int?]
   [:state/paddle-location Location]
   [:state/ball-location Location]
   [:state/ball-velocity Velocity]
   [:state/bricks-alive [:set Brick]]])

(def Constants
  [:map
   [:constant/world-width pos?]
   [:constant/world-height pos?]
   [:constant/paddle-width pos?]
   [:constant/paddle-speed pos?]
   [:constant/paddle-height pos?]
   [:constant/ball-max-velocity-x pos?]
   [:constant/ball-radius pos?]
   [:constant/brick-columns pos-int?]
   [:constant/brick-rows pos-int?]
   [:constant/brick-width pos?]
   [:constant/brick-height pos?]
   [:constant/bricks-top number?]])

(def valid-state?
  (m/validator State))

(def explain-state
  (m/explainer State))
