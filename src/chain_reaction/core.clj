(ns chain-reaction.core
  (:require
   [chain-reaction.system :as system]
   [integrant.core :as ig])
  (:gen-class))

(defn -main [& _args]
  (let [s (-> (system/read-config)
              (ig/expand)
              (ig/init))]
    (.addShutdownHook
      (Runtime/getRuntime)
      (new Thread #(ig/halt! s)))))

(comment

  ())
