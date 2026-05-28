# StudyFlow Project Plan

## Current focus

This version is based on the uploaded StudyFlow project and extends it directly, instead of replacing it with a separate unrelated build.

## Implemented upgrades

- Replaced Properties storage with SQLite via JDBC.
- Added migration from legacy `studyflow.properties` into SQLite.
- Added native desktop notification when the timer completes.
- Added Kanban board with horizontal drag gestures between task statuses.
- Added recurring tasks with automatic next-instance generation.
- Added daily habit tracking with streak calculation.
- Added tasks CSV import and notes Markdown import.
- Added habits CSV export and SQLite raw backup/restore.
- Preserved existing screens, tests and project structure where possible.

## Next improvements

- Replace manual path text fields with native file picker dialogs.
- Add SQLDelight code generation after the schema stabilizes.
- Add proper UI tests for Kanban drag gestures and habit tracking.
- Add reminder notifications for upcoming deadlines.
- Add tag filters for notes and tasks.
- Add cloud sync only after local-first behavior is fully stable.
