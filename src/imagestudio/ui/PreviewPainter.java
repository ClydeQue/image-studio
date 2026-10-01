package imagestudio.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

// Guhit sa canvas
final class PreviewPainter {

    private PreviewPainter() {
    }

    /* Hugis ng card */
    static RoundRectangle2D card(Rectangle r) {
        return new RoundRectangle2D.Float(r.x, r.y, r.width, r.height,
                Theme.RADIUS, Theme.RADIUS);
    }

    /* Anino ng card */
    static void paintShadow(Graphics2D g2, Rectangle r) {
        g2.setColor(new Color(0, 0, 0, 18));
        for (int i = 6; i >= 1; i--) {
            g2.fill(new RoundRectangle2D.Float(
                    r.x - i, r.y - i + 2, r.width + 2f * i, r.height + 2f * i,
                    Theme.RADIUS + i, Theme.RADIUS + i));
        }
    }

    /* Background ng transparency */
    static void paintCheckerboard(Graphics2D g2, Rectangle r) {
        Rectangle clip = g2.getClipBounds();
        Rectangle visible = (clip == null) ? r : r.intersection(clip);
        if (visible.isEmpty()) {
            return;
        }

        Shape previousClip = g2.getClip();
        g2.clip(card(r));

        g2.setColor(Theme.CHECKER_A);
        g2.fillRect(visible.x, visible.y, visible.width, visible.height);

        /* Nakapirming mga parisukat */
        int s = Theme.CHECKER_SIZE;
        int firstCol = Math.floorDiv(visible.x - r.x, s);
        int lastCol = Math.floorDiv(visible.x + visible.width - r.x, s);
        int firstRow = Math.floorDiv(visible.y - r.y, s);
        int lastRow = Math.floorDiv(visible.y + visible.height - r.y, s);

        g2.setColor(Theme.CHECKER_B);
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {
                if (((row + col) & 1) == 0) {
                    g2.fillRect(r.x + col * s, r.y + row * s, s, s);
                }
            }
        }
        g2.setClip(previousClip);
    }

    /* Gilid ng larawan */
    static void paintCardEdge(Graphics2D g2, Rectangle r) {
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(255, 255, 255, 40));
        g2.draw(card(r));
    }

    /* Wala pang larawan */
    static void paintEmptyState(Graphics2D g2, int panelW, int panelH, boolean hovering) {
        int w = Math.min(420, panelW - 2 * Theme.SPACE_4);
        if (w <= 0) {
            return;                              // Kulang ang espasyo
        }
        int h = 200;
        int x = (panelW - w) / 2;
        int y = (panelH - h) / 2;

        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, new float[]{6f, 6f}, 0));
        g2.setColor(hovering ? Theme.ACCENT : Theme.CANVAS_HINT);
        g2.draw(new RoundRectangle2D.Float(x, y, w, h,
                Theme.RADIUS * 1.6f, Theme.RADIUS * 1.6f));

        paintPictureGlyph(g2, panelW / 2, y + 58);

        g2.setColor(Theme.CANVAS_TEXT);
        g2.setFont(Theme.heading());
        drawCentred(g2, "Import an image to begin", panelW, y + 126);
        g2.setFont(Theme.small());
        drawCentred(g2, "or drop a PNG or JPG anywhere on this panel", panelW, y + 152);
    }

    /* Highlight kapag drop */
    static void paintDropHighlight(Graphics2D g2, int panelW, int panelH) {
        int inset = Theme.SPACE_2;
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(Theme.ACCENT);
        g2.draw(new RoundRectangle2D.Float(inset, inset,
                panelW - 2f * inset, panelH - 2f * inset, Theme.RADIUS, Theme.RADIUS));
    }

    /* Simbolo ng larawan */
    private static void paintPictureGlyph(Graphics2D g2, int cx, int cy) {
        int w = 56, h = 44;
        int x = cx - w / 2, y = cy - h / 2;

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(Theme.CANVAS_TEXT);
        g2.draw(new RoundRectangle2D.Float(x, y, w, h, 6, 6));
        g2.fillOval(x + 10, y + 9, 9, 9);

        Shape previousClip = g2.getClip();
        g2.clip(new RoundRectangle2D.Float(x, y, w, h, 6, 6));
        int[] xs = {x + 4, x + 22, x + 34, x + 52, x + 52, x + 4};
        int[] ys = {y + h, y + 22, y + 32, y + 14, y + h, y + h};
        g2.fillPolygon(xs, ys, xs.length);
        g2.setClip(previousClip);
    }

    private static void drawCentred(Graphics2D g2, String text, int panelW, int baselineY) {
        int width = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (panelW - width) / 2, baselineY);
    }
}
