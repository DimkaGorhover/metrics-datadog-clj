(defproject loopme/metrics-datadog-clj "0.1.6"
            :description "Loopme lib for working with ES"
            :url "http://loopme.biz"
            :license {:name "MIT license"
                      :url  "http://opensource.org/licenses/MIT"}
            :plugins [[s3-wagon-private "1.1.2"]]
            :repositories [["releases" {:url        "s3p://lm-artifacts/releases/"
                                        :username   :env
                                        :passphrase :env}]
                           ["snapshots" {:url        "s3p://lm-artifacts/snapshots/"
                                         :username   :env
                                         :passphrase :env}]]
            :deploy-repositories [["releases" {:url        "s3p://lm-artifacts/releases/"
                                               :username   :env
                                               :passphrase :env}]
                                  ["snapshots" {:url        "s3p://lm-artifacts/snapshots/"
                                                :username   :env
                                                :passphrase :env}]]
            :exclusions [org.clojure/clojure]
            :source-paths ["src/clojure"]
            :java-source-paths ["src/java"]
            :resource-paths ["resources"]
            :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
            :dependencies [[org.coursera/metrics-datadog "1.0.2"]]
            :profiles {:dev {:resource-paths ["test-resources"]
                             :dependencies   [[org.clojure/clojure "1.6.0"]
                                              [org.clojure/tools.trace "0.7.8"]]}})
