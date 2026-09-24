package imagestudio.ui;

import imagestudio.core.GrayscaleMethod;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ============================================================================
 *  ToolBarFactory
 *  ---------------------------------------------------------------------------
 *  PURPOSE : Builds the toolbar, which carries the "UI buttons for all of the
 *            image manipulation methods" the project asks for. Everything here
 *            also exists in the menu bar; both register with ControlBindings
 *            so neither can fall out of step with the other.
 *
 *  LAYOUT   : grouped into sections separated by rules, so related controls
 *            read as belonging together rather than as one long row of
 *            buttons. Import and Save, then the grayscale choice, then the
 *            flips, then compare and reset, with the view controls pushed to
 *            the far right where they are out of the way of the work.
 *
 *            Every button is made non-focusable. A toolbar button that takes
 *            focus would swallow the space bar, which this program uses for
 *            hold-to-compare.
 *
 *  Author : Clyde
 * ============================================================================
 */
final class ToolBarFactory {

    private ToolBarFactory() {
    }

    static JToolBar build(StudioController controller, StudioActions actions,
                          ControlBindings bindings) {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setOpaque(true);
        bar.setBackground(Theme.SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(Theme.SPACE_2, Theme.SPACE_3,
                        Theme.SPACE_2, Theme.SPACE_3)));

        bar.add(toolButton(actions.importImage, "Import"));
        bar.add(Box.createHorizontalStrut(Theme.SPACE_1));
        bar.add(toolButton(actions.save, "Save"));

        bar.add(separator());
        bar.add(caption("Grayscale:"));
        bar.add(Box.createHorizontalStrut(Theme.SPACE_2));
        addGrayscaleButtons(bar, controller, bindings);

        bar.add(separator());
        addFlipButtons(bar, controller, bindings);

        bar.add(separator());
        bar.add(compareButton(controller, bindings));
        bar.add(Box.createHorizontalStrut(Theme.SPACE_1));
        bar.add(toolButton(actions.reset, "Reset"));

        bar.add(Box.createHorizontalGlue());
        bar.add(toolButton(actions.fit, "Fit"));
        bar.add(Box.createHorizontalStrut(Theme.SPACE_1));
        bar.add(toolButton(actions.actualSize, "100%"));
        return bar;
    }

    /** Same ButtonGroup idea as the menu, so one method is always showing. */
    private static void addGrayscaleButtons(JToolBar bar, StudioController controller,
                                            ControlBindings bindings) {
        ButtonGroup group = new ButtonGroup();
        for (GrayscaleMethod method : GrayscaleMethod.values()) {
            JToggleButton button = new JToggleButton(method.label());
            button.setToolTipText(method.formula() + "   (key " + method.accelerator() + ")");
            button.setFocusable(false);
            button.addActionListener(e -> controller.applyGrayscale(method));
            group.add(button);
            bar.add(button);
            bar.add(Box.createHorizontalStrut(Theme.SPACE_1));
            bindings.bindGrayscale(method, button);
        }
    }

    private static void addFlipButtons(JToolBar bar, StudioController controller,
                                       ControlBindings bindings) {
        JToggleButton horizontal = new JToggleButton("Flip Horizontal");
        horizontal.setToolTipText(
                "Mirror left to right, reflecting across the vertical axis   (key H)");
        horizontal.setFocusable(false);
        horizontal.addActionListener(e -> controller.toggleFlipHorizontal());

        JToggleButton vertical = new JToggleButton("Flip Vertical");
        vertical.setToolTipText(
                "Mirror top to bottom, reflecting across the horizontal axis   (key V)");
        vertical.setFocusable(false);
        vertical.addActionListener(e -> controller.toggleFlipVertical());

        bar.add(horizontal);
        bar.add(Box.createHorizontalStrut(Theme.SPACE_1));
        bar.add(vertical);
        bindings.bindFlipHorizontal(horizontal);
        bindings.bindFlipVertical(vertical);
    }

    /**
     * Press and hold to see the untouched original, release to come back.
     * <p>
     * A plain ActionListener fires once on click, which is no use for a hold,
     * so this listens for the press and the release separately.
     */
    private static JButton compareButton(StudioController controller, ControlBindings bindings) {
        JButton button = new JButton("Compare");
        button.setToolTipText("Press and hold to see the original   (or hold the space bar)");
        button.setFocusable(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                controller.setComparing(true);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                controller.setComparing(false);
            }
        });
        bindings.bindNeedsModification(button);
        return button;
    }

    /** A toolbar button carrying an Action but showing a shorter label. */
    private static JButton toolButton(Action action, String shortLabel) {
        JButton button = new JButton(action);
        button.setText(shortLabel);
        button.setFocusable(false);
        return button;
    }

    private static JToolBar.Separator separator() {
        JToolBar.Separator separator = new JToolBar.Separator();
        separator.setOrientation(SwingConstants.VERTICAL);
        return separator;
    }

    private static JLabel caption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.small());
        label.setForeground(Theme.TEXT_MUTED);
        return label;
    }
}
