package newlayer;

import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) throws ScriptException {
        Product product = new Product();

        product.setSource(
            "addLayer('Pre persistence')\n" +
            "addLayer('Persistence')\n");

        product.getLayer("Pre persistence").setSource(
            "addClass('Person')\n" +
            "addClass('Address')\n"
        );

        JFrame frame = new JFrame("NewLayer");

        JPanel contentPane = (JPanel) frame.getContentPane();

        JTabbedPane tabbedPane = new JTabbedPane();

        Overview overview = new Overview() {
            @Override
            public void open(Resource resource) {
                tabbedPane.add(resource.getName(), resource.toView());
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
