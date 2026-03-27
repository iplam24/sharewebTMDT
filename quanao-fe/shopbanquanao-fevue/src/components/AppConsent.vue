<!-- src/components/AppConsent.vue -->
<template>
  <div v-if="show" class="app-consent-banner">
    <div class="consent-inner">
      <p class="consent-text">
        Chúng tôi sử dụng cookie để cải thiện trải nghiệm. 
        <a href="/privacy" @click.prevent="goToPrivacy" class="consent-link">Xem chính sách</a>
      </p>
      <div class="consent-buttons">
        <button @click="accept" class="btn-accept">Chấp nhận</button>
        <button @click="decline" class="btn-decline">Từ chối</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const show = ref(false);

onMounted(() => {
  if (!localStorage.getItem('user-consent')) {
    show.value = true;
  }
});

const accept = () => {
  localStorage.setItem('user-consent', 'accepted');
  show.value = false;
};

const decline = () => {
  localStorage.setItem('user-consent', 'declined');
  show.value = false;
};

const goToPrivacy = () => {
  show.value = false;
  router.push('/privacy');
};
</script>
<style scoped>
.app-consent-banner {
  position: fixed !important;
  inset: auto 0 16px 0 !important; /* bottom:16px + full width */
  display: flex;
  justify-content: center;
  pointer-events: none; /* để chỉ card bên trong nhận click */
  z-index: 99999 !important;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif !important;
  animation: slideUp 0.35s ease-out !important;
}

/* Card chính */
.consent-inner {
  pointer-events: auto;
  max-width: 960px;
  width: min(960px, 95vw);
  margin: 0 auto;
  padding: 14px 18px;
  border-radius: 16px;
  background: radial-gradient(circle at top left, rgba(96, 165, 250, 0.18), transparent),
              rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(148, 163, 184, 0.35);
  box-shadow:
    0 18px 40px rgba(15, 23, 42, 0.85),
    0 0 0 1px rgba(15, 23, 42, 0.9);
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  column-gap: 20px;
  row-gap: 12px;
  align-items: center;
  color: #e5e7eb;
}

/* Text + link */
.consent-text {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.5;
  text-align: left;
}

.consent-text::before {
  content: "🍪 ";
  font-size: 1rem;
}

.consent-link {
  color: #60a5fa !important;
  font-weight: 500;
  text-decoration: underline;
  text-decoration-thickness: 1px;
  text-underline-offset: 3px;
  cursor: pointer;
}

.consent-link:hover {
  color: #bfdbfe !important;
}

/* Buttons */
.consent-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: nowrap;
  justify-content: flex-end;
}

.btn-accept,
.btn-decline {
  padding: 9px 18px !important;
  border-radius: 999px !important;
  font-weight: 600 !important;
  font-size: 0.78rem !important;
  letter-spacing: 0.06em;
  text-transform: uppercase !important;
  cursor: pointer !important;
  min-width: 120px !important;
  border: none;
  outline: none;
  transition: transform 0.12s ease-out, box-shadow 0.12s ease-out, background 0.15s ease-out, border-color 0.15s ease-out !important;
  white-space: nowrap;
}

.btn-accept {
  background: linear-gradient(135deg, #22c55e, #16a34a) !important;
  color: #f9fafb !important;
  box-shadow: 0 10px 25px rgba(22, 163, 74, 0.55);
}

.btn-accept:hover {
  background: linear-gradient(135deg, #16a34a, #15803d) !important;
  transform: translateY(-1px);
  box-shadow: 0 12px 30px rgba(21, 128, 61, 0.75);
}

.btn-decline {
  background: rgba(15, 23, 42, 0.8) !important;
  color: #e5e7eb !important;
  border: 1px solid rgba(148, 163, 184, 0.8) !important;
}

.btn-decline:hover {
  background: rgba(30, 41, 59, 0.95) !important;
  border-color: #e5e7eb !important;
  transform: translateY(-1px);
}

/* Active / focus state */
.btn-accept:active,
.btn-decline:active {
  transform: translateY(0);
  box-shadow: none;
}

/* Animation */
@keyframes slideUp {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* Mobile */
@media (max-width: 640px) {
  .app-consent-banner {
    inset: auto 0 8px 0 !important;
    padding: 0 8px;
  }

  .consent-inner {
    grid-template-columns: minmax(0, 1fr);
    row-gap: 10px;
    padding: 12px 12px;
    text-align: left;
  }

  .consent-text {
    font-size: 0.86rem;
  }

  .consent-buttons {
    width: 100%;
    justify-content: stretch;
  }

  .btn-accept,
  .btn-decline {
    flex: 1 1 auto;
    min-width: 0 !important;
    text-align: center;
  }
}
</style>
