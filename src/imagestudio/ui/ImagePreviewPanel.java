package imagestudio.ui;

import imagestudio.core.ImageScaler;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * ============================================================================
 *  ImagePreviewPanel
 *  ---------------------------------------------------------------------------
 *  PURPOSE : The image preview section required by the project. Painted by
 *            hand rather than being a JLabel with an icon, because it has to
 *            scale to fit, show transparency honestly, zoom while staying
 *            sharp, and accept a dropped file.
 *
 *            This class keeps only what genuinely belongs to a component: the
 *            image it shows, the zoom it shows it at, and the input it
 *            handles. The drawing lives in PreviewPainter and the scaling
 *            algorithm lives in core.ImageScaler, where it can be tested
 *            without a window.
 *
 *  THE RETINA DETAIL
 *            On a high-DPI display the Graphics2D handed to paintComponent
 *            already carries a 2x transform, so a "300 pixel wide" drawing
 *            really covers 600 device pixels. The scaler is therefore asked
 *            for the DEVICE size, read from that transform, and the last
 *            scaling step is left to drawImage. Reducing to the logical size
 *            first would throw away half the detail before anything is drawn.
 *
 *  Declared final so calling setOpaque and setBackground from the constructor
 *  is safe: with no possible subclass there is no half-built subclass for
 *  'this' to escape into. Swing components inherit Serializable but are never
 *  serialized, so that warning is suppressed rather than answered with a
 *  meaningless constant.
 *
 *  Author : Clyde
 * ============================================================================
 */
@SuppressWarnings("serial")
public final class ImagePreviewPanel extends JPanel implements Scrollable {

    private static final double MIN_ZOOM = 0.05;
    private static final double MAX_ZOOM = 16.0;
    private static final double ZOOM_STEP = 1.25;
    private static final Dimension DEFAULT_SIZE = new Dimension(760, 520);

    /** Cmd on macOS, Ctrl on Windows and Linux. Held down, the wheel zooms. */
    private static final int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    private final ImageScaler scaler = new ImageScaler();

    private BufferedImage image;
    private boolean fitToWindow = true;
    private double zoom = 1.0;
    private boolean dragHovering;

    /** Called with the dropped file when the user drags an image onto the panel. */
    private Consumer<File> fileDropHandler;

    public ImagePreviewPanel() {
        setOpaque(true);
        setBackground(Theme.BG_CANVAS);
        installDragAndDrop();
        installWheelZoom();
        installResizeReporting();
    }

    /* ============================ PUBLIC API =============================== */

    /** Shows a new image, or clears the panel when given null. */
    public void setImage(BufferedImage image) {
        this.image = image;
        scaler.clear();
        revalidate();
        repaint();
        reportZoom();
    }

    /** Fit mode scales the image down to sit inside the window. */
    public void setFitToWindow(boolean fit) {
        if (this.fitToWindow != fit) {
            this.fitToWindow = fit;
            revalidate();
            repaint();
            reportZoom();
        }
    }

    public boolean isFitToWindow() {
        return fitToWindow;
    }

    /** Leaves fit mode and pins the image at an exact scale. */
    public void setZoom(double newZoom) {
        this.zoom = Math.clamp(newZoom, MIN_ZOOM, MAX_ZOOM);
        this.fitToWindow = false;
        revalidate();
        repaint();
        reportZoom();
    }

    /* Zooming starts from whatever is on screen now, so stepping in from fit
       mode continues smoothly instead of jumping to 100% first. */
    public void zoomIn() {
        setZoom(effectiveScale() * ZOOM_STEP);
    }

    public void zoomOut() {
        setZoom(effectiveScale() / ZOOM_STEP);
    }

    public void zoomToActualSize() {
        setZoom(1.0);
    }

    /** Readout for the status bar, for example "Fit (43%)" or "100%". */
    public String zoomLabel() {
        if (image == null) {
            return "-";
        }
        int percent = (int) Math.round(effectiveScale() * 100);
        return fitToWindow ? "Fit (" + percent + "%)" : percent + "%";
    }

    public void setFileDropHandler(Consumer<File> handler) {
        this.fileDropHandler = handler;
    }

    /* ============================ PAINTING ================================= */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);                 // fills the dark canvas background

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (image == null) {
            PreviewPainter.paintEmptyState(g2, getWidth(), getHeight(), dragHovering);
        } else {
            paintImageCard(g2);
        }
        if (dragHovering) {
            PreviewPainter.paintDropHighlight(g2, getWidth(), getHeight());
        }
        g2.dispose();
    }

    /** The image, sitting on a checkerboard card with a soft shadow under it. */
    private void paintImageCard(Graphics2D g2) {
        Rectangle r = imageBounds();

        PreviewPainter.paintShadow(g2, r);
        PreviewPainter.paintCheckerboard(g2, r);

        /* 2.0 on a Retina display, 1.0 otherwise. Reducing to the device size
           keeps the full detail available for drawImage to use. */
        double deviceScale = g2.getTransform().getScaleX();
        int targetW = (int) Math.ceil(r.width * deviceScale);
        int targetH = (int) Math.ceil(r.height * deviceScale);

        /* Shrinking wants smooth interpolation. Enlarging wants hard pixel
           edges, so zooming in to compare two grayscale methods shows the
           actual pixels rather than a blur of them. */
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                effectiveScale() > 1.0
                        ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                        : RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        Shape clip = g2.getClip();
        g2.clip(PreviewPainter.card(r));
        g2.drawImage(scaler.scaledFor(image, targetW, targetH),
                r.x, r.y, r.width, r.height, null);
        g2.setClip(clip);

        PreviewPainter.paintCardEdge(g2, r);
    }

    /* ============================ GEOMETRY ================================= */

    /** The scale actually in use: either the fitted scale or the pinned zoom. */
    private double effectiveScale() {
        if (image == null) {
            return 1.0;
        }
        if (!fitToWindow) {
            return zoom;
        }
        int availableW = Math.max(1, getWidth() - 2 * Theme.CANVAS_PADDING);
        int availableH = Math.max(1, getHeight() - 2 * Theme.CANVAS_PADDING);
        double scale = Math.min(availableW / (double) image.getWidth(),
                availableH / (double) image.getHeight());

        /* Fit shrinks but never enlarges. Blowing a small image up to fill the
           window would misrepresent it; Actual Size is there for a closer look. */
        return Math.min(1.0, scale);
    }

    /** Where the image is drawn, centred, with the canvas padding respected. */
    private Rectangle imageBounds() {
        double scale = effectiveScale();
        int w = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int h = Math.max(1, (int) Math.round(image.getHeight() * scale));
        int x = Math.max(Theme.CANVAS_PADDING, (getWidth() - w) / 2);
        int y = Math.max(Theme.CANVAS_PADDING, (getHeight() - h) / 2);
        return new Rectangle(x, y, w, h);
    }

    /* ========================== INTERACTION ================================ */

    /**
     * Drop a file on the panel to open it. The empty state advertises this.
     * <p>
     * A DropTarget is used rather than a TransferHandler because it reports
     * dragEnter and dragExit as well as the drop itself. Without those the
     * panel could accept a file but give no sign it was going to, and a drop
     * target that looks inert is one nobody trusts enough to use.
     */
    private void installDragAndDrop() {
        new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent event) {
                setDragHovering(true);
            }

            @Override
            public void dragExit(DropTargetEvent event) {
                setDragHovering(false);
            }

            @Override
            public void drop(DropTargetDropEvent event) {
                setDragHovering(false);
                if (fileDropHandler == null) {
                    event.rejectDrop();
                    return;
                }

                File dropped = null;
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    Object data = event.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    List<?> files = (List<?>) data;
                    dropped = files.isEmpty() ? null : (File) files.get(0);
                    event.dropComplete(dropped != null);
                } catch (Exception ex) {
                    event.dropComplete(false);
                    return;
                }

                /* The handler runs only after the drop has been closed out.
                   Calling it inside the try would mean a failure in it reaches
                   dropComplete a second time, and completing an already
                   completed drop throws InvalidDnDOperationException from
                   inside the catch, where nothing can report it. */
                if (dropped != null) {
                    fileDropHandler.accept(dropped);
                }
            }
        });
    }

    private void setDragHovering(boolean hovering) {
        if (dragHovering != hovering) {
            dragHovering = hovering;
            repaint();
        }
    }

    /**
     * Cmd or Ctrl plus the wheel zooms. A plain wheel is passed up to the
     * scroll pane so it still scrolls, which is the behaviour every other
     * application has trained people to expect.
     */
    private void installWheelZoom() {
        addMouseWheelListener(event -> {
            boolean zooming = (event.getModifiersEx() & MENU_MASK) != 0;
            if (image == null || !zooming) {
                if (getParent() != null) {
                    getParent().dispatchEvent(
                            SwingUtilities.convertMouseEvent(this, event, getParent()));
                }
                return;
            }
            if (event.getWheelRotation() < 0) {
                zoomIn();
            } else {
                zoomOut();
            }
        });
    }

    /**
     * In fit mode the scale is derived from the panel size, so resizing the
     * window silently changes it. Without this the status bar would keep
     * showing the percentage from before the drag.
     */
    private void installResizeReporting() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                if (fitToWindow) {
                    reportZoom();
                }
            }
        });
    }

    /* Old value is deliberately null so the change always fires: the label is
       derived from several fields and comparing strings here would be fragile. */
    private void reportZoom() {
        firePropertyChange("zoomLabel", null, zoomLabel());
    }

    /* =========================== SCROLLABLE ================================ */
    /* Returning true from the tracks-viewport methods while fitting is what
       makes the scroll bars disappear in fit mode and reappear when zoomed. */

    @Override
    public Dimension getPreferredSize() {
        if (image == null || fitToWindow) {
            return DEFAULT_SIZE;
        }
        return new Dimension(
                (int) Math.round(image.getWidth() * zoom) + 2 * Theme.CANVAS_PADDING,
                (int) Math.round(image.getHeight() * zoom) + 2 * Theme.CANVAS_PADDING);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return DEFAULT_SIZE;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
        return Theme.SPACE_3;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
        return (orientation == SwingConstants.VERTICAL) ? visible.height : visible.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return fitToWindow;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return fitToWindow;
    }
}
