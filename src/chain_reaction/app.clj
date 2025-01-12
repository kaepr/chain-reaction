(ns chain-reaction.app
  (:require [reitit.ring :as ring]
            [chain-reaction.middleware :as mid]
            [chain-reaction.routes :as routes]))

(defn app [{:keys [db] :as opts}]
  (ring/ring-handler
   (routes/routes opts)
   #'routes/default-handler
   {:middleware [#(mid/wrap-ring-defaults % db)]}))
                 
