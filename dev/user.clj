(ns user
  (:require [babashka.fs :as fs]
            [clojure.java.shell :refer [sh]]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.config :as config]
            [nextjournal.clerk.view]
            [nextjournal.clerk.viewer :as cv]
            [shadow.cljs.devtools.api :as shadow]))

(def index
  "dev/jsxgraph/notebook.clj")

(def defaults
  {:out-path "public"
   :paths [index "dev/jsxgraph/notebook*.clj"]})

(defn start! []
  (swap! config/!resource->url
         assoc
         "/js/viewer.js" "http://localhost:8765/js/main.js")
  (clerk/serve!
   {:browse? true
    :watch-paths ["dev"]})
  (Thread/sleep 500)
  (clerk/show! index))

(defn github-pages! [opts]
  (let [opts (merge defaults opts)
        cas-url (cv/store+get-cas-url!
                 (merge opts {:ext "js"})
                 (fs/read-all-bytes "public/js/main.js"))]
    (swap! config/!resource->url assoc "/js/viewer.js" cas-url)
    (clerk/build!
     opts)))

(defn garden! [opts]
  (println "Running npm install...")
  (println
   (sh "npm" "install"))
  (shadow/release! :clerk)
  (github-pages! opts))
