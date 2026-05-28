const STORE_KEY = "studyflow.web.v1";
let apiMode = false;
let apiSyncTimer = null;
let apiSyncInProgress = false;
const ROUTES = [
  ["dashboard", "⌂", "Dashboard"],
  ["subjects", "▣", "Subjects"],
  ["tasks", "✓", "Tasks"],
  ["board", "☷", "Board"],
  ["calendar", "◫", "Calendar"],
  ["session", "◷", "Session"],
  ["notes", "✎", "Notes"],
  ["timer", "⏱", "Timer"],
  ["stats", "▤", "Statistics"],
  ["habits", "◆", "Habits"],
  ["settings", "⚙", "Settings"]
];

const STATUS_LABELS = { todo: "To do", progress: "In progress", done: "Done" };
const PRIORITY_LABELS = { low: "Low", medium: "Medium", high: "High" };
const RECURRENCE_LABELS = { none: "No repeat", daily: "Daily", weekly: "Weekly", monthly: "Monthly" };

let state = loadState();
let route = localStorage.getItem("studyflow.web.route") || "dashboard";
let filters = { search: "", subject: "", status: "all", priority: "all" };
let calendarCursor = new Date();
let timer = {
  running: false,
  paused: false,
  startedAt: null,
  duration: 25 * 60,
  remaining: 25 * 60,
  subject: "",
  taskId: ""
};
let timerInterval = null;

function defaultState() {
  return {
    subjects: structuredClone(SEED_SUBJECTS),
    exams: structuredClone(SEED_EXAMS),
    tasks: [],
    notes: [],
    habits: [],
    focusSessions: [],
    settings: { timerMinutes: 25 }
  };
}

function loadState() {
  try {
    const raw = localStorage.getItem(STORE_KEY);
    if (!raw) return defaultState();
    const parsed = JSON.parse(raw);
    return {
      ...defaultState(),
      ...parsed,
      subjects: parsed.subjects || [],
      exams: parsed.exams || [],
      tasks: parsed.tasks || [],
      notes: parsed.notes || [],
      habits: parsed.habits || [],
      focusSessions: parsed.focusSessions || [],
      settings: { ...defaultState().settings, ...(parsed.settings || {}) }
    };
  } catch {
    return defaultState();
  }
}

function saveState() {
  localStorage.setItem(STORE_KEY, JSON.stringify(state));
  if (apiMode) scheduleApiSave();
}

async function connectBackend() {
  try {
    const res = await fetch("/api/snapshot", { cache: "no-store" });
    if (!res.ok) throw new Error("Backend unavailable");
    state = await res.json();
    apiMode = true;
    localStorage.setItem(STORE_KEY, JSON.stringify(state));
  } catch (e) {
    apiMode = false;
  }
}

function scheduleApiSave() {
  clearTimeout(apiSyncTimer);
  apiSyncTimer = setTimeout(saveToBackendNow, 120);
}

async function saveToBackendNow() {
  if (!apiMode || apiSyncInProgress) return;
  apiSyncInProgress = true;
  try {
    const res = await fetch("/api/snapshot", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(state)
    });
    if (!res.ok) throw new Error("SQLite sync failed");
    state = await res.json();
    localStorage.setItem(STORE_KEY, JSON.stringify(state));
  } catch (e) {
    console.error(e);
    toast("SQLite sync не удался. Данные пока сохранены в браузере.");
  } finally {
    apiSyncInProgress = false;
  }
}

async function reloadFromBackend() {
  if (!apiMode) return toast("SQLite sync доступен только при запуске через run-web/run-local");
  const res = await fetch("/api/snapshot", { cache: "no-store" });
  if (!res.ok) return toast("Не удалось прочитать SQLite");
  state = await res.json();
  localStorage.setItem(STORE_KEY, JSON.stringify(state));
  render();
  toast("Данные обновлены из SQLite desktop-версии");
}

function uid(prefix) {
  return `${prefix}_${Date.now()}_${Math.random().toString(16).slice(2)}`;
}

function todayISO() {
  return new Date().toISOString().slice(0, 10);
}

function localDateKey(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

function parseLocalDateTime(s) {
  return s ? new Date(s) : null;
}

function formatDate(s) {
  if (!s) return "Без даты";
  const d = typeof s === "string" ? new Date(s.length === 10 ? `${s}T00:00` : s) : s;
  return d.toLocaleDateString("ru-RU", { day: "2-digit", month: "long", year: "numeric" });
}

function formatTime(s) {
  const d = typeof s === "string" ? new Date(s) : s;
  return d.toLocaleTimeString("ru-RU", { hour: "2-digit", minute: "2-digit" });
}

function escapeHtml(v) {
  return String(v ?? "").replace(/[&<>'"]/g, ch => ({"&":"&amp;","<":"&lt;",">":"&gt;","'":"&#39;",'"':"&quot;"}[ch]));
}

function subjectByName(name) {
  return state.subjects.find(s => s.name === name) || null;
}

function subjectPill(name) {
  const s = subjectByName(name);
  if (!s) return `<span class="badge">${escapeHtml(name || "Без предмета")}</span>`;
  return `<span class="subject-pill"><span class="subject-dot" style="background:${s.color}"></span>${escapeHtml(s.name)}</span>`;
}

function priorityBadge(p) {
  return `<span class="badge ${p}">${PRIORITY_LABELS[p] || p}</span>`;
}

function statusBadge(st) {
  return `<span class="badge">${STATUS_LABELS[st] || st}</span>`;
}

function toast(msg) {
  const el = document.getElementById("toast");
  el.textContent = msg;
  el.classList.remove("hidden");
  clearTimeout(el._timer);
  el._timer = setTimeout(() => el.classList.add("hidden"), 2600);
}

function setRoute(next) {
  route = next;
  localStorage.setItem("studyflow.web.route", next);
  render();
}

function renderShell(content, title, subtitle, actions = "") {
  document.getElementById("app").innerHTML = `
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <div class="logo">SF</div>
          <div><h1>StudyFlow</h1><p>Web/local версия</p></div>
        </div>
        <nav class="nav">
          ${ROUTES.map(([id, icon, label]) => `<button class="${route === id ? "active" : ""}" onclick="setRoute('${id}')"><b>${icon}</b><span>${label}</span></button>`).join("")}
        </nav>
        <div class="sidebar-note">${apiMode ? "SQLite sync: web и desktop используют одну базу." : "Public/direct mode: работает по ссылке, данные хранятся в localStorage браузера."}</div>
      </aside>
      <main class="main">
        <div class="topbar">
          <div><h2>${title}</h2><p>${subtitle || ""}</p></div>
          <div class="actions">${actions}</div>
        </div>
        ${content}
      </main>
    </div>`;
}

function render() {
  if (route === "dashboard") return renderDashboard();
  if (route === "subjects") return renderSubjects();
  if (route === "tasks") return renderTasks();
  if (route === "board") return renderBoard();
  if (route === "calendar") return renderCalendar();
  if (route === "session") return renderSession();
  if (route === "notes") return renderNotes();
  if (route === "timer") return renderTimer();
  if (route === "stats") return renderStats();
  if (route === "habits") return renderHabits();
  if (route === "settings") return renderSettings();
  return renderDashboard();
}

function getUpcomingExams(limit = 5) {
  const now = new Date();
  return [...state.exams].filter(e => new Date(e.end) >= now).sort((a,b) => new Date(a.start) - new Date(b.start)).slice(0, limit);
}

function getUpcomingTasks(limit = 6) {
  return [...state.tasks]
    .filter(t => t.status !== "done")
    .sort((a,b) => (a.deadline || "9999-99-99").localeCompare(b.deadline || "9999-99-99"))
    .slice(0, limit);
}

function renderDashboard() {
  const done = state.tasks.filter(t => t.status === "done").length;
  const open = state.tasks.length - done;
  const todayTasks = state.tasks.filter(t => t.deadline === todayISO() && t.status !== "done").length;
  const focusMin = state.focusSessions.reduce((sum, s) => sum + Number(s.minutes || 0), 0);
  const upcoming = getUpcomingExams(5);
  const taskList = getUpcomingTasks(6);
  renderShell(`
    <div class="grid cols-4">
      ${statCard(state.subjects.length, "предметов")}
      ${statCard(open, "открытых задач")}
      ${statCard(todayTasks, "задач сегодня")}
      ${statCard(Math.round(focusMin / 60 * 10) / 10, "часов фокуса")}
    </div>
    <div class="grid cols-2" style="margin-top:16px">
      <section class="card">
        <h3>Ближайшие события сессии</h3>
        <div class="list">${upcoming.length ? upcoming.map(examRow).join("") : empty("Событий нет")}</div>
      </section>
      <section class="card">
        <h3>Ближайшие задачи</h3>
        <div class="list">${taskList.length ? taskList.map(taskRow).join("") : empty("Задач пока нет")}</div>
      </section>
    </div>
  `, "Dashboard", "Краткая сводка по семестру, задачам и сессии", `<button class="btn primary" onclick="openTaskModal()">+ Задача</button>`);
}

function statCard(num, label) {
  return `<section class="card stat"><div class="label">${label}</div><div class="num">${escapeHtml(num)}</div><p>Локальные данные браузера</p></section>`;
}

function empty(text) { return `<div class="empty">${escapeHtml(text)}</div>`; }

function examDetailsText(e) {
  return [
    e.subject,
    `Дата: ${formatDate(e.start)}`,
    `Время: ${formatTime(e.start)}–${formatTime(e.end)}`,
    `Аудитория: ${e.location || "не указана"}`,
    e.teachers ? `Преподаватель: ${e.teachers}` : ""
  ].filter(Boolean).join("\n");
}

function examTooltipHtml(e) {
  return `<div class="event-tooltip">
    <b>${escapeHtml(e.subject)}</b>
    <span>Дата: ${formatDate(e.start)}</span>
    <span>Время: ${formatTime(e.start)}–${formatTime(e.end)}</span>
    <span class="room">Аудитория: ${escapeHtml(e.location || "не указана")}</span>
    ${e.teachers ? `<span>Преподаватель: ${escapeHtml(e.teachers)}</span>` : ""}
  </div>`;
}

function examRow(e) {
  return `<div class="row session-row" title="${escapeHtml(examDetailsText(e))}">
    <div>
      <div class="row-title">${escapeHtml(e.subject)}</div>
      <div class="row-meta">${formatDate(e.start)}, ${formatTime(e.start)}–${formatTime(e.end)}${e.teachers ? " • " + escapeHtml(e.teachers) : ""}${e.location ? " • ауд. " + escapeHtml(e.location) : ""}</div>
    </div>
  </div>`;
}

function taskRow(t) {
  return `<div class="row">
    <div>
      <div class="row-title">${escapeHtml(t.title)}</div>
      <div class="row-meta">${subjectPill(t.subject)} ${t.deadline ? " • дедлайн: " + formatDate(t.deadline) : ""}</div>
      <div class="badges">${statusBadge(t.status)} ${priorityBadge(t.priority)} ${t.recurrence !== "none" ? `<span class="badge">${RECURRENCE_LABELS[t.recurrence]}</span>` : ""}</div>
    </div>
    <div class="actions">
      <button class="btn small" onclick="openTaskModal('${t.id}')">Edit</button>
      <button class="btn small ok" onclick="completeTask('${t.id}')">Done</button>
    </div>
  </div>`;
}

function renderSubjects() {
  const totalHours = state.subjects.reduce((sum, s) => sum + (Number((s.description || "").match(/Всего:\s*(\d+)/)?.[1]) || 0), 0);
  renderShell(`
    <div class="grid cols-3">
      ${statCard(state.subjects.length, "предметов")}
      ${statCard(totalHours, "часов")}
      ${statCard(state.exams.length, "события сессии")}
    </div>
    <div class="list" style="margin-top:16px">${state.subjects.map(s => `<div class="row">
      <div>
        <div class="row-title">${subjectPill(s.name)}</div>
        <div class="row-meta">${escapeHtml(s.description)}</div>
      </div>
      <div class="badge">${escapeHtml(s.icon)}</div>
    </div>`).join("")}</div>
  `, "Subjects", "Предметы семестра с кодами и часами", `<button class="btn" onclick="loadSeedSubjects()">Load semester subjects</button>`);
}

function subjectOptions(selected = "", allowAll = false) {
  return `${allowAll ? `<option value="">Все предметы</option>` : `<option value="">Без предмета</option>`}${state.subjects.map(s => `<option value="${escapeHtml(s.name)}" ${s.name === selected ? "selected" : ""}>${escapeHtml(s.name)}</option>`).join("")}`;
}

function filteredTasks() {
  const q = filters.search.trim().toLowerCase();
  return state.tasks.filter(t => {
    if (q && !`${t.title} ${t.description || ""} ${t.subject || ""}`.toLowerCase().includes(q)) return false;
    if (filters.subject && t.subject !== filters.subject) return false;
    if (filters.status !== "all" && t.status !== filters.status) return false;
    if (filters.priority !== "all" && t.priority !== filters.priority) return false;
    return true;
  });
}

function renderTasks() {
  const tasks = filteredTasks();
  renderShell(`
    <div class="toolbar">
      <input class="input" style="min-width:260px" placeholder="Поиск задач" value="${escapeHtml(filters.search)}" oninput="filters.search=this.value; renderTasks()" />
      <select class="select" onchange="filters.subject=this.value; renderTasks()">${subjectOptions(filters.subject, true)}</select>
      <select class="select" onchange="filters.status=this.value; renderTasks()">
        <option value="all" ${filters.status === "all" ? "selected" : ""}>Все статусы</option>
        ${Object.entries(STATUS_LABELS).map(([k,v]) => `<option value="${k}" ${filters.status === k ? "selected" : ""}>${v}</option>`).join("")}
      </select>
      <select class="select" onchange="filters.priority=this.value; renderTasks()">
        <option value="all" ${filters.priority === "all" ? "selected" : ""}>Все приоритеты</option>
        ${Object.entries(PRIORITY_LABELS).map(([k,v]) => `<option value="${k}" ${filters.priority === k ? "selected" : ""}>${v}</option>`).join("")}
      </select>
    </div>
    <div class="list">${tasks.length ? tasks.map(taskRow).join("") : empty("Задач по выбранным фильтрам нет")}</div>
  `, "Tasks", "Задачи, дедлайны, приоритеты и повторение", `<button class="btn primary" onclick="openTaskModal()">+ Новая задача</button>`);
}

function openTaskModal(id = "") {
  const t = id ? state.tasks.find(x => x.id === id) : null;
  showModal(`
    <h3>${t ? "Редактировать задачу" : "Новая задача"}</h3>
    <div class="form-grid">
      <label class="full">Название<br><input id="taskTitle" class="input wide" value="${escapeHtml(t?.title || "")}" /></label>
      <label class="full">Предмет<br><select id="taskSubject" class="select wide">${subjectOptions(t?.subject || "")}</select></label>
      <label>Статус<br><select id="taskStatus" class="select wide">${Object.entries(STATUS_LABELS).map(([k,v]) => `<option value="${k}" ${t?.status === k ? "selected" : ""}>${v}</option>`).join("")}</select></label>
      <label>Приоритет<br><select id="taskPriority" class="select wide">${Object.entries(PRIORITY_LABELS).map(([k,v]) => `<option value="${k}" ${(t?.priority || "medium") === k ? "selected" : ""}>${v}</option>`).join("")}</select></label>
      <label>Дедлайн<br><input id="taskDeadline" type="date" class="input wide" value="${escapeHtml(t?.deadline || "")}" /></label>
      <label>Повтор<br><select id="taskRecurrence" class="select wide">${Object.entries(RECURRENCE_LABELS).map(([k,v]) => `<option value="${k}" ${(t?.recurrence || "none") === k ? "selected" : ""}>${v}</option>`).join("")}</select></label>
      <label>Оценка, минут<br><input id="taskEstimate" type="number" min="0" class="input wide" value="${escapeHtml(t?.estimatedMinutes || 60)}" /></label>
      <label class="full">Описание<br><textarea id="taskDescription" class="wide">${escapeHtml(t?.description || "")}</textarea></label>
    </div>
    <div class="modal-actions">
      ${t ? `<button class="btn danger" onclick="deleteTask('${t.id}')">Удалить</button>` : ""}
      <button class="btn" onclick="closeModal()">Отмена</button>
      <button class="btn primary" onclick="saveTask('${id}')">Сохранить</button>
    </div>`);
}

function saveTask(id = "") {
  const payload = {
    title: val("taskTitle").trim(),
    subject: val("taskSubject"),
    status: val("taskStatus") || "todo",
    priority: val("taskPriority") || "medium",
    deadline: val("taskDeadline"),
    recurrence: val("taskRecurrence") || "none",
    estimatedMinutes: Number(val("taskEstimate")) || 0,
    description: val("taskDescription")
  };
  if (!payload.title) return toast("Введите название задачи");
  if (id) {
    state.tasks = state.tasks.map(t => t.id === id ? { ...t, ...payload } : t);
  } else {
    state.tasks.push({ id: uid("task"), ...payload, spentMinutes: 0, createdAt: new Date().toISOString(), completedAt: null });
  }
  saveState(); closeModal(); render(); toast("Задача сохранена");
}

function completeTask(id) {
  const t = state.tasks.find(x => x.id === id);
  if (!t) return;
  t.status = "done";
  t.completedAt = new Date().toISOString();
  if (t.recurrence && t.recurrence !== "none" && t.deadline) {
    const next = addRecurrence(t.deadline, t.recurrence);
    state.tasks.push({ ...t, id: uid("task"), status: "todo", deadline: next, completedAt: null, createdAt: new Date().toISOString() });
  }
  saveState(); render(); toast("Задача завершена");
}

function deleteTask(id) {
  if (!confirm("Удалить задачу?")) return;
  state.tasks = state.tasks.filter(t => t.id !== id);
  saveState(); closeModal(); render();
}

function addRecurrence(date, recurrence) {
  const d = new Date(`${date}T00:00`);
  if (recurrence === "daily") d.setDate(d.getDate() + 1);
  if (recurrence === "weekly") d.setDate(d.getDate() + 7);
  if (recurrence === "monthly") d.setMonth(d.getMonth() + 1);
  return localDateKey(d);
}

function renderBoard() {
  const columns = ["todo", "progress", "done"];
  renderShell(`
    <div class="kanban">
      ${columns.map(st => `<section class="column" data-status="${st}" ondragover="dragOver(event)" ondragleave="dragLeave(event)" ondrop="dropTask(event, '${st}')">
        <h3>${STATUS_LABELS[st]} <span class="badge">${state.tasks.filter(t => t.status === st).length}</span></h3>
        ${state.tasks.filter(t => t.status === st).map(t => `<article class="task-card" draggable="true" ondragstart="dragTask(event, '${t.id}')">
          <b>${escapeHtml(t.title)}</b>
          <div class="row-meta">${escapeHtml(t.subject || "Без предмета")}</div>
          <div class="badges">${priorityBadge(t.priority)} ${t.deadline ? `<span class="badge">${formatDate(t.deadline)}</span>` : ""}</div>
        </article>`).join("") || empty("Пусто")}
      </section>`).join("")}
    </div>
  `, "Board", "Kanban-доска. Перетаскивай карточки между колонками", `<button class="btn primary" onclick="openTaskModal()">+ Задача</button>`);
}

function dragTask(ev, id) { ev.dataTransfer.setData("text/plain", id); }
function dragOver(ev) { ev.preventDefault(); ev.currentTarget.classList.add("drag-over"); }
function dragLeave(ev) { ev.currentTarget.classList.remove("drag-over"); }
function dropTask(ev, status) {
  ev.preventDefault(); ev.currentTarget.classList.remove("drag-over");
  const id = ev.dataTransfer.getData("text/plain");
  const task = state.tasks.find(t => t.id === id);
  if (!task) return;
  task.status = status;
  if (status === "done") task.completedAt = new Date().toISOString();
  saveState(); renderBoard();
}

function renderCalendar() {
  const year = calendarCursor.getFullYear();
  const month = calendarCursor.getMonth();
  const first = new Date(year, month, 1);
  const start = new Date(first);
  start.setDate(1 - ((first.getDay() + 6) % 7));
  const days = [];
  for (let i = 0; i < 42; i++) { const d = new Date(start); d.setDate(start.getDate() + i); days.push(d); }
  const heads = ["Пн","Вт","Ср","Чт","Пт","Сб","Вс"];
  renderShell(`
    <div class="month-controls">
      <button class="btn" onclick="changeMonth(-1)">←</button>
      <b>${calendarCursor.toLocaleDateString("ru-RU", { month: "long", year: "numeric" })}</b>
      <button class="btn" onclick="changeMonth(1)">→</button>
      <button class="btn" onclick="calendarCursor=new Date(); renderCalendar()">Сегодня</button>
    </div>
    <div class="calendar">
      ${heads.map(h => `<div class="day-head">${h}</div>`).join("")}
      ${days.map(d => renderDay(d, month)).join("")}
    </div>
  `, "Calendar", "Задачи и события сессии по датам");
}

function renderDay(d, currentMonth) {
  const key = localDateKey(d);
  const exams = state.exams.filter(e => e.start.slice(0,10) === key);
  const tasks = state.tasks.filter(t => t.deadline === key);
  return `<div class="day ${d.getMonth() !== currentMonth ? "out" : ""} ${key === todayISO() ? "today" : ""}">
    <div class="day-num">${d.getDate()}</div>
    ${exams.map(e => `<div class="event-mini has-tooltip" title="${escapeHtml(examDetailsText(e))}">${formatTime(e.start)} ${escapeHtml(e.subject)}${examTooltipHtml(e)}</div>`).join("")}
    ${tasks.map(t => `<div class="event-mini task" title="${escapeHtml(t.title)}">✓ ${escapeHtml(t.title)}</div>`).join("")}
  </div>`;
}

function changeMonth(delta) { calendarCursor.setMonth(calendarCursor.getMonth() + delta); renderCalendar(); }

function renderSession() {
  const grouped = groupBy([...state.exams].sort((a,b) => new Date(a.start) - new Date(b.start)), e => e.start.slice(0,10));
  renderShell(`
    <div class="list">
      ${Object.entries(grouped).map(([date, items]) => `<section class="card flat"><h3>${formatDate(date)}</h3><div class="list">${items.map(examRow).join("")}</div></section>`).join("") || empty("Расписание не загружено")}
    </div>
  `, "Session", "Полное расписание сессии: дата, время, предмет, преподаватель и аудитория", `<button class="btn" onclick="loadSeedExams()">Load session schedule</button>`);
}

function groupBy(arr, fn) { return arr.reduce((acc, x) => ((acc[fn(x)] ||= []).push(x), acc), {}); }

function renderNotes() {
  renderShell(`
    <div class="list">${state.notes.length ? state.notes.map(n => `<div class="row">
      <div><div class="row-title">${escapeHtml(n.title)}</div><div class="row-meta">${subjectPill(n.subject)} • ${formatDate(n.createdAt)}</div><p>${escapeHtml(n.body).slice(0, 240)}</p></div>
      <div class="actions"><button class="btn small" onclick="openNoteModal('${n.id}')">Edit</button></div>
    </div>`).join("") : empty("Заметок пока нет")}</div>
  `, "Notes", "Заметки по предметам", `<button class="btn primary" onclick="openNoteModal()">+ Заметка</button>`);
}

function openNoteModal(id = "") {
  const n = id ? state.notes.find(x => x.id === id) : null;
  showModal(`<h3>${n ? "Редактировать заметку" : "Новая заметка"}</h3>
    <div class="form-grid">
      <label class="full">Название<br><input id="noteTitle" class="input wide" value="${escapeHtml(n?.title || "")}" /></label>
      <label class="full">Предмет<br><select id="noteSubject" class="select wide">${subjectOptions(n?.subject || "")}</select></label>
      <label class="full">Текст<br><textarea id="noteBody" class="wide">${escapeHtml(n?.body || "")}</textarea></label>
    </div>
    <div class="modal-actions">
      ${n ? `<button class="btn danger" onclick="deleteNote('${n.id}')">Удалить</button>` : ""}
      <button class="btn" onclick="closeModal()">Отмена</button><button class="btn primary" onclick="saveNote('${id}')">Сохранить</button>
    </div>`);
}
function saveNote(id="") {
  const payload = { title: val("noteTitle").trim(), subject: val("noteSubject"), body: val("noteBody") };
  if (!payload.title) return toast("Введите название заметки");
  if (id) state.notes = state.notes.map(n => n.id === id ? { ...n, ...payload } : n);
  else state.notes.push({ id: uid("note"), ...payload, createdAt: new Date().toISOString() });
  saveState(); closeModal(); render();
}
function deleteNote(id) { if (confirm("Удалить заметку?")) { state.notes = state.notes.filter(n => n.id !== id); saveState(); closeModal(); render(); } }

function renderTimer() {
  timer.subject ||= state.subjects[0]?.name || "";
  const pct = Math.max(0, Math.min(100, 100 * (1 - timer.remaining / timer.duration)));
  renderShell(`
    <section class="card timer-box">
      <div class="form-grid">
        <label class="full">Предмет<br><select class="select wide" onchange="timer.subject=this.value">${subjectOptions(timer.subject)}</select></label>
        <label>Минуты<br><input class="input wide" type="number" min="1" value="${Math.round(timer.duration/60)}" onchange="setTimerMinutes(this.value)" ${timer.running ? "disabled" : ""}></label>
        <label>Задача<br><select class="select wide" onchange="timer.taskId=this.value"><option value="">Без задачи</option>${state.tasks.filter(t => t.status !== "done").map(t => `<option value="${t.id}" ${timer.taskId===t.id?"selected":""}>${escapeHtml(t.title)}</option>`).join("")}</select></label>
      </div>
      <div class="timer-display">${formatTimer(timer.remaining)}</div>
      <div class="progress"><div style="width:${pct}%"></div></div>
      <div class="actions" style="justify-content:center;margin-top:18px">
        <button class="btn primary" onclick="startTimer()">${timer.running && !timer.paused ? "Restart" : "Start"}</button>
        <button class="btn" onclick="pauseTimer()">${timer.paused ? "Continue" : "Pause"}</button>
        <button class="btn danger" onclick="resetTimer()">Reset</button>
      </div>
    </section>
  `, "Focus Timer", "Фокус-таймер с browser notification и beep fallback");
}

function setTimerMinutes(v) { timer.duration = Math.max(1, Number(v)||25) * 60; timer.remaining = timer.duration; renderTimer(); }
function formatTimer(sec) { const m = Math.floor(sec/60); const s = sec%60; return `${String(m).padStart(2,"0")}:${String(s).padStart(2,"0")}`; }
function startTimer() {
  timer.duration = timer.duration || 25*60;
  timer.remaining = timer.duration;
  timer.running = true; timer.paused = false; timer.startedAt = Date.now();
  requestNotifyPermission();
  tickTimer(); clearInterval(timerInterval); timerInterval = setInterval(tickTimer, 300);
}
function pauseTimer() {
  if (!timer.running) return;
  timer.paused = !timer.paused;
  if (!timer.paused) timer.startedAt = Date.now() - (timer.duration - timer.remaining) * 1000;
  renderTimer();
}
function resetTimer() { timer.running=false; timer.paused=false; timer.remaining=timer.duration; clearInterval(timerInterval); renderTimer(); }
function tickTimer() {
  if (!timer.running || timer.paused) return;
  timer.remaining = Math.max(0, timer.duration - Math.floor((Date.now() - timer.startedAt)/1000));
  if (route === "timer") renderTimer();
  if (timer.remaining <= 0) finishTimer();
}
function finishTimer() {
  clearInterval(timerInterval);
  timer.running=false; timer.paused=false;
  const minutes = Math.round(timer.duration/60);
  state.focusSessions.push({ id: uid("focus"), subject: timer.subject, taskId: timer.taskId, minutes, date: new Date().toISOString() });
  if (timer.taskId) {
    const t = state.tasks.find(x => x.id === timer.taskId);
    if (t) t.spentMinutes = Number(t.spentMinutes || 0) + minutes;
  }
  saveState(); notify("Фокус-сессия завершена", `${minutes} мин. • ${timer.subject || "StudyFlow"}`); timer.remaining=timer.duration; render();
}
function requestNotifyPermission() { if ("Notification" in window && Notification.permission === "default") Notification.requestPermission(); }
function notify(title, body) {
  if ("Notification" in window && Notification.permission === "granted") new Notification(title, { body });
  else beep();
  toast(title);
}
function beep() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator(); const gain = ctx.createGain();
    osc.connect(gain); gain.connect(ctx.destination); osc.frequency.value = 880; gain.gain.value = 0.08;
    osc.start(); setTimeout(() => { osc.stop(); ctx.close(); }, 350);
  } catch {}
}

function renderStats() {
  const bySubject = groupBy(state.tasks, t => t.subject || "Без предмета");
  const focusBySubject = groupBy(state.focusSessions, f => f.subject || "Без предмета");
  renderShell(`
    <div class="grid cols-3">
      ${statCard(state.tasks.length, "всего задач")}
      ${statCard(state.tasks.filter(t=>t.status==="done").length, "завершено")}
      ${statCard(state.focusSessions.reduce((s,f)=>s+Number(f.minutes||0),0), "минут фокуса")}
    </div>
    <div class="grid cols-2" style="margin-top:16px">
      <section class="card"><h3>Задачи по предметам</h3><div class="list">${Object.entries(bySubject).map(([k,v]) => `<div class="row"><b>${escapeHtml(k)}</b><span class="badge">${v.length}</span></div>`).join("") || empty("Нет данных")}</div></section>
      <section class="card"><h3>Фокус по предметам</h3><div class="list">${Object.entries(focusBySubject).map(([k,v]) => `<div class="row"><b>${escapeHtml(k)}</b><span class="badge">${v.reduce((s,f)=>s+Number(f.minutes||0),0)} мин</span></div>`).join("") || empty("Нет данных")}</div></section>
    </div>
  `, "Statistics", "Базовая аналитика по задачам и фокус-сессиям");
}

function renderHabits() {
  renderShell(`
    <div class="habit-grid">${state.habits.length ? state.habits.map(habitCard).join("") : empty("Привычек пока нет")}</div>
  `, "Habits", "Ежедневные привычки и streak", `<button class="btn primary" onclick="openHabitModal()">+ Привычка</button>`);
}
function habitCard(h) {
  const done = (h.history || []).includes(todayISO());
  return `<article class="habit-card ${done ? "done" : ""}"><h3>${escapeHtml(h.title)}</h3><p>${escapeHtml(h.subject || "Без предмета")}</p><div class="badges"><span class="badge">Streak: ${habitStreak(h)}</span><span class="badge">${(h.history || []).length} days</span></div><div class="actions" style="margin-top:14px"><button class="btn ${done?"":"ok"}" onclick="toggleHabit('${h.id}')">${done ? "Снять отметку" : "Done today"}</button><button class="btn danger" onclick="deleteHabit('${h.id}')">Удалить</button></div></article>`;
}
function habitStreak(h) {
  const set = new Set(h.history || []);
  let d = new Date(); let streak = 0;
  while (set.has(localDateKey(d))) { streak++; d.setDate(d.getDate()-1); }
  return streak;
}
function openHabitModal() {
  showModal(`<h3>Новая привычка</h3><div class="form-grid"><label class="full">Название<br><input id="habitTitle" class="input wide" /></label><label class="full">Предмет<br><select id="habitSubject" class="select wide">${subjectOptions("")}</select></label></div><div class="modal-actions"><button class="btn" onclick="closeModal()">Отмена</button><button class="btn primary" onclick="saveHabit()">Сохранить</button></div>`);
}
function saveHabit() { const title=val("habitTitle").trim(); if(!title) return toast("Введите название"); state.habits.push({id:uid("habit"), title, subject:val("habitSubject"), history:[]}); saveState(); closeModal(); render(); }
function toggleHabit(id) { const h=state.habits.find(x=>x.id===id); if(!h)return; h.history ||= []; const t=todayISO(); h.history = h.history.includes(t) ? h.history.filter(x=>x!==t) : [...h.history,t]; saveState(); renderHabits(); }
function deleteHabit(id) { if(confirm("Удалить привычку?")){ state.habits=state.habits.filter(h=>h.id!==id); saveState(); render(); } }

function renderSettings() {
  renderShell(`
    <div class="grid cols-2">
      <section class="card"><h3>Стартовые данные</h3><p>Можно заново загрузить предметы и полное расписание сессии.</p><div class="actions" style="justify-content:flex-start"><button class="btn" onclick="loadSeedSubjects()">Load semester subjects</button><button class="btn" onclick="loadSeedExams()">Load session schedule</button></div></section>
      <section class="card"><h3>Backup</h3><p>JSON backup переносит данные между браузерами и ноутбуками.</p><div class="actions" style="justify-content:flex-start"><button class="btn" onclick="exportJson()">Export JSON</button><button class="btn" onclick="pickFile('json')">Import JSON</button></div></section>
      <section class="card"><h3>CSV / Markdown</h3><div class="actions" style="justify-content:flex-start"><button class="btn" onclick="exportTasksCsv()">Export tasks CSV</button><button class="btn" onclick="pickFile('tasks')">Import tasks CSV</button><button class="btn" onclick="exportSubjectsCsv()">Export subjects CSV</button><button class="btn" onclick="pickFile('subjects')">Import subjects CSV</button><button class="btn" onclick="exportNotesMd()">Export notes MD</button><button class="btn" onclick="pickFile('notes')">Import notes MD</button></div></section>
      <section class="card"><h3>Синхронизация с Desktop</h3><p>${apiMode ? "Включена: сайт читает и пишет ту же SQLite-базу, что и desktop-версия." : "Выключена: открой сайт через run-web.bat / gradlew :app:runWeb."}</p><div class="actions" style="justify-content:flex-start"><button class="btn" onclick="reloadFromBackend()">Reload from SQLite</button><button class="btn" onclick="saveToBackendNow()">Save to SQLite</button></div></section>
      <section class="card"><h3>Очистка</h3><p>Полностью удаляет данные web-версии из localStorage.</p><button class="btn danger" onclick="clearAll()">Clear all web data</button></section>
    </div>
    <input id="filePicker" class="file-input" type="file" />
  `, "Settings", "Импорт, экспорт и управление локальными данными");
}

function loadSeedSubjects() { state.subjects = structuredClone(SEED_SUBJECTS); saveState(); render(); toast("Предметы загружены"); }
function loadSeedExams() { state.exams = structuredClone(SEED_EXAMS); saveState(); render(); toast("Расписание сессии загружено"); }
function clearAll() { if(confirm("Удалить все web-данные?")) { state = { ...defaultState(), tasks: [], notes: [], habits: [], focusSessions: [] }; saveState(); render(); toast("Очищено"); } }

function exportJson() { download("studyflow_web_backup.json", JSON.stringify(state, null, 2), "application/json"); }
function exportTasksCsv() { download("studyflow_tasks.csv", toCsv(["id","subject","title","status","priority","deadline","estimatedMinutes","spentMinutes","recurrence"], state.tasks), "text/csv"); }
function exportSubjectsCsv() { download("studyflow_subjects.csv", toCsv(["id","name","description","color","icon"], state.subjects), "text/csv"); }
function exportNotesMd() { download("studyflow_notes.md", state.notes.map(n => `## ${n.title}\n\n${n.body || ""}\n`).join("\n"), "text/markdown"); }
function exportHabitsCsv() { download("studyflow_habits.csv", toCsv(["id","title","subject","history"], state.habits), "text/csv"); }
function toCsv(headers, rows) { return headers.join(",") + "\n" + rows.map(r => headers.map(h => csvCell(r[h])).join(",")).join("\n"); }
function csvCell(v) { const s = Array.isArray(v) ? v.join("|") : String(v ?? ""); return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s; }
function download(name, content, type) { const blob = new Blob([content], {type}); const a = document.createElement("a"); a.href = URL.createObjectURL(blob); a.download = name; a.click(); setTimeout(()=>URL.revokeObjectURL(a.href), 1000); }

function pickFile(kind) {
  const input = document.getElementById("filePicker");
  input.value = ""; input.onchange = () => importFile(kind, input.files[0]); input.click();
}
function importFile(kind, file) {
  if (!file) return;
  const reader = new FileReader();
  reader.onload = () => {
    try {
      const text = String(reader.result || "");
      if (kind === "json") state = { ...defaultState(), ...JSON.parse(text) };
      if (kind === "subjects") state.subjects = parseCsv(text).map((r,i)=>({id:Number(r.id)||i+1,name:r.name||r.subject||"Без названия",description:r.description||"",color:r.color_hex||r.color||"#60A5FA",icon:r.icon||"SF"}));
      if (kind === "tasks") state.tasks.push(...parseCsv(text).map(r => ({ id: uid("task"), subject:r.subject||"", title:r.title||"Imported task", status:normalizeStatus(r.status), priority:normalizePriority(r.priority), deadline:r.deadline||"", estimatedMinutes:Number(r.estimated_minutes||r.estimatedMinutes)||0, spentMinutes:Number(r.spent_minutes||r.spentMinutes)||0, recurrence:normalizeRecurrence(r.recurrence), description:r.description||"", createdAt:new Date().toISOString(), completedAt:null })));
      if (kind === "notes") state.notes.push(...parseMarkdownNotes(text));
      saveState(); render(); toast("Импорт выполнен");
    } catch (e) { console.error(e); toast("Импорт не удался"); }
  };
  reader.readAsText(file, "utf-8");
}
function parseCsv(text) {
  const rows = []; let row=[], cell="", q=false;
  for (let i=0;i<text.length;i++) { const c=text[i], n=text[i+1];
    if (c==='"' && q && n==='"') { cell+='"'; i++; }
    else if (c==='"') q=!q;
    else if (c===',' && !q) { row.push(cell); cell=""; }
    else if ((c==='\n'||c==='\r') && !q) { if (cell || row.length) { row.push(cell); rows.push(row); row=[]; cell=""; } if(c==='\r'&&n==='\n') i++; }
    else cell+=c;
  }
  if (cell || row.length) { row.push(cell); rows.push(row); }
  const headers = rows.shift()?.map(h=>h.trim()) || [];
  return rows.filter(r=>r.length).map(r => Object.fromEntries(headers.map((h,i)=>[h, r[i] ?? ""])));
}
function parseMarkdownNotes(text) {
  const chunks = text.split(/^##\s+/m).filter(Boolean);
  if (chunks.length <= 1 && !text.match(/^##\s+/m)) return [{id:uid("note"), title:"Imported note", subject:"", body:text, createdAt:new Date().toISOString()}];
  return chunks.map(chunk => { const [title, ...body] = chunk.split(/\n/); return {id:uid("note"), title:title.trim() || "Imported note", subject:"", body:body.join("\n").trim(), createdAt:new Date().toISOString()}; });
}
function normalizeStatus(v) { v=String(v||"").toLowerCase(); if(v.includes("progress")) return "progress"; if(v.includes("done")) return "done"; return "todo"; }
function normalizePriority(v) { v=String(v||"").toLowerCase(); if(v.includes("high")) return "high"; if(v.includes("low")) return "low"; return "medium"; }
function normalizeRecurrence(v) { v=String(v||"").toLowerCase(); if(v.includes("daily")) return "daily"; if(v.includes("weekly")) return "weekly"; if(v.includes("monthly")) return "monthly"; return "none"; }

function showModal(html) {
  const bg = document.createElement("div");
  bg.id = "modalBg"; bg.className = "modal-bg"; bg.innerHTML = `<div class="modal">${html}</div>`;
  bg.addEventListener("mousedown", e => { if (e.target === bg) closeModal(); });
  document.body.appendChild(bg);
}
function closeModal() { document.getElementById("modalBg")?.remove(); }
function val(id) { return document.getElementById(id)?.value || ""; }

window.addEventListener("DOMContentLoaded", async () => { await connectBackend(); render(); });
window.addEventListener("keydown", e => { if (e.key === "Escape") closeModal(); });
