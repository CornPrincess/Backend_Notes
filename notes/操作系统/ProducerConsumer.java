package com.zte.practice;

import java.util.Random;

/**
 * @Description
 * @Author 周天斌 10252160
 * @Date 2020/7/6 11:57
 * @Version V1.0
 */
public class ProducerConsumer {
    static final int N = 100;
    static Producer p = new Producer();
    static Consumer c = new Consumer();
    static OurMonitor mon = new OurMonitor();

    public static void main(String[] args) {
        System.out.println("--------------------- start ---------------------");
        p.start();
        c.start();
    }

    static class Producer extends Thread {
        @Override
        public void run() {
            int item;
            while (true) {
                item = produce_item();
                mon.insert(item);
            }
        }

        private int produce_item() {
            int item = new Random().nextInt(100);
            System.out.println("Producer: " + item + ", count: " + mon.count);
            return item;
        }
    }

    static class Consumer extends Thread {
        @Override
        public void run() {
            int item;
            while (true) {
                item = mon.remove();
                consumer_item(item);
            }
        }

        private void consumer_item(int item) {
            System.out.println("Consumer: " + item + ", count: " + mon.count);
        }
    }

    static class OurMonitor  {
        private int[] buffer = new int[N];
        private volatile int count = 0;
        private int lo = 0;
        private int hi = 0;

        public synchronized void insert(int val) {
            if (count == N) {
                goToSleep();
            }
            buffer[hi] = val;
            hi = (hi + 1) % N;
//            count++;
            count = count + 1;
            if (count == 1) {
                notify();
            }
        }

        public synchronized int remove() {
            int val;
            if (count == 0) {
                goToSleep();
            }
            val = buffer[lo];
            lo = (lo + 1) % N;
//            count--;
            count = count - 1;
            if (count == N - 1) {
                notify();
            }
            return val;
        }

        private void goToSleep() {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
