/**
 * Task Manager, a desktop to-do list: JavaFX screens over Spring services, its tasks in one SQLite file on this
 * computer.
 *
 * <ul>
 *   <li>{@code ui}: the JavaFX front: the window and its FXML controllers;</li>
 *   <li>{@code services}: what the app does with tasks, and its rules; the only callers of repositories;</li>
 *   <li>{@code repositories}: Spring Data repositories;</li>
 *   <li>{@code entities}: what is kept: tasks and their statuses;</li>
 *   <li>{@code shared}: what every part uses: the folder the database lives in.</li>
 * </ul>
 */
package io.mcs.taskmanager;
