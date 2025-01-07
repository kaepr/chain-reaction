(ns chain-reaction.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql])
  (:import [java.sql SQLException]))

(defn get-users [db]
  (jdbc/execute! db (sql/format {:select [:*]
                                 :from [:users]})))

(defn create-user [db {:keys [username password]}]
  (try
    (let [user (jdbc/execute!
                db
                (sql/format {:insert-into [:users]
                             :columns [:username :password]
                             :values [[username password]]
                             :returning :*})
                {:builder-fn rs/as-unqualified-kebab-maps})]
      {:okay true
       :user user})
    (catch SQLException e
      {:okay false
       :error (if (= (.getErrorCode e) 19) ; checking if unique constraint failed
                  "Username already exists."
                  "Something went wrong.")})
    (catch Exception _e
      {:okay false
       :error "Something went wrong."})))

(defn find-user-by-name [db username]
  {:okay true
   :user (jdbc/execute-one!
          db
          (sql/format {:select :*
                       :from [:users]
                       :where [:= :username username]})
          {:builder-fn rs/as-unqualified-kebab-maps})})
