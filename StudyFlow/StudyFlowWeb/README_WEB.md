# StudyFlow Web

Это браузерная версия StudyFlow.

## Режим 1. Public/direct web

Можно открыть `index.html` напрямую или выложить папку `StudyFlowWeb` на GitHub Pages/Vercel/Netlify.

В этом режиме сайт работает на чистом ноутбуке по ссылке без установки зависимостей. Данные хранятся в `localStorage` браузера.

## Режим 2. Local SQLite sync

Если нужно, чтобы web и desktop использовали одну SQLite-базу, запускай web через локальный Kotlin-сервер:

```powershell
gradlew.bat :app:runWeb
```

или:

```text
run-web.bat
```

Откроется:

```text
http://127.0.0.1:5173/
```

В этом режиме web работает с той же базой:

```text
%USERPROFILE%\.studyflow\studyflow.sqlite
```

## Календарь

В календаре наведи курсор на событие сессии. Появится подсказка с предметом, датой, временем, преподавателем и аудиторией.

## Публикация по ссылке

### GitHub Pages

1. Загрузи проект в GitHub-репозиторий.
2. Включи GitHub Pages через GitHub Actions.
3. Workflow `.github/workflows/pages.yml` опубликует папку `StudyFlowWeb`.

### Vercel

Импортируй репозиторий и укажи:

```text
Root Directory: StudyFlowWeb
Build Command: оставить пустым
Output Directory: .
```

После деплоя получишь публичную ссылку.

## Calendar event layout fix

Calendar event labels are clipped with ellipsis inside each day cell, so long subject names no longer overlap neighboring days. Full session details are shown in the hover tooltip, including time, teachers and room.


## Calendar hover/click details

В календаре события сессии показывают подробную карточку при наведении мыши. По клику карточка фиксируется, это удобно на тачпадах и сенсорных экранах.
