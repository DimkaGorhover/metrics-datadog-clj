(ns loopme.metrics.metrics
  (:import [com.codahale.metrics Gauge Counter]
           [loopme.metrics Factory]))

(defn
  ^{:doc "
  usages:

  (create-gauge 1)
  (create-gauge 10.42)

  "}
  ^Gauge create-gauge [^Number value]
  (if (number? value)
    (Factory/gauge ^Number value)))

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