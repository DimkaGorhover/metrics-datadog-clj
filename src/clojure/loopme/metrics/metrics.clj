(ns loopme.metrics.metrics
  (:import [com.codahale.metrics Gauge Counter Timer Clock Clock$UserTimeClock Clock$CpuTimeClock Reservoir ExponentiallyDecayingReservoir]
           [loopme.metrics Factory SettableGauge]
           [java.util.concurrent TimeUnit]))

; =============================================================================
; gauge

(defn
  ^{:doc "
  usages:

  (create-gauge 1)
  (create-gauge 10.42)

  "}
  ^Gauge create-gauge
  ([]
   (create-gauge 0))
  ([^Number value]
   (if (number? value)
     (Factory/gauge ^Number value))))

(defn
  ^{:doc "
  function must return java.lang.Number,
  otherwise \"gauge\" not be created

  usages:

  (create-gauge-fn #(Math/random))
  (create-gauge-fn #(* 10 (Math/random)))

  (create-gauge-fn (fn [] (Math/random)))
  (create-gauge-fn (fn [] (* 10 (Math/random))))

  "}
  ^Gauge create-gauge-fn [^Callable func]
  (if (fn? func)
    (let [res (func)]
      (if (number? res)
        (Factory/gauge ^Callable func)))))

(defn
  ^{:doc "
  usages:

  (def gauge (create-gauge 1))
  (println (get-gauge-val gauge)) => 1

  (def gauge (create-gauge 235.23))
  (println (get-gauge-val gauge)) => 235.23

  "}
  ^Number get-gauge-val [^Gauge gauge]
  (if gauge
    (.getValue gauge)))

(defn
  ^{:doc "
  usages:

  (def gauge (create-gauge 1))
  (println (get-gauge-val gauge)) => 1

  (set-gauge-val gauge 235.23)
  (println (get-gauge-val gauge)) => 235.23

  "}
  ^Counter set-gauge-val [^SettableGauge gauge ^Number number]
  (if (and gauge number)
    (.setValue ^SettableGauge gauge number)))

(defmacro
  ^{:doc "
  set value in gauge in nanosec

  usages:

  (def gauge (create-gauge))
  (timed-gauge-macro gauge (+ 10 2)) => 12

  (get-gauge-val gauge) => 418946

  "}
  timed-gauge-macro [^Gauge gauge & body]
  (if gauge
    `(let [func# (fn [] ~@body)
           start-time# (System/nanoTime)]
       (try
         (func#)
         (finally
           (let [stop-time# (System/nanoTime)]
             (set-gauge-val ~gauge (- stop-time# start-time#))))))
    `(do ~@body)))

; =============================================================================
; counter

(defn ^Counter create-counter []
  (Counter.))

(defn
  ^{:doc "
  usages:

  (def counter (create-counter))
  (println (get-count counter))

  "}
  ^Number get-count [^Counter counter]
  (if counter
    (.getCount ^Counter counter)))

(defn
  ^{:doc "
  usages:

  (def counter (create-counter))

  (inc-counter counter)
  (inc-counter counter 10)

  (inc-counter counter nil) => (inc-counter counter)
  (inc-counter counter \"asdasd\") => (inc-counter counter)

  "}
  ^Counter inc-counter
  ([^Counter counter]
   (if counter
     (.inc counter))
   counter)
  ([^Counter counter ^Long value]
   (if counter
     (if value
       (.inc counter value)
       (.inc counter)))
   counter))

(defn
  ^{:doc "
  usages:

  (def counter (create-counter))

  (dec-counter counter)
  (dec-counter counter 10)

  (dec-counter counter nil) => (dec-counter counter)
  (dec-counter counter \"asdasd\") => (dec-counter counter)

  "}
  ^Counter dec-counter
  ([^Counter counter]
   (if counter
     (.dec counter))
   counter)
  ([^Counter counter ^Long value]
   (if counter
     (if (number? value)
       (.dec counter value)
       (.dec counter)))
   counter))

; =============================================================================
; clock

(def ^:priavate ^Clock default-clock (Clock/defaultClock))

(defn
  ^{:doc "
  usages:

  (def clock (create-user-clock))
  "}
  ^Clock create-user-clock []
  (Clock$UserTimeClock.))

(defn
  ^{:doc "
  usages:

  (def clock (create-cpu-clock))
  "}
  ^Clock create-cpu-clock []
  (Clock$CpuTimeClock.))

(defn
  ^{:doc "
  usages:

  (def clock (create-cpu-clock))
  (clock-tick clock)
  "}
  ^Long clock-tick [^Clock clock]
  (if clock
    (.getTick ^Clock clock)))

(defn
  ^{:doc "
  usages:

  (def clock (create-cpu-clock))
  (clock-time clock)
  "}
  ^Long clock-time [^Clock clock]
  (if clock
    (.getTime ^Clock clock)))

; =============================================================================
; timer

(defn ^:private ^Reservoir default-reservoir []
  (ExponentiallyDecayingReservoir.))

(defn
  ^{:doc "
  usages:

  (def timer (create-timer))

  (def clock (create-clock))
  (def timer (create-timer clock))

  "}
  ^Timer create-timer
  ([]
   (Timer.))
  ([^Clock clock]
   (if clock
     (create-timer (default-reservoir) clock)
     (create-timer)))
  ([^Reservoir reservoir ^Clock clock]
   (Timer. (or reservoir (default-reservoir))
           (or clock default-clock))))

(defn
  ^{:doc "
  usages:

  (import 'java.util.concurrent.TimeUnit)

  (def timer (create-timer))

  (timer-update timer 1000)
  (timer-update timer 1000 TimeUnit/MILLISECONDS)

  "}
  ^Timer timer-update
  ([^Timer timer ^Long timeout-ms]
   (timer-update timer timeout-ms nil))
  ([^Timer timer ^Long timeout ^TimeUnit time-unit]
   (if (and timer timeout)
     (.update timer timeout (or time-unit TimeUnit/MILLISECONDS)))
   timer))

(defn
  ^{:doc "
  usages:

  (def timer (create-timer))
  (timed-fn timer (fn [] (+ 10 2))) => 12

  "}
  timed-fn [^Timer timer ^Callable func]
  (if timer
    (if func
      (.time timer func))
    (if func
      (func))))

(defmacro
  ^{:doc "
  usages:

  (def timer (create-timer))
  (timed-macro timer (+ 10 2)) => 12

  "}
  timed-macro [^Timer timer & body]
  (if timer
    `(.time ~timer (fn [] ~@body))
    `(do ~@body)))
