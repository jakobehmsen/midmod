package jorch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProcedureList {
    private List<Procedure> procedures;
    private ArrayList<Runnable> waiting = new ArrayList<>();
    private ArrayList<String> startedProcedures = new ArrayList<>();
    private Consumer<TaskSelector> executor;

    public ProcedureList(Consumer<TaskSelector> executor, List<Procedure> procedures) {
        this.executor = executor;
        this.procedures = procedures;
    }

    public void requestHalt(Runnable activator) {
        waiting.add(activator);
    }

    public List<Procedure> getProcedures() {
        return procedures;
    }

    public void startProcedure(String name) {
        TaskSelector taskSelector = procedures.stream().filter(x -> x.getName().equals(name)).findFirst().get().getTaskSelector();
        executor.accept(taskSelector);
        /*RepositoryBasedToken token = repository.newToken(taskSelector);
        executorService.execute(() -> {
            token.proceed();
        });*/
    }

    public List<String> getStartedProcedures() {
        // Should be invokable from the client
        return waiting.stream().map(x -> x.toString()).collect(Collectors.toList());
    }
}
