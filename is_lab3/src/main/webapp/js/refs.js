const API_ADDRESSES = "api/addresses";
const API_COORDS = "api/coordinates";

let cachedAddresses = [];
let cachedCoords = [];

let replaceContext = null;

async function safeFetch(url, options) {
    const res = await fetch(url, options);
    const text = await res.text();
    if (!res.ok) {
        let msg = text || `HTTP ${res.status}`;
        try {
            const obj = JSON.parse(text);
            msg = obj?.message || obj?.error || msg;
        } catch {}
        throw new Error(msg);
    }
    if (!text) return null;
    try { return JSON.parse(text); } catch { return text; }
}

function showError(msg) {
    const el = document.getElementById("refsGlobalError");
    el.textContent = msg;
    el.style.display = "block";
    const ok = document.getElementById("refsGlobalOk");
    ok.style.display = "none";
}

function showOk(msg) {
    const ok = document.getElementById("refsGlobalOk");
    ok.textContent = msg;
    ok.style.display = "block";
    const el = document.getElementById("refsGlobalError");
    el.style.display = "none";
}

function escapeHtml(s) {
    return String(s ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

async function loadAll() {
    try {
        cachedAddresses = await safeFetch(API_ADDRESSES);
    } catch (e) {
        cachedAddresses = [];
        showError(`Не смог загрузить адреса: ${e.message}`);
    }

    try {
        cachedCoords = await safeFetch(API_COORDS);
    } catch (e) {
        cachedCoords = [];
        showError(`Не смог загрузить координаты: ${e.message}`);
    }

    renderAddresses();
    renderCoords();
}

function renderAddresses() {
    const body = document.getElementById("addressesBody");
    body.innerHTML = "";

    (cachedAddresses || []).forEach(a => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
      <td>${a.id}</td>
      <td>${escapeHtml(a.street)}</td>
      <td>
        <div class="actions">
          <button class="danger" onclick="tryDeleteAddress(${a.id})">Удалить</button>
        </div>
      </td>
    `;
        body.appendChild(tr);
    });
}

function renderCoords() {
    const body = document.getElementById("coordsBody");
    body.innerHTML = "";

    (cachedCoords || []).forEach(c => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
      <td>${c.id}</td>
      <td>${c.x ?? ""}</td>
      <td>${c.y ?? ""}</td>
      <td>
        <div class="actions">
          <button class="danger" onclick="tryDeleteCoord(${c.id})">Удалить</button>
        </div>
      </td>
    `;
        body.appendChild(tr);
    });
}

async function tryDeleteAddress(id) {
    await deleteWithOptionalReplace("address", id);
}

async function tryDeleteCoord(id) {
    await deleteWithOptionalReplace("coord", id);
}

async function deleteWithOptionalReplace(kind, id) {
    const base = kind === "address" ? API_ADDRESSES : API_COORDS;

    try {
        await safeFetch(`${base}/${id}`, { method: "DELETE" });
        showOk("Удалено");
        await loadAll();
    } catch (e) {
        // если сервер вернул 409 / текст типа "used" — открываем модалку
        // safeFetch уже кинул Error(text), так что проверяем по сообщению
        const msg = String(e.message || "");
        const looksLikeConflict = msg.includes("409") || msg.toLowerCase().includes("used") || msg.toLowerCase().includes("replacewith");

        if (looksLikeConflict) {
            openReplaceModal(kind, id, msg);
            return;
        }

        showError(`Не удалось удалить: ${msg}`);
    }
}

function openReplaceModal(kind, deleteId, serverMsg) {
    replaceContext = { kind, deleteId };

    document.getElementById("replaceError").style.display = "none";

    const title = document.getElementById("replaceTitle");
    const hint = document.getElementById("replaceHint");

    if (kind === "address") {
        title.textContent = "Перепривязка адреса";
        hint.textContent = "Этот адрес используется организациями. Выбери адрес-замену, чтобы можно было удалить старый.";
        fillReplaceSelect(cachedAddresses, deleteId, (a) => `${a.id} — ${a.street}`);
    } else {
        title.textContent = "Перепривязка координат";
        hint.textContent = "Эти координаты используются организациями. Выбери координаты-замену, чтобы удалить старые.";
        fillReplaceSelect(cachedCoords, deleteId, (c) => `${c.id} — (${c.x}, ${c.y})`);
    }

    document.getElementById("replaceModal").style.display = "flex";
}

function fillReplaceSelect(list, deleteId, labelFn) {
    const sel = document.getElementById("replaceSelect");
    sel.innerHTML = "";

    const options = (list || []).filter(x => x.id !== deleteId);

    if (options.length === 0) {
        const opt = document.createElement("option");
        opt.value = "";
        opt.textContent = "Нет доступных вариантов для замены";
        sel.appendChild(opt);
        sel.disabled = true;
        return;
    }

    sel.disabled = false;
    options.forEach(item => {
        const opt = document.createElement("option");
        opt.value = item.id;
        opt.textContent = labelFn(item);
        sel.appendChild(opt);
    });
}

function closeReplaceModal() {
    replaceContext = null;
    document.getElementById("replaceModal").style.display = "none";
}

async function confirmReplace() {
    if (!replaceContext) return;

    const { kind, deleteId } = replaceContext;
    const base = kind === "address" ? API_ADDRESSES : API_COORDS;

    const sel = document.getElementById("replaceSelect");
    const replaceWith = sel && !sel.disabled ? sel.value : "";

    if (!replaceWith) {
        return showReplaceError("Выбери, на что заменить.");
    }

    const url = `${base}/${deleteId}?replaceWith=${encodeURIComponent(replaceWith)}`;

    try {
        await safeFetch(url, { method: "DELETE" });
        closeReplaceModal();
        showOk("Удалено с перепривязкой");
        await loadAll();
    } catch (e) {
        showReplaceError(prettifyServerError(e));
    }
}


function showReplaceError(msg) {
    const el = document.getElementById("replaceError");
    el.textContent = msg;
    el.style.display = "block";
}

document.addEventListener("DOMContentLoaded", () => {
    loadAll();
    if (window.wsBus) {
        wsBus.start();
        wsBus.on((msg) => {
            if (["CREATE","UPDATE","DELETED","IMPORT"].includes(msg.type)) {
                showOk(`Событие: ${msg.type} id=${msg.id ?? ""}`);
                loadAll();
            }
        });
    }
});

function prettifyServerError(err) {
    const raw = String(err?.message ?? err ?? "");
    try {
        const obj = JSON.parse(raw);
        if (obj && typeof obj === "object") {
            return obj.message || obj.error || obj.details || raw;
        }
    } catch {}
    return raw;
}


window.tryDeleteAddress = tryDeleteAddress;
window.tryDeleteCoord = tryDeleteCoord;
window.closeReplaceModal = closeReplaceModal;
window.confirmReplace = confirmReplace;

