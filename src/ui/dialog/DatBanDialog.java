package ui.dialog;

import controller.ReservationController;
import entity.Ban;
import entity.DatBan;
import entity.KhuVuc;
import enums.TrangThaiDatBan;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Dialog Thêm / Sửa đặt bàn.
 *
 * Chế độ ADD: tạo đặt bàn mới (CHO_XAC_NHAN, thoiGianDat = now)
 * Chế độ EDIT: sửa thông tin + nút Huỷ đặt bàn / Xác nhận
 */
public class DatBanDialog extends JDialog {

    public enum Mode {
        ADD, EDIT
    }

    // ── State ────────────────────────────────────────────────────────────────────────
    private boolean confirmed          = false;
    private boolean cancelled          = false; // true nếu nhấn "Huỷ đặt bàn"
    private boolean saved              = false; // true nếu lưu thành công
    private boolean navigationRequested = false; // true sau khi Xác nhận → chuyển sang trang bàn
    
    // Lưu trạng thái ban đầu để kiểm tra thay đổi (Dirty Checking)
    private String initTen, initSdt, initMaBan;
    private int initSoNguoi, initGio, initPhut;
    private java.util.Date initNgay;

    private final DatBan datBan;
    private final Mode mode;
    private final ReservationController controller;

    // ── Form fields ──────────────────────────────────────────────────────────
    private JTextField txtMaDatBan, txtTenKhach, txtSoDienThoai;
    private JSpinner spnSoNguoi, spnGio, spnPhut;
    private JDateChooser dateChooser;
    private JList<BanItem> lstBan;
    private DefaultListModel<BanItem> banListModel;
    private JComboBox<KhuVucItem> cbKhuVuc;
    private JLabel lblNoTable, lblSucChua, lblTrangThai;
    private JButton btnRefreshBan, btnSave, btnXacNhan, btnHuy;

    // ── Colours ──────────────────────────────────────────────────────────────
    private static final Color PRIMARY = new Color(113, 76, 52); // nâu cafe
    private static final Color SUCCESS = new Color(46, 204, 113);
    private static final Color DANGER = new Color(231, 76, 60);
    private static final Color INFO = new Color(41, 128, 185);
    private static final Color BG = new Color(245, 247, 250);

    // ══════════════════════════════════════════════════════════════════════════
    public DatBanDialog(Frame parent, DatBan datBan, Mode mode, ReservationController controller) {
        super(parent, mode == Mode.ADD ? "Thêm Đặt Bàn Mới" : "Chỉnh Sửa Đặt Bàn", true);
        this.datBan = datBan;
        this.mode = mode;
        this.controller = controller;

        initUI();
        fillData();
        if (mode == Mode.EDIT)
            updateStatusBadge();
        
        registerDirtyCheckers();
        checkDirty();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INIT UI
    // ══════════════════════════════════════════════════════════════════════════
    private void initUI() {
        setSize(1100, 660);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 20, 10, 20));
        body.add(buildInfoCard());
        body.add(buildBanCard());
        add(body, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(0, 65));
        header.setBorder(new EmptyBorder(0, 25, 0, 25));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel ico = new JLabel(IconFontSwing.buildIcon(FontAwesome.CALENDAR_CHECK_O, 30, Color.WHITE));
        JLabel title = new JLabel(mode == Mode.ADD ? "ĐẶT BÀN MỚI" : "CẬP NHẬT ĐẶT BÀN");
        title.setFont(new Font("Roboto", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        left.add(ico);
        left.add(title);

        // Badge trạng thái (chỉ hiện ở chế độ EDIT)
        lblTrangThai = new JLabel();
        lblTrangThai.setFont(new Font("Roboto", Font.BOLD, 13));
        lblTrangThai.setForeground(new Color(220, 220, 220));
        lblTrangThai.setVisible(mode == Mode.EDIT);

        header.add(left, BorderLayout.WEST);
        header.add(lblTrangThai, BorderLayout.EAST);
        return header;
    }

    // ── Card trái: Thông tin đặt bàn ─────────────────────────────────────────
    private JPanel buildInfoCard() {
        JPanel card = createCard("THÔNG TIN ĐẶT BÀN");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = defaultGBC();

        // Mã đặt bàn (read-only)
        addLabel(form, gbc, "Mã đặt bàn:", FontAwesome.HASHTAG);
        txtMaDatBan = new JTextField();
        txtMaDatBan.setEditable(false);
        txtMaDatBan.setBackground(new Color(240, 240, 240));
        addField(form, gbc, txtMaDatBan);

        // Tên khách
        addLabel(form, gbc, "Tên khách *:", FontAwesome.USER);
        txtTenKhach = new JTextField();
        addField(form, gbc, txtTenKhach);

        // Số điện thoại
        addLabel(form, gbc, "Số điện thoại *:", FontAwesome.PHONE);
        txtSoDienThoai = new JTextField();
        addField(form, gbc, txtSoDienThoai);

        // Số người
        addLabel(form, gbc, "Số người *:", FontAwesome.USERS);
        spnSoNguoi = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
        spnSoNguoi.setPreferredSize(new Dimension(0, 35));
        addField(form, gbc, spnSoNguoi);

        // Ngày đến
        addLabel(form, gbc, "Ngày đến *:", FontAwesome.CALENDAR);
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setPreferredSize(new Dimension(0, 35));
        addField(form, gbc, dateChooser);

        // Giờ đến (JSpinner giờ + phút)
        addLabel(form, gbc, "Giờ đến *:", FontAwesome.CLOCK_O);
        JPanel pnlTime = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlTime.setOpaque(false);
        spnGio = new JSpinner(new SpinnerNumberModel(19, 7, 22, 1));
        spnPhut = new JSpinner(new SpinnerNumberModel(0, 0, 55, 5));
        JSpinner.NumberEditor edGio = new JSpinner.NumberEditor(spnGio, "00");
        JSpinner.NumberEditor edPhut = new JSpinner.NumberEditor(spnPhut, "00");
        spnGio.setEditor(edGio);
        spnPhut.setEditor(edPhut);
        spnGio.setPreferredSize(new Dimension(65, 35));
        spnPhut.setPreferredSize(new Dimension(65, 35));
        pnlTime.add(spnGio);
        pnlTime.add(new JLabel(":"));
        pnlTime.add(spnPhut);
        addField(form, gbc, pnlTime);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    // ── Card phải: Chọn bàn ──────────────────────────────────────────────────
    private JPanel buildBanCard() {
        JPanel card = createCard("CHỌN BÀN");

        // Combobox khu vực
        JPanel pnlKhuVuc = new JPanel(new BorderLayout(8, 0));
        pnlKhuVuc.setOpaque(false);
        pnlKhuVuc.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel lblKV = new JLabel(IconFontSwing.buildIcon(FontAwesome.MAP_MARKER, 14, Color.GRAY));
        lblKV.setText("  Khu vực:");
        lblKV.setFont(new Font("Roboto", Font.BOLD, 12));
        cbKhuVuc = new JComboBox<>();
        cbKhuVuc.setFont(new Font("Roboto", Font.PLAIN, 13));
        cbKhuVuc.setPreferredSize(new Dimension(0, 35));
        pnlKhuVuc.add(lblKV, BorderLayout.WEST);
        pnlKhuVuc.add(cbKhuVuc, BorderLayout.CENTER);
        card.add(pnlKhuVuc, BorderLayout.NORTH);

        // Danh sách bàn trống
        banListModel = new DefaultListModel<>();
        lstBan = new JList<>(banListModel);
        lstBan.setFont(new Font("Roboto", Font.PLAIN, 13));
        lstBan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstBan.setCellRenderer(new BanCellRenderer());
        lstBan.setFixedCellHeight(45);
        lstBan.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                updateSucChua();
        });

        // Thông báo hết bàn
        lblNoTable = new JLabel("<html><div style='text-align:center; color:#E74C3C;'>" +
                "⚠ Hết bàn đặt cho khung giờ này</div></html>", SwingConstants.CENTER);
        lblNoTable.setFont(new Font("Roboto", Font.BOLD, 13));
        lblNoTable.setVisible(false);

        // Nút refresh bàn
        btnRefreshBan = new JButton(" Tải lại bàn", IconFontSwing.buildIcon(FontAwesome.REFRESH, 13, PRIMARY));
        btnRefreshBan.setFont(new Font("Roboto", Font.BOLD, 12));
        btnRefreshBan.setFocusable(false);
        btnRefreshBan.addActionListener(e -> loadBanTrong());

        JScrollPane scroll = new JScrollPane(lstBan);
        scroll.setBorder(new LineBorder(new Color(220, 220, 220)));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(lblNoTable, BorderLayout.CENTER);

        // Sức chứa
        lblSucChua = new JLabel();
        lblSucChua.setFont(new Font("Roboto", Font.PLAIN, 12));
        lblSucChua.setForeground(new Color(100, 100, 100));
        bottom.add(lblSucChua, BorderLayout.WEST);
        bottom.add(btnRefreshBan, BorderLayout.EAST);

        center.add(bottom, BorderLayout.SOUTH);
        card.add(center, BorderLayout.CENTER);

        // Load khu vực và bàn
        loadKhuVuc();
        cbKhuVuc.addActionListener(e -> loadBanTrong());
        // Load bàn ngay khi thay đổi ngày/giờ
        spnGio.addChangeListener(e -> loadBanTrong());
        spnPhut.addChangeListener(e -> loadBanTrong());
        dateChooser.getDateEditor().addPropertyChangeListener("date", e -> loadBanTrong());

        // Khoá card nếu trạng thái không phải CHO_XAC_NHAN
        if (mode == Mode.EDIT && datBan.getTrangThai() != null
                && datBan.getTrangThai() != TrangThaiDatBan.CHO_XAC_NHAN) {
            setCardReadOnly(card);
        }

        return card;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setOpaque(false);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(225, 225, 225)));

        if (mode == Mode.EDIT) {
            TrangThaiDatBan tt = datBan.getTrangThai();
            boolean coTheHuy    = (tt == TrangThaiDatBan.CHO_XAC_NHAN);
            boolean coTheXacNhan = (tt == TrangThaiDatBan.CHO_XAC_NHAN);

            // Nút huỷ đặt bàn (chỉ khi CHO_XAC_NHAN)
            btnHuy = new JButton("  Huỷ đặt bàn", IconFontSwing.buildIcon(FontAwesome.BAN, 14, Color.WHITE));
            styleButton(btnHuy, DANGER);
            btnHuy.setEnabled(coTheHuy);
            if (!coTheHuy) btnHuy.setToolTipText("Không thể huỷ khi trạng thái: " + (tt != null ? tt.displayName() : ""));
            btnHuy.addActionListener(e -> handleHuyDatBan());

            // Nút xác nhận (chỉ khi CHO_XAC_NHAN)
            btnXacNhan = new JButton("  Xác nhận", IconFontSwing.buildIcon(FontAwesome.CHECK_CIRCLE, 14, Color.WHITE));
            styleButton(btnXacNhan, INFO);
            btnXacNhan.setEnabled(coTheXacNhan);
            btnXacNhan.addActionListener(e -> handleXacNhan());

            footer.add(btnHuy);
            footer.add(btnXacNhan);
        }

        JButton btnCancel = new JButton("  Đóng");
        btnCancel.setFont(new Font("Roboto", Font.PLAIN, 13));
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> dispose());

        btnSave = new JButton("  Lưu", IconFontSwing.buildIcon(FontAwesome.FLOPPY_O, 14, Color.WHITE));
        styleButton(btnSave, SUCCESS);
        // Khi không phải CHO_XAC_NHAN → ẩn nút Lưu
        if (mode == Mode.EDIT && datBan.getTrangThai() != null
                && datBan.getTrangThai() != TrangThaiDatBan.CHO_XAC_NHAN) {
            btnSave.setVisible(false);
        }
        btnSave.addActionListener(e -> handleSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DATA LOADING
    // ══════════════════════════════════════════════════════════════════════════
    private void fillData() {
        txtMaDatBan.setText(datBan.getMaDatBan() != null ? datBan.getMaDatBan() : controller.sinhMaDatBan());
        txtTenKhach.setText(datBan.getTenKhach() != null ? datBan.getTenKhach() : "");
        txtSoDienThoai.setText(datBan.getSoDienThoai() != null ? datBan.getSoDienThoai() : "");
        spnSoNguoi.setValue(datBan.getSoLuongNguoi() > 0 ? datBan.getSoLuongNguoi() : 2);

        if (datBan.getThoiGianDen() != null) {
            LocalDateTime den = datBan.getThoiGianDen();
            Date d = Date.from(den.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            dateChooser.setDate(d);
            spnGio.setValue(den.getHour());
            spnPhut.setValue(den.getMinute());
        } else {
            // Mặc định: ngày mai, 19:00
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            Date d = Date.from(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant());
            dateChooser.setDate(d);
            spnGio.setValue(19);
            spnPhut.setValue(0);
        }

        // Lưu snapshot để kiểm tra thay đổi
        initialSnapshot();
    }

    private void initialSnapshot() {
        initTen = txtTenKhach.getText().trim();
        initSdt = txtSoDienThoai.getText().trim();
        initSoNguoi = (Integer) spnSoNguoi.getValue();
        initNgay = dateChooser.getDate();
        initGio = (Integer) spnGio.getValue();
        initPhut = (Integer) spnPhut.getValue();
        initMaBan = datBan.getMaBan();
    }

    private boolean isDirty() {
        if (!txtTenKhach.getText().trim().equals(initTen)) return true;
        if (!txtSoDienThoai.getText().trim().equals(initSdt)) return true;
        if (!spnSoNguoi.getValue().equals(initSoNguoi)) return true;
        if (initNgay != null && !initNgay.equals(dateChooser.getDate())) return true;
        if (!spnGio.getValue().equals(initGio)) return true;
        if (!spnPhut.getValue().equals(initPhut)) return true;
        
        BanItem selected = lstBan.getSelectedValue();
        String currentMaBan = (selected != null) ? selected.ban.getMaBan() : initMaBan;
        if (initMaBan == null && currentMaBan != null) return true;
        if (initMaBan != null && !initMaBan.equals(currentMaBan)) return true;
        
        return false;
    }
    
    private void checkDirty() {
        if (btnSave != null && mode == Mode.EDIT) {
            btnSave.setEnabled(isDirty());
        }
    }

    private void registerDirtyCheckers() {
        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { checkDirty(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { checkDirty(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { checkDirty(); }
        };
        txtTenKhach.getDocument().addDocumentListener(dl);
        txtSoDienThoai.getDocument().addDocumentListener(dl);
        
        javax.swing.event.ChangeListener cl = e -> checkDirty();
        spnSoNguoi.addChangeListener(cl);
        spnGio.addChangeListener(cl);
        spnPhut.addChangeListener(cl);
        
        dateChooser.getDateEditor().addPropertyChangeListener("date", e -> checkDirty());
        
        lstBan.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) checkDirty();
        });
    }

    private void loadKhuVuc() {
        cbKhuVuc.removeAllItems();
        List<KhuVuc> dsKV = controller.getDanhSachKhuVuc();
        for (KhuVuc kv : dsKV) {
            cbKhuVuc.addItem(new KhuVucItem(kv));
        }
        // Nếu đang edit và bàn đã chọn, chọn đúng khu vực
        if (mode == Mode.EDIT && datBan.getMaBan() != null) {
            selectKhuVucCuaBan();
        }
    }

    private void selectKhuVucCuaBan() {
        // Tìm khu vực của bàn hiện tại và chọn
        for (int i = 0; i < cbKhuVuc.getItemCount(); i++) {
            // Sẽ load bàn sau khi đổi khu vực; sau đó select bàn đúng
        }
        loadBanTrong();
        // Select bàn hiện tại trong list
        for (int i = 0; i < banListModel.size(); i++) {
            if (banListModel.getElementAt(i).ban.getMaBan().equals(datBan.getMaBan())) {
                lstBan.setSelectedIndex(i);
                break;
            }
        }
    }

    private void loadBanTrong() {
        banListModel.clear();
        lblNoTable.setVisible(false);
        lblSucChua.setText("");

        KhuVucItem kvItem = (KhuVucItem) cbKhuVuc.getSelectedItem();
        if (kvItem == null)
            return;

        LocalDateTime thoiGianDen = buildThoiGianDen();
        String excludeId = mode == Mode.EDIT ? datBan.getMaDatBan() : null;

        List<Ban> dsBan = controller.getBanTrongChoKhuVuc(kvItem.kv.getMaKhuVuc(), thoiGianDen, excludeId);

        // Nếu đang EDIT, đảm bảo bàn hiện tại luôn có trong danh sách và được chọn
        if (mode == Mode.EDIT && datBan.getMaBan() != null) {
            boolean coTrongDS = dsBan.stream().anyMatch(b -> b.getMaBan().equals(datBan.getMaBan()));
            if (!coTrongDS) {
                // Thử lấy thông tin bàn từ database để thêm vào list
                entity.Ban bht = controller.findBanById(datBan.getMaBan());
                if (bht != null && bht.getMaKhuVuc().equals(kvItem.kv.getMaKhuVuc())) {
                    dsBan.add(0, bht); // Thêm vào đầu danh sách
                }
            }
        }

        if (dsBan.isEmpty()) {
            lblNoTable.setVisible(true);
        } else {
            for (Ban b : dsBan) {
                banListModel.addElement(new BanItem(b));
            }
            // Tự động chọn bàn cũ/đang edit
            String targetMaBan = (mode == Mode.EDIT) ? datBan.getMaBan() : null;
            if (targetMaBan != null) {
                for (int i = 0; i < banListModel.size(); i++) {
                    if (banListModel.getElementAt(i).ban.getMaBan().equals(targetMaBan)) {
                        lstBan.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private LocalDateTime buildThoiGianDen() {
        if (dateChooser.getDate() == null)
            return null;
        LocalDate date = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int gio = (Integer) spnGio.getValue();
        int phut = (Integer) spnPhut.getValue();
        return date.atTime(gio, phut);
    }

    private void updateSucChua() {
        BanItem item = lstBan.getSelectedValue();
        if (item != null) {
            lblSucChua.setText("👥 Sức chứa: " + item.ban.getSucChua() + " người");
        } else {
            lblSucChua.setText("");
        }
    }

    private void updateStatusBadge() {
        if (datBan.getTrangThai() == null)
            return;
        String icon = "";
        switch (datBan.getTrangThai()) {
            case CHO_XAC_NHAN:
                icon = "⏳ ";
                break;
            case DA_XAC_NHAN:
                icon = "✅ ";
                break;
            case DA_THANH_TOAN:
                icon = "🟢 ";
                break;
            case HET_HAN:
                icon = "🔴 ";
                break;
            case DA_HUY:
                icon = "⛔ ";
                break;
        }
        lblTrangThai.setText(icon + datBan.getTrangThai().displayName() + "   ");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ACTIONS
    // ══════════════════════════════════════════════════════════════════════════
    private void applyDataToEntity() {
        datBan.setMaDatBan(txtMaDatBan.getText().trim());
        datBan.setTenKhach(txtTenKhach.getText().trim());
        datBan.setSoDienThoai(txtSoDienThoai.getText().trim());
        datBan.setSoLuongNguoi((Integer) spnSoNguoi.getValue());
        datBan.setThoiGianDen(buildThoiGianDen());
        BanItem selected = lstBan.getSelectedValue();
        if (selected != null)
            datBan.setMaBan(selected.ban.getMaBan());
    }

    private void handleSave() {
        if (!validateInput())
            return;

        if (!isDirty()) {
            JOptionPane.showMessageDialog(this, "Không có thay đổi nào để lưu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        applyDataToEntity();
        saved = true;
        confirmed = true;
        dispose();
    }

    private void handleXacNhan() {
        if (!validateInput()) return;
        
        // 1. Lưu các thay đổi mà người dùng nhập trên form
        applyDataToEntity();

        int xn = JOptionPane.showConfirmDialog(this,
                "<html><b>Xác nhận đặt bàn?</b><br><br>" +
                "Khách: <b>" + datBan.getTenKhach() + "</b><br>" +
                "Bàn: <b>" + (datBan.getSoBan() != null ? datBan.getSoBan() : datBan.getMaBan()) + "</b><br>" +
                "Giờ đến: <b>" + (datBan.getThoiGianDen() != null ?
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").format(datBan.getThoiGianDen()) : "?") +
                "</b><br><br>" +
                "Trạng thái chuyển <b>Đã xác nhận</b> và bàn chuyển sang <b>Có khách</b>.</html>",
                "Xác nhận đặt bàn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (xn == JOptionPane.YES_OPTION) {
            // 2. Cập nhật trạng thái ngay trên đối tượng (để ReservationManagementPanel lưu vào DB)
            datBan.setTrangThai(TrangThaiDatBan.DA_XAC_NHAN);
            
            // 3. Thực hiện xác nhận (controller sẽ chặn nếu bàn đang CO_KHACH)
            boolean ok = controller.xacNhan(datBan.getMaDatBan());
            if (ok) {
                // Đánh dấu để ReservationManagementPanel gọi controller.sua() lưu lại các sửa đổi vào DB
                saved = true; 
                confirmed = true;
                navigationRequested = true; // yêu cầu chuyển sang trang bàn
                dispose();
            } else {
                // Khôi phục trạng thái nếu bị chặn
                datBan.setTrangThai(TrangThaiDatBan.CHO_XAC_NHAN);
                // Kiểm tra lý do thất bại
                if (controller.isBanDangCoKhach(datBan.getMaBan())) {
                    JOptionPane.showMessageDialog(this,
                        "<html>⛔ <b>Không thể xác nhận!</b><br><br>" +
                        "Bàn đang <b>phục vụ khách vãng lai</b>.<br>" +
                        "Vui lòng đợi khách vãng lai thanh toán xong,<br>" +
                        "hoặc chuyển đặt bàn sang bàn khác.</html>",
                        "Bàn đang có khách", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xác nhận. Vui lòng thử lại.", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void handleHuyDatBan() {
        TrangThaiDatBan tt = datBan.getTrangThai();
        if (tt == TrangThaiDatBan.DA_XAC_NHAN || tt == TrangThaiDatBan.DA_THANH_TOAN) {
            JOptionPane.showMessageDialog(this,
                "<html>⛔ Không thể huỷ đặt bàn khi đã ở trạng thái <b>" + tt.displayName() + "</b>.<br>" +
                "Chỉ có thể huỷ khi đang <b>Chờ xác nhận</b>.</html>",
                "Không thể huỷ", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int xn = JOptionPane.showConfirmDialog(this,
                "<html><b>⚠ Huỷ đặt bàn?</b><br><br>" +
                "Bạn có chắc muốn huỷ đặt bàn của <b>" + datBan.getTenKhach() + "</b>?<br>" +
                "Hành động này không thể hoàn tác.</html>",
                "Xác nhận huỷ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xn == JOptionPane.YES_OPTION) {
            boolean ok = controller.huy(datBan.getMaDatBan());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã huỷ đặt bàn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                cancelled = true;
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể huỷ. Vui lòng thử lại.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // VALIDATION
    // ══════════════════════════════════════════════════════════════════════════
    private boolean validateInput() {
        String ten = txtTenKhach.getText().trim();
        if (ten.isEmpty()) {
            warn("Tên khách không được để trống!", txtTenKhach);
            return false;
        }
        String sdt = txtSoDienThoai.getText().trim();
        if (!sdt.matches("\\d{10,11}")) {
            warn("Số điện thoại phải gồm 10-11 chữ số!", txtSoDienThoai);
            return false;
        }
        if (dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày đến!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        LocalDateTime thoiGianDen = buildThoiGianDen();
        if (thoiGianDen == null || thoiGianDen.isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this, "Giờ đến phải sau thời điểm hiện tại!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Ràng buộc giờ/phút (phòng trường hợp Spinner bị gõ sai giá trị)
        int h = (Integer) spnGio.getValue();
        int m = (Integer) spnPhut.getValue();
        // Validate giờ hành chính (7h–22h)
        if (h < 7 || h >= 22) {
            JOptionPane.showMessageDialog(this, "Giờ đặt bàn phải trong khung giờ hành chính (7h00 – 22h00)!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        // Ràng buộc phút (phòng trường hợp Spinner bị gõ sai giá trị)
        if (m < 0 || m > 55) {
            JOptionPane.showMessageDialog(this, "Phút (0-55) không hợp lệ!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Trong chế độ sửa, nếu không chọn bàn mới thì vẫn dùng bàn cũ
        if (mode == Mode.ADD && lstBan.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (mode == Mode.EDIT && lstBan.getSelectedValue() == null && datBan.getMaBan() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        // Kiểm tra trùng giờ lần cuối
        BanItem bi = lstBan.getSelectedValue();
        String excludeId = mode == Mode.EDIT ? datBan.getMaDatBan() : null;
        if (controller.isTrungGio(bi.ban.getMaBan(), thoiGianDen, excludeId)) {
            JOptionPane.showMessageDialog(this,
                    "<html>⚠ Bàn <b>" + bi.ban.getSoBan() + "</b> đã có đặt trong khoảng ±1 giờ.<br>" +
                            "Vui lòng chọn giờ hoặc bàn khác!</html>",
                    "Trùng giờ đặt bàn", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void warn(String msg, JComponent focus) {
        JOptionPane.showMessageDialog(this, msg, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        if (focus != null)
            focus.requestFocus();
    }

    /**
     * Khoá toàn bộ các component trong một panel (dùng khi trạng thái != CHO_XAC_NHAN)
     */
    private void setCardReadOnly(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            comp.setEnabled(false);
            if (comp instanceof java.awt.Container) {
                setCardReadOnly((java.awt.Container) comp);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ACCESSORS
    // ══════════════════════════════════════════════════════════════════════════
    public boolean isConfirmed()          { return confirmed; }
    public boolean isCancelled()           { return cancelled; }
    public boolean isSaved()               { return saved; }
    public boolean isNavigationRequested() { return navigationRequested; }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 225, 225), 1),
                new EmptyBorder(15, 18, 15, 18)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Roboto", Font.BOLD, 13));
        lbl.setForeground(PRIMARY);
        lbl.setBorder(new MatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(0, 0, 8, 0)));
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    private void addLabel(JPanel p, GridBagConstraints gbc, String text, FontAwesome icon) {
        JLabel lbl = new JLabel(" " + text);
        lbl.setIcon(IconFontSwing.buildIcon(icon, 12, Color.GRAY));
        lbl.setFont(new Font("Roboto", Font.BOLD, 12));
        gbc.insets = new Insets(8, 0, 3, 0);
        p.add(lbl, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
    }

    private void addField(JPanel p, GridBagConstraints gbc, JComponent field) {
        if (field instanceof JTextField || field instanceof JSpinner) {
            field.setPreferredSize(new Dimension(0, 35));
        }
        p.add(field, gbc);
        gbc.gridy++;
    }

    private GridBagConstraints defaultGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Roboto", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setFocusable(false);
    }

    // ── Inner classes ─────────────────────────────────────────────────────────
    static class KhuVucItem {
        final KhuVuc kv;

        KhuVucItem(KhuVuc kv) {
            this.kv = kv;
        }

        @Override
        public String toString() {
            return kv.getTenKhuVuc();
        }
    }

    static class BanItem {
        final Ban ban;

        BanItem(Ban ban) {
            this.ban = ban;
        }

        @Override
        public String toString() {
            return ban.getSoBan() + " (sức chứa: " + ban.getSucChua() + " người)";
        }
    }

    /** Renderer cho bàn trong JList */
    static class BanCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                    new EmptyBorder(8, 12, 8, 12)));
            if (isSelected) {
                lbl.setBackground(new Color(232, 245, 253));
                lbl.setForeground(new Color(41, 128, 185));
            }
            if (value instanceof BanItem) {
                BanItem bi = (BanItem) value;
                lbl.setText("🪑 " + bi.ban.getSoBan() + "   —   👥 " + bi.ban.getSucChua() + " người");
            }
            return lbl;
        }
    }
}
