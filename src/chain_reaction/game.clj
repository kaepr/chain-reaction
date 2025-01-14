(ns chain-reaction.game
  "Namespace for the game logic.

  The game is played on a 9x6 grid.
  Each grid has a cell, which is a tuple of [mass, type]

  Mass is an integer, denoting the count of orbs.
  Keyword can be #{:empty, :red, :green}

  Cells are identified by their position on the board.

  Corners: Contains at max 1 orb.

  Edges: Contains at max 2 orbs.

  Rest: Contains at max 3 orbs.

  If a cell goes over it's maximum size, then it `explodes` to the adjacent cells.

  The explosion will occur in clockwise manner starting from the up.")

(def grid-rows 9)
(def grid-cols 6)

(defn in-bounds? [r c]
  (and (>= r 0)
       (>= c 0)
       (< r grid-rows)
       (< c grid-cols)))

(defn corner?
  [r c]
  (or (and (= r 0) (= c 0))
      (and (= r 0) (= c (dec grid-cols)))
      (and (= r (dec grid-rows)) (= c 0))
      (and (= r (dec grid-rows)) (= c (dec grid-cols)))))

(defn neighbors [r c]
  (filterv #(apply in-bounds? %)
           [[(dec r) c]
            [r (inc c)]
            [(inc r) c]
            [r (dec c)]]))

(defn edge? [r c]
  (or (= r 0)
      (= r (dec grid-rows))
      (= c 0)
      (= c (dec grid-cols))))

(defn max-mass [r c]
  (cond
    (corner? r c) 1
    (edge? r c) 2
    :else 3))

(defn make-cell
  ([] [0 :empty])
  ([mass color]
   [mass color]))

(defn valid-move?
  "Player can either move on the same color cell or empty cell."
  [board color row col]
  (let [cur-color (second (get-in board [row col]))]
    (or (= cur-color color) (= cur-color :empty))))

(defn explode [board color row col]
  (let [cell (get-in board [row col])
        mass (first cell)
        max-mass (max-mass row col)]
    (if (<= (inc mass) max-mass)
      (assoc-in board [row col] (make-cell (inc mass) color))
      (reduce (fn [board [r c]] (explode board color r c))
              (assoc-in board [row col] (make-cell))
              (neighbors row col)))))

(defn move
  "Returns updated board state after making a move. `nil` is invalid move."
  [board color row col]
  (when-let [_ (valid-move? board color row col)]
    (explode board color row col)))

(defn empty-board []
  (vec (repeat grid-rows (vec (repeat grid-cols (make-cell))))))

(defn merge-count [x y]
  {:green (+ (get x :green 0) (get y :green 0))
   :red (+ (get x :red 0) (get y :red 0))})

(defn finished? [board]
  (let [{:keys [green red]}
        (reduce
          (fn [acc row]
            (merge-count
             acc
             (reduce (fn [acc2 item]
                       (merge-count
                        acc2
                        (condp = (second item)
                          :red {:red 1}
                          :green {:green 1}
                          {})))
                     {:green 0
                      :red 0}
                     row)))
          {:green 0
           :red 0}
          board)]
    (or
     (and (zero? green) (> red 0))
     (and (zero? red) (> green 0)))))

(comment

  (-> (empty-board))

  (finished? (-> (empty-board))
           (move :green 0 0))

  ())
