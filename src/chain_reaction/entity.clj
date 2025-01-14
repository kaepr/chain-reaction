(ns chain-reaction.entity
  (:require [chain-reaction.game :as game]))

(def room-states #{:not-started
                   :p1-turn
                   :p2-turn
                   :unfinished
                   :processing
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
   :moves 0
   :p1 nil
   :p2 nil
   :log nil})

(defn add-log [room status message]
  (assoc room :log {:status status
                    :message message}))

(defn add-player
  "Returns updated room after adding a new player.
   Otherwise returns `{:error <reason>}`."
  [{:keys [p1 p2] :as room} {:keys [id username] :as player}]
  (cond
    (empty? p1) (-> room
                  (assoc :p1 player)
                  (add-log :info (str username " joined as player 1.")))
    (empty? p2) (if-not (= (player-id p1) (player-id player))
                  (-> room
                    (assoc :p2 player)
                    (add-log :info (str username " joined as player 2."))
                    (assoc :state :p1-turn))
                  (-> room
                      (add-log :error "Player 1 cannot be second player.")))
    :else (add-log room :error "Room already full.")))

(defn invalid-move-states [state]
  (#{:finished :unfinished :not-started} state))

(defn update-room-state [room state]
  (assoc room :state state))

(defn valid-end-state? [state]
  (#{:finished :p1-turn :p2-turn} state))

(defn player-sockets [room]
  (filterv (comp not nil?) [(get-in room [:p1 :socket]) (get-in room [:p2 :socket])]))
