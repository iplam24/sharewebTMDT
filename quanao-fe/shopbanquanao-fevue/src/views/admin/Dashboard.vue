<template>
  <div class="dashboard-page container-fluid">
    <h1 class="h3 mb-4 text-gray-800">Tổng quan Quản trị</h1>
    
    <div v-if="loadingRevenue || loadingTop" class="text-center py-5">
      <i class="fa-solid fa-spinner fa-spin fa-2x text-primary"></i>
      <p class="mt-2">Đang tải dữ liệu thống kê...</p>
    </div>

    <div v-else class="stats-cards-grid">
      <div class="stat-card">
        <div class="card-icon bg-success"><i class="fa-solid fa-money-bill-wave"></i></div>
        <div>
          <p class="card-title">Doanh thu hôm nay</p>
          <p class="card-value text-success">{{ formatPrice(revenueToday) }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="card-icon bg-success" style="filter: brightness(0.9)"><i class="fa-solid fa-sack-dollar"></i></div>
        <div>
          <p class="card-title">Tổng Doanh thu 30 ngày gần nhất</p>
          <p class="card-value text-success">{{ formatPrice(totalRevenue) }}</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="card-icon bg-primary"><i class="fa-solid fa-truck-fast"></i></div>
        <div>
          <p class="card-title">Tổng Đơn hàng đã giao</p>
          <!-- Đã sửa để hiển thị giá trị thực tế từ Backend -->
          <p class="card-value text-primary">{{ totalOrders }} Đơn</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="card-icon bg-warning"><i class="fa-solid fa-user-plus"></i></div>
        <div>
          <p class="card-title">User mới (7 ngày)</p>
          <p class="card-value text-warning">{{ newUsersCount }} Người</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="card-icon bg-info"><i class="fa-solid fa-boxes-stacked"></i></div>
        <div>
          <p class="card-title">Tổng tồn kho</p>
          <p class="card-value text-info">{{ totalStock }} SP</p>
        </div>
      </div>
    </div>
    
    <div v-if="!loadingRevenue && !loadingTop" class="row mt-5">
      <div class="col-md-7 mb-4">
        <div class="card shadow">
          <div class="card-header fw-bold d-flex justify-content-between align-items-center">
            <span>{{ chartTitle }}</span>
            <div class="d-flex gap-2">
              <select v-model="filterType" class="form-select form-select-sm" style="width: auto;" @change="handleFilterChange">
                <option value="30days">30 ngày qua</option>
                <option value="year">Theo năm</option>
              </select>
              <input v-if="filterType === 'year'" type="number" v-model="selectedYear" class="form-control form-control-sm" style="width: 80px;" @change="fetchRevenueData" min="2020" max="2030">
            </div>
          </div>
          <div class="card-body">
            <p v-if="dailyRevenue.length === 0" class="text-muted text-center">Không có dữ liệu đơn hàng đã giao.</p>
            <canvas v-else id="dailyRevenueChart"></canvas> 
          </div>
        </div>
      </div>

      <div class="col-md-5 mb-4">
        <div class="card shadow mb-4">
          <div class="card-header fw-bold">
            Top 5 Sản phẩm bán chạy nhất
          </div>
          <div class="card-body p-0">
            <p v-if="topSelling.length === 0" class="text-muted p-3">Chưa có dữ liệu bán hàng.</p>
            <ul v-else class="list-group list-group-flush">
              <li v-for="(product, index) in topSelling" :key="index" class="list-group-item d-flex justify-content-between align-items-center">
                <span class="fw-bold text-primary">{{ index + 1 }}.</span>
                <span>{{ product.productName }}</span>
                <span class="badge bg-danger rounded-pill">{{ product.totalSoldQuantity }} SP</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- Top Slowest Selling -->
        <div class="card shadow">
          <div class="card-header fw-bold text-danger">
            Top 5 Sản phẩm bán chậm nhất
          </div>
          <div class="card-body p-0">
            <p v-if="slowestSelling.length === 0" class="text-muted p-3">Chưa có dữ liệu.</p>
            <ul v-else class="list-group list-group-flush">
              <li v-for="(product, index) in slowestSelling" :key="index" class="list-group-item d-flex justify-content-between align-items-center">
                <span class="fw-bold text-secondary">{{ index + 1 }}.</span>
                <span>{{ product.productName }}</span>
                <span class="badge bg-warning text-dark rounded-pill">{{ product.totalSold }} SP</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import apiClient from '@/services/api.js';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables); // Đăng ký các thành phần biểu đồ

const dailyRevenue = ref([]);
const topSelling = ref([]);
const slowestSelling = ref([]);
const loadingRevenue = ref(true);
const loadingTop = ref(true);
const totalRevenue = ref(0);
const revenueToday = ref(0);
const totalOrders = ref(0); 
const newUsersCount = ref(0);
const totalStock = ref(0);

// Filter states
const filterType = ref('30days');
const selectedYear = ref(new Date().getFullYear());

const chartTitle = computed(() => {
  if (filterType.value === 'year') {
    return `Doanh thu năm ${selectedYear.value}`;
  }
  return 'Doanh thu 30 ngày gần nhất';
});

const formatPrice = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);

const fetchData = async () => {
    try {
        // 1. Lấy thống kê tổng quan (Dashboard Stats)
        const dashboardRes = await apiClient.get('/api/admin/stats/dashboard');
        const stats = dashboardRes.data;

        totalRevenue.value = stats.totalRevenue || 0;
        revenueToday.value = stats.revenueToday || 0;
        totalOrders.value = stats.deliveredOrders || 0;
        newUsersCount.value = stats.newUsersThisWeek || 0;
        totalStock.value = stats.totalStock || 0;

        // 2. Lấy biểu đồ doanh thu
        await fetchRevenueData();

        // 3. Lấy Top sản phẩm bán chạy
        const topRes = await apiClient.get('/api/admin/stats/top-products?limit=5');
        // API trả về List<Object[]> -> [name, quantity]
        if (topRes.data && Array.isArray(topRes.data)) {
            topSelling.value = topRes.data.map(item => ({
                productName: item[0],
                totalSoldQuantity: item[1]
            }));
        }

        // 4. Lấy Top sản phẩm bán chậm
        const slowestRes = await apiClient.get('/api/admin/stats/slowest-selling-products?limit=5');
        slowestSelling.value = slowestRes.data || [];
    } catch (err) {
        console.error("Lỗi tải dữ liệu Dashboard:", err);
    } finally {
        loadingRevenue.value = false;
        loadingTop.value = false;
        // Vẽ lại biểu đồ sau khi toàn bộ dữ liệu đã tải và DOM hiển thị
        await nextTick();
        drawChart();
    }
};

const fetchRevenueData = async () => {
    loadingRevenue.value = true;
    try {
        let url = '';
        
        if (filterType.value === 'year') {
            url = `/api/admin/stats/monthly-revenue?year=${selectedYear.value}`;
        } else {
            url = `/api/admin/stats/revenue?days=30`;
        }

        const revenueRes = await apiClient.get(url);
        dailyRevenue.value = revenueRes.data || [];
        
        // Tắt loading để DOM hiển thị lại thẻ canvas
        loadingRevenue.value = false;
        // Chờ DOM cập nhật xong mới vẽ biểu đồ
        await nextTick();
        drawChart();
    } catch (err) {
        console.error("Lỗi tải biểu đồ doanh thu:", err);
        loadingRevenue.value = false;
    }
};

const handleFilterChange = () => {
    fetchRevenueData();
};

let chartInstance = null;

const drawChart = () => {
    const ctx = document.getElementById('dailyRevenueChart');
    if (!ctx) return;
    
    if (chartInstance) chartInstance.destroy();

    const labels = dailyRevenue.value.map(item => item.date);
    const data = dailyRevenue.value.map(item => item.revenue);

    chartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Doanh thu (VNĐ)',
                data: data,
                backgroundColor: 'rgba(0, 123, 255, 0.6)',
                borderColor: 'rgba(0, 123, 255, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value, index, ticks) {
                            return formatPrice(value); // Định dạng tiền tệ cho trục Y
                        }
                    }
                }
            }
        }
    });
};

onMounted(() => {
    fetchData();
});
</script>

<style scoped>
.dashboard-page {
  font-family: "Segoe UI", system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Tiêu đề trang */
.dashboard-page h1 {
  font-weight: 700;
  letter-spacing: 0.5px;
  margin-bottom: 24px !important;
  color: #111827;
}

/* Loading */
.dashboard-page .text-center p {
  color: #6b7280;
  font-size: 0.95rem;
}

/* ====== STAT CARDS (TỔNG QUAN) ====== */
.stats-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 18px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  border: 1px solid #f3f4f6;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 35px rgba(15, 23, 42, 0.12);
  border-color: #e5e7eb;
}

.card-icon {
  width: 52px;
  height: 52px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 1.4rem;
}

/* Màu icon (ghi đè nhẹ để đẹp hơn) */
.bg-success {
  background: linear-gradient(135deg, #22c55e, #16a34a) !important;
}
.bg-primary {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8) !important;
}
.bg-warning {
  background: linear-gradient(135deg, #facc15, #eab308) !important;
}
.bg-info {
  background: linear-gradient(135deg, #06b6d4, #0891b2) !important;
}

.card-title {
  font-size: 0.8rem;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 4px;
}

.card-value {
  font-size: 1.6rem;
  font-weight: 700;
  line-height: 1.2;
}

.card-value.text-success {
  color: #16a34a !important;
}
.card-value.text-primary {
  color: #2563eb !important;
}
.card-value.text-warning {
  color: #eab308 !important;
}
.card-value.text-info {
  color: #0891b2 !important;
}

/* ====== CARDS BIỂU ĐỒ & TOP SẢN PHẨM ====== */
.row.mt-5 .card {
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.card-header {
  font-weight: 600;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  color: #111827;
  font-size: 0.95rem;
}

/* Chart container */
.card-body {
  position: relative;
}

#dailyRevenueChart {
  max-height: 380px;
}

/* List top sản phẩm */
.list-group-item {
  font-size: 0.95rem;
  padding: 10px 16px;
  border-color: #f3f4f6;
}

.list-group-item span:nth-child(2) {
  flex: 1;
  margin: 0 8px;
}

/* Badge số lượng */
.badge.bg-danger {
  background-color: #ef4444 !important;
}

/* Text muted trong card */
.card .text-muted {
  font-size: 0.9rem;
}

/* Responsive */
@media (max-width: 768px) {
  .dashboard-page h1 {
    font-size: 1.1rem;
  }

  .stats-cards-grid {
    grid-template-columns: 1fr;
  }

  #dailyRevenueChart {
    max-height: 320px;
  }
}
</style>