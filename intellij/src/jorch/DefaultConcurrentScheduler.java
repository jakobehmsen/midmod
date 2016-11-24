package jorch;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class DefaultConcurrentScheduler implements ConcurrentScheduler {
    private ExecutorService processor = Executors.newCachedThreadPool();
    private ArrayList<DefaultSequentialScheduler> schedulers = new ArrayList<>();

    /*protected static class DefaultTaskFuture<T> implements TaskFuture<T> {
        private DefaultSequentialScheduler sequentialScheduler;
        private CountDownLatch latch = new CountDownLatch(1);

        public void proceedToFinish() {
            sequentialScheduler.proceedToFinish();
            latch.countDown();
        }

        @Override
        public T get() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return (T) sequentialScheduler.getResult();
        }
    }*/

    @Override
    public <T> TaskFuture<T> call(Consumer<SequentialScheduler> task) {
        synchronized (processor) {
            Future<T> f = processor.submit(() -> {
                DefaultSequentialScheduler sequentialScheduler = newSequentialScheduler();
                schedulers.add(sequentialScheduler);
                sequentialScheduler.proceedToFinish();
                return (T) sequentialScheduler.getResult();
            });

            return new TaskFuture<T>() {
                @Override
                public T get() {
                    try {
                        return f.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            };
        }

        /*Future<T> f = processor.execute(() -> {
            DefaultSequentialScheduler sequentialScheduler = new DefaultSequentialScheduler();
            DefaultTaskFuture<T> future = newCall();
            futures.add(future);
            future.proceedToFinish();

            sequentialScheduler.scheduleNext(task);

            Object result = sequentialScheduler.proceedToFinish();

            future.put((T) result);
        });*/
    }

    @Override
    public void close() {
        synchronized (processor) {
            processor.execute(() -> {
                schedulers.forEach(s -> {
                    try {
                        s.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
            processor.shutdown();
        }
        wasClosed();
    }

    protected void wasClosed() {

    }

    protected DefaultSequentialScheduler newSequentialScheduler() {
        //return new DefaultSequentialScheduler(parent);
        return null;
    }
}
