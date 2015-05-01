(ns org.clojure.metrics.metrics
  (:import [com.codahale.metrics Gauge Counter Timer Clock Reservoir ExponentiallyDecayingReservoir Histogram Snapshot]
           [org.clojure.metrics SettableGauge FutureCounter FutureHistogram Metrics]
           [java.util.concurrent TimeUnit]))

(defn- is? [object clazz]
  (when (and object clazz)
    (.isAssignableFrom ^Class clazz ^Class (class object))))

(defn- timer? [timer] (is? timer Timer))
(defn- histogram? [histogram] (is? histogram Histogram))
(defn- counter? [counter] (is? counter Counter))
(defn- clock? [clock] (is? clock Clock))
(defn- gauge? [gauge] (is? gauge Gauge))
(defn- settable-gauge? [gauge] (is? gauge SettableGauge))
(defn- time-unit? [time-unit] (is? time-unit TimeUnit))

(defn- ^Reservoir reservoir:default []
  (ExponentiallyDecayingReservoir.))

; =============================================================================
; clock

(defn ^Clock clock:default [] (Metrics/defaultClock))
(defn ^TimeUnit clock:default-time-unit [] (Metrics/defaultTimeUnit))
(defn ^Clock clock:user [] (Metrics/userTimeClock))
(defn ^Clock clock:cpu [] (Metrics/cpuTimeClock))

(defn ^Long clock:tick [^Clock clock]
  (when (clock? clock)
    (.getTick ^Clock clock)))

(defn ^Long clock:time [^Clock clock]
  (when (clock? clock)
    (.getTime ^Clock clock)))

(comment

  (clock:default)
  ;=> #<UserTimeClock com.codahale.metrics.Clock$UserTimeClock@892c746>

  (clock:default-time-unit)
  ;=> #<NANOSECONDS>

  (def clock (clock:user))

  (def clock (clock:cpu))

  (def clock (clock:cpu))
  (clock:tick clock)

  (def clock (clock:cpu))

  (clock:time clock)
  ;=> (System/currentTimeMillis)

  )

; =============================================================================
; gauge

(defn ^Gauge gauge:fn [^Callable func]
  (when (fn? func)
    (let [res (func)]
      (when (number? res)
        (Metrics/gauge ^Callable func)))))

(defn ^Gauge gauge:create
  ([]
   (gauge:create 0))
  ([value]
   (or
     (when (number? value)
       (Metrics/gauge ^Number value))
     (when (fn? value)
       (gauge:fn value)))))

(defn ^Number gauge:get [^Gauge gauge]
  (when (gauge? gauge)
    (.getValue ^Gauge gauge)))

(defn ^Gauge gauge:set [^SettableGauge gauge value]
  (when (settable-gauge? gauge)
    (or
      (when (number? value)
        (.setValue ^SettableGauge gauge ^Number value))
      (when (fn? value)
        (.setValue ^SettableGauge gauge ^Callable value)))
    gauge))

(defmacro
  ^{:arglists '([gauge & body]
                 [gauge {:keys [^TimeUnit time-unit]} & body])}
  gauge:timed-macro [gauge & body]
  (if-not (and gauge (-> gauge eval gauge?))
    `(do ~@body)
    (let [{:keys [time-unit]} (first body)
          time-unit (when (time-unit? (eval time-unit))
                      time-unit)]
      `(let [start-time# (clock:tick (clock:default))]
         (try
           (do ~@body)
           (finally
             (gauge:set ~gauge
                        (.convert (or ~time-unit (clock:default-time-unit))
                                  (- (clock:tick (clock:default)) start-time#)
                                  (clock:default-time-unit)))))))))

(comment

  (gauge:create 1)
  (gauge:create 10.42)

  (gauge:fn #(Math/random))
  (gauge:fn #(* 10 (Math/random)))

  (gauge:fn (fn [] (Math/random)))
  (gauge:fn (fn [] (* 10 (Math/random))))

  (def gauge (gauge:create 1))
  (gauge:get gauge)
  ;=> 1

  (gauge:set gauge 235.23)
  (gauge:get gauge)
  ;=> 235.23

  (def gauge (gauge:create))
  (gauge:timed-macro gauge (+ 10 2))
  ;=> 12

  (gauge:get gauge)
  ;=> 418946
  )

; =============================================================================
; counter

(defn ^Counter counter:create []
  (FutureCounter.))

(defn ^Number counter:get [^Counter counter]
  (when (counter? counter)
    (.getCount ^Counter counter)))

(comment

  (def counter (counter:create))
  (println (counter:get counter))
  )

(defn ^Counter counter:inc
  ([^Counter counter]
   (when (counter? counter)
     (.inc ^Counter counter))
   counter)
  ([^Counter counter ^Long value]
   (when (counter? counter)
     (if value
       (.inc ^Counter counter value)
       (.inc ^Counter counter)))
   counter))

(comment

  (def counter (counter:create))

  (counter:inc counter)
  (counter:inc counter 10)

  (counter:inc counter nil) => (counter:inc counter)
  (counter:inc counter "asdasd") => (counter:inc counter)
  )

(defn ^Counter counter:dec
  ([^Counter counter]
   (when (counter? counter)
     (.dec ^Counter counter))
   counter)
  ([^Counter counter ^Long value]
   (when (counter? counter)
     (if (number? value)
       (.dec ^Counter counter value)
       (.dec ^Counter counter)))
   counter))

(comment

  (def counter (counter:create))

  (counter:dec counter)
  (counter:dec counter 10)

  (counter:dec counter nil)
  ;=> (dec-counter counter)
  (counter:dec counter "asdasd")
  ;=> (dec-counter counter)

  )

; =============================================================================
; timer

(defn ^Timer timer:create
  ([]
   (Timer.))
  ([^Clock clock]
   (if clock
     (timer:create (reservoir:default) clock)
     (timer:create)))
  ([^Reservoir reservoir ^Clock clock]
   (Timer. (or reservoir (reservoir:default))
           (or clock (clock:default)))))

(defn ^Timer timer:update
  (^:deprecated [^Timer timer ^Long timeout-ms]
   (timer:update timer timeout-ms nil))
  ([^Timer timer ^Long timeout ^TimeUnit time-unit]
   (if (and (timer? timer) timeout)
     (.update ^Timer timer timeout (or time-unit TimeUnit/MILLISECONDS)))
   timer))

(defmacro timer:macro [^Timer timer & body]
  (if (timer? timer)
    `(.time ^Timer ~timer ^Callable (fn [] ~@body))
    `(do ~@body)))

(comment

  (def timer (timer:create))
  (def timer (timer:create (clock:user)))

  (import '[java.util.concurrent TimeUnit])

  (def timer (timer:create))

  (timer:update timer 1000)
  (timer:update timer 1000 TimeUnit/MILLISECONDS)


  (def timer (timer:create))
  (timer:macro timer (+ 10 2))
  ;=> 12

  )

; =============================================================================
; histogram

(defn ^Histogram histogram:create
  ([]
   (histogram:create nil))
  ([^Reservoir reservoir]
   (FutureHistogram. (or reservoir (reservoir:default)))))

(defn ^Histogram histogram:update [^Histogram histogram value]
  (when (and (histogram? histogram) value)
    (.update ^Histogram histogram value)
    histogram))

(defn histogram:count [^Histogram histogram]
  (when (histogram? histogram)
    (.getCount ^Histogram histogram)))

(defn ^Snapshot histogram:snapshot [^Histogram histogram]
  (when (histogram? histogram)
    (.getSnapshot ^Histogram histogram)))

(comment

  (let [hist (histogram:create)]
    (histogram:update hist 10)
    (histogram:update hist 10)
    (histogram:update hist 20)
    (histogram:update hist 10)
    (histogram:update hist 10)

    (histogram:count hist)
    ; => 5

    (histogram:snapshot hist)

    @hist
    ;=> {:min 10.0,
    ;    :mean 12.0,
    ;    :75 15.0,
    ;    :95 20.0,
    ;    :98 20.0,
    ;    :median 10.0,
    ;    :max 20.0,
    ;    :999 20.0,
    ;    :std-dev 4.47213595499958,
    ;    :99 20.0}

    ))