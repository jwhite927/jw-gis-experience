(ns jw-intro-map.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom])
  (:require-macros [jw-intro-map.macros]))

(def mapbox-token (jw-intro-map.macros/load-mapbox-token))

(def locations [{:title "The REED Center for Ecosystem Reintegration"
                 :lnglat #js [-77.602403 39.436613]
                 :zoom 4
                 :place "Middletown, MD"
                 :year "2022 - 2026"
                 :role "Sole Developer"
                 :stack ["Clojure" "ClojureScript" "Electric v3" "Datahike" "OpenDroneMap" "OpenLayers"]
                 :blurb "I developed a mapping application to help orient visitors to a food forest under active development."}
                {:title "SLB's ValveCommander (Infiswift)"
                 :lnglat #js [-95.473717 29.749439]
                 :zoom 4
                 :place "Houston, TX"
                 :year "2022 - 2023"
                 :role "Consulting Full-Stack Developer"
                 :stack ["Typescript" "Angular" "C#" "Azure IoT" "Azure Pipelines"]
                 :blurb "I designed and shipped new features in an app used to monitor and control an active oil well."}
                {:title "Virtual Power Plant"
                 :lnglat #js [139.767336 35.651331]
                 :zoom 4
                 :place ""
                 :year "2020 - 2021"
                 :role "Deployment Engineer"
                 :stack ["Python" "AWS" "Docker" "MySql" "Elasticsearch" "Logstash" "Prometheus" "Jenkins Pipelines"]
                 :blurb "I migrated a virtual power plant application from California to Japan"}])

(defn current-location [index]
  (get locations (mod index (count locations))))

(defn has-lnglat?
  "Skip the map camera / marker for locations without coordinates yet."
  [^js lnglat]
  (and lnglat (pos? (.-length lnglat))))

;; Holds the mapboxgl.Map instance so it survives reloads and isn't
;; recreated on every render.
(defonce map-instance (atom nil))
;; Marker objects collected in location order so we can highlight the active
;; one. nil where a location has no :lnglat yet.
(defonce markers (atom []))
(def selected-location (r/atom 0))

;; ---- camera + marker highlight (mapbox stays outside Reagent's render) ----

(defn fly-to!
  "Ease the camera to location `i`."
  [i]
  (when-let [^js m @map-instance]
    (let [{:keys [^js lnglat zoom]} (current-location i)]
      (when (has-lnglat? lnglat)
        (.flyTo m #js {:center lnglat
                       :zoom (or zoom 9)
                       :duration 1500
                       :curve 1.42
                       :essential true})))))

(defn highlight!
  "Toggle the active class on the marker for location `i`."
  [i]
  (let [idx (mod i (count locations))]
    (doseq [[j ^js marker] (map-indexed vector @markers)]
      (when marker
        (.toggle (.. marker getElement -classList) "jw-marker--active" (= j idx))))))

(defn go!
  "Cycle the selected index; current-location does the mod."
  [step]
  (swap! selected-location + step))

;; Drive the camera off a watch, not render.
(defonce camera-watch
  (add-watch selected-location ::fly
             (fn [_ _ _ i]
               (fly-to! i)
               (highlight! i))))

;; ←/→ keys cycle, same as the arrow buttons.
(defonce keyboard-listener
  (.addEventListener
    js/document "keydown"
    (fn [^js e]
      (case (.-key e)
        "ArrowLeft" (go! -1)
        "ArrowRight" (go! 1)
        nil))))

;; ---- map construction ----

(defn create-map!
  "Initialize a Mapbox GL map on the given DOM node."
  [^js id]
  (set! (.-accessToken js/mapboxgl) mapbox-token)
  (let [^js m (js/mapboxgl.Map.
                #js {:container (.getElementById js/document id)
                     :style "mapbox://styles/mapbox/streets-v12"
                     :center (-> locations first :lnglat) ;; [lng lat]
                     :zoom 4})]
    (.addControl m (js/mapboxgl.NavigationControl.))
    (reset! map-instance m)))

(defn create-marker!
  "Build a custom DOM marker on `m` for location index `i`. Clicking it
  selects that location. Returns the Marker, or nil when there's no lnglat."
  [^js m i {:keys [^js lnglat]}]
  (when (has-lnglat? lnglat)
    (let [el (.createElement js/document "div")
          ^js marker (js/mapboxgl.Marker. #js {:element el})]
      (set! (.-className el) "jw-marker")
      (.addEventListener el "click" #(reset! selected-location i))
      (.setLngLat marker lnglat)
      (.addTo marker m)
      marker)))

;; ---- components ----

(defn intro-header []
  [:div {:class "border-b-2 border-ink pb-4 mb-5"}
   [:div {:class "font-mono text-[10px] tracking-[0.16em] text-accent font-semibold"}
    "FIELD NOTES · GIS IN PRODUCTION"]
   [:h1 {:class "text-3xl font-bold leading-tight mt-2"}
    "Josh's Fit with SIG"]
   [:p {:class "text-sm text-ink-2 leading-relaxed mt-3"}
    "In my journey as an engineer, I developed strong values for maintainability, simplicity, and reliability in developing systems and processes. As a result, I believe I am well suited for the "
    [:span {:class "font-semibold text-ink"} "Full Stack Developer - Pyrecast at Spatial Informatics Group"]
    " role."]
   [:p {:class "text-sm text-ink-2 leading-relaxed mt-2"}
    "I created this map using Reagent and Mapbox to both demonstrate these skills and describe these experiences:"]])

(defn- pad2 [n]
  (if (< n 10) (str "0" n) (str n)))

(defn progress-bar []
  (let [total (count locations)
        i (mod @selected-location total)]
    [:div {:class "flex items-center justify-between mt-1"}
     [:span {:class "font-mono text-sm font-semibold"}
      (pad2 (inc i))
      [:span {:class "text-ink-3"} (str " / " (pad2 total))]]
     [:span {:class "flex gap-1.5"}
      (for [j (range total)]
        ^{:key j}
        [:i {:class (str "h-1 rounded-full cursor-pointer transition-all "
                         (if (= j i) "w-7 bg-accent" "w-5 bg-hatch"))
             :on-click #(reset! selected-location j)}])]]))

(defn map-view
  "Form-3 Reagent component that owns a Mapbox map. Reagent never
  re-renders the inner div's contents, so Mapbox keeps control of it."
  []
  (r/create-class
    {:display-name "map-view"
     :component-did-mount
     (fn [_]
       (let [m (create-map! "jw-map")]
         (reset! markers
                 (vec (map-indexed (fn [i l] (create-marker! m i l)) locations)))
         (highlight! @selected-location)))
     :component-will-unmount
     (fn [_]
       (when-let [^js m @map-instance]
         (.remove m)
         (reset! map-instance nil)
         (reset! markers [])))
     :component-did-catch
     (fn []
       [:div "No Mapbox token detected!"])
     :reagent-render
     (fn []
       [:div#jw-map])}))

(defn project-card [{:keys [title place year role stack blurb]}]
  [:div {:class "jw-card flex-1 relative rounded-lg border border-ink-2 bg-card p-3.5"}
   [:div {:class "flex justify-between items-start gap-2.5 mb-2"}
    [:div {:class "flex-1"}
     (when (seq place)
       [:div {:class "font-mono text-[10px] tracking-[0.06em] text-accent font-semibold"}
        (str "◆ " place)])
     [:div {:class "text-lg font-semibold leading-tight mt-1"} title]]
    (when (or (seq year) (seq role))
      [:div {:class "text-right shrink-0"}
       (when (seq year) [:div {:class "font-mono text-xs text-accent"} year])
       (when (seq role) [:div {:class "font-mono text-[10px] text-ink-3"} role])])]
   (when (seq blurb)
     [:p {:class "text-sm text-ink-2 leading-relaxed"} blurb])
   (when (seq stack)
     [:div {:class "flex gap-1.5 flex-wrap mt-2"}
      (for [s stack]
        ^{:key s}
        [:span {:class "font-mono text-[10px] text-ink-2 border border-ink-3 rounded px-1.5 py-0.5"}
         s])])])

(defn arrow-btn [dir]
  [:button {:class (str "w-11 shrink-0 grid place-items-center rounded-lg border "
                        "border-ink-2 text-ink-2 bg-card text-xl "
                        "transition-colors hover:border-accent hover:text-accent")
            :on-click #(go! (if (= dir :prev) -1 1))}
   (if (= dir :prev) "‹" "›")])

(defn cycler []
  [:div
   [:div {:class "flex items-stretch gap-3 mt-4"}
    [arrow-btn :prev]
    ^{:key @selected-location} [project-card (current-location @selected-location)]
    [arrow-btn :next]]])

(defn init-component []
  [:div {:class "max-w-2xl mx-auto px-4 py-10"}
   [:div {:class "bg-card border-2 border-ink rounded-xl p-6 shadow-[6px_6px_0_#e3ddd0]"}
    [intro-header]
    [progress-bar]
    [map-view]
    [cycler]]])

(defn mount-root []
  (rdom/render [init-component] (.getElementById js/document "app")))

(defn ^:export init []
  (mount-root))

(defn on-js-reload []
  (mount-root))

(mount-root)
