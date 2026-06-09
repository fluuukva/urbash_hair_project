document.addEventListener('DOMContentLoaded', function() {
  const menuToggle = document.getElementById('menu-toggle');
  const mainNav = document.getElementById('main-nav');
  
  if (!menuToggle || !mainNav) return;

  menuToggle.addEventListener('click', function(event) {
    event.stopPropagation();
    mainNav.classList.toggle('active');
  });

  document.addEventListener('click', function(event) {
    if (mainNav.classList.contains('active') && 
        !mainNav.contains(event.target) && 
        !menuToggle.contains(event.target)) {
      mainNav.classList.remove('active');
    }
  });

  const menuItemsWithChildren = mainNav.querySelectorAll('.menu-item-has-children > a');
  
  menuItemsWithChildren.forEach(item => {
    item.addEventListener('click', function(e) {
      e.preventDefault();
      e.stopPropagation();
      const parent = this.parentElement;
      const subMenu = parent.querySelector('.sub-menu');
      
      mainNav.querySelectorAll('.menu-item-has-children.active').forEach(el => {
        if (el !== parent) {
          el.classList.remove('active');
          const otherSub = el.querySelector('.sub-menu');
          if (otherSub) otherSub.classList.remove('open');
        }
      });
      
      parent.classList.toggle('active');
      if (subMenu) subMenu.classList.toggle('open');
    });
  });

  mainNav.querySelectorAll('a:not(.menu-item-has-children > a)').forEach(link => {
    link.addEventListener('click', function() {
      mainNav.classList.remove('active');
    });
  });
});