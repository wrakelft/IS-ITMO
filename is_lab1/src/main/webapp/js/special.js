const API_BASE = "/is_lab1-1.0-SNAPSHOT/api/organizations";

function showError(msg) {
    const el = document.getElementById("specialGlobalError");
    el.textContent = msg;
    el.style.display = "block";
    const ok = document.getElementById("specialGlobalOk");
    ok.style.display = "none";
}

function showOk(msg) {
    const el = document.getElementById("specialGlobalOk");
    el.textContent = msg;
    el.style.display = "block";
    const err = document.getElementById("specialGlobalError");
    err.style.display = "none";
}

async function safeFetch(url, options = {}, {allow404 = false} = {}) {
    const res = await fetch(url, options);

    if (allow404 && res.status === 404) return null;

    const text = await res.text();
    if (!res.ok) {
        throw new Error(text || `HTTP ${res.status}`);
    }
    if (!text) return null;

    try { return JSON.parse(text); } catch { return text; }
}

async function loadAverageRating() {
    try {
        const data = await safeFetch(`${API_BASE}/average-rating`);
        document.getElementById("avgRatingResult").textContent =
            `Среднее rating: ${data === null ? "нет данных" : data}`;
        showOk("Готово");
    } catch (e) {
        showError(`Не удалось получить среднее rating: ${e.message}`);
    }
}

async function loadMinRatingOrg() {
    try {
        const org = await safeFetch(`${API_BASE}/min-rating`, { allow404: true });

        renderOrganizationsToTbody("minRatingTableBody", org ? [org] : []);
        showOk(org ? "Готово" : "Организаций нет");
    } catch (e) {
        console.error(e);
        renderOrganizationsToTbody("minRatingTableBody", []);
        showError(e.message || "Ошибка загрузки");
    }
}

async function loadRatingGreaterThan() {
    const raw = document.getElementById("ratingThreshold").value.trim();
    if (!raw) return showError("Введи порог rating");

    const threshold = Number(raw);
    if (!Number.isFinite(threshold) || threshold < 0) {
        return showError("Порог rating должен быть числом >= 0");
    }
    try {
        const list = await safeFetch(`${API_BASE}/rating-greater-than/${encodeURIComponent(threshold)}`);
        renderOrganizationsToTbody("ratingGreaterTableBody", list, true);
        showOk(`Найдено: ${(list || []).length}`);
    } catch (e) {
        showError(`Не удалось получить список: ${e.message}`);
    }
}

async function fireAll() {
    const id = document.getElementById("fireOrgId").value;
    if (!id) return showError("Введи ID организации");
    return fireAllFromRow(Number(id));
}

async function hire() {
    const id = document.getElementById("hireOrgId").value;
    if (!id) return showError("Введи ID организации");
    return hireFromRow(Number(id));
}

async function fireAllFromRow(orgId) {
    try {
        await safeFetch(`${API_BASE}/${orgId}/fire-all`, { method: "PUT" });
        showOk(`Уволил всех в организации ${orgId}`);
    } catch (e) {
        showError(`Не удалось уволить, организации не существует`);
    }
}

async function hireFromRow(orgId) {
    try {
        await safeFetch(`${API_BASE}/${orgId}/hire`, { method: "PUT" });
        showOk(`Нанял сотрудника в организации ${orgId}`);
    } catch (e) {
        showError(`Не удалось нанять, организации не существует`);
    }
}

function escapeHtml(s) {
    return (s ?? "")
        .toString()
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function formatCoords(o) {
    return o.coordinates ? `${o.coordinates.x}, ${o.coordinates.y}` : '';
}

function formatAddress(a) {
    return a && a.street ? a.street : '';
}

function formatCreationDate(o) {
    return o.creationDate ? o.creationDate.substring(0, 19).replace('T', ' ') : '';
}

function renderOrganizationsToTbody(tbodyId, organizations) {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!organizations || organizations.length === 0) {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td colspan="10" style="opacity:0.8;">Нет данных</td>`;
        tbody.appendChild(tr);
        return;
    }

    organizations.forEach(o => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
      <td>${o.id ?? ''}</td>
      <td>${o.name ?? ''}</td>
      <td>${formatCoords(o)}</td>
      <td>${formatCreationDate(o)}</td>
      <td>${formatAddress(o.officialAddress)}</td>
      <td>${o.annualTurnover ?? ''}</td>
      <td>${o.employeesCount ?? ''}</td>
      <td>${o.rating ?? ''}</td>
      <td>${o.type ?? ''}</td>
      <td>${formatAddress(o.postalAddress)}</td>
    `;
        tbody.appendChild(tr);
    });
}

wsBus.start();
wsBus.on((msg) => {
    if (["FIRE_ALL","HIRE","CREATE","UPDATE","DELETED"].includes(msg.type)) {
        showOk(`Событие: ${msg.type} id=${msg.id ?? ""}`);

        if (document.getElementById("minRatingTableBody")) loadMinRatingOrg();
        if (document.getElementById("avgRatingResult")) loadAverageRating();
        if (document.getElementById("ratingThreshold")?.value?.trim()) loadRatingGreaterThan();
    }
});

window.loadAverageRating = loadAverageRating;
window.loadMinRatingOrg = loadMinRatingOrg;
window.loadRatingGreaterThan = loadRatingGreaterThan;
window.fireAll = fireAll;
window.hire = hire;



