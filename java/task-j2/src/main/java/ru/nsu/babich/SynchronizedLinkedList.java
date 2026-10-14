package ru.nsu.babich;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedLinkedList implements SortableList {
    private final Node head = new Node(null);

    @Override
    public void addFirst(String value) {
        Node node = new Node(value);
        head.lock.lock();
        try {
            node.next = head.next;
            head.next = node;
        } finally {
            head.lock.unlock();
        }
    }

    @Override
    public long bubblePass(Delay delay) throws InterruptedException {
        long steps = 0;
        Node prev = head;
        while (true) {
            Node a;
            Node b;
            boolean swapped = false;
            prev.lock.lock();
            try {
                a = prev.next;
                if (a == null) {
                    return steps;
                }
                a.lock.lock();
                try {
                    b = a.next;
                    if (b == null) {
                        return steps;
                    }
                    b.lock.lock();
                    try {
                        delay.await();
                        steps++;
                        if (a.value.compareTo(b.value) > 0) {
                            prev.next = b;
                            a.next = b.next;
                            b.next = a;
                            swapped = true;
                        }
                    } finally {
                        b.lock.unlock();
                    }
                } finally {
                    a.lock.unlock();
                }
            } finally {
                prev.lock.unlock();
            }
            prev = swapped ? b : a;
            delay.await();
        }
    }

    /**
     * The iterator keeps the last returned node locked, so the iteration must be run to the end.
     */
    @Override
    public Iterator<String> iterator() {
        return new LockingIterator();
    }

    private static class Node {
        private final String value;
        private final ReentrantLock lock = new ReentrantLock();
        private Node next;

        private Node(String value) {
            this.value = value;
        }
    }

    private class LockingIterator implements Iterator<String> {
        private Node current = head;
        private boolean finished;

        private LockingIterator() {
            current.lock.lock();
        }

        @Override
        public boolean hasNext() {
            if (finished) {
                return false;
            }
            if (current.next == null) {
                current.lock.unlock();
                finished = true;
                return false;
            }
            return true;
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Node next = current.next;
            next.lock.lock();
            current.lock.unlock();
            current = next;
            return current.value;
        }
    }
}
