package ru.nsu.babich;

public interface SortableList extends Iterable<String> {

    void addFirst(String value);

    /**
     * One full bubble sort pass.
     *
     * @return Number of steps made.
     */
    long bubblePass(Delay delay) throws InterruptedException;

    @FunctionalInterface
    interface Delay {
        void await() throws InterruptedException;
    }
}
