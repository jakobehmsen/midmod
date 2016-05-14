package vers;

import java.util.ArrayList;

public class Contact {
    private String name;
    private ArrayList<ContactListener> listeners = new ArrayList<>();

    public Contact(String name) {
        this.name = name;
    }

    public void addContactListener(ContactListener listener) {
        listeners.add(listener);
    }

    public void removeContactListener(ContactListener listener) {
        listeners.remove(listener);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

        listeners.forEach(x -> x.namedChanged(name));
    }

    @Override
    public String toString() {
        return name;
    }
}
