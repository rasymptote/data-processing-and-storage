package ru.nsu.babich;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SynchronizedListAdapter implements SortableList {
    private final List<String> list = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void addFirst(String value) {
        list.addFirst(value);
    }

    @Override
    public void bubblePass(Delay delay, AtomicLong steps) throws InterruptedException {
        for (int i = 0; ; i++) {
            synchronized (list) {
                if (i + 1 >= list.size()) {
                    return;
                }
                delay.await();
                steps.incrementAndGet();
                String a = list.get(i);
                String b = list.get(i + 1);
                if (a.compareTo(b) > 0) {
                    list.set(i, b);
                    list.set(i + 1, a);
                }
            }
            delay.await();
        }
    }

    @Override
    public Iterator<String> iterator() {
        synchronized (list) {
            return new ArrayList<>(list).iterator();
        }
    }
}
