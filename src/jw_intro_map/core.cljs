(ns jw-intro-map.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

(enable-console-print!)

(println "This text is printed from src/jw-intro-map/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (r/atom {:text "Hello world!"}))

(defn init-component []
  [:div
   [:main
    [:h1
     (:text @app-state)]]])

(defn mount-root []
  (rdom/render [init-component] (.getElementById js/document "app")))

(defn ^:export init []
  (mount-root))

(defn on-js-reload []
  (mount-root))
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

(mount-root)
