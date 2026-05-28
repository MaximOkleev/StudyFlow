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
