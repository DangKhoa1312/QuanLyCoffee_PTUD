-- ================================================================
-- DATABASE: QuanLyQuanCafe
-- Anh xa tu Class Diagram Final (v10)
-- DonHang/ChiTietDonHang luu tam tren RAM (khong co trong DB)
-- ChiTietHoaDon/ChiTietHoaDonTopping luu vao DB khi thanh toan
-- SQL Server | Nhom 03
-- ================================================================

USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'QuanLyQuanCafe')
BEGIN
    ALTER DATABASE QuanLyQuanCafe SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyQuanCafe;
END
GO

CREATE DATABASE QuanLyQuanCafe COLLATE Vietnamese_CI_AS;
GO

USE QuanLyQuanCafe;
GO

-- 1. KhuVuc
CREATE TABLE KhuVuc (
    maKhuVuc  VARCHAR(20)   NOT NULL,
    tenKhuVuc NVARCHAR(100) NOT NULL,
    moTa      NVARCHAR(255) NULL,
    trangThai BIT           NOT NULL DEFAULT 1,
    CONSTRAINT PK_KhuVuc PRIMARY KEY (maKhuVuc)
);
GO

-- 2. NhanVien
CREATE TABLE NhanVien (
    maNV         VARCHAR(20)   NOT NULL,
    tenNV        NVARCHAR(100) NOT NULL,
    ngaySinh     DATE          NULL,
    soDienThoai  VARCHAR(15)   NULL,
    diaChi       NVARCHAR(255) NULL,
    username     VARCHAR(50)   NOT NULL,
    passwordHash VARCHAR(255)  NOT NULL,
    trangThai    VARCHAR(20)   NOT NULL DEFAULT 'DANG_LAM_VIEC',
    vaiTro       VARCHAR(20)   NOT NULL DEFAULT 'NHAN_VIEN',

    CONSTRAINT PK_NhanVien      PRIMARY KEY (maNV),
    CONSTRAINT UQ_NV_Username   UNIQUE (username),
    CONSTRAINT UQ_NV_SDT        UNIQUE (soDienThoai),
    CONSTRAINT CHK_NV_TrangThai CHECK (trangThai IN ('DANG_LAM_VIEC','DA_NGHI')),
    CONSTRAINT CHK_NV_VaiTro    CHECK (vaiTro    IN ('QUAN_LY','NHAN_VIEN'))
);
GO

-- 3. CaLamViec
CREATE TABLE CaLamViec (
    maCa         VARCHAR(20)   NOT NULL,
    ngayLam      DATE          NOT NULL,
    gioBatDau    TIME          NOT NULL,
    gioKetThuc   TIME          NULL,
    tongDoanhThu DECIMAL(12,2) NOT NULL DEFAULT 0,
    trangThai    VARCHAR(20)   NOT NULL DEFAULT 'DANG_LAM',
    maNV         VARCHAR(20)   NOT NULL,
    maKhuVuc     VARCHAR(20)   NULL,

    CONSTRAINT PK_CaLamViec      PRIMARY KEY (maCa),
    CONSTRAINT FK_Ca_NhanVien    FOREIGN KEY (maNV)     REFERENCES NhanVien(maNV),
    CONSTRAINT FK_Ca_KhuVuc      FOREIGN KEY (maKhuVuc) REFERENCES KhuVuc(maKhuVuc),
    CONSTRAINT CHK_Ca_TrangThai  CHECK (trangThai IN ('DANG_LAM','DA_DONG')),
    CONSTRAINT CHK_Ca_Gio        CHECK (gioKetThuc IS NULL OR gioKetThuc > gioBatDau)
);
GO

-- 4. Mon
CREATE TABLE Mon (
    maMon     VARCHAR(20)   NOT NULL,
    tenMon    NVARCHAR(100) NOT NULL,
    moTa      NVARCHAR(500) NULL,
    hinhAnh   NVARCHAR(255) NULL,
    loaiMon   VARCHAR(10)   NOT NULL DEFAULT 'DO_UONG',
    trangThai BIT           NOT NULL DEFAULT 1,

    CONSTRAINT PK_Mon       PRIMARY KEY (maMon),
    CONSTRAINT CHK_Mon_Loai CHECK (loaiMon IN ('DO_UONG','DO_AN'))
);
GO

-- 5. Size
CREATE TABLE Size (
    maSize  VARCHAR(20)  NOT NULL,
    tenSize NVARCHAR(10) NOT NULL,
    maMon   VARCHAR(20)  NOT NULL,

    CONSTRAINT PK_Size      PRIMARY KEY (maSize),
    CONSTRAINT FK_Size_Mon  FOREIGN KEY (maMon) REFERENCES Mon(maMon) ON DELETE CASCADE,
    CONSTRAINT UQ_Size_Mon  UNIQUE (maMon, tenSize)
);
GO

-- 6. Topping
CREATE TABLE Topping (
    maTopping  VARCHAR(20)   NOT NULL,
    tenTopping NVARCHAR(100) NOT NULL,
    giaTopping DECIMAL(10,2) NOT NULL DEFAULT 0,
    trangThai  BIT           NOT NULL DEFAULT 1,

    CONSTRAINT PK_Topping      PRIMARY KEY (maTopping),
    CONSTRAINT CHK_Topping_Gia CHECK (giaTopping >= 0)
);
GO

-- 7. BangGia
CREATE TABLE BangGia (
    maBangGia   VARCHAR(20)   NOT NULL,
    tenBangGia  NVARCHAR(100) NOT NULL,
    ngayBatDau  DATE          NOT NULL,
    ngayKetThuc DATE          NOT NULL,
    trangThai   BIT           NOT NULL DEFAULT 1,

    CONSTRAINT PK_BangGia   PRIMARY KEY (maBangGia),
    CONSTRAINT CHK_BG_Ngay  CHECK (ngayKetThuc >= ngayBatDau)
);
GO

-- 8. BangGiaChiTiet
CREATE TABLE BangGiaChiTiet (
    maBGCT     VARCHAR(20)   NOT NULL,
    giaBan     DECIMAL(10,2) NOT NULL,
    maSize     VARCHAR(20)   NOT NULL,
    maBangGia  VARCHAR(20)   NOT NULL,

    CONSTRAINT PK_BangGiaChiTiet   PRIMARY KEY (maBGCT),
    CONSTRAINT FK_BGCT_Size        FOREIGN KEY (maSize)    REFERENCES Size(maSize),
    CONSTRAINT FK_BGCT_BangGia     FOREIGN KEY (maBangGia) REFERENCES BangGia(maBangGia),
    CONSTRAINT CHK_BGCT_Gia        CHECK (giaBan >= 0),
    CONSTRAINT UQ_BGCT_Size_BangGia UNIQUE (maSize, maBangGia)
);
GO

-- 9. NguyenLieu
CREATE TABLE NguyenLieu (
    maNL        VARCHAR(20)   NOT NULL,
    tenNL       NVARCHAR(100) NOT NULL,
    donViTinh   NVARCHAR(20)  NOT NULL,
    donGiaNhap  DECIMAL(10,2) NOT NULL DEFAULT 0,
    ngayHetHan  DATE          NULL,

    CONSTRAINT PK_NguyenLieu  PRIMARY KEY (maNL),
    CONSTRAINT CHK_NL_DonGia  CHECK (donGiaNhap >= 0)
);
GO

-- 10. DinhMucNguyenLieu
CREATE TABLE DinhMucNguyenLieu (
    maDinhMuc VARCHAR(20)   NOT NULL,
    soLuong   DECIMAL(10,3) NOT NULL,
    maMon     VARCHAR(20)   NOT NULL,
    maNL      VARCHAR(20)   NOT NULL,

    CONSTRAINT PK_DinhMuc        PRIMARY KEY (maDinhMuc),
    CONSTRAINT FK_DinhMuc_Mon    FOREIGN KEY (maMon) REFERENCES Mon(maMon) ON DELETE CASCADE,
    CONSTRAINT FK_DinhMuc_NL     FOREIGN KEY (maNL)  REFERENCES NguyenLieu(maNL),
    CONSTRAINT CHK_DinhMuc_SL    CHECK (soLuong > 0),
    CONSTRAINT UQ_DinhMuc_Mon_NL UNIQUE (maMon, maNL)
);
GO

-- 11. Ban
CREATE TABLE Ban (
    maBan     VARCHAR(20)  NOT NULL,
    soBan     NVARCHAR(20) NOT NULL,
    maKhuVuc  VARCHAR(20)  NOT NULL,
    sucChua   INT          NOT NULL DEFAULT 4,
    trangThai VARCHAR(20)  NOT NULL DEFAULT 'TRONG',

    CONSTRAINT PK_Ban            PRIMARY KEY (maBan),
    CONSTRAINT FK_Ban_KhuVuc     FOREIGN KEY (maKhuVuc) REFERENCES KhuVuc(maKhuVuc),
    CONSTRAINT UQ_Ban_SoBan      UNIQUE (soBan),
    CONSTRAINT CHK_Ban_TrangThai CHECK (trangThai IN ('TRONG','DA_DAT_TRUOC','CO_KHACH')),
    CONSTRAINT CHK_Ban_SucChua   CHECK (sucChua > 0)
);
GO

-- 12. NhaCungCap
CREATE TABLE NhaCungCap (
    maNCC       VARCHAR(20)   NOT NULL,
    tenNCC      NVARCHAR(100) NOT NULL,
    diaChi      NVARCHAR(255) NULL,
    soDienThoai VARCHAR(15)   NULL,
    email       VARCHAR(100)  NULL,
    CONSTRAINT PK_NhaCungCap PRIMARY KEY (maNCC)
);
GO

-- 13. Kho
CREATE TABLE Kho (
    maKho  VARCHAR(20)   NOT NULL,
    tenKho NVARCHAR(100) NOT NULL,
    diaChi NVARCHAR(255) NULL,
    maNV   VARCHAR(20)   NULL,

    CONSTRAINT PK_Kho          PRIMARY KEY (maKho),
    CONSTRAINT FK_Kho_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
GO

-- 14. PhieuNhap
CREATE TABLE PhieuNhap (
    maPN     VARCHAR(20)   NOT NULL,
    ngayNhap DATETIME      NOT NULL DEFAULT GETDATE(),
    tongTien DECIMAL(12,2) NOT NULL DEFAULT 0,
    maNV     VARCHAR(20)   NOT NULL,
    maNCC    VARCHAR(20)   NOT NULL,
    maKho    VARCHAR(20)   NOT NULL,

    CONSTRAINT PK_PhieuNhap   PRIMARY KEY (maPN),
    CONSTRAINT FK_PN_NhanVien FOREIGN KEY (maNV)  REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PN_NCC      FOREIGN KEY (maNCC) REFERENCES NhaCungCap(maNCC),
    CONSTRAINT FK_PN_Kho      FOREIGN KEY (maKho) REFERENCES Kho(maKho)
);
GO

-- 15. ChiTietPhieuNhap
CREATE TABLE ChiTietPhieuNhap (
    maCTPN    VARCHAR(20)   NOT NULL,
    soLuong   DECIMAL(10,3) NOT NULL,
    donGia    DECIMAL(10,2) NOT NULL,
    thanhTien DECIMAL(12,2) NOT NULL DEFAULT 0,
    maPN      VARCHAR(20)   NOT NULL,
    maNL      VARCHAR(20)   NOT NULL,

    CONSTRAINT PK_CTPN      PRIMARY KEY (maCTPN),
    CONSTRAINT FK_CTPN_PN   FOREIGN KEY (maPN) REFERENCES PhieuNhap(maPN) ON DELETE CASCADE,
    CONSTRAINT FK_CTPN_NL   FOREIGN KEY (maNL) REFERENCES NguyenLieu(maNL),
    CONSTRAINT CHK_CTPN_SL  CHECK (soLuong > 0),
    CONSTRAINT CHK_CTPN_Gia CHECK (donGia >= 0)
);
GO

-- 16. TonKho
CREATE TABLE TonKho (
    maTonKho    VARCHAR(20)   NOT NULL,
    soLuongTon  DECIMAL(10,3) NOT NULL DEFAULT 0,
    mucToiThieu DECIMAL(10,3) NOT NULL DEFAULT 0,
    ngayCapNhat DATETIME      NOT NULL DEFAULT GETDATE(),
    maKho       VARCHAR(20)   NOT NULL,
    maNL        VARCHAR(20)   NOT NULL,

    CONSTRAINT PK_TonKho      PRIMARY KEY (maTonKho),
    CONSTRAINT FK_TK_Kho      FOREIGN KEY (maKho) REFERENCES Kho(maKho) ON DELETE CASCADE,
    CONSTRAINT FK_TK_NL       FOREIGN KEY (maNL)  REFERENCES NguyenLieu(maNL),
    CONSTRAINT UQ_TK_Kho_NL   UNIQUE (maKho, maNL),
    CONSTRAINT CHK_TK_SoLuong CHECK (soLuongTon  >= 0),
    CONSTRAINT CHK_TK_MucMin  CHECK (mucToiThieu >= 0)
);
GO

-- 17. HoaDon (Luu tru vinh vien khi khach thanh toan)
-- Chua truc tiep maBan, maCa, loaiDon, ghiChu (truoc day nam trong DonHang)
CREATE TABLE HoaDon (
    maHD              VARCHAR(20)   NOT NULL,
    thoiGianXuat      DATETIME      NOT NULL DEFAULT GETDATE(),
    thoiGianThanhToan DATETIME      NULL,
    tongTienPhaiTra   DECIMAL(12,2) NOT NULL,
    trangThai         VARCHAR(20)   NOT NULL DEFAULT 'CHUA_THANH_TOAN',
    hinhThucThanhToan VARCHAR(20)   NULL,
    maBan             VARCHAR(20)   NULL,       -- FK Ban (NULL neu mang ve)
    maCa              VARCHAR(20)   NOT NULL,   -- FK CaLamViec
    loaiDon           VARCHAR(10)   NOT NULL DEFAULT 'TAI_BAN',
    ghiChu            NVARCHAR(500) NULL,
    maNV              VARCHAR(20)   NOT NULL,   -- Thu Ngan

    CONSTRAINT PK_HoaDon         PRIMARY KEY (maHD),
    CONSTRAINT FK_HD_Ban         FOREIGN KEY (maBan)  REFERENCES Ban(maBan),
    CONSTRAINT FK_HD_Ca          FOREIGN KEY (maCa)   REFERENCES CaLamViec(maCa),
    CONSTRAINT FK_HD_NhanVien    FOREIGN KEY (maNV)   REFERENCES NhanVien(maNV),
    CONSTRAINT CHK_HD_TrangThai  CHECK (trangThai IN ('CHUA_THANH_TOAN','DA_THANH_TOAN')),
    CONSTRAINT CHK_HD_HinhThuc   CHECK (hinhThucThanhToan IN ('TIEN_MAT','CHUYEN_KHOAN') OR hinhThucThanhToan IS NULL),
    CONSTRAINT CHK_HD_LoaiDon    CHECK (loaiDon IN ('TAI_BAN','MANG_VE'))
);
GO

-- 18. ChiTietHoaDon (Chi tiet cac mon trong hoa don - luu DB)
CREATE TABLE ChiTietHoaDon (
    maCTHD    VARCHAR(20)   NOT NULL,
    soLuong   INT           NOT NULL DEFAULT 1,
    donGia    DECIMAL(10,2) NOT NULL,
    thanhTien DECIMAL(12,2) NOT NULL DEFAULT 0,
    ghiChu    NVARCHAR(255) NULL,
    maHD      VARCHAR(20)   NOT NULL,
    maMon     VARCHAR(20)   NOT NULL,
    maSize    VARCHAR(20)   NOT NULL,

    CONSTRAINT PK_CTHD      PRIMARY KEY (maCTHD),
    CONSTRAINT FK_CTHD_HD   FOREIGN KEY (maHD)   REFERENCES HoaDon(maHD) ON DELETE CASCADE,
    CONSTRAINT FK_CTHD_Mon  FOREIGN KEY (maMon)  REFERENCES Mon(maMon),
    CONSTRAINT FK_CTHD_Size FOREIGN KEY (maSize)  REFERENCES Size(maSize)
);
GO

-- 19. ChiTietHoaDonTopping
CREATE TABLE ChiTietHoaDonTopping (
    maID       VARCHAR(20)   NOT NULL,
    soLuong    INT           NOT NULL DEFAULT 1,
    giaTopping DECIMAL(10,2) NOT NULL,
    maCTHD     VARCHAR(20)   NOT NULL,
    maTopping  VARCHAR(20)   NOT NULL,

    CONSTRAINT PK_CTHDT         PRIMARY KEY (maID),
    CONSTRAINT FK_CTHDT_CTHD    FOREIGN KEY (maCTHD)    REFERENCES ChiTietHoaDon(maCTHD) ON DELETE CASCADE,
    CONSTRAINT FK_CTHDT_Topping FOREIGN KEY (maTopping) REFERENCES Topping(maTopping)
);
GO

-- 20. DatBan (Lien ket voi HoaDon thay vi DonHang)
CREATE TABLE DatBan (
    maDatBan     VARCHAR(20)   NOT NULL,
    tenKhach     NVARCHAR(100) NOT NULL,
    soDienThoai  VARCHAR(15)   NULL,
    soLuongNguoi INT           NOT NULL DEFAULT 1,
    trangThai    VARCHAR(20)   NOT NULL DEFAULT 'CHO_XAC_NHAN',
    thoiGianDen  DATETIME      NOT NULL,
    thoiGianDat  DATETIME      NOT NULL DEFAULT GETDATE(),
    maBan        VARCHAR(20)   NOT NULL,
    maHD         VARCHAR(20)   NULL,

    CONSTRAINT PK_DatBan        PRIMARY KEY (maDatBan),
    CONSTRAINT FK_DB_Ban        FOREIGN KEY (maBan) REFERENCES Ban(maBan),
    CONSTRAINT FK_DB_HoaDon     FOREIGN KEY (maHD)  REFERENCES HoaDon(maHD),
    CONSTRAINT CHK_DB_TrangThai CHECK (trangThai IN ('CHO_XAC_NHAN','DA_XAC_NHAN','DA_DEN','HET_HAN','DA_HUY')),
    CONSTRAINT CHK_DB_SoNguoi  CHECK (soLuongNguoi > 0),
    CONSTRAINT CHK_DB_ThoiGian CHECK (thoiGianDen > thoiGianDat)
);
GO

-- ================================================================
-- INDEXES
-- ================================================================
CREATE INDEX IX_HoaDon_ThoiGian       ON HoaDon (thoiGianXuat, maNV);
CREATE INDEX IX_HoaDon_Ca             ON HoaDon (maCa, trangThai);
CREATE INDEX IX_HoaDon_Ban            ON HoaDon (maBan, trangThai);
CREATE INDEX IX_CaLamViec_NhanVien    ON CaLamViec (maNV, trangThai);
CREATE INDEX IX_TonKho_SapHet         ON TonKho (soLuongTon, mucToiThieu, maKho);
CREATE INDEX IX_DatBan_ThoiGian       ON DatBan (trangThai, thoiGianDen);
CREATE INDEX IX_BangGia_HieuLuc       ON BangGia (trangThai, ngayBatDau, ngayKetThuc);
CREATE INDEX IX_CTHD_Mon              ON ChiTietHoaDon (maMon);
CREATE INDEX IX_Ban_KhuVuc            ON Ban (maKhuVuc, trangThai);
GO

PRINT 'Thiet lap Database v10 Thanh Cong!';
PRINT 'DonHang/ChiTietDonHang chi luu tren RAM (khong co trong DB)';
PRINT 'ChiTietHoaDon/ChiTietHoaDonTopping duoc luu vao DB khi thanh toan';