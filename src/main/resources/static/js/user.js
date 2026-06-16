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
    document.getElementById('login-email').value = '';
    document.getElementById('login-telegram-id').value = '';
    document.getElementById('delivery-method').value = 'EMAIL';
    document.getElementById('login-delivery-method').value = 'EMAIL';
    document.getElementById('telegram-id-group').style.display = 'none';
    document.getElementById('login-telegram-group').style.display = 'none';
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
    const registerFields = document.getElementById('register-fields');
    const loginFields = document.getElementById('login-fields');

    if (mode === 'register') {
        titleEl.textContent = 'Регистрация';
        regFields.style.display = 'block';
        if (registerFields) registerFields.style.display = 'block';
        if (loginFields) loginFields.style.display = 'none';
        document.getElementById('auth-fullname').required = true;
        document.getElementById('auth-phone').required = true;
        switchLink.textContent = 'Уже есть аккаунт? Войти';
        document.getElementById('login-email').required = false;
        document.getElementById('login-telegram-id').required = false;
    } else {
        titleEl.textContent = 'Вход';
        regFields.style.display = 'block';
        if (registerFields) registerFields.style.display = 'none';
        if (loginFields) loginFields.style.display = 'block';
        document.getElementById('auth-fullname').required = false;
        document.getElementById('auth-phone').required = false;
        switchLink.textContent = 'Нет аккаунта? Зарегистрироваться';
        document.getElementById('login-email').required = true;
        document.getElementById('login-telegram-id').required = false;
    }
}

function initDeliveryMethodToggle() {
    // Для регистрации
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

    // Для входа
    const loginDeliverySelect = document.getElementById('login-delivery-method');
    const loginTelegramGroup = document.getElementById('login-telegram-group');
    if (loginDeliverySelect) {
        loginDeliverySelect.addEventListener('change', () => {
            if (loginDeliverySelect.value === 'TELEGRAM') {
                loginTelegramGroup.style.display = 'block';
                document.getElementById('login-telegram-id').required = true;
                document.getElementById('login-email').required = false;
            } else {
                loginTelegramGroup.style.display = 'none';
                document.getElementById('login-telegram-id').required = false;
                document.getElementById('login-email').required = true;
            }
        });
        if (loginDeliverySelect.value === 'TELEGRAM') {
            loginTelegramGroup.style.display = 'block';
            document.getElementById('login-telegram-id').required = true;
            document.getElementById('login-email').required = false;
        } else {
            loginTelegramGroup.style.display = 'none';
            document.getElementById('login-telegram-id').required = false;
            document.getElementById('login-email').required = true;
        }
    }
}

async function sendCode(phone, deliveryMethod, telegramId, email, isLogin) {
    const payload = { deliveryMethod };
    if (isLogin) {
        // Вход: отправляем email + опционально telegramId
        if (!email) throw new Error('Укажите email');
        payload.email = email;
        if (telegramId) payload.telegramId = telegramId;
    } else {
        // Регистрация: отправляем телефон + опционально email/telegramId
        if (!phone) throw new Error('Введите телефон');
        payload.phone = phone;
        if (telegramId) payload.telegramId = telegramId;
        if (email) payload.email = email;
    }
    const response = await fetch(`${API_BASE_URL}/auth/send-code`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    });
    if (!response.ok) throw new Error(await response.text());
    return response.text();
}

async function verifyCode(phone, code, consentGiven, firstName, lastName, middleName, email, deliveryMethod, telegramId, preferredDelivery) {
    const payload = { phone, code, deliveryMethod };
    if (consentGiven) {
        payload.consentGiven = true;
    }
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
            const deliveryMethod = currentAuthMode === 'register' 
                ? document.getElementById('delivery-method').value 
                : document.getElementById('login-delivery-method').value;
            const isLogin = currentAuthMode === 'login';
            let phone = null, email = null, telegramId = null;

            if (isLogin) {
                // Вход: проверяем email и, если выбран Telegram, telegramId
                email = document.getElementById('login-email').value.trim();
                if (!email) {
                    errorDiv.textContent = 'Введите email';
                    errorDiv.style.display = 'block';
                    return;
                }
                if (deliveryMethod === 'TELEGRAM') {
                    telegramId = document.getElementById('login-telegram-id').value.trim();
                    if (!telegramId) {
                        errorDiv.textContent = 'Укажите Telegram username';
                        errorDiv.style.display = 'block';
                        return;
                    }
                }
            } else {
                // Регистрация: проверяем телефон
                phone = document.getElementById('auth-phone').value.trim();
                if (!phone) {
                    errorDiv.textContent = 'Введите номер телефона';
                    errorDiv.style.display = 'block';
                    return;
                }
                const fullname = document.getElementById('auth-fullname').value.trim();
                if (!fullname) {
                    errorDiv.textContent = 'Введите ФИО';
                    errorDiv.style.display = 'block';
                    return;
                }
                if (deliveryMethod === 'TELEGRAM') {
                    telegramId = document.getElementById('telegram-id').value.trim();
                    if (!telegramId) {
                        errorDiv.textContent = 'Укажите Telegram username';
                        errorDiv.style.display = 'block';
                        return;
                    }
                } else if (deliveryMethod === 'EMAIL') {
                    email = document.getElementById('auth-email').value.trim();
                    if (!email) {
                        errorDiv.textContent = 'Укажите email';
                        errorDiv.style.display = 'block';
                        return;
                    }
                }
            }

            try {
                errorDiv.style.display = 'none';
                const result = await sendCode(phone, deliveryMethod, telegramId, email, isLogin);
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

            if (!code) {
                errorDiv.textContent = 'Введите код';
                errorDiv.style.display = 'block';
                return;
            }

            try {
                errorDiv.style.display = 'none';

                let regFirstName = null, regLastName = null, regMiddleName = null, regEmail = null;
                let telegramId = null;
                let preferredDelivery = null;
                let consentGiven = false;
                let deliveryMethod = currentAuthMode === 'register' 
                    ? document.getElementById('delivery-method').value 
                    : document.getElementById('login-delivery-method').value;

                if (currentAuthMode === 'register') {
                    const fullname = document.getElementById('auth-fullname').value.trim();
                    regEmail = document.getElementById('auth-email').value.trim() || null;
                    if (fullname) {
                        const parts = fullname.split(/\s+/);
                        regLastName = parts[0] || null;
                        regFirstName = parts[1] || null;
                        regMiddleName = parts[2] || null;
                    }
                    preferredDelivery = deliveryMethod;
                    if (preferredDelivery === 'TELEGRAM') {
                        telegramId = document.getElementById('telegram-id').value.trim() || null;
                    }
                    const consentCheckbox = document.getElementById('auth-consent');
                    if (!consentCheckbox || !consentCheckbox.checked) {
                        errorDiv.textContent = 'Необходимо дать согласие на обработку персональных данных';
                        errorDiv.style.display = 'block';
                        return;
                    }
                    consentGiven = true;
                } else {
                    // Вход: только email и telegramId (если выбран Telegram)
                    regEmail = document.getElementById('login-email').value.trim() || null;
                    if (deliveryMethod === 'TELEGRAM') {
                        telegramId = document.getElementById('login-telegram-id').value.trim() || null;
                    }
                    consentGiven = false;
                }

                const authResponse = await verifyCode(
                    phone, code, consentGiven,
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