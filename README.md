# Task Manager

Authors: 
Hamza Rasool - SE 30 C
Abdullah Khan - SE 30 C


**A desktop to-do list for Windows.** Each task has a title, a description and a status: Pending, In Progress or
Completed. Tasks are added, listed, changed and deleted in one window, and kept in a SQLite file on this computer, so
they are still there the next time the app opens.

It is the Open-Ended Lab of Software Construction (BESE-30, MCS NUST): a JavaFX window over Spring services, built
with the JDK, Maven, Git and Javadoc.

## What it does

- **Add a task** with a title and a description. A new task starts as Pending.
- **View all tasks**, newest first. An empty list says so and how to add the first.
- **Update a task**: its title, description and status, in the edit form.
- **Delete a task**, after a question naming it.
- **Set the status** straight from the list, from the status pill on each row, or in the edit form.
- **Validation**: a blank title is refused with a message, and so is a title over 100 characters or a description
  over 1000. Both are trimmed before they are counted or saved. Every refusal is shown as a sentence, never as an
  error's trace.
- **Search** by title or description as you type, and **filter** by status from the sidebar. Both narrow the list
  together.
- **Counts** of all tasks and of each status, always in view in the sidebar.
- **Created and last-updated times** on every row.
- **Two copies at once.** The app can be open twice on the same database. A task deleted in one copy and then
  edited, deleted or re-statused in the other is reported there in plain words, and the list is read again.

### Controls and shortcuts

| Control | Where | Key |
|---|---|---|
| **Add task** | top right | Ctrl+N |
| **Edit** | on the row pointed at or chosen; or double-click a row; or right-click, **Edit task** | Enter on the chosen row |
| **Delete** | on the row pointed at or chosen; or right-click, **Delete task**; or **Delete task** in the edit form | Delete on the chosen row |
| **Status pill** (Pending, In Progress, Completed) | on every row: click it for the menu of the three | |
| **All tasks**, **Pending**, **In Progress**, **Completed** | sidebar, each with its count | |
| **Search titles and descriptions** | above the list | Ctrl+F; Esc clears it; Down goes to the list |
| **Refresh** | top right: reads the list again, as after the other copy changed it | F5 |

In a dialog, Enter saves from the title field, Ctrl+Enter saves from anywhere, and Esc cancels.

## Requirements

- **Windows 10 or 11.**
- **JDK 25.** `JAVA_HOME` should point to it.

Nothing else: the Maven wrapper (`mvnw`) downloads Maven, and Maven downloads JavaFX, Spring, Hibernate and the
SQLite driver, on first use.

## Run it

From the project folder, in Git Bash:

```bash
./mvnw spring-boot:run
```

In PowerShell or cmd, use `.\mvnw.cmd spring-boot:run`.

**From VS Code:** open this folder with the Extension Pack for Java installed, then press **F5**, or choose **Task
Manager** in Run and Debug. The configuration is in `.vscode/launch.json`, and the app's output shows in the Debug
Console.

**Two copies:** run `./mvnw spring-boot:run` in two terminals, or once from a terminal and once with F5. Both work on
the same database.

## Test it

```bash
./mvnw test
```

38 JUnit tests, none of which opens a window: the brief's seven test cases at the service level
(`BriefTestCasesTest`), the rules for titles, descriptions and statuses (`TaskRulesTest`), search, filter and counts
(`SearchAndCountsTest`), tasks surviving a restart (`RestartTest`), two copies on one database (`TwoCopiesTest`), the
database schema (`FoundationTest`), and the words the window shows (`TaskTextTest`). Each runs on a fresh database in
a temporary folder, never on yours.

## Documentation

```bash
./mvnw javadoc:javadoc
```

writes the HTML to `docs/javadoc/apidocs`: open `docs/javadoc/apidocs/index.html` in a browser. Every class,
constructor and method is documented, private ones included, with its parameters, what it returns and its authors.
The run checks every comment with doclint and fails on any warning. The generated pages are committed, so they are
in the repository without running anything.

## Where the data lives

Everything is kept for the signed-in Windows user in `%LOCALAPPDATA%\TaskManager`, that is
`C:\Users\<you>\AppData\Local\TaskManager`:

| File | What it is |
|---|---|
| `taskmanager.db` | The SQLite database: every task. |
| `taskmanager.db-wal`, `taskmanager.db-shm` | SQLite's working files while the app runs. Leave them alone. |

- **Start over with an empty list:** close every copy of the app, then delete the `TaskManager` folder. The next
  start makes a new, empty one.
- **Back up:** close the app, then copy `taskmanager.db`.

## How it is built

One process: a JavaFX window over a Spring application context, with no web server. Spring starts before the window
opens, so the database is migrated by the time it shows, and closes with the window.

- **`ui`:** `DesktopApp` starts Spring and opens the window. The window is FXML (`main-window.fxml`) whose controller,
  `MainWindowController`, is a Spring bean handed `TaskService`. `TaskCell` draws a row of the list, and `TaskDialog`
  and `DeleteTaskDialog` open over the window in `DialogFrame`. `ui.kit` holds the pieces and icons, and
  `taskmanager.css` the look, from the Intellidger design system, in Nunito.
- **`services`:** `TaskService` adds, lists, searches, counts, changes and deletes tasks, and holds every rule. A
  change it will not make throws `Refused`, whose message the window shows as it is. Screens call the service, never
  a repository.
- **`repositories`**, **`entities`:** Spring Data JPA over Hibernate, and the `Task` entity with its `TaskStatus`.
- **Persistence:** SQLite through one connection, in WAL mode, each transaction taking the write lock as it begins,
  so two copies of the app take turns rather than fail. Flyway owns the schema (`db/migration`); Hibernate only checks
  the entity against it.

## Tech stack

| Part | What | Version |
|---|---|---|
| Language | Java | 25 |
| UI | JavaFX (OpenJFX), FXML | 27.0.1 |
| Application | Spring Boot, no web server | 4.1.2 |
| Persistence | Hibernate ORM with the community SQLite dialect, Spring Data JPA | 7.4 |
| Database | SQLite through xerial's `sqlite-jdbc` | 3.53 |
| Schema | Flyway | 12.4 |
| Tests | JUnit Jupiter and AssertJ, through Spring Boot's test starters | 6.0, 3.27 |
| Documentation | Javadoc, through the Maven Javadoc plugin | 3.12 |
| Font | Nunito, SIL Open Font License (`src/main/resources/io/mcs/taskmanager/ui/fonts/OFL.txt`) | |
