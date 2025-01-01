(ns chain-reaction.system
  (:require
   [aero.core :as aero]
   [clojure.tools.logging :as log]
   [clojure.java.io :as io]
   [next.jdbc.connection :as connection]
   [integrant.core :as ig]
   [ring.adapter.jetty :as jetty])
  (:import (com.zaxxer.hikari HikariDataSource)
           (org.flywaydb.core Flyway)))

(defn read-config []
  {:app/config (-> "config.edn"
                 io/resource
                 aero/read-config)})

(defmethod ig/expand-key :app/config
  [_ {:keys [server db] :as _opts}]
  {:app/server {:port (:port server)
                :db (ig/ref :app/db)}
   :app/db db})

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
    (log/info "Server started on port: " port)
    server))

(defmethod ig/init-key :app/db
  [_ opts]
  (let [jdbcUrl (connection/jdbc-url opts)
        datasource (connection/->pool HikariDataSource {:jdbcUrl jdbcUrl})
        _ (do
            (log/info "Database migrations started")
            (.migrate
             (.. (Flyway/configure)
                 (dataSource datasource)
                 (locations (into-array String ["classpath:database/migrations"]))
                 (table "schema_version")
                 (load)))
            (log/info "Database migrations are done"))]
    datasource))

(defmethod ig/halt-key! :app/db
  [_ datasource]
  (.close datasource))

(defmethod ig/halt-key! :app/server
  [_ server]
  (log/info "Stopping server !")
  (.stop server))
