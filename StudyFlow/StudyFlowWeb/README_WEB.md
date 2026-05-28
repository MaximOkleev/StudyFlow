# StudyFlow Web

Это локальная браузерная версия StudyFlow без сборки и без внешних зависимостей.

## Как запустить

### Вариант 1. Просто открыть файл

Открой файл:

```text
StudyFlowWeb\index.html
```

Можно открыть двойным кликом или через IntelliJ IDEA: правой кнопкой по `index.html` → **Open in Browser**.

### Вариант 2. Запустить как локальный сайт по ссылке

На Windows запусти:

```text
StudyFlowWeb\run-local.bat
```

Откроется ссылка:

```text
http://127.0.0.1:5173/
```

Этот способ удобнее, если хочешь показывать проект именно как сайт.

## Где хранятся данные

Web-версия хранит данные в `localStorage` браузера. Это отдельное хранилище от desktop-версии, которая использует SQLite.

Для переноса данных используй:

```text
Settings → Export JSON
Settings → Import JSON
```

## Что работает

- Dashboard
- Subjects
- Tasks
- Kanban Board
- Calendar
- Session
- Notes
- Focus Timer
- Statistics
- Habits
- Settings
- CSV/Markdown/JSON import/export
- Browser notifications для таймера

## Важно

Это не заменяет desktop-приложение на Kotlin. Это отдельная локальная web-версия, сделанная для запуска по ссылке и демонстрации в браузере.


## Общие данные с desktop-версией

Чтобы web-версия и desktop-версия использовали одинаковые данные, открывай сайт не двойным кликом по `index.html`, а через локальный сервер:

```bat
run-local.bat
```

или из корня проекта:

```powershell
gradlew.bat :app:runWeb
```

После запуска откроется ссылка:

```text
http://127.0.0.1:5173/
```

В этом режиме сайт читает и записывает ту же SQLite-базу, что и desktop-приложение:

```text
%USERPROFILE%\.studyflow\studyflow.sqlite
```

То есть задачи, предметы, заметки, привычки и события сессии будут общими. Если открыть `index.html` напрямую, сайт работает в запасном direct mode и хранит данные только в `localStorage` браузера.
