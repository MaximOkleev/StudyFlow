# StudyFlow

StudyFlow is a Kotlin Compose Desktop study planner. It combines subjects, tasks, notes, a focus timer, calendar view and progress statistics in one local-first desktop app.

## Features

- Dashboard with active tasks, overdue tasks and focus totals.
- Subjects with color, icon, description and progress.
- Tasks with status, priority, deadline date, estimate, spent time and filters.
- Calendar with editable deadline cards.
- Notes with tags and subject links.
- Focus timer with real-time countdown, manual logging, beep notification and session logging.
- Statistics for completed tasks and focus minutes.
- Responsive sidebar: compact mode on smaller window widths.
- Window size persistence between launches.
- Atomic local persistence with backup file fallback.
- Timestamped CSV/Markdown/readable exports.
- Restorable raw backup with validation before restore.
- Delete/reset confirmations to avoid accidental data loss.
- JVM unit tests for storage and date helpers.

## Run

```bash
./gradlew :app:run
```

On Windows:

```bat
gradlew.bat :app:run
```

## Test

```bash
./gradlew test
```

On Windows:

```bat
gradlew.bat test
```

## Package

```bash
./gradlew packageDistributionForCurrentOS
```

Compose Desktop writes native packages under:

```text
app/build/compose/binaries
```

## Project structure

```text
app/src/main/kotlin/studyflow
├─ data          LocalStore, repository, seed data, window preferences
├─ domain/model  Subject, StudyTask, Note, FocusSession
├─ presentation  Compose UI screens, cards and dialogs
└─ util          Date and color helpers

app/src/test/kotlin/studyflow
├─ data          LocalStore tests
└─ util          Date utility tests
```

## Storage

Data is saved locally in:

```text
~/.studyflow/studyflow.properties
```

Window size is saved in:

```text
~/.studyflow/window.properties
```

A safety copy of the main data file is kept as:

```text
~/.studyflow/studyflow.properties.bak
```

Exports and backups are written to the same folder using timestamped filenames.

## Release hygiene

Do not commit or ship generated files:

```text
.gradle/
.idea/
build/
*/build/
out/
*.iml
```

## Suggested next upgrades

- Replace Properties storage with SQLite/SQLDelight.
- Add native desktop notifications instead of only beep on timer completion.
- Add drag-and-drop kanban task board.
- Add recurring tasks and daily habit tracking.
- Add import from CSV/Markdown.
