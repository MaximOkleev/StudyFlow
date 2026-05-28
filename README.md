# StudyFlow

**StudyFlow** — учебный планер на Kotlin + Compose Desktop с дополнительной web-версией. В проекте есть desktop-приложение, локальный web-сайт с общей SQLite-базой и отдельная public web-версия, которую можно выложить на GitHub Pages/Vercel и открыть по ссылке на чистом ноутбуке.

## Что есть в приложении

- Dashboard с ближайшими задачами и событиями сессии.
- Subjects — предметы семестра с кодами, часами и описанием.
- Tasks — задачи с дедлайнами, статусами, приоритетами и повторением.
- Board — Kanban-доска с перетаскиванием задач.
- Calendar — календарь задач и событий сессии.
- Session — полное расписание сессии.
- Notes — заметки по предметам.
- Focus Timer — таймер с уведомлением.
- Statistics — статистика по задачам и фокусу.
- Habits — ежедневные привычки и streak.
- Settings — импорт, экспорт, backup, загрузка стартовых данных.

## Стартовые данные

В проект уже добавлены:

- 20 предметов текущего семестра;
- полное расписание сессии;
- преподаватели;
- аудитории/кабинеты для событий сессии.

В интерфейсе не показываются служебные метки типа `АТТ`, `ЗАЧ`, `З/О`, `ЭКЗ`. Они не нужны для планера.

Файлы со стартовыми данными лежат здесь:

```text
sample_data/semester_subjects.csv
sample_data/exam_schedule.csv
```

Если нужно перезагрузить стартовые данные:

```text
Settings → Load semester subjects
Settings → Load session schedule
```

Если нужно полностью очистить базу:

```text
Settings → Clear all data
```

Либо удалить папку вручную:

```text
%USERPROFILE%\.studyflow
```

## Подсказки в календаре

В календаре события сессии отображаются прямо внутри дня. Если навести курсор на предмет, появится карточка с подробностями:

- предмет;
- дата;
- время;
- аудитория;
- преподаватель;
- описание предмета, если оно есть.

Аудитории также видны в карточках Session и в web-версии.

## Desktop-запуск из исходников

Открой папку `StudyFlow`, где лежит `settings.gradle.kts`.

Windows:

```powershell
gradlew.bat :app:run
```

macOS/Linux:

```bash
./gradlew :app:run
```

При первом запуске Gradle может скачать зависимости. Если их ещё нет в локальном Gradle-кэше, нужен интернет.

## Локальный web с общей базой desktop-версии

Этот режим нужен, если web и desktop должны видеть одни и те же данные.

Запуск из корня проекта:

```powershell
gradlew.bat :app:runWeb
```

Или двойным кликом:

```text
run-web.bat
```

Откроется ссылка:

```text
http://127.0.0.1:5173/
```

В этом режиме web-версия читает и пишет ту же SQLite-базу, что desktop-приложение:

```text
%USERPROFILE%\.studyflow\studyflow.sqlite
```

Важно: это локальный режим. Он работает на твоём ноутбуке и даёт общие данные между desktop и web.

## Public web-версия по ссылке

Этот режим нужен, если хочешь открыть StudyFlow на чистом ноутбуке просто по ссылке, без установки Gradle, JDK, Kotlin, Node.js и без скачивания проекта.

Для этого нужно выложить папку:

```text
StudyFlowWeb
```

на любой static hosting:

- GitHub Pages;
- Vercel;
- Netlify;
- любой обычный web-сервер.

После публикации можно открыть ссылку с любого ноутбука, и сайт будет работать в браузере.

Ограничение: браузерная public-версия не может напрямую читать SQLite-файл с твоего desktop-приложения. Это запрещено браузером по безопасности. Поэтому public web хранит данные в `localStorage` конкретного браузера. Стартовые предметы и сессия будут такими же, но личные задачи между desktop и public web автоматически не синхронизируются.

Для переноса данных используй:

```text
Settings → Export JSON
Settings → Import JSON
```

## Быстрая публикация через GitHub Pages

1. Создай репозиторий на GitHub.
2. Загрузи туда проект StudyFlow.
3. Убедись, что в репозитории есть папка `.github/workflows/pages.yml`.
4. Открой GitHub → Settings → Pages.
5. В Source выбери GitHub Actions.
6. После выполнения workflow сайт появится по ссылке вида:

```text
https://ТВОЙ_ЛОГИН.github.io/ИМЯ_РЕПОЗИТОРИЯ/
```

Workflow публикует именно папку `StudyFlowWeb`.

## Быстрая публикация через Vercel

1. Зайди на Vercel.
2. Импортируй GitHub-репозиторий.
3. В настройках проекта укажи:

```text
Root Directory: StudyFlowWeb
Build Command: оставить пустым
Output Directory: оставить пустым или .
```

4. Нажми Deploy.
5. Получишь публичную ссылку вида:

```text
https://studyflow-...vercel.app
```

## Portable desktop-версия без скачивания

Если нужно перенести desktop-приложение на другой Windows-ноутбук без установки Gradle/JDK/Kotlin, собери distributable:

```powershell
gradlew.bat :app:createDistributable
```

Готовая папка появится примерно здесь:

```text
app\build\compose\binaries\main\app\StudyFlow
```

Копируй всю папку `StudyFlow`, а не только `.exe`.

Можно сразу сделать zip:

```powershell
Compress-Archive `
  -Path .\app\build\compose\binaries\main\app\StudyFlow `
  -DestinationPath .\StudyFlow_portable_windows.zip `
  -Force
```

На другом ноутбуке:

```text
1. Распаковать архив.
2. Открыть папку StudyFlow.
3. Запустить StudyFlow.exe или bin\StudyFlow.exe.
```

## Перенос desktop-данных

Desktop-данные лежат здесь:

```text
%USERPROFILE%\.studyflow
```

Если нужно перенести задачи, заметки, привычки и прогресс на другой ноутбук, скопируй эту папку в профиль пользователя на новом ноутбуке.

## Сборка установщика

Windows:

```powershell
gradlew.bat :app:packageDistributionForCurrentOS
```

macOS/Linux:

```bash
./gradlew :app:packageDistributionForCurrentOS
```

Артефакты появятся здесь:

```text
app\build\compose\binaries
```

## Тесты

Windows:

```powershell
gradlew.bat test
```

macOS/Linux:

```bash
./gradlew test
```

## Хранилище

Основная SQLite-база desktop-версии:

```text
%USERPROFILE%\.studyflow\studyflow.sqlite
```

Raw backup:

```text
%USERPROFILE%\.studyflow\studyflow_raw_backup.sqlite
```

## Import / Export

Поддерживается:

- Subjects CSV;
- Tasks CSV;
- Notes Markdown;
- Habits CSV;
- JSON export/import в web-версии;
- SQLite raw backup/restore.

## Native-access warning

На новых JDK может появиться предупреждение про `System::load` от Skiko. Это не ошибка. Compose Desktop использует Skiko для нативной отрисовки.

В Gradle уже добавлен параметр:

```text
--enable-native-access=ALL-UNNAMED
```

Если запускаешь через IDE и предупреждение остаётся, добавь этот параметр в VM options.

## Чистый репозиторий

Не коммитить и не класть в финальный архив:

```text
.gradle/
build/
app/build/
.idea/
out/
*.iml
```


## Обычные задачи, дни рождения и праздники

В задачах теперь есть не только предметные задачи, но и обычные задачи без привязки к предмету. При создании задачи можно выбрать:

- конкретный предмет;
- пункт **Обычная задача без предмета**;
- быстрый шаблон: базовая задача, день рождения, праздник или учебная подготовка.

В стартовые данные добавлены ежегодные напоминания:

- мой день рождения — 9 июня;
- день рождения Вани — 20 января;
- день рождения Артёма — 4 января;
- основные нерабочие праздничные дни РФ: новогодние каникулы, Рождество, 23 февраля, 8 марта, 1 мая, 9 мая, 12 июня и 4 ноября.

Если база уже была создана раньше, открой:

```text
Settings → Load basic tasks / holidays
```

В Tasks есть фильтры:

```text
Все / Предметные / Обычные / Праздники и ДР
```
