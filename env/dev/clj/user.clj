(ns user
  (:require [mount.core :as mount]
            luminus-uas.core))

(defn start []
  (mount/start-without #'luminus-uas.core/repl-server))

(defn stop []
  (mount/stop-except #'luminus-uas.core/repl-server))

(defn restart []
  (stop)
  (start))


