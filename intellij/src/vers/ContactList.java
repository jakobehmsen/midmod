package vers;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ContactList {
    private ArrayList<Contact> contacts = new ArrayList<>();
    private ArrayList<ContactListListener> listeners = new ArrayList<>();

    public void addContactListener(ContactListListener listener) {
        listeners.add(listener);
    }

    public void removeContactListener(ContactListListener listener) {
        listeners.remove(listener);
    }

    public Contact addContact() {
        Contact contact = new Contact("New Contact " + (contacts.size() + 1));

        contacts.add(contact);
        listeners.forEach(x -> x.addedContact(this, contact));

        return contact;
    }

    public Contact getContact(int index) {
        return contacts.get(index);
    }
}
