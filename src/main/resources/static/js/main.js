// ═══════════════════════════════════════════════
//   JOBSPARK — MAIN.JS
//   All animations and interactive features
// ═══════════════════════════════════════════════

// ── 1. LOADING SCREEN ───────────────────────────
function initLoadingScreen() {
  const screen = document.getElementById('loadingScreen');
  if (!screen) return;
  setTimeout(() => {
    screen.classList.add('hidden');
    setTimeout(() => screen.remove(), 600);
  }, 1800);
}

// ── 2. CUSTOM CURSOR ────────────────────────────
function initCursor() {
  const dot  = document.createElement('div');
  const ring = document.createElement('div');
  dot.className  = 'cursor-dot';
  ring.className = 'cursor-ring';
  document.body.appendChild(dot);
  document.body.appendChild(ring);

  let mouseX = 0, mouseY = 0;
  let ringX  = 0, ringY  = 0;

  document.addEventListener('mousemove', e => {
    mouseX = e.clientX;
    mouseY = e.clientY;
    dot.style.left = mouseX - 4  + 'px';
    dot.style.top  = mouseY - 4  + 'px';
  });

  // Smooth ring follow
  function animateRing() {
    ringX += (mouseX - ringX) * 0.12;
    ringY += (mouseY - ringY) * 0.12;
    ring.style.left = ringX - 18 + 'px';
    ring.style.top  = ringY - 18 + 'px';
    requestAnimationFrame(animateRing);
  }
  animateRing();

  // Hover effects
  document.querySelectorAll('a, button, .btn, .job-card, .category-card')
    .forEach(el => {
      el.addEventListener('mouseenter', () => {
        dot.style.transform  = 'scale(2)';
        ring.style.transform = 'scale(1.5)';
        ring.style.borderColor = 'rgba(236,72,153,0.6)';
      });
      el.addEventListener('mouseleave', () => {
        dot.style.transform  = 'scale(1)';
        ring.style.transform = 'scale(1)';
        ring.style.borderColor = 'rgba(124,58,237,0.5)';
      });
    });
}

// ── 3. NAVBAR SCROLL EFFECT ──────────────────────
function initNavbar() {
  const navbar = document.querySelector('.navbar');
  if (!navbar) return;
  window.addEventListener('scroll', () => {
    if (window.scrollY > 50) {
      navbar.classList.add('scrolled');
    } else {
      navbar.classList.remove('scrolled');
    }
  });
}

// ── 4. SCROLL ANIMATIONS (AOS) ───────────────────
function initScrollAnimations() {
  const elements = document.querySelectorAll(
    '.job-card, .feature-card, .category-card, .card, .stat-card'
  );

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry, index) => {
      if (entry.isIntersecting) {
        setTimeout(() => {
          entry.target.style.opacity   = '1';
          entry.target.style.transform = 'translateY(0)';
        }, index * 80);
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.1 });

  elements.forEach(el => {
    el.style.opacity   = '0';
    el.style.transform = 'translateY(30px)';
    el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
    observer.observe(el);
  });
}

// ── 5. COUNTER ANIMATION ─────────────────────────
function animateCounter(el, target, duration = 2000) {
  let start     = 0;
  const step    = target / (duration / 16);
  const timer   = setInterval(() => {
    start += step;
    if (start >= target) {
      el.textContent = target.toLocaleString();
      clearInterval(timer);
    } else {
      el.textContent = Math.floor(start).toLocaleString();
    }
  }, 16);
}

function initCounters() {
  // Hero stats counter
  const heroStats = document.querySelectorAll('.hero-stat h3');
  heroStats.forEach(el => {
    const text   = el.textContent;
    const num    = parseInt(text.replace(/[^0-9]/g, ''));
    const suffix = text.replace(/[0-9,]/g, '');
    if (!num) return;

    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          animateCounter(el, num, 2000);
          el.dataset.suffix = suffix;
          observer.unobserve(el);
        }
      });
    });
    observer.observe(el);
  });

  // Dashboard stat cards counter
  const statCards = document.querySelectorAll('.stat-card h3');
  statCards.forEach(el => {
    const num = parseInt(el.textContent);
    if (isNaN(num)) return;
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          animateCounter(el, num, 1500);
          observer.unobserve(el);
        }
      });
    });
    observer.observe(el);
  });
}

// ── 6. 3D TILT EFFECT ────────────────────────────
function initTiltEffect() {
  const cards = document.querySelectorAll('.job-card');
  cards.forEach(card => {
    card.addEventListener('mousemove', e => {
      const rect   = card.getBoundingClientRect();
      const x      = e.clientX - rect.left;
      const y      = e.clientY - rect.top;
      const centerX = rect.width  / 2;
      const centerY = rect.height / 2;
      const rotateX = (y - centerY) / 10;
      const rotateY = (centerX - x) / 10;

      card.style.transform =
        `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateZ(10px)`;
      card.style.transition = 'none';
    });

    card.addEventListener('mouseleave', () => {
      card.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) translateZ(0)';
      card.style.transition = 'all 0.5s ease';
    });
  });
}

// ── 7. BUTTON RIPPLE EFFECT ──────────────────────
function initRipple() {
  document.querySelectorAll('.btn').forEach(btn => {
    btn.addEventListener('click', function(e) {
      const ripple   = document.createElement('span');
      const rect     = this.getBoundingClientRect();
      const size     = Math.max(rect.width, rect.height);
      ripple.className = 'ripple';
      ripple.style.cssText = `
        width: ${size}px;
        height: ${size}px;
        left: ${e.clientX - rect.left - size/2}px;
        top: ${e.clientY - rect.top  - size/2}px;
      `;
      this.appendChild(ripple);
      setTimeout(() => ripple.remove(), 600);
    });
  });
}

// ── 8. TOAST NOTIFICATIONS ───────────────────────
function showToast(message, type = 'info', duration = 3000) {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    document.body.appendChild(container);
  }

  const icons = { success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️' };
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <span style="font-size:1.1rem;">${icons[type] || 'ℹ️'}</span>
    <span>${message}</span>
  `;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.animation = 'slideOutToast 0.4s ease forwards';
    setTimeout(() => toast.remove(), 400);
  }, duration);
}

// Auto show toasts for alerts
function initAlertToasts() {
  document.querySelectorAll('.alert-success').forEach(alert => {
    showToast(alert.textContent.trim(), 'success');
  });
  document.querySelectorAll('.alert-danger').forEach(alert => {
    showToast(alert.textContent.trim(), 'error');
  });
}

// ── 9. TYPEWRITER EFFECT ─────────────────────────
function initTypewriter() {
  const el = document.getElementById('typewriter');
  if (!el) return;

  const words = [
    'Dream Career',
    'Next Opportunity',
    'Perfect Job',
    'Bright Future'
  ];

  let wordIndex = 0;
  let charIndex = 0;
  let isDeleting = false;

  function type() {
    const current = words[wordIndex];

    if (isDeleting) {
      el.textContent = current.substring(0, charIndex - 1);
      charIndex--;
    } else {
      el.textContent = current.substring(0, charIndex + 1);
      charIndex++;
    }

    if (!isDeleting && charIndex === current.length) {
      setTimeout(() => { isDeleting = true; }, 1500);
    } else if (isDeleting && charIndex === 0) {
      isDeleting = false;
      wordIndex  = (wordIndex + 1) % words.length;
    }

    const speed = isDeleting ? 80 : 120;
    setTimeout(type, speed);
  }
  type();
}

// ── 10. CONFETTI ANIMATION ───────────────────────
function launchConfetti() {
  const canvas = document.createElement('canvas');
  canvas.id = 'confettiCanvas';
  document.body.appendChild(canvas);
  const ctx = canvas.getContext('2d');
  canvas.width  = window.innerWidth;
  canvas.height = window.innerHeight;

  const colors = [
    '#7C3AED', '#EC4899', '#10B981',
    '#F59E0B', '#3B82F6', '#EF4444'
  ];

  const pieces = Array.from({ length: 150 }, () => ({
    x:       Math.random() * canvas.width,
    y:       Math.random() * canvas.height - canvas.height,
    w:       Math.random() * 10 + 5,
    h:       Math.random() * 6  + 3,
    color:   colors[Math.floor(Math.random() * colors.length)],
    speed:   Math.random() * 4 + 2,
    angle:   Math.random() * 360,
    spin:    Math.random() * 10 - 5,
    opacity: Math.random() * 0.7 + 0.3
  }));

  let frame = 0;
  function draw() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    pieces.forEach(p => {
      ctx.save();
      ctx.globalAlpha = p.opacity;
      ctx.translate(p.x + p.w / 2, p.y + p.h / 2);
      ctx.rotate(p.angle * Math.PI / 180);
      ctx.fillStyle = p.color;
      ctx.fillRect(-p.w / 2, -p.h / 2, p.w, p.h);
      ctx.restore();
      p.y     += p.speed;
      p.angle += p.spin;
      if (p.y > canvas.height) {
        p.y = -10;
        p.x = Math.random() * canvas.width;
      }
    });
    frame++;
    if (frame < 180) {
      requestAnimationFrame(draw);
    } else {
      canvas.remove();
    }
  }
  draw();
}

// Check if student is shortlisted
function initConfetti() {
  const shortlistedCount = document.querySelector('.stat-card.green h3');
  if (shortlistedCount && parseInt(shortlistedCount.textContent) > 0) {
    const shown = sessionStorage.getItem('confettiShown');
    if (!shown) {
      setTimeout(() => {
        launchConfetti();
        showToast('🎉 Congratulations! You have been shortlisted!', 'success', 5000);
        sessionStorage.setItem('confettiShown', 'true');
      }, 2000);
    }
  }
}

// ── 11. PARTICLES BACKGROUND ─────────────────────
function initParticles() {
  const canvas = document.getElementById('particles-js');
  if (!canvas) return;

  // Use particles.js if available
  if (typeof particlesJS !== 'undefined') {
    particlesJS('particles-js', {
      particles: {
        number: { value: 80, density: { enable: true, value_area: 800 } },
        color:  { value: ['#7C3AED', '#EC4899', '#10B981'] },
        shape:  { type: 'circle' },
        opacity: { value: 0.5, random: true,
          anim: { enable: true, speed: 1, opacity_min: 0.1 } },
        size: { value: 3, random: true,
          anim: { enable: true, speed: 2, size_min: 0.3 } },
        line_linked: {
          enable: true, distance: 150,
          color: '#7C3AED', opacity: 0.2, width: 1
        },
        move: {
          enable: true, speed: 1.5,
          direction: 'none', random: true,
          straight: false, out_mode: 'out'
        }
      },
      interactivity: {
        detect_on: 'canvas',
        events: {
          onhover: { enable: true, mode: 'grab' },
          onclick: { enable: true, mode: 'push' }
        },
        modes: {
          grab:  { distance: 200, line_linked: { opacity: 0.8 } },
          push:  { particles_nb: 4 }
        }
      },
      retina_detect: true
    });
  } else {
    // Fallback simple canvas particles
    const ctx = canvas.getContext('2d');
    canvas.width  = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;

    const dots = Array.from({ length: 60 }, () => ({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      r: Math.random() * 2 + 1,
      dx: (Math.random() - 0.5) * 0.8,
      dy: (Math.random() - 0.5) * 0.8,
      color: ['rgba(124,58,237,0.6)',
              'rgba(236,72,153,0.6)',
              'rgba(16,185,129,0.6)']
              [Math.floor(Math.random() * 3)]
    }));

    function drawParticles() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      dots.forEach(d => {
        ctx.beginPath();
        ctx.arc(d.x, d.y, d.r, 0, Math.PI * 2);
        ctx.fillStyle = d.color;
        ctx.fill();
        d.x += d.dx;
        d.y += d.dy;
        if (d.x < 0 || d.x > canvas.width)  d.dx *= -1;
        if (d.y < 0 || d.y > canvas.height) d.dy *= -1;
      });

      // Draw connecting lines
      dots.forEach((d1, i) => {
        dots.slice(i + 1).forEach(d2 => {
          const dist = Math.hypot(d1.x - d2.x, d1.y - d2.y);
          if (dist < 120) {
            ctx.beginPath();
            ctx.moveTo(d1.x, d1.y);
            ctx.lineTo(d2.x, d2.y);
            ctx.strokeStyle = `rgba(124,58,237,${0.15 * (1 - dist/120)})`;
            ctx.lineWidth = 0.5;
            ctx.stroke();
          }
        });
      });
      requestAnimationFrame(drawParticles);
    }
    drawParticles();
  }
}

// ── 12. SKELETON LOADING ─────────────────────────
function showSkeletonLoading(container, count = 3) {
  if (!container) return;
  container.innerHTML = Array.from({ length: count }, () => `
    <div class="skeleton-card">
      <div style="display:flex; gap:10px; margin-bottom:12px;">
        <div class="skeleton skeleton-badge"></div>
        <div class="skeleton skeleton-badge"></div>
      </div>
      <div class="skeleton skeleton-title"></div>
      <div class="skeleton skeleton-line" style="width:40%;"></div>
      <div class="skeleton skeleton-line" style="width:80%; margin-top:14px;"></div>
      <div class="skeleton skeleton-line" style="width:60%;"></div>
    </div>
  `).join('');
}

// ── 13. SMOOTH PAGE TRANSITIONS ──────────────────
function initPageTransitions() {
  document.body.style.animation = 'fadeIn 0.4s ease forwards';

  document.querySelectorAll('a:not([href^="#"]):not([target="_blank"])')
    .forEach(link => {
      link.addEventListener('click', function(e) {
        const href = this.getAttribute('href');
        if (!href || href === '#' || href.startsWith('javascript')) return;
        e.preventDefault();
        document.body.style.animation = 'none';
        document.body.style.opacity   = '0';
        document.body.style.transition = 'opacity 0.3s ease';
        setTimeout(() => { window.location.href = href; }, 300);
      });
    });
}

// ── 14. LIVE CLOCK ───────────────────────────────
function initClock() {
  const clockEl = document.getElementById('liveClock');
  if (!clockEl) return;
  function updateClock() {
    const now = new Date();
    clockEl.textContent = now.toLocaleTimeString('en-IN', {
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
  }
  updateClock();
  setInterval(updateClock, 1000);
}

// ── 15. FLOATING BACK TO TOP ─────────────────────
function initBackToTop() {
  const btn = document.createElement('button');
  btn.innerHTML  = '↑';
  btn.id = 'backToTop';
  btn.style.cssText = `
    position: fixed;
    bottom: 2rem;
    right: 2rem;
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: linear-gradient(135deg, #7C3AED, #EC4899);
    color: white;
    border: none;
    font-size: 1.2rem;
    font-weight: 700;
    cursor: pointer;
    box-shadow: 0 4px 20px rgba(124,58,237,0.4);
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.3s ease;
    z-index: 999;
  `;
  document.body.appendChild(btn);

  window.addEventListener('scroll', () => {
    if (window.scrollY > 300) {
      btn.style.opacity   = '1';
      btn.style.transform = 'translateY(0)';
    } else {
      btn.style.opacity   = '0';
      btn.style.transform = 'translateY(20px)';
    }
  });

  btn.addEventListener('click', () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  });
}

// ═══════════════════════════════════════════════
//   INITIALIZE ALL FEATURES
// ═══════════════════════════════════════════════
document.addEventListener('DOMContentLoaded', () => {
  initLoadingScreen();
  initCursor();
  initNavbar();
  initScrollAnimations();
  initCounters();
  initTiltEffect();
  initRipple();
  initAlertToasts();
  initTypewriter();
  initConfetti();
  initParticles();
  initPageTransitions();
  initClock();
  initBackToTop();

  console.log('%c⚡ JobSpark UI Loaded!',
    'color: #7C3AED; font-size: 1.2rem; font-weight: bold;');
});

// Expose globally
window.showToast        = showToast;
window.launchConfetti   = launchConfetti;
window.showSkeletonLoading = showSkeletonLoading;