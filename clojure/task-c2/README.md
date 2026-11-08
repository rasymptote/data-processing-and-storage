# Task C2

Define the infinite sequence of prime numbers. Use Sieve of Eratosthenes algorithm with infinite cap.
Cover code with unit tests.

## Usage

[Leiningen](https://leiningen.org/) must be installed.

```
cd clojure/task-c2
lein repl
```

```clojure
(take 10 primes)
;; => (2 3 5 7 11 13 17 19 23 29)

(nth primes 99)
;; => 541
```

## Tests

```
cd clojure/task-c2
lein test
```