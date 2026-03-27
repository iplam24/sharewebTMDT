<template>
  <div class="login-page-wrapper">
    <div class="login-container">
      
      <div class="login-image">
        <img src="https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=1000&auto=format&fit=crop" alt="Fashion Model" />
        <div class="image-overlay">
          <h3>CLOTHES VNUA</h3>
          <p>Refined Origins</p>
        </div>
      </div>

      <div class="login-form-section">
        
        <div v-if="mode === 'login'" class="form-content">
          <h2 class="form-title">ĐĂNG NHẬP</h2>
          <p class="form-subtitle">Chào mừng bạn quay trở lại.</p>

          <form @submit.prevent="handleLogin" autocomplete="on">
            <div class="input-group">
              <label><i class="fa-solid fa-user"></i> Tên đăng nhập</label>
              <input 
                key="login-username" 
                type="text" 
                name="username"
                id="login-username"
                autocomplete="username"
                v-model="loginForm.username" 
                required 
              />
            </div>
            
            <div class="input-group password-group">
              <label><i class="fa-solid fa-lock"></i> Mật khẩu</label>
              <input 
                key="login-password"
                :type="showLoginPass ? 'text' : 'password'" 
                name="password"
                id="login-password"
                autocomplete="current-password"
                v-model="loginForm.password" 
                required 
              />
              <i class="fa-solid toggle-password" :class="showLoginPass ? 'fa-eye-slash' : 'fa-eye'" @click="showLoginPass = !showLoginPass"></i>
            </div>

            <div class="options-row">
              <label class="custom-checkbox">
                <input type="checkbox" v-model="loginForm.rememberMe">
                <span class="checkmark"></span> Ghi nhớ tôi
              </label>
              <a href="#" @click.prevent="mode = 'forgot'" class="link-text">Quên mật khẩu?</a>
            </div>

            <div v-if="error" class="error-text"><i class="fa-solid fa-circle-exclamation"></i> {{ error }}</div>

            <button type="submit" class="submit-btn" :disabled="loading">
              <i v-if="loading" class="fa-solid fa-spinner fa-spin"></i>
              <span v-else>ĐĂNG NHẬP</span>
            </button>
          </form>

          <div class="switch-mode">
            Chưa có tài khoản? <a href="#" @click.prevent="mode = 'register'">Tạo tài khoản ngay <i class="fa-solid fa-arrow-right"></i></a>
          </div>
        </div>

        <div v-else-if="mode === 'register'" class="form-content">
          <h2 class="form-title">TẠO TÀI KHOẢN</h2>
          <p class="form-subtitle">Trở thành thành viên của CLOTHES.</p>

          <form @submit.prevent="handleRegister" autocomplete="off">
            <div class="input-group">
              <label><i class="fa-solid fa-user-plus"></i> Tên đăng nhập</label>
              <input 
                key="reg-username"
                type="text" 
                name="new-username"
                id="reg-username"
                autocomplete="off"
                v-model="registerForm.username" 
                required 
              />
            </div>
            <div class="input-group">
              <label><i class="fa-solid fa-envelope"></i> Email</label>
              <input 
                key="reg-email"
                type="email" 
                name="email"
                id="reg-email"
                autocomplete="email"
                v-model="registerForm.email" 
                required 
              />
            </div>
            
            <div class="input-group password-group">
              <label><i class="fa-solid fa-key"></i> Mật khẩu</label>
              <input 
                key="reg-password"
                :type="showRegPass ? 'text' : 'password'" 
                name="new-password"
                id="reg-password"
                autocomplete="new-password"
                v-model="registerForm.password" 
                required 
              />
              <i class="fa-solid toggle-password" :class="showRegPass ? 'fa-eye-slash' : 'fa-eye'" @click="showRegPass = !showRegPass"></i>
            </div>

            <div class="input-group">
              <label><i class="fa-solid fa-lock"></i> Nhập lại mật khẩu</label>
              <input 
                key="reg-confirm"
                type="password" 
                name="confirm-password"
                id="reg-confirm"
                autocomplete="new-password"
                v-model="registerForm.confirmPassword" 
                required 
              />
            </div>

            <div v-if="error" class="error-text"><i class="fa-solid fa-circle-exclamation"></i> {{ error }}</div>

            <button type="submit" class="submit-btn" :disabled="loading">
              <i v-if="loading" class="fa-solid fa-spinner fa-spin"></i>
              <span v-else>ĐĂNG KÝ</span>
            </button>
          </form>

          <div class="switch-mode">
            Đã có tài khoản? <a href="#" @click.prevent="mode = 'login'"><i class="fa-solid fa-arrow-left"></i> Đăng nhập</a>
          </div>
        </div>

        <div v-else-if="mode === 'forgot'" class="form-content">
          <h2 class="form-title">KHÔI PHỤC</h2>
          <p class="form-subtitle">
            {{ forgotStep === 1 ? 'Nhập email để nhận mã xác thực.' : 'Nhập mã OTP và mật khẩu mới.' }}
          </p>

          <form v-if="forgotStep === 1" @submit.prevent="handleForgotStep1" autocomplete="off">
            <div class="input-group">
              <label><i class="fa-solid fa-at"></i> Email của bạn</label>
              <input 
                key="forgot-email"
                type="email" 
                name="forgot-email"
                id="forgot-email"
                autocomplete="off"
                v-model="forgotEmail" 
                required 
                placeholder="Ví dụ: admin@gmail.com" 
              />
            </div>
            
            <div v-if="error" class="error-text"><i class="fa-solid fa-circle-exclamation"></i> {{ error }}</div>

            <button type="submit" class="submit-btn" :disabled="loading">
              <i v-if="loading" class="fa-solid fa-spinner fa-spin"></i>
              <span v-else>GỬI MÃ OTP <i class="fa-solid fa-paper-plane"></i></span>
            </button>
          </form>

          <form v-else @submit.prevent="handleForgotStep2" autocomplete="off">
            <div class="input-group">
              <label><i class="fa-solid fa-envelope-open-text"></i> Mã OTP (6 số)</label>
              <input 
                key="reset-otp"
                type="text" 
                name="otp"
                id="reset-otp"
                autocomplete="off"
                v-model="resetForm.otp" 
                required 
                placeholder="Nhập mã trong email" 
              />
            </div>
            
            <div class="input-group password-group">
              <label><i class="fa-solid fa-key"></i> Mật khẩu mới</label>
              <input 
                key="reset-new-pass"
                :type="showRegPass ? 'text' : 'password'" 
                name="new-password"
                id="reset-new-pass"
                autocomplete="new-password"
                v-model="resetForm.newPassword" 
                required 
              />
              <i class="fa-solid toggle-password" :class="showRegPass ? 'fa-eye-slash' : 'fa-eye'" @click="showRegPass = !showRegPass"></i>
            </div>

            <div v-if="error" class="error-text"><i class="fa-solid fa-circle-exclamation"></i> {{ error }}</div>

            <button type="submit" class="submit-btn" :disabled="loading">
              <i v-if="loading" class="fa-solid fa-spinner fa-spin"></i>
              <span v-else>ĐỔI MẬT KHẨU</span>
            </button>
          </form>

          <div v-if="successMsg" class="success-text" style="margin-top:15px">
            <i class="fa-solid fa-circle-check"></i> {{ successMsg }}
          </div>

          <div class="switch-mode">
            <a href="#" @click.prevent="mode = 'login'"><i class="fa-solid fa-arrow-left"></i> Quay lại Đăng nhập</a>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>

import { ref } from 'vue';
import { useAuthStore } from '@/stores/authStore.js';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const mode = ref('login');
const forgotStep = ref(1);
const loading = ref(false);
const error = ref('');
const successMsg = ref('');
const showLoginPass = ref(false);
const showRegPass = ref(false);

const loginForm = ref({ username: '', password: '', rememberMe: false });
const registerForm = ref({ username: '', email: '', password: '', confirmPassword: '' });
const forgotEmail = ref('');
const resetForm = ref({ otp: '', newPassword: '' });

const handleLogin = async () => {
  await authStore.login(loginForm.value, loginForm.value.rememberMe); 
  loading.value = true; error.value = ''; successMsg.value = '';
  try {
    await authStore.login(loginForm.value, loginForm.value.rememberMe);
  } catch (err) {
    error.value = "Tên đăng nhập hoặc mật khẩu không đúng.";
  } finally { loading.value = false; }
};

const handleRegister = async () => {
  error.value = ''; successMsg.value = '';
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    error.value = "Mật khẩu nhập lại không khớp!"; return;
  }
  if (registerForm.value.password.length < 6) { error.value = "Mật khẩu phải từ 6 ký tự trở lên!"; return; }
  loading.value = true;
  try {
    await authStore.register(registerForm.value);
    successMsg.value = "Đăng ký thành công! Vui lòng đăng nhập.";
    mode.value = 'login'; loginForm.value.username = registerForm.value.username;
    registerForm.value = { username: '', email: '', password: '', confirmPassword: '' };
  } catch (err) {
    error.value = err.response?.data?.message || "Đăng ký thất bại.";
  } finally { loading.value = false; }
};

const handleForgotStep1 = async () => {
  loading.value = true; error.value = ''; successMsg.value = '';
  try {
    await authStore.forgotPassword(forgotEmail.value);
    successMsg.value = "Mã OTP đã được gửi! Vui lòng kiểm tra email.";
    forgotStep.value = 2;
  } catch (err) {
    error.value = err.response?.data?.message || "Email không tồn tại.";
  } finally { loading.value = false; }
};

const handleForgotStep2 = async () => {
  loading.value = true; error.value = ''; successMsg.value = '';
  try {
    await authStore.resetPassword({
      email: forgotEmail.value,
      otp: resetForm.value.otp,
      newPassword: resetForm.value.newPassword
    });
    alert("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
    mode.value = 'login'; forgotStep.value = 1; forgotEmail.value = '';
    resetForm.value = { otp: '', newPassword: '' };
  } catch (err) {
    error.value = err.response?.data?.message || "Mã OTP sai hoặc hết hạn.";
  } finally { loading.value = false; }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oswald:wght@400;700&family=Inter:wght@300;400;500;600;700&display=swap');

/* =========================
   NỀN & KHUNG LOGIN
   ========================= */
.login-page-wrapper {
  min-height: calc(100vh - 80px);
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(circle at top left, #f1f1f5, #e9eaee 40%, #f9fafb 80%);
  font-family: "Inter", system-ui, sans-serif;
}

.login-container {
  width: 100%;
  max-width: 1050px;
  min-height: 580px;
  background: #ffffff;
  border-radius: 18px;
  overflow: hidden;
  display: flex;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
  animation: loginPop .45s ease;
}

@keyframes loginPop {
  from { opacity: 0; transform: translateY(25px) scale(.98); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

/* =========================
   CỘT ẢNH BÊN TRÁI
   ========================= */
.login-image {
  flex: 1.1;
  position: relative;
  display: none; /* sẽ bật ở >=768px */
}

.login-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(.7) contrast(1.05);
  transform: scale(1.03);
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(0,0,0,0.55),
    rgba(0,0,0,0.15) 60%,
    transparent
  );
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 40px 40px 48px;
  color: #fff;
}

.image-overlay h3 {
  font-family: "Oswald", sans-serif;
  font-size: 2.4rem;
  letter-spacing: .2em;
  text-transform: uppercase;
  margin-bottom: 4px;
}

.image-overlay p {
  font-size: .9rem;
  text-transform: uppercase;
  letter-spacing: .16em;
  opacity: .95;
}

/* =========================
   FORM BÊN PHẢI
   ========================= */
.login-form-section {
  flex: 1;
  padding: 52px 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}

.form-content {
  width: 100%;
  max-width: 380px;
  animation: fadeUp .35s ease;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

.form-title {
  font-family: "Oswald", sans-serif;
  font-size: 2.3rem;
  letter-spacing: .1em;
  text-transform: uppercase;
  margin-bottom: 6px;
  color: #111827;
}

.form-subtitle {
  font-size: .95rem;
  color: #6b7280;
  margin-bottom: 28px;
}

/* =========================
   INPUTS
   ========================= */
.input-group {
  margin-bottom: 20px;
  position: relative;
}

.input-group label {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: .75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: .12em;
  color: #4b5563;
  margin-bottom: 6px;
}

.input-group label i {
  font-size: .8rem;
  color: #9ca3af;
}

.input-group input {
  width: 100%;
  padding: 11px 0;
  border: none;
  border-bottom: 1.8px solid #e5e7eb;
  background: transparent;
  font-size: .98rem;
  color: #111827;
  outline: none;
  transition: border-color .25s, background-color .25s;
}

.input-group input::placeholder {
  color: #9ca3af;
}

.input-group input:focus {
  border-bottom-color: #111827;
  background: #f9fafb;
}

/* icon mắt password */
.password-group {
  position: relative;
}

.toggle-password {
  position: absolute;
  right: 0;
  bottom: 11px;
  font-size: 1rem;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px;
  transition: color .2s, transform .15s;
}

.toggle-password:hover {
  color: #111827;
  transform: scale(1.05);
}

/* =========================
   CHECKBOX & LINK
   ========================= */
.options-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 18px 0 24px;
  font-size: .85rem;
}

.custom-checkbox {
  position: relative;
  padding-left: 24px;
  cursor: pointer;
  user-select: none;
  color: #4b5563;
}

.custom-checkbox input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.checkmark {
  position: absolute;
  left: 0;
  top: 0;
  width: 17px;
  height: 17px;
  border-radius: 4px;
  background: #e5e7eb;
  transition: background .2s, box-shadow .2s;
}

.custom-checkbox:hover .checkmark {
  background: #d1d5db;
}

.custom-checkbox input:checked ~ .checkmark {
  background: #111827;
  box-shadow: 0 0 0 1px #111827;
}

.checkmark:after {
  content: "";
  position: absolute;
  display: none;
}

.custom-checkbox input:checked ~ .checkmark:after {
  display: block;
}

.checkmark:after {
  left: 5px;
  top: 1px;
  width: 5px;
  height: 10px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.link-text {
  color: #4b5563;
  text-decoration: none;
  transition: color .2s;
}

.link-text:hover {
  color: #111827;
  text-decoration: underline;
}

/* =========================
   BUTTON
   ========================= */
.submit-btn {
  width: 100%;
  padding: 14px;
  border-radius: 999px;
  border: none;
  font-weight: 700;
  letter-spacing: .16em;
  text-transform: uppercase;
  font-size: .8rem;
  background: #111827;
  color: #fff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: background .25s, transform .1s, box-shadow .25s;
}

.submit-btn:hover:not(:disabled) {
  background: #020617;
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(15,23,42,.35);
}

.submit-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  box-shadow: none;
}

/* =========================
   ERROR / SUCCESS
   ========================= */
.error-text,
.success-text {
  margin-bottom: 16px;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: .85rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.error-text {
  background: #fef2f2;
  border: 1px solid #fca5a5;
  color: #b91c1c;
}

.success-text {
  background: #ecfdf3;
  border: 1px solid #bbf7d0;
  color: #15803d;
}

/* =========================
   SWITCH MODE
   ========================= */
.switch-mode {
  margin-top: 24px;
  text-align: center;
  font-size: .88rem;
  color: #6b7280;
}

.switch-mode a {
  color: #111827;
  font-weight: 600;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.switch-mode a:hover {
  text-decoration: underline;
}

/* =========================
   RESPONSIVE
   ========================= */
@media (min-width: 768px) {
  .login-image {
    display: block;
  }
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    max-width: 480px;
    min-height: auto;
  }
  .login-form-section {
    padding: 36px 26px 40px;
  }
}

@media (max-width: 480px) {
  .form-title {
    font-size: 2rem;
  }
  .login-page-wrapper {
    padding: 24px 12px;
  }
}
</style>
