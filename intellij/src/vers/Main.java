package vers;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;

public class Main {private static void print(String str) {
        System.out.println(System.currentTimeMillis() + ": " + str);
    }

    private static Repository repository;

    public static void main(String[] args) {
        print("Start");

        print("NodeWrapper create");

        print("Frame create");

        JFrame frame = new JFrame("Vers - Tree");

        ContactList contactList = new ContactList();

        JList<Contact> contactListView = new JList<>(new DefaultListModel<Contact>() {
            {
                DefaultListModel<Contact> self = this;
                contactList.addContactListener(new ContactListListener() {
                    @Override
                    public void addedContact(ContactList contactList, Contact contact) {
                        addElement(contact);

                        contact.addContactListener(new ContactListener() {
                            @Override
                            public void namedChanged(String name) {
                                fireContentsChanged(self, indexOf(contact), indexOf(contact));
                            }
                        });
                    }
                });
            }
        });

        JPopupMenu popupMenu2 = new JPopupMenu();

        popupMenu2.add(new AbstractAction("Set name") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String newName = JOptionPane.showInputDialog("Enter new name", contactListView.getSelectedValue().getName());
                    repository.execute("getContact(" + contactListView.getSelectedIndex() + ").setName(\"" + newName + "\");");
                } catch (ScriptException e1) {
                    e1.printStackTrace();
                }
            }
        });

        contactListView.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (SwingUtilities.isRightMouseButton(me)) {
                    if(contactListView.locationToIndex(me.getPoint()) != -1) {
                        contactListView.setSelectedIndex(contactListView.locationToIndex(me.getPoint()));
                        popupMenu2.show(contactListView, me.getX(), me.getY());
                    }
                }
            }
        });

        JToolBar toolBar = new JToolBar();

        toolBar.setFloatable(false);
        toolBar.add(new AbstractAction("Add contact") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    repository.execute("addContact()");
                } catch (ScriptException e1) {
                    e1.printStackTrace();
                }
            }
        });

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(toolBar, BorderLayout.NORTH);
        frame.getContentPane().add(contactListView, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                print("Start loading thread creation");
                new Thread(() -> {
                    print("Start loading");
                    String title = frame.getTitle();
                    frame.setTitle(title + " - Loading...");
                    frame.setEnabled(false);

                    repository = Repository.load("journal2.jnl", contactList);

                    frame.setEnabled(true);
                    frame.setTitle(title);
                    print("Finish loading");
                }).run();
            }
        });

        print("Before set visible");
        frame.setVisible(true);
    }
}
