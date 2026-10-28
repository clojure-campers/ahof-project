(ns brickbreaker.ui.board
  (:require
    [brickbreaker.constants :as constants]))

(defn bricks-view [{:state/keys [bricks-alive]}]
  (let [{:constant/keys [brick-width brick-height bricks-top]} constants/constants]
    [:g
     (for [[column row] bricks-alive]
       ^{:key (str column "-" row)}
       [:rect {:x (* column brick-width)
               :y (+ bricks-top (* row brick-height))
               :width brick-width
               :height brick-height
               :fill (str "hsl(" (* row 36) ", 70%, 55%)")
               :stroke "#111"
               :stroke-width 0.3}])]))

(defn board-view [{:keys [state width]}]
  (let [{:state/keys [paddle-location ball-location]} state
        {:constant/keys [world-width world-height paddle-width paddle-height ball-radius]} constants/constants]
    [:svg {:view-box (str "0 0 " world-width " " world-height)
           :style {:width width
                   :background "#222"
                   :display "block"}}
     [bricks-view state]
     [:rect {:x (- (:location/x paddle-location) (/ paddle-width 2))
             :y (- (:location/y paddle-location) (/ paddle-height 2))
             :width paddle-width
             :height paddle-height
             :fill "#eee"}]
     [:circle {:cx (:location/x ball-location)
               :cy (:location/y ball-location)
               :r ball-radius
               :fill "#fff"}]]))
