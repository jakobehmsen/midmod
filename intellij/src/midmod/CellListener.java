package midmod;

import java.util.ArrayList;

public abstract class CellListener {
    private ArrayList<Cell> subjects = new ArrayList<>();

    public void addSubject(Cell cell) {
        subjects.add(cell);

        if(subjects.size() == 1)
            initialize();
    }

    public void removeSubject(Cell cell) {
        subjects.remove(cell);

        if(subjects.size() == 0)
            cleanup();
    }

    protected void initialize() {

    }

    protected void cleanup() {

    }

    public abstract void consumeChange(Object change);
}
