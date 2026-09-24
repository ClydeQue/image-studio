package imagestudio.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * ============================================================================
 *  WrapLayout
 *  ---------------------------------------------------------------------------
 *  PURPOSE : A FlowLayout that reports its real height once it wraps, so the
 *            toolbar grows to a second row on a narrow window instead of
 *            running off the right edge.
 *
 *  WHY IT IS NEEDED
 *            A plain FlowLayout already places components on new rows, but its
 *            preferred size still claims ONE row. The window then gives it one
 *            row of height and everything that wrapped is cut off. This class
 *            recomputes the preferred size by laying the components out
 *            against the container's current width, row by row.
 *
 *  Author : Clyde
 * ============================================================================
 */
@SuppressWarnings("serial")
final class WrapLayout extends FlowLayout {

    WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return layoutSize(target, false);
    }

    /**
     * Walks the components left to right, starting a new row whenever the next
     * one would pass the available width, and adds up the row heights.
     */
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            // Before the first layout the width is still 0, so allow one long row.
            int targetWidth = target.getWidth();
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + getHgap() * 2);

            Dimension total = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            for (Component c : target.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();

                if (rowWidth > 0 && rowWidth + getHgap() + d.width > maxWidth) {
                    addRow(total, rowWidth, rowHeight);   // this one starts a new row
                    rowWidth = 0;
                    rowHeight = 0;
                }
                if (rowWidth > 0) {
                    rowWidth += getHgap();
                }
                rowWidth += d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }
            addRow(total, rowWidth, rowHeight);

            total.width += insets.left + insets.right + getHgap() * 2;
            total.height += insets.top + insets.bottom + getVgap() * 2;
            return total;
        }
    }

    private void addRow(Dimension total, int rowWidth, int rowHeight) {
        total.width = Math.max(total.width, rowWidth);
        if (total.height > 0) {
            total.height += getVgap();
        }
        total.height += rowHeight;
    }
}
