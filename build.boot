(def +version+ "0.1.0")

(set-env!
  :source-paths #{"src"}
  :dependencies '[[org.clojure/tools.logging   "0.4.0"]
                  [http-kit                    "2.2.0"]
                  [cheshire                    "5.8.0"]

                  ;; boot clj
                  [boot/core                   "2.7.2"           :scope "provided"]
                  [adzerk/boot-logservice      "1.2.0"           :scope "test"]
                  [adzerk/bootlaces            "0.1.13"          :scope "test"]])

(require '[adzerk.bootlaces :refer :all]
         '[clojure.tools.logging :as log]
         '[adzerk.boot-logservice :as log-service])

(bootlaces! +version+)

(def log4b
  [:configuration
   [:appender {:name "STDOUT" :class "ch.qos.logback.core.ConsoleAppender"}
    [:encoder [:pattern "%d %-5level %logger{36} - %msg%n"]]]
   [:root {:level "TRACE"}
    [:appender-ref {:ref "STDOUT"}]]])

(deftask dev []
  (alter-var-root #'log/*logger-factory*
                  (constantly (log-service/make-factory log4b)))
  (require '[miner.core :as m :refer [mine verify]]
           '[miner.bits :as bits])
  (repl))

(task-options!
  push {:ensure-branch nil}
  pom {:project     'tolitius/miner
       :version     +version+
       :description "bitcoin proof of work: hashcash based example"
       :url         "https://github.com/tolitius/miner"
       :scm         {:url "https://github.com/tolitius/miner"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})
