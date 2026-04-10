

USE QuanLyQuanCafe;
GO

-- 1. KhuVuc
INSERT INTO KhuVuc (maKhuVuc, tenKhuVuc, moTa, trangThai) VALUES
('KV001', N'Tầng 1',   N'Khu vực tầng 1, gần cửa ra vào', 1),
('KV002', N'Tầng 2',   N'Khu vực tầng 2, view đẹp',       1),
('KV003', N'Mang về',  N'Khu vực phục vụ đơn mang về',    1);
GO

-- 2. NhanVien
INSERT INTO NhanVien (maNV, tenNV, ngaySinh, soDienThoai, diaChi, username, passwordHash, trangThai, vaiTro) VALUES
('NV001', N'Nguyễn Văn An',   '1990-05-15', '0901111111', N'Q.1, TP.HCM',  'admin',   '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'DANG_LAM_VIEC', 'QUAN_LY'),
('NV002', N'Trần Thị Bình',   '1998-03-20', '0902222222', N'Q.3, TP.HCM',  'binhtran', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'DANG_LAM_VIEC', 'NHAN_VIEN'),
('NV003', N'Lê Minh Cường',   '1999-07-10', '0903333333', N'Q.5, TP.HCM',  'cuongle',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'DANG_LAM_VIEC', 'NHAN_VIEN'),
('NV004', N'Phạm Thị Dung',   '2000-11-25', '0904444444', N'Q.10, TP.HCM', 'dungpham', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'DANG_LAM_VIEC', 'NHAN_VIEN'),
('NV005', N'Hoàng Văn Em',    '1997-08-30', '0905555555', N'Q.Bình Thạnh', 'emhoang',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'DA_NGHI',       'NHAN_VIEN');
GO

-- 3. CaLamViec
INSERT INTO CaLamViec (maCa, ngayLam, gioBatDau, gioKetThuc, tongDoanhThu, trangThai, maNV, maKhuVuc) VALUES
('CA001', '2026-03-22', '06:00', '14:00', 0, 'DA_DONG',  'NV002', 'KV001'),
('CA002', '2026-03-22', '06:00', '14:00', 0, 'DA_DONG',  'NV003', 'KV002'),
('CA003', '2026-03-22', '14:00', '22:00', 0, 'DA_DONG',  'NV004', 'KV001'),
('CA004', '2026-03-23', '06:00',  NULL,   0, 'DANG_LAM', 'NV002', 'KV001'),
('CA005', '2026-03-23', '06:00',  NULL,   0, 'DANG_LAM', 'NV003', 'KV002');
GO

-- 4. Mon
INSERT INTO Mon (maMon, tenMon, moTa, hinhAnh, loaiMon, trangThai) VALUES
('MON001', N'Trà sữa truyền thống',  N'Trà sữa đài loan truyền thống',      NULL, 'TRA_SUA', 1),
('MON002', N'Cà phê sữa đá',         N'Cà phê phin pha với sữa đặc',        NULL, 'COFFEE', 1),
('MON003', N'Matcha latte',          N'Matcha Nhật kết hợp sữa tươi',       NULL, 'MATCHA_CACAO', 1),
('MON004', N'Trà đào cam sả',        N'Trà thơm vị đào, cam, sả tươi',      NULL, 'TRA', 1),
('MON005', N'Cà phê đen',            N'Cà phê đen đậm đà',                  NULL, 'COFFEE', 1),
('MON006', N'Bánh mì trứng',         N'Bánh mì trứng ốp la nóng giòn',      NULL, 'DO_AN_NHE', 1),
('MON007', N'Bánh croissant',        N'Bánh sừng bò bơ thơm',               NULL, 'DO_AN_NHE', 1),
('MON008', N'Cold Brew nguyên bản',  N'Cà phê ủ lạnh 24h',                  NULL, 'COLD_BREW', 1),
('MON009', N'Cold Brew Macchiato',   N'Cold Brew lớp kem macchiato béo',    NULL, 'COLD_BREW', 1),
('MON010', N'Cacao sữa đá',          N'Cacao Việt Nam đắng nhẹ',            NULL, 'MATCHA_CACAO', 1),
('MON011', N'Matcha đá xay',         N'Trà xanh Nhật Bản xay đá',           NULL, 'DA_XAY', 1),
('MON012', N'Oreo đá xay',           N'Đá xay cookie oreo chocolate',       NULL, 'DA_XAY', 1),
('MON013', N'Nước ép táo',           N'Táo Fuji ép tươi',                   NULL, 'NUOC_EP', 1),
('MON014', N'Nước ép thơm',          N'Thơm tươi nhiệt đới ép nguyên chất', NULL, 'NUOC_EP', 1),
('MON015', N'Soda chanh dây',        N'Soda chua thanh chanh dây',          NULL, 'SODA', 1),
('MON016', N'Soda việt quất',        N'Đậm vị việt quất chua ngọt',         NULL, 'SODA', 1),
('MON017', N'Yaourt trái cây',       N'Sữa chua trộn hạt dẻ và trái sơ ri', NULL, 'YAOURT', 1),
('MON018', N'Cà phê muối',           N'Cà phê phin thêm lớp kem mặn',       NULL, 'COFFEE', 1);
GO

-- 5. Size
INSERT INTO Size (maSize, tenSize, maMon) VALUES
('SZ001', 'S', 'MON001'), ('SZ002', 'M', 'MON001'), ('SZ003', 'L', 'MON001'),
('SZ004', 'S', 'MON002'), ('SZ005', 'M', 'MON002'), ('SZ006', 'L', 'MON002'),
('SZ007', 'S', 'MON003'), ('SZ008', 'M', 'MON003'), ('SZ009', 'L', 'MON003'),
('SZ010', 'S', 'MON004'), ('SZ011', 'M', 'MON004'), ('SZ012', 'L', 'MON004'),
('SZ013', 'S', 'MON005'), ('SZ014', 'M', 'MON005'), ('SZ015', 'L', 'MON005'),
('SZ016', 'Thường', 'MON006'),
('SZ017', 'Thường', 'MON007'),
('SZ018', 'M', 'MON008'), ('SZ019', 'L', 'MON008'),
('SZ020', 'M', 'MON009'), ('SZ021', 'L', 'MON009'),
('SZ022', 'M', 'MON010'), ('SZ023', 'L', 'MON010'),
('SZ024', 'M', 'MON011'), ('SZ025', 'L', 'MON011'),
('SZ026', 'M', 'MON012'), ('SZ027', 'L', 'MON012'),
('SZ028', 'Thường', 'MON013'),
('SZ029', 'Thường', 'MON014'),
('SZ030', 'Thường', 'MON015'),
('SZ031', 'Thường', 'MON016'),
('SZ032', 'Thường', 'MON017'),
('SZ033', 'M', 'MON018'), ('SZ034', 'L', 'MON018');
GO

-- 6. Topping
INSERT INTO Topping (maTopping, tenTopping, giaTopping, trangThai) VALUES
('TOP001', N'Trân châu đen',     5000,  1),
('TOP002', N'Trân châu trắng',   5000,  1),
('TOP003', N'Thạch cà phê',      5000,  1),
('TOP004', N'Thạch trái cây',    5000,  1),
('TOP005', N'Kem cheese',        10000, 1);
GO

-- 7. BangGia
INSERT INTO BangGia (maBangGia, tenBangGia, ngayBatDau, ngayKetThuc, trangThai) VALUES
('BG001', N'Bảng giá thường ngày',     '2026-01-01', '2026-12-31', 1),
('BG002', N'Bảng giá cuối tuần',       '2026-03-01', '2026-12-31', 1),
('BG003', N'Bảng giá ngày lễ Tết',     '2026-04-29', '2026-05-01', 0);
GO

-- 8. BangGiaChiTiet
INSERT INTO BangGiaChiTiet (maBGCT, giaBan, maSize, maBangGia) VALUES
('BGCT001', 35000, 'SZ001', 'BG001'),
('BGCT002', 42000, 'SZ002', 'BG001'),
('BGCT003', 49000, 'SZ003', 'BG001'),
('BGCT004', 38000, 'SZ001', 'BG002'),
('BGCT005', 45000, 'SZ002', 'BG002'),
('BGCT006', 52000, 'SZ003', 'BG002'),
('BGCT007', 30000, 'SZ004', 'BG001'),
('BGCT008', 35000, 'SZ005', 'BG001'),
('BGCT009', 40000, 'SZ006', 'BG001'),
('BGCT010', 38000, 'SZ007', 'BG001'),
('BGCT011', 45000, 'SZ008', 'BG001'),
('BGCT012', 52000, 'SZ009', 'BG001'),
('BGCT013', 25000, 'SZ016', 'BG001'),
('BGCT014', 35000, 'SZ017', 'BG001'),
('BGCT015', 30000, 'SZ010', 'BG001'),
('BGCT016', 35000, 'SZ011', 'BG001'),
('BGCT017', 40000, 'SZ012', 'BG001'),
('BGCT018', 25000, 'SZ013', 'BG001'),
('BGCT019', 30000, 'SZ014', 'BG001'),
('BGCT020', 35000, 'SZ015', 'BG001'),
('BGCT021', 45000, 'SZ018', 'BG001'),
('BGCT022', 52000, 'SZ019', 'BG001'),
('BGCT023', 49000, 'SZ020', 'BG001'),
('BGCT024', 55000, 'SZ021', 'BG001'),
('BGCT025', 38000, 'SZ022', 'BG001'),
('BGCT026', 45000, 'SZ023', 'BG001'),
('BGCT027', 49000, 'SZ024', 'BG001'),
('BGCT028', 55000, 'SZ025', 'BG001'),
('BGCT029', 49000, 'SZ026', 'BG001'),
('BGCT030', 55000, 'SZ027', 'BG001'),
('BGCT031', 40000, 'SZ028', 'BG001'),
('BGCT032', 40000, 'SZ029', 'BG001'),
('BGCT033', 35000, 'SZ030', 'BG001'),
('BGCT034', 35000, 'SZ031', 'BG001'),
('BGCT035', 38000, 'SZ032', 'BG001'),
('BGCT036', 35000, 'SZ033', 'BG001'),
('BGCT037', 42000, 'SZ034', 'BG001');
GO

-- 9. NguyenLieu
INSERT INTO NguyenLieu (maNL, tenNL, donViTinh, donGiaNhap, ngayHetHan) VALUES
('NL001', N'Trà đen',          'gram',  150,   NULL),
('NL002', N'Sữa tươi',         'ml',    25,    '2026-04-01'),
('NL003', N'Sữa đặc',          'gram',  50,    '2026-06-01'),
('NL004', N'Đường',            'gram',  20,    NULL),
('NL005', N'Trân châu đen',    'gram',  200,   '2026-03-30'),
('NL006', N'Bột matcha',       'gram',  800,   '2026-12-31'),
('NL007', N'Cà phê rang xay',  'gram',  300,   NULL),
('NL008', N'Đá viên',          'gram',  5,     NULL);
GO

-- 10. DinhMucNguyenLieu
INSERT INTO DinhMucNguyenLieu (maDinhMuc, soLuong, maMon, maNL) VALUES
('DM001', 5,   'MON001', 'NL001'),
('DM002', 150, 'MON001', 'NL002'),
('DM003', 15,  'MON001', 'NL004'),
('DM004', 100, 'MON001', 'NL008'),
('DM005', 20,  'MON002', 'NL007'),
('DM006', 30,  'MON002', 'NL003'),
('DM007', 150, 'MON002', 'NL008');
GO

-- 11. Ban
INSERT INTO Ban (maBan, soBan, maKhuVuc, sucChua, trangThai) VALUES
('BAN001', N'Bàn 01', 'KV001', 4, 'TRONG'),
('BAN002', N'Bàn 02', 'KV001', 4, 'TRONG'),
('BAN003', N'Bàn 03', 'KV001', 4, 'TRONG'),
('BAN004', N'Bàn 04', 'KV001', 6, 'TRONG'),
('BAN005', N'Bàn 09', 'KV002', 4, 'TRONG'),
('BAN006', N'Bàn 10', 'KV002', 4, 'TRONG');
GO

-- 12. NhaCungCap
INSERT INTO NhaCungCap (maNCC, tenNCC, diaChi, soDienThoai, email) VALUES
('NCC001', N'Công ty Trà Cao Nguyên',    N'Đà Lạt, Lâm Đồng',      '02633123456', 'contact@tracaonguyen.vn'),
('NCC002', N'Cà phê Buôn Ma Thuột',      N'Buôn Ma Thuột, Đắk Lắk', '02623456789', 'info@caphebuonmathuot.vn');
GO

-- 13. Kho
INSERT INTO Kho (maKho, tenKho, diaChi, maNV) VALUES
('KHO001', N'Kho nguyên liệu chính', N'Tầng trệt, khu bếp', 'NV001');
GO

-- 14. TonKho
INSERT INTO TonKho (maTonKho, soLuongTon, mucToiThieu, ngayCapNhat, maKho, maNL) VALUES
('TK001',  2000,  500,  GETDATE(), 'KHO001', 'NL001'),  
('TK002',  10000, 2000, GETDATE(), 'KHO001', 'NL002'),  
('TK003',  3000,  500,  GETDATE(), 'KHO001', 'NL003'),  
('TK004',  5000,  1000, GETDATE(), 'KHO001', 'NL004'),  
('TK005',  2000,  300,  GETDATE(), 'KHO001', 'NL005'),  
('TK006',  500,   100,  GETDATE(), 'KHO001', 'NL006'),  
('TK007',  3000,  500,  GETDATE(), 'KHO001', 'NL007'),  
('TK008',  50000, 5000, GETDATE(), 'KHO001', 'NL008');  
GO

PRINT '================================================';
PRINT 'Insert du lieu mau v9 thanh cong!';
PRINT '================================================';