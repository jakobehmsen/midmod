package jorch;

import java.util.ArrayList;
import java.util.List;

public class ProcedureList {
    private List<Procedure> procedures;
    private ArrayList<String> startedProcedures = new ArrayList<>();

    public ProcedureList(List<Procedure> procedures) {
        this.procedures = procedures;
    }

    public List<Procedure> getProcedures() {
        return procedures;
    }

    public void startProcedure(String name) {
        startedProcedures.add(name);
    }

    public ArrayList<String> getStartedProcedures() {
        return startedProcedures;
    }
}
