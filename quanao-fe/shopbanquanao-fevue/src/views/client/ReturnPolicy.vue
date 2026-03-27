<template>
  <div class="return-policy-page container">
    <div class="policy-content">
      <h1 class="page-title">CHÍNH SÁCH ĐỔI TRẢ</h1>
      
      <section class="policy-section">
        <h2>1. Điều kiện áp dụng</h2>
        <ul>
          <li>Sản phẩm phải có đầy đủ nhãn mác, tag và hóa đơn <strong>Clothes Vnua</strong> từ cửa hàng, fanpage hoặc các nền tảng thương mại điện tử như Shopee và Tiktokshop.</li>
          <li>Sản phẩm phải chưa được sử dụng, giặt hoặc sấy khô và không có mùi, vết bẩn, vết rách, v.v. (trừ trường hợp lỗi của nhà sản xuất).</li>
          <li>Mỗi đơn hàng chỉ được đổi một lần.</li>
        </ul>
      </section>

      <section class="policy-section">
        <h2>2. Điều kiện phí vận chuyển</h2>
        <p><strong>MIỄN PHÍ VẬN CHUYỂN</strong> cho đơn hàng có giá trị trên 800.000 VND.</p>
        
        <p>Trong trường hợp trả lại hoặc đổi hàng, <strong>Clothes Vnua</strong> sẽ chịu 100% phí vận chuyển nếu:</p>
        <ul>
          <li>Sản phẩm bị lỗi do lỗi của nhà sản xuất (có video/bằng chứng mở hộp).</li>
          <li>Sản phẩm nhận được không phải là sản phẩm đã đặt hàng hoặc không giống với hình ảnh Clothes Vnua đăng.</li>
        </ul>

        <p><strong>Clothes Vnua</strong> không chi trả phí vận chuyển trong các trường hợp sau:</p>
        <ul>
          <li>Nếu khách hàng muốn đổi sang sản phẩm/kích thước khác (sản phẩm có giá trị thấp hơn sản phẩm ban đầu sẽ không được hoàn lại tiền).</li>
        </ul>
      </section>

      <section class="policy-section">
        <h2>3. Ghi chú</h2>
        <ul>
          <li><strong>Clothes Vnua</strong> không chấp nhận trả lại hoặc đổi sản phẩm tham gia chương trình khuyến mãi giảm giá trên 30%.</li>
          <li>Nếu khách hàng muốn đổi sang sản phẩm khác, Clothes Vnua khuyến khích khách hàng chọn sản phẩm có giá trị tương đương vì Clothes Vnua không hoàn lại tiền chênh lệch nếu sản phẩm mới có giá trị thấp hơn. Nếu sản phẩm có giá trị cao hơn, khách hàng chỉ cần trả phần chênh lệch giá.</li>
        </ul>
      </section>

      <section class="policy-section">
        <h2>4. Thời gian áp dụng</h2>
        <p>Đổi trả hàng trong vòng <strong>7 ngày</strong> kể từ ngày nhận sản phẩm, áp dụng cho tất cả đơn hàng trực tuyến và ngoại tuyến trên toàn quốc.</p>
      </section>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oswald:wght@400;700&family=Roboto:wght@400;500;700&display=swap');

.return-policy-page {
  padding-top: 120px;
  padding-bottom: 80px;
  max-width: 900px;
  margin: 0 auto;
  font-family: 'Roboto', sans-serif;
  color: #333;
}

.page-title {
  font-family: 'Oswald', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  text-align: center;
  margin-bottom: 40px;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #111827;
}

.policy-section {
  margin-bottom: 30px;
  background: #fff;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  border: 1px solid #e5e7eb;
}

.policy-section h2 {
  font-size: 1.3rem;
  font-weight: 700;
  margin-bottom: 15px;
  color: #111827;
  border-left: 4px solid #d0021b;
  padding-left: 12px;
}

.policy-section ul {
  padding-left: 20px;
  margin-bottom: 15px;
}

.policy-section li {
  margin-bottom: 10px;
  line-height: 1.6;
}

.policy-section p {
  line-height: 1.6;
  margin-bottom: 15px;
}

@media (max-width: 768px) {
  .return-policy-page {
    padding-top: 100px;
    padding-left: 15px;
    padding-right: 15px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .policy-section {
    padding: 20px;
  }
}
</style>