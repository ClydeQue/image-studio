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

// Buuin ang window
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

        stateChanged();                          // Isabay ang controls
        // Buong macOS screen
        getRootPane().putClientProperty("apple.awt.fullscreenable", true);

        setSize(1200, 760);
        setMinimumSize(new Dimension(640, 480));   // Kasya kahit makitid
        setLocationRelativeTo(null);
    }

    /* Direktang pagbukas */
    public void openFile(File file) {
        controller.openFile(file);
    }


    @Override
    public void documentOpened() {
        setTitle(APP_NAME + " - " + controller.imageInfo().sourceName());
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


    private JScrollPane previewArea() {
        JScrollPane scroller = new JScrollPane(preview);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        scroller.getViewport().setBackground(Theme.BG_CANVAS);
        return scroller;
    }

    /* Space para compare */
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
