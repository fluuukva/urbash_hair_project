let pendingCallback = null;
let currentAuthMode = 'register';

function getUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
}

function setUser(user) {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('user');

    if (user) {
        localStorage.setItem('user', JSON.stringify(user));
        if (user.token) localStorage.setItem('token', user.token);
        if (user.id) localStorage.setItem('userId', user.id);
    }
    updateHeaderUser();
    prefillForms();
    updateReviewUserDisplay();
}

function logout() {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    updateHeaderUser();
    prefillForms();
    updateReviewUserDisplay();
}

function getUserInitials(user) {
    if (!user) return 'User';
    if (user.firstName || user.lastName) {
        const parts = [];
        if (user.lastName) parts.push(user.lastName);
        if (user.firstName) parts.push(user.firstName.charAt(0).toUpperCase() + '.');
        if (user.middleName) parts.push(user.middleName.charAt(0).toUpperCase() + '.');
        return parts.length > 0 ? parts.join(' ') : 'User';
    }
    if (user.phone) {
        const phone = user.phone.replace(/\D/g, '');
        return phone.slice(-4);
    }
    return 'User';
}

function getFullName(user) {
    if (!user) return '';
    const parts = [user.lastName, user.firstName, user.middleName].filter(Boolean);
    return parts.join(' ') || '';
}

function updateHeaderUser() {
    const user = getUser();
    const loginBtn = document.getElementById('login-btn');
    const headerUser = document.getElementById('header-user');
    if (loginBtn) loginBtn.style.display = user ? 'none' : 'inline-block';
    if (headerUser) {
        if (user) {
            const displayName = getUserInitials(user);
            headerUser.innerHTML = `<span>Привет, ${displayName}!</span> <button onclick="logout()" style="background:none;border:none;color:#e0b06b;cursor:pointer;">Выйти</button>`;
        } else {
            headerUser.innerHTML = '';
        }
    }
}

function prefillForms() {
    const user = getUser();
    if (user) {
        const fullName = getFullName(user);
        const inputs = {
            'request-name': fullName,
            'request-email': user.email || '',
            'request-phone': user.phone || '',
            'name': fullName,
            'email': user.email || '',
            'phone': user.phone || ''
        };
        for (const [id, value] of Object.entries(inputs)) {
            const el = document.getElementById(id);
            if (el) el.value = value;
        }
    }
}

function isLoggedIn() {
    return localStorage.getItem('token') !== null;
}

function checkLogin(callback) {
    if (isLoggedIn()) callback();
    else {
        pendingCallback = callback;
        openAuthModal();
    }
}

function openAuthModal() {
    const modal = document.getElementById('registration-modal');
    if (!modal) return;
    document.getElementById('step-phone').style.display = 'block';
    document.getElementById('step-code').style.display = 'none';
    document.getElementById('auth-phone').value = '';
    document.getElementById('auth-code').value = '';
    document.getElementById('auth-fullname').value = '';
    document.getElementById('auth-email').value = '';
    document.getElementById('telegram-id').value = '';
    document.getElementById('delivery-method').value = 'SMS';
    document.getElementById('telegram-id-group').style.display = 'none';
    document.getElementById('auth-error').style.display = 'none';

    setAuthMode('register');
    modal.classList.add('is-visible');
    document.body.style.overflow = 'hidden';
}

function closeAuthModal() {
    const modal = document.getElementById('registration-modal');
    if (modal) {
        modal.classList.remove('is-visible');
        document.body.style.overflow = '';
    }
    pendingCallback = null;
}

function setAuthMode(mode) {
    currentAuthMode = mode;
    const titleEl = document.getElementById('auth-modal-title');
    const regFields = document.getElementById('registration-fields');
    const switchLink = document.getElementById('switch-auth-mode');

    if (mode === 'register') {
        titleEl.textContent = 'Регистрация';
        regFields.style.display = 'block';
        document.getElementById('auth-fullname').required = true;
        switchLink.textContent = 'Уже есть аккаунт? Войти';
    } else {
        titleEl.textContent = 'Вход';
        regFields.style.display = 'none';
        document.getElementById('auth-fullname').required = false;
        switchLink.textContent = 'Нет аккаунта? Зарегистрироваться';
    }
}

function initDeliveryMethodToggle() {
    const deliverySelect = document.getElementById('delivery-method');
    const telegramGroup = document.getElementById('telegram-id-group');
    if (deliverySelect) {
        deliverySelect.addEventListener('change', () => {
            if (deliverySelect.value === 'TELEGRAM') {
                telegramGroup.style.display = 'block';
            } else {
                telegramGroup.style.display = 'none';
            }
        });
        if (deliverySelect.value === 'TELEGRAM') telegramGroup.style.display = 'block';
        else telegramGroup.style.display = 'none';
    }
}

// ИСПРАВЛЕННАЯ ФУНКЦИЯ: добавлен параметр email
async function sendCode(phone, deliveryMethod, telegramId, email) {
    const payload = { phone, deliveryMethod };
    if (telegramId) payload.telegramId = telegramId;
    if (email) payload.email = email;  // передаём email на сервер
    const response = await fetch(`${API_BASE_URL}/auth/send-code`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    });
    if (!response.ok) throw new Error(await response.text());
    return response.text();
}

async function verifyCode(phone, code, consentGiven, firstName, lastName, middleName, email, deliveryMethod, telegramId, preferredDelivery) {
    const payload = { phone, code, consentGiven, deliveryMethod };
    if (firstName) payload.firstName = firstName;
    if (lastName) payload.lastName = lastName;
    if (middleName) payload.middleName = middleName;
    if (email) payload.email = email;
    if (telegramId) payload.telegramId = telegramId;
    if (preferredDelivery) payload.preferredDelivery = preferredDelivery;

    const response = await fetch(`${API_BASE_URL}/auth/verify-code`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
}

async function updateUserProfile(fullname, email) {
    const parts = fullname.trim().split(/\s+/);
    const lastName = parts[0] || '';
    const firstName = parts[1] || '';
    const middleName = parts[2] || '';
    const payload = { lastName, firstName, middleName };
    if (email) payload.email = email;

    const token = localStorage.getItem('token');
    const response = await fetch(`${API_BASE_URL}/client/profile`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
    });
    if (!response.ok) throw new Error('Не удалось обновить профиль');
    return response.json();
}

function initAuthModal() {
    const sendCodeBtn = document.getElementById('send-code-btn');
    const backToPhone = document.getElementById('back-to-phone');
    const authForm = document.getElementById('auth-form');
    const errorDiv = document.getElementById('auth-error');
    const switchLink = document.getElementById('switch-auth-mode');

    initDeliveryMethodToggle();

    if (switchLink) {
        switchLink.addEventListener('click', (e) => {
            e.preventDefault();
            const newMode = currentAuthMode === 'register' ? 'login' : 'register';
            setAuthMode(newMode);
        });
    }

    if (sendCodeBtn) {
        sendCodeBtn.addEventListener('click', async () => {
            const phoneInput = document.getElementById('auth-phone');
            const phone = phoneInput.value.trim();
            if (!phone) {
                errorDiv.textContent = 'Введите номер телефона';
                errorDiv.style.display = 'block';
                return;
            }

            if (currentAuthMode === 'register') {
                const fullname = document.getElementById('auth-fullname').value.trim();
                if (!fullname) {
                    errorDiv.textContent = 'Введите ФИО';
                    errorDiv.style.display = 'block';
                    return;
                }
            }

            const deliveryMethod = document.getElementById('delivery-method').value;
            let telegramId = null;
            if (deliveryMethod === 'TELEGRAM') {
                telegramId = document.getElementById('telegram-id').value.trim();
                if (!telegramId) {
                    errorDiv.textContent = 'Укажите Telegram ID для получения кода';
                    errorDiv.style.display = 'block';
                    return;
                }
            }

            // Получаем email из поля формы (для способа EMAIL)
            let email = null;
            if (deliveryMethod === 'EMAIL') {
                email = document.getElementById('auth-email').value.trim();
                if (!email) {
                    errorDiv.textContent = 'Укажите email для получения кода';
                    errorDiv.style.display = 'block';
                    return;
                }
            }

            try {
                errorDiv.style.display = 'none';
                const result = await sendCode(phone, deliveryMethod, telegramId, email);
                alert(`Код отправлен (${deliveryMethod})! ${result}`);
                document.getElementById('step-phone').style.display = 'none';
                document.getElementById('step-code').style.display = 'block';
                document.getElementById('auth-code').focus();
            } catch (error) {
                errorDiv.textContent = 'Ошибка: ' + error.message;
                errorDiv.style.display = 'block';
            }
        });
    }

    if (backToPhone) {
        backToPhone.addEventListener('click', (e) => {
            e.preventDefault();
            document.getElementById('step-phone').style.display = 'block';
            document.getElementById('step-code').style.display = 'none';
            errorDiv.style.display = 'none';
        });
    }

    if (authForm) {
        authForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const phone = document.getElementById('auth-phone').value.trim();
            const code = document.getElementById('auth-code').value.trim();
            const consentCheckbox = document.getElementById('auth-consent');

            if (!code) {
                errorDiv.textContent = 'Введите код';
                errorDiv.style.display = 'block';
                return;
            }

            if (!consentCheckbox || !consentCheckbox.checked) {
                errorDiv.textContent = 'Необходимо дать согласие на обработку персональных данных';
                errorDiv.style.display = 'block';
                return;
            }

            try {
                errorDiv.style.display = 'none';

                let regFirstName = null, regLastName = null, regMiddleName = null, regEmail = null;
                let telegramId = null;
                let preferredDelivery = null;
                if (currentAuthMode === 'register') {
                    const fullname = document.getElementById('auth-fullname').value.trim();
                    regEmail = document.getElementById('auth-email').value.trim() || null;
                    if (fullname) {
                        const parts = fullname.split(/\s+/);
                        regLastName = parts[0] || null;
                        regFirstName = parts[1] || null;
                        regMiddleName = parts[2] || null;
                    }
                    preferredDelivery = document.getElementById('delivery-method').value;
                    if (preferredDelivery === 'TELEGRAM') {
                        telegramId = document.getElementById('telegram-id').value.trim() || null;
                    }
                }
                const deliveryMethod = document.getElementById('delivery-method').value;

                const authResponse = await verifyCode(
                    phone, code, true,
                    regFirstName, regLastName, regMiddleName, regEmail,
                    deliveryMethod, telegramId, preferredDelivery
                );

                setUser(authResponse);

                if (currentAuthMode === 'register') {
                    const fullname = document.getElementById('auth-fullname').value.trim();
                    const email = document.getElementById('auth-email').value.trim();
                    if (fullname) {
                        const updatedUser = await updateUserProfile(fullname, email);
                        setUser({ ...authResponse, ...updatedUser });
                    }
                }

                closeAuthModal();
                if (pendingCallback) {
                    pendingCallback();
                    pendingCallback = null;
                }
            } catch (error) {
                errorDiv.textContent = 'Ошибка: ' + error.message;
                errorDiv.style.display = 'block';
            }
        });
    }
}

function authFetch(url, options = {}) {
    const token = localStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers,
    };
    return fetch(API_BASE_URL + url, { ...options, headers });
}

function updateReviewUserDisplay() {
    const user = getUser();
    const reviewUsername = document.querySelector('.write-review__username');
    if (reviewUsername) {
        reviewUsername.textContent = user ? getUserInitials(user) : 'User';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    updateHeaderUser();
    prefillForms();
    updateReviewUserDisplay();
    initAuthModal();

    const loginBtn = document.getElementById('login-btn');
    if (loginBtn) {
        loginBtn.addEventListener('click', () => {
            setAuthMode('login');
            openAuthModal();
        });
    }

    const closeBtn = document.getElementById('close-registration-modal');
    if (closeBtn) closeBtn.addEventListener('click', closeAuthModal);

    const modal = document.getElementById('registration-modal');
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeAuthModal();
        });
    }
});