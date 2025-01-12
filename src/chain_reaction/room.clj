(ns chain-reaction.room
  (:require [chain-reaction.game :as game]))

(defn room-id
  "Returns a unique randomly generated id."
  []
  (subs (str (random-uuid)) 0 8))

;;;; Game States
;;;;
;;;; :not-started
;;;; :player-1-turn
;;;; :player-2-turn
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

(defn join
  "Joins a player to specific room.

  Attaches the socket and user id to an empty position in the room.
  If the second player joins, update room status to `:player-1-turn`.
  Othewise, returns `{:err <reason>}`."
  [*rooms room-id socket user-id]
  (swap! *rooms (fn [rooms]
                  (let [{:keys [p1 p2]} [get rooms room-id]]
                    (cond
                      (empty? p1) (assoc rooms room-id)
                      (empty? p2) ()
                      :else {:err "Room is full. Please try another."})))))


(defn remove-empty [*rooms])

(defn player-left [*rooms user-id])

(comment

  (def r (atom {}))

  (create r "123")

  @r


  ())
