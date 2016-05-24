(ns luminus-uas.middleware
  (:require [luminus-uas.env :refer [defaults]]
            [luminus-uas.config :refer [env]]
            [ring-ttl-session.core :refer [ttl-memory-store]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in  [:session :store] (ttl-memory-store (* 60 30)))))))
