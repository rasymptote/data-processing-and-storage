# Task C1

Given an alphabet in form of a list containing 1-character strings and a number N. Define a function that
returns all the possible strings of length N based on this alphabet and containing no equal subsequent
characters.
Use map/reduce/filter/remove operations and basic operations for lists such as str, cons, .concat, etc.
No recursion, generators or advanced functions such as flatten!
Example: for the alphabet ("а" "b " "c") and N=2 the result bust be ("ab" "ac" "ba" "bc" "ca" "cb") up to
permutation.

## Usage

[Leiningen](https://leiningen.org/) must be installed.

```
cd clojure/task-c1
lein repl
```

```clojure
(no-repeats 3 ["a" "b" "c"])
```
