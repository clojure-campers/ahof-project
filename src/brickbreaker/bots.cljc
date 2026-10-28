(ns brickbreaker.bots
  (:require
    [brickbreaker.bots.simple :as simple]
    [brickbreaker.simulate :as simulate]))

(def bots
  [{:bot/id :bot/simple
    :bot/name "Simple"
    :bot/decide simple/avoid-center}
   #_{:bot/id :bot/follow-ball
    :bot/name "Follow ball"
    :bot/decide simple/follow-ball}])
