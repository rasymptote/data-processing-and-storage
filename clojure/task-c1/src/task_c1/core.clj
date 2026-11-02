(ns task-c1.core)

(defn prefix-all
  [prefixes strings]
  (reduce concat
          (map (fn [prefix]
                 (map (fn [s] (str prefix s)) strings))
               prefixes)))

(defn starts-with-repeat?
  [s]
  (= (first s)
     (first (next s))))

(defn no-repeats
  [n alphabet]
  (reduce (fn [strings letters]
            (remove starts-with-repeat? (prefix-all letters strings)))
          (repeat n alphabet)))