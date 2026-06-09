const API_BASE_URL = '/api';

function getToken() {
    return localStorage.getItem('token');
}

async function authFetch(url, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers
    };
    const response = await fetch(API_BASE_URL + url, { ...options, headers });
    if (response.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        alert('Сессия истекла, войдите снова.');
        window.location.reload();
    }
    return response;
}