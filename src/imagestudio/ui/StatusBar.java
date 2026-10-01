package imagestudio.ui;

import imagestudio.core.ImageInfo;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

// Estado ng larawan
@SuppressWarnings("serial")
final class StatusBar extends JPanel {

    private final JLabel file = new JLabel();
    private final JLabel size = new JLabel();
    private final JLabel description = new JLabel();
    private final JLabel zoom = new JLabel();

    StatusBar() {
        super(new BorderLayout());
        setBackground(Theme.BG_CHROME);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(Theme.SPACE_2, Theme.SPACE_3,
                        Theme.SPACE_2, Theme.SPACE_3)));

        file.setFont(Theme.emphasis());
        file.setForeground(Theme.TEXT);
        muted(size);
        muted(description);
        muted(zoom);
        file.setMinimumSize(new Dimension(0, file.getPreferredSize().height));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.add(file);
        left.add(Box.createHorizontalStrut(Theme.SPACE_3));
        left.add(size);
        left.add(Box.createHorizontalStrut(Theme.SPACE_3));
        left.add(description);

        add(left, BorderLayout.CENTER);
        add(zoom, BorderLayout.EAST);
    }

    /* I-update ang status */
    void update(StudioController controller, String zoomLabel) {
        if (!controller.hasImage()) {
            file.setText("No image imported");
            size.setText("");
            description.setText(controller.statusDescription());
            zoom.setText("");
            return;
        }
        ImageInfo image = controller.imageInfo();
        file.setText(image.sourceName());
        file.setToolTipText(image.sourceName());
        size.setText(image.width() + " x " + image.height());
        description.setText(controller.statusDescription());
        description.setToolTipText(controller.statusDescription());
        zoom.setText(zoomLabel);
    }

    private void muted(JLabel label) {
        label.setFont(Theme.small());
        label.setForeground(Theme.TEXT_MUTED);
    }
}
