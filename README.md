# StudyFlow

**StudyFlow** — локальное приложение для планирования учёбы. В проекте теперь есть две версии:

1. **Desktop-версия** на Kotlin + Compose Desktop.
2. **Web-версия** в папке `StudyFlowWeb`, которую можно открыть в браузере как локальный сайт.

Desktop-версия хранит данные в SQLite. Web-версия хранит данные в localStorage браузера. Это два разных локальных хранилища.

---

## Что есть в проекте

- Dashboard с ближайшими задачами и событиями сессии.
- Предметы семестра с кодами дисциплин и количеством часов.
- Задачи с дедлайнами, приоритетами, статусами и повторением.
- Calendar с задачами и событиями сессии.
- Session — расписание сессии: дата, время, предмет, преподаватели.
- Notes — заметки по предметам.
- Focus Timer с уведомлением после завершения.
- Statistics — базовая статистика по задачам и фокус-сессиям.
- Board — Kanban-доска с перетаскиванием задач между To do / In progress / Done.
- Habits — ежедневные привычки и streak.
- Import/export через CSV, Markdown и JSON.

---

## Стартовые данные

В проект добавлены предметы текущего семестра и полное расписание сессии.

Предметы содержат:

- название дисциплины;
- код дисциплины;
- тип аттестации;
- лекции / практики / лабораторные;
- общее количество часов.

Расписание сессии содержит:

- дату;
- время;
- предмет;
- преподавателей.

Кабинеты и служебные слова типа `АТТ`, `ЗАЧ`, `З/О`, `ЭКЗ` в интерфейсе не показываются.

Готовые CSV-файлы лежат здесь:

```text
sample_data/semester_subjects.csv
sample_data/exam_schedule.csv
```

Если нужно заново загрузить стартовые данные в desktop-версии, открой **Settings** и нажми:

```text
Clear all data
Load semester subjects
Load session schedule
```

Если нужно заново загрузить стартовые данные в web-версии, открой **Settings** и нажми:

```text
Load semester subjects
Load session schedule
```

---

# Web-версия: запуск по ссылке

Web-версия находится в папке:

```text
StudyFlowWeb
```

Она не требует Gradle, Kotlin, JDK, npm, Node.js или скачивания зависимостей. Это обычный локальный сайт на HTML/CSS/JavaScript.

## Вариант 1. Открыть прямо в браузере

Открой файл:

```text
StudyFlowWeb/index.html
```

На Windows можно просто два раза нажать по `index.html`.

В IntelliJ IDEA можно открыть так:

```text
StudyFlowWeb/index.html → правой кнопкой мыши → Open in Browser
```

После этого сайт откроется в браузере и будет работать локально.

## Вариант 2. Запустить как локальный сайт по ссылке

На Windows запусти файл:

```text
StudyFlowWeb/run-local.bat
```

Он поднимет локальный сервер и откроет ссылку:

```text
http://127.0.0.1:5173/
```

Эту ссылку можно открыть в браузере. Сайт будет работать полностью локально на твоём ноутбуке.

Чтобы остановить локальный сервер, закрой окно терминала или нажми `Ctrl+C`.

## Как запустить web-версию через IDEA

Самый простой способ:

```text
1. Открой проект в IntelliJ IDEA.
2. Найди файл StudyFlowWeb/index.html.
3. Нажми по нему правой кнопкой мыши.
4. Выбери Open in Browser.
```

Если хочешь именно запуск через локальную ссылку:

```text
1. Открой встроенный терминал IDEA.
2. Перейди в папку StudyFlowWeb.
3. Выполни run-local.bat.
4. Открой http://127.0.0.1:5173/.
```

Команды:

```powershell
cd StudyFlowWeb
.\run-local.bat
```

---

# Desktop-версия: запуск из исходников

Desktop-версия запускается через Gradle.

На Windows:

```powershell
gradlew.bat :app:run
```

На macOS/Linux:

```bash
./gradlew :app:run
```

При первом запуске Gradle может скачать зависимости. Если зависимости ещё не лежат в локальном Gradle-кэше, для desktop-запуска из исходников нужен интернет.

---

## Portable desktop-запуск на другом Windows-ноутбуке без скачивания

Если нужно запустить desktop-версию StudyFlow на другом ноутбуке без установки Gradle, Kotlin, JDK и без скачивания зависимостей, нужно заранее собрать portable-версию на своём компьютере.

В корне проекта выполни:

```powershell
gradlew.bat :app:createDistributable
```

После сборки готовое приложение появится примерно здесь:

```text
app\build\compose\binaries\main\app\StudyFlow
```

Именно эту папку `StudyFlow` нужно скопировать целиком на флешку или в архив.

Важно: нельзя переносить только `.exe` файл. Рядом с ним лежат runtime-файлы и библиотеки, без которых приложение не запустится.

Чтобы создать zip-архив portable-версии, выполни:

```powershell
Compress-Archive `
  -Path .\app\build\compose\binaries\main\app\StudyFlow `
  -DestinationPath .\StudyFlow_portable_windows.zip `
  -Force
```

На другом Windows-ноутбуке:

```text
1. Распакуй StudyFlow_portable_windows.zip.
2. Открой папку StudyFlow.
3. Запусти StudyFlow.exe.
```

Если `StudyFlow.exe` не лежит сразу в корне папки, проверь подпапку:

```text
StudyFlow\bin\StudyFlow.exe
```

---

## Перенос данных

### Desktop-версия

Desktop-версия хранит данные здесь:

```text
%USERPROFILE%\.studyflow\studyflow.sqlite
```

Например:

```text
C:\Users\Honor\.studyflow\studyflow.sqlite
```

Чтобы перенести данные desktop-версии на другой ноутбук, скопируй папку:

```text
%USERPROFILE%\.studyflow
```

На новый компьютер её нужно положить в папку пользователя:

```text
C:\Users\ИМЯ_ПОЛЬЗОВАТЕЛЯ\.studyflow
```

### Web-версия

Web-версия хранит данные в localStorage браузера. Это значит, что данные привязаны к конкретному браузеру и адресу запуска.

Для переноса web-данных используй:

```text
Settings → Export JSON
Settings → Import JSON
```

---

## Сборка установщика под текущую ОС

Для создания desktop-пакета под текущую операционную систему:

На Windows:

```powershell
gradlew.bat :app:packageDistributionForCurrentOS
```

На macOS/Linux:

```bash
./gradlew :app:packageDistributionForCurrentOS
```

Готовые файлы появятся здесь:

```text
app\build\compose\binaries
```

Важно: Windows-сборку лучше собирать на Windows. Для macOS и Linux нужно собирать отдельные версии на соответствующих системах.

---

## Тесты

На Windows:

```powershell
gradlew.bat test
```

На macOS/Linux:

```bash
./gradlew test
```

---

## Import / Export

В приложении есть импорт и экспорт:

- Subjects CSV;
- Tasks CSV;
- Notes Markdown;
- Habits CSV;
- JSON backup для web-версии;
- SQLite raw backup для desktop-версии.

---

## Формат CSV для импорта предметов

```csv
id,name,description,color_hex,icon
1,Теория игр,"Код: Б1-ОПМ.Б.7 • Аттестация: Атт/Экз • Всего: 45 ч",#A78BFA,ТИ
```

---

## Формат CSV для импорта задач

```csv
id,subject,title,status,priority,deadline,estimated_minutes,spent_minutes,recurrence
1,Теория игр,Повторить билеты,Todo,High,2026-06-14,60,0,Daily
```

Основные поля:

- `subject`;
- `title`;
- `status`;
- `priority`;
- `deadline`;
- `estimated_minutes`;
- `recurrence`.

Неизвестные или пустые значения заменяются стандартными значениями.

---

## Формат Markdown для импорта заметок

```md
# Imported notes

## Первая заметка
Текст первой заметки.

## Вторая заметка
Текст второй заметки.
```

Если в файле нет заголовков `##`, весь Markdown импортируется как одна заметка.

---

## Предупреждение Native Access

На новых версиях JDK может появиться предупреждение про `System::load` от Skiko.

Это не ошибка и не проблема проекта. Compose Desktop использует Skiko для нативной отрисовки интерфейса.

В Gradle уже добавлен VM option:

```text
--enable-native-access=ALL-UNNAMED
```

Если приложение запускается через IDE и предупреждение всё равно появляется, добавь этот параметр вручную в VM options конфигурации запуска.

---

## Длинные названия предметов

В приложении используются выпадающие списки на всю ширину. Длинные названия дисциплин показываются полностью и не обрезаются до 12–16 символов.

Это исправлено для:

- фильтров в Tasks;
- выбора предмета в задаче;
- выбора предмета в заметке;
- выбора предмета в Focus Timer.

---

## Правила чистого репозитория

Не нужно коммитить и добавлять в финальный архив сгенерированные папки:

```text
.gradle/
build/
app/build/
.idea/
out/
*.iml
```

---

## Быстрая памятка

Запуск web-версии напрямую:

```text
StudyFlowWeb/index.html
```

Запуск web-версии по локальной ссылке:

```powershell
cd StudyFlowWeb
.\run-local.bat
```

Локальная ссылка:

```text
http://127.0.0.1:5173/
```

Запуск desktop-версии из исходников:

```powershell
gradlew.bat :app:run
```

Создание portable desktop-версии:

```powershell
gradlew.bat :app:createDistributable
```

Очистка старой локальной базы desktop-версии:

```text
Settings → Clear all data
```

Загрузка предметов:

```text
Settings → Load semester subjects
```

Загрузка расписания сессии:

```text
Settings → Load session schedule
```


## Web-версия с общей базой данных

В проекте есть web-интерфейс `StudyFlowWeb`. Чтобы данные web-версии и desktop-версии были одинаковыми, запускай сайт через локальный сервер, а не напрямую через `index.html`.

### Запуск web-версии с синхронизацией

Из корня проекта:

```powershell
gradlew.bat :app:runWeb
```

Или через файл:

```text
StudyFlowWeb\run-local.bat
```

После этого откроется локальная ссылка:

```text
http://127.0.0.1:5173/
```

В этом режиме web-версия работает с той же SQLite-базой, что и desktop-приложение:

```text
%USERPROFILE%\.studyflow\studyflow.sqlite
```

Что это значит:

- добавил задачу в desktop — она появится в web после `Reload from SQLite` или перезапуска страницы;
- добавил задачу в web — она сохраняется в SQLite и будет видна в desktop после перезапуска/перечитывания данных;
- предметы, сессия, заметки, привычки и фокус-сессии общие.

Если открыть `StudyFlowWeb/index.html` двойным кликом, синхронизация с desktop не включится. В таком режиме браузер хранит данные отдельно в `localStorage`.
