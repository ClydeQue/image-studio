package imagestudio.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * ============================================================================
 *  StatusBar
 *  ---------------------------------------------------------------------------
 *  PURPOSE : Answers, at a glance and at all times, the four questions someone
 *            using this program actually has: which file is open, how big it
 *            is, what has been applied to it, and what scale it is being shown
 *            at.
 *
 *            Keeping the user informed of system state is the first rule of
 *            interface design, and it is cheap to honour. The alternative is a
 *            window where the only way to know whether Luminosity is active is
 *            to look at a button and hope.
 *
 *  Author : Clyde
 * ============================================================================
 */
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

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.add(file);
        left.add(Box.createHorizontalStrut(Theme.SPACE_3));
        left.add(size);
        left.add(Box.createHorizontalStrut(Theme.SPACE_3));
        left.add(description);

        add(left, BorderLayout.WEST);
        add(zoom, BorderLayout.EAST);
    }

    /** Re-reads everything from the controller. Never holds state of its own. */
    void update(StudioController controller, String zoomLabel) {
        if (!controller.hasImage()) {
            file.setText("No image imported");
            size.setText("");
            description.setText(controller.statusDescription());
            zoom.setText("");
            return;
        }
        file.setText(controller.document().sourceName());
        size.setText(controller.document().width() + " x " + controller.document().height());
        description.setText(controller.statusDescription());
        zoom.setText(zoomLabel);
    }

    private void muted(JLabel label) {
        label.setFont(Theme.small());
        label.setForeground(Theme.TEXT_MUTED);
    }
}
