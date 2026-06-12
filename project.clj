(defproject jw-intro-map "0.1.0-SNAPSHOT"
  :description "A demo using reagent to describe my GIS experience in clojure"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.9.1"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.773"]
                 [org.clojure/core.async  "0.4.500"]
                 [reagent "2.0.1"]
                 [cljsjs/react "18.3.1-1"]
                 [cljsjs/react-dom "18.3.1-1"]

  :plugins [[lein-figwheel "0.5.20"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-shell "0.5.0"]

  :source-paths ["src"]

  ;; Tailwind CSS (v4) via the pnpm-installed @tailwindcss/cli.
  ;; Input lives in resources/css/tailwind.css; output overwrites the
  ;; style.css that index.html links and figwheel watches.
  :aliases {"tailwind"       ["shell" "node_modules/.bin/tailwindcss"
                              "-i" "resources/css/tailwind.css"
                              "-o" "resources/public/css/style.css"]
            "tailwind-min"   ["shell" "node_modules/.bin/tailwindcss"
                              "-i" "resources/css/tailwind.css"
                              "-o" "resources/public/css/style.css" "--minify"]
            "tailwind-watch" ["shell" "node_modules/.bin/tailwindcss"
                              "-i" "resources/css/tailwind.css"
                              "-o" "resources/public/css/style.css" "--watch"]
            ;; one-shot CSS build, then start figwheel (run tailwind-watch in a
            ;; second terminal if you're adding classes during the session)
            "dev"            ["do" ["tailwind"] ["figwheel"]]
            ;; production: minified CSS, then the advanced cljs build
            "build"          ["do" ["tailwind-min"] ["cljsbuild" "once" "min"]]}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                :figwheel {:on-jsload "jw-intro-map.core/on-js-reload"
                           :open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main jw-intro-map.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/jw_intro_map.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/jw_intro_map.js"
                           :main jw-intro-map.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]} ;; watch and update CSS

  :profiles {:dev [:project/dev :profiles/dev]
             :profiles/dev {}
             :project/dev {:dependencies [[binaryage/devtools "1.0.0"]
                                          [figwheel-sidecar "0.5.20"]]
                           ;; need to add dev source path here to get user.clj loaded
                           :source-paths ["src" "dev"]
                           ;; need to add the compiled assets to the :clean-targets
                           :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                             :target-path]}})
