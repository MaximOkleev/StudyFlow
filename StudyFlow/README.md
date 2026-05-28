# StudyFlow

StudyFlow is a Kotlin Compose Desktop study planner. It combines subjects, tasks, notes, a focus timer, calendar view and progress statistics in one local desktop app.

## Features

- Dashboard with active tasks, overdue tasks and focus totals.
- Subjects with color, icon, description and progress.
- Tasks with status, priority, deadline date, estimate and spent time.
- Calendar with editable deadline cards.
- Notes with tags and subject links.
- Focus timer with real-time countdown, manual logging and session logging.
- Statistics for completed tasks and focus minutes.
- Local persistence in the user folder.
- CSV/Markdown export, readable backup and restorable raw backup.
- Delete/reset confirmations to avoid accidental data loss.

## Run

```bash
./gradlew :app:run
```

On Windows:

```bat
gradlew.bat :app:run
```

## Project structure

```text
app/src/main/kotlin/studyflow
├─ data          LocalStore, repository, seed data
├─ domain/model  Subject, StudyTask, Note, FocusSession
├─ presentation  Compose UI screens, cards and dialogs
└─ util          Date and color helpers
```

## Storage

Data is saved locally in:

```text
~/.studyflow/studyflow.properties
```

Exports and backups are written to the same folder.

## Suggested next upgrades

- Replace Properties storage with JSON or SQLite/SQLDelight.
- Add native desktop notifications for timer completion.
- Add drag-and-drop kanban task board.
- Add recurring tasks and daily habit tracking.
- Add import from CSV/Markdown.
