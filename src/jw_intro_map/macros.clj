(ns jw-intro-map.macros)

(defmacro load-mapbox-token []
  (System/getenv "MAPBOX_PUBLIC_TOKEN"))
