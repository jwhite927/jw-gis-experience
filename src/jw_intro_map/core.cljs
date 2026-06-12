(ns jw-intro-map.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [environ.core :refer [env]]))

;; Your Mapbox public access token (starts with "pk."). Get one at
;; https://account.mapbox.com/access-tokens/
(def mapbox-token (env :mapbox-public-token))

;; Holds the mapboxgl.Map instance so it survives reloads and isn't
;; recreated on every render.
(defonce map-instance (atom nil))

(defn create-map!
  "Initialize a Mapbox GL map on the given DOM node."
  [^js id]
  (set! (.-accessToken js/mapboxgl) mapbox-token)
  (let [^js m (js/mapboxgl.Map.
                #js {:container (.getElementById js/document id)
                     :style "mapbox://styles/mapbox/streets-v12"
                     :center #js [-122.27 37.80] ;; [lng lat]
                     :zoom 9})]
    (.addControl m (js/mapboxgl.NavigationControl.))
    (reset! map-instance m)))

(defn map-view
  "Form-3 Reagent component that owns a Mapbox map. Reagent never
  re-renders the inner div's contents, so Mapbox keeps control of it."
  []
  (r/create-class
    {:display-name "map-view"
     :component-did-mount
     (fn [_]
       (create-map! "jw-map"))
     :component-will-unmount
     (fn [_]
       (when-let [^js m @map-instance]
         (.remove m)
         (reset! map-instance nil)))
     :reagent-render
     (fn []
       [:div#jw-map])}))

(defn init-component []
  [:div {:class "flex flex-col mx-auto max-w-lg py-10"}
   [:h1 {:class "text-4xl font-bold"}
    "Josh's Fit with SIG"]
   [:div {}
    [:span "In my journey as an engineer, my sensibility and skillset to be closely aligned with the "]
    [:span {:class "font-bold"} "Full Stack Developer - Pyrecast at Spatial Informatics Group"]
    [:span " role."]]
   [:span "I created this map using reagent and mapbox to both demonstrate these skills and describe these experiences:"]
   [map-view]])

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
