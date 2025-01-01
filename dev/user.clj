(ns user
  (:require
   [chain-reaction.system :as system]
   [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
   [integrant.core :as ig]
   [integrant.repl :refer [go halt reset reset-all set-prep!]]))

(set-prep!
  (fn []
    (ig/expand (system/read-config))))

(set-refresh-dirs "src" "resources")

(comment

  (go)
  (halt)
  (reset)
  (reset-all)

  ())
