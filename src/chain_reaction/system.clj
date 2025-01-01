(ns chain-reaction.system
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [ring.adapter.jetty :as jetty]))

(defn read-config []
  {:app/config (-> "config.edn"
                 io/resource
                 aero/read-config)})

(defmethod ig/expand-key :app/config
  [_ {:keys [port] :as opts}]
  {:app/server {:port port}})

(defn app [req]
  {:status 200
   :body "Hello, World, testing !"
   :headers {}})

(defmethod ig/init-key :app/server
  [_ opts]
  (let [port (:port opts)
        server (jetty/run-jetty
                 (fn [req] (app req))
                 {:port port
                  :join? false})]
    (println (str "Server started on port: " port))
    server))

(defmethod ig/halt-key! :app/server
  [_ server]
  (println "Stopping server !")
  (.stop server))
