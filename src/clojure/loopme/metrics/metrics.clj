(ns loopme.metrics.metrics
  (:import [com.codahale.metrics Gauge Counter Timer Clock Clock$UserTimeClock Clock$CpuTimeClock Reservoir ExponentiallyDecayingReservoir Histogram]
           [loopme.metrics Factory SettableGauge]
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

(defn- ^Reservoir default-reservoir []
  (ExponentiallyDecayingReservoir.))

; =============================================================================
; gauge

(defn ^Gauge create-gauge
  ([]
   (create-gauge 0))
  ([^Number value]
   (if (number? value)
     (Factory/gauge ^Number value))))

(comment

  (create-gauge 1)
  (create-gauge 10.42)
  )

(defn ^Gauge create-gauge-fn [^Callable func]
  (when (fn? func)
    (let [res (func)]
      (when (number? res)
        (Factory/gauge ^Callable func)))))

(comment

  (create-gauge-fn #(Math/random))
  (create-gauge-fn #(* 10 (Math/random)))

  (create-gauge-fn (fn [] (Math/random)))
  (create-gauge-fn (fn [] (* 10 (Math/random))))
  )

(defn ^Number get-gauge-val [^Gauge gauge]
  (when (gauge? gauge)
    (.getValue ^Gauge gauge)))

(defn ^Counter set-gauge-val [^SettableGauge gauge ^Number number]
  (when (and (settable-gauge? gauge) number)
    (.setValue ^SettableGauge gauge number)))

(comment

  (def gauge (create-gauge 1))
  (println (get-gauge-val gauge))
  ;=> 1

  (set-gauge-val gauge 235.23)
  (println (get-gauge-val gauge))
  ;=> 235.23
  )

(defmacro timed-gauge-macro [^Gauge gauge & body]
  (if gauge
    `(let [func# (fn [] ~@body)
           start-time# (System/nanoTime)]
       (try
         (func#)
         (finally
           (let [stop-time# (System/nanoTime)]
             (set-gauge-val ~gauge (- stop-time# start-time#))))))
    `(do ~@body)))

(comment

  (def gauge (create-gauge))
  (timed-gauge-macro gauge (+ 10 2))
  ;=> 12

  (get-gauge-val gauge)
  ;=> 418946
  )

; =============================================================================
; counter

(defn ^Counter create-counter []
  (Counter.))

(defn ^Number get-count [^Counter counter]
  (when (counter? counter)
    (.getCount ^Counter counter)))

(comment

  (def counter (create-counter))
  (println (get-count counter))
  )

(defn ^Counter inc-counter
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

  (def counter (create-counter))

  (inc-counter counter)
  (inc-counter counter 10)

  (inc-counter counter nil) => (inc-counter counter)
  (inc-counter counter "asdasd") => (inc-counter counter)
  )

(defn ^Counter dec-counter
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

  (def counter (create-counter))

  (dec-counter counter)
  (dec-counter counter 10)

  (dec-counter counter nil)
  ;=> (dec-counter counter)
  (dec-counter counter "asdasd")
  ;=> (dec-counter counter)

  )

; =============================================================================
; clock

(def ^:priavate ^Clock default-clock (Clock/defaultClock))

(defn ^Clock create-user-clock []
  (Clock$UserTimeClock.))

(comment

  (def clock (create-user-clock))
  )

(defn ^Clock create-cpu-clock []
  (Clock$CpuTimeClock.))

(comment

  (def clock (create-cpu-clock))
  )

(defn ^Long clock-tick [^Clock clock]
  (when (clock? clock)
    (.getTick ^Clock clock)))

(comment

  (def clock (create-cpu-clock))
  (clock-tick clock)
  )

(defn ^Long clock-time [^Clock clock]
  (when (clock? clock)
    (.getTime ^Clock clock)))

(comment

  (def clock (create-cpu-clock))
  (clock-time clock)
  )

; =============================================================================
; timer

(defn ^Timer create-timer
  ([]
   (Timer.))
  ([^Clock clock]
   (if clock
     (create-timer (default-reservoir) clock)
     (create-timer)))
  ([^Reservoir reservoir ^Clock clock]
   (Timer. (or reservoir (default-reservoir))
           (or clock default-clock))))

(comment

  (def timer (create-timer))

  (def clock (create-clock))
  (def timer (create-timer clock))

  )

(defn ^Timer timer-update
  ([^Timer timer ^Long timeout-ms]
   (timer-update timer timeout-ms nil))
  ([^Timer timer ^Long timeout ^TimeUnit time-unit]
   (if (and (timer? timer) timeout)
     (.update ^Timer timer timeout (or time-unit TimeUnit/MILLISECONDS)))
   timer))

(comment

  (import '[java.util.concurrent TimeUnit])

  (def timer (create-timer))

  (timer-update timer 1000)
  (timer-update timer 1000 TimeUnit/MILLISECONDS)

  )

(defmacro timed-macro [^Timer timer & body]
  (if (timer? timer)
    `(.time ^Timer ~timer
            ^Callable (fn [] ~@body))
    `(do ~@body)))

(comment

  (def timer (create-timer))
  (timed-macro timer (+ 10 2))
  ;=> 12

  )

; =============================================================================
; histogram

(defn ^Histogram create-histogram
  ([]
   (create-histogram nil))
  ([^Reservoir reservoir]
   (Histogram. (or reservoir (default-reservoir)))))

(defn ^Histogram histogram-update [^Histogram histogram value]
  (when (and (histogram? histogram) value)
    (.update ^Histogram histogram value)
    histogram))

(defn histogram-count [^Histogram histogram]
  (when (histogram? histogram)
    (.getCount ^Histogram histogram)))

(comment

  (let [hist (create-histogram)]
    (histogram-update hist 10)
    (histogram-update hist 10)
    (histogram-update hist 20)
    (histogram-update hist 10)
    (histogram-update hist 10)

    (histogram-count hist)
    ; => 5

    ))