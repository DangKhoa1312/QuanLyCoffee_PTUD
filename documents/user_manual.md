# HƯỚNG DẪN SỬ DỤNG ỨNG DỤNG CHI TIẾT

---

### 1. GIỚI THIỆU ỨNG DỤNG
Hệ thống Quản lý Quán Cà Phê Thông Minh (Smart Coffee Shop Management System) là một ứng dụng Desktop (Java Swing) được thiết kế đặc biệt nhằm tối ưu hóa quy trình vận hành của các quán cà phê vừa và nhỏ. Ứng dụng cung cấp các tính năng mạnh mẽ từ khâu gọi món (POS), quản lý sơ đồ bàn, đến hệ thống quản lý tồn kho tự động theo định mức (BOM), phân quyền nhân sự, và hệ thống báo cáo doanh thu trực quan.

Đặc biệt, hệ thống được thiết kế với giao diện hiện đại (Flat Design), tích hợp tính năng "Kho Lưu Tạm" (Bàn Ma) chống thất thoát nguyên liệu, giúp chủ cửa hàng tối ưu hóa việc quản lý thức uống thừa hoặc bị hủy.

---

### 2. CẤU HÌNH PHẦN CỨNG - PHẦN MỀM

**Phần cứng:**
- **CPU:** Intel Core i3 / AMD Ryzen 3 trở lên (Khuyến nghị Core i5 để chạy mượt mà cơ sở dữ liệu lớn).
- **RAM:** Tối thiểu 4GB (Khuyến nghị 8GB để xử lý đa luồng UI).
- **Ổ cứng:** Trống tối thiểu 500MB (Khuyến nghị dùng ổ cứng SSD để tăng tốc truy xuất CSDL).
- **Màn hình:** Độ phân giải tối thiểu 1366x768 (Khuyến nghị Full HD 1920x1080 để hiển thị trọn vẹn Sơ đồ bàn).
- **Thiết bị ngoại vi:** Hệ thống tương thích 100% với Máy in nhiệt (Máy in hóa đơn chuẩn 80mm), Chuột, và Bàn phím cơ.

**Phần mềm:**
- **Hệ điều hành:** Windows 10/11 (64-bit), macOS 10.15+, hoặc Linux Ubuntu 20.04+.
- **Môi trường chạy (Runtime):** Java Runtime Environment (JRE) hoặc Java Development Kit (JDK) phiên bản 17 trở lên.
- **Cơ sở dữ liệu:** SQL Server (sử dụng thư viện mssql-jdbc) hoặc MySQL tùy thuộc vào phiên bản cài đặt của quán.

---

### 3. CÁC CHỨC NĂNG CHÍNH

Hệ thống được chia thành 2 phân quyền chính (Actors): **Quản lý (Admin)** và **Thu ngân (Cashier)**. Thanh Menu bên trái hiển thị các tính năng phụ thuộc vào quyền hạn người dùng.

#### 3.1. Chức năng của Quản lý (Admin)
Quản lý là tài khoản có đặc quyền cao nhất (Full Access), được phép truy cập tất cả các menu thuộc nhóm: Vận Hành, Thiết Lập, và Quản Trị.

**A. Đăng nhập hệ thống**
1. Mở ứng dụng, tại màn hình Đăng nhập, nhập số điện thoại (ID) và mật khẩu của Quản lý.
2. Nhấn nút "Đăng nhập". Hệ thống sẽ chuyển vào giao diện Tổng quan (Dashboard) với đầy đủ thanh Menu.

**B. Nhóm Quản Trị: Nhân Sự, Khách Hàng, Khuyến Mãi**
1. **Nhân viên:** Thêm nhân viên mới, cấp quyền Thu ngân/Quản lý, khôi phục mật khẩu.
2. **Khách hàng:** Quản lý danh sách khách hàng thân thiết, xem hạng thành viên (Dựa vào tổng tiền đã chi tiêu) và điểm tích lũy.
3. **Khuyến mãi:** Tạo các chương trình Voucher (Giảm phần trăm hoặc giảm tiền mặt trực tiếp), giới hạn thời gian áp dụng và giá trị đơn hàng tối thiểu.

**C. Nhóm Thiết Lập: Thực Đơn & Cấu hình**
1. **Món & Size, Topping:** Khởi tạo danh sách các thức uống, cài đặt hình ảnh và phân loại Size (Nhỏ, Vừa, Lớn). Quản lý danh sách các món ăn kèm (Topping).
2. **Công Thức & Bảng Giá:** Đây là chức năng cốt lõi. Quản lý thiết lập định mức tiêu hao nguyên liệu cho từng món (Ví dụ: 1 ly cafe sữa cần 15g cà phê + 20ml sữa). Cập nhật giá bán theo từng khu vực.
3. **Cấu Hình:** Thiết lập Tên quán, Địa chỉ, Tỷ lệ quy đổi điểm thưởng, Thời gian giữ bàn đặt trước.

**D. Quản Lý Kho & Thống Kê**
1. **Quản Lý Kho:** Lập Phiếu Nhập Kho khi nhập hàng từ nhà cung cấp. Xem tồn kho nguyên liệu hiện tại. Hệ thống sẽ tự động trừ định mức nguyên liệu mỗi khi có một đơn hàng được Thu ngân Thanh toán. Các món sắp hết nguyên liệu sẽ được hiển thị màu đỏ ở Sơ đồ bán hàng.
2. **Thống Kê:** Trực quan hóa dữ liệu kinh doanh thông qua biểu đồ doanh thu, xem danh sách Top Món Bán Chạy.

---

#### 3.2. Chức năng của Thu ngân (Cashier)
Tài khoản Thu ngân bị giới hạn quyền, chỉ thao tác các nghiệp vụ trong nhóm "Vận Hành" (Bán Hàng, Đặt Bàn, Hóa Đơn) và Đóng/Mở ca.

**A. Giao ca - Mở ca làm việc**
1. Đăng nhập bằng tài khoản Thu ngân.
2. Hộp thoại **Mở Ca** sẽ hiện ra. Nhập số tiền mặt hiện có trong két sắt -> Nhấn `Xác nhận mở ca`.

**B. Bán hàng tại bàn (POS)**
1. Chọn tab **Bán Hàng**. Màn hình sẽ hiển thị các Thẻ Khu Vực (Tầng 1, Tầng 2, Sân Vườn, Mang Về, Bàn Ma).
2. Các bàn được thiết kế trực quan: Xanh lá (Trống), Đỏ (Đang phục vụ), Vàng (Đã đặt trước).
3. Click vào Bàn Trống để Order. Chọn món, chọn Tùy chọn size, ghi chú (Ít đá, Ít đường) hoặc thêm Topping.
4. Bấm `Báo bếp` để hệ thống cập nhật trạng thái "Đã Phục Vụ" (Gửi thông báo làm nước xuống quầy pha chế).

**C. Thao tác ĐẶC BIỆT: Xử lý Kho Lưu Tạm (Bàn Ma)**
- *Tình huống hủy món:* Bếp đã làm xong ly Trà Đào nhưng khách hủy đơn (đòi đổi món hoặc không uống nữa).
- *Xử lý Hủy:* Thu ngân click chọn ly Trà Đào, bấm `Hủy đơn`. Thay vì xóa ngang hóa đơn, phần mềm hỏi: *"Chuyển các món đã nấu sang Bàn Ma (Lưu tạm) để hủy đơn này không?"*. Ấn YES. Ly Trà Đào sẽ được giấu vào Thẻ Khu Vực "Bàn Ma".
- *Gợi ý tái sử dụng:* Nếu lát sau có khách mới gọi ly Trà Đào giống hệt. Hệ thống lập tức hiển thị nhãn màu Cam (Badge) ở trên cùng: `Sẵn có: 1`. Click vào món đó, hệ thống sẽ bật Dialog: *"Ở Bàn Ma đang có sẵn món này. Bạn có muốn lấy 1 phần CÓ SẴN (đỡ phải làm lại) không?"*. Ấn **Lấy món có sẵn**, hệ thống sẽ rút ly nước từ Bàn Ma qua bàn hiện tại. Rất tiết kiệm nguyên liệu!
- *Dọn Kho (Xóa hoàn toàn):* Cuối ngày, thu ngân vào Thẻ "Bàn Ma", ấn Hủy đơn. Hệ thống sẽ cảnh báo *"Bạn có CHẮC CHẮN muốn xóa bỏ vĩnh viễn toàn bộ đồ uống tồn trong kho này không?"*. Ấn YES để dọn sạch rác khỏi RAM hệ thống trước khi đóng ca.

**D. Thao tác Tùy Chọn Bàn (Chuyển / Ghép / Tách món)**
1. Chọn dropdown `Tùy chọn Bàn...` trên Giỏ hàng.
2. Thu ngân có thể chọn **Chuyển Bàn** (Chuyển toàn bộ món sang bàn mới), **Ghép Bàn** (Gộp chung với bàn đang có người), hoặc **Tách món** (Tách một số món sang hóa đơn riêng).

**E. Thanh toán & Tích điểm**
1. Khi khách gọi tính tiền, mở bàn đó và nhấn `Thanh Toán`.
2. Hộp thoại thanh toán cho phép tìm kiếm khách hàng bằng Số điện thoại. Hệ thống sẽ quy đổi điểm thành tiền giảm giá (dựa trên Cấu hình hệ thống).
3. Có thể nhập Mã Khuyến Mãi (Voucher) để nhận chiết khấu.
4. Nhập tiền khách đưa, phần mềm tính tiền thối lại. Bấm `Hoàn Tất`, kho nguyên liệu sẽ tự động được trừ dựa theo Công thức của các món trên hóa đơn.

**F. Kết thúc Ca làm việc (Đóng ca)**
1. Cuối ca, Thu ngân bấm vào Avatar tài khoản, chọn **Đóng Ca**.
2. Đếm lại tiền mặt thực tế trong két sắt, nhập lý do chênh lệch (nếu có).
3. Bấm `Xác nhận đóng ca`, hệ thống lưu lại lịch sử làm việc để Quản lý dễ dàng đối soát doanh thu.
