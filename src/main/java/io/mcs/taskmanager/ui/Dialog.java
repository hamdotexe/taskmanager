package io.mcs.taskmanager.ui;

import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

/**
 * A dialog over the window: its title and note, its fields, and what its buttons do. A save the service refuses
 * leaves the dialog open with the service's words under the fields, unless the task it was opened on has gone, deleted
 * in another copy of the app: then it closes, and the window says so over the list. A save the service takes closes
 * it, and the window shows what was saved.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
public abstract class Dialog {

    /**
     * Makes a dialog; each kind says what it holds.
     */
    protected Dialog() {
    }

    /**
     * The dialog's heading.
     *
     * @return the title, such as "Edit task"
     */
    public abstract String title();

    /**
     * What the title alone does not say.
     *
     * @return the note under the title; empty for none
     */
    public String note() {
        return "";
    }

    /**
     * The fields, the same ones each time it is asked, so what was typed stays when a refusal is shown.
     *
     * @return the fields, or {@code null} for a dialog that only asks a question
     */
    public @Nullable Node fields() {
        return null;
    }

    /**
     * The field the keyboard starts in when the dialog opens.
     *
     * @return the field, or {@code null} to leave the focus on the dialog's buttons
     */
    public @Nullable Node firstFocus() {
        return null;
    }

    /**
     * What the main button says.
     *
     * @return the label, "Save" unless the dialog says otherwise
     */
    public String saveLabel() {
        return "Save";
    }

    /**
     * Whether the main button does something that cannot be undone, which makes it red.
     *
     * @return {@code true} for a delete
     */
    public boolean destructive() {
        return false;
    }

    /**
     * What the quiet red button at the dialog's foot says, if it has one.
     *
     * @return the label, or {@code null} for no such button
     */
    public @Nullable String deleteLabel() {
        return null;
    }

    /**
     * Saves what the fields say.
     *
     * @throws io.mcs.taskmanager.services.Refused with the reason, in words to show, if the service will not save it
     */
    public abstract void save();

    /**
     * What follows a save, once the dialog has closed, such as showing the saved task in the list.
     */
    public abstract void afterSave();

    /**
     * What the quiet red button does, for a dialog that has one: the edit form asks to delete its task.
     */
    public void delete() {
    }

    /**
     * Whether the task the dialog was opened on has gone since, so that a refusal is about that and not about what
     * was typed. A new task's dialog never has one.
     *
     * @return {@code true} if the task no longer exists
     */
    public boolean taskGone() {
        return false;
    }
}
