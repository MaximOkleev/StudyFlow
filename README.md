# StudyFlow

StudyFlow is a desktop planner for studying, built with Kotlin and Compose Desktop.

The project is already filled with working screens, local persistence, CRUD operations, a Pomodoro timer, notes, calendar, statistics, export tools and seed data. It is meant to be opened and developed further, not started from an empty template.

## Features

- Dark dashboard with study summary
- Subjects with colors, icons and progress
- Tasks with statuses, priorities, deadlines, estimated time and spent time
- Notes with subject links and tags
- Calendar month view with deadlines
- Focus timer with presets and session logging
- Statistics: weekly tasks, focus time by subject, progress and overdue counters
- Local storage in the user data folder using Java Properties files
- Export tasks to CSV
- Export notes to Markdown
- Export full backup to a readable text file
- Demo data reset

## Tech stack

- Kotlin
- Compose Desktop / Material 3
- Gradle
- Local file storage, no server required

## Run

Open the folder in IntelliJ IDEA as a Gradle project and run `studyflow.MainKt` or use:

```bash
cd StudyFlow
gradle :app:run
```

If you want a Gradle Wrapper later:

```bash
gradle wrapper
```

## Suggested next upgrades

- Replace the Properties storage with SQLite / SQLDelight
- Add drag-and-drop task ordering
- Add real date picker for deadlines
- Add native notifications
- Add charts with a dedicated charting library
- Add cloud sync only after the desktop MVP is stable

## Project structure

```text
app/src/main/kotlin/studyflow
├── App.kt
├── Main.kt
├── data
│   ├── LocalStore.kt
│   ├── SeedData.kt
│   └── StudyRepository.kt
├── domain/model
├── presentation
│   ├── components
│   ├── dialogs
│   ├── screens
│   └── theme
└── util
```
