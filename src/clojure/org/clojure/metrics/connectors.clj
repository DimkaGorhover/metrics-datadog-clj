(ns org.clojure.metrics.connectors
  (:refer-clojure :exclude [replace])
  (:import [org.coursera.metrics.datadog.transport Transport HttpTransport$Builder UdpTransport$Builder]
           [com.codahale.metrics MetricRegistry Metric Clock MetricFilter]
           [org.coursera.metrics.datadog DatadogReporter DatadogReporter$Builder MetricNameFormatter]
           [java.util.concurrent TimeUnit])
  (:require [clojure.string :refer [blank?]]))

(defn
  ^{:doc "
  config for example:

 {:prefix nil
  :host   \"localhost\"
  :port   8125}

  "}
  ^Transport create-udp-transport
  ([]
    (create-udp-transport {}))
  ([{:keys [prefix host port]}]
    (let [builder (UdpTransport$Builder.)]
      (if (-> prefix blank? not)
        (.withPrefix builder prefix))
      (if (-> host blank? not)
        (.withStatsdHost builder host))
      (if (-> port number?)
        (.withPort builder port))
      (.build builder))))

(defn
  ^{:doc "
  config for example:

  {:api-key         \"FSDF98SYDF7YW79F9\"
   :connect-timeout 5000
   :socket-timeout  5000}

   "}
  ^Transport create-http-transport
  ([]
    (create-http-transport nil))
  ([{:keys [api-key connect-timeout socket-timeout]}]
    (if (blank? api-key)
      (throw (IllegalArgumentException. "api-key must be setted")))
    (let [builder (HttpTransport$Builder.)]
      (.withApiKey builder api-key)
      (if (and (number? connect-timeout)
               (< 0 connect-timeout))
        (.withConnectTimeout builder connect-timeout))
      (if (and (number? socket-timeout)
               (< 0 socket-timeout))
        (.withSocketTimeout builder socket-timeout))
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
  ([{:keys [registry metric name]}]
    (if name
      (register-metric registry name metric)
      (register-metric registry metric)))
  ([^MetricRegistry registry ^Metric metric]
    (chain-register-metric registry metric)
    metric)
  ([^MetricRegistry registry ^String name ^Metric metric]
    (chain-register-metric registry name metric)
    metric))

(defn ^DatadogReporter create-datadog-reporter
  [{:keys [^MetricRegistry metric-registry
           ^Transport transport
           ^Clock clock
           ^TimeUnit rate-unit
           ^TimeUnit duration-unit
           ^MetricFilter metric-filter
           ^MetricNameFormatter metric-name-formatter
           tags]
    :or   {^MetricRegistry metric-registry            nil
           ^Transport transport                       nil
           ^Clock clock                               nil
           ^TimeUnit rate-unit                        nil
           ^TimeUnit duration-unit                    nil
           ^MetricFilter metric-filter                nil
           ^MetricNameFormatter metric-name-formatter nil
           tags                                       '()}}]
  (if (not metric-registry)
    (throw (IllegalArgumentException. "metric-registry must be not nil")))
  (if (not transport)
    (throw (IllegalArgumentException. "transport must be not nil")))
  (let [^DatadogReporter$Builder builder (-> metric-registry
                                             DatadogReporter/forRegistry
                                             (.withTransport ^Transport transport)
                                             (.withTags tags))]
    (if clock
      (.withClock builder ^Clock clock))
    (if rate-unit
      (.convertRatesTo builder ^TimeUnit rate-unit))
    (if duration-unit
      (.convertDurationsTo builder ^TimeUnit duration-unit))
    (if metric-filter
      (.filter builder ^MetricFilter metric-filter))
    (if metric-name-formatter
      (.withMetricNameFormatter builder ^MetricNameFormatter metric-name-formatter))
    (.build builder)))

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

(comment
  ; example
  (let [transport (create-udp-transport)
        metric-registry (create-metric-registry)
        reporter (-> {:metric-registry metric-registry
                      :transport       transport
                      :tags            '("app:api_ads" "test:true")}
                     create-datadog-reporter)]
    (start-reporter reporter)
    (stop-reporter reporter)))
