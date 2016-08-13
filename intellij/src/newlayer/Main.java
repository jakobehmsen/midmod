package newlayer;

import javax.script.ScriptException;
import javax.swing.*;
import java.util.Hashtable;

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

        JTabbedPane tabbedPane = new JTabbedPane();

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

        contentPane.add(splitPane);

        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
