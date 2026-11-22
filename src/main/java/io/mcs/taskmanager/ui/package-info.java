/**
 * The JavaFX front: {@code DesktopApp}, which starts Spring and opens the window; the window's controller, a Spring
 * bean, with the task list, its filters and counts, and the search; the rows of the list; and the dialogs that open
 * over it to add, edit and delete a task. Screens call {@code TaskService}, never a repository, and show a refusal's
 * message as it is.
 */
package io.mcs.taskmanager.ui;
