(ns luminus-uas.env
  (:require [clojure.tools.logging :as log]
            [luminus-uas.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[luminus-uas started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[luminus-uas has shutdown successfully]=-"))
   :middleware wrap-dev})
