package imagestudio.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

// Mga shared command
final class StudioActions {

    /* Shortcut ng platform */
    private static final int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    final Action importImage;
    final Action save;
    final Action reset;
    final Action fit;
    final Action actualSize;
    final Action zoomIn;
    final Action zoomOut;
    final Action fullScreen;
    final Action exit;
    final Action formulas;
    final Action about;

    StudioActions(StudioController controller, ImagePreviewPanel preview,
                  Window owner, ControlBindings bindings) {

        importImage = build("Import Image...", "Open an image file (PNG, JPG, GIF or BMP)",
                KeyEvent.VK_I, KeyStroke.getKeyStroke(KeyEvent.VK_O, MENU_MASK),
                e -> controller.importImage());

        save = build("Save Image As...", "Save the image exactly as previewed",
                KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_S, MENU_MASK),
                e -> controller.save());

        reset = build("Reset to Original", "Clear every applied method",
                KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_R, MENU_MASK),
                e -> controller.reset());

        fit = build("Fit to Window", "Scale the image to fit the window",
                KeyEvent.VK_F, KeyStroke.getKeyStroke(KeyEvent.VK_0, MENU_MASK),
                e -> preview.setFitToWindow(true));

        actualSize = build("Actual Size (100%)", "Show one image pixel per screen pixel",
                KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_1, MENU_MASK),
                e -> preview.zoomToActualSize());

        zoomIn = build("Zoom In", "Zoom in. Cmd or Ctrl with the mouse wheel also works",
                KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, MENU_MASK),
                e -> preview.zoomIn());

        zoomOut = build("Zoom Out", "Zoom out. Cmd or Ctrl with the mouse wheel also works",
                KeyEvent.VK_U, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, MENU_MASK),
                e -> preview.zoomOut());

        fullScreen = build("Full Screen", "Fill the whole screen with the window, choose again to leave",
                KeyEvent.VK_S, fullScreenKey(), e -> toggleFullScreen(owner));

        exit = build("Exit", "Close the program",
                KeyEvent.VK_X, KeyStroke.getKeyStroke(KeyEvent.VK_Q, MENU_MASK),
                e -> owner.dispose());

        formulas = build("Grayscale Formulas...", "The maths behind the three methods",
                KeyEvent.VK_G, null, e -> HelpDialogs.showFormulas(owner));

        about = build("About Image Studio...", "What this program is and how it works",
                KeyEvent.VK_B, null, e -> HelpDialogs.showAbout(owner));

        /* Irehistro ang dependencies */
        bindings.bindActionNeedsSavable(save);
        bindings.bindActionNeedsModification(reset);
        bindings.bindActionNeedsImage(fit);
        bindings.bindActionNeedsImage(actualSize);
        bindings.bindActionNeedsImage(zoomIn);
        bindings.bindActionNeedsImage(zoomOut);
    }

    /* Shortcut sa fullscreen */
    private static KeyStroke fullScreenKey() {
        boolean mac = System.getProperty("os.name", "").toLowerCase().contains("mac");
        return mac
                ? KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK)
                : KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
    }

    /* Buong screen mode */
    private static void toggleFullScreen(Window window) {
        GraphicsDevice screen = window.getGraphicsConfiguration().getDevice();
        if (screen.isFullScreenSupported()) {
            boolean isFull = screen.getFullScreenWindow() == window;
            screen.setFullScreenWindow(isFull ? null : window);
        } else if (window instanceof Frame frame) {
            boolean isMax = (frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
            frame.setExtendedState(isMax ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
        }
        window.validate();
    }

    private static Action build(String name, String tip, int mnemonic,
                                KeyStroke accelerator, Consumer<ActionEvent> handler) {
        AbstractAction action = new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent event) {
                handler.accept(event);
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, tip);
        action.putValue(Action.MNEMONIC_KEY, mnemonic);
        if (accelerator != null) {
            action.putValue(Action.ACCELERATOR_KEY, accelerator);
        }
        return action;
    }
}
