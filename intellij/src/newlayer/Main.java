package newlayer;

import com.sun.glass.events.KeyEvent;
import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) throws ScriptException {
        Product product = new Product();

        product.addLayer("Pre persistence");
        product.addLayer("Persistence");

        product.getLayer("Pre persistence").setSource(
            "addClass('Person')\n" +
            "getClass('Person').addField('firstName', 'private', 'String')\n" +
            "getClass('Person').addField('lastName', 'private', 'String')\n" +
            "getClass('Person').addMethod('toString', 'public', 'String', '', 'String str = \"test\";\\nreturn str;')\n" +
            "addClass('Address')\n"
        );

        product.getLayer("Persistence").setSource(
            "addClass('PersistenceStuff')\n" +
            "getClass('Person').getFields().forEach(function(f) {\n" +
            "    getClass('Person').addField(f.getName() + 'ForPersistence', 'private', f.getType())\n" +
            "})\n" +
            "getClass('Person').addField('extraSpecialField', 'private', 'String')\n"
        );

        JFrame frame = new JFrame("NewLayer");

        JPanel contentPane = (JPanel) frame.getContentPane();
        contentPane.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private boolean dragging;
            private int tabIndex;
            private String tabTitle;
            private JComponent tabComponent;

            @Override
            public void mousePressed(MouseEvent e) {
                int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                if(tabIndex != -1) {
                    dragging = true;
                    this.tabIndex = tabIndex;
                    tabComponent = (JComponent) tabbedPane.getComponent(tabIndex);
                    tabTitle = tabbedPane.getTitleAt(tabIndex);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(dragging) {
                    tabTitle = null;
                    tabComponent = null;
                    tabIndex = -1;
                    dragging = false;
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(dragging) {
                    int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if(tabIndex != -1 && this.tabIndex != tabIndex) {
                        if(tabIndex < this.tabIndex) {
                            tabbedPane.removeTabAt(this.tabIndex);
                            tabbedPane.insertTab(tabTitle, null, tabComponent, null, tabIndex);
                            this.tabIndex = tabIndex;
                        } else {
                            tabbedPane.removeTabAt(this.tabIndex);
                            tabbedPane.insertTab(tabTitle, null, tabComponent, null, tabIndex);
                            this.tabIndex = tabIndex;
                        }

                        tabbedPane.setSelectedIndex(this.tabIndex);
                    }
                }
            }
        };

        tabbedPane.addMouseListener(mouseAdapter);
        tabbedPane.addMouseMotionListener(mouseAdapter);

        Overview overview = new Overview() {
            private Hashtable<Resource, JComponent> openedResources = new Hashtable<>();

            @Override
            public void open(Resource resource) {
                if(openedResources.containsKey(resource)) {
                    JComponent resourceView = openedResources.get(resource);
                    int tabIndex = tabbedPane.indexOfComponent(resourceView);
                    tabbedPane.setSelectedIndex(tabIndex);
                } else {
                    JComponent resourceView = resource.toView();
                    tabbedPane.add(resource.getPath(), resourceView);
                    openedResources.put(resource, resourceView);
                    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                }
            }
        };
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, product.toOverview(overview), tabbedPane);

        splitPane.setDividerLocation(200);

        contentPane.add(splitPane, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();

        toolBar.setFloatable(false);

        toolBar.add(new AbstractAction("Change product...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                fc.setDialogTitle("Select product");

                if(fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    fc.getSelectedFile().toString();
                }
            }
        });

        toolBar.add(new AbstractAction("New product...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productName = JOptionPane.showInputDialog(frame, "Product name");

                JFileChooser fc = new JFileChooser();

                fc.setDialogTitle("Product folder");

                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if(fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    fc.getSelectedFile().toString();
                }
            }
        });

        contentPane.add(toolBar, BorderLayout.NORTH);

        JTextPane shell = new JTextPane();
        shell.setBackground(Color.BLACK);
        shell.setForeground(Color.WHITE);
        shell.setCaretColor(Color.WHITE);
        shell.registerKeyboardAction(e -> {
            String command = shell.getText();

            shell.setText("");

            ScriptEngineManager engineManager = new ScriptEngineManager();
            NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
            engine.put("addLayer", (Consumer<String>) s -> {
                product.addLayer(s);
            });
            engine.put("insertLayer", (BiConsumer<String, Integer>) (s, i) -> {
                product.insertLayer(s, i);
            });
            engine.put("removeLayer", (Consumer<String>) s -> {
                product.removeLayer(s);
            });
            /*engine.put("getLayer", (Function<String, Layer>) s -> {
                return product.getLayer(s);
            });*/
            try {
                engine.eval(command);
            } catch (ScriptException e1) {
                e1.printStackTrace();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        contentPane.add(shell, BorderLayout.SOUTH);

        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
