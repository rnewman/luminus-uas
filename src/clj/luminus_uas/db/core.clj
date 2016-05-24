(ns luminus-uas.db.core
  (:require [datomic.api :as d]
            [mount.core :refer [defstate]]
            [luminus-uas.config :refer [env]]))

(defn create-schema [c]
  (let [schema [{:db/id #db/id[:db.part/db]
                 :db/ident              :page/url
                 :db/valueType          :db.type/string          ; Because not all URLs are java.net.URIs.
                 :db/cardinality        :db.cardinality/one
                 :db/unique             :db.unique/identity
                 :db/doc                "A page's URL."
                 :db.install/_attribute :db.part/db}
                {:db/id #db/id[:db.part/db]
                 :db/ident              :page/title
                 :db/valueType          :db.type/string
                 :db/cardinality        :db.cardinality/one      ; We supersede as we see new titles.
                 :db/doc                "A page's title."
                 :db.install/_attribute :db.part/db}
                {:db/id #db/id[:db.part/db]
                 :db/ident              :page/visitAt
                 :db/valueType          :db.type/instant
                 :db/cardinality        :db.cardinality/many
                 :db/doc                "A visit to the page."
                 :db.install/_attribute :db.part/db}

                {:db/id #db/id[:db.part/db]
                 :db/ident              :session/startedFromAncestor
                 :db/valueType          :db.type/ref     ; To a session.
                 :db/cardinality        :db.cardinality/one
                 :db/doc                "The ancestor of a session."
                 :db.install/_attribute :db.part/db}
                {:db/id #db/id[:db.part/db]
                 :db/ident              :session/startedInScope
                 :db/valueType          :db.type/string
                 :db/cardinality        :db.cardinality/one
                 :db/doc                "The parent scope of a session."
                 :db.install/_attribute :db.part/db}
                {:db/id #db/id[:db.part/db]
                 :db/ident              :session/startReason
                 :db/valueType          :db.type/string    ; TODO: enum?
                 :db/cardinality        :db.cardinality/many
                 :db/doc                "The start reasons of a session."
                 :db.install/_attribute :db.part/db}
                {:db/id #db/id[:db.part/db]
                 :db/ident              :session/endReason
                 :db/valueType          :db.type/string    ; TODO: enum?
                 :db/cardinality        :db.cardinality/many
                 :db/doc                "The end reasons of a session."
                 :db.install/_attribute :db.part/db}

                {:db/id #db/id[:db.part/db]
                 :db/ident              :event/session
                 :db/valueType          :db.type/ref      ; To a session.
                 :db/cardinality        :db.cardinality/one
                 :db/doc                "The session in which a tx took place."
                 :db.install/_attribute :db.part/db}


                ]]
    @(d/transact c schema)))

(defn init-db [url]
  (let [c (d/connect url)]
    (create-schema c)
    c))

(defstate conn
          :start (-> env :database-url init-db)
          :stop (-> conn .release))

(defn entity [conn id]
  (d/entity (d/db conn) id))

;; Returns the session ID.
(defn start-session [conn {:keys [ancestor scope reason]
                           :or {reason "none"}}]
  (let [id (d/tempid :db.part/user)
        body
        (if ancestor
          [{:db/id         (d/tempid :db.part/tx)
            :event/session ancestor}
           {:db/id                       id
            :session/startedInScope      (.toString scope)
            :session/startReason         reason
            :session/startedFromAncestor ancestor}]
          [{:db/id                  id
            :session/startedInScope (.toString scope)
            :session/startReason    reason
            }])
        result @(d/transact conn body)]
    (d/resolve-tempid (d/db conn) (:tempids result) id)))

(defn end-session [conn {:keys [session reason]
                         :or   {reason "none"}}]
  @(d/transact
     conn
     [{:db/id         (d/tempid :db.part/tx)
       :event/session session}                              ; So meta!
      {:db/id             session
       :session/endReason reason}]))

(defn active-sessions [conn]
  (d/q '[:find ?id ?reason ?ts :in $
         :where
         [?id :session/startReason ?reason ?tx]
         [?tx :db/txInstant ?ts]
         (not-join [?id]
                   [?id :session/endReason])]
       (d/db conn)))

(defn ended-sessions [conn]
  (d/q '[:find ?id ?endReason ?ts :in $
         :where
         [?id :session/endReason ?endReason ?tx]
         [?tx :db/txInstant ?ts]]
       (d/db conn)))

(defn add-visit [conn {:keys [url title session]}]
  @(d/transact
     conn
     [{:db/id        (d/tempid :db.part/tx)
       :event/session session}
      {:db/id        (d/tempid :db.part/user)
       :page/url     url
       :page/title   title
       :page/visitAt (java.util.Date.)}]))

(defn visited [conn
               {:keys [limit]
                :or {limit 10}}]
  (map (fn [[uri title lastVisited]]
         {:uri uri :title title :lastVisited lastVisited})
       (take
         limit
         (->>
           (d/q '[:find ?uri ?title (max ?time) :in $
                  :where
                  [?page :page/url ?uri]
                  [?page :page/title ?title]
                  [?page :page/visitAt ?time]]
                (d/db conn))
           (sort-by #(nth %1 2))))))

(defn find-title [conn url]
  (d/q '[:find ?title . :in $ ?url
         :where [?page :page/url ?url] [?page :page/title ?title]]
       (d/db conn) url))