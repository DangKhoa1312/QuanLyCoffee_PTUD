# 📋 Gap Analysis — QuanLyCaFe_Final

> **Mục đích:** Tài liệu này liệt kê toàn bộ các chức năng còn thiếu trong hệ thống, đối chiếu trực tiếp giữa schema database (`cafe_database_final.sql`) và giao diện người dùng hiện có.
>
> **Cập nhật lần cuối:** 2026-04-12 | **Nhóm:** N03

---

## 1. Bảng Đối Chiếu Tổng Quan (20 Bảng DB)

| # | Bảng Database | Panel / Dialog có UI | Trạng thái |
|---|---|---|---|
| 1 | `KhuVuc` | `TableManagementPanel` | ✅ Đầy đủ |
| 2 | `Ban` | `TableManagementPanel` + `TablePanel` | ✅ Đầy đủ |
| 3 | `NhanVien` | `StaffManagementPanel` + `StaffDialog` | ✅ Đầy đủ |
| 4 | `CaLamViec` | Header info + `ShiftOpenDialog` + `ShiftCloseDialog` | ⚠️ Chỉ vận hành, không xem lịch sử |
| 5 | `Mon` | `MenuManagementPanel` + `MenuDialog` | ✅ Đầy đủ |
| 6 | `Size` | Nằm trong `MenuDialog` (quản lý cùng món) | ✅ Đầy đủ |
| 7 | `Topping` | ❌ Không có panel quản lý | ❌ **THIẾU HOÀN TOÀN** |
| 8 | `BangGia` | `PriceManagementPanel` + `PriceMasterDialog` | ✅ Đầy đủ |
| 9 | `BangGiaChiTiet` | `PriceDetailDialog` | ✅ Đầy đủ |
| 10 | `NguyenLieu` | `WarehouseManagementPanel` — Tab Nguyên Liệu | ✅ Đầy đủ |
| 11 | `DinhMucNguyenLieu` | ❌ Không có UI | ❌ **THIẾU HOÀN TOÀN** |
| 12 | `Kho` | Chỉ dùng làm dropdown filter, không CRUD | ⚠️ Chỉ đọc |
| 13 | `NhaCungCap` | `WarehouseManagementPanel` — Tab Nhà CC | ✅ Đầy đủ |
| 14 | `PhieuNhap` | `WarehouseManagementPanel` — Tab Phiếu Nhập | ✅ Đầy đủ |
| 15 | `ChiTietPhieuNhap` | `PhieuNhapDialog` | ✅ Đầy đủ |
| 16 | `TonKho` | `WarehouseManagementPanel` — Tab Tồn Kho | ✅ Đầy đủ |
| 17 | `HoaDon` | `InvoicePanel` | ⚠️ Thiếu bộ lọc & in ấn |
| 18 | `ChiTietHoaDon` | `InvoiceDetailDialog` | ⚠️ Chỉ xem, không lọc |
| 19 | `ChiTietHoaDonTopping` | Hiển thị trong `InvoiceDetailDialog` | ⚠️ Xem được, không lọc |
| 20 | `DatBan` | `createPlaceholderPanel("DAT_BAN")` | ❌ **CHƯA LÀM** |

---

## 2. Chi Tiết Các Lỗ Hổng Nghiêm Trọng

---

### ❌ LỖ HỔNG 1: Topping — Không Có Giao Diện Quản Lý

**Schema:**
```sql
CREATE TABLE Topping (
    maTopping  VARCHAR(20),
    tenTopping NVARCHAR(100),
    giaTopping DECIMAL(10,2) DEFAULT 0,
    trangThai  BIT DEFAULT 1  -- 1=đang cung cấp, 0=ngưng
);
```

**Hiện trạng:**
- `ToppingDAO` + `ToppingDAOImpl` đã có đầy đủ
- `MenuController.getToppingDangCungCap()` chỉ **đọc** để hiển thị khi nhân viên gọi món
- **Không có 1 màn hình nào** để admin thêm / sửa / xóa / bật-tắt topping

**Hệ quả thực tế:**
> Muốn thêm "Thạch Matcha" hoặc đổi giá "Trân Châu" → phải vào SQL Server gõ lệnh `INSERT/UPDATE` thủ công. **Không chuyên nghiệp.**

**Giải pháp đề xuất:**
- Thêm **Tab "Topping"** vào `MenuManagementPanel` (cùng nhóm Thực Đơn)
- Tính năng cần: Danh sách, Thêm mới, Sửa tên/giá, Bật/Tắt trạng thái

---

### ❌ LỖ HỔNG 2: DinhMucNguyenLieu — Công Thức Pha Chế Không Có UI

**Schema:**
```sql
CREATE TABLE DinhMucNguyenLieu (
    maDinhMuc VARCHAR(20),
    soLuong   DECIMAL(10,3),   -- VD: 0.030 kg = 30g
    maMon     VARCHAR(20),     -- FK -> Mon
    maNL      VARCHAR(20)      -- FK -> NguyenLieu
);
```

**Ý nghĩa nghiệp vụ:**
Bảng này định nghĩa **công thức pha chế** của từng món. Ví dụ:
- Cà Phê Sữa = 30ml Espresso + 20ml Sữa tươi + 10g Đường

**Hiện trạng:**
- `DinhMucNguyenLieuDAO` + `DinhMucNguyenLieuDAOImpl` đã có đầy đủ
- `InventoryController.checkTonKhoMoiMon()` **đọc bảng này** để kiểm tra có đủ nguyên liệu không
- **Nhưng không có UI để tạo/sửa công thức** → Hệ thống kiểm tra tồn kho nhưng không có dữ liệu để kiểm tra

**Hệ quả thực tế:**
> Tính năng cảnh báo "Hết hàng" (hiển thị trên màn hình gọi món) gần như **vô dụng** vì công thức rỗng. Admin không có cách nào nhập định mức nguyên liệu từ giao diện.

**Giải pháp đề xuất:**
- Thêm **Tab "Công Thức / Định Mức"** vào `WarehouseManagementPanel`
- Hoặc thêm **section "Nguyên Liệu Cần" vào `MenuDialog`** khi chỉnh sửa từng món
- Tính năng: Xem công thức của từng món, Thêm/Sửa/Xóa định mức nguyên liệu

---

### ❌ LỖ HỔNG 3: DatBan — Trang Trắng

**Schema:**
```sql
CREATE TABLE DatBan (
    maDatBan, tenKhach, soDienThoai, soLuongNguoi,
    trangThai  -- CHO_XAC_NHAN / DA_XAC_NHAN / DA_DEN / HET_HAN / DA_HUY
    thoiGianDen, thoiGianDat, maBan, maHD
);
```

**Hiện trạng:**
```java
// MainFrame.java dòng 249
contentPanel.add(createPlaceholderPanel("QUẢN LÝ ĐẶT BÀN"), "DAT_BAN");
```
Nhân viên bấm "Đặt Bàn" → chỉ thấy màn hình trắng với chữ _"Sẽ có trong bản cập nhật sau"_.

**Hệ quả:** `DatBanDAO`, `DatBanDAOImpl`, entity `DatBan`, enum `TrangThaiDatBan` đều **sẵn sàng 100%** nhưng không có giao diện để dùng.

**Giải pháp đề xuất:**
- Tạo `ReservationPanel.java` với danh sách đặt bàn phân theo trạng thái
- Thêm nút: Xác nhận / Hủy / Đánh dấu Đã Đến
- Hiển thị **badge số lượng** đặt bàn chờ xác nhận trực tiếp trên nút sidebar

---

## 3. Các Thiếu Hụt Mức Trung Bình

### ⚠️ THIẾU HỤT 4: Hóa Đơn — Không Có Bộ Lọc

`InvoicePanel` hiện chỉ hiển thị **toàn bộ hóa đơn** theo thứ tự thời gian, không lọc được:

| Bộ lọc cần có | Cột trong DB |
|---|---|
| Lọc theo khoảng ngày | `HoaDon.thoiGianXuat` |
| Lọc theo ca làm việc | `HoaDon.maCa` |
| Lọc theo nhân viên | `HoaDon.maNV` |
| Lọc theo trạng thái | `HoaDon.trangThai` |
| Lọc theo loại đơn | `HoaDon.loaiDon` |
| Tổng tiền cuối trang | Tính từ danh sách |
| In / Xuất hóa đơn | — |

---

### ⚠️ THIẾU HỤT 5: Thống Kê — Quá Sơ Sài

**Hiện chỉ có 2 biểu đồ:**
- Line chart: Doanh thu 7 ngày qua
- Bar chart: Top 5 món bán chạy

**Dữ liệu trong DB cho phép làm thêm:**

| Thống kê | Query |
|---|---|
| Doanh thu theo nhân viên | `HoaDon GROUP BY maNV` |
| Doanh thu theo ca | `HoaDon JOIN CaLamViec` |
| Tỷ lệ Tại Bàn / Mang Về | `HoaDon GROUP BY loaiDon` |
| Phân tích thanh toán (Tiền mặt vs CK) | `HoaDon GROUP BY hinhThucThanhToan` |
| Cảnh báo tồn kho sắp hết | `TonKho WHERE soLuongTon < mucToiThieu` |
| Top khu vực doanh thu | `HoaDon JOIN Ban JOIN KhuVuc` |
| Giá trị nhập kho theo tháng | `PhieuNhap GROUP BY MONTH` |

---

### ⚠️ THIẾU HỤT 6: Lịch Sử Ca Làm Việc

`CaLamViecDAOImpl` có đầy đủ các method `findAll()`, `findByNhanVien()`, `findByNgay()`, nhưng không có màn hình nào cho Quản Lý xem lại:
- Ai làm ca nào?
- Doanh thu từng ca?
- Giờ vào / ra của từng nhân viên?

---

### ⚠️ THIẾU HỤT 7: Quản Lý Kho (bảng `Kho`) — Chỉ Đọc

`WarehouseManagementPanel` dùng `Kho` chỉ để lọc tồn kho. Không có chức năng thêm kho mới, đổi tên kho, thay đổi người phụ trách.

---

## 4. Bản Đồ Menu Admin Lý Tưởng

```
SIDEBAR ── NHÓM "VẬN HÀNH" (cả 2 vai trò)
├── 🛒  Bán Hàng            ✅ Hoạt động
├── 📅  Đặt Bàn  [badge 🔴] ❌ CẦN XÂY DỰNG
└── 📜  Hoá Đơn             ⚠️ Cần thêm bộ lọc

SIDEBAR ── NHÓM "CA LÀM VIỆC" (cả 2 vai trò)
├── 🕐  Ca Hiện Tại         ✅ Có trên header
└── 📋  Lịch Sử Ca          ❌ CẦN XÂY DỰNG (chỉ QUAN_LY)

SIDEBAR ── NHÓM "THIẾT LẬP" (chỉ QUAN_LY)
├── 🍽️  Thực Đơn
│       Tab 1: Món & Size   ✅ Đã có
│       Tab 2: Topping      ❌ CẦN THÊM TAB
│       Tab 3: Công Thức    ❌ CẦN THÊM TAB
├── 💰  Bảng Giá            ✅ Đã có
└── 🪑  Sơ Đồ Bàn           ✅ Đã có

SIDEBAR ── NHÓM "QUẢN TRỊ" (chỉ QUAN_LY)
├── 👥  Nhân Viên           ✅ Đã có
├── 📦  Quản Lý Kho
│       Tab 1-4: ...        ✅ Đã có
│       Tab 5: Kho (CRUD)   ❌ CẦN THÊM
└── 📊  Thống Kê            ⚠️ Cần mở rộng đáng kể
```

---

## 5. Bảng Điểm Hoàn Thiện

| Module | Điểm | Nhận Xét |
|---|---|---|
| Bán Hàng (Bàn → Order) | ⭐⭐⭐⭐⭐ 5/5 | Hoàn chỉnh nhất |
| Thực Đơn (Mon + Size) | ⭐⭐⭐⭐☆ 4/5 | Thiếu tab Topping |
| **Topping Admin** | ☆☆☆☆☆ **0/5** | **HOÀN TOÀN THIẾU** |
| **Công Thức Pha Chế** | ☆☆☆☆☆ **0/5** | **HOÀN TOÀN THIẾU** |
| Bảng Giá | ⭐⭐⭐⭐☆ 4/5 | Tốt |
| Sơ Đồ Bàn & Khu Vực | ⭐⭐⭐⭐☆ 4/5 | Tốt |
| Nhân Viên | ⭐⭐⭐⭐☆ 4/5 | Tốt |
| Quản Lý Kho | ⭐⭐⭐⭐☆ 4/5 | Thiếu CRUD bảng Kho |
| Hóa Đơn | ⭐⭐⭐☆☆ 3/5 | Thiếu bộ lọc & in |
| **Đặt Bàn** | ☆☆☆☆☆ **0/5** | **HOÀN TOÀN THIẾU** |
| Ca Làm Việc (lịch sử) | ⭐☆☆☆☆ 1/5 | Không có màn hình |
| Thống Kê | ⭐⭐☆☆☆ 2/5 | Rất sơ sài |
| **TỔNG** | **33/60** | **~55%** |

---

## 6. Danh Sách Công Việc Phải Làm (Backlog)

| Ưu tiên | Task | Ước tính | Ghi chú |
|---|---|---|---|
| 🔴 **P1** | Xây dựng `ReservationPanel` (Đặt Bàn) | ~3h | DAO đã sẵn sàng |
| 🔴 **P1** | Thêm Tab Topping vào `MenuManagementPanel` | ~2h | DAO đã sẵn sàng |
| 🔴 **P1** | Thêm Tab Công Thức vào `MenuManagementPanel` hoặc `WarehouseManagementPanel` | ~3h | DAO đã sẵn sàng |
| 🟡 **P2** | Thêm bộ lọc (ngày/ca/NV) vào `InvoicePanel` | ~1.5h | |
| 🟡 **P2** | Mở rộng `StatisticPanel` với 5-6 KPI thực sự | ~3h | Cần thêm query vào `StatisticDAOImpl` |
| 🟡 **P2** | Tạo `ShiftHistoryPanel` (Lịch Sử Ca Làm Việc) | ~1.5h | DAO đã sẵn sàng |
| 🟡 **P2** | Thiết kế lại Sidebar (badge, active state, phân nhóm đúng) | ~2h | |
| 🟢 **P3** | Thêm CRUD `Kho` vào `WarehouseManagementPanel` | ~1h | |

---

> **Ghi chú kỹ thuật:**
> - Tất cả các DAO và Entity cho mọi tính năng trên đều **đã tồn tại** trong codebase.
> - Không cần thay đổi schema database.
> - Công việc chỉ là tạo thêm **Controller** (nếu cần) và **Panel/Tab UI**.
