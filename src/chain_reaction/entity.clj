(ns chain-reaction.entity
  (:require [chain-reaction.game :as game]))

(def room-states #{:not-started
                   :p1-turn
                   :p2-turn
                   :finished})

;; Player
(defn ->player [id username socket]
  {:id id
   :username username
   :socket socket})

(defn player-id [player]
  (:id player))

(defn player-name [player]
  (:username player))

;; Room

(defn room-id
  "Returns a unique randomly generated id."
  []
  (subs (str (random-uuid)) 0 8))

(defn ->room [id]
  {:id id
   :state :not-started
   :board (game/empty-board)
   :p1 nil
   :p2 nil})

(defn add-player
  "Returns updated room after adding a new player.
   Otherwise returns `{:err <reason>}`."
  [{:keys [p1 p2] :as room} player]
  (cond
    (empty? p1) (assoc room :p1 player)
    (empty? p2) (if-not (= (player-id p1) (player-id p2))
                  (-> room
                    (assoc :p2 player)
                    (assoc :state :p1-turn))
                  {:err "Player 1 cannot be second player."})
    :else {:err "Failed to add player. Room already full."}))

(defn update-room-state [room state]
  (assoc room :state state))
