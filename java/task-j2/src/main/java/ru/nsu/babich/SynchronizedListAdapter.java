package ru.nsu.babich;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SynchronizedListAdapter implements SortableList {
    private final List<String> list = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void addFirst(String value) {
        list.addFirst(value);
    }

    @Override
    public long bubblePass(Delay delay) throws InterruptedException {
        long steps = 0;
        for (int i = 0; ; i++) {
            synchronized (list) {
                if (i + 1 >= list.size()) {
                    return steps;
                }
                delay.await();
                steps++;
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
