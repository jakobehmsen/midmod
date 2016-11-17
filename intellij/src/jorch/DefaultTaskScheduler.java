package jorch;

import java.util.ArrayDeque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultTaskScheduler implements TaskScheduler {
    //private Task2<?> nextRunnable;
    private ExecutorService processor = Executors.newSingleThreadExecutor();
    //private ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(10);

    private static class DefaultTaskFuture<T> implements TaskFuture<T> {
        private CountDownLatch latch = new CountDownLatch(1);
        private T value;

        public void put(T result) {
            value = result;
            latch.countDown();
        }

        @Override
        public T get() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return value;
        }
    }

    @Override
    public TaskScheduler fork() {
        DefaultTaskScheduler scheduler = new DefaultTaskScheduler();

        scheduler.start();

        return scheduler;
    }

    @Override
    public <T> TaskFuture<T> schedule(Task2<T> runnable) {
        DefaultTaskFuture<T> future = new DefaultTaskFuture<>();

        processor.execute(() -> {
            future.put(runnable.perform(this));
        });

        return future;
    }

    //private Thread processor;
    private volatile boolean mergeRequested;
    private volatile boolean endRequested;
    private volatile CountDownLatch endEvent;

    public void start() {
        //processor = Executors.newSingleThreadExecutor();
        /*processor = new Thread(() -> {
            while(!endRequested) {
                int count = queue.size();

                if(mergeRequested && count == 0)
                    break;

                Runnable next = null;
                try {
                    next = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                next.run();
            }
            endEvent.countDown();
        });*/
        //processor.start();

    }

    public void waitTillStop() {
        endEvent = new CountDownLatch(1);
        try {
            endEvent.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        processor.shutdown();
        if(endEvent != null)
            endEvent.countDown();
        /*endEvent = new CountDownLatch(1);
        queue.add(() -> {
            endRequested = true;
        });
        try {
            endEvent.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        processor = null;*/
    }

    @Override
    public void proceed() {

    }

    @Override
    public boolean hasMore() {
        return false;
    }

    @Override
    public void merge() {
        stop();

        //processor.shutdown();
        /*endEvent = new CountDownLatch(1);
        mergeRequested = true;
        try {
            endEvent.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        //processor = null;
    }
}
