(ns user
  (:require
   [chain-reaction.system :as system]
   [clojure.data.json :as json]
   [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
   [integrant.core :as ig]
   [integrant.repl :refer [go halt reset reset-all set-prep!]]))

(set-prep!
  (fn []
    (ig/expand (system/read-config))))

(set-refresh-dirs "src" "resources")

(comment
  ;; integrant

  (go)
  (halt)
  (reset)
  (reset-all)

  ())

(comment
  ;; hotload libs

  #_(require '[clojure.deps.repl] :refer [add-lib add-libs sync-deps])

  #_(add-libs '{org.clojure/data.json {:mvn/version "2.5.1"}})

  #_(sync-deps)

  ())
