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
 * Panel Quản Lý Kho — Giao diện admin với 4 tab:
 *  1. Tồn Kho  — xem chi tiết, nhập kho, xuất kho, tìm kiếm
 *  2. Nguyên Liệu — double-click mở chi tiết
 *  3. Nhà Cung Cấp — double-click mở chi tiết
 *  4. Phiếu Nhập — xem phiếu + tạo phiếu mới
 */
public class WarehouseManagementPanel extends JPanel {

    private final KhoController controller = new KhoController();

    private final Color PRIMARY_COLOR = new Color(52, 73, 94);
    private final Color BG_COLOR      = new Color(245, 247, 250);
    private final Color WARN_COLOR    = new Color(231, 76, 60);
    private final Color OK_COLOR      = new Color(39, 174, 96);

    // ── Tab: Tồn Kho ──
    private DefaultTableModel tonKhoModel;
    private JTable tonKhoTable;
    private JComboBox<String> cbFilterLoaiNL;
    private JTextField txtTonKhoSearch;

    // ── Tab: Nguyên Liệu ──
    private DefaultTableModel nlModel;
    private JTable nlTable;
    private JTextField txtNLSearch;

    // ── Tab: Nhà Cung Cấp ──
    private DefaultTableModel nccModel;
    private JTable nccTable;
    private JTextField txtNCCSearch;

    // ── Tab: Phiếu Nhập ──
    private DefaultTableModel pnModel;
    private JTable pnTable;

    public WarehouseManagementPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(15, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);

        loadAllData();
    }

    // ═══════════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);

        JPanel bc = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bc.setOpaque(false);
        JLabel lbcGray = new JLabel("Admin / Quản Trị / ");
        lbcGray.setForeground(Color.GRAY);
        lbcGray.setFont(new Font("Roboto", Font.PLAIN, 13));
        JLabel lbcCurrent = new JLabel("Quản Lý Kho");
        lbcCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lbcCurrent.setForeground(PRIMARY_COLOR);
        bc.add(lbcGray); bc.add(lbcCurrent);
        pnl.add(bc);
        pnl.add(Box.createVerticalStrut(8));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel title = new JLabel("QUẢN LÝ KHO HÀNG");
        title.setFont(new Font("Roboto", Font.BOLD, 24));
        title.setForeground(new Color(44, 62, 80));
        title.setIcon(IconFontSwing.buildIcon(FontAwesome.ARCHIVE, 28, PRIMARY_COLOR));
        title.setIconTextGap(12);
        titleBar.add(title, BorderLayout.WEST);
        pnl.add(titleBar);
        pnl.add(Box.createVerticalStrut(10));
        return pnl;
    }

    // ═══════════════════════════════════════════════════════════════
    // TABS
    // ═══════════════════════════════════════════════════════════════
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Roboto", Font.BOLD, 13));
        tabs.setBackground(Color.WHITE);
        tabs.addTab("  📦 Tồn Kho  ",      buildTonKhoTab());
        tabs.addTab("  🌿 Nguyên Liệu  ",  buildNguyenLieuTab());
        tabs.addTab("  🚚 Nhà Cung Cấp  ", buildNhaCungCapTab());
        tabs.addTab("  📋 Phiếu Nhập  ",   buildPhieuNhapTab());
        return tabs;
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB 1: TỒN KHO — xem chi tiết + nhập/xuất kho + tìm kiếm
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildTonKhoTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 5, 5));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(new Color(230, 230, 230)));

        // Left: Search + Filter
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        leftPanel.setOpaque(false);

        leftPanel.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY)));
        txtTonKhoSearch = new JTextField(18);
        txtTonKhoSearch.setPreferredSize(new Dimension(0, 35));
        txtTonKhoSearch.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtTonKhoSearch.setToolTipText("Tìm theo mã hoặc tên nguyên liệu");
        txtTonKhoSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadTonKhoData(); }
        });
        leftPanel.add(txtTonKhoSearch);

        leftPanel.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.FILTER, 16, Color.GRAY)));
        leftPanel.add(new JLabel("Loại NL:"));
        cbFilterLoaiNL = new JComboBox<>(new String[]{"Tất cả", "Chính", "Phụ"});
        cbFilterLoaiNL.setPreferredSize(new Dimension(120, 35));
        cbFilterLoaiNL.setFont(new Font("Roboto", Font.PLAIN, 14));
        cbFilterLoaiNL.addActionListener(e -> loadTonKhoData());
        leftPanel.add(cbFilterLoaiNL);

        topBar.add(leftPanel, BorderLayout.WEST);
        p.add(topBar, BorderLayout.NORTH);

        // Table — columns: Mã Nguyên Liệu, Loại NL, Tên NL, ĐVT, Tồn Kho, Mức TT, Ngày CN, Trạng Thái
        String[] cols = {"Mã Nguyên Liệu", "Loại NL", "Tên Nguyên Liệu", "ĐVT",
                         "Tồn Kho", "Mức Tối Thiểu", "Ngày Cập Nhật", "Trạng Thái"};
        tonKhoModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tonKhoTable = buildStyledTable(tonKhoModel);
        tonKhoTable.setDefaultRenderer(Object.class, new TonKhoRenderer());
        tonKhoTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        tonKhoTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        tonKhoTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        tonKhoTable.getColumnModel().getColumn(7).setPreferredWidth(100);

        // Double-click → xem chi tiết style dialog
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

        JLabel hint = new JLabel("  Nhấp đúp vào hàng để xem chi tiết nguyên liệu");
        hint.setFont(new Font("Roboto", Font.ITALIC, 12));
        hint.setForeground(Color.GRAY);
        hint.setBorder(new EmptyBorder(5, 5, 0, 0));
        p.add(hint, BorderLayout.SOUTH);

        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB 2: NGUYÊN LIỆU — double-click mở chi tiết
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildNguyenLieuTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 5, 5));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(8, 15, 8, 15)
        ));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY)));
        txtNLSearch = new JTextField(22);
        txtNLSearch.setPreferredSize(new Dimension(0, 35));
        txtNLSearch.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtNLSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchNguyenLieu(); }
        });
        left.add(txtNLSearch);
        bar.add(left, BorderLayout.WEST);

        JButton btnAdd = createSmallBtn("  Thêm", FontAwesome.PLUS, new Color(46, 204, 113));
        btnAdd.setPreferredSize(new Dimension(180, 40));
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 14));
        btnAdd.addActionListener(e -> handleAddNL());
        bar.add(btnAdd, BorderLayout.EAST);
        p.add(bar, BorderLayout.NORTH);

        // Table — columns: Mã NL, Tên NL, Loại NL, ĐVT, Đơn Giá Nhập, Object (hidden)
        String[] cols = {"Mã NL", "Tên Nguyên Liệu", "Loại NL", "Đơn Vị Tính", "Đơn Giá Nhập", "Object"};
        nlModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        nlTable = buildStyledTable(nlModel);
        nlTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        nlTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        nlTable.removeColumn(nlTable.getColumnModel().getColumn(5));

        nlTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = nlTable.getSelectedRow();
                    if (row >= 0) {
                        NguyenLieu nl = (NguyenLieu) nlModel.getValueAt(row, 5);
                        handleEditNL(nl);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(nlTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB 3: NHÀ CUNG CẤP — double-click mở chi tiết
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildNhaCungCapTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 5, 5));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(8, 15, 8, 15)
        ));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY)));
        txtNCCSearch = new JTextField(22);
        txtNCCSearch.setPreferredSize(new Dimension(0, 35));
        txtNCCSearch.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtNCCSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchNhaCungCap(); }
        });
        left.add(txtNCCSearch);
        bar.add(left, BorderLayout.WEST);

        JButton btnAdd = createSmallBtn("  Thêm Nhà Cung Cấp", FontAwesome.PLUS, new Color(46, 204, 113));
        btnAdd.addActionListener(e -> handleAddNCC());
        bar.add(btnAdd, BorderLayout.EAST);
        p.add(bar, BorderLayout.NORTH);

        // Table — hidden col 5 giữ NhaCungCap object
        String[] cols = {"Mã NCC", "Tên Nhà Cung Cấp", "Số Điện Thoại", "Email", "Địa Chỉ", "Object"};
        nccModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        nccTable = buildStyledTable(nccModel);
        nccTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        nccTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        nccTable.removeColumn(nccTable.getColumnModel().getColumn(5));

        nccTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = nccTable.getSelectedRow();
                    if (row >= 0) {
                        NhaCungCap ncc = (NhaCungCap) nccModel.getValueAt(row, 5);
                        handleEditNCC(ncc);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(nccTable);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB 4: PHIẾU NHẬP — xem phiếu + tạo phiếu mới
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildPhieuNhapTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 5, 5, 5));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new LineBorder(new Color(230, 230, 230)));

        JButton btnNhapHang = createSmallBtn("  Tạo Phiếu Nhập", FontAwesome.DOWNLOAD, new Color(46, 204, 113));
        btnNhapHang.setPreferredSize(new Dimension(180, 35));
        btnNhapHang.addActionListener(e -> handleTaoPhieuNhap());

        JButton btnRefresh = createSmallBtn("Làm mới", FontAwesome.REFRESH, new Color(220, 220, 220));
        btnRefresh.addActionListener(e -> loadPhieuNhapData());
        bar.add(btnRefresh);
        bar.add(btnNhapHang);
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

    // ═══════════════════════════════════════════════════════════════
    // LOAD DATA
    // ═══════════════════════════════════════════════════════════════
    private void loadAllData() {
        loadTonKhoData();
        loadNguyenLieuData();
        loadNhaCungCapData();
        loadPhieuNhapData();
    }

    private void loadTonKhoData() {
        tonKhoModel.setRowCount(0);
        List<TonKho> list = controller.getAllTonKho();
        String filterLoai = (String) cbFilterLoaiNL.getSelectedItem();
        String searchKw = txtTonKhoSearch.getText().toLowerCase().trim();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (TonKho tk : list) {
            NguyenLieu nl = controller.getNguyenLieuById(tk.getMaNL());
            String tenNL  = nl != null ? nl.getTenNL() : tk.getMaNL();
            String dvt    = nl != null ? nl.getDonViTinh() : "";
            String loaiNL = nl != null && nl.getLoaiNL() != null ? nl.getLoaiNL() : "Chính";

            // Filter by loại NL
            if (filterLoai != null && !"Tất cả".equals(filterLoai)) {
                if (!loaiNL.equals(filterLoai)) continue;
            }

            // Filter by search keyword
            if (!searchKw.isEmpty()) {
                if (!tenNL.toLowerCase().contains(searchKw) &&
                    !tk.getMaNL().toLowerCase().contains(searchKw)) continue;
            }

            String ngayCN = tk.getNgayCapNhat() != null ? tk.getNgayCapNhat().format(fmt) : "";
            String status = tk.isSapHet() ? "⚠ Sắp hết" : "✓ Bình thường";

            tonKhoModel.addRow(new Object[]{
                tk.getMaNL(), loaiNL, tenNL, dvt,
                tk.getSoLuongTon(), tk.getMucToiThieu(), ngayCN, status
            });
        }
    }

    private void loadNguyenLieuData() {
        nlModel.setRowCount(0);
        for (NguyenLieu nl : controller.getAllNguyenLieu()) {
            nlModel.addRow(new Object[]{
                nl.getMaNL(), nl.getTenNL(),
                nl.getLoaiNL() != null ? nl.getLoaiNL() : "Chính",
                nl.getDonViTinh(),
                CurrencyUtils.formatNoUnit(nl.getDonGiaNhap()) + " đ",
                nl
            });
        }
    }

    private void searchNguyenLieu() {
        String kw = txtNLSearch.getText().toLowerCase().trim();
        nlModel.setRowCount(0);
        for (NguyenLieu nl : controller.getAllNguyenLieu()) {
            if (nl.getTenNL().toLowerCase().contains(kw) || nl.getMaNL().toLowerCase().contains(kw)) {
                nlModel.addRow(new Object[]{
                    nl.getMaNL(), nl.getTenNL(),
                    nl.getLoaiNL() != null ? nl.getLoaiNL() : "Chính",
                    nl.getDonViTinh(),
                    CurrencyUtils.formatNoUnit(nl.getDonGiaNhap()) + " đ",
                    nl
                });
            }
        }
    }

    private void loadNhaCungCapData() {
        nccModel.setRowCount(0);
        for (NhaCungCap ncc : controller.getAllNhaCungCap()) {
            nccModel.addRow(new Object[]{
                ncc.getMaNCC(), ncc.getTenNCC(),
                ncc.getSoDienThoai() != null ? ncc.getSoDienThoai() : "",
                ncc.getEmail() != null ? ncc.getEmail() : "",
                ncc.getDiaChi() != null ? ncc.getDiaChi() : "",
                ncc
            });
        }
    }

    private void searchNhaCungCap() {
        String kw = txtNCCSearch.getText().toLowerCase().trim();
        nccModel.setRowCount(0);
        for (NhaCungCap ncc : controller.getAllNhaCungCap()) {
            if (ncc.getTenNCC().toLowerCase().contains(kw) || ncc.getMaNCC().toLowerCase().contains(kw)) {
                nccModel.addRow(new Object[]{
                    ncc.getMaNCC(), ncc.getTenNCC(),
                    ncc.getSoDienThoai() != null ? ncc.getSoDienThoai() : "",
                    ncc.getEmail() != null ? ncc.getEmail() : "",
                    ncc.getDiaChi() != null ? ncc.getDiaChi() : "",
                    ncc
                });
            }
        }
    }

    private void loadPhieuNhapData() {
        pnModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (PhieuNhap pn : controller.getAllPhieuNhap()) {
            NhaCungCap ncc = controller.getNhaCungCapById(pn.getMaNCC());
            pnModel.addRow(new Object[]{
                pn.getMaPN(),
                pn.getNgayNhap() != null ? pn.getNgayNhap().format(fmt) : "",
                ncc  != null ? ncc.getTenNCC()  : pn.getMaNCC(),
                pn.getMaNV(),
                CurrencyUtils.formatNoUnit(pn.getTongTien()) + " đ"
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HANDLERS — TỒN KHO
    // ═══════════════════════════════════════════════════════════════

    private void viewTonKhoDetailDialog(int row) {
        String maNL     = (String) tonKhoModel.getValueAt(row, 0);
        String loaiNL   = (String) tonKhoModel.getValueAt(row, 1);
        String tenNL    = (String) tonKhoModel.getValueAt(row, 2);
        String dvt      = (String) tonKhoModel.getValueAt(row, 3);
        Object tonObj   = tonKhoModel.getValueAt(row, 4);
        Object mucTTObj = tonKhoModel.getValueAt(row, 5);
        String ngayCN   = (String) tonKhoModel.getValueAt(row, 6);
        String trangThai = (String) tonKhoModel.getValueAt(row, 7);

        String tonKho = tonObj != null ? String.valueOf(tonObj) : "0";
        String mucTT  = mucTTObj != null ? String.valueOf(mucTTObj) : "0";

        // Lấy thêm thông tin từ NguyenLieu
        NguyenLieu nl = controller.getNguyenLieuById(maNL);
        String donGia = nl != null ? CurrencyUtils.formatNoUnit(nl.getDonGiaNhap()) + " đ" : "";
        String ngayHH = "";
        if (nl != null && nl.getNgayHetHan() != null) {
            ngayHH = nl.getNgayHetHan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        // Tạo JDialog
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Chi Tiết Tồn Kho: " + tenNL, true);
        dlg.setSize(550, 480);
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

        infoPanel.add(createStyledLabel("Loại Nguyên Liệu:", true));
        infoPanel.add(createStyledLabel(loaiNL, false));

        infoPanel.add(createStyledLabel("Đơn Vị Tính:", true));
        infoPanel.add(createStyledLabel(dvt, false));

        infoPanel.add(createStyledLabel("Đơn Giá Nhập:", true));
        infoPanel.add(createStyledLabel(donGia, false));

        infoPanel.add(createStyledLabel("Số Lượng Tồn:", true));
        JLabel lblTon = createStyledLabel(tonKho, false);
        lblTon.setFont(new Font("Roboto", Font.BOLD, 14));
        lblTon.setForeground(trangThai.contains("Sắp hết") ? WARN_COLOR : OK_COLOR);
        infoPanel.add(lblTon);

        infoPanel.add(createStyledLabel("Mức Tối Thiểu:", true));
        infoPanel.add(createStyledLabel(mucTT, false));

        infoPanel.add(createStyledLabel("Ngày Hết Hạn:", true));
        infoPanel.add(createStyledLabel(ngayHH.isEmpty() ? "—" : ngayHH, false));

        infoPanel.add(createStyledLabel("Ngày Cập Nhật:", true));
        infoPanel.add(createStyledLabel(ngayCN.isEmpty() ? "—" : ngayCN, false));

        infoPanel.add(createStyledLabel("Trạng Thái:", true));
        JLabel lblStatus = createStyledLabel(trangThai, false);
        lblStatus.setFont(new Font("Roboto", Font.BOLD, 13));
        lblStatus.setForeground(trangThai.contains("Sắp hết") ? WARN_COLOR : OK_COLOR);
        infoPanel.add(lblStatus);

        JPanel infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setOpaque(false);
        infoWrapper.setBorder(new EmptyBorder(10, 20, 5, 20));
        infoWrapper.add(infoPanel, BorderLayout.CENTER);
        dlg.add(infoWrapper, BorderLayout.CENTER);

        // Nút đóng
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

    // ═══════════════════════════════════════════════════════════════
    // HANDLERS — NGUYÊN LIỆU
    // ═══════════════════════════════════════════════════════════════
    private void handleAddNL() {
        NguyenLieu nl = new NguyenLieu();
        nl.setMaNL(controller.generateNextMaNL());
        ui.dialog.NguyenLieuDialog dlg =
            new ui.dialog.NguyenLieuDialog((Frame) SwingUtilities.getWindowAncestor(this), nl, false);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            try {
                if (controller.addNguyenLieu(nl)) {
                    loadNguyenLieuData();
                    JOptionPane.showMessageDialog(this, "Thêm nguyên liệu thành công!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditNL(NguyenLieu nl) {
        ui.dialog.NguyenLieuDialog dlg =
            new ui.dialog.NguyenLieuDialog((Frame) SwingUtilities.getWindowAncestor(this), nl, true);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            if (dlg.isDeleted()) {
                if (controller.deleteNguyenLieu(nl.getMaNL())) {
                    loadNguyenLieuData();
                    loadTonKhoData();
                    JOptionPane.showMessageDialog(this, "Đã xóa nguyên liệu!");
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa (có thể đang được sử dụng)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                try {
                    if (controller.updateNguyenLieu(nl)) {
                        loadNguyenLieuData();
                        loadTonKhoData();
                        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HANDLERS — NHÀ CUNG CẤP
    // ═══════════════════════════════════════════════════════════════
    private void handleAddNCC() {
        NhaCungCap ncc = new NhaCungCap();
        ncc.setMaNCC(controller.generateNextMaNCC());
        ui.dialog.NhaCungCapDialog dlg =
            new ui.dialog.NhaCungCapDialog((Frame) SwingUtilities.getWindowAncestor(this), ncc, false);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            try {
                if (controller.addNhaCungCap(ncc)) {
                    loadNhaCungCapData();
                    JOptionPane.showMessageDialog(this, "Thêm nhà cung cấp thành công!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditNCC(NhaCungCap ncc) {
        ui.dialog.NhaCungCapDialog dlg =
            new ui.dialog.NhaCungCapDialog((Frame) SwingUtilities.getWindowAncestor(this), ncc, true);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            if (dlg.isDeleted()) {
                if (controller.deleteNhaCungCap(ncc.getMaNCC())) {
                    loadNhaCungCapData();
                    JOptionPane.showMessageDialog(this, "Đã xóa nhà cung cấp!");
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa (có phiếu nhập liên quan)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                try {
                    if (controller.updateNhaCungCap(ncc)) {
                        loadNhaCungCapData();
                        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HANDLERS — PHIẾU NHẬP
    // ═══════════════════════════════════════════════════════════════
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

        // Tạo JDialog chi tiết
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

        // Bảng chi tiết
        String[] cols = {"Mã NL", "Tên Nguyên Liệu", "Số Lượng", "Đơn Giá", "Thành Tiền"};
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

        // Nút đóng
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

    private JLabel createStyledLabel(String text, boolean bold) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", bold ? Font.BOLD : Font.PLAIN, 13));
        lbl.setForeground(new Color(44, 62, 80));
        return lbl;
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS — TABLE & BUTTON
    // ═══════════════════════════════════════════════════════════════
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

    // ═══════════════════════════════════════════════════════════════
    // RENDERERS
    // ═══════════════════════════════════════════════════════════════
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
            Object statusVal = tonKhoModel.getValueAt(row, 7);
            boolean warn = statusVal != null && statusVal.toString().contains("Sắp hết");
            if (!sel) {
                if (warn) {
                    c.setBackground(new Color(254, 243, 242));
                    c.setForeground(col == 7 ? WARN_COLOR : Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
                    c.setForeground(col == 7 ? OK_COLOR : Color.BLACK);
                }
            }
            return c;
        }
    }
}
