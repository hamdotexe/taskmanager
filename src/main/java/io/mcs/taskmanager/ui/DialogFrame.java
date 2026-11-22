package io.mcs.taskmanager.ui;

import io.mcs.taskmanager.services.Refused;
import io.mcs.taskmanager.ui.kit.Ui;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderDragType;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What every dialog sits in: the dimmed window, and a white card with the dialog's title, note, fields, the reason a
 * save was refused, and Cancel and the main button at its foot. Enter saves from a one-line field, Ctrl+Enter from
 * anywhere in the dialog, and Escape cancels.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
final class DialogFrame {

    /** The widest the card grows, however wide the window. */
    private static final double CARD_WIDTH = 560;

    /** The keys that save from anywhere in the dialog, the description's several lines included. */
    private static final KeyCombination SAVE_KEYS = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN);

    /** Where an unexpected failure is written, for whoever looks into it. */
    private static final Logger LOG = LoggerFactory.getLogger(DialogFrame.class);

    /** The dialog shown. */
    private final Dialog dialog;

    /** The window the dialog is over, which closes it and shows what follows. */
    private final MainWindowController window;

    /** The white card holding the dialog. */
    private final VBox card = new VBox();

    /** The dimmed layer over the window, which scrolls the card should the window be too short for it. */
    private final ScrollPane overlay;

    /** Why the last save was refused; empty before one. */
    private String error = "";

    /**
     * Frames a dialog over the window.
     *
     * @param dialog the dialog
     * @param window the window it opens over
     */
    DialogFrame(Dialog dialog, MainWindowController window) {
        this.dialog = dialog;
        this.window = window;
        card.getStyleClass().add("dialog");
        card.setMaxWidth(CARD_WIDTH);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (SAVE_KEYS.match(event)) {
                event.consume();
                save();
            }
        });
        draw();
        StackPane centred = new StackPane(card);
        centred.setPadding(new Insets(24));
        centred.setAlignment(Pos.CENTER);
        overlay = Ui.uncached(new ScrollPane(centred));
        overlay.getStyleClass().add("backdrop");
        overlay.setFitToWidth(true);
        overlay.setFitToHeight(true);
        overlay.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // the dimmed header still drags the window; the card never does, should it reach up into the header
        HeaderBar.setDragType(overlay, HeaderDragType.TRANSPARENT_SUBTREE);
        HeaderBar.setDragType(card, HeaderDragType.NONE);
    }

    /**
     * The dimmed layer with the card on it, for the window to lay over everything else.
     *
     * @return the layer
     */
    Node overlay() {
        return overlay;
    }

    /**
     * Puts the keyboard where the dialog starts: its first field, or the card itself for a question. The layer's
     * stylesheet is applied first: until then the scroll pane has no skin, so the card is not yet in the window and
     * could not take the focus.
     */
    void focus() {
        overlay.applyCss();
        Node first = dialog.firstFocus();
        if (first != null) {
            first.requestFocus();
        } else {
            card.requestFocus();
        }
    }

    /**
     * Whether a node is part of the dialog, so the window can keep the keyboard inside it.
     *
     * @param node the node that has the focus
     * @return {@code true} if it is the card or anything in it
     */
    boolean holds(Node node) {
        for (Node at = node; at != null; at = at.getParent()) {
            if (at == overlay) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lays out the card afresh: the heading, the fields, the refusal if there is one, and the buttons.
     */
    private void draw() {
        VBox head = new VBox(4, Ui.wrapped(dialog.title(), "heading"));
        String note = dialog.note();
        if (!note.isEmpty()) {
            head.getChildren().add(Ui.wrapped(note, "caption"));
        }
        card.getChildren().setAll(head);
        Node fields = dialog.fields();
        if (fields != null) {
            card.getChildren().add(fields);
        }
        if (!error.isEmpty()) {
            card.getChildren().add(Ui.alert(error));
        }
        HBox foot = new HBox(8);
        foot.setAlignment(Pos.CENTER_LEFT);
        foot.setPadding(new Insets(4, 0, 0, 0));
        String deleteLabel = dialog.deleteLabel();
        if (deleteLabel != null) {
            foot.getChildren().add(Ui.button(deleteLabel, () -> dialog.delete(), "danger-text"));
        }
        Button cancel = Ui.button("Cancel", () -> window.closeDialog(), "secondary");
        cancel.setCancelButton(true);
        Button save = Ui.button(dialog.saveLabel(), () -> save(), dialog.destructive() ? "destructive" : "primary");
        save.setDefaultButton(true);
        foot.getChildren().addAll(Ui.grow(), cancel, save);
        card.getChildren().add(foot);
    }

    /**
     * Saves through the dialog. A refusal stays in the dialog under its fields, except that a task which has gone
     * closes it and is reported over the list, where it has disappeared. A failure nobody expected is written to the
     * log and shown as a plain sentence, never as an error's trace.
     */
    private void save() {
        try {
            dialog.save();
        } catch (Refused refused) {
            String reason = String.valueOf(refused.getMessage());
            if (dialog.taskGone()) {
                window.closeDialog();
                window.gone(reason);
                return;
            }
            refuse(reason);
            return;
        } catch (RuntimeException failure) {
            LOG.error("A dialog's save failed", failure);
            refuse(MainWindowController.FAILED);
            return;
        }
        window.closeDialog();
        dialog.afterSave();
    }

    /**
     * Shows why a save did not happen under the fields, and puts the keyboard back in the first of them.
     *
     * @param reason the sentence to show
     */
    private void refuse(String reason) {
        error = reason;
        draw();
        focus();
    }
}
