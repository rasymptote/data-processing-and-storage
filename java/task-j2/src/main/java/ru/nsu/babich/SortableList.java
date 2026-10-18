package ru.nsu.babich;

import java.util.concurrent.atomic.AtomicLong;

public interface SortableList extends Iterable<String> {

    void addFirst(String value);

    /**
     * One full bubble sort pass.
     *
     * @param steps Incremented on every step made, so steps of an interrupted pass are counted too.
     */
    void bubblePass(Delay delay, AtomicLong steps) throws InterruptedException;

    @FunctionalInterface
    interface Delay {
        void await() throws InterruptedException;
    }
}
