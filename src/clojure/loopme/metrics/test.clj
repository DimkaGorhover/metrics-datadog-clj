(ns loopme.metrics.test
  (:require [loopme.metrics.connectors :refer :all]
            [loopme.metrics.metrics :refer :all]))

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
         counter (create-counter)
         const (create-gauge (Math/round (* 100 (Math/random))))
         timeout (create-gauge-fn #(- max-time (* (- max-time min-time) (Math/random))))
         queue (create-gauge-fn #(Math/round (- max-time (* (- max-time min-time) (Math/random)))))

         timer (create-gauge)
         func (fn [a]
                (timed-gauge-macro
                  timer
                  (let [t (-> (Math/random) (* 1000) Math/round)]
                    (Thread/sleep t)
                    (str a "_" t))))

         timer2 (create-timer)
         func2 (fn [a]
                 (timed-macro
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

     (inc-counter counter)
     (dec-counter counter)
     (inc-counter counter)
     (->> counter get-count (println "test count:"))

     (set-gauge-val const -1)
     (->> const get-gauge-val (println "test_const:"))

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