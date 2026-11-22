package io.mcs.taskmanager.ui.kit;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * The pieces the window is built of, styled by {@code taskmanager.css}: text, buttons, key caps, fields' labels, and
 * the alert and notice a refusal is shown in. Black is kept for the one main action of a place; every other button
 * is white with a hairline.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
public final class Ui {

    /** Where the stylesheet is, beside the window's FXML. */
    private static final String STYLESHEET = "/io/mcs/taskmanager/ui/taskmanager.css";

    /**
     * Not made: the class only builds pieces.
     */
    private Ui() {
    }

    /**
     * The stylesheet's address, for the window's scene. Menus and tooltips take it from the scene they open over.
     *
     * @return the stylesheet's URL as text
     */
    public static String stylesheet() {
        return Objects.requireNonNull(Ui.class.getResource(STYLESHEET), STYLESHEET).toExternalForm();
    }

    /**
     * A line of text that keeps its whole width, never cut short with an ellipsis.
     *
     * @param text the text
     * @param styles its style classes, such as {@code "caption"}
     * @return the label
     */
    public static Label text(String text, String... styles) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styles);
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }

    /**
     * Text that wraps to its container's width instead of being cut short.
     *
     * @param text the text
     * @param styles its style classes
     * @return the label
     */
    public static Label wrapped(String text, String... styles) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styles);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    /**
     * A line of text that gives way when there is no room, ending in an ellipsis, as a long title in the list.
     *
     * @param text the text
     * @param styles its style classes
     * @return the label
     */
    public static Label shrinking(String text, String... styles) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styles);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    /**
     * A button that runs the action when clicked, or pressed with Enter or Space while it has focus.
     *
     * @param text what it says
     * @param action what it does
     * @param styles its style classes besides {@code btn}, such as {@code "secondary"}
     * @return the button
     */
    public static Button button(String text, Runnable action, String... styles) {
        Button button = new Button(text);
        button.getStyleClass().setAll("btn");
        button.getStyleClass().addAll(styles);
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnAction(_ -> action.run());
        return button;
    }

    /**
     * Gives a control a tooltip naming its shortcut, as "Add task (Ctrl+N)".
     *
     * @param control the control
     * @param text the tooltip's words
     * @param <C> the control's type
     * @return the same control
     */
    public static <C extends Control> C tip(C control, String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(400));
        control.setTooltip(tooltip);
        return control;
    }

    /**
     * A key as the keyboard shows it, as "Ctrl+N", in a small white cap.
     *
     * @param keys the key or keys
     * @return the cap
     */
    public static HBox keycap(String keys) {
        HBox cap = new HBox(text(keys, "keycap-text"));
        cap.getStyleClass().add("keycap");
        cap.setAlignment(Pos.CENTER);
        cap.setMaxHeight(Region.USE_PREF_SIZE);
        cap.setMaxWidth(Region.USE_PREF_SIZE);
        return cap;
    }

    /**
     * A field's label above it, as every form has them, with a note at its end such as a count of characters.
     *
     * @param label what the field holds, such as "Title"
     * @param end what sits at the label's end, or {@code null} for nothing
     * @param field the field
     * @return the label and the field, one above the other
     */
    public static VBox labelled(String label, @Nullable Node end, Node field) {
        HBox top = new HBox(8, text(label, "field-label"), grow());
        top.setAlignment(Pos.BASELINE_LEFT);
        if (end != null) {
            top.getChildren().add(end);
        }
        VBox labelled = new VBox(6, top, field);
        labelled.setMinWidth(0);
        return labelled;
    }

    /**
     * A refusal, in the service's own words, on the danger tint, as a dialog shows it under its fields.
     *
     * @param message the sentence
     * @return the alert
     */
    public static HBox alert(String message) {
        Pane icon = Icons.line(Icons.ALERT_14, 14, 1.6);
        HBox.setMargin(icon, new Insets(3, 0, 0, 0));
        Label text = wrapped(message);
        HBox.setHgrow(text, Priority.ALWAYS);
        HBox alert = new HBox(icon, text);
        alert.getStyleClass().add("alert");
        alert.setAlignment(Pos.TOP_LEFT);
        alert.setAccessibleRole(AccessibleRole.TEXT);
        alert.setAccessibleText(message);
        return alert;
    }

    /**
     * A sentence over the list that stays until dismissed, on the danger tint, as for a task that has gone.
     *
     * @param message the sentence
     * @param dismiss what Dismiss does
     * @return the notice
     */
    public static HBox notice(String message, Runnable dismiss) {
        Pane icon = Icons.line(Icons.ALERT_14, 14, 1.6);
        Label text = wrapped(message);
        HBox.setHgrow(text, Priority.ALWAYS);
        HBox notice = new HBox(icon, text, button("Dismiss", dismiss, "ghost", "size30"));
        notice.getStyleClass().add("notice");
        notice.setAccessibleRole(AccessibleRole.TEXT);
        notice.setAccessibleText(message);
        return notice;
    }

    /**
     * Space that takes what a row has left, pushing what follows to its end.
     *
     * @return the space
     */
    public static Region grow() {
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        return space;
    }

    /**
     * Centres a node in a square of the size, as an icon in its tile.
     *
     * @param inside the node
     * @param size the square's side, in pixels
     * @param styles the square's style classes
     * @return the square
     */
    public static StackPane tile(Node inside, double size, String... styles) {
        StackPane tile = new StackPane(inside);
        tile.getStyleClass().addAll(styles);
        tile.setMinSize(size, size);
        tile.setPrefSize(size, size);
        tile.setMaxSize(size, size);
        return tile;
    }

    /**
     * Draws a control's scrolling content straight to the window. JavaFX renders a scroll pane's viewport through a
     * cached bitmap, which at display scales such as 125% cuts the tops off its text; a text area scrolls through
     * one too. Every scroll pane and text area in the app goes through this.
     *
     * @param control the scroll pane or text area
     * @param <C> the control's type
     * @return the same control
     */
    public static <C extends Control> C uncached(C control) {
        Runnable off = () -> control.lookupAll(".viewport").forEach(viewport -> viewport.setCache(false));
        control.skinProperty().addListener((_, _, skin) -> {
            if (skin != null) {
                off.run();
            }
        });
        off.run();
        return control;
    }

    /**
     * Fixes a region's width, so a column keeps it whatever it holds.
     *
     * @param region the region
     * @param width its width, in pixels
     * @param <R> the region's type
     * @return the same region
     */
    public static <R extends Region> R fixWidth(R region, double width) {
        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
        return region;
    }
}
