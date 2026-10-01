package imagestudio.ui;

import imagestudio.core.GrayscaleMethod;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

// Buuin ang menu
final class MenuBarFactory {

    private MenuBarFactory() {
    }

    static JMenuBar build(StudioController controller, StudioActions actions,
                          ControlBindings bindings) {
        JMenuBar bar = new JMenuBar();
        bar.add(fileMenu(actions));
        bar.add(grayscaleMenu(controller, bindings));
        bar.add(transformMenu(controller, bindings));
        bar.add(viewMenu(actions));
        bar.add(helpMenu(actions));
        return bar;
    }

    private static JMenu fileMenu(StudioActions actions) {
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(new JMenuItem(actions.importImage));
        menu.add(new JMenuItem(actions.save));
        menu.addSeparator();
        menu.add(new JMenuItem(actions.reset));
        menu.addSeparator();
        menu.add(new JMenuItem(actions.exit));
        return menu;
    }

    /* Piliin ang grayscale */
    private static JMenu grayscaleMenu(StudioController controller, ControlBindings bindings) {
        JMenu menu = new JMenu("Grayscale");
        menu.setMnemonic(KeyEvent.VK_G);
        ButtonGroup group = new ButtonGroup();

        for (GrayscaleMethod method : GrayscaleMethod.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(method.label());
            item.setToolTipText(method.formula());
            item.setAccelerator(KeyStroke.getKeyStroke(method.accelerator()));
            item.addActionListener(e -> controller.applyGrayscale(method));
            group.add(item);
            menu.add(item);
            bindings.bindGrayscale(method, item);
        }
        return menu;
    }

    /* Mga pangbaliktad */
    private static JMenu transformMenu(StudioController controller, ControlBindings bindings) {
        JMenu menu = new JMenu("Image Transformations");
        menu.setMnemonic(KeyEvent.VK_T);

        JCheckBoxMenuItem horizontal =
                new JCheckBoxMenuItem("Flip Horizontal  -  about vertical axis");
        horizontal.setToolTipText("Reflects the image across its vertical axis");
        horizontal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0));
        horizontal.addActionListener(e -> controller.toggleFlipHorizontal());

        JCheckBoxMenuItem vertical =
                new JCheckBoxMenuItem("Flip Vertical  -  about horizontal axis");
        vertical.setToolTipText("Reflects the image across its horizontal axis");
        vertical.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));
        vertical.addActionListener(e -> controller.toggleFlipVertical());

        menu.add(horizontal);
        menu.add(vertical);
        bindings.bindFlipHorizontal(horizontal);
        bindings.bindFlipVertical(vertical);
        return menu;
    }

    private static JMenu viewMenu(StudioActions actions) {
        JMenu menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        menu.add(new JMenuItem(actions.fit));
        menu.add(new JMenuItem(actions.actualSize));
        menu.addSeparator();
        menu.add(new JMenuItem(actions.zoomIn));
        menu.add(new JMenuItem(actions.zoomOut));
        menu.addSeparator();
        menu.add(new JMenuItem(actions.fullScreen));
        return menu;
    }

    private static JMenu helpMenu(StudioActions actions) {
        JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.add(new JMenuItem(actions.formulas));
        menu.add(new JMenuItem(actions.about));
        return menu;
    }
}
