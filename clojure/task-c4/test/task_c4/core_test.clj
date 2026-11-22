(ns task-c4.core-test
  (:require [clojure.test :refer :all]
            [task-c4.core :as c4]))

(deftest both-final-wares-produced
  (testing "safes and clocks are produced despite metal shortage"
    (c4/start)
    (Thread/sleep 40000)
    (c4/stop)
    (Thread/sleep 2000)
    (let [safes @(:storage c4/safe-storage)
          clocks @(:storage c4/cuckoo-clock-storage)]
      (println "Safes:" safes "Clocks:" clocks)
      (is (pos? safes))
      (is (pos? clocks)))))
