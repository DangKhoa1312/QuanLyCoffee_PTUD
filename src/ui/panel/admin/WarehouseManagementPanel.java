package ui.panel.admin;

import controller.KhoController;
import entity.*;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import utils.CurrencyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel Quan Ly Kho — Giao dien admin voi 3 tab:
 *  1. Ton Kho  — xem chi tiet, loc trang thai
 *  2. Phieu Nhap — xem phieu + tao phieu moi + quan ly NL / NCC
 *  3. Phieu Xuat — xem phieu xuat + tao phieu xuat
 */
public class WarehouseManagementPanel extends JPanel {

    private final KhoController controller = new KhoController();

    private final Color PRIMARY_COLOR = new Color(52, 73, 94);
    private final Color BG_COLOR      = new Color(245, 247, 250);
    private final Color WARN_COLOR    = new Color(231, 76, 60);
    private final Color OK_COLOR      = new Color(39, 174, 96);

    // -- Tab: Ton Kho --
    private DefaultTableModel tonKhoModel;
    private JTable tonKhoTable;
    private JTextField txtSearchTonKho;
    private JComboBox<String> cbFilterTrangThai;

    // -- Tab: Phieu Nhap --
    private DefaultTableModel pnModel;
    private JTable pnTable;
    private JTextField txtSearchPN;

    // -- Tab: Phieu Xuat --
    private DefaultTableModel pxModel;
    private JTable pxTable;
    private JTextField txtSearchPX;

    public WarehouseManagementPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(15, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);

        loadAllData();
    }

    // ===============================================================
    // HEADER
    // ===============================================================
    private JPanel buildHeader() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);

        JPanel bc = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bc.setOpaque(false);
        JLabel lbcGray = new JLabel("Admin / Qu\u1EA3n Tr\u1ECB / ");
        lbcGray.setForeground(Color.GRAY);
        lbcGray.setFont(new Font("Roboto", Font.PLAIN, 13));
        JLabel lbcCurrent = new JLabel("Qu\u1EA3n L\u00FD Kho");
        lbcCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lbcCurrent.setForeground(PRIMARY_COLOR);
        bc.add(lbcGray); bc.add(lbcCurrent);
        pnl.add(bc);
        pnl.add(Box.createVerticalStrut(8));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel title = new JLabel("QU\u1EA2N L\u00DD KHO H\u00C0NG");
        title.setFont(new Font("Roboto", Font.BOLD, 24));
        title.setForeground(new Color(44, 62, 80));
        title.setIcon(IconFontSwing.buildIcon(FontAwesome.ARCHIVE, 28, PRIMARY_COLOR));
        title.setIconTextGap(12);
        titleBar.add(title, BorderLayout.WEST);
        pnl.add(titleBar);
        pnl.add(Box.createVerticalStrut(10));
        return pnl;
    }

    // ===============================================================
    // TABS
    // ===============================================================
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Roboto", Font.BOLD, 13));
        tabs.setBackground(Color.WHITE);
        tabs.addTab("  \uD83D\uDCE6 T\u1ED3n Kho  ",      buildTonKhoTab());
        tabs.addTab("  \uD83D\uDCCB Phi\u1EBFu Nh\u1EADp  ",   buildPhieuNhapTab());
        tabs.addTab("  \uD83D\uDCE4 Phi\u1EBFu Xu\u1EA5t  ",   buildPhieuXuatTab());
        return tabs;
    }

    // ===============================================================
    // TAB 1: TON KHO — xem chi tiet + loc trang thai
    // ===============================================================
    private JPanel buildTonKhoTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 5, 5));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(new Color(230, 230, 230)));

        // Left: Search + Filter Trạng Thái
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        leftPanel.setOpaque(false);

        leftPanel.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY)));
        txtSearchTonKho = new JTextField(18);
        txtSearchTonKho.setPreferredSize(new Dimension(0, 35));
        txtSearchTonKho.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtSearchTonKho.setToolTipText("Tìm kiếm nguyên liệu...");
        txtSearchTonKho.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadTonKhoData(); }
        });
        leftPanel.add(txtSearchTonKho);

        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.FILTER, 16, Color.GRAY)));
        leftPanel.add(new JLabel("Trạng thái:"));
        cbFilterTrangThai = new JComboBox<>(new String[]{"Tất cả", "Bình thường", "Sắp hết", "Hết hàng"});
        cbFilterTrangThai.setPreferredSize(new Dimension(150, 35));
        cbFilterTrangThai.setFont(new Font("Roboto", Font.PLAIN, 14));
        cbFilterTrangThai.addActionListener(e -> loadTonKhoData());
        leftPanel.add(cbFilterTrangThai);

        topBar.add(leftPanel, BorderLayout.WEST);
        p.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Mã Nguyên Liệu", "Tên Nguyên Liệu", "Đơn Vị Đóng Gói",
                         "Tồn Kho", "Mức Tối Thiểu", "Ngày Cập Nhật", "Trạng Thái"};
        tonKhoModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tonKhoTable = buildStyledTable(tonKhoModel);
        tonKhoTable.setDefaultRenderer(Object.class, new TonKhoRenderer());
        tonKhoTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        tonKhoTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        tonKhoTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        // Double-click -> xem chi tiet
        tonKhoTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tonKhoTable.getSelectedRow();
                    if (row >= 0) viewTonKhoDetailDialog(row);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tonKhoTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("  Nh\u1EA5p \u0111\u00FAp v\u00E0o h\u00E0ng \u0111\u1EC3 xem chi ti\u1EBFt nguy\u00EAn li\u1EC7u");
        hint.setFont(new Font("Roboto", Font.ITALIC, 12));
        hint.setForeground(Color.GRAY);
        hint.setBorder(new EmptyBorder(5, 5, 0, 0));
        p.add(hint, BorderLayout.SOUTH);

        return p;
    }

    // ===============================================================
    // TAB 2: PHIEU NHAP — xem phieu + tao phieu moi + nut NL/NCC
    // ===============================================================
    private JPanel buildPhieuNhapTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 5, 5));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new LineBorder(new Color(230, 230, 230)));

        // Left: Search
        JPanel leftPN = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        leftPN.setOpaque(false);
        leftPN.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY)));
        txtSearchPN = new JTextField(18);
        txtSearchPN.setPreferredSize(new Dimension(0, 35));
        txtSearchPN.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtSearchPN.setToolTipText("Tìm kiếm phiếu nhập...");
        txtSearchPN.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadPhieuNhapData(); }
        });
        leftPN.add(txtSearchPN);
        bar.add(leftPN, BorderLayout.WEST);

        // Right: Buttons
        JPanel rightPN = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightPN.setOpaque(false);

        // Nut Nguyen Lieu
        JButton btnNguyenLieu = createSmallBtn("  Nguyên Liệu", FontAwesome.LEAF, new Color(52, 152, 219));
        btnNguyenLieu.setPreferredSize(new Dimension(180, 40));
        btnNguyenLieu.addActionListener(e -> openNguyenLieuListDialog());

        // Nut Nha Cung Cap
        JButton btnNhaCungCap = createSmallBtn("  Nhà Cung Cấp", FontAwesome.TRUCK, new Color(155, 89, 182));
        btnNhaCungCap.setPreferredSize(new Dimension(190, 40));
        btnNhaCungCap.addActionListener(e -> openNhaCungCapListDialog());

        JButton btnRefresh = createSmallBtn("Làm mới", FontAwesome.REFRESH, new Color(220, 220, 220));
        btnRefresh.addActionListener(e -> loadPhieuNhapData());

        JButton btnNhapHang = createSmallBtn("  Tạo Phiếu Nhập", FontAwesome.DOWNLOAD, new Color(46, 204, 113));
        btnNhapHang.setPreferredSize(new Dimension(200, 40));
        btnNhapHang.addActionListener(e -> handleTaoPhieuNhap());

        rightPN.add(btnNguyenLieu);
        rightPN.add(btnNhaCungCap);
        rightPN.add(btnRefresh);
        rightPN.add(btnNhapHang);
        bar.add(rightPN, BorderLayout.EAST);

        p.add(bar, BorderLayout.NORTH);

        String[] cols = {"Mã Phiếu", "Ngày Nhập", "Nhà Cung Cấp", "Nhân Viên", "Tổng Tiền"};
        pnModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pnTable = buildStyledTable(pnModel);
        pnTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        pnTable.getColumnModel().getColumn(1).setPreferredWidth(155);
        pnTable.getColumnModel().getColumn(4).setPreferredWidth(130);

        pnTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) viewPhieuNhapDetail();
            }
        });

        JScrollPane scroll = new JScrollPane(pnTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("  Nhấp đúp vào phiếu để xem chi tiết");
        hint.setFont(new Font("Roboto", Font.ITALIC, 12));
        hint.setForeground(Color.GRAY);
        hint.setBorder(new EmptyBorder(5, 5, 0, 0));
        p.add(hint, BorderLayout.SOUTH);
        return p;
    }

    // ===============================================================
    // TAB 3: PHIEU XUAT — xem phieu xuat + tao phieu xuat
    // ===============================================================
    private JPanel buildPhieuXuatTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 5, 5));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new LineBorder(new Color(230, 230, 230)));

        // Left: empty spacer
        JPanel leftPX = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        leftPX.setOpaque(false);
        bar.add(leftPX, BorderLayout.WEST);

        // Right: Buttons
        JPanel rightPX = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightPX.setOpaque(false);

        JButton btnLichSu = createSmallBtn("  Lịch sử phiếu xuất", FontAwesome.HISTORY, new Color(52, 152, 219));
        btnLichSu.setPreferredSize(new Dimension(210, 35));
        btnLichSu.addActionListener(e -> openLichSuPhieuXuat());

        JButton btnRefresh = createSmallBtn("Làm mới", FontAwesome.REFRESH, new Color(220, 220, 220));
        btnRefresh.addActionListener(e -> loadPhieuXuatData());

        JButton btnXuatKho = createSmallBtn("  Tạo Phiếu Xuất", FontAwesome.UPLOAD, new Color(231, 76, 60));
        btnXuatKho.setPreferredSize(new Dimension(180, 35));
        btnXuatKho.addActionListener(e -> handleTaoPhieuXuat());

        rightPX.add(btnLichSu);
        rightPX.add(btnRefresh);
        rightPX.add(btnXuatKho);
        bar.add(rightPX, BorderLayout.EAST);

        p.add(bar, BorderLayout.NORTH);

        // Bảng nguyên liệu xuất kho (bắt đầu rỗng, cộng dữ liệu khi tạo phiếu xuất)
        String[] cols = {"Mã Nguyên Liệu", "Tên Nguyên Liệu", "Đơn Vị Tính", "Số Lượng Xuất"};
        pxModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pxTable = buildStyledTable(pxModel);
        pxTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        pxTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        pxTable.getColumnModel().getColumn(1).setPreferredWidth(250);

        JScrollPane scroll = new JScrollPane(pxTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("  Khi tạo phiếu xuất, nguyên liệu xuất sẽ được cộng vào bảng này");
        hint.setFont(new Font("Roboto", Font.ITALIC, 12));
        hint.setForeground(Color.GRAY);
        hint.setBorder(new EmptyBorder(5, 5, 0, 0));
        p.add(hint, BorderLayout.SOUTH);
        return p;
    }

    // ===============================================================
    // LOAD DATA
    // ===============================================================
    private void loadAllData() {
        loadTonKhoData();
        loadPhieuNhapData();
        loadPhieuXuatData();
    }

    private void loadTonKhoData() {
        tonKhoModel.setRowCount(0);
        List<TonKho> list = controller.getAllTonKho();
        String filterTrangThai = (String) cbFilterTrangThai.getSelectedItem();
        String searchKw = txtSearchTonKho.getText().toLowerCase().trim();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (TonKho tk : list) {
            NguyenLieu nl = controller.getNguyenLieuById(tk.getMaNL());
            String tenNL  = nl != null ? nl.getTenNL() : tk.getMaNL();
            String dvdg   = nl != null && nl.getDonViDongGoi() != null ? nl.getDonViDongGoi() : "";

            // Tìm kiếm theo tên hoặc mã nguyên liệu
            if (!searchKw.isEmpty()) {
                if (!tenNL.toLowerCase().contains(searchKw) && !tk.getMaNL().toLowerCase().contains(searchKw)) {
                    continue;
                }
            }

            // Xác định trạng thái
            String status;
            if (tk.getSoLuongTon() <= 0) {
                status = "Hết hàng";
            } else if (tk.isSapHet()) {
                status = "Sắp hết";
            } else {
                status = "Bình thường";
            }

            // Lọc theo trạng thái
            if (filterTrangThai != null && !"Tất cả".equals(filterTrangThai)) {
                if (!status.equals(filterTrangThai)) continue;
            }

            String ngayCN = tk.getNgayCapNhat() != null ? tk.getNgayCapNhat().format(fmt) : "";
            String statusDisplay;
            if ("Hết hàng".equals(status)) {
                statusDisplay = "\u274C Hết hàng";
            } else if ("Sắp hết".equals(status)) {
                statusDisplay = "\u26A0 Sắp hết";
            } else {
                statusDisplay = "\u2713 Bình thường";
            }

            // TonKho da luu theo don vi dong goi, hien thi truc tiep
            tonKhoModel.addRow(new Object[]{
                tk.getMaNL(), tenNL, dvdg,
                String.format("%.1f", tk.getSoLuongTon()),
                String.format("%.1f", tk.getMucToiThieu()),
                ngayCN, statusDisplay
            });
        }
    }

    private void loadPhieuNhapData() {
        pnModel.setRowCount(0);
        String searchKw = txtSearchPN != null ? txtSearchPN.getText().toLowerCase().trim() : "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (PhieuNhap pn : controller.getAllPhieuNhap()) {
            NhaCungCap ncc = controller.getNhaCungCapById(pn.getMaNCC());
            String tenNCC = ncc != null ? ncc.getTenNCC() : pn.getMaNCC();
            String maPN = pn.getMaPN();
            String maNV = pn.getMaNV();

            if (!searchKw.isEmpty()) {
                if (!maPN.toLowerCase().contains(searchKw)
                    && !tenNCC.toLowerCase().contains(searchKw)
                    && !maNV.toLowerCase().contains(searchKw)) {
                    continue;
                }
            }

            pnModel.addRow(new Object[]{
                maPN,
                pn.getNgayNhap() != null ? pn.getNgayNhap().format(fmt) : "",
                tenNCC,
                maNV,
                CurrencyUtils.formatNoUnit(pn.getTongTien()) + " đ"
            });
        }
    }

    private void loadPhieuXuatData() {
        pxModel.setRowCount(0);
        // 1. Load tat ca nguyen lieu voi so luong xuat = 0
        for (NguyenLieu nl : controller.getAllNguyenLieu()) {
            pxModel.addRow(new Object[]{
                nl.getMaNL(),
                nl.getTenNL(),
                nl.getDonViTinh() != null ? nl.getDonViTinh() : "",
                0.0  // luu Double truc tiep, khong format String
            });
        }
        // 2. Cong tong so luong xuat tu tat ca phieu xuat trong DB
        //    So luong xuat = soLuong * khoiLuongDongGoi
        for (PhieuXuat px : controller.getAllPhieuXuat()) {
            List<ChiTietPhieuXuat> chiTiet = controller.getChiTietByPhieuXuat(px.getMaPX());
            for (ChiTietPhieuXuat ct : chiTiet) {
                NguyenLieu nl = controller.getNguyenLieuById(ct.getMaNL());
                double klDG = nl != null && nl.getKhoiLuongDongGoi() > 0 ? nl.getKhoiLuongDongGoi() : 1;
                double soLuongHienThi = ct.getSoLuong() * klDG;
                for (int i = 0; i < pxModel.getRowCount(); i++) {
                    if (ct.getMaNL().equals(pxModel.getValueAt(i, 0))) {
                        double existing = ((Number) pxModel.getValueAt(i, 3)).doubleValue();
                        pxModel.setValueAt(existing + soLuongHienThi, i, 3);
                        break;
                    }
                }
            }
        }
    }

    // ===============================================================
    // HANDLERS — TON KHO
    // ===============================================================

    private void viewTonKhoDetailDialog(int row) {
        String maNL     = (String) tonKhoModel.getValueAt(row, 0);
        String tenNL    = (String) tonKhoModel.getValueAt(row, 1);
        String dvdg     = (String) tonKhoModel.getValueAt(row, 2);
        Object tonObj   = tonKhoModel.getValueAt(row, 3);
        Object mucTTObj = tonKhoModel.getValueAt(row, 4);
        String ngayCN   = (String) tonKhoModel.getValueAt(row, 5);
        String trangThai = (String) tonKhoModel.getValueAt(row, 6);

        String tonKho = tonObj != null ? String.valueOf(tonObj) : "0";
        String mucTT  = mucTTObj != null ? String.valueOf(mucTTObj) : "0";

        // Lấy thêm thông tin từ NguyenLieu
        NguyenLieu nl = controller.getNguyenLieuById(maNL);
        String donGia = nl != null ? CurrencyUtils.formatNoUnit(nl.getDonGiaNhap()) + " đ" : "";
        String klDongGoi = nl != null && nl.getKhoiLuongDongGoi() > 0 ? String.valueOf((long) nl.getKhoiLuongDongGoi()) : "--";
        String ngayHH = "";
        if (nl != null && nl.getNgayHetHan() != null) {
            ngayHH = nl.getNgayHetHan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        // Tạo JDialog
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Chi Tiết Tồn Kho: " + tenNL, true);
        dlg.setSize(550, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 10));
        dlg.getContentPane().setBackground(new Color(245, 247, 250));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(0, 50));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel lblTitle = new JLabel("  CHI TIẾT TỒN KHO");
        lblTitle.setIcon(IconFontSwing.buildIcon(FontAwesome.CUBES, 22, Color.WHITE));
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);
        dlg.add(header, BorderLayout.NORTH);

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 15, 8));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(20, 25, 20, 25)
        ));

        infoPanel.add(createStyledLabel("Mã Nguyên Liệu:", true));
        infoPanel.add(createStyledLabel(maNL, false));

        infoPanel.add(createStyledLabel("Tên Nguyên Liệu:", true));
        infoPanel.add(createStyledLabel(tenNL, false));

        infoPanel.add(createStyledLabel("Đơn Vị Đóng Gói:", true));
        infoPanel.add(createStyledLabel(dvdg, false));

        infoPanel.add(createStyledLabel("KL Đóng Gói:", true));
        infoPanel.add(createStyledLabel(klDongGoi, false));

        infoPanel.add(createStyledLabel("Đơn Giá Nhập:", true));
        infoPanel.add(createStyledLabel(donGia, false));

        infoPanel.add(createStyledLabel("Số Lượng Tồn (" + (dvdg.isEmpty() ? "đvdg" : dvdg) + "):", true));
        JLabel lblTon = createStyledLabel(tonKho, false);
        lblTon.setFont(new Font("Roboto", Font.BOLD, 14));
        lblTon.setForeground(trangThai.contains("Sắp hết") || trangThai.contains("Hết hàng") ? WARN_COLOR : OK_COLOR);
        infoPanel.add(lblTon);

        infoPanel.add(createStyledLabel("Mức Tối Thiểu (" + (dvdg.isEmpty() ? "đvdg" : dvdg) + "):", true));
        infoPanel.add(createStyledLabel(mucTT, false));

        infoPanel.add(createStyledLabel("Ngày Hết Hạn:", true));
        infoPanel.add(createStyledLabel(ngayHH.isEmpty() ? "--" : ngayHH, false));

        infoPanel.add(createStyledLabel("Ngày Cập Nhật:", true));
        infoPanel.add(createStyledLabel(ngayCN.isEmpty() ? "--" : ngayCN, false));

        infoPanel.add(createStyledLabel("Trạng Thái:", true));
        JLabel lblStatus = createStyledLabel(trangThai, false);
        lblStatus.setFont(new Font("Roboto", Font.BOLD, 13));
        lblStatus.setForeground(trangThai.contains("Sắp hết") || trangThai.contains("Hết hàng") ? WARN_COLOR : OK_COLOR);
        infoPanel.add(lblStatus);

        JPanel infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setOpaque(false);
        infoWrapper.setBorder(new EmptyBorder(10, 20, 5, 20));
        infoWrapper.add(infoPanel, BorderLayout.CENTER);
        dlg.add(infoWrapper, BorderLayout.CENTER);

        // Nut dong
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        btnPanel.setOpaque(false);
        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setFont(new Font("Roboto", Font.BOLD, 13));
        btnClose.setBackground(PRIMARY_COLOR);
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dlg.dispose());
        btnPanel.add(btnClose);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ===============================================================
    // HANDLERS — NGUYEN LIEU (popup dialog)
    // ===============================================================
    private void openNguyenLieuListDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Quản Lý Nguyên Liệu", true);
        dlg.setSize(1100, 700);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 10));
        dlg.getContentPane().setBackground(new Color(245, 247, 250));

        // Top bar: search + add
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(8, 15, 8, 15)
        ));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY)));
        JTextField txtSearch = new JTextField(22);
        txtSearch.setPreferredSize(new Dimension(0, 35));
        txtSearch.setFont(new Font("Roboto", Font.PLAIN, 14));
        left.add(txtSearch);
        bar.add(left, BorderLayout.WEST);

        JButton btnAdd = createSmallBtn("  Thêm", FontAwesome.PLUS, new Color(46, 204, 113));
        btnAdd.setPreferredSize(new Dimension(200, 45));
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 15));
        bar.add(btnAdd, BorderLayout.EAST);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.setBorder(new EmptyBorder(10, 10, 0, 10));
        topWrapper.add(bar, BorderLayout.CENTER);
        dlg.add(topWrapper, BorderLayout.NORTH);

        // Table
        String[] cols = {"Mã Nguyên Liệu", "Tên Nguyên Liệu", "Đơn Vị Đóng Gói", "KL Đóng Gói", "Đơn Vị Tính", "Đơn Giá Nhập", "Object"};
        DefaultTableModel nlModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable nlTable = buildStyledTable(nlModel);
        nlTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        nlTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        nlTable.removeColumn(nlTable.getColumnModel().getColumn(6));

        // Load data
        Runnable loadNL = () -> {
            nlModel.setRowCount(0);
            String kw = txtSearch.getText().toLowerCase().trim();
            for (NguyenLieu nl : controller.getAllNguyenLieu()) {
                if (!kw.isEmpty() && !nl.getTenNL().toLowerCase().contains(kw) && !nl.getMaNL().toLowerCase().contains(kw))
                    continue;
                nlModel.addRow(new Object[]{
                    nl.getMaNL(), nl.getTenNL(),
                    nl.getDonViDongGoi() != null ? nl.getDonViDongGoi() : "",
                    nl.getKhoiLuongDongGoi() > 0 ? String.valueOf((long) nl.getKhoiLuongDongGoi()) : "",
                    nl.getDonViTinh(),
                    CurrencyUtils.formatNoUnit(nl.getDonGiaNhap()) + " đ",
                    nl
                });
            }
        };
        loadNL.run();

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadNL.run(); }
        });

        // Add button
        btnAdd.addActionListener(e -> {
            NguyenLieu nl = new NguyenLieu();
            nl.setMaNL(controller.generateNextMaNL());
            ui.dialog.NguyenLieuDialog nlDlg =
                new ui.dialog.NguyenLieuDialog((Frame) SwingUtilities.getWindowAncestor(dlg), nl, false);
            nlDlg.setVisible(true);
            if (nlDlg.isConfirmed()) {
                try {
                    if (controller.addNguyenLieu(nl)) {
                        loadNL.run();
                        loadTonKhoData();
                        JOptionPane.showMessageDialog(dlg, "Thêm nguyên liệu thành công!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Double-click edit
        nlTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = nlTable.getSelectedRow();
                    if (row >= 0) {
                        NguyenLieu nl = (NguyenLieu) nlModel.getValueAt(row, 6);
                        ui.dialog.NguyenLieuDialog nlDlg =
                            new ui.dialog.NguyenLieuDialog((Frame) SwingUtilities.getWindowAncestor(dlg), nl, true);
                        nlDlg.setVisible(true);
                        if (nlDlg.isConfirmed()) {
                            if (nlDlg.isDeleted()) {
                                if (controller.deleteNguyenLieu(nl.getMaNL())) {
                                    loadNL.run();
                                    loadTonKhoData();
                                    JOptionPane.showMessageDialog(dlg, "Đã xóa nguyên liệu!");
                                } else {
                                    JOptionPane.showMessageDialog(dlg, "Không thể xóa (có thể đang được sử dụng)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                }
                            } else {
                                try {
                                    if (controller.updateNguyenLieu(nl)) {
                                        loadNL.run();
                                        loadTonKhoData();
                                        JOptionPane.showMessageDialog(dlg, "Cập nhật thành công!");
                                    }
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(nlTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(5, 10, 5, 10));
        tableWrapper.add(scroll, BorderLayout.CENTER);
        dlg.add(tableWrapper, BorderLayout.CENTER);

        // Bottom
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setOpaque(false);
        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setFont(new Font("Roboto", Font.BOLD, 13));
        btnClose.setBackground(PRIMARY_COLOR);
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dlg.dispose());
        btnPanel.add(btnClose);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ===============================================================
    // HANDLERS — NHA CUNG CAP (popup dialog)
    // ===============================================================
    private void openNhaCungCapListDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Quản Lý Nhà Cung Cấp", true);
        dlg.setSize(950, 650);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 10));
        dlg.getContentPane().setBackground(new Color(245, 247, 250));

        // Top bar
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(8, 15, 8, 15)
        ));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY)));
        JTextField txtSearch = new JTextField(22);
        txtSearch.setPreferredSize(new Dimension(0, 35));
        txtSearch.setFont(new Font("Roboto", Font.PLAIN, 14));
        left.add(txtSearch);
        bar.add(left, BorderLayout.WEST);

        JButton btnAdd = createSmallBtn("  Thêm", FontAwesome.PLUS, new Color(46, 204, 113));
        bar.add(btnAdd, BorderLayout.EAST);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.setBorder(new EmptyBorder(10, 10, 0, 10));
        topWrapper.add(bar, BorderLayout.CENTER);
        dlg.add(topWrapper, BorderLayout.NORTH);

        // Table
        String[] cols = {"Mã Nhà Cung Cấp", "Tên Nhà Cung Cấp", "Số Điện Thoại", "Email", "Địa Chỉ", "Object"};
        DefaultTableModel nccModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable nccTable = buildStyledTable(nccModel);
        nccTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        nccTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        nccTable.removeColumn(nccTable.getColumnModel().getColumn(5));

        // Load data
        Runnable loadNCC = () -> {
            nccModel.setRowCount(0);
            String kw = txtSearch.getText().toLowerCase().trim();
            for (NhaCungCap ncc : controller.getAllNhaCungCap()) {
                if (!kw.isEmpty() && !ncc.getTenNCC().toLowerCase().contains(kw) && !ncc.getMaNCC().toLowerCase().contains(kw))
                    continue;
                nccModel.addRow(new Object[]{
                    ncc.getMaNCC(), ncc.getTenNCC(),
                    ncc.getSoDienThoai() != null ? ncc.getSoDienThoai() : "",
                    ncc.getEmail() != null ? ncc.getEmail() : "",
                    ncc.getDiaChi() != null ? ncc.getDiaChi() : "",
                    ncc
                });
            }
        };
        loadNCC.run();

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadNCC.run(); }
        });

        // Add button
        btnAdd.addActionListener(e -> {
            NhaCungCap ncc = new NhaCungCap();
            ncc.setMaNCC(controller.generateNextMaNCC());
            ui.dialog.NhaCungCapDialog nccDlg =
                new ui.dialog.NhaCungCapDialog((Frame) SwingUtilities.getWindowAncestor(dlg), ncc, false);
            nccDlg.setVisible(true);
            if (nccDlg.isConfirmed()) {
                try {
                    if (controller.addNhaCungCap(ncc)) {
                        loadNCC.run();
                        JOptionPane.showMessageDialog(dlg, "Thêm nhà cung cấp thành công!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Double-click edit
        nccTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = nccTable.getSelectedRow();
                    if (row >= 0) {
                        NhaCungCap ncc = (NhaCungCap) nccModel.getValueAt(row, 5);
                        ui.dialog.NhaCungCapDialog nccDlg =
                            new ui.dialog.NhaCungCapDialog((Frame) SwingUtilities.getWindowAncestor(dlg), ncc, true);
                        nccDlg.setVisible(true);
                        if (nccDlg.isConfirmed()) {
                            if (nccDlg.isDeleted()) {
                                if (controller.deleteNhaCungCap(ncc.getMaNCC())) {
                                    loadNCC.run();
                                    JOptionPane.showMessageDialog(dlg, "Đã xóa nhà cung cấp!");
                                } else {
                                    JOptionPane.showMessageDialog(dlg, "Không thể xóa (có phiếu nhập liên quan)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                }
                            } else {
                                try {
                                    if (controller.updateNhaCungCap(ncc)) {
                                        loadNCC.run();
                                        JOptionPane.showMessageDialog(dlg, "Cập nhật thành công!");
                                    }
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(nccTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(5, 10, 5, 10));
        tableWrapper.add(scroll, BorderLayout.CENTER);
        dlg.add(tableWrapper, BorderLayout.CENTER);

        // Bottom
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setOpaque(false);
        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setFont(new Font("Roboto", Font.BOLD, 13));
        btnClose.setBackground(PRIMARY_COLOR);
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dlg.dispose());
        btnPanel.add(btnClose);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ===============================================================
    // HANDLERS — PHIEU NHAP
    // ===============================================================
    private void handleTaoPhieuNhap() {
        ui.dialog.PhieuNhapDialog dlg = new ui.dialog.PhieuNhapDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadPhieuNhapData();
            loadTonKhoData();
        }
    }

    private void viewPhieuNhapDetail() {
        int row = pnTable.getSelectedRow();
        if (row < 0) return;
        String maPN = (String) pnModel.getValueAt(row, 0);
        String ngayNhap = (String) pnModel.getValueAt(row, 1);
        String tenNCC = (String) pnModel.getValueAt(row, 2);
        String maNV = (String) pnModel.getValueAt(row, 3);
        String tongTien = (String) pnModel.getValueAt(row, 4);
        List<ChiTietPhieuNhap> chiTiet = controller.getChiTietByPhieuNhap(maPN);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi Tiết Phiếu Nhập: " + maPN, true);
        dlg.setSize(700, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 10));
        dlg.getContentPane().setBackground(new Color(245, 247, 250));

        // Header info
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        infoPanel.add(createStyledLabel("Mã Phiếu:", true)); infoPanel.add(createStyledLabel(maPN, false));
        infoPanel.add(createStyledLabel("Ngày Nhập:", true)); infoPanel.add(createStyledLabel(ngayNhap, false));
        infoPanel.add(createStyledLabel("Nhà Cung Cấp:", true)); infoPanel.add(createStyledLabel(tenNCC, false));
        infoPanel.add(createStyledLabel("Nhân Viên:", true)); infoPanel.add(createStyledLabel(maNV, false));
        infoPanel.add(createStyledLabel("Tổng Tiền:", true)); infoPanel.add(createStyledLabel(tongTien, false));

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.setBorder(new EmptyBorder(15, 15, 0, 15));
        topWrapper.add(infoPanel, BorderLayout.CENTER);
        dlg.add(topWrapper, BorderLayout.NORTH);

        // Bang chi tiet
        String[] cols = {"Mã Nguyên Liệu", "Tên Nguyên Liệu", "Số Lượng", "Đơn Giá", "Thành Tiền"};
        DefaultTableModel detailModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (ChiTietPhieuNhap ct : chiTiet) {
            NguyenLieu nl = controller.getNguyenLieuById(ct.getMaNL());
            String tenNL = nl != null ? nl.getTenNL() : ct.getMaNL();
            detailModel.addRow(new Object[]{
                ct.getMaNL(), tenNL,
                String.format("%.2f", ct.getSoLuong()),
                CurrencyUtils.format(ct.getDonGia()),
                CurrencyUtils.format(ct.getThanhTien())
            });
        }
        JTable detailTable = buildStyledTable(detailModel);
        detailTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        detailTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        JScrollPane scroll = new JScrollPane(detailTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(5, 15, 5, 15));
        tableWrapper.add(scroll, BorderLayout.CENTER);
        dlg.add(tableWrapper, BorderLayout.CENTER);

        // Nut dong
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setOpaque(false);
        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setFont(new Font("Roboto", Font.BOLD, 13));
        btnClose.setBackground(PRIMARY_COLOR);
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dlg.dispose());
        btnPanel.add(btnClose);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ===============================================================
    // HANDLERS — PHIEU XUAT
    // ===============================================================
    private void handleTaoPhieuXuat() {
        ui.dialog.PhieuXuatDialog dlg = new ui.dialog.PhieuXuatDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            // Reload bang phieu xuat tu DB (da luu phieu xuat moi)
            loadPhieuXuatData();
            loadTonKhoData();
        }
    }

    private void viewPhieuXuatDetail(String maPX, String ngayXuat, String lyDo, String maNV) {
        List<ChiTietPhieuXuat> chiTiet = controller.getChiTietByPhieuXuat(maPX);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi Tiết Phiếu Xuất: " + maPX, true);
        dlg.setSize(650, 450);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 10));
        dlg.getContentPane().setBackground(new Color(245, 247, 250));

        // Header info
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        infoPanel.add(createStyledLabel("Mã Phiếu:", true)); infoPanel.add(createStyledLabel(maPX, false));
        infoPanel.add(createStyledLabel("Ngày Xuất:", true)); infoPanel.add(createStyledLabel(ngayXuat, false));
        infoPanel.add(createStyledLabel("Lý Do:", true)); infoPanel.add(createStyledLabel(lyDo, false));
        infoPanel.add(createStyledLabel("Nhân Viên:", true)); infoPanel.add(createStyledLabel(maNV, false));

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.setBorder(new EmptyBorder(15, 15, 0, 15));
        topWrapper.add(infoPanel, BorderLayout.CENTER);
        dlg.add(topWrapper, BorderLayout.NORTH);

        // Bang chi tiet
        String[] cols = {"Mã Nguyên Liệu", "Tên Nguyên Liệu", "Đơn Vị Đóng Gói", "Số Lượng Xuất"};
        DefaultTableModel detailModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (ChiTietPhieuXuat ct : chiTiet) {
            NguyenLieu nl = controller.getNguyenLieuById(ct.getMaNL());
            String tenNL = nl != null ? nl.getTenNL() : ct.getMaNL();
            String dvdg = nl != null && nl.getDonViDongGoi() != null ? nl.getDonViDongGoi() : "";
            detailModel.addRow(new Object[]{
                ct.getMaNL(), tenNL, dvdg,
                String.format("%.2f", ct.getSoLuong())
            });
        }
        JTable detailTable = buildStyledTable(detailModel);
        detailTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        detailTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        JScrollPane scroll = new JScrollPane(detailTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(5, 15, 5, 15));
        tableWrapper.add(scroll, BorderLayout.CENTER);
        dlg.add(tableWrapper, BorderLayout.CENTER);

        // Nut dong
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setOpaque(false);
        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setFont(new Font("Roboto", Font.BOLD, 13));
        btnClose.setBackground(PRIMARY_COLOR);
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dlg.dispose());
        btnPanel.add(btnClose);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ===============================================================
    // LỊCH SỪ PHIẾU XUẤT (dialog)
    // ===============================================================
    private void openLichSuPhieuXuat() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Lịch Sử Phiếu Xuất Kho", true);
        dlg.setSize(900, 600);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 10));
        dlg.getContentPane().setBackground(new Color(245, 247, 250));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(0, 50));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel lblTitle = new JLabel("  LỊCH SỪ PHIẾU XUẤT KHO");
        lblTitle.setIcon(IconFontSwing.buildIcon(FontAwesome.HISTORY, 22, Color.WHITE));
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);
        dlg.add(header, BorderLayout.NORTH);

        // Center: search + table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 5));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY)));
        JTextField txtSearch = new JTextField(22);
        txtSearch.setPreferredSize(new Dimension(0, 35));
        txtSearch.setFont(new Font("Roboto", Font.PLAIN, 14));
        searchBar.add(txtSearch);
        centerPanel.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"Mã Phiếu", "Ngày Xuất", "Lý Do", "Nhân Viên", "Số NL"};
        DefaultTableModel historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable historyTable = buildStyledTable(historyModel);
        historyTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(155);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(200);

        Runnable loadHistory = () -> {
            historyModel.setRowCount(0);
            String searchKw = txtSearch.getText().toLowerCase().trim();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (PhieuXuat px : controller.getAllPhieuXuat()) {
                List<ChiTietPhieuXuat> chiTiet = controller.getChiTietByPhieuXuat(px.getMaPX());
                String maPX = px.getMaPX();
                String lyDo = px.getLyDoXuat() != null ? px.getLyDoXuat() : "";
                String maNV = px.getMaNV();
                if (!searchKw.isEmpty()) {
                    if (!maPX.toLowerCase().contains(searchKw)
                        && !lyDo.toLowerCase().contains(searchKw)
                        && !maNV.toLowerCase().contains(searchKw)) {
                        continue;
                    }
                }
                historyModel.addRow(new Object[]{
                    maPX,
                    px.getNgayXuat() != null ? px.getNgayXuat().format(fmt) : "",
                    lyDo, maNV,
                    chiTiet.size() + " nguyên liệu"
                });
            }
        };
        loadHistory.run();

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadHistory.run(); }
        });

        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = historyTable.getSelectedRow();
                    if (row >= 0) {
                        String maPX = (String) historyModel.getValueAt(row, 0);
                        String ngayXuat = (String) historyModel.getValueAt(row, 1);
                        String lyDo = (String) historyModel.getValueAt(row, 2);
                        String maNV = (String) historyModel.getValueAt(row, 3);
                        viewPhieuXuatDetail(maPX, ngayXuat, lyDo, maNV);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        centerPanel.add(scroll, BorderLayout.CENTER);
        dlg.add(centerPanel, BorderLayout.CENTER);

        // Bottom
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setOpaque(false);
        JLabel hint = new JLabel("Nhấp đúp vào phiếu để xem chi tiết  ");
        hint.setFont(new Font("Roboto", Font.ITALIC, 12));
        hint.setForeground(Color.GRAY);
        btnPanel.add(hint);
        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setFont(new Font("Roboto", Font.BOLD, 13));
        btnClose.setBackground(PRIMARY_COLOR);
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> dlg.dispose());
        btnPanel.add(btnClose);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private JLabel createStyledLabel(String text, boolean bold) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", bold ? Font.BOLD : Font.PLAIN, 13));
        lbl.setForeground(new Color(44, 62, 80));
        return lbl;
    }

    // ===============================================================
    // HELPERS — TABLE & BUTTON
    // ===============================================================
    private JTable buildStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(55);
        t.setFont(new Font("Roboto", Font.PLAIN, 14));
        t.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        t.getTableHeader().setBackground(new Color(236, 240, 241));
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        return t;
    }

    private JButton createSmallBtn(String text, FontAwesome icon, Color bg) {
        JButton b = new JButton(text);
        Color fg = (bg.equals(new Color(220, 220, 220))) ? Color.BLACK : Color.WHITE;
        b.setIcon(IconFontSwing.buildIcon(icon, 14, fg));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("Roboto", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(160, 35));
        b.setFocusable(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ===============================================================
    // RENDERERS
    // ===============================================================
    class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            return c;
        }
    }

    class TonKhoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            Object statusVal = tonKhoModel.getValueAt(row, 6);
            boolean warn = statusVal != null && (statusVal.toString().contains("S\u1eafp h\u1ebft") || statusVal.toString().contains("H\u1ebft h\u00e0ng"));
            if (!sel) {
                if (warn) {
                    c.setBackground(new Color(254, 243, 242));
                    c.setForeground(col == 6 ? WARN_COLOR : Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
                    c.setForeground(col == 6 ? OK_COLOR : Color.BLACK);
                }
            }
            return c;
        }
    }

    class PxTonKhoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            Object statusVal = pxModel.getValueAt(row, 5);
            boolean warn = statusVal != null && (statusVal.toString().contains("S\u1eafp h\u1ebft") || statusVal.toString().contains("H\u1ebft h\u00e0ng"));
            if (!sel) {
                if (warn) {
                    c.setBackground(new Color(254, 243, 242));
                    c.setForeground(col == 5 ? WARN_COLOR : Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
                    c.setForeground(col == 5 ? OK_COLOR : Color.BLACK);
                }
            }
            return c;
        }
    }
}
