package ru.nsu.babich;

import java.util.concurrent.atomic.AtomicLong;

public class SorterThread extends Thread {
    private final SynchronizedLinkedList list;
    private final long delayMillis;
    private final AtomicLong steps;

    public SorterThread(SynchronizedLinkedList list, long delayMillis, AtomicLong steps) {
        this.list = list;
        this.delayMillis = delayMillis;
        this.steps = steps;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                steps.addAndGet(list.bubblePass(() -> Thread.sleep(delayMillis)));
                Thread.sleep(delayMillis);
            }
        } catch (InterruptedException e) {
            interrupt();
        }
    }
}
