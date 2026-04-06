# Cập nhật luồng nghiệp vụ Order & Thanh toán (Fast Checkout)

## Phân tích Chuyên sâu Vấn đề 1: Đơn "Mang Về" bị mất tích

### 1. Nguyên nhân Gốc rễ (Root Cause) trong Code hiện tại
Trong file `TablePanel.java`, khi bạn bấm vào nút "Bàn MANG VỀ", hệ thống đang được [Code cứng/Hardcode] luôn ép biến `DonHang dh = null;` trước khi gửi sang màn hình Order.
Hệ quả là:
1. Bạn gọi món xong -> bấm `[Lưu Bếp]`. CSDL (Database) sẽ tạo 1 phiếu Order mới dạng `MANG_VE` và lưu thành công.
2. Giao diện đá văng bạn ra ngoài màn hình Sơ Đồ Bàn.
3. Lát sau khách ra lấy nước, bạn hớt hải **Bấm lại vào cái nút "MANG VỀ" đó để tìm đơn**. Nhưng vì Code ép `dh = null`, hệ thống lại bưng ra cho bạn một cái Màn hình Order trống trơn y như mới! Toàn bộ các đơn "Mang Về" cũ bị kẹt cứng dưới DB ở trạng thái Đang Phục Vụ mà không có Cửa nào trên UI để lôi nó lên Thanh toán.

### 2. Thiết kế Kiến Trúc Luồng Mới (Mô phỏng UI)
Thay vì ép `null`, chúng ta sẽ phá vỡ liên kết 1:1 của Bàn Mang Về. Nút "MANG VỀ" sẽ đóng vai trò như một Thư Mục chứa các Đơn. Khi bấm vào:

```text
 ┌────────────────────────────────────────────────────────┐
 │ 🛍  DANH SÁCH CÁC ĐƠN MANG VỀ ĐANG CHỜ (HIỆN CÓ: 3)    │
 ├────────────────────────────────────────────────────────┤
 │                                                        │
 │ ◉ [DH-0010] - Tạo lúc 14:15 - Tổng: 65.000đ            │
 │     Trà đào cam sả (L), Cà phê đen                     │
 │ ------------------------------------------------------ │
 │ ◉ [DH-0015] - Tạo lúc 14:22 - Tổng: 40.000đ            │
 │     Bạc xỉu đá (M)                                     │
 │ ------------------------------------------------------ │
 │ ◉ [DH-0022] - Tạo lúc 14:30 - Tổng: 120.000đ           │
 │     Trà chanh dây x 3                                  │
 │                                                        │
 ├────────────────────────────────────────────────────────┤
 │  [ ➕ TẠO ĐƠN MANG VỀ MỚI ]           [ ❌ Hủy Chọn]  │
 └────────────────────────────────────────────────────────┘
```

**Kịch bản thao tác:**
* **Khách mới 100% bước vào:** Nhân viên bấm vào ô xanh "MANG VỀ", hộp thoại trên hiện lên, nhân viên bấm luôn cái nút to ở dưới **[+ TẠO ĐƠN MANG VỀ MỚI]**. Màn hình Order mở ra, chọn thức uống cho khách.
* **Khách đang đợi lấy nước:** Nhân viên làm nước xong, gọi to "Mời khách số 22". Nhân viên bấm lại vào "MANG VỀ", chọn dòng `[DH-0022]`. Màn hình Order của khách này sẽ hiển thị lại đủ Trà chanh dây, bấm liền mạch **[Thanh Toán]**! Quá ư là trơn tru.

### 3. Vấn đề 2: Tính năng "Fast Checkout" (Tích hợp Auto-Save)
> [!IMPORTANT]
> **Vẫn giữ nguyên nút [Lưu Vào Bếp]**. Nút này dùng để khách gọi xong ra bàn ngồi chơi, lát nữa thu tiền sau. 

**👉 Giải pháp Auto-Save cho nút [Thanh Toán]:**
Nếu khách trả luôn tiền mặt, thu ngân khỏi bấm Lưu Bếp. Bấm thẳng nút `[Thanh Toán]`:
- Code tự động mượn hàm `saveDonHang()` để phi ngay dữ liệu vào DB (bếp có bill).
- Ngay sau đó 0.1s, có mã ID rồi -> Code tự động lôi `PaymentDialog.java` ra quét QR / thu tiền thừa.
- Thay vì mất công Bấm Xong -> Bấm Thoát -> Tìm Bàn -> Mở Lại -> Chọn Tính Tiền... Giờ rút gọn về 1 nhịp click!

---
## User Review Required
Như vậy Phân tích Gốc rễ trên đã sát với sự băn khoăn của bạn (tại sao tôi bấm "Mang Về" lại kh thấy Đơn) rồi chứ? Nếu bạn duyệt quy trình của "Cỗ máy Bán Hàng" này, cho phép tôi Viết Code Update nhé!
