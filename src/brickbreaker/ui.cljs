(ns brickbreaker.ui
  (:require
    [reagent.core :as r]
    [reagent.dom.client :as rdc]
    [brickbreaker.ui.play :as play]
    [brickbreaker.ui.replay :as replay]))

(defonce current-page
  (r/atom :page/replay))

(def pages
  [{:page/id :page/play
    :page/name "Play"}
   {:page/id :page/replay
    :page/name "Bots"}])

(defn app-view []
  [:div {:style {:font-family "sans-serif"
                 :color "#eee"
                 :background "#111"
                 :min-height "100vh"
                 :padding 16}}
   [:div {:style {:display "flex"
                  :gap 8
                  :margin-bottom 16}}
    (for [{:page/keys [id name]} pages]
      ^{:key id}
      [:button {:on-click (fn [event]
                            (.blur (.-target event))
                            (reset! current-page id))
                :disabled (= id @current-page)}
       name])]
   (case @current-page
     :page/play [play/play-view]
     :page/replay [replay/replay-view])])

(defn on-key-down! [event]
  (case @current-page
    :page/play (play/on-key-down! event)
    :page/replay (replay/on-key-down! event)))

(defn on-key-up! [event]
  (play/on-key-up! event))

(defn tick! []
  (case @current-page
    :page/play (play/tick!)
    :page/replay (replay/tick!))
  (js/requestAnimationFrame tick!))

(defonce root
  (rdc/create-root (js/document.getElementById "app")))

(defn ^:dev/after-load render! []
  (rdc/render root [app-view]))

(defonce started
  (do
    (js/window.addEventListener "keydown" on-key-down!)
    (js/window.addEventListener "keyup" on-key-up!)
    (js/requestAnimationFrame tick!)
    true))

(defn init! []
  (render!))
