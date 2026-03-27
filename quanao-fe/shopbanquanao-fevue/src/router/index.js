import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/authStore.js';

// Admin Components
import AdminLogin from '@/views/admin/Login.vue'; 
import Dashboard from '@/views/admin/Dashboard.vue';
import ProductList from '@/views/admin/ProductList.vue';
import ProductCreate from '@/views/admin/ProductCreate.vue';
import ProductEdit from '@/views/admin/ProductEdit.vue';
import OrderList from '@/views/admin/OrderList.vue';
import OrderDetail from '@/views/admin/OrderDetail.vue';
import AdminLayout from '@/layouts/AdminLayout.vue';
import CategoryList from '@/views/admin/CategoryList.vue';
import UserManagement from '@/views/admin/UserManagement.vue'; 
import DiscountManagement from '@/views/admin/DiscountManagement.vue';
import NotificationList from '@/views/admin/NotificationList.vue';


// Client Components
import ClientLayout from '@/layouts/ClientLayout.vue';
import HomeView from '@/views/client/HomeView.vue';
import ClientLogin from '@/views/client/Login.vue'; 
import ShopView from '@/views/client/ShopView.vue'; 
import ProductDetail from '@/views/client/ProductDetail.vue';
import CartView from '@/views/client/CartView.vue';
import CheckoutView from '@/views/client/CheckoutView.vue';
import OrderHistory from '@/views/client/OrderHistory.vue';
import PaymentSuccess from '@/views/client/PaymentSuccess.vue';
import WishlistView from '@/views/client/WishlistView.vue';
import ProductReviewView from '@/views/client/ProductReviewView.vue';
import UserProfile from '@/views/client/UserProfile.vue';
import AboutWeb from '@/views/client/AboutWeb.vue';
import DiscountList from '@/views/client/DiscountList.vue';
import ReturnPolicy from '../views/client/ReturnPolicy.vue';


const routes = [
    // =========================================
    // 1. KHU VỰC KHÁCH HÀNG
    // =========================================
    {
        path: '/',
        component: ClientLayout,
        children: [
            { path: '', name: 'Home', component: HomeView },
            { path: 'cart', name: 'Cart', component: CartView },
            { path: 'shop', name: 'Shop', component: ShopView },
            { path: 'product/:id', name: 'ProductDetail', component: ProductDetail },
            {
                path: 'my-orders',
                name: 'OrderHistory',
                component: OrderHistory,
                meta: { requiresAuth: true } 
            },
            
            { 
                path: 'checkout', 
                name: 'Checkout', 
                component: CheckoutView,
                meta: { requiresAuth: true } 
            },

              { 
                path: 'payment-success', 
                name: 'PaymentSuccess', 
                component: PaymentSuccess,
                meta: { requiresAuth: true } 
            },
            {
                path: '/wishlist',
                name: 'Wishlist',
                component: WishlistView,
            },
            {
                path: '/product/:id/review',
                name: 'ProductReview',
                component: ProductReviewView,
            },   {
                path: '/profile',
                name: 'UserProfile',
                component: UserProfile,
            },    
            
            {
                path: '/about',
                name: 'AboutWeb',
                component: AboutWeb,
            },
            {
                path: '/discounts',
                name: 'DiscountList',
                component: DiscountList,
            },   {
                path: '/return-policy',
                name: 'ReturnPolicy',
                component: ReturnPolicy,
            },
        
            // Trang Đăng nhập cho Khách hàng
            { 
                path: 'login', name: 'ClientLogin', component: ClientLogin
             }, 
        ],
    },

    // =========================================
    // 2. KHU VỰC ADMIN
    // =========================================
    
    // Trang Đăng nhập riêng cho Admin (Nằm ngoài AdminLayout)
    {
        path: '/admin/login',
        name: 'AdminLogin',
        component: AdminLogin,
    },

    // Container cho các trang quản trị
    {
        path: '/admin',
        component: AdminLayout,
        // Chỉ cần đặt requiresAuth ở đây, role sẽ được kiểm tra ở từng route con
        meta: { requiresAuth: true }, 
        children: [
            { 
                path: '', 
                redirect: '/admin/dashboard',
                meta: { role: 'ADMIN' } 
            },
            { 
                path: 'dashboard', 
                name: 'Dashboard', 
                component: Dashboard,
                meta: { role: 'ADMIN' } 
            },
            { 
                path: 'products', 
                name: 'ProductList', 
                component: ProductList,
                meta: { role: 'ADMIN' } 
            },
            { 
                path: 'products/create', 
                name: 'ProductCreate', 
                component: ProductCreate,
                meta: { role: 'ADMIN' } 
            },
            { 
                path: 'products/edit/:id', 
                name: 'ProductEdit', 
                component: ProductEdit,
                meta: { role: 'ADMIN' } 
            },
            { 
                path: 'orders', 
                name: 'OrderList', 
                component: OrderList,
                meta: { role: 'ADMIN' } 
            },
            { 
                path: 'orders/:id', 
                name: 'OrderDetail', 
                component: OrderDetail,
                meta: { role: 'ADMIN' } 
            },
            { 
                path: 'categories', 
                name: 'CategoryList', 
                component: CategoryList,
                meta: { role: 'ADMIN' } 
            },
            // ĐÃ CẬP NHẬT: Route Quản lý Người dùng
            { 
                path: 'users', // path thống nhất
                name: 'UserManagement', // name thống nhất
                component: UserManagement, 
                meta: { role: 'ADMIN' } // Yêu cầu quyền ADMIN
            },
            { 
                path: 'discounts', 
                name: 'DiscountManagement', 
                component: DiscountManagement,
                meta: { role: 'ADMIN' } 
            },
            { 
                path: 'notifications', 
                name: 'NotificationList', 
                component: NotificationList,
                meta: { role: 'ADMIN' } 
            },
        ],
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  // ⭐ ĐẢM BẢO INIT XONG TRƯỚC
  if (!authStore.initialized) {
    await authStore.init();
  }

  // 1. Kiểm tra đăng nhập
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    if (to.path.startsWith('/admin')) {
      return next({ name: 'AdminLogin' });
    }
    return next({ name: 'ClientLogin' });
  }

  // 2. Kiểm tra quyền Admin
  if (to.meta.role && to.meta.role === 'ADMIN') {
    if (!authStore.isAdmin) {
      alert("Bạn không có quyền truy cập trang quản trị!");
      return next({ name: 'Home' });
    }
  }

  return next();
});

export default router;