package io.mcs.taskmanager.ui.kit;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Transform;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The app's icons, drawn from SVG paths on a square of their size: 16px line icons at a 1.5 stroke beside a label,
 * and smaller marks inside controls. Their colour is the stylesheet's ({@code .icon} and the classes beside it), so a
 * control's state can change it. Also the Task Manager's mark, a short checklist on a white square.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
public final class Icons {

    /** Every task: three lines of a list. On a 16px square. */
    public static final String ALL = "M6 4h7.5M6 8h7.5M6 12h7.5M2.75 4h.01M2.75 8h.01M2.75 12h.01";

    /** Pending: a circle with a dash, not started. On a 16px square. */
    public static final String PENDING = "M8 2.5a5.5 5.5 0 1 1 0 11 5.5 5.5 0 0 1 0-11ZM5.5 8h5";

    /** In Progress: a clock, under way. On a 16px square. */
    public static final String IN_PROGRESS = "M8 2.5a5.5 5.5 0 1 1 0 11 5.5 5.5 0 0 1 0-11ZM8 5.25V8l2 1.25";

    /** Completed: a circle with a check. On a 16px square. */
    public static final String COMPLETED = "M8 2.5a5.5 5.5 0 1 1 0 11 5.5 5.5 0 0 1 0-11ZM5.4 8.1l1.8 1.8 3.4-3.7";

    /** A magnifying glass, in the search field. On a 16px square. */
    public static final String SEARCH = "M7.25 2.75a4.5 4.5 0 1 1 0 9 4.5 4.5 0 0 1 0-9ZM10.5 10.5l3 3";

    /** Pending, small, inside a status pill. On a 14px square. */
    public static final String PENDING_14 = "M7 1.75a5.25 5.25 0 1 1 0 10.5 5.25 5.25 0 0 1 0-10.5ZM4.75 7h4.5";

    /** In Progress, small, inside a status pill. On a 14px square. */
    public static final String IN_PROGRESS_14 = "M7 1.75a5.25 5.25 0 1 1 0 10.5 5.25 5.25 0 0 1 0-10.5ZM7 4.4V7l1.8 1.1";

    /** Completed, small, inside a status pill. On a 14px square. */
    public static final String COMPLETED_14 = "M7 1.75a5.25 5.25 0 1 1 0 10.5 5.25 5.25 0 0 1 0-10.5ZM4.75 7.1l1.5 1.5"
            + " 3-3.2";

    /** An exclamation in a circle, leading a refusal. On a 14px square. */
    public static final String ALERT_14 = "M7 1.75a5.25 5.25 0 1 1 0 10.5 5.25 5.25 0 0 1 0-10.5ZM7 4.4v3.1M7 9.55v.05";

    /** Two arrows turning, on Refresh. On a 14px square. */
    public static final String REFRESH_14 = "M11.6 6.2A4.75 4.75 0 0 0 3 4.4M2.4 7.8A4.75 4.75 0 0 0 11 9.6"
            + "M3 2v2.6h2.6M11 12V9.4H8.4";

    /** A plus, on Add task. On a 12px square. */
    public static final String ADD = "M6 1.5v9M1.5 6h9";

    /** A chevron pointing down, after a status that opens a menu. On a 12px square. */
    public static final String CHEVRON_DOWN = "M3.5 4.75 6 7.25 8.5 4.75";

    /** A check, before the status a task has now in the status menu. On a 12px square. */
    public static final String CHECK = "M2.5 6.2 5 8.5 9.5 3.5";

    /** The mark's size, the same as the header's other marks in the design system. */
    private static final double MARK_SIZE = 28;

    /** The sizes the window's icon is drawn at, for the taskbar, Alt+Tab and the title of the window. */
    private static final List<Integer> ICON_SIZES = List.of(16, 20, 24, 32, 40, 48, 64, 128, 256);

    /**
     * Not made: the class only draws icons.
     */
    private Icons() {
    }

    /**
     * A line icon on a square of the size, coloured by the style classes given.
     *
     * @param path the icon's SVG path, drawn on a square of {@code size}
     * @param size the square's side, in pixels
     * @param strokeWidth the width of its lines, in pixels
     * @param styles more style classes, such as {@code "ink"} for the icon of a current item
     * @return the icon, a pane of exactly that size
     */
    public static Pane line(String path, double size, double strokeWidth, String... styles) {
        SVGPath svg = new SVGPath();
        svg.setContent(path);
        svg.setStrokeWidth(strokeWidth);
        svg.getStyleClass().add("icon");
        svg.getStyleClass().addAll(styles);
        return square(svg, size);
    }

    /**
     * The Task Manager's mark: a checklist of three rows on a white rounded square, the first two ticked off in the
     * main action's colour, the third still open.
     *
     * @return the mark, 28 pixels square
     */
    public static Pane mark() {
        Pane mark = new Pane();
        mark.getChildren().addAll(
                rect(0.5, 0.5, 27, 27, 7.5, Color.web("#ffffff"), Color.web("#e4e4e7")),
                rect(6, 7, 4, 4, 1.5, Color.web("#232326"), null),
                rect(12, 7, 10, 4, 1.5, Color.web("#d4d4d8"), null),
                rect(6, 12.5, 4, 4, 1.5, Color.web("#232326"), null),
                rect(12, 12.5, 8, 4, 1.5, Color.web("#d4d4d8"), null),
                rect(6, 18, 4, 4, 1.5, Color.web("#d4d4d8"), null),
                rect(12, 18, 10, 4, 1.5, Color.web("#d4d4d8"), null));
        fix(mark, MARK_SIZE);
        return mark;
    }

    /**
     * The mark drawn at each size Windows asks a window's icon for, so the taskbar and Alt+Tab show it sharp.
     *
     * @return one image for each size, smallest first
     */
    public static List<Image> markImages() {
        List<Image> images = new ArrayList<>();
        for (int size : ICON_SIZES) {
            Node mark = mark();
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            parameters.setTransform(Transform.scale(size / MARK_SIZE, size / MARK_SIZE));
            images.add(mark.snapshot(parameters, null));
        }
        return images;
    }

    /**
     * A rounded rectangle of the mark.
     *
     * @param x its left edge
     * @param y its top edge
     * @param width its width
     * @param height its height
     * @param radius the radius of its corners
     * @param fill its colour
     * @param stroke its edge's colour, or {@code null} for no edge
     * @return the rectangle
     */
    private static Rectangle rect(double x, double y, double width, double height, double radius, Color fill,
            @Nullable Color stroke) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect.setArcWidth(radius * 2);
        rect.setArcHeight(radius * 2);
        rect.setFill(fill);
        if (stroke != null) {
            rect.setStroke(stroke);
            rect.setStrokeWidth(1);
        }
        return rect;
    }

    /**
     * Puts a shape on a square of the size, so icons line up whatever their path's own bounds.
     *
     * @param shape the shape, drawn in the square's own coordinates
     * @param size the square's side, in pixels
     * @return the square holding the shape
     */
    private static Pane square(SVGPath shape, double size) {
        Pane square = new Pane(shape);
        fix(square, size);
        return square;
    }

    /**
     * Fixes a pane to a square of the size, so a layout never stretches or squeezes it.
     *
     * @param pane the pane
     * @param size the square's side, in pixels
     */
    private static void fix(Pane pane, double size) {
        pane.setMinSize(size, size);
        pane.setPrefSize(size, size);
        pane.setMaxSize(size, size);
    }
}
