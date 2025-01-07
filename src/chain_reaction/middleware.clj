(ns chain-reaction.middleware
  (:require [rum.core :as rum]
            [ring.util.response :as resp]
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

(defn wrap-ring-defaults [handler db]
  (-> handler
      (ring-defaults/wrap-defaults (assoc-in ring-defaults/site-defaults
                                             [:session :store]
                                             (jdbc-ring-session/jdbc-store
                                                                 db
                                                                 {:table :session_store})))))

(defn wrap-logged-in [handler]
  (fn [req]
    (let [session (:session req)]
      (if (seq session)
        (handler req)
        (resp/redirect "/sign-in")))))
