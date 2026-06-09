document.addEventListener('DOMContentLoaded', function() {
    const applicationForm = document.getElementById('application-form');
    const successMessage = document.getElementById('success-message');

    function getUser() {
      const user = localStorage.getItem('user');
      return user ? JSON.parse(user) : null;
    }

    if (applicationForm) {
        applicationForm.addEventListener('submit', async function(event) {
            event.preventDefault(); 
            
            const user = getUser();
            const formData = new FormData(applicationForm);
            const data = Object.fromEntries(formData.entries());
            
            if (user && user.id) {
                data.clientId = user.id;
            }

            try {
                const response = await fetch(`${API_BASE_URL}/job-applications`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(data),
                });

                if (response.ok) {
                    successMessage.style.display = 'block';
                    applicationForm.reset();
                    setTimeout(() => {
                        successMessage.style.display = 'none';
                    }, 7000);
                } else {
                    alert('Ошибка при отправке заявки');
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Ошибка при отправке заявки');
            }
        });
    }
});