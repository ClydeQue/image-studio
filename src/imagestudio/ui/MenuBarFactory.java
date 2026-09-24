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

/**
 * ============================================================================
 *  MenuBarFactory
 *  ---------------------------------------------------------------------------
 *  PURPOSE : Builds the menu bar, which is the "menu-driven interface" the
 *            project asks for. Every operation in the program is reachable
 *            from here, and every control it creates registers itself with
 *            ControlBindings so one place keeps them all in step.
 *
 *  ABOUT THE KEYBOARD SHORTCUTS
 *            The grayscale methods use bare digits and the flips use bare
 *            letters, the way image editors do. The two are built differently
 *            on purpose.
 *
 *            KeyStroke.getKeyStroke('H') builds a KEY_TYPED stroke for the
 *            character capital H, and Swing matches a typed event by its
 *            character. Typing plain h produces the character h, which is not
 *            equal to H, so that shortcut would silently need Shift. Letters
 *            therefore use a key CODE instead, which is about the physical key
 *            and ignores the shift state.
 *
 *            Digits keep the character form. They are unaffected by Shift, and
 *            the character form has the advantage of matching the number pad
 *            as well as the top row, which a key code would not.
 *
 *  Author : Clyde
 * ============================================================================
 */
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

    /**
     * The grayscale selector. A radio group means exactly one method is
     * selected at all times and the interface always shows which, which is the
     * requirement that the user be able to choose the method.
     */
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

    /**
     * The two flips.
     * <p>
     * The menu text names the direction the pixels move and the tooltip names
     * the mirror axis, because the two conventions disagree: "flip horizontal"
     * usually means a left-to-right mirror, which is a reflection across the
     * VERTICAL axis. Stating both removes the ambiguity instead of picking a
     * side and hoping the reader shares it.
     */
    private static JMenu transformMenu(StudioController controller, ControlBindings bindings) {
        JMenu menu = new JMenu("Image Transformations");
        menu.setMnemonic(KeyEvent.VK_T);

        JCheckBoxMenuItem horizontal =
                new JCheckBoxMenuItem("Flip Horizontal  -  mirror left to right");
        horizontal.setToolTipText("Reflects the image across its vertical axis");
        horizontal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0));
        horizontal.addActionListener(e -> controller.toggleFlipHorizontal());

        JCheckBoxMenuItem vertical =
                new JCheckBoxMenuItem("Flip Vertical  -  mirror top to bottom");
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
