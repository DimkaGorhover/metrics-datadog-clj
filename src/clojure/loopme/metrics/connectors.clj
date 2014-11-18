(ns loopme.metrics.connectors
  (:refer-clojure :exclude [replace])
  (:import [org.coursera.metrics.datadog.transport Transport HttpTransport$Builder UdpTransport$Builder]
           [com.codahale.metrics MetricRegistry Metric]
           [org.coursera.metrics.datadog DatadogReporter DatadogReporter$Builder]
           [java.util.concurrent TimeUnit])
  (:require [clojure.string :refer [blank?]]))

(defn ^{:doc "
  config for example:

 {:prefix nil
  :host   \"localhost\"
  :port   8125}

  "}
      ^Transport
      create-udp-transport
  ([]
    (create-udp-transport nil))
  ([config]
    (let [builder (UdpTransport$Builder.)]
      (if (-> config :prefix blank? not)
        (.withPrefix builder (:prefix config)))
      (if (-> config :host blank? not)
        (.withStatsdHost builder (:host config)))
      (if (-> config :port number?)
        (.withPort builder (:port config)))
      (.build builder))))

(defn ^{:doc "
  config for example:

  {:api-key         \"FSDF98SYDF7YW79F9\"
   :connect-timeout 5000
   :socket-timeout  5000}

   "}
      ^Transport
      create-http-transport
  ([]
    (create-http-transport nil))
  ([config]
    (if (-> config :api-key blank?)
      (throw (IllegalArgumentException. "api-key must be setted")))
    (let [builder (HttpTransport$Builder.)]
      (.withApiKey builder (:api-key config))
      (let [^Integer connect-timeout (:connect-timeout config)]
        (if (and (number? connect-timeout)
                 (< 0 connect-timeout))
          (.withConnectTimeout builder connect-timeout)))
      (let [^Integer socket-timeout (:socket-timeout config)]
        (if (and (number? socket-timeout)
                 (< 0 socket-timeout))
          (.withSocketTimeout builder socket-timeout)))
      (.build builder))))

(defn ^MetricRegistry create-metric-registry []
  (MetricRegistry.))

(defn ^MetricRegistry chain-register-metric
  ([^MetricRegistry registry ^Metric metric]
    (if (and registry metric)
      (let [name (str (-> metric class .getSimpleName) "_" (.hashCode metric))]
        (chain-register-metric registry name metric)))
    registry)
  ([^MetricRegistry registry ^String name ^Metric metric]
    (if (and registry metric)
      (.register registry name metric))
    registry))

(defn ^Metric register-metric
  ([^MetricRegistry registry ^Metric metric]
    (chain-register-metric registry metric)
    metric)
  ([^MetricRegistry registry ^String name ^Metric metric]
    (chain-register-metric registry name metric)
    metric))

(defn ^DatadogReporter create-datadog-reporter [^MetricRegistry metric-registry ^Transport transport]
  (if (not metric-registry)
    (throw (IllegalArgumentException. "metric-registry must be not nil")))
  (if (not transport)
    (throw (IllegalArgumentException. "transport must be not nil")))
  (-> ^DatadogReporter$Builder
      (DatadogReporter/forRegistry metric-registry)
      (.withTransport ^Transport transport)
      (.convertDurationsTo ^TimeUnit TimeUnit/MILLISECONDS)
      (.build)))

(defonce ^:private DEFAULT-SCHEDULE-TIME-MS 10000)

(defn ^DatadogReporter start-reporter
  ([^DatadogReporter reporter]
    (start-reporter reporter DEFAULT-SCHEDULE-TIME-MS))
  ([^DatadogReporter reporter ms-schedule]
    (if reporter
      (.start reporter (or ms-schedule DEFAULT-SCHEDULE-TIME-MS) TimeUnit/MILLISECONDS))
    reporter))

(defn ^DatadogReporter stop-reporter [^DatadogReporter reporter]
  (if reporter
    (.stop reporter))
  reporter)