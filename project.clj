(defproject loopme/metrics-datadog-clj "0.1.1"
            :description ""
            :url "http://loopme.biz"
            :license {:name "MIT license"
                      :url  "http://opensource.org/licenses/MIT"}
            :source-paths ["src/clojure"]
            :java-source-paths ["src/java"]
            :resource-paths ["resources"]
            :javac-options ["-target" "1.7"
                            "-source" "1.7"
                            "-Xlint:-options"]
            :plugins [[s3-wagon-private "1.1.2"]]
            :warn-on-reflection true
            :repositories [["loopme" {:url           "s3p://lm-artifacts/releases/"
                                      :username      :env
                                      :passphrase    :env
                                      :sign-releases false}]]
            :dependencies [[org.clojure/clojure "1.5.1"]
                           [org.coursera/metrics-datadog "1.0.2"]])
