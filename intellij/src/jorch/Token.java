package jorch;

/**
 * Created by jakob on 04-10-16.
 */
public interface Token {
    void moveForward(Step step);
    void moveOut();
    void moveInto(Step step, Step callSite);
}
