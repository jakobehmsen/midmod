package reo.runtime;

public class PutSlotAdapter extends AbstractObservable implements Observer {
    private Object value;

    @Override
    protected void sendStateTo(Observer observer) {
        if(value != null)
            observer.handle(value);
    }

    @Override
    public void handle(Object value) {
        if(value instanceof Dictionary.PutSlotChange) {
            this.value = ((Dictionary.PutSlotChange) value).getNewValue();
            sendChange(value);
        }
    }
}
