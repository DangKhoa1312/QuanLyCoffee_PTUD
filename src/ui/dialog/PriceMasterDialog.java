package ui.dialog;

import controller.MenuController;
import controller.PriceController;
import entity.BangGia;
import entity.BangGiaChiTiet;
import entity.Mon;
import entity.Size;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.util.List;
import com.toedter.calendar.JDateChooser;

/**
 * PriceMasterDialog: Quản lý thông tin Bảng giá và Chi tiết giá kết hợp.
 * Tích hợp: Clone giá, Batch adjust, Sync thực đơn, Lockdown mode (bảng đã ẩn).
 */
public class PriceMasterDialog extends JDialog {

    private final PriceController priceController = new PriceController();
    private final MenuController menuController = new MenuController();

    private final BangGia bangGia;
    private final boolean isEditMode;
    private String sourceMaBG;
    private boolean confirmed = false;

    // UI Components - Header General Info
    private JTextField txtMa, txtTen;
    private JDateChooser dcBatDau, dcKetThuc;
    private JCheckBox chkStatus;

    // UI Components - Detail Table
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearchDish;
    private JCheckBox chkShowInactive;

    // UI Components - Batch Actions
    private JComboBox<BangGia> cbCloneSource;
    private JTextField txtPercent, txtFixed;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public PriceMasterDialog(Frame parent, BangGia bg, boolean isEditMode, String sourceMaBG) {
        super(parent, isEditMode ? "Tùy chỉnh Bảng Giá" : "Tạo Bảng Giá Mới", true);
        this.bangGia    = bg;
        this.isEditMode = isEditMode;
        this.sourceMaBG = sourceMaBG;

        initUI();
        fillGeneralInfo();
        loadTableData();
    }

    private void initUI() {
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // Header Title
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(PRIMARY_COLOR);
        h.setPreferredSize(new Dimension(0, 50));
        h.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel title = new JLabel(isEditMode
            ? " CHI TIẾT BẢNG GIÁ: " + bangGia.getMaBangGia()
            : " TẠO BẢNG GIÁ MỚI");
        title.setFont(new Font("Roboto", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setIcon(IconFontSwing.buildIcon(FontAwesome.MONEY, 24, Color.WHITE));
        h.add(title, BorderLayout.WEST);
        add(h, BorderLayout.NORTH);

        // Body Content
        JPanel body = new JPanel(new BorderLayout(15, 15));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(15, 20, 15, 20));

        body.add(createGeneralInfoPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(createTablePanel(), BorderLayout.CENTER);
        centerPanel.add(createBatchToolsPanel(), BorderLayout.EAST);

        body.add(centerPanel, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createGeneralInfoPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        pnl.add(new JLabel("Mã bảng giá:"), gbc);
        txtMa = new JTextField(); txtMa.setEditable(false);
        gbc.gridy = 1; pnl.add(txtMa, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        pnl.add(new JLabel("Tên bảng giá (*):"), gbc);
        txtTen = new JTextField();
        gbc.gridy = 1; pnl.add(txtTen, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        pnl.add(new JLabel("Ngày bắt đầu:"), gbc);
        dcBatDau = new JDateChooser();
        dcBatDau.setDateFormatString("yyyy-MM-dd");
        gbc.gridy = 3; pnl.add(dcBatDau, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        pnl.add(new JLabel("Ngày kết thúc (Tùy chọn - để trống = vô thời hạn):"), gbc);
        dcKetThuc = new JDateChooser();
        dcKetThuc.setDateFormatString("yyyy-MM-dd");
        gbc.gridy = 3; pnl.add(dcKetThuc, gbc);

        // Logic giới hạn ngày (End >= Start)
        dcBatDau.addPropertyChangeListener("date", evt -> {
            java.util.Date minD = dcBatDau.getDate();
            if (minD != null) {
                dcKetThuc.setMinSelectableDate(minD);
                java.util.Date endD = dcKetThuc.getDate();
                if (endD != null && endD.before(minD)) {
                    dcKetThuc.setDate(null);
                }
            }
        });

        chkStatus = new JCheckBox("Kích hoạt ngay");
        chkStatus.setOpaque(false);
        gbc.gridx = 0; gbc.gridy = 4;
        pnl.add(chkStatus, gbc);

        // Lockdown: khóa giao diện nếu bảng đã ẩn
        if (!bangGia.isHoatDong()) {
            txtTen.setEditable(false);
            dcBatDau.setEnabled(false);
            dcKetThuc.setEnabled(false);
            chkStatus.setEnabled(false);
        }

        return pnl;
    }

    private JPanel createTablePanel() {
        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(Color.WHITE);
        pnlTable.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            BorderFactory.createTitledBorder(
                new EmptyBorder(5, 5, 5, 5),
                "CHI TIẾT GIÁ BÁN",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Roboto", Font.BOLD, 13), PRIMARY_COLOR)
        ));

        // Cột ẩn: col 3=maSize, col 4=isDishActive, col 5=isSizeActive
        String[] cols = {"Tên món ăn", "Kích thước", "Giá bán (VNĐ)", "maSize", "isDishActive", "isSizeActive"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return bangGia.isHoatDong() && c == 2;
            }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4 || columnIndex == 5) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
        };

        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setRowHeight(35);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 245, 245));
        table.setDefaultRenderer(Object.class, new PriceRowRenderer());

        // Ẩn các cột phụ trợ
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(5)); // isSizeActive
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(4)); // isDishActive
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(3)); // maSize

        // Header bảng: tìm kiếm + checkbox ẩn/hiện
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(0, 5, 10, 5));

        txtSearchDish = new JTextField();
        txtSearchDish.setPreferredSize(new Dimension(200, 32));

        chkShowInactive = new JCheckBox("Hiện món đã ẩn");
        chkShowInactive.setOpaque(false);
        chkShowInactive.setFont(new Font("Roboto", Font.PLAIN, 12));
        chkShowInactive.setForeground(new Color(120, 130, 140));

        JPanel leftTool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTool.setOpaque(false);
        leftTool.add(new JLabel(" Tìm kiếm: "));
        leftTool.add(txtSearchDish);
        leftTool.add(chkShowInactive);
        hdr.add(leftTool, BorderLayout.WEST);

        // Nút Đồng bộ thực đơn (Sync)
        JButton btnSync = new JButton(" Đồng bộ thực đơn");
        btnSync.setIcon(IconFontSwing.buildIcon(FontAwesome.REFRESH, 14, PRIMARY_COLOR));
        btnSync.setFocusPainted(false);
        btnSync.addActionListener(e -> syncNewMenuItems());
        if (!bangGia.isHoatDong()) btnSync.setEnabled(false);
        hdr.add(btnSync, BorderLayout.EAST);

        // RowFilter kết hợp: từ khóa + trạng thái ẩn/hiện
        Runnable updateFilter = () -> {
            String kw = txtSearchDish.getText().trim().toLowerCase();
            boolean showHidden = chkShowInactive.isSelected();

            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String name = entry.getStringValue(0).toLowerCase();
                    if (!kw.isEmpty() && !name.contains(kw)) return false;

                    if (!showHidden) {
                        Boolean dishActive = (Boolean) entry.getModel().getValueAt(entry.getIdentifier(), 4);
                        Boolean sizeActive = (Boolean) entry.getModel().getValueAt(entry.getIdentifier(), 5);
                        if ((dishActive != null && !dishActive) || (sizeActive != null && !sizeActive)) return false;
                    }
                    return true;
                }
            });
        };

        txtSearchDish.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { updateFilter.run(); }
        });
        chkShowInactive.addActionListener(e -> updateFilter.run());
        updateFilter.run();

        pnlTable.add(hdr, BorderLayout.NORTH);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        pnlTable.add(new JScrollPane(table), BorderLayout.CENTER);
        return pnlTable;
    }

    private JPanel createBatchToolsPanel() {
        JPanel toolBox = new JPanel();
        toolBox.setLayout(new BoxLayout(toolBox, BoxLayout.Y_AXIS));
        toolBox.setBackground(Color.WHITE);
        toolBox.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            BorderFactory.createTitledBorder(
                new EmptyBorder(10, 10, 10, 10),
                "CÔNG CỤ NHANH",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Roboto", Font.BOLD, 13), PRIMARY_COLOR)
        ));
        toolBox.setPreferredSize(new Dimension(300, 0));

        // 1. Sao chép (Clone)
        JLabel lblClone = new JLabel("1. Sao chép nguồn giá:");
        lblClone.setFont(new Font("Roboto", Font.BOLD, 12));
        toolBox.add(lblClone);
        toolBox.add(Box.createVerticalStrut(5));

        cbCloneSource = new JComboBox<>();
        List<BangGia> lists = priceController.getAllBangGia();
        for (BangGia b : lists) {
            // Chỉ hiện bảng chưa bị ẩn và không phải chính nó
            if (!b.getMaBangGia().equals(bangGia.getMaBangGia()) && b.isHoatDong()) {
                cbCloneSource.addItem(b);
            }
        }
        cbCloneSource.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        toolBox.add(cbCloneSource);
        toolBox.add(Box.createVerticalStrut(5));

        JButton btnClone = createStyledBtn(" Thực hiện Sao chép", FontAwesome.FILES_O, PRIMARY_COLOR);
        btnClone.addActionListener(e -> applyMemoryClone());
        toolBox.add(btnClone);

        toolBox.add(Box.createVerticalStrut(30));

        // 2. Điều chỉnh (Batch Adjust)
        JLabel lblAdjust = new JLabel("2. Điều chỉnh giá hàng loạt:");
        lblAdjust.setFont(new Font("Roboto", Font.BOLD, 12));
        toolBox.add(lblAdjust);
        toolBox.add(Box.createVerticalStrut(10));

        JPanel adjRow = new JPanel(new GridLayout(2, 2, 5, 8));
        adjRow.setOpaque(false);
        adjRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        adjRow.add(new JLabel("Tăng/Giảm %:"));
        txtPercent = new JTextField("0");
        adjRow.add(txtPercent);
        adjRow.add(new JLabel("Cộng tiền VNĐ:"));
        txtFixed = new JTextField("0");
        adjRow.add(txtFixed);
        toolBox.add(adjRow);
        toolBox.add(Box.createVerticalStrut(15));

        JButton btnAdjust = createStyledBtn(" Áp dụng tính toán", FontAwesome.MAGIC, new Color(39, 174, 96));
        btnAdjust.addActionListener(e -> applyMemoryBatchAdjust());
        toolBox.add(btnAdjust);

        // LOCKDOWN: khóa tất cả nếu bảng đã ẩn
        if (!bangGia.isHoatDong()) {
            cbCloneSource.setEnabled(false);
            btnClone.setEnabled(false);
            txtPercent.setEditable(false);
            txtFixed.setEditable(false);
            btnAdjust.setEnabled(false);
        }

        return toolBox;
    }

    private JPanel createFooterPanel() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        f.setOpaque(false);

        JButton btnSave = new JButton(" LƯU TOÀN BỘ");
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Roboto", Font.BOLD, 13));
        btnSave.setIcon(IconFontSwing.buildIcon(FontAwesome.FLOPPY_O, 16, Color.WHITE));
        btnSave.addActionListener(e -> handleSaveAll());

        JButton btnClose = new JButton(" ĐÓNG");
        btnClose.setPreferredSize(new Dimension(100, 40));
        btnClose.addActionListener(e -> dispose());

        f.add(btnClose);
        if (bangGia.isHoatDong()) {
            f.add(btnSave);
        } else {
            JLabel lblLock = new JLabel("Bảng giá này đã bị khóa để bảo vệ lịch sử.");
            lblLock.setForeground(Color.RED);
            lblLock.setFont(new Font("Roboto", Font.ITALIC, 13));
            f.add(lblLock);
        }
        return f;
    }

    private void fillGeneralInfo() {
        txtMa.setText(bangGia.getMaBangGia());
        txtTen.setText(bangGia.getTenBangGia() != null ? bangGia.getTenBangGia() : "");
        if (bangGia.getNgayBatDau() != null) dcBatDau.setDate(java.sql.Date.valueOf(bangGia.getNgayBatDau()));
        if (bangGia.getNgayKetThuc() != null) dcKetThuc.setDate(java.sql.Date.valueOf(bangGia.getNgayKetThuc()));
        chkStatus.setSelected(bangGia.isTrangThai());
    }

    private void loadTableData() {
        model.setRowCount(0);
        List<Mon> dishes = menuController.getAllMon();

        // Chi tiết giá hiện tại (nếu edit)
        List<BangGiaChiTiet> currentDetails = priceController.getDetailsOf(bangGia.getMaBangGia());

        // Chi tiết giá nguồn (nếu tạo mới theo kiểu clone)
        List<BangGiaChiTiet> sourceDetails = null;
        if (!isEditMode && sourceMaBG != null) {
            sourceDetails = priceController.getDetailsOf(sourceMaBG);
        }

        for (Mon m : dishes) {
            List<Size> sizes = menuController.getAllSizesOfMon(m.getMaMon());
            for (Size s : sizes) {
                double price = 0.0;

                // Ưu tiên 1: giá thật của chính bảng này (edit mode)
                for (BangGiaChiTiet d : currentDetails) {
                    if (d.getMaSize().equals(s.getMaSize())) {
                        price = d.getGiaBan();
                        break;
                    }
                }

                // Ưu tiên 2: giá nguồn (clone khi tạo mới)
                if (price == 0 && sourceDetails != null) {
                    for (BangGiaChiTiet sd : sourceDetails) {
                        if (sd.getMaSize().equals(s.getMaSize())) {
                            price = sd.getGiaBan();
                            break;
                        }
                    }
                }

                // isTrangThai() luôn true trong TARGET (không có DB column)
                model.addRow(new Object[]{m.getTenMon(), s.getTenSize(), price, s.getMaSize(), m.isTrangThai(), s.isTrangThai()});
            }
        }

        // Cảnh báo nếu bảng giá không đầy đủ
        if (bangGia.isHoatDong() && isEditMode && !priceController.isBangGiaComplete(bangGia.getMaBangGia())) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    "<html><b style='color:orange'>⚠ Cảnh báo: Bảng giá này đang thiếu món mới!</b><br>" +
                    "Hệ thống phát hiện bạn vừa thêm món mới vào thực đơn nhưng chưa có trong bảng giá này.<br>" +
                    "Hãy nhấn nút <b>'Đồng bộ thực đơn'</b> ở góc trên bên phải để bổ sung ngay.</html>",
                    "Thiếu dữ liệu giá", JOptionPane.WARNING_MESSAGE)
            );
        }
    }

    // --- ACTION LOGIC (Memory-first, lưu thật khi nhấn LƯU TOÀN BỘ) ---

    private void applyMemoryClone() {
        BangGia source = (BangGia) cbCloneSource.getSelectedItem();
        if (source == null) return;

        int opt = JOptionPane.showConfirmDialog(this,
            "Lấy giá từ [" + source.getTenBangGia() + "]?\nThao tác này sẽ áp mảng giá lên lưới hiện tại.",
            "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            List<BangGiaChiTiet> srcData = priceController.getDetailsOf(source.getMaBangGia());
            for (int i = 0; i < model.getRowCount(); i++) {
                String tbSize = (String) model.getValueAt(i, 3);
                for (BangGiaChiTiet s : srcData) {
                    if (s.getMaSize().equals(tbSize)) {
                        model.setValueAt(s.getGiaBan(), i, 2);
                        break;
                    }
                }
            }
            JOptionPane.showMessageDialog(this,
                "Đã sao chép lên bảng. Hãy nhấn LƯU TOÀN BỘ để ghi đè dữ liệu thật!");
        }
    }

    private void applyMemoryBatchAdjust() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        try {
            double p = Double.parseDouble(txtPercent.getText().trim()) / 100.0;
            double f = Double.parseDouble(txtFixed.getText().trim());

            for (int i = 0; i < model.getRowCount(); i++) {
                Object val = model.getValueAt(i, 2);
                if (val == null) continue;
                double currentPrice = Double.parseDouble(val.toString());
                if (currentPrice > 0) {
                    double newPrice = currentPrice * (1 + p) + f;
                    newPrice = Math.round(newPrice / 1000.0) * 1000.0;
                    model.setValueAt(newPrice, i, 2);
                }
            }
            JOptionPane.showMessageDialog(this,
                "Đã tính toán làm tròn lên bảng. Nhấn LƯU TOÀN BỘ để hoàn tất!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Dữ liệu phần trăm/số tiền không hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void syncNewMenuItems() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        List<Mon> allDishes = menuController.getAllMon();
        int count = 0;

        for (Mon m : allDishes) {
            List<Size> sizes = menuController.getAllSizesOfMon(m.getMaMon());
            for (Size s : sizes) {
                boolean found = false;
                for (int i = 0; i < model.getRowCount(); i++) {
                    if (model.getValueAt(i, 3).equals(s.getMaSize())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    model.addRow(new Object[]{m.getTenMon(), s.getTenSize(), 0.0, s.getMaSize(), m.isTrangThai(), s.isTrangThai()});
                    count++;
                }
            }
        }

        if (count > 0) {
            JOptionPane.showMessageDialog(this,
                "Đã tìm thấy và bổ sung " + count + " món/size mới vào cuối danh sách.");
        } else {
            JOptionPane.showMessageDialog(this,
                "Thực đơn hiện tại đã đầy đủ, không có món mới cần đồng bộ.");
        }
    }

    private void handleSaveAll() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        // 1. Validate tên bảng giá
        if (ValidationUtils.isEmpty(txtTen.getText())) {
            JOptionPane.showMessageDialog(this, "Tên bảng giá không được để trống!");
            return;
        }

        try {
            bangGia.setTenBangGia(txtTen.getText().trim());

            java.util.Date dStart = dcBatDau.getDate();
            if (dStart == null) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được để trống!");
                return;
            }
            bangGia.setNgayBatDau(new java.sql.Date(dStart.getTime()).toLocalDate());

            java.util.Date dEnd = dcKetThuc.getDate();
            bangGia.setNgayKetThuc(dEnd != null ? new java.sql.Date(dEnd.getTime()).toLocalDate() : null);

            bangGia.setTrangThai(chkStatus.isSelected());
            bangGia.setHoatDong(true); // Bảng mới luôn hoạt động

            // Lưu thông tin bảng giá chính (master)
            boolean isSavedMaster = priceController.saveBangGia(bangGia, isEditMode);
            if (!isSavedMaster) {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu Bảng Giá chung!");
                return;
            }

            // 2. Lưu chi tiết giá (detail)
            List<BangGiaChiTiet> currentDbDetails = priceController.getDetailsOf(bangGia.getMaBangGia());

            for (int i = 0; i < model.getRowCount(); i++) {
                String maSize = (String) model.getValueAt(i, 3);
                Object val = model.getValueAt(i, 2);
                if (val == null) continue;

                double guiPrice = Double.parseDouble(val.toString());

                // VALIDATION: Chặn giá 0đ
                if (guiPrice <= 0) {
                    String tenMon = (String) model.getValueAt(i, 0);
                    JOptionPane.showMessageDialog(this,
                        "Phát hiện món [" + tenMon + "] có giá bằng 0.\nBạn phải điền giá hợp lệ (>0) trước khi lưu!",
                        "Lỗi dữ liệu giá", JOptionPane.ERROR_MESSAGE);
                    table.setRowSelectionInterval(i, i);
                    table.scrollRectToVisible(table.getCellRect(i, 0, true));
                    return;
                }

                // Tìm xem đã có trong DB chưa
                BangGiaChiTiet existing = null;
                for (BangGiaChiTiet dbD : currentDbDetails) {
                    if (dbD.getMaSize().equals(maSize)) {
                        existing = dbD;
                        break;
                    }
                }

                if (existing != null) {
                    // Cập nhật nếu giá thay đổi
                    if (existing.getGiaBan() != guiPrice) {
                        existing.setGiaBan(guiPrice);
                        priceController.saveDetail(existing, true);
                    }
                } else {
                    // Thêm mới
                    BangGiaChiTiet nw = new BangGiaChiTiet(
                        priceController.generateNextMaBGCT(),
                        guiPrice, maSize, bangGia.getMaBangGia()
                    );
                    priceController.saveDetail(nw, false);
                }
            }

            confirmed = true;
            JOptionPane.showMessageDialog(this, "Đã lưu thành công toàn bộ Bảng giá & Chi tiết!");
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu: " + e.getMessage());
        }
    }

    public boolean isConfirmed() { return confirmed; }

    // --- RENDERER ---
    private class PriceRowRenderer extends DefaultTableCellRenderer {
        private final Color COLOR_INACTIVE = new Color(150, 160, 170);
        private final Font FONT_ITALIC = new Font("Roboto", Font.ITALIC, 14);
        private final Font FONT_NORMAL = new Font("Roboto", Font.PLAIN, 14);

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);

            int modelRow = t.convertRowIndexToModel(r);
            Boolean dishActive = (Boolean) t.getModel().getValueAt(modelRow, 4);
            Boolean sizeActive = (Boolean) t.getModel().getValueAt(modelRow, 5);

            boolean isInactive = (dishActive != null && !dishActive) || (sizeActive != null && !sizeActive);

            if (isInactive) {
                comp.setForeground(COLOR_INACTIVE);
                comp.setFont(FONT_ITALIC);
                if (!isS) comp.setBackground(new Color(242, 242, 242));
            } else {
                comp.setForeground(t.getForeground());
                comp.setFont(FONT_NORMAL);
                if (!isS) comp.setBackground(t.getBackground());
            }

            if (c == 2) {
                setHorizontalAlignment(RIGHT);
                if (v instanceof Double) {
                    setText(String.format("%,.0f", (Double) v));
                }
            } else {
                setHorizontalAlignment(LEFT);
            }

            return comp;
        }
    }

    private JButton createStyledBtn(String text, FontAwesome icon, Color color) {
        JButton b = new JButton(text);
        b.setIcon(IconFontSwing.buildIcon(icon, 14, Color.WHITE));
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Roboto", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }
}
