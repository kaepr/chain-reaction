(ns chain-reaction.app
  (:require [reitit.ring :as ring]
            [chain-reaction.middleware :as mid]
            [chain-reaction.routes :as routes]))

(defn app [db]
  (ring/ring-handler
   (routes/routes db)
   #'routes/default-handler
   {:middleware [#(mid/wrap-ring-defaults % db)]}))
                 
