<template>
  <div class="about-page container">
    <div class="content-wrapper">
      <h2 class="page-title">Giới Thiệu Clothes VNUA</h2>

      <p class="intro-text">
          <span class="highlight">Clothes VNUA</span> là thương hiệu thời trang nam giới trẻ thành lập năm <strong>2025</strong>, mang phong cách đường phố (street style) châu Âu lịch lãm, thời thượng và cá tính. Thương hiệu này được biết đến với các thiết kế độc đáo, sản xuất bằng công nghệ hiện đại, và thường cho ra mắt các bộ sưu tập giới hạn để tôn vinh cá tính riêng của khách hàng.
      </p>

      <div class="section-block">
        <h3>Thông tin chi tiết</h3>
        <ul class="styled-list">
            <li><i class="fa-solid fa-check"></i> <span><strong>Thành lập:</strong> Năm 2025.</span></li>
            <li><i class="fa-solid fa-check"></i> <span><strong>Phong cách:</strong> Lấy cảm hứng từ đường phố châu Âu, lịch lãm, thời thượng và cá tính.</span></li>
        </ul>
      </div>

      <div class="section-block">
        <h3>Đặc điểm nổi bật</h3>
        <ul class="styled-list">
            <li><i class="fa-solid fa-star"></i> <span>Sản xuất các sản phẩm thời trang đường phố.</span></li>
            <li><i class="fa-solid fa-star"></i> <span>Sử dụng công nghệ AR hiện đại bậc nhất Việt Nam.</span></li>
            <li><i class="fa-solid fa-star"></i> <span>Thường xuyên ra mắt các bộ sưu tập giới hạn (limited) và các bộ sưu tập theo mùa, mỗi bộ sưu tập ẩn chứa một câu chuyện riêng.</span></li>
        </ul>
      </div>

      <div class="section-block">
        <h3>Đánh giá</h3>
        <p>
            <span class="highlight">Clothes VNUA</span> được đánh giá là một trong những thương hiệu thời trang giữ gìn và phát huy vẻ đẹp của phong cách street style tại Việt Nam.
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oswald:wght@400;700&family=Roboto:wght@300;400;500;700&display=swap');

.about-page {
  padding-top: 120px;
  padding-bottom: 80px;
  font-family: 'Roboto', sans-serif;
  color: #333;
}

.content-wrapper {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.page-title {
  font-family: 'Oswald', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  text-transform: uppercase;
  text-align: center;
  margin-bottom: 30px;
  color: #111827;
  letter-spacing: 1px;
}

.intro-text {
  font-size: 1.1rem;
  line-height: 1.8;
  margin-bottom: 40px;
  text-align: justify;
}

.highlight {
  color: #d0021b;
  font-weight: 700;
}

.section-block {
  margin-bottom: 30px;
}

.section-block h3 {
  font-family: 'Oswald', sans-serif;
  font-size: 1.5rem;
  margin-bottom: 15px;
  border-left: 4px solid #d0021b;
  padding-left: 12px;
  color: #111827;
}

.styled-list {
  list-style: none;
  padding: 0;
}

.styled-list li {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 1rem;
  line-height: 1.6;
}

.styled-list li i {
  color: #d0021b;
  margin-top: 5px;
  font-size: 0.9rem;
}
</style>