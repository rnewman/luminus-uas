# A user agent service for Tofino, backed by Datomic

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

You'll also need a running Datomic instance. Its configuration should be added to a `transactor.properties` file.

## Running Datomic

Start a Datomic transactor. You can do this in a way that also gives you a REPL:

```
cd datomic-free-0.9.5359
/usr/bin/env java -server -Xmx1g -cp `bin/classpath` jline.ConsoleRunner clojure.main -i "bin/bridge.clj" -r

(require '(datomic launcher))
(datomic.launcher/-main "/path/to/transactor.properties")
(def db-uri "datomic:free://localhost:4334/uas_dev")
(d/create-database db-uri)
```

You can also start the REST server right here if you wish:

```
(require '(datomic rest))
(datomic.rest/-main "-p" "1025" "-o" "tofino://" "dev" "datomic:free://localhost:4334/")
```

or run queries directly:

```
(def conn (d/connect db-uri))
(def db (d/db conn))
(d/q '[:find ?title :in $ ?url
       :where
       [?page :page/url ?url]
       [?page :page/title ?title]]
     db "http://foo.com/")
```

## Running the UA service

```
lein run
```

will work, but it won't create storage.

You can also launch from inside IntelliJ + Cursive, or with `lein repl`.

```
;; Configure the schema.
(luminus-uas.db.core/create-schema luminus-uas.db.core/conn)

;; Launch the app.
(luminus-uas.core/start-app [])
```

In development mode you get live code reloading when a request is processed, or manually in the IDE.

## Interesting parts of the code

Schema and queries are in `src/clj/luminus_uas/db/core.clj`. HTTP services are in `src/clj/luminus_uas/routes`. Everything else is a bog-standard Luminus app.

## License

MPL2.0.
