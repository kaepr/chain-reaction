(ns chain-reaction.routes
  (:require [reitit.ring :as ring]
            [chain-reaction.handler :as handler]
            [chain-reaction.middleware :as mid]
            [chain-reaction.ui :as ui]))

(def default-handler
  (ring/routes
   (ring/redirect-trailing-slash-handler)
   (ring/create-resource-handler
    {:path "/"})
   (ring/create-default-handler
    {:not-found
     (constantly (#'ui/on-error {:status 404}))})))

(defn routes [db]
  (ring/router
    [["/" {:get #'handler/home-page}]
     ["/sign-up" {:get {:handler #'handler/sign-up-page}
                  :post {:handler #'handler/sign-up}}]
     ["/sign-in" {:get #'handler/sign-in-page
                  :post #'handler/sign-in}]
     ["/logout" {:post #'handler/logout}]
     ["/room" :middleware [mid/wrap-logged-in]
      ["/:id" {:get {:handler #'handler/room}}]]
     ["/dashboard" {:get #'handler/dashboard-page
                    :middleware [mid/wrap-logged-in]}]]
    {:data {:db db
            :middleware [#(mid/wrap-db % db)
                         mid/wrap-render-rum]}}))
                         
