# StudyFlow

StudyFlow is a local-first desktop study planner built with Kotlin and Compose Desktop.

## What is included

- Dashboard, subjects, tasks, calendar, notes, focus timer, statistics and settings.
- SQLite persistence at `~/.studyflow/studyflow.sqlite`.
- Automatic migration from the older `studyflow.properties` file if it exists.
- Native desktop notification on timer completion, with a beep fallback.
- Kanban board with draggable task cards.
- Recurring tasks: no repeat, daily, weekly, monthly.
- Daily habit tracking with streaks.
- CSV export/import for tasks.
- Markdown export/import for notes.
- CSV export for habits.
- SQLite raw backup/restore.
- Window size persistence.

## Run

```bash
./gradlew :app:run
```

On Windows:

```powershell
gradlew.bat :app:run
```

## Test

```bash
./gradlew test
```

## Package

```bash
./gradlew packageDistributionForCurrentOS
```

The generated desktop package is placed under `app/build/compose/binaries`.

## Storage

StudyFlow stores data in the user's home directory:

```text
~/.studyflow/studyflow.sqlite
```

Raw backups are saved as:

```text
~/.studyflow/studyflow_raw_backup.sqlite
```

Readable exports use timestamped filenames so old exports are not overwritten.

## CSV task import format

The importer expects a header and columns close to the app export format:

```csv
id,subject,title,status,priority,deadline,estimated_minutes,spent_minutes,recurrence
1,Mathematics,Repeat logarithms,Todo,High,2026-05-30,60,0,Daily
```

The importer mainly uses `subject`, `title`, `description/status` slot, `priority`, `deadline`, `estimated_minutes`, and `recurrence`. Unknown values fall back safely.

## Markdown note import format

Notes can be imported from Markdown headings:

```md
# Imported notes

## First note
Text of the first note.

## Second note
Text of the second note.
```

If the file has no `##` headings, the whole file is imported as one note.

## Clean repository rules

Do not commit generated directories:

```text
.gradle/
build/
app/build/
.idea/
out/
*.iml
```
