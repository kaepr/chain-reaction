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
   :moves 0
   :p1 nil
   :p2 nil})

(defn create
  "Creates a new room `*rooms` atom using the given id as key."
  [*rooms id]
  (swap! *rooms (fn [rooms]
                  (assoc rooms id (empty-room room-id)))))

(defn ->player [& {:keys [id socket]}]
  {:id id
   :socket socket})

(defn player-sockets [room]
  [(get-in room [:p1 :socket]) (get-in room [:p2 :socket])])

(defn join
  "Joins a player to specific room and returns the updated room.

  Attaches the socket and user id to an empty position in the room.
  If the second player joins, update room status to `:p1-turn`.

  Othewise, assocs reason for error to `:log` key."
  [*rooms room-id player]
  (swap! *rooms (fn [rooms]
                  (let [room (get rooms room-id)
                        new-room (entity/add-player room player)]
                    (assoc rooms room-id new-room))))
  (get @*rooms room-id))

(defn remove-empty [*rooms])

(defn player-left [*rooms user-id])

(defn- invalid-move? [{:keys [state p1 p2] :as room} player]
  (let [p2? (= (entity/player-id player) (entity/player-id p2))
        p1? (= (entity/player-id player) (entity/player-id p1))]
    (cond
      (entity/invalid-move-states state) {:error "Invalid move."}
      (or (and (= :p1-turn state) p2?)
          (and (= :p2-turn state) p1?)) {:error "Invalid turn."})))

(defn handle-click
  "Returns the updated room after handling player click."
  [*rooms room-id {:keys [row col] :as event} player]
  (let [f (fn [rooms]
            (let [{:keys [p1 p2 state moves board] :as room} (get rooms room-id)
                  p1? (= (entity/player-id player) (entity/player-id p1))
                  color (if p1? :green :red)
                  {:keys [error]} (invalid-move? room player)]
              (cond
                error (assoc rooms room-id (-> room
                                             (entity/add-log :info error)))
                (not (game/valid-move? board color row col)) (assoc rooms
                                                                    room-id
                                                                    (-> room
                                                                       (entity/add-log :info "Invalid move by current player.")))
                :else (let [updated-board (game/move board color row col)
                            message (str "Player " (if p1?
                                                     (entity/player-name p1)
                                                     (entity/player-name p2))
                                         " played [ " row ", " col " ].")
                            finished? (and (>= moves 2) (game/finished? updated-board))
                            new-room (-> room
                                         (assoc :board updated-board)
                                         (entity/add-log :info message)
                                         (assoc :moves (inc moves)))
                            new-room (if finished?
                                       (-> new-room
                                           (entity/update-room-state :finished)
                                           (assoc :winner (entity/player-id player)))
                                       (entity/update-room-state new-room (if (= state :p1-turn)
                                                                            :p2-turn
                                                                            :p1-turn)))]
                       (assoc rooms room-id new-room)))))]
    (get (swap! *rooms f) room-id)))

(comment

  (def r (atom {}))

  (create r "123")

  @r


  ())
