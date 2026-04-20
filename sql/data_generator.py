import random
from datetime import datetime, timedelta

def write_sql():
    # Cấu hình thời gian: Từ 01/01/2026 đến nay
    start_date = datetime(2026, 1, 1)
    end_date = datetime(2026, 4, 20)
    
    with open('cafe_insert_data_v2.sql', 'w', encoding='utf-8') as f:
        f.write("USE QuanLyQuanCafe;\nGO\n\n")

        # 1. KhuVuc
        f.write("-- 1. KhuVuc\n")
        khu_vuc = [
            ("KV_MTV", "Khu Mang Về", "Đơn takeaway, không có bàn"),
            ("KV_A", "Khu A", "Khu vực sảnh chính và sân vườn ngoài trời"),
            ("KV_B", "Khu B", "Khu vực tầng trệt phòng lạnh"),
            ("KV_C", "Khu C", "Khu vực tầng 1 ngoài trời"),
            ("KV_D", "Khu D", "Khu vực tầng 1 phòng lạnh"),
            ("KV_NV", "Khu Nhân Viên", "Khu vực dành cho nhân viên")
        ]
        f.write("INSERT INTO KhuVuc (maKhuVuc, tenKhuVuc, moTa, trangThai) VALUES\n")
        kv_vals = [f"('{kv[0]}', N'{kv[1]}', N'{kv[2]}', 1)" for kv in khu_vuc]
        f.write(",\n".join(kv_vals) + ";\nGO\n\n")

        # 2. Ban (110 bàn)
        f.write("-- 2. Ban\n")
        ban_list = []
        ban_idx = 1
        def add_ban_range(kv_ma, count, min_s, max_s):
            nonlocal ban_idx
            for _ in range(count):
                ma = f"BAN{ban_idx:03d}"
                ban_list.append(f"('{ma}', N'Bàn {ban_idx:03d}', '{kv_ma}', {random.randint(min_s, max_s)}, 'TRONG')")
                ban_idx += 1
        
        add_ban_range("KV_A", 30, 4, 8)
        add_ban_range("KV_B", 25, 2, 4)
        add_ban_range("KV_C", 30, 4, 6)
        add_ban_range("KV_D", 25, 2, 4)
        
        f.write("INSERT INTO Ban (maBan, soBan, maKhuVuc, sucChua, trangThai) VALUES\n")
        f.write(",\n".join(ban_list) + ";\nGO\n\n")

        # 3. NhanVien
        f.write("-- 3. NhanVien\n")
        pw_hash = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92" # 123456
        nhan_vien = [
            ("NV001", "Phan Đào Đăng Khoa", "QUAN_LY", "dangkhoa"),
            ("NV002", "Phạm Minh Hùng", "QUAN_LY", "minhhung"),
            ("NV003", "Lê Sỹ Hùng", "QUAN_LY", "syhung"),
            ("NV004", "Nguyễn Đức Hùng", "QUAN_LY", "duchung"),
            ("NV005", "Trần Gia Huy", "QUAN_LY", "giahuy"),
            ("NV006", "Admin", "QUAN_LY", "admin"),
        ]
        # Thêm nhân viên phục vụ
        for i in range(7, 16):
            nhan_vien.append((f"NV{i:03d}", f"Nhân viên {i}", "NHAN_VIEN", f"nv{i}"))

        f.write("INSERT INTO NhanVien (maNV, tenNV, ngaySinh, soDienThoai, diaChi, username, passwordHash, trangThai, vaiTro) VALUES\n")
        nv_vals = [f"('{nv[0]}', N'{nv[1]}', '2000-01-01', '090123456{i}', N'TP.HCM', '{nv[3]}', '{pw_hash}', 'DANG_LAM_VIEC', '{nv[2]}')" for i, nv in enumerate(nhan_vien)]
        f.write(",\n".join(nv_vals) + ";\nGO\n\n")

        # 4. CaLamViec (Tạo ca làm việc hàng ngày)
        f.write("-- 4. CaLamViec\n")
        cas = []
        cur = start_date
        ca_idx = 1
        while cur <= end_date:
            d_str = cur.strftime("%Y-%m-%d")
            # Ca sáng, chiều, tối
            for shift in [("06:00", "14:00"), ("14:00", "22:00")]:
                nv = random.choice(nhan_vien)[0]
                kv = random.choice(["KV_A", "KV_B", "KV_C", "KV_D", "KV_MTV"])
                cas.append((f"CA{ca_idx:05d}", d_str, shift[0], shift[1], nv, kv))
                ca_idx += 1
            cur += timedelta(days=1)
        
        f.write("INSERT INTO CaLamViec (maCa, ngayLam, gioBatDau, gioKetThuc, tongDoanhThu, trangThai, maNV, maKhuVuc) VALUES\n")
        ca_vals = [f"('{c[0]}', '{c[1]}', '{c[2]}', '{c[3]}', 0, 'DA_DONG', '{c[4]}', '{c[5]}')" for c in cas]
        for i in range(0, len(ca_vals), 100):
            f.write(",\n".join(ca_vals[i:i+100]) + (";\nINSERT INTO CaLamViec (maCa, ngayLam, gioBatDau, gioKetThuc, tongDoanhThu, trangThai, maNV, maKhuVuc) VALUES\n" if i+100 < len(ca_vals) else ";\nGO\n\n"))

        # 5. Mon (40 món)
        f.write("-- 5. Mon\n")
        mon_data = [
            ('MON001', 'Cà phê đen', 'COFFEE'), ('MON002', 'Cà phê sữa', 'COFFEE'), ('MON003', 'Cà phê muối', 'COFFEE'),
            ('MON004', 'Bạc xỉu', 'COFFEE'), ('MON005', 'Americano', 'COFFEE'), ('MON006', 'Cà phê sữa tươi', 'COFFEE'),
            ('MON007', 'Latte', 'COFFEE'), ('MON008', 'Cold Brew truyền thống', 'COLD_BREW'), ('MON009', 'Cold Brew cam sả', 'COLD_BREW'),
            ('MON010', 'Cold Brew sữa tươi', 'COLD_BREW'), ('MON011', 'Matcha Latte', 'MATCHA_CACAO'), ('MON012', 'Matcha đá xay', 'MATCHA_CACAO'),
            ('MON013', 'Cacao nóng', 'MATCHA_CACAO'), ('MON014', 'Trà đào cam sả', 'TRA'), ('MON015', 'Trà vải', 'TRA'),
            ('MON016', 'Trà dâu tây', 'TRA'), ('MON017', 'Trà lài macchiato', 'TRA'), ('MON018', 'Trà ô long sen', 'TRA'),
            ('MON019', 'Trà sữa truyền thống', 'TRA_SUA'), ('MON020', 'Trà sữa thái xanh', 'TRA_SUA'), ('MON021', 'Trà sữa oolong', 'TRA_SUA'),
            ('MON022', 'Trà sữa matcha', 'TRA_SUA'), ('MON023', 'Đá xay chocolate', 'DA_XAY'), ('MON024', 'Đá xay việt quất', 'DA_XAY'),
            ('MON025', 'Đá xay caramel', 'DA_XAY'), ('MON026', 'Nước ép cam', 'NUOC_EP'), ('MON027', 'Nước ép dưa hấu', 'NUOC_EP'),
            ('MON028', 'Nước ép thơm', 'NUOC_EP'), ('MON029', 'Nước ép cà rốt', 'NUOC_EP'), ('MON030', 'Soda chanh dây', 'SODA'),
            ('MON031', 'Soda việt quất', 'SODA'), ('MON032', 'Soda táo xanh', 'SODA'), ('MON033', 'Yaourt trái cây', 'YAOURT'),
            ('MON034', 'Yaourt dâu tây', 'YAOURT'), ('MON035', 'Bánh mì ốp la', 'DO_AN_NHE'), ('MON036', 'Bánh Croissant', 'DO_AN_NHE'),
            ('MON037', 'Bánh Tiramisu', 'DO_AN_NHE'), ('MON038', 'Bánh Mousse chanh dây', 'DO_AN_NHE'), ('MON039', 'Bánh Cookies', 'DO_AN_NHE'),
            ('MON040', 'Hướng dương', 'DO_AN_NHE')
        ]
        f.write("INSERT INTO Mon (maMon, tenMon, loaiMon, trangThai) VALUES\n")
        mon_vals = [f"('{m[0]}', N'{m[1]}', '{m[2]}', 1)" for m in mon_data]
        f.write(",\n".join(mon_vals) + ";\nGO\n\n")

        # 6. Size
        f.write("-- 6. Size\n")
        sizes = []
        sz_idx = 1
        for m in mon_data:
            if m[2] == 'DO_AN_NHE':
                sizes.append((f"SZ{sz_idx:03d}", "Thường", m[0], 1.0))
                sz_idx += 1
            else:
                for t in [("S", 1.0), ("M", 1.2), ("L", 1.4)]:
                    sizes.append((f"SZ{sz_idx:03d}", t[0], m[0], t[1]))
                    sz_idx += 1
        f.write("INSERT INTO Size (maSize, tenSize, maMon, tileSize) VALUES\n")
        sz_vals = [f"('{s[0]}', N'{s[1]}', '{s[2]}', {s[3]})" for s in sizes]
        for i in range(0, len(sz_vals), 100):
            f.write(",\n".join(sz_vals[i:i+100]) + (";\nINSERT INTO Size (maSize, tenSize, maMon, tileSize) VALUES\n" if i+100 < len(sz_vals) else ";\nGO\n\n"))

        # 7. Topping
        f.write("-- 7. Topping\n")
        toppings = [
            ('TOP001', 'Trân châu đen', 5000), ('TOP002', 'Trân châu trắng', 5000), ('TOP003', 'Thạch trái cây', 5000),
            ('TOP004', 'Kem cheese', 10000), ('TOP005', 'Pudding trứng', 8000), ('TOP006', 'Thạch nha đam', 5000),
            ('TOP007', 'Đào ngâm', 7000), ('TOP008', 'Sương sáo', 5000)
        ]
        f.write("INSERT INTO Topping (maTopping, tenTopping, giaTopping, trangThai) VALUES\n")
        tp_vals = [f"('{t[0]}', N'{t[1]}', {t[2]}, 1)" for t in toppings]
        f.write(",\n".join(tp_vals) + ";\nGO\n\n")

        # 8. BangGia & BangGiaChiTiet
        f.write("-- 8. BangGia\n")
        f.write("INSERT INTO BangGia (maBangGia, tenBangGia, ngayBatDau, trangThai, hoatDong) VALUES ('BG001', N'Giá chuẩn', '2026-01-01', 1, 1);\nGO\n\n")
        
        base_prices = {
            'COFFEE': 25000, 'COLD_BREW': 45000, 'MATCHA_CACAO': 40000, 'TRA': 35000,
            'TRA_SUA': 35000, 'DA_XAY': 50000, 'NUOC_EP': 40000, 'SODA': 35000,
            'YAOURT': 35000, 'DO_AN_NHE': 30000
        }
        f.write("-- 9. BangGiaChiTiet\n")
        bgct_vals = []
        bg_idx = 1
        for s in sizes:
            ma_mon = s[2]
            loai = next(m[2] for m in mon_data if m[0] == ma_mon)
            base = base_prices[loai]
            price = int(base * s[3])
            bgct_vals.append(f"('BGCT{bg_idx:04d}', {price}, '{s[0]}', 'BG001')")
            bg_idx += 1
        
        f.write("INSERT INTO BangGiaChiTiet (maBGCT, giaBan, maSize, maBangGia) VALUES\n")
        for i in range(0, len(bgct_vals), 100):
            f.write(",\n".join(bgct_vals[i:i+100]) + (";\nINSERT INTO BangGiaChiTiet (maBGCT, giaBan, maSize, maBangGia) VALUES\n" if i+100 < len(bgct_vals) else ";\nGO\n\n"))

        # 10. NguyenLieu & TonKho
        f.write("-- 10. NguyenLieu & TonKho\n")
        nls = [
            ('NL001', 'Cà phê hạt', 'g', 200), ('NL002', 'Trà đen', 'g', 150), ('NL003', 'Sữa đặc', 'ml', 50),
            ('NL004', 'Sữa tươi', 'ml', 30), ('NL005', 'Trân châu đen', 'g', 100), ('NL006', 'Đường', 'g', 20),
            ('NL007', 'Đá viên', 'g', 5), ('NL008', 'Trân châu trắng', 'g', 120), ('NL009', 'Thạch trái cây', 'g', 80),
            ('NL010', 'Bột phô mai', 'g', 300), ('NL011', 'Bột Pudding', 'g', 250), ('NL012', 'Nha đam miếng', 'g', 90),
            ('NL013', 'Đào miếng ngâm', 'g', 150), ('NL014', 'Sương sáo', 'g', 40)
        ]
        f.write("INSERT INTO NhaCungCap (maNCC, tenNCC, soDienThoai) VALUES ('NCC001', N'NCC Tổng', '0912345678');\n")
        f.write("INSERT INTO Kho (maKho, tenKho, maNV) VALUES ('KHO001', N'Kho chính', 'NV001');\n")
        f.write("INSERT INTO NguyenLieu (maNL, tenNL, donViTinh, donGiaNhap, donViDongGoi, khoiLuongDongGoi) VALUES\n")
        nl_vals = [f"('{n[0]}', N'{n[1]}', '{n[2]}', {n[3]}, N'Bao/Bịch', 1000)" for n in nls]
        f.write(",\n".join(nl_vals) + ";\nGO\n\n")
        
        f.write("INSERT INTO TonKho (maTonKho, soLuongTon, mucToiThieu, maKho, maNL) VALUES\n")
        tk_vals = [f"('TK{i+1:03d}', 100000, 5000, 'KHO001', '{n[0]}')" for i, n in enumerate(nls)]
        f.write(",\n".join(tk_vals) + ";\nGO\n\n")

        # 11. DinhMucNguyenLieu (CHO TẤT CẢ MÓN)
        f.write("-- 11. DinhMucNguyenLieu\n")
        dm_vals = []
        dm_idx = 1
        # Gán định mức mẫu cho từng loại món
        for m in mon_data:
            ma = m[0]
            loai = m[2]
            if loai == 'COFFEE':
                dm_vals.append(f"('DM{dm_idx:04d}', 20, '{ma}', NULL, 'NL001')"); dm_idx += 1
                dm_vals.append(f"('DM{dm_idx:04d}', 15, '{ma}', NULL, 'NL003')"); dm_idx += 1
            elif loai in ['TRA', 'TRA_SUA']:
                dm_vals.append(f"('DM{dm_idx:04d}', 15, '{ma}', NULL, 'NL002')"); dm_idx += 1
                dm_vals.append(f"('DM{dm_idx:04d}', 20, '{ma}', NULL, 'NL006')"); dm_idx += 1
            elif loai == 'MATCHA_CACAO':
                dm_vals.append(f"('DM{dm_idx:04d}', 100, '{ma}', NULL, 'NL004')"); dm_idx += 1
            # Thêm đá cho tất cả đồ uống
            if loai != 'DO_AN_NHE':
                dm_vals.append(f"('DM{dm_idx:04d}', 150, '{ma}', NULL, 'NL007')"); dm_idx += 1

        # Định mức cho Topping
        topping_nl_map = {'TOP001': 'NL005', 'TOP002': 'NL008', 'TOP003': 'NL009', 'TOP004': 'NL010', 'TOP005': 'NL011', 'TOP006': 'NL012', 'TOP007': 'NL013', 'TOP008': 'NL014'}
        for tid, nid in topping_nl_map.items():
            dm_vals.append(f"('DM{dm_idx:04d}', 30, NULL, '{tid}', '{nid}')")
            dm_idx += 1

        f.write("INSERT INTO DinhMucNguyenLieu (maDinhMuc, soLuong, maMon, maTopping, maNL) VALUES\n")
        for i in range(0, len(dm_vals), 100):
            f.write(",\n".join(dm_vals[i:i+100]) + (";\nINSERT INTO DinhMucNguyenLieu (maDinhMuc, soLuong, maMon, maTopping, maNL) VALUES\n" if i+100 < len(dm_vals) else ";\nGO\n\n"))

        # 12. HoaDon & ChiTietHoaDon (Dữ liệu phong phú)
        f.write("-- 12. HoaDon & ChiTiet\n")
        hd_vals = []
        ct_vals = []
        ct_tp_vals = []
        
        hd_idx = 1
        ct_idx = 1
        ctp_idx = 1
        
        # Chọn ra 500 hóa đơn ngẫu nhiên rải rác
        for i in range(1, 1001): # 1000 hóa đơn
            ca = random.choice(cas)
            ma_ca = ca[0]
            ngay = ca[1]
            ma_nv = ca[4]
            
            is_takeaway = random.random() < 0.3
            ma_ban = "NULL" if is_takeaway else f"'BAN{random.randint(1, 110):03d}'"
            loai_don = 'MANG_VE' if is_takeaway else 'TAI_BAN'
            
            ma_hd = f"HD{hd_idx:05d}"
            hd_idx += 1
            
            tong_tien = 0
            # 1-4 món mỗi hóa đơn
            for _ in range(random.randint(1, 4)):
                sz = random.choice(sizes)
                ma_mon = sz[2]
                ma_size = sz[0]
                price = next(int(b.split(',')[1]) for b in bgct_vals if ma_size in b)
                qty = random.randint(1, 2)
                subtotal = price * qty
                
                ma_ct = f"CTHD{ct_idx:06d}"
                ct_idx += 1
                ct_vals.append(f"('{ma_ct}', {qty}, {price}, {subtotal}, NULL, '{ma_hd}', '{ma_mon}', '{ma_size}')")
                tong_tien += subtotal
                
                # Topping?
                if random.random() < 0.4 and loai_don != 'DO_AN_NHE':
                    tp = random.choice(toppings)
                    tp_price = tp[2]
                    ct_tp_vals.append(f"('CTHDT{ctp_idx:06d}', {qty}, {tp_price}, '{ma_ct}', '{tp[0]}')")
                    ctp_idx += 1
                    tong_tien += tp_price * qty
            
            hd_vals.append(f"('{ma_hd}', '{ngay} 08:00', '{ngay} 08:30', {tong_tien}, 'DA_THANH_TOAN', 'TIEN_MAT', {ma_ban}, '{ma_ca}', '{loai_don}', NULL, '{ma_nv}')")

        # Ghi file theo chunk
        def write_chunks(table, cols, vals):
            f.write(f"INSERT INTO {table} ({cols}) VALUES\n")
            for i in range(0, len(vals), 100):
                f.write(",\n".join(vals[i:i+100]) + (f";\nINSERT INTO {table} ({cols}) VALUES\n" if i+100 < len(vals) else ";\nGO\n\n"))

        write_chunks("HoaDon", "maHD, thoiGianXuat, thoiGianThanhToan, tongTienPhaiTra, trangThai, hinhThucThanhToan, maBan, maCa, loaiDon, ghiChu, maNV", hd_vals)
        write_chunks("ChiTietHoaDon", "maCTHD, soLuong, donGia, thanhTien, ghiChu, maHD, maMon, maSize", ct_vals)
        write_chunks("ChiTietHoaDonTopping", "maID, soLuong, giaTopping, maCTHD, maTopping", ct_tp_vals)

if __name__ == '__main__':
    write_sql()
    print("Done! Generated cafe_insert_data_v2.sql with 1000 invoices and full recipes.")
