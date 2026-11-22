(ns task-c3.core)

(defn parallel-filter
  [pred coll n-threads block-size]
  (->> coll
       (partition-all block-size)
       (partition-all n-threads)
       (mapcat (fn [blocks]
                 (->> blocks
                      (map #(future (doall (filter pred %))))
                      doall
                      (mapcat deref))))))

(defn heavy-even?
  "Slow predicate for performance demonstration."
  [x]
  (Thread/sleep 10)
  (even? x))

(defn demo
  "Compare sequential filter vs parallel-filter using time."
  []
  (let [n 100
        data (range n)]
    (println "Sequential filter:")
    (time (doall (filter heavy-even? data)))
    (println "Parallel filter (4 threads, block-size 10):")
    (time (doall (parallel-filter heavy-even? data 4 10)))
    nil))
