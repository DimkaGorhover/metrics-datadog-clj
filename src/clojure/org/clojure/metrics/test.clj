(ns org.clojure.metrics.test
  (:require [org.clojure.metrics.connectors :refer :all]
            [org.clojure.metrics.metrics :refer :all]))

(def max-time 600)
(def min-time 300)

(defn metric-test
  ([] (metric-test {}))
  ([config]
   (let [transport (-> {:api-key "227d8cbbbe455977dbea9f98a126d1da"}
                       create-udp-transport)
         metric-registry (create-metric-registry)
         reporter (-> {:metric-registry metric-registry
                       :transport       transport
                       :tags            '("app:api_ads" "test:true")}
                      create-datadog-reporter)
         counter (counter:create)
         const (gauge:create (Math/round (* 100 (Math/random))))
         timeout (gauge:fn #(- max-time (* (- max-time min-time) (Math/random))))
         queue (gauge:fn #(Math/round (- max-time (* (- max-time min-time) (Math/random)))))

         timer (gauge:create)
         func (fn [a]
                (gauge:timed-macro
                  timer
                  (let [t (-> (Math/random) (* 1000) Math/round)]
                    (Thread/sleep t)
                    (str a "_" t))))

         timer2 (timer:create)
         func2 (fn [a]
                 (timer:macro
                   timer2
                   (let [t (-> (Math/random) (* 1000) Math/round)]
                     (Thread/sleep t)
                     (str a "_" t))))

         ]

     (register-metric metric-registry "request_queuing" queue)

     (-> metric-registry
         (chain-register-metric "test_counter" counter)
         (chain-register-metric "test_const" const)
         (chain-register-metric "test_timeout" timeout)
         (chain-register-metric "test_timer" timer)
         (chain-register-metric "test_timer2" timer2)
         (chain-register-metric "test_queue" queue))

     (start-reporter reporter 2000)

     (counter:inc counter)
     (counter:dec counter)
     (counter:inc counter)
     (->> counter counter:get (println "test count:"))

     (gauge:set const -1)
     (->> const gauge:get (println "test_const:"))

     (future
       (dotimes [a 1000000]
         (func a)
         (func2 a)))

     (let [test-time (or (:test-time-ms config) (* 1000 60 5))]
       (println "test-time:" test-time "ms")
       (Thread/sleep test-time))

     (stop-reporter reporter))))

(comment

  (println "run this command for manual test")

  (metric-test)

  )