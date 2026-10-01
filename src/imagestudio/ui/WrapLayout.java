package imagestudio.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

// Paglipat ng hanay
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

    /* Sukatin bawat row */
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            // Unang layout muna
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
                    addRow(total, rowWidth, rowHeight);   // Bagong hanay ito
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
