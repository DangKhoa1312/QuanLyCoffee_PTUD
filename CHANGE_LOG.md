# 📋 NHẬT KÝ LÀM VIỆC – Dự Án Quản Lý Quán Cafe
**Ngày làm việc:** 08/04/2026  
**Thư mục dự án:** `d:\PHAT_TRIEN_UD\quanlyquancafe-cafe-nhom03\`

---

## 🔧 VẤN ĐỀ 1: Lỗi kết nối Database

### Câu hỏi / Vấn đề gặp phải
Ứng dụng báo lỗi khi khởi động:
```
Loi ket noi database: The TCP/IP connection to the host localhost, port 1433 has failed.
Error: "Connection refused: getsockopt.
```
Kèm theo lỗi `NullPointerException` tại `NhanVienDAOImpl.findByUsername()`.

### Nguyên nhân
SQL Server đang chạy trên **instance có tên là `HUNG`**, không phải instance mặc định trên cổng 1433.

### Cách sửa

| File | Thay đổi |
|------|----------|
| `src/config/DatabaseConfig.java` | Đổi `SERVER = "localhost"` → `SERVER = "localhost\\HUNG"` |

---

## 🎨 VẤN ĐỀ 2: Hiện đại hóa giao diện (Font Roboto + Tiếng Việt thuần)

### Câu hỏi / Yêu cầu
> "Đổi sang tiếng Việt hết trong project này được không với font Roboto"

Toàn bộ giao diện đang dùng mã Unicode escape như `DANH S\u00C1CH B\u1EA2NG GI\u00C1` thay vì hiển thị tiếng Việt trực tiếp. Yêu cầu:
1. Chuyển toàn bộ sang tiếng Việt thuần (UTF-8).
2. Chuẩn hóa font sang **Roboto** cho tất cả component.

### Danh sách file đã sửa

#### 📁 `src/ui/`
| File | Nội dung thay đổi |
|------|-------------------|
| `MainFrame.java` | Giải mã toàn bộ Unicode escape trong Sidebar, Header, Menu điều hướng. Chuẩn hóa font Roboto. |
| `LoginForm.java` | Giải mã Unicode escape trong form đăng nhập. |

#### 📁 `src/ui/panel/`
| File | Nội dung thay đổi |
|------|-------------------|
| `TablePanel.java` | Giải mã nhãn trạng thái bàn (TRỐNG, ĐANG PHỤC VỤ, ĐÃ ĐẶT, MANG VỀ), legend, tiêu đề. |
| `InvoicePanel.java` | Giải mã tiêu đề, tên cột bảng (Mã Hóa Đơn, Bàn, Loại đơn...), nhãn nút. |
| `StatisticPanel.java` | Giải mã tiêu đề biểu đồ (Doanh Thu 7 Ngày Qua, Top 5 Món Bán Chạy...). |
| `OrderPanel.java` | Giải mã toàn bộ nhãn giao diện gọi món (Giỏ Hàng, Tổng tiền, các nút...). |

#### 📁 `src/ui/panel/admin/`
| File | Nội dung thay đổi |
|------|-------------------|
| `PriceManagementPanel.java` | Giải mã breadcrumb, tiêu đề "DANH SÁCH BẢNG GIÁ", nhãn cột. |
| `StaffManagementPanel.java` | Giải mã breadcrumb "Nhân viên", tiêu đề, trạng thái (Đang làm / Đã nghỉ), thông báo. |
| `MenuManagementPanel.java` | File đã sử dụng tiếng Việt trực tiếp – không cần sửa. |

#### 📁 `src/ui/dialog/`
| File | Nội dung thay đổi |
|------|-------------------|
| `StaffDialog.java` | Giải mã nhãn form: Mã Nhân Viên, comment, thông báo. |
| `MenuDialog.java` | Giải mã nhãn form dialog thêm/sửa món. |
| `PaymentDialog.java` | Giải mã toàn bộ: Thanh Toán Đơn Hàng, Tiền khách trả, Tiền thừa, XÁC NHẬN THANH TOÁN... |
| `InvoiceDetailDialog.java` | Giải mã: Chi Tiết Hóa Đơn, HÓA ĐƠN THANH TOÁN, tên cột, nút IN HÓA ĐƠN. |
| `ShiftOpenDialog.java` | Giải mã: Mở Ca Làm Việc, Nhân viên, Ngày, Số tiền đầu ca, BẮT ĐẦU CA. |
| `ShiftCloseDialog.java` | Giải mã: Đóng Ca Làm Việc, BÁO CÁO CUỐI CA, Doanh thu, Chênh lệch, XÁC NHẬN ĐÓNG CA. |
| `TransferTableDialog.java` | Giải mã: Chuyển / Gộp Bàn, Chọn Bàn Đích, trạng thái bàn, thông báo xác nhận. |
| `TakeawayListDialog.java` | Giải mã: Danh Sách Đơn Mang Về, + TẠO ĐƠN MỚI, Đóng, Tổng. |
| `ItemOptionDialog.java` | Giải mã nhãn dialog chọn size/topping khi gọi món. |

---

## 🛒 VẤN ĐỀ 3: Logic Bảng Giá – Tạo mới & Sao chép

### Câu hỏi / Yêu cầu
> "Ở phần bảng giá, tôi muốn chọn bảng giá mới thì ở phần detail nó phải để trống để nút Thêm chúng tôi thêm từ từ vào. Còn nếu chọn từ bảng giá khác thì phải hiện chi tiết các món từ bảng giá khác đó trên khung chi tiết. Và ấn Sao chép ngay thì chính thức sao chép và lưu được."

### Vấn đề trước đó
Khi mở dialog tạo **bảng giá mới**, hệ thống tự động nạp **toàn bộ menu với giá 0.0**, khiến khung chi tiết luôn đầy - không cho phép user bắt đầu từ một bảng trống.

### Cách sửa

#### `src/ui/dialog/PriceDetailDialog.java`

| Hàm / Vị trí | Thay đổi |
|---|---|
| `loadPriceTable()` | **Nếu tạo mới**: trả về bảng trống ngay. **Nếu sửa**: chỉ load các món đã có giá trong bảng đó (không load toàn bộ menu với 0.0 nữa). |
| Thêm `ActionListener` trên `cbCloneSource` | Khi user chọn bảng giá từ dropdown → **preview tức thì** chi tiết bảng đó vào khung (chưa lưu). Chọn lại "Bảng giá mới" → khôi phục trạng thái ban đầu. |
| `handleClone()` – cải viết hoàn toàn | 1. Validate: bắt buộc nhập Tên bảng giá & chọn nguồn. 2. Lưu header bảng giá vào DB. 3. Xóa chi tiết cũ (nếu có). 4. Sao chép toàn bộ chi tiết từ nguồn. 5. Hiện thông báo thành công & đóng dialog. |
| Thêm `handleCloneSourceChange()` | Hàm mới xử lý event khi user thay đổi dropdown – hiển thị preview. |

#### `src/controller/PriceController.java`

| Hàm | Thay đổi |
|-----|----------|
| `deleteAllDetailsOf(String maBG)` | **Thêm mới** – Xóa toàn bộ chi tiết giá của một bảng giá trước khi sao chép đè. |

---

## 🌯 VẤN ĐỀ 4: Tái cấu trúc Quản lý Thực đơn (Menu Management)

### Yêu cầu
- Loại bỏ cột "Thao tác" dư thừa trong bảng danh sách món ăn.
- Chuyển sang tương tác **Double-click** để chỉnh sửa món ăn.
- Tách biệt quản lý Giá bán khỏi Menu (Giá sẽ do module Bảng giá quản lý).
- Cải thiện UI: Tăng chiều cao ô Mô tả sản phẩm, đổi tên nút "LƯU THÔNG TIN" → "LƯU".

### Chi tiết thay đổi

| File | Nội dung thay đổi |
|------|-------------------|
| `src/ui/panel/admin/MenuManagementPanel.java` | Xóa cột Thao tác, ẩn cột chứa đối tượng Mon, thêm MouseListener (Double-click) để mở dialog sửa. |
| `src/ui/dialog/MenuDialog.java` | Xóa cột Giá bán trong bảng Size, tăng `txtMoTa` lên 7 dòng, cập nhật logic lưu (chỉ lưu Size). |
| `src/controller/MenuController.java` | Thêm hàm `saveSize()` để lưu thông tin kích thước độc lập. |

---

## 🔢 VẤN ĐỀ 5: Sửa lỗi Mã Size (ID trùng & Thứ tự hiển thị)

### Vấn đề
- Khi nhấn "Thêm Size" nhiều lần trước khi lưu, hệ thống bị trùng mã ID (ví dụ cùng là `SZ018`).
- Thứ tự hiển thị bị nhảy lộn xộn (009 -> 008 -> 007) do sắp xếp theo tên (S, M, L).

### Cách sửa

| File | Thay đổi |
|------|----------|
| `src/ui/dialog/MenuDialog.java` | Thêm hàm `generateNextMaSizeInSession()` để kiểm tra ID trong bảng trước khi tạo mã mới, đảm bảo không trùng khi chưa lưu DB. Đổi mặc định size mới từ "Mới" → "Thường". |
| `src/dao/impl/SizeDAOImpl.java` | Đổi câu lệnh SQL `ORDER BY tenSize` → `ORDER BY maSize` để hiển thị đúng thứ tự số. |

---

## 🏠 VẤN ĐỀ 6: Việt hóa & Tinh chỉnh Giao diện Gọi món (Order UI)

### Các tinh chỉnh nhỏ
1. **Lỗi lặp từ**: Sửa "Bàn số Bàn 1" → "Bàn 1" trong `OrderPanel` và `TablePanel`.
2. **Việt hóa Enum**: Đổi `DO_UONG`/`DO_AN` → "Đồ uống"/"Đồ ăn" trên các Tab danh mục.
3. **Robust Encoding**: Xử lý trường hợp chữ "Thường" bị lỗi font thành `Thư?ng` trong code để ẩn/đổi tên nhãn tương ứng.
4. **Popup**: Tăng kích thước `TransferTableDialog` (Chuyển/Gộp bàn) để dễ thao tác hơn.

---

## 📊 Tổng hợp số file đã sửa

| Loại | Số file |
|------|---------|
| Config / Connection | 1 |
| UI chính (Frame, Form) | 2 |
| UI Panel | 7 |
| UI Dialog | 11 |
| Controller | 1 |
| DAO | 1 |
| **Tổng** | **23 file** |

---

## 📝 Ghi chú kỹ thuật

- **Font Roboto**: Được khai báo trực tiếp trong code Java `new Font("Roboto", Font.BOLD, 14)`. Nếu máy tính chưa cài font Roboto, hệ thống tự động fallback về font mặc định của Windows nhưng vẫn hiển thị tiếng Việt đúng.
- **Encoding file**: Các file `.java` sử dụng encoding UTF-8, cho phép viết tiếng Việt trực tiếp trong code thay vì dùng `\uXXXX`.
- **SQL Server Instance**: Kết nối qua Named Instance `HUNG` (không dùng cổng 1433 mặc định).

---

*File này được tạo tự động để lưu lại lịch sử thay đổi của dự án.*
