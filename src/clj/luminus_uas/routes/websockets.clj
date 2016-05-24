(ns luminus-uas.routes.websockets
  (:require
    [org.httpkit.server
     :refer [send! with-channel on-close on-receive]]))

(defonce channels (atom #{}))

(defn connect! [channel]
  (swap! channels conj channel))

(defn disconnect! [channel status]
  (swap! channels #(remove #{channel} %)))

(defn ws-handler [request]
  (with-channel
    request channel
    (on-close channel (partial disconnect! channel))
    (on-receive channel
                #(send! channel {}))))

