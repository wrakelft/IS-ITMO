let currentPage = 1;
const pageSize = 5;

let allOrganizations = [];
let editingOrganizationId = null;
let originalRefs = {
    coordinatesId: null,
    officialAddressId: null,
    postalAddressId: null,
    coordinatesX: null,
    coordinatesY: null,
    officialStreet: null,
    postalStreet: null
};

let filterState = { field: "name", value: "" };
let sortState = { field: "id", dir: "asc" };

function norm(s) {
    return (s ?? "").toString().trim().toLowerCase();
}

function getFieldValue(org, field) {
    switch (field) {
        case "name": return org.name;
        case "type": return org.type;
        case "officialAddress": return org.officialAddress?.street;
        case "postalAddress": return org.postalAddress?.street;
        case "creationDate":
            return org.creationDate ? org.creationDate.substring(0, 19).replace("T", " ") : "";
        case "id": return org.id;
        default: return "";
    }
}

function applyFilter() {
    filterState.field = document.getElementById("filterField").value;
    filterState.value = document.getElementById("filterValue").value;
    currentPage = 1;
    renderTable();
}

function applySort() {
    sortState.field = document.getElementById("sortField").value;
    sortState.dir = document.getElementById("sortDir").value;
    currentPage = 1;
    renderTable();
}

function resetFilter() {
    const v = document.getElementById('filterValue');
    if (v) v.value = "";
    filterState.value = "";
    currentPage = 1;
    renderTable();
}

function getProcessedOrganizations() {
    let list = [...allOrganizations];

    const fvRaw = (filterState.value ?? "").toString().trim();
    const fv = norm(fvRaw);

    if (fv) {
        if (filterState.field === "id") {
            const idNum = Number(fvRaw);
            if (!Number.isFinite(idNum)) {
                showGlobalError("Id должен быть числом")
                list = [];
            } else {
                showGlobalError("");
                list = list.filter(org => Number(org.id) === idNum);
            }
        } else {
            showGlobalError("");
            list = list.filter(org => norm(getFieldValue(org, filterState.field)) === fv);
        }
    } else {
        showGlobalError("");
    }

    const dirMul = sortState.dir === "desc" ? -1 : 1;
    list.sort((a, b) => {
        const va = getFieldValue(a, sortState.field);
        const vb = getFieldValue(b, sortState.field);

        if (sortState.field === "id") return (Number(va) - Number(vb)) * dirMul;

        const sa = norm(va);
        const sb = norm(vb);
        if (sa < sb) return -1 * dirMul;
        if (sa > sb) return 1 * dirMul;
        return 0;
    });

    return list;
}



function clearOrganizationForm() {
    document.getElementById('organizationName').value = '';
    document.getElementById('organizationRating').value = '';
    document.getElementById('organizationAnnualTurnover').value = '';
    document.getElementById('organizationEmployees').value = '';
    document.getElementById('organizationType').value = 'COMMERCIAL';
    document.getElementById('organizationCoordinatesX').value = '';
    document.getElementById('organizationCoordinatesY').value = '';
    document.getElementById('organizationAddress').value = '';
    document.getElementById('organizationPostalAddress').value = '';
}

function startCreateOrganization() {
    editingOrganizationId = null;
    document.getElementById('organizationEditTitle').textContent = 'Новая организация';
    clearOrganizationForm();
    document.getElementById('organizationEditSection').style.display = 'flex';
}


function editOrganization(org) {
    editingOrganizationId = org.id;

    originalRefs.coordinatesId = org.coordinates?.id ?? null;
    originalRefs.officialAddressId = org.officialAddress?.id ?? null;
    originalRefs.postalAddressId = org.postalAddress?.id ?? null;

    originalRefs.coordinatesX = org.coordinates?.x != null ? Number(org.coordinates.x) : null;
    originalRefs.coordinatesY = org.coordinates?.y != null ? Number(org.coordinates.y) : null;
    originalRefs.officialStreet = normalizeStr(org.officialAddress?.street);
    originalRefs.postalStreet = normalizeStr(org.postalAddress?.street);

    document.getElementById('organizationEditTitle').textContent = 'Редактирование организации';

    document.getElementById('organizationName').value = org.name || '';
    document.getElementById('organizationRating').value =
        org.rating != null ? org.rating : '';
    document.getElementById('organizationAnnualTurnover').value =
        org.annualTurnover != null ? org.annualTurnover : '';
    document.getElementById('organizationEmployees').value =
        org.employeesCount != null ? org.employeesCount : '';
    document.getElementById('organizationType').value = org.type || 'COMMERCIAL';

    document.getElementById('organizationCoordinatesX').value =
        org.coordinates && org.coordinates.x != null ? org.coordinates.x : '';
    document.getElementById('organizationCoordinatesY').value =
        org.coordinates && org.coordinates.y != null ? org.coordinates.y : '';

    document.getElementById('organizationAddress').value =
        org.officialAddress && org.officialAddress.street ? org.officialAddress.street : '';

    document.getElementById('organizationPostalAddress').value =
        org.postalAddress && org.postalAddress.street ? org.postalAddress.street : '';
    clearFormError();
    document.getElementById('organizationEditSection').style.display = 'flex';
}

function normalizeStr(s) {
    return (s ?? '').toString().trim();
}

function saveOrganization() {
    clearFormError();

    const name = normalizeStr(document.getElementById('organizationName').value);
    const ratingRaw = document.getElementById('organizationRating').value;
    const annualTurnoverRaw = document.getElementById('organizationAnnualTurnover').value;
    const employeesCountRaw = document.getElementById('organizationEmployees').value;
    const type = document.getElementById('organizationType').value;
    const coordinatesXRaw = document.getElementById('organizationCoordinatesX').value;
    const coordinatesYRaw = document.getElementById('organizationCoordinatesY').value;
    const officialAddress = normalizeStr(document.getElementById('organizationAddress').value);
    const postalAddress = normalizeStr(document.getElementById('organizationPostalAddress').value);

    if (!name) return showFormError("Название обязательно");
    if (!officialAddress) return showFormError("Адрес (official) обязателен");

    if (!isFiniteNumber(ratingRaw)) return showFormError("Рейтинг должен быть числом");
    const rating = Number(ratingRaw);
    if (rating <= 0) return showFormError("Рейтинг должен быть > 0");

    if (!isFiniteNumber(annualTurnoverRaw)) return showFormError("Годовой оборот должен быть числом");
    const annualTurnover = Number(annualTurnoverRaw);
    if (annualTurnover <= 0) return showFormError("Годовой оборот должен быть > 0");

    if (!isFiniteNumber(employeesCountRaw)) return showFormError("Количество сотрудников должно быть числом");
    const employeesCount = Number(employeesCountRaw);
    if (employeesCount < 0) return showFormError("Количество сотрудников должно быть ≥ 0");

    if (!isFiniteNumber(coordinatesXRaw) || !isFiniteNumber(coordinatesYRaw))
        return showFormError("Координаты X/Y должны быть числами");

    const xNum = Number(coordinatesXRaw);
    const yNum = Number(coordinatesYRaw);

    const isUpdate = !!editingOrganizationId;

    const payload = {
        name,
        rating,
        annualTurnover,
        employeesCount,
        type
    };

    const coordsChanged = (originalRefs.coordinatesX !== xNum || originalRefs.coordinatesY !== yNum);

    if (!isUpdate || coordsChanged) {
        payload.coordinates = { x: xNum, y: yNum };
    } else if (originalRefs.coordinatesId != null) {
        payload.coordinatesId = originalRefs.coordinatesId;
    }

    const officialChanged = originalRefs.officialStreet !== officialAddress;

    if (!isUpdate || officialChanged) {
        payload.officialAddress = { street: officialAddress };
    } else if (originalRefs.officialAddressId != null) {
        payload.officialAddressId = originalRefs.officialAddressId;
    }

    const postalChanged = originalRefs.postalStreet !== postalAddress;

    if (!isUpdate) {
        if (postalAddress) payload.postalAddress = { street: postalAddress };
    } else {
        if (postalChanged) {
            payload.postalAddress = postalAddress ? { street: postalAddress } : null;
        } else {
            if (originalRefs.postalAddressId != null) payload.postalAddressId = originalRefs.postalAddressId;
        }
    }

    const method = isUpdate ? 'PUT' : 'POST';
    const url = isUpdate ? `api/organizations/${editingOrganizationId}` : 'api/organizations';

    fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    })
        .then(async (resp) => {
            const parsed = await parseResponse(resp);
            if (!resp.ok) {
                const msg = pickErrorMessage(resp, parsed);
                showFormError(msg);
                throw new Error(msg);
            }
            return parsed.data ?? parsed.text ?? null;
        })
        .then(() => {
            clearFormError();
            document.getElementById('organizationEditSection').style.display = 'none';
            editingOrganizationId = null;
            loadTableData();
        })
        .catch((error) => {
            console.error('Ошибка при сохранении организации:', error);
        });
}

function cancelOrganizationEdit() {
    editingOrganizationId = null;
    document.getElementById('organizationEditSection').style.display = 'none';
}


function deleteOrganization(id) {
    if (!confirm('Удалить организацию?')) {
        return;
    }

    fetch(`api/organizations/${id}`, {
        method: 'DELETE'
    })
        .then(async (resp) => {
            if (resp.ok || resp.status === 204) return;
            const parsed = await parseResponse(resp);
            throw new Error(pickErrorMessage(resp, parsed));
        })
        .then(() => loadTableData())
        .catch((error) => {
            console.error('Ошибка при удалении организации:', error);
            alert("Удаление не ок: " + error.message);
        });
}


function previousPage() {
    if (currentPage > 1) {
        currentPage--;
        renderTable();
    }
}

function nextPage() {
    const filtered = getProcessedOrganizations();
    const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));

    if (currentPage < totalPages) {
        currentPage++;
        renderTable();
    }
}


function loadTableData() {
    fetch('api/organizations')
        .then(async (resp) => {
            const parsed = await parseResponse(resp);
            if (!resp.ok) {
                throw new Error(pickErrorMessage(resp, parsed));
            }
            return parsed.data ?? [];
        })
        .then(data => {
            allOrganizations = Array.isArray(data) ? data : [];
            renderTable();
        })
        .catch((error) => {
            console.error('Ошибка при загрузке данных организаций:', error);
            showGlobalError("Загрузка не ок: " + error.message);
        });
}

function renderTable() {
    const tableBody = document.getElementById('organizationsTableBody');
    tableBody.innerHTML = '';

    const organizations = getProcessedOrganizations();
    const totalPages = Math.max(1, Math.ceil(organizations.length / pageSize));
    if (currentPage > totalPages) currentPage = totalPages;

    const startIndex = (currentPage - 1) * pageSize;
    const pageItems = organizations.slice(startIndex, startIndex + pageSize);

    pageItems.forEach(organization => {
        const row = document.createElement('tr');

        const coordsText = organization.coordinates
            ? `${organization.coordinates.x}, ${organization.coordinates.y}`
            : '';

        const officialAddressText = organization.officialAddress && organization.officialAddress.street
            ? organization.officialAddress.street
            : '';

        const postalAddressText = organization.postalAddress && organization.postalAddress.street
            ? organization.postalAddress.street
            : '';

        const creationDateText = organization.creationDate
            ? organization.creationDate.substring(0, 19).replace('T', ' ')
            : '';

        row.innerHTML = `
            <td>${organization.id}</td>
            <td>${organization.name}</td>
            <td>${coordsText}</td>
            <td>${creationDateText}</td>
            <td>${officialAddressText}</td>
            <td>${organization.annualTurnover}</td>
            <td>${organization.employeesCount}</td>
            <td>${organization.rating}</td>
            <td>${organization.type}</td>
            <td>${postalAddressText}</td>
            <td>
                <div class="actions">
                    <button class="primary edit-btn">Редактировать</button>
                    <button class="danger delete-btn">Удалить</button>
                </div>
            </td>
        `;

        row.querySelector('.edit-btn').addEventListener('click', () => editOrganization(organization));
        row.querySelector('.delete-btn').addEventListener('click', () => deleteOrganization(organization.id));

        tableBody.appendChild(row);
    });

    const pageInfo = document.getElementById('pageInfo');
    if (pageInfo) {
        pageInfo.textContent = `${currentPage} / ${totalPages}`;
    }
}


document.addEventListener('DOMContentLoaded', () => {
    loadTableData();

    const fieldSel = document.getElementById("filterField");
    const valInp = document.getElementById("filterValue");

    if (fieldSel && valInp) {
        fieldSel.addEventListener("change", () => {
            valInp.type = fieldSel.value === "id" ? "number" : "text";
            valInp.value = "";
        });
    }
});

if (window.wsBus) {
    wsBus.start();
    wsBus.on((msg) => {
        if (["CREATE","UPDATE","DELETED","FIRE_ALL","HIRE"].includes(msg.type)) {
            loadTableData();
        }
    });
}



function showFormError(msg) {
    const box = document.getElementById("organizationFormError");
    if (!box) return alert(msg);
    box.textContent = msg;
    box.style.display = "block";
}

function clearFormError() {
    const box = document.getElementById("organizationFormError");
    if (!box) return;
    box.textContent = "";
    box.style.display = "none";
}

function showGlobalError(msg) {
    const box = document.getElementById("organizationGlobalError");
    if (!box) return console.error(msg);
    box.textContent = msg;
    box.style.display = msg ? "block" : "none";
}

function isFiniteNumber(v) {
    const n = Number(v);
    return Number.isFinite(n);
}

async function importOrganizations() {
    const input = document.getElementById("importFile");
    const status = document.getElementById("importStatus");

    const setStatus = (text, isError = false) => {
        if (!status) return;
        status.textContent = text || "";
        status.classList.toggle("error", !!isError);
        status.classList.toggle("info", !isError && !!text);
    };

    const file = input?.files?.[0];
    if (!file) {
        setStatus("Выбери .json файл", true);
        return;
    }

    const fd = new FormData();
    fd.append("file", file);

    try {
        setStatus("Загружаю...", false);

        const resp = await fetch("api/import/organizations", {
            method: "POST",
            body: fd
        });

        const parsed = await parseResponse(resp);

        if (!resp.ok) {
            const msg = pickErrorMessage(resp, parsed);
            setStatus("Импорт не ок: " + msg, true);
            return;
        }

        const added = parsed?.data?.addedCount ?? 0;
        setStatus(`Готово добавлено: ${added}`, false);

        loadTableData();
        input.value = "";

    } catch (e) {
        console.error(e);
        setStatus("Ошибка импорта (см. консоль)", true);
    }
}

async function parseResponse(resp) {
    const ct = resp.headers.get("content-type") || "";
    let data = null;
    let text = "";

    if (ct.includes("application/json")) {
        data = await resp.json().catch(() => null);
    } else {
        text = await resp.text().catch(() => "");
        const t = (text || "").trim();
        if (t.startsWith("{") || t.startsWith("[")) {
            try { data = JSON.parse(t); } catch {}
        }
    }

    return { data, text };
}

function pickErrorMessage(resp, parsed) {
    return (
        parsed?.data?.message ||
        parsed?.data?.error ||
        (parsed?.text ? parsed.text.trim() : "") ||
        `HTTP ${resp.status}`
    );
}



window.applyFilter = applyFilter;
window.resetFilter = resetFilter;
window.applySort = applySort;

window.startCreateOrganization = startCreateOrganization;
window.saveOrganization = saveOrganization;
window.cancelOrganizationEdit = cancelOrganizationEdit;

window.previousPage = previousPage;
window.nextPage = nextPage;
window.editOrganization = editOrganization;
window.deleteOrganization = deleteOrganization;
window.importOrganizations = importOrganizations;


