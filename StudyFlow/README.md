# StudyFlow

**StudyFlow** — локальное desktop-приложение для планирования учёбы на Kotlin + Compose Desktop. Приложение хранит данные локально в SQLite, умеет работать с предметами, задачами, календарём, сессией, заметками, фокус-таймером, статистикой, Kanban-доской и привычками.

## Что есть в проекте

- Dashboard с ближайшими задачами и событиями сессии.
- Предметы семестра с кодами дисциплин и количеством часов.
- Задачи с дедлайнами, приоритетами, статусами и повторением.
- Calendar с задачами и событиями сессии.
- Session — расписание сессии: дата, время, предмет, преподаватели.
- Notes — заметки по предметам.
- Focus Timer с системным уведомлением после завершения.
- Statistics — базовая статистика по задачам и фокус-сессиям.
- Board — Kanban-доска с перетаскиванием задач между To do / In progress / Done.
- Habits — ежедневные привычки и streak.
- SQLite-хранилище вместо `.properties`.
- CSV/Markdown import/export.
- Raw backup/restore SQLite-базы.
- Сохранение размера окна.

## Стартовые данные

В сборку добавлены предметы текущего семестра и полное расписание сессии.

Предметы содержат:

- название дисциплины;
- код дисциплины;
- тип аттестации;
- лекции / практики / лабораторные;
- общее количество часов.

Расписание сессии содержит только полезные для планера данные:

- дату;
- время;
- предмет;
- преподавателей.

Кабинеты и служебные слова типа `АТТ`, `ЗАЧ`, `З/О`, `ЭКЗ` в интерфейсе не показываются.

Если у тебя уже была старая локальная база, зайди в **Settings** и нажми:

```text
Clear all data
Load semester subjects
Load session schedule
```

Или вручную удали папку:

```text
%USERPROFILE%\.studyflow
```

После этого приложение загрузит чистую базу с предметами и расписанием сессии.

## Запуск из исходников

Открой папку `StudyFlow`, где лежит `settings.gradle.kts`.

На Windows:

```powershell
gradlew.bat :app:run
```

На macOS/Linux:

```bash
./gradlew :app:run
```

При первом запуске Gradle может скачать зависимости. Для запуска из исходников нужен интернет, если зависимости ещё не лежат в локальном Gradle-кэше.

## Portable-запуск на другом Windows-ноутбуке без скачивания

Если нужно запустить StudyFlow на другом ноутбуке **без установки Gradle, Kotlin, JDK и без скачивания зависимостей**, нужно заранее собрать portable-версию на своём ноутбуке.

В корне проекта выполни:

```powershell
cd путь\к\StudyFlow
.\gradlew.bat :app:createDistributable
```

После сборки готовое приложение появится примерно здесь:

```text
StudyFlow\app\build\compose\binaries\main\app\StudyFlow
```

Именно эту папку `StudyFlow` нужно скопировать целиком на флешку или в архив. Не копируй только `.exe`, потому что рядом лежат runtime-файлы и библиотеки, без которых приложение не запустится.

Чтобы сделать zip-архив portable-версии, выполни:

```powershell
Compress-Archive `
  -Path .\app\build\compose\binaries\main\app\StudyFlow `
  -DestinationPath .\StudyFlow_portable_windows.zip `
  -Force
```

На другом Windows-ноутбуке:

```text
1. Распакуй StudyFlow_portable_windows.zip
2. Открой папку StudyFlow
3. Запусти StudyFlow.exe
```

Если внутри нет `StudyFlow.exe`, проверь подпапку:

```text
StudyFlow\bin\StudyFlow.exe
```

Главное правило: переносится вся собранная папка приложения, а не один exe-файл.

## Перенос данных на другой ноутбук

Данные StudyFlow хранятся отдельно от приложения:

```text
%USERPROFILE%\.studyflow
```

Обычно на твоём ноутбуке это может быть:

```text
C:\Users\Honor\.studyflow
```

Если хочешь перенести свои задачи, заметки, привычки и прогресс на другой ноутбук, скопируй папку `.studyflow` на новый компьютер в папку пользователя:

```text
C:\Users\ИМЯ_ПОЛЬЗОВАТЕЛЯ\.studyflow
```

Если хочешь начать на другом ноутбуке с чистой базы, эту папку переносить не нужно. После запуска можно нажать:

```text
Settings → Load semester subjects
Settings → Load session schedule
```

## Сборка установщика под текущую ОС

Для создания desktop-пакета под текущую систему:

```powershell
gradlew.bat :app:packageDistributionForCurrentOS
```

Артефакты появятся в:

```text
app\build\compose\binaries
```

Важно: Windows-сборку лучше собирать на Windows. Для macOS/Linux нужно собирать отдельные версии на соответствующих системах.

## Тесты

```powershell
gradlew.bat test
```

На macOS/Linux:

```bash
./gradlew test
```

## Хранилище

Основная база:

```text
%USERPROFILE%\.studyflow\studyflow.sqlite
```

Raw backup:

```text
%USERPROFILE%\.studyflow\studyflow_raw_backup.sqlite
```

Readable exports создаются с timestamp в названии, поэтому старые экспорты не перезаписываются.

## Import / Export

В приложении есть импорт и экспорт:

- Subjects CSV;
- Tasks CSV;
- Notes Markdown;
- Habits CSV;
- SQLite raw backup.

Готовые стартовые CSV лежат в:

```text
sample_data\semester_subjects.csv
sample_data\exam_schedule.csv
```

## Формат CSV для предметов

```csv
id,name,description,color_hex,icon
1,Теория игр,"Код: Б1-ОПМ.Б.7 • Аттестация: Атт/Экз • Всего: 45 ч",#A78BFA,ТИ
```

## Формат CSV для задач

```csv
id,subject,title,status,priority,deadline,estimated_minutes,spent_minutes,recurrence
1,Теория игр,Повторить билет 1,Todo,High,2026-06-14,60,0,No repeat
```

## Формат Markdown для заметок

```md
# Notes

## Первая заметка
Текст первой заметки.

## Вторая заметка
Текст второй заметки.
```

Если в файле нет заголовков `##`, весь Markdown импортируется как одна заметка.

## Native-access warning

На новых версиях JDK может появиться предупреждение про `System::load` от Skiko. Это не ошибка и не проблема проекта. Compose Desktop использует Skiko для нативной отрисовки интерфейса.

В Gradle уже добавлен VM option:

```text
--enable-native-access=ALL-UNNAMED
```

Если запускаешь приложение через IDE и предупреждение всё равно появляется, добавь этот параметр в VM options конфигурации запуска.

## Правила чистого репозитория

Не коммитить и не класть в финальный архив:

```text
.gradle/
build/
app/build/
.idea/
out/
*.iml
```

## Быстрая памятка

Запуск из исходников:

```powershell
gradlew.bat :app:run
```

Создание portable-версии:

```powershell
gradlew.bat :app:createDistributable
```

Папка portable-приложения:

```text
app\build\compose\binaries\main\app\StudyFlow
```

Создание zip для другого ноутбука:

```powershell
Compress-Archive `
  -Path .\app\build\compose\binaries\main\app\StudyFlow `
  -DestinationPath .\StudyFlow_portable_windows.zip `
  -Force
```
