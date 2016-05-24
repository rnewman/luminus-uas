(defproject luminus-uas "0.1.0-SNAPSHOT"
  :description "Simple Tofino user agent service."
  :url "https://github.com/rnewman/luminus-uas"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring-middleware-format "0.7.0"]
                 [ring/ring-json "0.4.0"]
                 [metosin/ring-http-response "0.6.5"]
                 [bouncer "1.0.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [compojure "1.5.0"]
                 [ring/ring-defaults "0.2.0"]
                 [ring-cors "0.1.7"]
                 [luminus/ring-ttl-session "0.3.1"]
                 [mount "0.1.10"]
                 [cprop "0.1.7"]
                 [org.clojure/tools.cli "0.3.5"]
                 [luminus-nrepl "0.1.4"]
                 [com.datomic/datomic-free "0.9.5359" :exclusions [org.slf4j/log4j-over-slf4j org.slf4j/slf4j-nop]]
                 [metosin/compojure-api "1.1.0"]
                 [luminus-log4j "0.1.3"]
                 [luminus-http-kit "0.1.3"]]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src/clj"]
  :resource-paths ["resources"]

  :main luminus-uas.core

  :plugins [[lein-cprop "1.0.1"]]
  :target-path "target/%s/"
  :profiles
  {:uberjar {:omit-source true
             
             :aot :all
             :uberjar-name "luminus-uas.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}
   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]
   :project/dev  {:dependencies [[prone "1.1.1"]
                                 [ring/ring-mock "0.3.0"]
                                 [ring/ring-devel "1.4.0"]
                                 [pjstadig/humane-test-output "0.8.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.14.0"]]
                  
                  
                  :source-paths ["env/dev/clj" "test/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/dev/resources" "env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
