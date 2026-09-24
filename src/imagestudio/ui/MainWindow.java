package imagestudio.ui;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * ============================================================================
 *  MainWindow
 *  ---------------------------------------------------------------------------
 *  PURPOSE : Assembles the window out of the parts and keeps them in step.
 *            That is all it does.
 *
 *            The operations live in StudioController, the menu and toolbar are
 *            built by their own factories, the drawing is in the preview panel
 *            and its painter, and the readout is the status bar's business.
 *            This class puts them together and forwards what the controller
 *            announces.
 *
 *            Written this way because the alternative, one window class that
 *            holds the document, builds every widget, tracks enabled states
 *            and runs the background worker, grows past what anyone can read
 *            in one sitting and gives every one of those concerns a reason to
 *            tangle with the others.
 *
 *  Declared final so calling JFrame's methods from the constructor is safe:
 *  with no possible subclass there is no half-built subclass for 'this' to
 *  escape into.
 *
 *  Author : Clyde
 * ============================================================================
 */
@SuppressWarnings("serial")
public final class MainWindow extends JFrame implements StudioController.Listener {

    private static final String APP_NAME = "Image Studio";

    private final ImagePreviewPanel preview = new ImagePreviewPanel();
    private final StatusBar statusBar = new StatusBar();
    private final ControlBindings bindings = new ControlBindings();
    private final StudioController controller = new StudioController(this);

    public MainWindow() {
        super(APP_NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        StudioActions actions = new StudioActions(controller, preview, this, bindings);
        setJMenuBar(MenuBarFactory.build(controller, actions, bindings));
        add(ToolBarFactory.build(controller, actions, bindings), BorderLayout.NORTH);
        add(previewArea(), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        controller.addListener(this);
        preview.setFileDropHandler(controller::openFile);
        preview.addPropertyChangeListener("zoomLabel", event -> refreshStatus());
        installCompareKeyBinding();

        stateChanged();                          // start with everything in step
        // Lets the macOS green title bar button enter full screen. Ignored elsewhere.
        getRootPane().putClientProperty("apple.awt.fullscreenable", true);

        setSize(1200, 760);
        setMinimumSize(new Dimension(640, 480));   // the toolbar wraps, so it can go narrow
        setLocationRelativeTo(null);
    }

    /**
     * Opens a file directly, without going through the chooser.
     * <p>
     * Used by the optional command line argument, so
     * {@code java -cp classes imagestudio.ImageStudio photo.png} opens that
     * photo straight away.
     */
    public void openFile(File file) {
        controller.openFile(file);
    }

    /* ====================== CONTROLLER CALLBACKS =========================== */

    @Override
    public void documentOpened() {
        setTitle(APP_NAME + " - " + controller.document().sourceName());
        preview.setFitToWindow(true);
    }

    @Override
    public void displayImage(BufferedImage image) {
        preview.setImage(image);
    }

    @Override
    public void stateChanged() {
        bindings.sync(controller);
        refreshStatus();
    }

    private void refreshStatus() {
        statusBar.update(controller, preview.zoomLabel());
    }

    /* ============================ ASSEMBLY ================================= */

    private JScrollPane previewArea() {
        JScrollPane scroller = new JScrollPane(preview);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        scroller.getViewport().setBackground(Theme.BG_CANVAS);
        return scroller;
    }

    /**
     * The space bar does what the Compare button does, for anyone working from
     * the keyboard. It needs separate press and release bindings because a
     * hold is two events, not one.
     */
    private void installCompareKeyBinding() {
        JComponent root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "compareDown");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("released SPACE"), "compareUp");
        root.getActionMap().put("compareDown", compareAction(true));
        root.getActionMap().put("compareUp", compareAction(false));
    }

    private AbstractAction compareAction(boolean comparing) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                controller.setComparing(comparing);
            }
        };
    }
}
