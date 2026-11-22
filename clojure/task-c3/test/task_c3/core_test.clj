(ns task-c3.core-test
  (:require [clojure.test :refer :all]
            [task-c3.core :refer :all]))

(deftest parallel-filter-matches-filter-finite
  (is (= (filter even? (range 100))
         (parallel-filter even? (range 100) 4 10))))

(deftest parallel-filter-empty
  (is (= '() (parallel-filter even? [] 2 5))))

(deftest parallel-filter-preserves-order
  (is (= '(0 2 4 6 8)
         (parallel-filter even? (range 10) 3 2))))

(deftest parallel-filter-all-pass
  (is (= (range 20)
         (parallel-filter identity (range 20) 4 5))))

(deftest parallel-filter-none-pass
  (is (= '()
         (parallel-filter (constantly false) (range 20) 4 5))))

(deftest parallel-filter-lazy-infinite
  (is (= '(0 2 4 6 8)
         (take 5 (parallel-filter even? (range) 4 10)))))

(deftest parallel-filter-lazy-does-not-realize-all
  (let [realized (atom 0)
        coll (map (fn [x] (swap! realized inc) x) (range))]
    (doall (take 5 (parallel-filter even? coll 2 5)))
    (is (< @realized 50))))
