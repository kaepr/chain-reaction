(ns chain-reaction.handler
  (:require [reitit.ring :as ring]))

(defn index-handler [req]
  {:status 200
   :body "Hello World"
   :headers {}})

(defn app [db]
  (ring/ring-handler
   (ring/router
    ["/" {:handler index-handler}])))
