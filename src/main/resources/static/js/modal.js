document.addEventListener('DOMContentLoaded', function() {
  if (typeof flatpickr !== 'undefined') {
    flatpickr("#appointment-date", {
      locale: "ru",
      minDate: "today",
      dateFormat: "Y-m-d",
      placeholder: "гггг-мм-дд"
    });
  }

  function getUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  const modalAppointment = document.getElementById('appointment-modal');
  const modalRequest = document.getElementById('request-modal');
  const closeBtns = document.querySelectorAll('.appointment-modal__close');
  const navAppointmentBtns = document.querySelectorAll('.nav-btn-appointment, #hero-appointment-btn');
  const requestLink = document.getElementById('request-link');
  const serviceItems = document.querySelectorAll('.price-services__item');

  function openModal(modal) {
    if (!modal) return;
    modal.classList.add('is-visible');
  }

  function closeModal(modal) {
    if (!modal) return;
    modal.classList.remove('is-visible');
  }

  closeBtns.forEach(btn => {
    btn.addEventListener('click', function(e) {
      const modal = this.closest('.appointment-modal');
      closeModal(modal);
    });
  });

  [modalAppointment, modalRequest].forEach(modal => {
    if (modal) {
      modal.addEventListener('click', function(e) {
        if (e.target === modal) {
          closeModal(modal);
        }
      });
    }
  });

  navAppointmentBtns.forEach(btn => {
    btn.addEventListener('click', function(e) {
      e.preventDefault();
      const user = getUser();
      if (!user) {
        openModal(document.getElementById('registration-modal'));
        return;
      }
      const form = document.getElementById('main-appointment-form');
      if (form) form.reset();
      const successMsg = document.getElementById('appointment-success');
      if (successMsg) successMsg.style.display = 'none';
      openModal(modalAppointment);
    });
  });

  if (requestLink) {
    requestLink.addEventListener('click', function(e) {
      e.preventDefault();
      const user = getUser();
      if (!user) {
        openModal(document.getElementById('registration-modal'));
        return;
      }
      const form = document.getElementById('request-form');
      if (form) form.reset();
      const successMsg = document.getElementById('request-success');
      if (successMsg) successMsg.style.display = 'none';
      openModal(modalRequest);
    });
  }

  serviceItems.forEach(item => {
    item.addEventListener('click', function(e) {
      e.preventDefault();
      const user = getUser();
      if (!user) {
        openModal(document.getElementById('registration-modal'));
        return;
      }
      const service = this.dataset.service;
      const serviceSelect = document.getElementById('serviceId');
      if (service && serviceSelect) {
        const serviceMap = {
          'keratin': 1,
          'botox': 2,
          'recovery': 3,
          'courses': 4
        };
        serviceSelect.value = serviceMap[service] || '';
        openModal(modalAppointment);
      }
    });
  });

  const appointmentForm = document.getElementById('main-appointment-form');
  if (appointmentForm) {
    appointmentForm.addEventListener('submit', async function(e) {
      e.preventDefault();

      const user = getUser();
      const formData = new FormData(appointmentForm);
      const rawData = Object.fromEntries(formData.entries());

      const data = {
        appointment_date: rawData.appointment_date,
        time: rawData.time || '',
        notes: rawData.notes || '',
        serviceId: parseInt(rawData.serviceId, 10),
        masterId: parseInt(rawData.masterId, 10),
        phone: rawData.phone || '',
        clientId: user ? user.id : null
      };

      try {
        const response = await fetch(`${API_BASE_URL}/appointments`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(data)
        });

        if (response.ok) {
          const successMsg = document.getElementById('appointment-success');
          if (successMsg) {
            successMsg.style.display = 'block';
            setTimeout(() => {
              successMsg.style.display = 'none';
              appointmentForm.reset();
              closeModal(modalAppointment);
            }, 2000);
          }
        } else {
          const errorText = await response.text();
          console.error('Server error:', errorText);
          showError(errorText, { containerId: 'request-error' });
        }
      } catch (error) {
        console.error('Error:', error);
        alert('Ошибка при отправке заявки');
      }
    });
  }

  function sendCourseOrJobApplication(form, successElementId, closeModalElement) {
    return async function(e) {
      e.preventDefault();

      const user = getUser();
      const formData = new FormData(form);
      const rawData = Object.fromEntries(formData.entries());

      const fullName = rawData.name ? rawData.name.trim().split(' ') : [];
      const firstName = fullName[0] || '';
      const lastName = fullName.length > 1 ? fullName.slice(1).join(' ') : '';

      let courseId = null;
      let vacancy = null;
      const interest = rawData.interest || '';

      if (interest.includes('Курс')) {
        if (interest.includes('Мастер кератина')) {
          courseId = 1;
        } else if (interest.includes('Ботокс')) {
          courseId = 2;
        }
      } else if (interest.includes('Вакансия')) {
        if (interest.includes('Мастер')) {
          vacancy = 'Мастер';
        } else if (interest.includes('Администратор')) {
          vacancy = 'Администратор';
        }
      }

      const data = {
        firstName: firstName,
        lastName: lastName,
        email: rawData.email,
        phone: rawData.phone,
        interest: interest,
        message: rawData.message || '',
        courseId: courseId,
        vacancy: vacancy,
        clientId: user ? user.id : null
      };

      try {
        const response = await fetch(`${API_BASE_URL}/course-applications`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(data)
        });

        if (response.ok) {
          const successMsg = document.getElementById(successElementId);
          if (successMsg) {
            successMsg.style.display = 'block';
            setTimeout(() => {
              successMsg.style.display = 'none';
              form.reset();
              if (closeModalElement) closeModal(closeModalElement);
            }, 2000);
          }
        } else {
          const errorText = await response.text();
          console.error('Server error:', errorText);
          showError(errorText, { containerId: 'request-error' });
        }
      } catch (error) {
        console.error('Error:', error);
        alert('Ошибка при отправке заявки');
      }
    };
  }

  const requestForm = document.getElementById('request-form');
  if (requestForm) {
    requestForm.addEventListener('submit', sendCourseOrJobApplication(requestForm, 'request-success', modalRequest));
  }

  const applicationForm = document.getElementById('application-form');
  if (applicationForm) {
    const appFormLink = document.getElementById('application-form-link');
    if (appFormLink) {
      appFormLink.addEventListener('click', function(e) {
        const user = getUser();
        if (!user) {
          e.preventDefault();
          openModal(document.getElementById('registration-modal'));
        }
      });
    }
    applicationForm.addEventListener('submit', sendCourseOrJobApplication(applicationForm, 'success-message', null));
  }

  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
      if (modalAppointment && modalAppointment.classList.contains('is-visible')) closeModal(modalAppointment);
      if (modalRequest && modalRequest.classList.contains('is-visible')) closeModal(modalRequest);
    }
  });
});