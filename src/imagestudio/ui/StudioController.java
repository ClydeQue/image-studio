package imagestudio.ui;

import imagestudio.core.GrayscaleMethod;
import imagestudio.core.ImageDocument;
import imagestudio.core.ImageInfo;
import imagestudio.core.ImageRecipe;
import imagestudio.io.ImageFileService;

import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/* Daloy ng edits */
public final class StudioController {

    /* Abiso sa view */
    public interface Listener {
        /* Bagong larawan */
        void documentOpened();

        /* Ipakita ang larawan */
        void displayImage(BufferedImage image);

        /* Nagbagong estado */
        void stateChanged();
    }

    private final Component owner;                 // Magulang ng dialog
    private final ImageFileService files = new ImageFileService();
    private final ImageFileDialogs dialogs;
    private final List<Listener> listeners = new ArrayList<>();

    private ImageDocument document;
    private BufferedImage lastRendered;
    private boolean comparing;
    private boolean rendering;
    private boolean renderFailed;
    private boolean loading;
    private boolean saving;
    private int loadGeneration;

    /* Bilang ng render */
    private int renderGeneration;

    public StudioController(Component owner) {
        this.owner = owner;
        this.dialogs = new ImageFileDialogs(owner);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }


    /* Pumili ng file */
    public void importImage() {
        File file = dialogs.chooseImportFile();
        if (file != null) {
            openFile(file);
        }
    }

    /* Buksan ang file */
    public void openFile(File file) {
        final int generation = ++loadGeneration;
        loading = true;
        refreshBusyState();

        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return files.readImage(file);
            }

            @Override
            protected void done() {
                // Pinakabagong file muna
                if (generation != loadGeneration) {
                    return;
                }
                try {
                    BufferedImage image = get();
                    dialogs.rememberDirectory(file);
                    adopt(image, file.getName());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    showFailure("Cannot open that file", ex.getCause());
                } finally {
                    loading = false;
                    refreshBusyState();
                }
            }
        }.execute();
    }

    private void adopt(BufferedImage image, String fileName) {
        document = new ImageDocument(image, fileName);

        /* Linisin lumang render */
        lastRendered = null;
        comparing = false;

        listeners.forEach(Listener::documentOpened);
        display(image);
        render();
    }

    /* I-save ang preview */
    public void save() {
        if (!canSave()) {
            return;
        }
        // Kopyahin image filename
        BufferedImage image = lastRendered;
        File target = dialogs.chooseSaveFile(document.suggestedFileName("png"));
        if (target == null) {
            return;
        }
        saving = true;
        refreshBusyState();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                files.saveImage(image, target);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    showFailure("Cannot save the image", ex.getCause());
                } finally {
                    saving = false;
                    refreshBusyState();
                }
            }
        }.execute();
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

    /* Ihambing sa original */
    public void setComparing(boolean on) {
        if (document == null || comparing == on) {
            return;
        }
        comparing = on;

        /* Original muna ipakita */
        display(on || lastRendered == null ? document.original() : lastRendered);
        fireStateChanged();
    }


    /* Render sa background */
    private void render() {
        if (document == null) {
            return;
        }
        rendering = true;
        renderFailed = false;
        final int generation = ++renderGeneration;
        final BufferedImage original = document.original();
        final ImageRecipe recipe = document.recipe();
        refreshBusyState();

        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() {
                return recipe.applyTo(original);
            }

            @Override
            protected void done() {
                /* Laktawan lumang resulta */
                if (generation != renderGeneration) {
                    return;
                }
                try {
                    lastRendered = get();

                    /* Panatilihin ang compare */
                    if (!comparing) {
                        display(lastRendered);
                    }
                } catch (InterruptedException ex) {
                    lastRendered = null;
                    renderFailed = true;
                    display(original);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    lastRendered = null;
                    renderFailed = true;
                    display(original);
                    showFailure("Processing failed", ex.getCause());
                } finally {
                    rendering = false;
                    refreshBusyState();
                }
            }
        }.execute();
    }

    private void refreshBusyState() {
        if (owner != null) {
            owner.setCursor(Cursor.getPredefinedCursor(
                    rendering || loading || saving ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        }
        fireStateChanged();
    }

    private void showFailure(String title, Throwable cause) {
        String message;
        if (cause instanceof OutOfMemoryError) {
            message = "This image needs more memory than is available. Try a smaller image.";
        } else {
            message = cause.getMessage();
            if (message == null || message.isBlank()) {
                message = "The image could not be processed. The file may be damaged or incomplete.";
            }
        }
        dialogs.showError(title, message);
    }

    private void display(BufferedImage image) {
        listeners.forEach(listener -> listener.displayImage(image));
    }

    private void fireStateChanged() {
        listeners.forEach(Listener::stateChanged);
    }


    public boolean hasImage() {
        return document != null;
    }

    public ImageInfo imageInfo() {
        return document == null ? null
                : new ImageInfo(document.sourceName(), document.width(), document.height());
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

    /* Hintayin ang trabaho */
    public boolean canSave() {
        return document != null && lastRendered != null && !rendering && !loading && !saving && !comparing;
    }

    /* Paglalarawan ng preview */
    public String statusDescription() {
        if (loading) {
            return "Opening image...";
        }
        if (saving) {
            return "Saving image...";
        }
        if (document == null) {
            return "Import an image, or drop one on the canvas";
        }
        if (rendering) {
            return "Working...";
        }
        if (renderFailed) {
            return "Processing failed. Reset or choose another method.";
        }
        return comparing ? "Showing the original" : document.recipeSummary();
    }
}
