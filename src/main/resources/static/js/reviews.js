// static/js/reviews.js

async function fetchReviews() {
  try {
    const response = await fetch(`${API_BASE_URL}/reviews`);
    if (!response.ok) throw new Error('Failed to fetch reviews');
    return await response.json();
  } catch (error) {
    console.error('Error fetching reviews:', error);
    return [];
  }
}

function renderReviewsToTrack(reviews) {
    const track = document.getElementById('reviews-track');
    if (!track) return;
    
    track.innerHTML = '';
    const approvedReviews = reviews.filter(r => r.status === 'APPROVED');
    
    if (approvedReviews.length === 0) {
        track.innerHTML = '<div class="review-card" style="text-align:center;padding:40px;">Пока нет одобренных отзывов.</div>';
        return;
    }
    
    approvedReviews.forEach(review => {
        const card = document.createElement('div');
        card.className = 'review-card';
        const rating = parseInt(review.rating) || 0;
        let starsHtml = '';
        for (let i = 1; i <= 5; i++) {
            starsHtml += `<span class="star ${i <= rating ? 'filled' : ''}">★</span>`;
        }
        const username = review.client ? 
            `${review.client.lastName || ''} ${review.client.firstName || ''}`.trim() || 'Аноним' : 
            'Аноним';
        card.innerHTML = `
            <div class="review-card__header">
                <span class="review-card__username">${escapeHtml(username)}</span>
                <div class="review-card__rating">${starsHtml}</div>
            </div>
            <p class="review-card__text">${escapeHtml(review.comment || '')}</p>
            <span class="review-card__date">${escapeHtml(review.date || '')}</span>
        `;
        track.appendChild(card);
    });
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

function updateOverallRating(reviews) {
    const approvedReviews = reviews.filter(r => r.status === 'APPROVED');
    const avgContainer = document.querySelector('.reviews-section__average-rating');
    const starsContainer = document.querySelector('.reviews-section__rating--overall');
    
    if (approvedReviews.length === 0) {
        if (avgContainer) avgContainer.textContent = 'Нет оценок';
        if (starsContainer) starsContainer.innerHTML = '';
        return;
    }
    
    const sum = approvedReviews.reduce((acc, r) => acc + (parseInt(r.rating) || 0), 0);
    const average = sum / approvedReviews.length;
    if (avgContainer) avgContainer.textContent = `${average.toFixed(1)} / 5`;
    
    // Очищаем контейнер звёзд и создаём 5 звёзд заново
    starsContainer.innerHTML = '';
    for (let i = 1; i <= 5; i++) {
        const starSpan = document.createElement('span');
        starSpan.className = 'star';
        if (i <= Math.floor(average)) {
            starSpan.classList.add('filled');
        } else if (i === Math.floor(average) + 1) {
            const fraction = average - Math.floor(average);
            starSpan.classList.add('partial');
            starSpan.style.setProperty('--fill-width', `${fraction * 100}%`);
        } else {
            starSpan.classList.add('unfilled');
        }
        starsContainer.appendChild(starSpan);
    }
}

document.addEventListener('DOMContentLoaded', async () => { 
    const track = document.getElementById('reviews-track');
    const nextBtn = document.getElementById('review-next-btn');
    const prevBtn = document.getElementById('review-prev-btn');
    const searchInput = document.getElementById('reviews-search-input');
    const sendReviewBtn = document.querySelector('.write-review__button');
    const reviewInput = document.querySelector('.write-review__input');
    const editableStars = document.querySelectorAll('.write-review__rating--editable .star');
    const editableRatingText = document.querySelector('.write-review__rating--editable .write-review__rating-text');
    const anonymousCheckbox = document.getElementById('review-anonymous');

    let allReviews = await fetchReviews();
    renderReviewsToTrack(allReviews);
    updateOverallRating(allReviews);

    if (!track || !nextBtn || !prevBtn) return;

    const gap = 20;
    let isAnimating = false;
    let allCards = Array.from(track.querySelectorAll('.review-card'));
    let currentCards = [...allCards];

    function getCardWidth() {
        const card = track.querySelector('.review-card');
        if (!card) return 0;
        return card.offsetWidth + gap;
    }

    function updateActive() {
        const cards = track.querySelectorAll('.review-card');
        cards.forEach(card => card.classList.remove('active'));
        if (cards[1]) cards[1].classList.add('active');
    }

    function renderCards(cardsArray) {
        track.innerHTML = '';
        cardsArray.forEach(card => track.appendChild(card));
        updateActive();
    }

    function nextSlide() {
        if (isAnimating || currentCards.length <= 1) return;
        isAnimating = true;
        const cardWidth = getCardWidth();
        track.style.transform = `translateX(-${cardWidth}px)`;
        track.addEventListener('transitionend', function handler() {
            track.style.transition = 'none';
            const first = currentCards.shift();
            currentCards.push(first);
            renderCards(currentCards);
            track.style.transform = 'translateX(0)';
            track.offsetHeight;
            track.style.transition = 'transform 0.4s ease';
            isAnimating = false;
            track.removeEventListener('transitionend', handler);
        });
    }

    function prevSlide() {
        if (isAnimating || currentCards.length <= 1) return;
        isAnimating = true;
        const cardWidth = getCardWidth();
        track.style.transition = 'none';
        const last = currentCards.pop();
        currentCards.unshift(last);
        renderCards(currentCards);
        track.style.transform = `translateX(-${cardWidth}px)`;
        track.offsetHeight;
        track.style.transition = 'transform 0.4s ease';
        track.style.transform = 'translateX(0)';
        track.addEventListener('transitionend', function handler() {
            isAnimating = false;
            track.removeEventListener('transitionend', handler);
        });
    }

    nextBtn.addEventListener('click', nextSlide);
    prevBtn.addEventListener('click', prevSlide);
    updateActive();

    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            if (!query) {
                currentCards = [...allCards];
            } else {
                currentCards = allCards.filter(card => {
                    const username = card.querySelector('.review-card__username')?.textContent.toLowerCase() || '';
                    const reviewText = card.querySelector('.review-card__text')?.textContent.toLowerCase() || '';
                    return username.includes(query) || reviewText.includes(query);
                });
            }
            renderCards(currentCards);
        });
    }

    let currentRating = 0;
    editableStars.forEach(star => {
        star.addEventListener('mouseover', () => {
            const value = parseInt(star.dataset.value);
            editableStars.forEach((s, i) => {
                if (i < value) s.classList.add('hover');
                else s.classList.remove('hover');
            });
        });
        star.addEventListener('mouseout', () => {
            editableStars.forEach(s => s.classList.remove('hover'));
        });
        star.addEventListener('click', () => {
            currentRating = parseInt(star.dataset.value);
            editableStars.forEach((s, i) => {
                if (i < currentRating) s.classList.add('filled');
                else s.classList.remove('filled');
            });
            editableRatingText.textContent = `${currentRating}/5`;
        });
    });

    if (sendReviewBtn) {
        sendReviewBtn.addEventListener('click', async () => {
            const reviewText = reviewInput.value.trim();
            if (reviewText === '' || currentRating === 0) {
                alert('Пожалуйста, напишите отзыв и выберите оценку.');
                return;
            }
            const user = getUser();
            const isAnonymous = anonymousCheckbox ? anonymousCheckbox.checked : false;
            const reviewData = {
                comment: reviewText,
                rating: currentRating.toString(),
                date: new Date().toLocaleDateString('ru-RU')
            };
            if (user && user.id && !isAnonymous) {
                reviewData.client = { id: user.id };
            }
            try {
                const response = await fetch(`${API_BASE_URL}/reviews`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(reviewData)
                });
                if (response.ok) {
                    alert('Спасибо! Ваш отзыв отправлен на модерацию и появится после проверки.');
                    reviewInput.value = '';
                    currentRating = 0;
                    editableStars.forEach(s => s.classList.remove('filled'));
                    editableRatingText.textContent = '0/5';
                    if (anonymousCheckbox) anonymousCheckbox.checked = false;
                } else {
                    const errorText = await response.text();
                    alert('Ошибка при отправке отзыва: ' + errorText);
                }
            } catch (error) {
                console.error('Error submitting review:', error);
                alert('Ошибка при отправке отзыва');
            }
        });
    }
});