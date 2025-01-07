(ns css
  (:require [clojure.java.process :as process]))

(defonce css-watch-ref (atom nil))

(defn css-watch []
  (apply process/start
         {:out :inherit
          :err :inherit} ; prints output to same process as server
         ["tailwindcss"
          "-c" "resources/tailwind.config.js"
          "-i" "resources/tailwind.css"
          "-o" "resources/public/css/main.css"
          "--watch"]))

(defn start-css-watch []
  (reset! css-watch-ref (css-watch)))

(defn stop-css-watch []
  (when-some [css-watch @css-watch-ref]
    (.destroyForcibly css-watch)
    (reset! css-watch-ref nil)))

(comment

  (start-css-watch)

  (stop-css-watch)

  ())
