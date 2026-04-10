/**
 * SmartFight Main JavaScript
 * UFC/MMA Web Template
 */

'use strict';

/* ============================================================
   COUNTDOWN TIMER
   Reads data-event-date="YYYY-MM-DD" from .sf-countdown elements
   ============================================================ */

function initCountdowns() {
  const countdowns = document.querySelectorAll('[data-event-date]');

  countdowns.forEach(function (el) {
    const targetDateStr = el.getAttribute('data-event-date');
    if (!targetDateStr) return;

    const targetDate = new Date(targetDateStr + 'T20:00:00').getTime();

    const daysEl    = el.querySelector('[data-unit="days"]');
    const hoursEl   = el.querySelector('[data-unit="hours"]');
    const minutesEl = el.querySelector('[data-unit="minutes"]');
    const secondsEl = el.querySelector('[data-unit="seconds"]');

    function updateTimer() {
      const now  = Date.now();
      const diff = targetDate - now;

      if (diff <= 0) {
        if (daysEl)    daysEl.textContent    = '00';
        if (hoursEl)   hoursEl.textContent   = '00';
        if (minutesEl) minutesEl.textContent = '00';
        if (secondsEl) secondsEl.textContent = '00';

        // Show "LIVE NOW" if the element exists
        const liveTag = el.closest('[data-countdown-wrap]');
        if (liveTag) {
          const liveEl = liveTag.querySelector('.countdown-live-tag');
          if (liveEl) liveEl.style.display = 'flex';
        }
        return;
      }

      const days    = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours   = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((diff % (1000 * 60)) / 1000);

      if (daysEl)    daysEl.textContent    = String(days).padStart(2, '0');
      if (hoursEl)   hoursEl.textContent   = String(hours).padStart(2, '0');
      if (minutesEl) minutesEl.textContent = String(minutes).padStart(2, '0');
      if (secondsEl) secondsEl.textContent = String(seconds).padStart(2, '0');
    }

    updateTimer();
    const intervalId = setInterval(updateTimer, 1000);

    // Clean up on page unload
    window.addEventListener('beforeunload', function () {
      clearInterval(intervalId);
    });
  });
}

/* ============================================================
   NAVBAR SCROLL BEHAVIOR
   ============================================================ */

function initNavbar() {
  const navbar = document.querySelector('.sf-navbar');
  if (!navbar) return;

  function onScroll() {
    if (window.scrollY > 40) {
      navbar.classList.add('scrolled');
    } else {
      navbar.classList.remove('scrolled');
    }
  }

  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();
}

/* ============================================================
   ACTIVE NAV LINK
   Sets .active on nav link that matches current page
   ============================================================ */

function initActiveNav() {
  const currentPage = window.location.pathname.split('/').pop() || 'index.html';
  const navLinks = document.querySelectorAll('.sf-navbar .nav-link');

  navLinks.forEach(function (link) {
    const href = link.getAttribute('href');
    if (href && (href === currentPage || href === './' + currentPage)) {
      link.classList.add('active');
    }
  });
}

/* ============================================================
   TABS (Predictions page + others)
   ============================================================ */

function initTabs() {
  const tabBtns = document.querySelectorAll('[data-sf-tab]');
  const tabPanes = document.querySelectorAll('[data-sf-pane]');

  tabBtns.forEach(function (btn) {
    btn.addEventListener('click', function () {
      const target = btn.getAttribute('data-sf-tab');

      // Update buttons
      tabBtns.forEach(function (b) { b.classList.remove('active'); });
      btn.classList.add('active');

      // Update panes
      tabPanes.forEach(function (pane) {
        if (pane.getAttribute('data-sf-pane') === target) {
          pane.classList.remove('d-none');
          pane.classList.add('animate-in');
        } else {
          pane.classList.add('d-none');
          pane.classList.remove('animate-in');
        }
      });
    });
  });
}

/* ============================================================
   PREDICTION PICK BUTTONS
   ============================================================ */

function initPredictions() {
  const predCards = document.querySelectorAll('.prediction-card');

  predCards.forEach(function (card) {
    const pickBtns = card.querySelectorAll('.pred-pick-btn');

    pickBtns.forEach(function (btn) {
      btn.addEventListener('click', function () {
        pickBtns.forEach(function (b) { b.classList.remove('selected'); });
        btn.classList.add('selected');

        // Show confirmation feedback
        const feedback = card.querySelector('.pred-feedback');
        if (feedback) {
          feedback.textContent = 'Pick saved: ' + btn.textContent.trim();
          feedback.style.display = 'block';
          setTimeout(function () {
            feedback.style.display = 'none';
          }, 2500);
        }
      });
    });
  });
}

/* ============================================================
   REACTION BUTTONS (Live Stream page)
   ============================================================ */

function initReactions() {
  const emojiBtns = document.querySelectorAll('.emoji-btn');

  emojiBtns.forEach(function (btn) {
    btn.addEventListener('click', function () {
      // Increment count
      const countEl = btn.querySelector('.emoji-count');
      if (countEl) {
        const current = parseInt(countEl.textContent, 10) || 0;
        countEl.textContent = current + 1;
      }

      // Animate
      btn.style.transform = 'scale(1.2)';
      setTimeout(function () {
        btn.style.transform = '';
      }, 180);

      // Add a reaction to the feed
      const emoji = btn.querySelector('.emoji-char') ? btn.querySelector('.emoji-char').textContent : btn.textContent.trim().charAt(0);
      appendReaction(emoji);
    });
  });

  function appendReaction(emoji) {
    const feed = document.querySelector('.reaction-feed');
    if (!feed) return;

    const names = ['MMAFan92', 'FightNight', 'OctagonKing', 'IronFist', 'TKOwatch', 'JabCross'];
    const texts = [
      'What a fight!!!',
      'Did NOT see that coming!',
      'Absolute banger!',
      'Best fight of the year!',
      'The crowd is going wild!',
      'Incredible performance!',
    ];

    const name = names[Math.floor(Math.random() * names.length)];
    const text = texts[Math.floor(Math.random() * texts.length)];

    const item = document.createElement('div');
    item.className = 'reaction-item animate-in';
    item.innerHTML = `
      <div class="reaction-emoji">${emoji}</div>
      <div style="flex:1">
        <div class="reaction-user">${name}</div>
        <div class="reaction-text">${text}</div>
      </div>
      <div class="reaction-time">just now</div>
    `;

    feed.insertBefore(item, feed.firstChild);

    // Trim to 20 items max
    while (feed.children.length > 20) {
      feed.removeChild(feed.lastChild);
    }
  }
}

/* ============================================================
   NOTIFICATION READ STATE
   ============================================================ */

function initNotifications() {
  const items = document.querySelectorAll('.notification-item');

  items.forEach(function (item) {
    item.addEventListener('click', function () {
      item.classList.remove('unread', 'unread-gold');
      const dot = item.querySelector('.notif-dot');
      if (dot) dot.style.display = 'none';
    });
  });

  const markAllBtn = document.querySelector('[data-action="mark-all-read"]');
  if (markAllBtn) {
    markAllBtn.addEventListener('click', function () {
      items.forEach(function (item) {
        item.classList.remove('unread', 'unread-gold');
        const dot = item.querySelector('.notif-dot');
        if (dot) dot.style.display = 'none';
      });

      const badge = document.querySelector('.notif-badge-count');
      if (badge) badge.textContent = '0';
    });
  }
}

/* ============================================================
   GALLERY LIGHTBOX (simple)
   ============================================================ */

function initGallery() {
  const galleryItems = document.querySelectorAll('.gallery-item');

  galleryItems.forEach(function (item) {
    item.addEventListener('click', function () {
      const img = item.querySelector('img');
      if (!img) return;

      // Create modal overlay
      const overlay = document.createElement('div');
      overlay.style.cssText = `
        position: fixed; inset: 0; z-index: 9999;
        background: rgba(0,0,0,0.92);
        display: flex; align-items: center; justify-content: center;
        cursor: zoom-out; padding: 20px;
      `;

      const modalImg = document.createElement('img');
      modalImg.src = img.src;
      modalImg.style.cssText = `
        max-width: 90vw; max-height: 85vh;
        object-fit: contain; border-radius: 8px;
        box-shadow: 0 20px 60px rgba(0,0,0,0.8);
      `;

      overlay.appendChild(modalImg);
      document.body.appendChild(overlay);
      document.body.style.overflow = 'hidden';

      overlay.addEventListener('click', function () {
        document.body.removeChild(overlay);
        document.body.style.overflow = '';
      });
    });
  });
}

/* ============================================================
   BOOKING - EVENT SELECTOR
   ============================================================ */

function initBooking() {
  const eventSelect = document.getElementById('booking-event-select');
  const eventDisplay = document.querySelector('.selected-event-display');

  if (eventSelect && eventDisplay) {
    eventSelect.addEventListener('change', function () {
      const selected = eventSelect.options[eventSelect.selectedIndex];
      if (selected.value) {
        eventDisplay.textContent = selected.text;
      }
    });
  }

  // Ticket quantity controls
  const qtyBtns = document.querySelectorAll('[data-qty-action]');
  qtyBtns.forEach(function (btn) {
    btn.addEventListener('click', function () {
      const action   = btn.getAttribute('data-qty-action');
      const targetId = btn.getAttribute('data-qty-target');
      const input    = document.getElementById(targetId);
      if (!input) return;

      let val = parseInt(input.value, 10) || 0;
      if (action === 'inc') val = Math.min(val + 1, 10);
      if (action === 'dec') val = Math.max(val - 1, 0);
      input.value = val;
      updateBookingTotal();
    });
  });

  function updateBookingTotal() {
    const prices = { standing: 49, regular: 149, vip: 299 };
    let total = 0;

    Object.keys(prices).forEach(function (tier) {
      const input = document.getElementById('qty-' + tier);
      if (input) {
        const qty = parseInt(input.value, 10) || 0;
        total += qty * prices[tier];
      }
    });

    const totalEl = document.querySelector('.booking-total');
    if (totalEl) totalEl.textContent = '$' + total.toFixed(2);
  }
}

/* ============================================================
   SMOOTH SCROLL for anchor links
   ============================================================ */

function initSmoothScroll() {
  document.querySelectorAll('a[href^="#"]').forEach(function (anchor) {
    anchor.addEventListener('click', function (e) {
      const targetId = anchor.getAttribute('href');
      if (targetId === '#') return;

      const target = document.querySelector(targetId);
      if (target) {
        e.preventDefault();
        target.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  });
}

/* ============================================================
   ANIMATE ON SCROLL (Intersection Observer)
   ============================================================ */

function initAnimateOnScroll() {
  if (!window.IntersectionObserver) return;

  const observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate-in');
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.1, rootMargin: '0px 0px -40px 0px' });

  document.querySelectorAll('.sf-card, .event-card, .fighter-card, .blog-card, .ticket-card').forEach(function (el) {
    observer.observe(el);
  });
}

/* ============================================================
   MOBILE NAV: close collapse on link click
   ============================================================ */

function initMobileNav() {
  const navCollapse = document.querySelector('#sfNavCollapse');
  if (!navCollapse) return;

  const navLinks = navCollapse.querySelectorAll('.nav-link');
  navLinks.forEach(function (link) {
    link.addEventListener('click', function () {
      // Bootstrap 5 collapse
      if (window.bootstrap && bootstrap.Collapse) {
        const bsCollapse = bootstrap.Collapse.getInstance(navCollapse);
        if (bsCollapse) bsCollapse.hide();
      }
    });
  });
}

/* ============================================================
   INIT ALL
   ============================================================ */

document.addEventListener('DOMContentLoaded', function () {
  initNavbar();
  initActiveNav();
  initCountdowns();
  initTabs();
  initPredictions();
  initReactions();
  initNotifications();
  initGallery();
  initBooking();
  initSmoothScroll();
  initAnimateOnScroll();
  initMobileNav();
});
