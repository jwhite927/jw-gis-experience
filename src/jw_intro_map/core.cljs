(ns jw-intro-map.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom])
  (:require-macros [jw-intro-map.macros]))

;; Your Mapbox public access token (starts with "pk."). Get one at
;; https://account.mapbox.com/access-tokens/
(def mapbox-token (jw-intro-map.macros/load-mapbox-token))

(def locations [{:title "The REED Center for Ecosystem Reintegration"
                 :lnglat #js [-77.602403 39.436613]}
                {:title "Infiswift Technologies"
                 :lnglat #js [-121.910136 37.450127]}])

(defn current-location [index]
  (get locations (mod index (count locations))))

;; Holds the mapboxgl.Map instance so it survives reloads and isn't
;; recreated on every render.
(defonce map-instance (atom nil))
(def selected-location (r/atom 0))

(defn create-map!
  "Initialize a Mapbox GL map on the given DOM node."
  [^js id]
  (set! (.-accessToken js/mapboxgl) mapbox-token)
  (let [^js m (js/mapboxgl.Map.
                #js {:container (.getElementById js/document id)
                     :style "mapbox://styles/mapbox/streets-v12"
                     :center #js [-77.602403 39.436613] ;; [lng lat]
                     :zoom 9})]
    (.addControl m (js/mapboxgl.NavigationControl.))
    (reset! map-instance m)))

(defn create-marker!
  "Initialize a Mapbox marker on map at the given lng lat."
  [^js m {:keys [^js lnglat]}]
  (let [^js marker (js/mapboxgl.Marker.)]
    (.setLngLat marker lnglat)
    (.addTo marker m)))

(defn map-view
  "Form-3 Reagent component that owns a Mapbox map. Reagent never
  re-renders the inner div's contents, so Mapbox keeps control of it."
  []
  (r/create-class
    {:display-name "map-view"
     :component-did-mount
     (fn [_]
       (let [m (create-map! "jw-map")]
         (doseq [l locations]
           (create-marker! m l))))
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
    [:span "In my journey as an engineer, I developed strong values for maintainability, simplicity, and reliability in developing systems and processes. As a result, I believe I am well suited for the "]
    [:span {:class "font-bold"} "Full Stack Developer - Pyrecast at Spatial Informatics Group"]
    [:span " role."]]
   [:span "I created this map using Reagent and Mapbox to both demonstrate these skills and describe these experiences:"]
   [map-view]
   [:button {:on-click #(do
                          (swap! selected-location + 1)
                          (.panTo @map-instance (:lnglat (current-location @selected-location))))} (str "A button " @selected-location)]
   [:div (str (current-location @selected-location))]])

(defn mount-root []
  (rdom/render [init-component] (.getElementById js/document "app")))

(defn ^:export init []
  (mount-root))

(defn on-js-reload []
  (mount-root))

(mount-root)
