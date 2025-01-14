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

(defn find-matches-for-user-id [db user-id]
  (jdbc/execute!
   db
   (sql/format {:select [[:m.created_at :created_at]
                         [:m.match_result :match_result]
                         [:u2.username :player_2]
                         [:w.username :winner]
                         [:u1.username :player_1]]
                :from [[:matches :m]]
                :where [:or [:= :m.player_1 user-id] [:= :m.player_2 user-id]]
                :join [[:users :u1] [:= :m.player_1 :u1.id]
                       [:users :u2] [:= :m.player_2 :u2.id]]
                :left-join [[:users :w] [:= :m.winner :w.id]]
                :order-by [[:m.created_at :desc]]})
   {:builder-fn rs/as-unqualified-kebab-maps}))

(defn create-match [db {:keys [user-id-1 user-id-2 winner-id]}]
  (let [sql (if winner-id
              (sql/format {:insert-into [:matches]
                           :columns [:player_1 :player_2 :winner :match_result]
                           :values [[user-id-1 user-id-2 winner-id "finished"]]})
              (sql/format {:insert-into [:matches]
                           :columns [:player_1 :player_2 :match_result]
                           :values [[user-id-1 user-id-2 "dnf"]]}))]
    (jdbc/execute! db sql)))

