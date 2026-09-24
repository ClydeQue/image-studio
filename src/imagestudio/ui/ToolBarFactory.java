package imagestudio.ui;

import imagestudio.core.GrayscaleMethod;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
 *  LAYOUT   : grouped into sections with a gap between them, so related
 *            controls read as belonging together rather than as one long row
 *            of buttons. Import and Save, then the grayscale choice, then the
 *            flips, then compare and reset, then the view controls.
 *
 *            The sections sit in a WrapLayout, so on a narrow window whole
 *            sections move to a second row instead of running off the right
 *            edge, and on a wide or full screen window they share one row.
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

    static JComponent build(StudioController controller, StudioActions actions,
                            ControlBindings bindings) {
        JPanel bar = new JPanel(new WrapLayout(FlowLayout.LEFT, Theme.SPACE_3, Theme.SPACE_1));
        bar.setBackground(Theme.SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(Theme.SPACE_1, 0, Theme.SPACE_1, 0)));

        JPanel file = group();
        file.add(toolButton(actions.importImage, "Import"));
        file.add(toolButton(actions.save, "Save"));
        bar.add(file);

        JPanel grayscale = group();
        grayscale.add(caption("Grayscale:"));
        addGrayscaleButtons(grayscale, controller, bindings);
        bar.add(grayscale);

        JPanel flips = group();
        flips.add(caption("Flip:"));
        addFlipButtons(flips, controller, bindings);
        bar.add(flips);

        JPanel edit = group();
        edit.add(compareButton(controller, bindings));
        edit.add(toolButton(actions.reset, "Reset"));
        bar.add(edit);

        JPanel view = group();
        view.add(toolButton(actions.fit, "Fit"));
        view.add(toolButton(actions.actualSize, "100%"));
        bar.add(view);

        // The wrap height depends on the width, so re-measure after every resize.
        bar.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                bar.revalidate();
            }
        });
        return bar;
    }

    /**
     * One section of the toolbar. A section never splits across rows, so when
     * the window is narrow whole groups wrap together and a label never ends
     * up separated from its buttons.
     */
    private static JPanel group() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACE_1, 0));
        panel.setOpaque(false);
        return panel;
    }

    /** Same ButtonGroup idea as the menu, so one method is always showing. */
    private static void addGrayscaleButtons(JPanel bar, StudioController controller,
                                            ControlBindings bindings) {
        ButtonGroup group = new ButtonGroup();
        for (GrayscaleMethod method : GrayscaleMethod.values()) {
            JToggleButton button = new JToggleButton(method.label());
            button.setToolTipText(method.formula() + "   (key " + method.accelerator() + ")");
            style(button);
            button.addActionListener(e -> controller.applyGrayscale(method));
            group.add(button);
            bar.add(button);
            bindings.bindGrayscale(method, button);
        }
    }

    private static void addFlipButtons(JPanel bar, StudioController controller,
                                       ControlBindings bindings) {
        JToggleButton horizontal = new JToggleButton("Flip Horizontal \u2194");
        horizontal.setToolTipText(
                "Mirror left to right, reflecting across the vertical axis   (key H)");
        style(horizontal);
        horizontal.addActionListener(e -> controller.toggleFlipHorizontal());

        JToggleButton vertical = new JToggleButton("Flip Vertical \u2195");
        vertical.setToolTipText(
                "Mirror top to bottom, reflecting across the horizontal axis   (key V)");
        style(vertical);
        vertical.addActionListener(e -> controller.toggleFlipVertical());

        bar.add(horizontal);
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
        style(button);
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
        style(button);
        return button;
    }

    /**
     * Gives a toolbar button a visible outline and fill on every look and feel.
     * <p>
     * A toolbar draws its buttons flat by default, so an unselected toggle
     * showed as plain text and did not look clickable. Here every button gets
     * an outline, and a selected or pressed one fills with the accent colour,
     * so the chosen grayscale method and active flips can be seen at a glance.
     * <p>
     * The colours follow the button model, which also changes when
     * ControlBindings selects or disables a button from the menu side, so the
     * toolbar and the menu can never show different states.
     */
    private static void style(AbstractButton button) {
        button.setFocusable(false);
        button.setContentAreaFilled(false);          // stop the look and feel's flat paint
        button.setOpaque(true);                      // ...and paint our own background
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BUTTON_OUTLINE),
                BorderFactory.createEmptyBorder(Theme.BUTTON_PAD_Y, Theme.BUTTON_PAD_X,
                        Theme.BUTTON_PAD_Y, Theme.BUTTON_PAD_X)));

        Runnable paintState = () -> {
            boolean on = button.isEnabled()
                    && (button.isSelected() || button.getModel().isPressed());
            button.setBackground(on ? Theme.ACCENT : Theme.SURFACE);
            button.setForeground(on ? Theme.ON_ACCENT : Theme.TEXT);
        };
        button.getModel().addChangeListener(e -> paintState.run());
        paintState.run();
    }

    private static JLabel caption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.small());
        label.setForeground(Theme.TEXT_MUTED);
        return label;
    }
}
