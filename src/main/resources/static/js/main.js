// main.js – финальная версия с авторизацией
let currentDate = new Date();
let selectedDateStr = null;
let currentSelectedSlotId = null;

document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('appointment-modal');
    const closeBtn = document.getElementById('close-modal-btn');
    const form = document.getElementById('appointment-form');
    const serviceSelect = document.getElementById('service-id');
    const masterSelect = document.getElementById('master-id');
    const slotsContainer = document.getElementById('slots-container');
    const slotsList = document.getElementById('slots-list');
    const notesInput = document.getElementById('notes');
    const hiddenSlotId = document.getElementById('selected-slot-id');
    const hiddenDate = document.getElementById('selected-date');

    // Загрузка мастеров (публичный эндпоинт)
    async function loadMasters() {
        try {
            const res = await fetch('/api/masters');
            const masters = await res.json();
            masterSelect.innerHTML = '<option value="">-- Выберите мастера --</option>';
            masters.forEach(m => {
                const opt = document.createElement('option');
                opt.value = m.id;
                opt.textContent = `${m.firstName} ${m.lastName} (${m.specialization || 'мастер'})`;
                masterSelect.appendChild(opt);
            });
        } catch (e) { console.error(e); }
    }

    // Загрузка услуг (публичный эндпоинт)
    async function loadServices() {
        try {
            const res = await fetch('/api/services');
            const services = await res.json();
            serviceSelect.innerHTML = '<option value="">-- Выберите услугу --</option>';
            services.forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.id;
                opt.textContent = s.name;
                serviceSelect.appendChild(opt);
            });
        } catch (e) { console.error(e); }
    }

    // Загрузка слотов для выбранной даты
    async function loadSlotsForDate(dateStr) {
        const masterId = masterSelect.value;
        const serviceId = serviceSelect.value;
        if (!masterId) {
            slotsContainer.style.display = 'none';
            return;
        }
        let url = `/api/appointments/available?date=${dateStr}&masterId=${masterId}`;
        if (serviceId && serviceId !== '') url += `&serviceId=${serviceId}`;
        try {
            const res = await fetch(url);
            const slots = await res.json();
            renderSlots(slots, dateStr);
        } catch (e) {
            console.error(e);
            slotsContainer.style.display = 'none';
        }
    }

    function renderSlots(slots, dateStr) {
        slotsList.innerHTML = '';
        if (!slots || slots.length === 0) {
            slotsList.innerHTML = '<p style="color: #ccc;">Нет свободных слотов на эту дату.</p>';
            slotsContainer.style.display = 'block';
            return;
        }
        slots.forEach(slot => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'slot-button';
            btn.textContent = slot.time;
            btn.dataset.id = slot.id;
            btn.addEventListener('click', () => {
                document.querySelectorAll('.slot-button').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                currentSelectedSlotId = slot.id;
                hiddenSlotId.value = slot.id;
                hiddenDate.value = dateStr;
            });
            slotsList.appendChild(btn);
        });
        slotsContainer.style.display = 'block';
    }

    // Календарь месяца: если есть хотя бы один AVAILABLE слот => день активный,
    // если слоты есть, но нет AVAILABLE (только BOOKED/CONFIRMED/CANCELLED) => has-booked,
    // если нет слотов => disabled
    async function renderCalendar(year, month) {

        const firstDay = new Date(year, month, 1);
        let startDayOfWeek = firstDay.getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const prevMonthDays = new Date(year, month, 0).getDate();

        const selectedMasterId = masterSelect.value;
        const selectedServiceId = serviceSelect.value ? serviceSelect.value : null;

        const slotsByDate = new Map(); // dateStr -> { hasAvailable: boolean, hasAny: boolean }

        try {
            if (selectedMasterId) {
                const startDateStr = new Date(year, month, 1).toISOString().split('T')[0];
                const endDateStr = new Date(year, month, daysInMonth).toISOString().split('T')[0];

                let url = `/api/appointments/all-by-month?startDate=${startDateStr}&endDate=${endDateStr}&masterId=${selectedMasterId}`;
                if (selectedServiceId) url += `&serviceId=${selectedServiceId}`;

                const res = await fetch(url);
                if (res.ok) {
                    const allSlots = await res.json();
                    allSlots.forEach(slot => {
                        const dateStr = slot.date;
                        if (!dateStr) return;

                        if (!slotsByDate.has(dateStr)) {
                            slotsByDate.set(dateStr, { hasAvailable: false, hasAny: false });
                        }

                        const info = slotsByDate.get(dateStr);
                        info.hasAny = true;
                        if (slot.status === 'AVAILABLE') info.hasAvailable = true;
                    });
                }
            }
        } catch (e) {
            console.error('Failed to load all-by-month:', e);
        }

        // DEBUG: статусы слотов по дням
        try {
            const debugSlotsByDate = {};
            if (slotsByDate && typeof slotsByDate.forEach === 'function') {
                slotsByDate.forEach((value, key) => {
                    debugSlotsByDate[key] = { ...value, statuses: undefined };
                });
            }

            console.group('DEBUG /api/appointments/all-by-month result');
            // Воссоздаём список статусов по фактическим слотам через повторный запрос не делаем,
            // но выводим вычисленные флаги по дням.
            console.log('dates in slotsByDate =', Array.from(slotsByDate.keys()).length);
            Array.from(slotsByDate.entries()).forEach(([dateStr, info]) => {
                console.log(dateStr,
                    'hasAvailable=', info.hasAvailable,
                    'hasBooked=', !info.hasAvailable && info.hasAny,
                    'hasAnySlot=', info.hasAny
                );
            });
            console.groupEnd();
        } catch (err) {
            console.warn('DEBUG slotsByDate failed:', err);
        }



        let calendarHtml = `
            <div class="calendar-header">
                <button id="prev-month">&lt;</button>
                <span>${firstDay.toLocaleString('ru', { month: 'long', year: 'numeric' })}</span>
                <button id="next-month">&gt;</button>
            </div>
            <div class="calendar-weekdays">
                <div>Пн</div><div>Вт</div><div>Ср</div><div>Чт</div><div>Пт</div><div>Сб</div><div>Вс</div>
            </div>
            <div class="calendar-days" id="calendar-days"></div>
        `;
        document.getElementById('month-calendar').innerHTML = calendarHtml;
        const daysContainer = document.getElementById('calendar-days');
        let daysHtml = '';
        let startOffset = (startDayOfWeek === 0 ? 6 : startDayOfWeek - 1);
        for (let i = startOffset - 1; i >= 0; i--) {
            let day = prevMonthDays - i;
            daysHtml += `<div class="calendar-day other-month" data-date="">${day}</div>`;
        }
        for (let d = 1; d <= daysInMonth; d++) {
            let dateStr = `${year}-${String(month+1).padStart(2,'0')}-${String(d).padStart(2,'0')}`;
            const info = slotsByDate.get(dateStr);

            let className = '';
            if (!info || !info.hasAny) {
                className = 'disabled';
            } else if (info.hasAvailable) {
                className = '';
            } else {
                className = 'has-booked';
            }

            daysHtml += `<div class="calendar-day ${className}" data-date="${dateStr}">${d}</div>`;
        }

        // inline styles for calendar-day status (to match requirement)
        const styleId = 'calendar-day-status-style';
        if (!document.getElementById(styleId)) {
            const style = document.createElement('style');
            style.id = styleId;
            style.textContent = `.calendar-day.has-booked { opacity: 0.5; background: #1a1a1a; }
.calendar-day.disabled { opacity: 0.3; pointer-events: none; }`;
            document.head.appendChild(style);
        }

        let totalCells = 42;
        let currentCells = startOffset + daysInMonth;
        let remaining = totalCells - currentCells;
        for (let i = 1; i <= remaining; i++) {
            daysHtml += `<div class="calendar-day other-month" data-date="">${i}</div>`;
        }
        daysContainer.innerHTML = daysHtml;

        document.querySelectorAll('.calendar-day[data-date]').forEach(el => {
            el.addEventListener('click', () => {
                if (el.classList.contains('has-booked') || el.classList.contains('disabled')) return;
                const date = el.dataset.date;
                if (!date) return;
                document.querySelectorAll('.calendar-day').forEach(d => d.classList.remove('selected'));
                el.classList.add('selected');
                selectedDateStr = date;
                loadSlotsForDate(date);
            });

        });

        document.getElementById('prev-month').addEventListener('click', () => {
            currentDate = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1);
            renderCalendar(currentDate.getFullYear(), currentDate.getMonth());
        });
        document.getElementById('next-month').addEventListener('click', () => {
            currentDate = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1);
            renderCalendar(currentDate.getFullYear(), currentDate.getMonth());
        });
    }

    // Открытие модалки
    const openButtons = document.querySelectorAll('.price-services__item, .nav-btn-appointment');
    openButtons.forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            const token = localStorage.getItem('token');
            if (!token) {
                if (typeof openAuthModal === 'function') openAuthModal();
                else alert('Пожалуйста, войдите в систему.');
                return;
            }
            if (masterSelect.options.length <= 1) await loadMasters();
            if (serviceSelect.options.length <= 1) await loadServices();
            form.reset();
            hiddenSlotId.value = '';
            slotsContainer.style.display = 'none';
            selectedDateStr = null;
            currentDate = new Date();
            renderCalendar(currentDate.getFullYear(), currentDate.getMonth());
            modal.classList.add('is-visible');
            document.body.style.overflow = 'hidden';
            const serviceType = btn.dataset.service;
            if (serviceType) {
                const map = { keratin: '1', botox: '2', recovery: '3', courses: '4' };
                if (map[serviceType]) serviceSelect.value = map[serviceType];
            }
        });
    });

    closeBtn.addEventListener('click', () => {
        modal.classList.remove('is-visible');
        document.body.style.overflow = '';
    });
    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.classList.remove('is-visible');
            document.body.style.overflow = '';
        }
    });

    serviceSelect.addEventListener('change', () => {
        if (selectedDateStr) loadSlotsForDate(selectedDateStr);
    });
    masterSelect.addEventListener('change', () => {
        if (selectedDateStr) loadSlotsForDate(selectedDateStr);
    });

    // Отправка формы бронирования
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const slotId = hiddenSlotId.value;
        if (!slotId) {
            alert('Пожалуйста, выберите дату и время.');
            return;
        }
        const notes = notesInput.value.trim();
        try {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('Пожалуйста, войдите в систему.');
            return;
        }

        const response = await authFetch(`/appointments/${slotId}/book`, {
            method: 'POST',
            body: JSON.stringify({ notes })
        });

            if (response.ok) {
                const successDiv = document.getElementById('appointment-success');
                successDiv.style.display = 'block';
                form.reset();
                hiddenSlotId.value = '';
                slotsContainer.style.display = 'none';
                if (selectedDateStr) loadSlotsForDate(selectedDateStr);
                setTimeout(() => {
                    modal.classList.remove('is-visible');
                    document.body.style.overflow = '';
                    successDiv.style.display = 'none';
                }, 3000);
            } else {
                const errorText = await response.text();
                showError('Ошибка: ' + errorText, { containerId: 'auth-error', hideAfterMs: 6000 });
            }
        } catch (error) {
            alert('Ошибка при бронировании.');
        }
    });
});
