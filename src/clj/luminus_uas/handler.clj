(ns luminus-uas.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [luminus-uas.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [luminus-uas.env :refer [defaults]]
            [mount.core :as mount]
            [luminus-uas.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    #'service-routes
    (route/not-found
      "page not found")))


(defn app [] (middleware/wrap-base #'app-routes))
