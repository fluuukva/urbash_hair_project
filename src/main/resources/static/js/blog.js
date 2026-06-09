document.addEventListener('DOMContentLoaded', function() {
    loadPosts();
});

async function loadPosts() {
    const blogGrid = document.getElementById('blog-posts');
    if (!blogGrid) return;

    try {
        const response = await fetch(`${API_BASE_URL}/posts`);
        if (!response.ok) throw new Error('Failed to fetch posts');
        
        const posts = await response.json();
        renderPosts(posts);
    } catch (error) {
        console.error('Error loading posts:', error);
        blogGrid.innerHTML = '<p style="text-align: center; color: #888;">Загрузка постов...</p>';
    }
}

function renderPosts(posts) {
    const blogGrid = document.getElementById('blog-posts');
    if (!blogGrid) return;

    blogGrid.innerHTML = posts.map(post => {
        let imageUrl = 'images/seat.jpg';
        if (post.image) {
            imageUrl = post.image;
        }
        return `
        <article class="blog-post">
            <div class="post-image-wrapper">
                <img src="${imageUrl}" alt="${post.title}" class="post-image" 
                     onerror="this.onerror=null; this.src='images/seat.jpg';">
            </div>
            <div class="post-content">
                <h2 class="post-title">${post.title}</h2>
                <p class="post-meta">${post.date}</p>
                <p class="post-excerpt">${post.description || ''}</p>
                <a href="#" class="btn-read-more" data-post-id="${post.id}">Читать далее</a>
            </div>
        </article>
    `}).join('');

    attachPostClickHandlers();
}

function attachPostClickHandlers() {
    const readMoreButtons = document.querySelectorAll('.btn-read-more');
    const modal = document.getElementById('modal-container');
    const closeButton = document.querySelector('.close-button');
    const modalTitle = document.getElementById('modal-title');
    const modalImage = document.getElementById('modal-image');
    const modalText = document.getElementById('modal-text');

    readMoreButtons.forEach(button => {
        button.addEventListener('click', async function(event) {
            event.preventDefault();
            const postId = this.dataset.postId;
            
            try {
                const response = await fetch(`${API_BASE_URL}/posts`);
                const posts = await response.json();
                const post = posts.find(p => p.id == postId);
                
                if (post) {
                    modalTitle.textContent = post.title;
                    
                    let imageUrl = 'images/seat.jpg';
                    if (post.image) {
                        imageUrl = post.image;
                    }
                    
                    modalImage.src = imageUrl;
                    modalImage.alt = post.title;
                    modalImage.onerror = function() { 
                        this.onerror = null; 
                        this.src = 'images/seat.jpg'; 
                    };
                    
                    modalText.innerHTML = `<p>${post.description || ''}</p>`;
                    
                    modal.classList.add('is-open');
                    document.body.style.overflow = 'hidden';
                }
            } catch (error) {
                console.error('Error loading post:', error);
            }
        });
    });

    if (closeButton) {
        closeButton.addEventListener('click', () => {
            modal.classList.remove('is-open');
            document.body.style.overflow = '';
        });
    }

    if (modal) {
        modal.addEventListener('click', function(event) {
            if (event.target === modal) {
                modal.classList.remove('is-open');
                document.body.style.overflow = '';
            }
        });
    }

    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape' && modal && modal.classList.contains('is-open')) {
            modal.classList.remove('is-open');
            document.body.style.overflow = '';
        }
    });
}