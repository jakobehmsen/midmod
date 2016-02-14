package midmod.pal.evaluation;

public class CouldNotResolveSignalHandlerException extends RuntimeException {
    private Frame frame;
    private Object signal;

    public CouldNotResolveSignalHandlerException(Frame frame, Object signal) {
        this.frame = frame;
        this.signal = signal;
    }

    @Override
    public String getMessage() {
        return "Could not resolve signal handler for signal: " + signal;
    }

    public Frame getFrame() {
        return frame;
    }

    public Object getSignal() {
        return signal;
    }
}
