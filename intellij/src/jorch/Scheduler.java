package jorch;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class Scheduler {
    private Token token;
    private ExecutorService executorService;

    public Scheduler(Token token, ExecutorService executorService) {
        this.token = token;
        this.executorService = executorService;
    }

    public <T> TaskFuture<T> call(Consumer<Token> task) {
        Token t = token.newToken(task);

        Future<T> future = executorService.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                if(((SQLToken)t).hasMore()) {
                    CountDownLatch countDownLatch = new CountDownLatch(1);

                    t.getEventChannel().add(new TokenListener() {
                        @Override
                        public void wasPassed() {

                        }

                        @Override
                        public void finished() {
                            countDownLatch.countDown();
                            t.getEventChannel().remove(this);
                        }

                        @Override
                        public void wasClosed() {

                        }
                    });

                    countDownLatch.await();
                }

                return (T)t.getResult();
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
