(ns task-c1.core)

(defn extend-strings
      "Extends every string in `strings` by each letter of `alphabet`
       that differs from the string's last character."
      [alphabet strings]
      (reduce
        (fn [acc s]
            (concat acc
                    (map (fn [c] (str s c))
                         (remove (fn [c] (= c (str (last s)))) alphabet))))
        '()
        strings))

(defn no-repeats
      "All strings of length n over `alphabet` with no two equal adjacent chars."
      [alphabet n]
      (if (<= n 0)
        '()
        (reduce
          (fn [strings _] (extend-strings alphabet strings))
          alphabet
          (range (dec n)))))