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

        if(!((SQLToken)t).hasMore()) {
            return new TaskFuture<T>() {
                @Override
                public T get() {
                    return (T) t.getResult();
                }
            };
        }

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

        executorService.execute(() -> {
            ((SQLToken) t).proceed();
        });

        return new TaskFuture<T>() {
            @Override
            public T get() {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return (T)t.getResult();
            }
        };
    }
}
