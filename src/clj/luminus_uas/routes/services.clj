(ns luminus-uas.routes.services
  (:require
    [prone.middleware :as prone]
    [ring.util.http-response :refer :all]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.json :refer [wrap-json-response]]
    [ring.util.response :refer [response]]
    [compojure.api.sweet :refer :all]
    [schema.core :as s]
    [luminus-uas.routes.websockets :refer [ws-handler]]
    [luminus-uas.db.core :refer
     [start-session
      end-session
      add-visit
      visited
      find-title
      conn]]))

(s/defschema PageVisit
  {:url                    s/Str
   (s/optional-key :title) s/Str
   :session                s/Int})

(s/defschema SessionStart
  {(s/optional-key :ancestor) s/Int
   :scope s/Int})

(s/defschema SessionEnd
  {:session s/Int
   (s/optional-key :reason) s/Str})

(s/defschema StarredPage
  {:location  s/Str
   :title     s/Str
   :createdAt s/Int
   :visitedAt s/Int})

(s/defschema AwesomebarMatch
  {:uri                          s/Str
   (s/optional-key :title)       s/Str
   (s/optional-key :snippet)     s/Str
   (s/optional-key :lastVisited) Long})

;* GET /visits
;* POST /visits
;* GET /recentStars
;* GET /stars
;* PUT /stars/:url
;* DELETE /stars/:url
;* GET /query
;* POST /session/start
;* POST /session/end
;* POST /pages/:url
(def service-routes
  (->
    (->
      (api
      {:swagger {:ui   "/swagger-ui"
                 :spec "/swagger.json"
                 :data {:info {:version     "1.0.0"
                               :title       "User Agent Service API"
                               :description "Tofino UA service"}}}}
      (context
        "/v1" []
        :tags []
        (GET "/diffs" [] ws-handler)

        (POST "/visits" []
              :body [visit PageVisit]
              :summary "Saves a visit to a page."
              (response (and (add-visit conn visit) {})))

        (GET "/visits" []
             :query-params [limit :- Long]
             :summary "Retrieve visited pages."
             (response (visited conn {:limit limit})))

        (POST "/session/start" []
              :body [start SessionStart]
              (response
                {:session
                 (start-session conn start)}))

        (POST "/session/end" []
              :body [end SessionEnd]
              (response (and (end-session conn end) {})))

        (GET "/title" []
             :query-params [url :- String]
             :summary "Fetch the title of the provided page."
             (let [title (find-title conn url)]
               (if (nil? title)
                 (not-found "No such URL.")
                 (.toString title))))

        (GET "/recentStars" []
             (response []))

        (GET "/stars" []
             (response []))

        (GET "/query" []
             (response []))

        (POST "/pages/:url" []
              (response []))

        ))
      wrap-json-response)
    (wrap-cors
    :access-control-allow-origin [#"null" #"^tofino://" #"^http://localhost:3000/"]
    :access-control-allow-methods [:get :put :post :delete :options])))
