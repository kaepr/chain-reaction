(ns user
  (:require [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
            [integrant.core :as ig]
            [chain-reaction.system :as system]
            [integrant.repl :refer [set-prep! go halt reset reset-all]]))

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
