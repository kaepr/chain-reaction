(ns chain-reaction.handler
  (:require [ring.util.response :as resp]
            [chain-reaction.ui :as ui]
            [ring.websocket :as ws]
            [clojure.data.json :as json]
            [rum.core :as rum]
            [chain-reaction.entity :as entity]
            [chain-reaction.room :as room]
            [buddy.hashers :as bh]
            [chain-reaction.db :as db]
            [chain-reaction.entity :as e]
            [clojure.tools.logging :as log]))
            
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

(defn dashboard-page [req]
  (let [username (:username (:session req))]
    (ui/dashboard {:username username})))

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

(defn room-ws-handler [{:keys [path-params *rooms] :as req}]
  (let [room-id (:id path-params)
        {:keys [id username]} (:session req)]
    {::ws/listener
     {:on-open
      (fn [socket]
        (tap> socket)
        (tap> *rooms)
        (tap> id)
        (tap> username)
        (let [player (entity/->player id username socket)
              _ (tap> "room state")
              _ (tap> @*rooms)
              {:keys [error]} (room/join *rooms room-id player)]
          (if error
            (do
              (tap> error)
              (ui/on-error {:message "Failed to join the room."}))
            (let [_ (ws/send socket (rum/render-static-markup [:div {:id "hi"} "Hello"]))
                  room (get @*rooms room-id)
                  sockets (entity/player-sockets room)
                  _ (do
                      (tap> "namaste")
                      (tap> room)
                      (tap> sockets))
                  s1 (first socket)
                  s2 (second socket)
                  _ (tap> s1)
                  _ (tap> s2)]
              (when s1
                (ws/send s1 (rum/render-static-markup (ui/room-info room))))
              (when s2
                (ws/send s2 (rum/render-static-markup (ui/room-info room))))))))
      :on-message
      (fn [socket message]
        (if (= message "exit")
          (ws/close socket)
          (ws/send socket message)))}}))

(defn room-page [{:keys [path-params *rooms] :as req}]
  (if (ws/upgrade-request? req)
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
