# Task C4

Implement a program simulating a production line for safes and clocks. Each clock is produced from 5 units
of lumber and 10 unit of gears, gears are produced from iron ore and so on. Most part of code is already
implemented and provided, including production line configuration. The task is to implement missing parts
marked in code explicitly.
Implementation must use agents for processors (factories) and atoms in combination with agents for
storages.
The storage’s agent must be notified by the factory that the appropriate resource is produced, the agent
puts it to the storage’s atom and notifies all the engaged processors.
The missing part in code is the processor’s notification message (a function).
See requirements for the notification message implementation inside the code provided.
Note that there is a shortage for metal. Make sure that both final wares (clocks and safes) are produced
despite this (at reduced rate of course).

## Usage

[Leiningen](https://leiningen.org/) must be installed.

```
cd clojure/task-c4
lein repl
```

```clojure
(start)
;; log lines show Safe / Cuckoo-clock amounts growing
(stop)
;; recompile the namespace to reset agents and threads
```

## Tests

```
cd clojure/task-c4
lein test
```
