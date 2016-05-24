(ns luminus-uas.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[luminus-uas started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[luminus-uas has shutdown successfully]=-"))
   :middleware identity})
