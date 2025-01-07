(ns portal
  (:require [portal.api :as inspect]))

(def instance
  "Open portal window if no portal sessions have been created.
   A portal session is created when opening a portal window"
  (or (seq (inspect/sessions))
      (inspect/open)))

;; Add portal as tapsource (add to clojure.core/tapset)
(add-tap #'portal.api/submit)

(comment
  ;; Clear all values in the portal inspector window
  (inspect/clear)
  ;; Close the inspector
  (inspect/close))
