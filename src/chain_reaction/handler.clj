(ns chain-reaction.handler
  (:require [chain-reaction.ui :as ui]
            [ring.websocket :as ws]
            [rum.core :as rum]
            [chain-reaction.entity :as entity]
            [clojure.tools.logging :as log]
            [chain-reaction.room :as room]
            [clojure.data.json :as json]
            [buddy.hashers :as bh]
            [chain-reaction.db :as db]))

(defn home-page [{:keys [db] :as req}]
  (ui/home-page))

(defn sign-up-page [_req]
  (ui/sign-up-page (ui/sign-up-form {})))

(defn sign-up [{:keys [db params] :as _req}]
  (let [{:keys [username password confirmpassword]} params]
    (if (not= password confirmpassword)
      (ui/sign-up-form {:error "Passwords are not matching."})
      (let [hashed-password (bh/derive password)
            {:keys [user okay error]} (db/create-user db {:username username
                                                          :password hashed-password})]
        (if okay
         {:status 200
          :headers {"hx-redirect" "/dashboard"}
          :session (select-keys (into {} user) [:id :username])}
         (ui/sign-up-form {:error error}))))))

(defn sign-in-page [_req]
  (ui/sign-in-page (ui/sign-in-form {})))

(defn sign-in [{:keys [db params]}]
  (let [{:keys [username password]} params
        {:keys [user okay]} (db/find-user-by-name db username)]
    (if (and user okay)
      (let [password-match? (:valid (bh/verify password (:password user)))]
        (if password-match?
          {:status 200
           :headers {"hx-redirect" "/dashboard"}
           :session (select-keys (into {} user) [:id :username])}
          (ui/sign-in-form {:error "Invalid credentials."})))
      (ui/sign-in-form {:error (if okay
                                 "User does not exist."
                                 "Something went wrong.")}))))

(defn dashboard-page [{:keys [db session] :as req}]
  (let [{:keys [id username]} session
        matches (db/find-matches-for-user-id db id)]
    (ui/dashboard {:username username
                   :matches matches}
                  (ui/match-history matches))))

(defn logout [_req]
  {:status 200
   :session nil
   :headers {"HX-Redirect" "/"}})

(defn create-room [{:keys [*rooms] :as req}]
  (let [room-id (entity/room-id)
        new-room (entity/->room room-id)
        _ (swap! *rooms (fn [rooms] (assoc rooms room-id new-room)))]
    {:status 200
     :headers {"HX-Redirect" (str "/room/play/" room-id)}}))

(defn join-room [{:keys [*rooms params] :as req}]
  (let [room-id (:room-id params)
        {:keys [p1 p2] :as room} (get @*rooms room-id)]
    (cond
      (not room) (ui/join-room-form {:error "Room does not exist."})
      (and p1 p2) (ui/join-room-form {:error "Room already full."})
      :else {:status 200
             :headers {"HX-Redirect" (str "/room/play/" room-id)}})))

(defn room-ws-handler [{:keys [path-params *rooms db] :as req}]
  (let [room-id (:id path-params)
        {:keys [id username]} (:session req)]
    {::ws/listener
     {:on-open
      (fn [socket]
        (let [player (entity/->player id username socket)
              {:keys [log]} (room/join *rooms room-id player)]
          (if (= :error (:status log))
            (do
              (ws/send socket (rum/render-static-markup (ui/room-error-card {:message log})))
              (ws/close))
            (let [room (get @*rooms room-id)
                  sockets (entity/player-sockets room)]
              (run!
               (fn [s]
                 (ws/send s (rum/render-static-markup (ui/room-info room))))
               sockets)))))
      :on-message
      (fn [socket message]
        (let [{:keys [event-type] :as event} (update-keys (json/read-str message) keyword)
              player (entity/->player id username socket)]
          (condp = event-type
            "cell-click" (let [{:keys [log board] :as room} (room/handle-click *rooms room-id event player)
                               sockets (entity/player-sockets room)]
                           (run!
                            (fn [s]
                              (ws/send s (rum/render-static-markup (ui/room-info room)))
                              (ws/send s (rum/render-static-markup (ui/render-board board)))
                              (when (= :info (:status log))
                                (ws/send s (rum/render-static-markup (ui/game-log log)))))
                            sockets)))))
      :on-pong
      (fn [_socket _buffer])
      :on-close
      (fn [_socket _code _reason]
        (let [{:keys [p1 p2 state winner] :as room} (get @*rooms room-id)
              sockets (entity/player-sockets room)]
          (when room
            (when (entity/valid-end-state? state)
              (db/create-match db {:user-id-1 (entity/player-id p1)
                                   :user-id-2 (entity/player-id p2)
                                   :winner-id winner}))
            (run!
             (fn [s]
               (ws/send s (rum/render-static-markup (ui/room-error-card {:message "Player left. Please start a new game."})))
               (ws/close s))
             sockets)
            (swap! *rooms (fn [rooms] (dissoc rooms room-id))))))}}))

(defn room-page [{:keys [path-params *rooms] :as req}]
  (if (or (:websocket? req) (ws/upgrade-request? req))
    (room-ws-handler req)
    (let [room-id (:id path-params)
          username (:username (:session req))
          room (get @*rooms room-id)]
      (if room
        (ui/room {:id room-id
                  :username username
                  :room room})
        (ui/on-error {:status 404
                      :message "Room not found."})))))
