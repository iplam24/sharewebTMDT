import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import './assets/main.css'

import { useAuthStore } from '@/stores/authStore'  

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)


const authStore = useAuthStore()
authStore.init().finally(() => {
  app.mount('#app')
})
