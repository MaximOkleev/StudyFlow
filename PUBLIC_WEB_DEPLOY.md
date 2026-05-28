# Публикация StudyFlow по ссылке

Public web-версия — это папка `StudyFlowWeb`. Её можно выложить на любой static hosting и открыть по ссылке на чистом ноутбуке.

## Важно про данные

Публичный сайт не может читать локальный SQLite-файл desktop-приложения. Это ограничение браузера. Поэтому:

- desktop и локальный web через `gradlew.bat :app:runWeb` используют общую SQLite-базу;
- public web по ссылке использует `localStorage` браузера;
- стартовые предметы, сессия, преподаватели и аудитории одинаковые;
- личные задачи можно переносить через `Settings → Export JSON` и `Settings → Import JSON`.

## GitHub Pages

1. Создай GitHub-репозиторий.
2. Загрузи проект StudyFlow.
3. В репозитории открой `Settings → Pages`.
4. В `Source` выбери `GitHub Actions`.
5. Сделай push в `main` или `master`.
6. Workflow `.github/workflows/pages.yml` опубликует папку `StudyFlowWeb`.

Ссылка будет такого вида:

```text
https://ТВОЙ_ЛОГИН.github.io/ИМЯ_РЕПОЗИТОРИЯ/
```

## Vercel

1. Импортируй GitHub-репозиторий в Vercel.
2. Укажи root/output для статического сайта:

```text
Root Directory: StudyFlowWeb
Build Command: оставить пустым
Output Directory: .
```

3. Нажми Deploy.

## Netlify

1. Импортируй GitHub-репозиторий в Netlify.
2. Publish directory должен быть:

```text
StudyFlowWeb
```

3. Build command пустой.
