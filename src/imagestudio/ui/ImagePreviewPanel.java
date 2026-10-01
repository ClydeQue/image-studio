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

// Preview ng larawan
@SuppressWarnings("serial")
public final class ImagePreviewPanel extends JPanel implements Scrollable {

    private static final double MIN_ZOOM = 0.05;
    private static final double MAX_ZOOM = 16.0;
    private static final double ZOOM_STEP = 1.25;
    private static final Dimension DEFAULT_SIZE = new Dimension(760, 520);

    /* Shortcut sa zoom */
    private static final int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    private final ImageScaler scaler = new ImageScaler();

    private BufferedImage image;
    private boolean fitToWindow = true;
    private double zoom = 1.0;
    private boolean dragHovering;

    /* Handler ng drop */
    private Consumer<File> fileDropHandler;

    public ImagePreviewPanel() {
        setOpaque(true);
        setBackground(Theme.BG_CANVAS);
        installDragAndDrop();
        installWheelZoom();
        installResizeReporting();
    }


    /* Ipakita ang larawan */
    public void setImage(BufferedImage image) {
        this.image = image;
        scaler.clear();
        revalidate();
        repaint();
        reportZoom();
    }

    /* Kasya sa window */
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

    /* Itakda ang zoom */
    public void setZoom(double newZoom) {
        this.zoom = Math.clamp(newZoom, MIN_ZOOM, MAX_ZOOM);
        this.fitToWindow = false;
        revalidate();
        repaint();
        reportZoom();
    }

    /* Tuloy na zoom */
    public void zoomIn() {
        setZoom(effectiveScale() * ZOOM_STEP);
    }

    public void zoomOut() {
        setZoom(effectiveScale() / ZOOM_STEP);
    }

    public void zoomToActualSize() {
        setZoom(1.0);
    }

    /* Basa ng zoom */
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


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);                 // Kulayan ang canvas

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

    /* Larawan sa card */
    private void paintImageCard(Graphics2D g2) {
        Rectangle r = imageBounds();

        PreviewPainter.paintShadow(g2, r);
        PreviewPainter.paintCheckerboard(g2, r);

        /* Sukat sa display */
        double deviceScale = g2.getTransform().getScaleX();
        int targetW = (int) Math.ceil(r.width * deviceScale);
        int targetH = (int) Math.ceil(r.height * deviceScale);

        /* Linaw ng pixels */
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


    /* Kasalukuyang sukat */
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

        /* Paliitin lang larawan */
        return Math.min(1.0, scale);
    }

    /* Gitna ng larawan */
    private Rectangle imageBounds() {
        double scale = effectiveScale();
        int w = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int h = Math.max(1, (int) Math.round(image.getHeight() * scale));
        int x = Math.max(Theme.CANVAS_PADDING, (getWidth() - w) / 2);
        int y = Math.max(Theme.CANVAS_PADDING, (getHeight() - h) / 2);
        return new Rectangle(x, y, w, h);
    }


    /* Tanggapin ang file */
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

                /* Tapusin muna drop */
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

    /* Zoom gamit wheel */
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

    /* I-update kapag resize */
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

    /* Ibalita ang zoom */
    private void reportZoom() {
        firePropertyChange("zoomLabel", null, zoomLabel());
    }

    /* Scrollbar kapag zoom */

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
