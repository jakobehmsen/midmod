package jorch;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class Scheduler {
    private SequentialScheduler sequentialScheduler;
    private ExecutorService executorService;

    public Scheduler(SequentialScheduler sequentialScheduler, ExecutorService executorService) {
        this.sequentialScheduler = sequentialScheduler;
        this.executorService = executorService;
    }

    public <T> TaskFuture<T> call(Consumer<SequentialScheduler> task) {
        SequentialScheduler ss = sequentialScheduler.newSequentialScheduler(task);

        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                if(((SQLSequentialScheduler)ss).hasMore()) {
                    CountDownLatch countDownLatch = new CountDownLatch(1);

                    ss.getEventHandlerContainer().addEventHandler(new SequentialSchedulerEventHandler() {
                        @Override
                        public void proceeded() {

                        }

                        @Override
                        public void finished() {
                            countDownLatch.countDown();
                            ss.getEventHandlerContainer().removeEventHandler(this);
                        }

                        @Override
                        public void wasClosed() {

                        }
                    });

                    countDownLatch.await();
                }

                return (Integer) ss.getResult();
            }
        });

        return new TaskFuture<T>() {
            @Override
            public T get() {
                try {
                    T result = (T) future.get();
                    return result;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
}
