package jorch;

import java.io.Serializable;

public interface Task extends Serializable {
    void perform(Scheduler scheduler);
}
