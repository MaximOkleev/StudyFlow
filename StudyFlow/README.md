# StudyFlow


## Semester workspace

This build ships with your semester subjects preloaded, including course codes, assessment type and classroom hours. It still contains no tasks, notes, focus sessions or habits, so you can add your own work manually.

If you clear the database and later want to restore the subject list, open **Settings → Load semester subjects**. The same list is also included as `sample_data/semester_subjects.csv`.

Current preloaded subject count: **20**. Total listed classroom hours: **993 ч**.

If an older local database already exists from a previous build, open Settings and use **Clear all data**, or delete the local folder manually:

```text
%USERPROFILE%\.studyflow
```

StudyFlow is a local-first desktop study planner built with Kotlin and Compose Desktop.

## What is included

- Dashboard, subjects, tasks, calendar, notes, focus timer, statistics and settings.
- SQLite persistence at `~/.studyflow/studyflow.sqlite`.
- Automatic migration from the older `studyflow.properties` file if it exists.
- Native desktop notification on timer completion, with a beep fallback.
- Kanban board with draggable task cards.
- Recurring tasks: no repeat, daily, weekly, monthly.
- Daily habit tracking with streaks.
- CSV export/import for subjects and tasks.
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


## Native-access warning

Newer JDKs may print a warning about `System::load` from Skiko when running Compose Desktop. The Gradle config passes `--enable-native-access=ALL-UNNAMED` for `:app:run` and packaged builds, so the warning should be suppressed. If the warning still appears in an IDE run configuration, add this VM option manually:

```text
--enable-native-access=ALL-UNNAMED
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

## CSV subject import format

Subject import expects a header and columns close to:

```csv
id,name,description,color_hex,icon
1,Теория игр,"Код: Б1-ОПМ.Б.7 • Аттестация: Атт/Экз • Всего: 45 ч",#A78BFA,ТИ
```

A ready file is included at:

```text
sample_data/semester_subjects.csv
```

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

### Long subject names

The app now uses full-width dropdown selectors for subject filters and subject pickers. Long semester discipline names are shown in full instead of being cut to 12–16 characters. This affects Tasks, task/note dialogs and the Focus Timer.

## Session schedule

The app includes the full current session schedule from the uploaded table. It stores only what is useful inside the planner:

- date and time;
- subject;
- teacher list.

Rooms and control-type labels such as `АТТ`, `ЗАЧ`, `З/О`, `ЭКЗ` are intentionally not shown in the UI.

Open **Session** in the sidebar to see the full list. Events are also shown in **Calendar** on their dates and in the dashboard quick diagnosis block.

If you already have an old local SQLite database, open **Settings → Load session schedule**. This reloads the bundled session schedule and removes the older short five-exam seed.

The source CSV is included at:

```text
sample_data/exam_schedule.csv
```

