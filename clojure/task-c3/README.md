# Task C3

Implement a parallel variant of filter using futures. Each future must process a block of elements, not just a
single element. The input sequence could be either finite or infinite. Thus, the implemented filter must
possess both laziness and performance improvement by utilizing parallelism.
Cover code with unit tests.
Demonstrate the efficiency using time.

## Usage

[Leiningen](https://leiningen.org/) must be installed.

```
cd clojure/task-c3
lein repl
```

```clojure
(parallel-filter even? (range 20) 4 5)
;; => (0 2 4 6 8 10 12 14 16 18)

(take 10 (parallel-filter odd? (range) 4 10))
;; => (1 3 5 7 9 11 13 15 17 19)

;; efficiency demo (sequential vs parallel, ~10ms predicate)
(demo)
```

## Tests

```
cd clojure/task-c3
lein test
```
