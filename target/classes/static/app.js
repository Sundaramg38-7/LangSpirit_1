// ================= Helpers =================
let session = JSON.parse(sessionStorage.getItem('session') || 'null');
let authMode = 'login';
const $ = id => document.getElementById(id);

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
function fmt(dt) { return dt ? new Date(dt).toLocaleString() : ''; }

async function api(method, url, body) {
  const res = await fetch('/api' + url, {
    method,
    headers: { 'Content-Type': 'application/json', 'X-Auth-Token': session ? session.token : '' },
    body: body ? JSON.stringify(body) : undefined
  });
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch (e) { /* not json */ }
  if (!res.ok) {
    if (res.status === 401 && session) { logout(true); }
    throw new Error((data && data.message) || 'Request failed (' + res.status + ')');
  }
  return data;
}

function notify(text, isError) {
  const n = $('notice');
  n.textContent = text;
  n.className = 'msg ' + (isError ? 'error' : 'ok');
  if (text) setTimeout(() => { n.textContent = ''; }, 4000);
}
async function safe(fn) {
  try { await fn(); } catch (e) { notify(e.message, true); }
}

// ================= Auth =================
async function loadBrand() {
  try {
    const s = await api('GET', '/settings/public');
    const name = s.platformName || 'Language Platform';
    $('brand').textContent = name;
    $('appBrand').textContent = name;
    document.title = name;
  } catch (e) { /* ignore */ }
}

function switchAuth(mode) {
  authMode = mode;
  $('tabLogin').classList.toggle('active', mode === 'login');
  $('tabRegister').classList.toggle('active', mode === 'register');
  $('registerOnly').classList.toggle('hidden', mode === 'login');
  $('authMsg').textContent = '';
}

async function submitAuth() {
  const msg = $('authMsg');
  msg.className = 'msg error';
  try {
    const email = $('authEmail').value.trim();
    const password = $('authPassword').value;
    if (!email || !password) throw new Error('Please enter email and password');
    let data;
    if (authMode === 'login') {
      data = await api('POST', '/auth/login', { email, password });
    } else {
      data = await api('POST', '/auth/register', { name: $('regName').value.trim(), email, password });
    }
    session = data;
    sessionStorage.setItem('session', JSON.stringify(session));
    startApp();
  } catch (e) { msg.textContent = e.message; }
}

async function logout(silent) {
  if (!silent) { try { await api('POST', '/auth/logout'); } catch (e) { /* ignore */ } }
  session = null;
  sessionStorage.removeItem('session');
  $('appView').classList.add('hidden');
  $('authView').classList.remove('hidden');
}

// ================= App shell =================
const MENUS = {
  ADMIN: [
    ['Users', adminUsers], ['Content Approval', adminContent],
    ['System Settings', adminSettings], ['Activity Monitor', adminActivity], ['Discussion', discussion]
  ],
  INSTRUCTOR: [
    ['My Lessons', instrLessons], ['Create Lesson', instrCreate],
    ['Learner Progress', instrProgress], ['Give Feedback', instrFeedback], ['Discussion', discussion]
  ],
  LEARNER: [
    ['Lessons', learnerLessons], ['My Progress', learnerProgress],
    ['My Feedback', learnerFeedback], ['Discussion', discussion]
  ]
};

function startApp() {
  $('authView').classList.add('hidden');
  $('appView').classList.remove('hidden');
  $('userLabel').textContent = session.name + ' (' + session.role + ')';
  const menu = $('menu');
  menu.innerHTML = '';
  MENUS[session.role].forEach(([label, fn], i) => {
    const b = document.createElement('button');
    b.textContent = label;
    b.onclick = () => {
      [...menu.children].forEach(c => c.classList.remove('active'));
      b.classList.add('active');
      safe(fn);
    };
    menu.appendChild(b);
    if (i === 0) b.click();
  });
}

// ================= ADMIN =================
async function adminUsers() {
  const list = await api('GET', '/admin/users');
  $('content').innerHTML = `
    <div class="card">
      <h2>Add User</h2>
      <div class="row">
        <div><label>Name</label><input id="uName"></div>
        <div><label>Email</label><input id="uEmail" type="email"></div>
        <div><label>Password</label><input id="uPass" type="password"></div>
        <div><label>Role</label>
          <select id="uRole"><option>LEARNER</option><option>INSTRUCTOR</option><option>ADMIN</option></select></div>
      </div>
      <button class="btn" style="margin-top:14px" onclick="addUser()">Create User</button>
    </div>
    <div class="card">
      <h2>All Users</h2>
      <table>
        <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Actions</th></tr>
        ${list.map(u => `<tr>
          <td>${u.id}</td><td>${esc(u.name)}</td><td>${esc(u.email)}</td><td>${u.role}</td>
          <td>
            <button class="btn small gray" onclick="editUser(${u.id}, '${esc(u.name).replace(/'/g, '')}', '${u.role}')">Edit</button>
            <button class="btn small danger" onclick="deleteUser(${u.id})">Delete</button>
          </td></tr>`).join('')}
      </table>
    </div>`;
}
function addUser() {
  safe(async () => {
    const r = await api('POST', '/admin/users', {
      name: $('uName').value.trim(), email: $('uEmail').value.trim(),
      password: $('uPass').value, role: $('uRole').value
    });
    notify(r.message); await adminUsers();
  });
}
function editUser(id, name, role) {
  const newName = prompt('New name:', name);
  if (newName === null) return;
  const newRole = prompt('Role (ADMIN / INSTRUCTOR / LEARNER):', role);
  if (newRole === null) return;
  safe(async () => {
    const r = await api('PUT', '/admin/users/' + id, { name: newName, role: newRole.toUpperCase() });
    notify(r.message); await adminUsers();
  });
}
function deleteUser(id) {
  if (!confirm('Delete this user?')) return;
  safe(async () => { const r = await api('DELETE', '/admin/users/' + id); notify(r.message); await adminUsers(); });
}

async function adminContent() {
  const list = await api('GET', '/admin/lessons');
  $('content').innerHTML = `<div class="card"><h2>Lesson Content Approval</h2>
    ${list.length === 0 ? '<p class="muted">No lessons yet.</p>' : `
    <table>
      <tr><th>Title</th><th>Language</th><th>Instructor</th><th>Questions</th><th>Status</th><th>Action</th></tr>
      ${list.map(l => `<tr>
        <td>${esc(l.title)}</td><td>${esc(l.language)}</td><td>${esc(l.instructorName)}</td>
        <td>${l.questions.length}</td><td><span class="badge ${l.status}">${l.status}</span></td>
        <td>
          <button class="btn small success" onclick="decide(${l.id}, 'approve')">Approve</button>
          <button class="btn small danger" onclick="decide(${l.id}, 'reject')">Reject</button>
        </td></tr>`).join('')}
    </table>`}</div>`;
}
function decide(id, decision) {
  safe(async () => { const r = await api('POST', `/admin/lessons/${id}/${decision}`); notify(r.message); await adminContent(); });
}

async function adminSettings() {
  const s = await api('GET', '/admin/settings');
  $('content').innerHTML = `<div class="card"><h2>System Settings</h2>
    <label>Platform Name</label><input id="sName" value="${esc(s.platformName)}">
    <label>Allow new learner registration</label>
    <select id="sReg">
      <option value="true" ${s.allowRegistration === 'true' ? 'selected' : ''}>Yes</option>
      <option value="false" ${s.allowRegistration === 'false' ? 'selected' : ''}>No</option>
    </select>
    <button class="btn" style="margin-top:14px" onclick="saveSettings()">Save Settings</button></div>`;
}
function saveSettings() {
  safe(async () => {
    const r = await api('PUT', '/admin/settings', { platformName: $('sName').value.trim() || 'Language Platform', allowRegistration: $('sReg').value });
    notify(r.message); loadBrand();
  });
}

async function adminActivity() {
  const list = await api('GET', '/admin/activity');
  $('content').innerHTML = `<div class="card"><h2>Activity Monitoring</h2>
    <button class="btn small" onclick="safe(adminActivity)">Refresh</button>
    <table style="margin-top:12px"><tr><th>Time</th><th>Activity</th></tr>
    ${list.map(a => `<tr><td>${fmt(a.time)}</td><td>${esc(a.message)}</td></tr>`).join('')}</table></div>`;
}

// ================= INSTRUCTOR =================
async function instrLessons() {
  const list = await api('GET', '/instructor/lessons');
  $('content').innerHTML = `<div class="card"><h2>My Lessons</h2>
    ${list.length === 0 ? '<p class="muted">You have not created any lessons yet.</p>' : `
    <table><tr><th>Title</th><th>Language</th><th>Questions</th><th>Status</th><th></th></tr>
    ${list.map(l => `<tr><td>${esc(l.title)}</td><td>${esc(l.language)}</td><td>${l.questions.length}</td>
      <td><span class="badge ${l.status}">${l.status}</span></td>
      <td><button class="btn small danger" onclick="delLesson(${l.id})">Delete</button></td></tr>`).join('')}
    </table>`}</div>`;
}
function delLesson(id) {
  if (!confirm('Delete this lesson?')) return;
  safe(async () => { const r = await api('DELETE', '/instructor/lessons/' + id); notify(r.message); await instrLessons(); });
}

function instrCreate() {
  $('content').innerHTML = `<div class="card"><h2>Create Lesson</h2>
    <div class="row">
      <div><label>Title</label><input id="lTitle" placeholder="e.g. French Numbers 1-10"></div>
      <div><label>Language</label><input id="lLang" placeholder="e.g. French"></div>
    </div>
    <label>Lesson Content</label><textarea id="lContent" placeholder="Write the lesson here..."></textarea>
    <h3>Quiz Questions</h3>
    <div id="qList"></div>
    <button class="btn small gray" onclick="addQuestionForm()">+ Add Question</button><br><br>
    <button class="btn" onclick="saveLesson()">Submit Lesson for Approval</button></div>`;
  addQuestionForm();
}
function addQuestionForm() {
  const d = document.createElement('div');
  d.className = 'q-form';
  d.innerHTML = `
    <label>Question</label><input class="qText">
    <div class="row">
      <div><label>Option 1</label><input class="o1"></div><div><label>Option 2</label><input class="o2"></div>
      <div><label>Option 3</label><input class="o3"></div><div><label>Option 4</label><input class="o4"></div>
    </div>
    <label>Correct option</label>
    <select class="qCorrect"><option value="1">Option 1</option><option value="2">Option 2</option>
      <option value="3">Option 3</option><option value="4">Option 4</option></select>
    <button class="btn small danger" style="margin-top:10px" onclick="this.parentElement.remove()">Remove</button>`;
  $('qList').appendChild(d);
}
function saveLesson() {
  safe(async () => {
    const questions = [...document.querySelectorAll('.q-form')].map(f => ({
      questionText: f.querySelector('.qText').value.trim(),
      option1: f.querySelector('.o1').value.trim(), option2: f.querySelector('.o2').value.trim(),
      option3: f.querySelector('.o3').value.trim(), option4: f.querySelector('.o4').value.trim(),
      correctOption: parseInt(f.querySelector('.qCorrect').value)
    })).filter(q => q.questionText);
    const r = await api('POST', '/instructor/lessons', {
      title: $('lTitle').value.trim(), language: $('lLang').value.trim(), content: $('lContent').value, questions
    });
    notify(r.message); await instrLessons();
  });
}

async function instrProgress() {
  const list = await api('GET', '/instructor/progress');
  $('content').innerHTML = `<div class="card"><h2>Learner Progress Report</h2>
    ${list.length === 0 ? '<p class="muted">No quiz attempts yet.</p>' : `
    <table><tr><th>Learner</th><th>Lesson</th><th>Score</th><th>Date</th></tr>
    ${list.map(p => `<tr><td>${esc(p.learnerName)}</td><td>${esc(p.lessonTitle)}</td>
      <td>${p.score} / ${p.total}</td><td>${fmt(p.takenAt)}</td></tr>`).join('')}</table>`}</div>`;
}

async function instrFeedback() {
  const learners = await api('GET', '/instructor/learners');
  $('content').innerHTML = `<div class="card"><h2>Give Feedback</h2>
    <label>Learner</label>
    <select id="fLearner">${learners.map(l => `<option value="${l.id}">${esc(l.name)} (${esc(l.email)})</option>`).join('')}</select>
    <label>Feedback</label><textarea id="fMsg" placeholder="Write feedback on the learner's progress..."></textarea>
    <button class="btn" style="margin-top:14px" onclick="sendFeedback()">Submit Feedback</button></div>`;
}
function sendFeedback() {
  safe(async () => {
    const r = await api('POST', '/instructor/feedback', { learnerId: $('fLearner').value, message: $('fMsg').value });
    notify(r.message); $('fMsg').value = '';
  });
}

// ================= LEARNER =================
async function learnerLessons() {
  const list = await api('GET', '/learner/lessons');
  $('content').innerHTML = `<h2>Available Lessons</h2>
    ${list.length === 0 ? '<p class="muted">No lessons available yet.</p>' : ''}
    <div class="grid">${list.map(l => `<div class="lesson-card">
      <h3>${esc(l.title)}</h3>
      <p class="muted">${esc(l.language)} · by ${esc(l.instructorName)} · ${l.questions.length} questions</p>
      <button class="btn small" onclick="openLesson(${l.id})">Start Lesson</button></div>`).join('')}</div>`;
}
function openLesson(id) {
  safe(async () => {
    const l = await api('GET', '/learner/lessons/' + id);
    $('content').innerHTML = `<div class="card">
      <button class="btn small gray" onclick="safe(learnerLessons)">&larr; Back</button>
      <h2>${esc(l.title)} <small class="muted">(${esc(l.language)})</small></h2>
      <pre class="content">${esc(l.content)}</pre>
      ${l.questions.length ? '<h3>Quiz</h3>' : ''}
      ${l.questions.map((q, i) => `<div class="question"><b>${i + 1}. ${esc(q.questionText)}</b>
        ${[1, 2, 3, 4].map(n => `<label class="opt"><input type="radio" name="q${i}" value="${n}"> ${esc(q['option' + n])}</label>`).join('')}
      </div>`).join('')}
      ${l.questions.length ? `<button class="btn" onclick="submitQuiz(${l.id}, ${l.questions.length})">Submit Quiz</button>` : ''}
      <div id="quizResult"></div></div>`;
  });
}
function submitQuiz(id, count) {
  safe(async () => {
    const answers = [];
    for (let i = 0; i < count; i++) {
      const c = document.querySelector(`input[name="q${i}"]:checked`);
      answers.push(c ? parseInt(c.value) : 0);
    }
    const r = await api('POST', `/learner/lessons/${id}/submit`, { answers });
    $('quizResult').innerHTML = `<p>Your score:</p><div class="score">${r.score} / ${r.total}</div>`;
  });
}

async function learnerProgress() {
  const list = await api('GET', '/learner/progress');
  const avg = list.length ? Math.round(list.reduce((s, p) => s + (p.total ? p.score / p.total : 0), 0) / list.length * 100) : 0;
  $('content').innerHTML = `<div class="card"><h2>My Progress</h2>
    <p>Quizzes taken: <b>${list.length}</b> &nbsp;|&nbsp; Average score: <b>${avg}%</b></p>
    ${list.length === 0 ? '<p class="muted">Take a lesson quiz to see progress here.</p>' : `
    <table><tr><th>Lesson</th><th>Score</th><th>Date</th></tr>
    ${list.map(p => `<tr><td>${esc(p.lessonTitle)}</td><td>${p.score} / ${p.total}</td><td>${fmt(p.takenAt)}</td></tr>`).join('')}</table>`}</div>`;
}

async function learnerFeedback() {
  const list = await api('GET', '/learner/feedback');
  $('content').innerHTML = `<div class="card"><h2>Instructor Feedback</h2>
    ${list.length === 0 ? '<p class="muted">No feedback yet.</p>' : list.map(f => `
    <div class="post"><b>${esc(f.instructorName)}</b> <small>${fmt(f.createdAt)}</small><br>${esc(f.message)}</div>`).join('')}</div>`;
}

// ================= DISCUSSION (all roles) =================
async function discussion() {
  const list = await api('GET', '/posts');
  $('content').innerHTML = `<div class="card"><h2>Discussion Board</h2>
    <textarea id="pMsg" style="min-height:70px" placeholder="Ask a question or share a tip..."></textarea>
    <button class="btn" style="margin-top:10px" onclick="sendPost()">Post</button></div>
    <div class="card">${list.length === 0 ? '<p class="muted">No posts yet. Start the conversation!</p>' : list.map(p => `
    <div class="post"><b>${esc(p.authorName)}</b> <small>${p.authorRole} · ${fmt(p.createdAt)}</small><br>${esc(p.message)}</div>`).join('')}</div>`;
}
function sendPost() {
  safe(async () => { await api('POST', '/posts', { message: $('pMsg').value }); await discussion(); });
}

// ================= Init =================
loadBrand();
if (session) startApp();
