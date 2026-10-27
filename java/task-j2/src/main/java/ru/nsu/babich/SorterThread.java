package ru.nsu.babich;

import java.util.concurrent.atomic.AtomicLong;

public class SorterThread extends Thread {
    private final SortableList list;
    private final long delayMillis;
    private final AtomicLong steps;

    public SorterThread(SortableList list, long delayMillis, AtomicLong steps) {
        this.list = list;
        this.delayMillis = delayMillis;
        this.steps = steps;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                list.bubblePass(() -> Thread.sleep(delayMillis), steps);
                Thread.sleep(delayMillis);
            }
        } catch (InterruptedException e) {
            interrupt();
        }
    }
}
