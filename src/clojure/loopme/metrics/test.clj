(ns loopme.metrics.test
  (:require [loopme.metrics.connectors :refer :all]
            [loopme.metrics.metrics :refer :all]))

(def max-time 60)
(def min-time 30)

(defn metric-test
  ([] (metric-test {}))
  ([config]
    (let [transport (create-udp-transport {:api-key "227d8cbbbe455977dbea9f98a126d1da"})
          metric-registry (create-metric-registry)
          reporter (create-datadog-reporter metric-registry transport)
          counter (create-counter)
          const (create-gauge (Math/round (* 100 (Math/random))))
          timeout (create-gauge-fn #(- max-time (* (- max-time min-time) (Math/random))))
          queue (create-gauge-fn #(Math/round (- max-time (* (- max-time min-time) (Math/random)))))]

      (register-metric metric-registry "request_queuing" queue)

      (-> metric-registry
          (chain-register-metric "test_counter" counter)
          (chain-register-metric "test_const" const)
          (chain-register-metric "test_timeout" timeout)
          (chain-register-metric "test_queue" queue))

      (start-reporter reporter 2000)

      (inc-counter counter)
      (dec-counter counter)
      (inc-counter counter)

      (->> counter get-count (println "test count:"))

      (let [test-time (or (:test-time-ms config) (* 1000 60 5))]
        (println "test-time:" test-time "ms")
        (Thread/sleep test-time))

      (stop-reporter reporter))))

(comment

  (println "run this command for manual test")

  (metric-test)

  )