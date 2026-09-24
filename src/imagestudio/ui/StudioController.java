package imagestudio.ui;

import imagestudio.core.GrayscaleMethod;
import imagestudio.core.ImageDocument;
import imagestudio.io.ImageFileService;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 *  StudioController
 *  ---------------------------------------------------------------------------
 *  PURPOSE : Everything the program DOES, separated from everything it LOOKS
 *            like. It owns the open document, performs every operation, and
 *            drives the preview. It builds no widgets and knows nothing about
 *            menus or toolbars.
 *
 *            Views tell it what the user asked for and listen for "something
 *            changed", then read the state back through the getters below.
 *            That is what lets the menu, the toolbar and the status bar all
 *            show the same thing without any of them knowing the others exist.
 *
 *  THREADING
 *            render() is the only heavy work in the program and the only place
 *            two threads meet, so it is worth being precise about why it is
 *            safe.
 *
 *            The worker reads the document's recipe from a background thread
 *            while the interface may be changing it. That is safe here for two
 *            reasons. The recipe is a boolean, a boolean and an enum reference,
 *            and reads and writes of those are atomic in Java, so a worker can
 *            never see a half-written value. And every change is made on the
 *            event dispatch thread and immediately followed by a new render
 *            request, which bumps the generation and makes any in-flight
 *            result stale, so a worker that read an older recipe has its
 *            result discarded rather than painted.
 *
 *            Everything the worker calls is a pure function, so there is no
 *            shared mutable state inside the image processing itself.
 *
 *  Author : Clyde
 * ============================================================================
 */
public final class StudioController {

    /** What a view implements to stay in step. Deliberately tiny. */
    public interface Listener {
        /** A different file was opened, so the view should reset how it shows it. */
        void documentOpened();

        /** This image should now be on screen. */
        void displayImage(BufferedImage image);

        /** Some state changed; re-read the getters and refresh. */
        void stateChanged();
    }

    private final Component owner;                 // parent for dialogs
    private final List<Listener> listeners = new ArrayList<>();

    private ImageDocument document;
    private BufferedImage lastRendered;
    private boolean comparing;
    private boolean rendering;

    /**
     * Incremented on every render request. A worker whose generation is no
     * longer current has been superseded and throws its result away. Without
     * this, clicking Average then Luminosity quickly can leave the slower
     * Average worker finishing last and painting over the newer result, so the
     * picture and the selected method disagree.
     */
    private int renderGeneration;

    public StudioController(Component owner) {
        this.owner = owner;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    /* ============================ OPERATIONS =============================== */

    /** Asks the user for a file, then opens it. */
    public void importImage() {
        adopt(ImageFileService.importImage(owner));
    }

    /** Opens a known file. Used by drag and drop and by the command line. */
    public void openFile(File file) {
        adopt(ImageFileService.readImage(owner, file));
    }

    private void adopt(ImageFileService.Loaded loaded) {
        if (loaded == null) {
            return;                                // the user was already told why
        }
        document = new ImageDocument(loaded.image(), loaded.fileName());

        /* Drop the previous file's render at once. Leaving it in place would
           let Compare show the old image during the moment before the new one
           has finished rendering. */
        lastRendered = null;
        comparing = false;

        listeners.forEach(Listener::documentOpened);
        render();
    }

    /** Saves the image exactly as previewed. */
    public void save() {
        if (!canSave()) {
            return;
        }
        File saved = ImageFileService.saveImage(owner, lastRendered,
                document.suggestedFileName("png"));
        if (saved != null) {
            fireStateChanged();
        }
    }

    public void reset() {
        if (document == null) {
            return;
        }
        document.reset();
        render();
    }

    public void applyGrayscale(GrayscaleMethod method) {
        if (document == null) {
            return;
        }
        document.setGrayscale(method);
        render();
    }

    public void toggleFlipHorizontal() {
        if (document == null) {
            return;
        }
        document.toggleFlipHorizontal();
        render();
    }

    public void toggleFlipVertical() {
        if (document == null) {
            return;
        }
        document.toggleFlipVertical();
        render();
    }

    /**
     * Press and hold to see the untouched original, release to come back.
     * <p>
     * Practically free, because the non-destructive model means the original
     * is still in memory and has never been altered.
     * <p>
     * Known platform note: under X11 key auto-repeat, holding a key produces
     * repeated press and release pairs, so the keyboard route flickers on some
     * Linux setups. The Compare button, which is the primary affordance, uses
     * real mouse press and release and is unaffected.
     */
    public void setComparing(boolean on) {
        if (document == null || comparing == on) {
            return;
        }
        comparing = on;

        /* lastRendered is null until the first render lands, so until then the
           original is the only thing there is to show. */
        display(on || lastRendered == null ? document.original() : lastRendered);
        fireStateChanged();
    }

    /* ============================ RENDERING ================================ */

    /** Rebuilds the output from the original, off the event dispatch thread. */
    private void render() {
        if (document == null) {
            return;
        }
        rendering = true;
        setBusyCursor(true);
        fireStateChanged();                        // greys out Save while the work runs

        final int generation = ++renderGeneration;
        final ImageDocument target = document;

        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() {
                return target.render();
            }

            @Override
            protected void done() {
                /* A newer render was already requested, so this result is
                   stale. Returning also leaves the busy state alone, which the
                   newer worker is responsible for clearing. */
                if (generation != renderGeneration) {
                    return;
                }
                try {
                    lastRendered = get();

                    /* Do not steal the screen back from Compare. The user may
                       be holding it while this finishes, and replacing the
                       original under their thumb would contradict the status
                       bar, which still reads "Showing the original". */
                    if (!comparing) {
                        display(lastRendered);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(owner,
                            "The image could not be processed.\n\n" + ex.getMessage(),
                            "Processing failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    rendering = false;
                    setBusyCursor(false);
                    fireStateChanged();
                }
            }
        }.execute();
    }

    private void setBusyCursor(boolean busy) {
        if (owner != null) {
            owner.setCursor(Cursor.getPredefinedCursor(
                    busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        }
    }

    private void display(BufferedImage image) {
        listeners.forEach(listener -> listener.displayImage(image));
    }

    private void fireStateChanged() {
        listeners.forEach(Listener::stateChanged);
    }

    /* ============================== STATE ================================== */

    public boolean hasImage() {
        return document != null;
    }

    public ImageDocument document() {
        return document;
    }

    public boolean isModified() {
        return document != null && document.isModified();
    }

    public GrayscaleMethod grayscale() {
        return document != null ? document.grayscale() : GrayscaleMethod.NONE;
    }

    public boolean isFlippedHorizontally() {
        return document != null && document.isFlippedHorizontally();
    }

    public boolean isFlippedVertically() {
        return document != null && document.isFlippedVertically();
    }

    public boolean isComparing() {
        return comparing;
    }

    /**
     * Save is refused while a render is in flight.
     * <p>
     * The document's recipe has already changed by then but lastRendered still
     * holds the previous result, so saving in that window would write the old
     * pixels under a filename describing the new recipe. Better to have the
     * button greyed for the moment it takes than to write a file that
     * contradicts its own name.
     */
    public boolean canSave() {
        return document != null && lastRendered != null && !rendering;
    }

    /** Plain-English description of what is on screen, for the status bar. */
    public String statusDescription() {
        if (document == null) {
            return "Import an image, or drop one on the canvas";
        }
        if (rendering) {
            return "Working...";
        }
        return comparing ? "Showing the original" : document.recipeSummary();
    }
}
