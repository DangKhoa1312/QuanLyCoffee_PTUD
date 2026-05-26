package ui.dialog;

import controller.PaymentController;
import dao.KhachHangDAO;
import dao.KhuyenMaiDAO;
import dao.impl.KhachHangDAOImpl;
import dao.impl.KhuyenMaiDAOImpl;
import dto.CartItem;
import entity.DonHang;
import entity.HoaDon;
import entity.KhachHang;
import entity.KhuyenMai;
import enums.HinhThucThanhToan;
import exception.AppException;
import utils.AppConfig;
import utils.OrderManager;
import utils.PDFPrinter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PaymentDialog extends JDialog {

    private final DonHang donHang;
    private final PaymentController paymentController;
    private final KhachHangDAO khachHangDAO;
    private final KhuyenMaiDAO khuyenMaiDAO;
    private boolean isPaid = false;

    private JRadioButton rbTienMat;
    private JRadioButton rbChuyenKhoan;
    private JTextField txtKhachDua;
    private JLabel lblTienThua;

    // UI Elements for Phase 2
    private JTextField txtSdtKH;
    private JButton btnTimKH;
    private JLabel lblTenKH;
    private JLabel lblDiemKH;
    private JComboBox<KhuyenMai> cbxKhuyenMai;
    private JCheckBox chkDungDiem;

    // Bill summary labels
    private JLabel lblTongTienMon;
    private JLabel lblTienGiamKM;
    private JLabel lblTienGiamDiem;
    private JLabel lblTienThueVAT;
    private JLabel lblTongPhaiTra;
    private JLabel lblDiemCong;
    private JLabel lblDiemConLai;

    private double tongTienDon; // Từ đơn hàng gốc
    private double tongPhaiTra; // Số tiền cuối cùng
    private double tienGiamGiaKM = 0;
    private double tienGiamGiaDiem = 0;
    private double tienThueVAT = 0;
    private int diemSuDung = 0;

    private KhachHang currentKhachHang = null;

    private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    public PaymentDialog(JFrame parent, DonHang donHang) {
        super(parent, "Thanh Toán Đơn Hàng", true);
        this.donHang = donHang;
        this.paymentController = new PaymentController();
        this.khachHangDAO = new KhachHangDAOImpl();
        this.khuyenMaiDAO = new KhuyenMaiDAOImpl();
        this.tongTienDon = donHang.getTongTienTamTinh();
        this.tongPhaiTra = tongTienDon;

        setSize(900, 550);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
        loadKhuyenMai();
        calculateTotal();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        // ── Header ──
        JLabel lblTitle = new JLabel("Thanh Toán Hóa Đơn", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setBorder(new EmptyBorder(15, 0, 15, 0));
        main.add(lblTitle, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(450);
        split.setBorder(null);
        split.setOpaque(false);
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                    }
                };
            }
        });
        split.setDividerSize(1);

        // ════════ LEFT PANE: KHÁCH HÀNG & DANH SÁCH MÓN ════════
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 10));
        pnlLeft.setBorder(new EmptyBorder(10, 15, 10, 10));
        pnlLeft.setBackground(Color.WHITE);

        // -- 1. Khách hàng --
        JPanel pnlKH = new JPanel(new BorderLayout(5, 5));
        pnlKH.setBackground(new Color(250, 252, 255));
        pnlKH.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 230, 240), 1),
                new EmptyBorder(10, 10, 10, 10)));

        JPanel pnlSearch = new JPanel(new BorderLayout(5, 0));
        pnlSearch.setOpaque(false);
        txtSdtKH = new JTextField();
        txtSdtKH.setPreferredSize(new Dimension(150, 35));
        txtSdtKH.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtSdtKH.setToolTipText("Nhập SĐT khách hàng...");
        btnTimKH = new JButton("Tìm KH");
        btnTimKH.setFont(new Font("Roboto", Font.BOLD, 13));
        btnTimKH.setBackground(new Color(52, 152, 219));
        btnTimKH.setForeground(Color.WHITE);
        btnTimKH.setFocusable(false);
        btnTimKH.addActionListener(e -> searchKhachHang());
        pnlSearch.add(new JLabel("SĐT: "), BorderLayout.WEST);
        pnlSearch.add(txtSdtKH, BorderLayout.CENTER);
        pnlSearch.add(btnTimKH, BorderLayout.EAST);

        JPanel pnlKHInfo = new JPanel(new GridLayout(2, 2, 5, 5));
        pnlKHInfo.setOpaque(false);
        pnlKHInfo.setBorder(new EmptyBorder(10, 0, 0, 0));
        lblTenKH = new JLabel("Khách vãng lai");
        lblTenKH.setFont(new Font("Roboto", Font.BOLD, 14));
        lblDiemKH = new JLabel("Điểm: 0");
        lblDiemKH.setFont(new Font("Roboto", Font.PLAIN, 13));
        lblDiemKH.setForeground(Color.GRAY);
        chkDungDiem = new JCheckBox("Dùng điểm");
        chkDungDiem.setFont(new Font("Roboto", Font.PLAIN, 13));
        chkDungDiem.setOpaque(false);
        chkDungDiem.setEnabled(false);
        chkDungDiem.setFocusable(false);
        chkDungDiem.addActionListener(e -> calculateTotal());

        pnlKHInfo.add(lblTenKH);
        pnlKHInfo.add(lblDiemKH);
        pnlKHInfo.add(chkDungDiem);
        pnlKHInfo.add(new JLabel()); // empty space

        pnlKH.add(pnlSearch, BorderLayout.NORTH);
        pnlKH.add(pnlKHInfo, BorderLayout.CENTER);
        pnlLeft.add(pnlKH, BorderLayout.NORTH);

        // -- 2. Danh sách món (Double Check) --
        String[] cols = { "Tên món", "SL", "Thành tiền" };
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable tbl = new JTable(model);
        tbl.setRowHeight(30);
        tbl.setFont(new Font("Roboto", Font.PLAIN, 13));
        tbl.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));
        tbl.setShowVerticalLines(false);

        tbl.getColumnModel().getColumn(0).setPreferredWidth(180);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(40);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(90);

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tbl.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        List<dto.CartItem> cart = utils.OrderManager.getInstance().getCart(donHang.getMaDonHang());
        for (dto.CartItem item : cart) {
            String sizeStr = item.getSize().getTenSize().equalsIgnoreCase("Thường") ? ""
                    : " (" + item.getSize().getTenSize() + ")";
            model.addRow(new Object[] { item.getMon().getTenMon() + sizeStr, item.getSoLuong(),
                    nf.format(item.getThanhTien()) });
        }

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        pnlLeft.add(scroll, BorderLayout.CENTER);

        // -- 3. Tóm tắt & Khuyến mãi --
        JPanel pnlSummary = new JPanel();
        pnlSummary.setLayout(new BoxLayout(pnlSummary, BoxLayout.Y_AXIS));
        pnlSummary.setBackground(Color.WHITE);
        pnlSummary.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel pnlKM = new JPanel(new BorderLayout(10, 0));
        pnlKM.setOpaque(false);
        JLabel lblKMLabel = new JLabel("Khuyến mãi:");
        lblKMLabel.setFont(new Font("Roboto", Font.BOLD, 13));
        pnlKM.add(lblKMLabel, BorderLayout.WEST);
        cbxKhuyenMai = new JComboBox<>();
        cbxKhuyenMai.setPreferredSize(new Dimension(150, 32));
        cbxKhuyenMai.setFont(new Font("Roboto", Font.PLAIN, 13));
        cbxKhuyenMai.addActionListener(e -> calculateTotal());
        pnlKM.add(cbxKhuyenMai, BorderLayout.CENTER);

        lblTongTienMon = new JLabel("Tổng món: " + nf.format(tongTienDon) + " đ");
        lblTongTienMon.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblTongTienMon.setHorizontalAlignment(SwingConstants.RIGHT);

        lblTienGiamKM = new JLabel("Giảm giá (KM): -0 đ");
        lblTienGiamKM.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblTienGiamKM.setHorizontalAlignment(SwingConstants.RIGHT);

        lblTienGiamDiem = new JLabel("Dùng điểm: -0 đ");
        lblTienGiamDiem.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblTienGiamDiem.setHorizontalAlignment(SwingConstants.RIGHT);

        lblTienThueVAT = new JLabel("Thuế VAT: +0 đ");
        lblTienThueVAT.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblTienThueVAT.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel pnlLabels = new JPanel(new GridLayout(4, 1));
        pnlLabels.setOpaque(false);
        pnlLabels.add(lblTongTienMon);
        pnlLabels.add(lblTienGiamKM);
        pnlLabels.add(lblTienGiamDiem);
        pnlLabels.add(lblTienThueVAT);

        pnlSummary.add(pnlKM);
        pnlSummary.add(pnlLabels);

        JPanel pnlDiemDetails = new JPanel(new GridLayout(2, 1));
        pnlDiemDetails.setOpaque(false);
        lblDiemCong = new JLabel("Sẽ cộng: +0 điểm");
        lblDiemCong.setFont(new Font("Roboto", Font.ITALIC, 13));
        lblDiemCong.setForeground(new Color(39, 174, 96));
        lblDiemCong.setHorizontalAlignment(SwingConstants.RIGHT);

        lblDiemConLai = new JLabel("Dư nợ điểm: 0");
        lblDiemConLai.setFont(new Font("Roboto", Font.BOLD, 13));
        lblDiemConLai.setForeground(new Color(41, 128, 185));
        lblDiemConLai.setHorizontalAlignment(SwingConstants.RIGHT);

        pnlDiemDetails.add(lblDiemCong);
        pnlDiemDetails.add(lblDiemConLai);

        lblDiemCong.setVisible(false);
        lblDiemConLai.setVisible(false);

        pnlSummary.add(pnlDiemDetails);

        pnlLeft.add(pnlSummary, BorderLayout.SOUTH);
        split.setLeftComponent(pnlLeft);

        // ════════ RIGHT PANE: THAO TÁC THANH TOÁN ════════
        JPanel pnlRight = new JPanel(new BorderLayout(0, 15));
        pnlRight.setBorder(new EmptyBorder(10, 10, 10, 15));
        pnlRight.setBackground(Color.WHITE);

        // -- 1. Cần thanh toán & Phương thức --
        JPanel pnlTopRight = new JPanel(new BorderLayout(0, 15));
        pnlTopRight.setOpaque(false);

        lblTongPhaiTra = new JLabel("CẦN TT: " + nf.format(tongPhaiTra) + " đ", SwingConstants.CENTER);
        lblTongPhaiTra.setFont(new Font("Roboto", Font.BOLD, 28));
        lblTongPhaiTra.setForeground(new Color(231, 76, 60)); // Đỏ
        pnlTopRight.add(lblTongPhaiTra, BorderLayout.NORTH);

        JPanel pnlMethod = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlMethod.setOpaque(false);
        rbTienMat = new JRadioButton("Tiền Mặt");
        rbChuyenKhoan = new JRadioButton("Chuyển Khoản");
        rbTienMat.setFont(new Font("Roboto", Font.BOLD, 15));
        rbChuyenKhoan.setFont(new Font("Roboto", Font.BOLD, 15));
        rbTienMat.setHorizontalAlignment(SwingConstants.CENTER);
        rbChuyenKhoan.setHorizontalAlignment(SwingConstants.CENTER);
        rbTienMat.setFocusable(false);
        rbChuyenKhoan.setFocusable(false);
        ButtonGroup group = new ButtonGroup();
        group.add(rbTienMat);
        group.add(rbChuyenKhoan);
        rbTienMat.setSelected(true);
        pnlMethod.add(rbTienMat);
        pnlMethod.add(rbChuyenKhoan);

        rbTienMat.addActionListener(e -> {
            txtKhachDua.setEnabled(true);
            txtKhachDua.requestFocus();
            updateTienThua();
        });
        rbChuyenKhoan.addActionListener(e -> {
            txtKhachDua.setText(nf.format(tongPhaiTra).replace(".", "").replace(",", ""));
            txtKhachDua.setEnabled(false);
            updateTienThua();
        });
        pnlTopRight.add(pnlMethod, BorderLayout.CENTER);
        pnlRight.add(pnlTopRight, BorderLayout.NORTH);

        // -- 2. Nhập tiền & Gợi ý mệnh giá --
        JPanel pnlPay = new JPanel(new BorderLayout(0, 10));
        pnlPay.setOpaque(false);

        JPanel pnlInputWrap = new JPanel(new BorderLayout(0, 5));
        pnlInputWrap.setOpaque(false);

        JLabel lblKD = new JLabel("Tiền khách đưa (VNĐ):");
        lblKD.setFont(new Font("Roboto", Font.BOLD, 14));
        lblKD.setForeground(new Color(100, 100, 100));
        pnlInputWrap.add(lblKD, BorderLayout.NORTH);

        txtKhachDua = new JTextField(nf.format(tongPhaiTra).replace(".", "").replace(",", ""));
        txtKhachDua.setFont(new Font("Roboto", Font.BOLD, 36));
        txtKhachDua.setHorizontalAlignment(JTextField.RIGHT);
        txtKhachDua.setForeground(new Color(41, 128, 185));
        // Prevent vertical stretching by putting it in NORTH or wrapping it
        txtKhachDua.setPreferredSize(new Dimension(0, 60));
        pnlInputWrap.add(txtKhachDua, BorderLayout.CENTER);

        pnlPay.add(pnlInputWrap, BorderLayout.NORTH);

        txtKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateTienThua();
            }

            public void removeUpdate(DocumentEvent e) {
                updateTienThua();
            }

            public void changedUpdate(DocumentEvent e) {
                updateTienThua();
            }
        });

        // Quick cash buttons
        JPanel pnlQuick = new JPanel(new GridLayout(2, 3, 8, 8));
        pnlQuick.setOpaque(false);
        int[] cashValues = { 50000, 100000, 200000, 500000 };
        for (int val : cashValues) {
            JButton btnQ = new JButton(nf.format(val));
            btnQ.setFont(new Font("Roboto", Font.BOLD, 16));
            btnQ.setBackground(new Color(245, 245, 245));
            btnQ.setFocusable(false);
            btnQ.addActionListener(e -> txtKhachDua.setText(String.valueOf(val)));
            pnlQuick.add(btnQ);
        }
        JButton btnExact = new JButton("Vừa đủ");
        btnExact.setFont(new Font("Roboto", Font.BOLD, 16));
        btnExact.setBackground(new Color(220, 245, 255));
        btnExact.setForeground(new Color(0, 100, 200));
        btnExact.setFocusable(false);
        btnExact.addActionListener(e -> txtKhachDua.setText(String.valueOf((long) tongPhaiTra)));
        pnlQuick.add(btnExact);

        JButton btnClear = new JButton("Xóa");
        btnClear.setFont(new Font("Roboto", Font.BOLD, 16));
        btnClear.setBackground(new Color(255, 235, 235));
        btnClear.setForeground(new Color(200, 50, 50));
        btnClear.setFocusable(false);
        btnClear.addActionListener(e -> txtKhachDua.setText(""));
        pnlQuick.add(btnClear);

        pnlPay.add(pnlQuick, BorderLayout.CENTER);
        pnlRight.add(pnlPay, BorderLayout.CENTER);

        // -- 3. Tiền thừa & Nút Xác nhận --
        JPanel pnlBotRight = new JPanel(new BorderLayout(0, 15));
        pnlBotRight.setOpaque(false);

        lblTienThua = new JLabel("Tiền thừa: 0 đ", SwingConstants.RIGHT);
        lblTienThua.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTienThua.setForeground(new Color(39, 174, 96));
        pnlBotRight.add(lblTienThua, BorderLayout.NORTH);

        JPanel pnlBotActions = new JPanel(new BorderLayout(10, 0));
        pnlBotActions.setOpaque(false);

        JButton btnCancel = new JButton("HỦY");
        btnCancel.setFont(new Font("Roboto", Font.BOLD, 15));
        btnCancel.setPreferredSize(new Dimension(100, 55));
        btnCancel.setFocusable(false);
        btnCancel.addActionListener(e -> dispose());

        JButton btnPayAction = new JButton("XÁC NHẬN THANH TOÁN");
        btnPayAction.setFont(new Font("Roboto", Font.BOLD, 18));
        btnPayAction.setBackground(new Color(39, 174, 96));
        btnPayAction.setForeground(Color.WHITE);
        btnPayAction.setPreferredSize(new Dimension(0, 55));
        btnPayAction.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPayAction.addActionListener(e -> handleThanhToan());

        pnlBotActions.add(btnCancel, BorderLayout.WEST);
        pnlBotActions.add(btnPayAction, BorderLayout.CENTER);

        // Add Enter key binding to dialog root pane
        JRootPane rootPane = SwingUtilities.getRootPane(btnPayAction);
        if (rootPane != null) {
            rootPane.setDefaultButton(btnPayAction);
        } else {
            this.getRootPane().setDefaultButton(btnPayAction);
        }

        pnlBotRight.add(pnlBotActions, BorderLayout.SOUTH);
        pnlRight.add(pnlBotRight, BorderLayout.SOUTH);

        split.setRightComponent(pnlRight);
        main.add(split, BorderLayout.CENTER);

        setContentPane(main);

        SwingUtilities.invokeLater(() -> {
            txtKhachDua.requestFocus();
            txtKhachDua.selectAll();
        });
    }

    private JLabel addBillRow(JPanel p, String title, String val) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 15, 0, 15));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Roboto", Font.PLAIN, 14));
        t.setForeground(new Color(80, 80, 80));
        JLabel v = new JLabel(val);
        v.setFont(new Font("Roboto", Font.BOLD, 14));
        v.setForeground(new Color(44, 62, 80));
        row.add(t, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        p.add(row);
        return v;
    }

    private void addDetailRow(JPanel p, String title, String val) {
        addBillRow(p, title, val);
    }

    private void loadKhuyenMai() {
        cbxKhuyenMai.addItem(null); // Option rỗng
        List<KhuyenMai> kms = khuyenMaiDAO.findValidPromotions(tongTienDon);
        for (KhuyenMai km : kms) {
            cbxKhuyenMai.addItem(km);
        }
        // Custom renderer để hiển thị "Không áp dụng" khi null
        cbxKhuyenMai.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("-- Không áp dụng --");
                } else if (value instanceof KhuyenMai) {
                    setText(((KhuyenMai) value).getTenKhuyenMai());
                }
                return this;
            }
        });
    }

    private void searchKhachHang() {
        String sdt = txtSdtKH.getText().trim();
        if (sdt.isEmpty()) {
            clearKhachHang();
            return;
        }
        KhachHang kh = khachHangDAO.findById(sdt);
        if (kh != null) {
            setKhachHang(kh);
        } else {
            // Theo plan: cho phép tạo nhanh khách mới
            int choice = JOptionPane.showConfirmDialog(this,
                    "SĐT \"" + sdt + "\" chưa có trong hệ thống.\nBạn có muốn tạo khách hàng mới không?",
                    "Tạo khách hàng mới", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                String ten = JOptionPane.showInputDialog(this, "Nhập tên khách hàng:", "Tạo nhanh",
                        JOptionPane.PLAIN_MESSAGE);
                if (ten != null && !ten.trim().isEmpty()) {
                    KhachHang newKH = new KhachHang(sdt, ten.trim(), 0, java.time.LocalDateTime.now(), true);
                    if (khachHangDAO.insert(newKH)) {
                        setKhachHang(newKH);
                        JOptionPane.showMessageDialog(this, "Đã tạo khách hàng: " + ten.trim());
                    } else {
                        JOptionPane.showMessageDialog(this, "Lỗi tạo khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        clearKhachHang();
                    }
                } else {
                    clearKhachHang();
                }
            } else {
                clearKhachHang();
            }
        }
        calculateTotal();
    }

    private void setKhachHang(KhachHang kh) {
        currentKhachHang = kh;
        lblTenKH.setText(kh.getTenKhachHang());
        lblTenKH.setForeground(new Color(41, 128, 185));
        lblDiemKH.setText("Điểm tích luỹ: " + kh.getDiemTichLuy());
        if (kh.getDiemTichLuy() > 0) {
            chkDungDiem.setEnabled(true);
        } else {
            chkDungDiem.setSelected(false);
            chkDungDiem.setEnabled(false);
        }
    }

    private void clearKhachHang() {
        currentKhachHang = null;
        lblTenKH.setText("Khách vãng lai");
        lblTenKH.setForeground(Color.BLACK);
        lblDiemKH.setText("Điểm tích luỹ: 0");
        chkDungDiem.setSelected(false);
        chkDungDiem.setEnabled(false);
        calculateTotal();
    }

    private void calculateTotal() {
        // 1. Tính giảm giá khuyến mãi
        tienGiamGiaKM = 0;
        KhuyenMai km = (KhuyenMai) cbxKhuyenMai.getSelectedItem();
        if (km != null) {
            tienGiamGiaKM = km.tinhTienGiam(tongTienDon);
        }

        // 2. Tính tiền giảm từ điểm
        tienGiamGiaDiem = 0;
        diemSuDung = 0;
        if (chkDungDiem.isSelected() && currentKhachHang != null) {
            double giaTri1Diem = AppConfig.getInstance().getDouble("GIA_TRI_DIEM", 1000);
            double tienSauKM = tongTienDon - tienGiamGiaKM;
            if (tienSauKM > 0) {
                int diemToiDaCanDung = (int) Math.ceil(tienSauKM / giaTri1Diem);
                diemSuDung = Math.min(currentKhachHang.getDiemTichLuy(), diemToiDaCanDung);
                tienGiamGiaDiem = diemSuDung * giaTri1Diem;
                // Nếu vượt quá tiền sau KM thì chỉ lấy đúng tiền sau KM
                if (tienGiamGiaDiem > tienSauKM) {
                    tienGiamGiaDiem = tienSauKM;
                }
            }
        }

        // 3. Update UI
        double tienSauGiam = tongTienDon - tienGiamGiaKM - tienGiamGiaDiem;
        if (tienSauGiam < 0)
            tienSauGiam = 0;

        double vatRate = AppConfig.getInstance().getDouble("THUE_VAT", 0);
        tienThueVAT = tienSauGiam * (vatRate / 100.0);

        tongPhaiTra = tienSauGiam + tienThueVAT;

        lblTienGiamKM.setText("Giảm giá (KM): -" + nf.format(tienGiamGiaKM) + " đ");
        lblTienGiamDiem.setText("Dùng điểm: -" + nf.format(tienGiamGiaDiem) + " đ");
        lblTienThueVAT.setText("Thuế VAT: +" + nf.format(tienThueVAT) + " đ");
        lblTongPhaiTra.setText("Cần TT: " + nf.format(tongPhaiTra) + " đ");

        double tyLe = AppConfig.getInstance().getDouble("TY_LE_TICH_DIEM", 10000);
        int diemCong = 0;
        if (tyLe > 0) {
            diemCong = (int) (tongPhaiTra / tyLe);
        }

        if (currentKhachHang != null) {
            lblDiemCong.setText("Sẽ được cộng: +" + diemCong + " điểm");
            int diemCuoi = currentKhachHang.getDiemTichLuy() - diemSuDung + diemCong;
            lblDiemConLai.setText("Điểm sau TT: " + diemCuoi);
            lblDiemCong.setVisible(true);
            lblDiemConLai.setVisible(true);
        } else {
            lblDiemCong.setVisible(false);
            lblDiemConLai.setVisible(false);
        }

        if (rbChuyenKhoan.isSelected()) {
            txtKhachDua.setText(nf.format(tongPhaiTra));
        }
        updateTienThua();
    }

    private void updateTienThua() {
        if (!rbTienMat.isSelected()) {
            lblTienThua.setText("Tiền thừa: 0 đ");
            return;
        }

        try {
            String raw = txtKhachDua.getText().trim().replace(".", "").replace(",", "");
            if (raw.isEmpty())
                raw = "0";
            double kd = Double.parseDouble(raw);
            double thua = kd - tongPhaiTra;

            if (thua < 0) {
                lblTienThua.setText("Khách đưa thiếu!");
                lblTienThua.setForeground(new Color(231, 76, 60));
            } else {
                lblTienThua.setText("Tiền thừa: " + nf.format(thua) + " đ");
                lblTienThua.setForeground(new Color(39, 174, 96));
            }
        } catch (NumberFormatException ignored) {
            lblTienThua.setText("Số tiền không hợp lệ");
            lblTienThua.setForeground(Color.GRAY);
        }
    }

    private void handleThanhToan() {
        try {
            String raw = txtKhachDua.getText().trim().replace(".", "").replace(",", "");
            double kd = Double.parseDouble(raw);
            if (kd < tongPhaiTra) {
                JOptionPane.showMessageDialog(this, "Số tiền khách đưa chưa đủ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            HinhThucThanhToan ht = rbTienMat.isSelected() ? HinhThucThanhToan.TIEN_MAT : HinhThucThanhToan.CHUYEN_KHOAN;
            List<CartItem> cart = OrderManager.getInstance().getCart(donHang.getMaDonHang());
            KhuyenMai km = (KhuyenMai) cbxKhuyenMai.getSelectedItem();
            double tongGiam = tienGiamGiaKM + tienGiamGiaDiem;

            HoaDon hd = paymentController.thanhToan(donHang, cart, tongPhaiTra, ht, tongGiam, km, currentKhachHang,
                    diemSuDung, tienThueVAT);
            isPaid = true;

            JOptionPane.showMessageDialog(this, "Đã thanh toán thành công mã " + hd.getMaHD() + "!", "Thành Công",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();

            int inBill = JOptionPane.showConfirmDialog(this.getParent(), "Bạn có muốn In Hóa Đơn không?", "In Bill",
                    JOptionPane.YES_NO_OPTION);
            if (inBill == JOptionPane.YES_OPTION) {
                try {
                    String pdfPath = PDFPrinter.exportBill(hd, cart, kd);
                    Desktop.getDesktop().open(new File(pdfPath));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this.getParent(), "Lỗi in Hóa đơn: " + ex.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (AppException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi Thanh Toán", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isPaid() {
        return isPaid;
    }
}
