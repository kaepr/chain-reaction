(ns chain-reaction.room
  (:require [chain-reaction.game :as game]
            [chain-reaction.entity :as entity]))

(defn room-id
  "Returns a unique randomly generated id."
  []
  (subs (str (random-uuid)) 0 8))

;;;; Game States
;;;;
;;;; :not-started
;;;; :player-1-turn
;;;; :player-2-turn
;;;; :did-not-finish
;;;; :finished
;;;;

(defn- empty-room [room-id]
  {:id room-id
   :board (game/empty-board)
   :state :not-started
   :p1 nil
   :p2 nil})

(defn create
  "Creates a new room `*rooms` atom using the given id as key."
  [*rooms id]
  (swap! *rooms (fn [rooms]
                  (assoc rooms id (empty-room room-id)))))

(empty? nil)

(defn ->player [& {:keys [id socket]}]
  {:id id
   :socket socket})

(defn add-player-to-room [])

(defn player-sockets [room]
  [(get-in room [:p1 :socket]) (get-in room [:p2 :socket])])

(defn join
  "Joins a player to specific room.

  Attaches the socket and user id to an empty position in the room.
  If the second player joins, update room status to `:p1-turn`.
  Othewise, returns `{:err <reason>}`."
  [*rooms room-id player]
  (let [[old-rooms new-rooms] (swap-vals!
                               *rooms
                               (fn [rooms]
                                 (let [room (get rooms room-id)
                                       {:keys [error] :as new-room} (entity/add-player room player)]
                                   (if error
                                     rooms
                                     (assoc rooms room-id new-room)))))
        room (get @*rooms room-id)
        {:keys [error]} (entity/add-player room player)
        changed? (= old-rooms new-rooms)]
    (if changed?
      {:okay "Player joined successfully."}
      {:error "Something went wrong while joining room."})))

(defn remove-empty [*rooms])

(defn player-left [*rooms user-id])

(comment

  (def r (atom {}))

  (create r "123")

  @r


  ())
