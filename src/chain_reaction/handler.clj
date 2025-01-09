(ns chain-reaction.handler
  (:require [ring.util.response :as resp]
            [chain-reaction.ui :as ui]
            [ring.websocket :as ws]
            [clojure.data.json :as json]
            [buddy.hashers :as bh]
            [chain-reaction.db :as db]
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
        _ (tap> username)
        _ (tap> password)
        {:keys [user okay]} (db/find-user-by-name db username)]
    (if (and user okay)
      (let [_ (tap> user)
            _ (tap> "hello")
            _ (tap> (:password user))
            password-match? (:valid (bh/verify password (:password user)))
            _ (tap> password-match?)]
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

(defn room [{:keys [path-params] :as req}]
  (tap> req)
  (assert (ws/upgrade-request? req))
  (let [room-id (:id path-params)]
    (tap> room-id)
    {::ws/listener
     {:on-open
      (fn [socket]
        (ws/send socket "I will echo your messages"))
      :on-message
      (fn [socket message]
        (tap> socket)
        (tap> message)
        (tap> (json/read-str message))
        (if (= message "exit")
          (ws/close socket)
          (ws/send socket message)))}}))
