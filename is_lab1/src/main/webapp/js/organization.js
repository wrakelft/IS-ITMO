let currentPage = 1;
const pageSize = 5;

let allOrganizations = [];
let editingOrganizationId = null;


function getFilteredOrganizations() {
    const input = document.getElementById('nameFilter');
    const filterValue = input ? input.value.toLowerCase().trim() : '';

    if (!filterValue) {
        return allOrganizations;
    }

    return allOrganizations.filter(org =>
        (org.name || '').toLowerCase().includes(filterValue)
    );
}

function applyNameFilter() {
    currentPage = 1;
    renderTable();
}

function resetFilter() {
    const input = document.getElementById('nameFilter');
    if (input) input.value = '';
    currentPage = 1;
    renderTable();
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

    document.getElementById('organizationEditSection').style.display = 'flex';
}

function saveOrganization() {
    const name = document.getElementById('organizationName').value;
    const rating = document.getElementById('organizationRating').value;
    const annualTurnover = document.getElementById('organizationAnnualTurnover').value;
    const employeesCount = document.getElementById('organizationEmployees').value;
    const type = document.getElementById('organizationType').value;
    const coordinatesX = document.getElementById('organizationCoordinatesX').value;
    const coordinatesY = document.getElementById('organizationCoordinatesY').value;
    const officialAddress = document.getElementById('organizationAddress').value;
    const postalAddress = document.getElementById('organizationPostalAddress').value;

    if (!name || !rating || !annualTurnover || !employeesCount || !coordinatesX || !coordinatesY) {
        alert('Все обязательные поля должны быть заполнены');
        return;
    }

    const organization = {
        name,
        rating: Number(rating),
        annualTurnover: Number(annualTurnover),
        employeesCount: Number(employeesCount),
        type,
        coordinates: {
            x: Number(coordinatesX),
            y: Number(coordinatesY)
        },
        officialAddress: {
            street: officialAddress
        },
        postalAddress: {
            street: postalAddress
        }
    };

    const method = editingOrganizationId ? 'PUT' : 'POST';
    const url = editingOrganizationId
        ? `api/organizations/${editingOrganizationId}`
        : 'api/organizations';

    fetch(url, {
        method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(organization),
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка сохранения');
            }
            return response.json();
        })
        .then(data => {
            console.log('Организация сохранена:', data);
            document.getElementById('organizationEditSection').style.display = 'none';
            editingOrganizationId = null;
            loadTableData(); // перезагружаем таблицу с сервера
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

    fetch(`api/organizations/${id}?cascade=true`, {
        method: 'DELETE'
    })
        .then(response => {
            if (!response.ok && response.status !== 204) {
                throw new Error('Ошибка удаления');
            }
            console.log('Организация удалена:', id);
            loadTableData();
        })
        .catch((error) => {
            console.error('Ошибка при удалении организации:', error);
        });
}


function previousPage() {
    if (currentPage > 1) {
        currentPage--;
        renderTable();
    }
}

function nextPage() {
    const filtered = getFilteredOrganizations();
    const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));

    if (currentPage < totalPages) {
        currentPage++;
        renderTable();
    }
}


function loadTableData() {
    fetch('api/organizations')
        .then(response => response.json())
        .then(data => {
            allOrganizations = data;
            renderTable();
        })
        .catch((error) => {
            console.error('Ошибка при загрузке данных организаций:', error);
        });
}

function renderTable() {
    const tableBody = document.getElementById('organizationsTableBody');
    tableBody.innerHTML = '';

    const organizations = getFilteredOrganizations();
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
                <button class="primary edit-btn">Редактировать</button>
                <button class="delete-btn">Удалить</button>
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
});
