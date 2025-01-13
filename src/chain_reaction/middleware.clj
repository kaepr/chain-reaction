(ns chain-reaction.middleware
  (:require [rum.core :as rum]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]
            [jdbc-ring-session.core :as jdbc-ring-session]
            [ring.middleware.defaults :as ring-defaults]))

(defn wrap-render-rum [handler]
  (fn [req]
    (let [response (handler req)]
      (if (vector? response)
        (-> {:status 200
             :body (str "<!DOCTYPE html>\n"
                        (rum/render-static-markup response))}
            (resp/content-type "text/html"))
        response))))

(defn wrap-db [handler db]
  (fn [req]
    (handler (assoc req :db db))))

(defn wrap-rooms [handler *rooms]
  (fn [req]
    (handler (assoc req :*rooms *rooms))))

(defn wrap-ring-defaults [handler db]
  (-> handler
      (ring-defaults/wrap-defaults (assoc-in ring-defaults/site-defaults
                                             [:session :store]
                                             (jdbc-ring-session/jdbc-store
                                                                 db
                                                                 {:table :session_store})))))

(defn wrap-redirect-logged-in [handler]
  (fn [req]
    (if (seq (get-in req [:session :username]))
      (resp/redirect "/dashboard")
      (handler req))))

(seq (get-in {:session {}} [:session :username]))

(defn wrap-logged-in [handler]
  (fn [req]
    (if (empty? (get-in req [:session :username]))
      (resp/redirect "/sign-in")
      (handler req))))
