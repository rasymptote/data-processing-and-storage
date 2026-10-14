package ru.nsu.babich;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    private static final String USAGE = "Usage: main <sorter-threads> <delay-ms>";
    private static final int MAX_LINE_LENGTH = 80;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            System.err.println(USAGE);
            System.exit(1);
        }

        int threadCount = Integer.parseInt(args[0]);
        long delayMillis = Long.parseLong(args[1]);

        if (threadCount < 1 || delayMillis < 0) {
            System.err.println("sorter-threads must be positive and delay-ms must be non-negative");
            System.exit(1);
        }

        SynchronizedLinkedList list = new SynchronizedLinkedList();
        AtomicLong steps = new AtomicLong();
        List<SorterThread> sorters = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            SorterThread sorter = new SorterThread(list, delayMillis, steps);
            sorters.add(sorter);
            sorter.start();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                printList(list);
                continue;
            }
            for (int start = 0; start < line.length(); start += MAX_LINE_LENGTH) {
                list.addFirst(line.substring(start, Math.min(start + MAX_LINE_LENGTH, line.length())));
            }
        }

        for (SorterThread sorter : sorters) {
            sorter.interrupt();
        }
        for (SorterThread sorter : sorters) {
            sorter.join();
        }
        System.out.println("Total steps: " + steps.get());
    }

    private static void printList(SynchronizedLinkedList list) {
        StringBuilder builder = new StringBuilder("--- list ---\n");
        for (String value : list) {
            builder.append(value).append('\n');
        }
        builder.append("------------");
        System.out.println(builder);
    }
}
