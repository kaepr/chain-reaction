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

(defn routes [{:keys [*rooms db]}]
  (ring/router
    [["/" {:get #'handler/home-page
           :middleware [mid/wrap-redirect-logged-in]}]
     ["/sign-up" {:get {:handler #'handler/sign-up-page
                        :middleware [mid/wrap-redirect-logged-in]}
                  :post {:handler #'handler/sign-up}}]
     ["/sign-in" {:get {:handler #'handler/sign-in-page
                        :middleware [mid/wrap-redirect-logged-in]}
                  :post #'handler/sign-in}]
     ["/logout" {:post #'handler/logout}]
     ["/room" {:middleware [mid/wrap-logged-in]}
      ["/create" {:post {:handler #'handler/create-room}}]
      ["/join" {:post {:handler #'handler/join-room}}]
      ["/play/:id" {:get {:handler #'handler/room-page}}]]
     ["/dashboard" {:get {:handler #'handler/dashboard-page}
                    :middleware [mid/wrap-logged-in]}]]
    {:data {:db db
            :middleware [#(mid/wrap-db % db)
                         #(mid/wrap-rooms % *rooms)
                         mid/wrap-render-rum]}}))
                         
