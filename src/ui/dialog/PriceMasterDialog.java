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
    private boolean isDirty   = false; // [IMP-01] Track thay đổi chưa lưu

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
    private JCheckBox chkMissingPrice;
    private Runnable  updateFilter;  // [BUG-07 FIX] Field để syncNewMenuItems có thể gọi refresh filter

    // UI Components - Batch Actions
    private JComboBox<BangGia> cbCloneSource;
    private JTextField txtPercent, txtFixed;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public PriceMasterDialog(Frame parent, BangGia bg, boolean isEditMode, String sourceMaBG) {
        super(parent, isEditMode ? "Tùy chỉnh Bảng Giá" : "Tạo Bảng Giá Mới", true);
        this.bangGia = bg;
        this.isEditMode = isEditMode;
        this.sourceMaBG = sourceMaBG;

        initUI();
        fillGeneralInfo();
        loadTableData();

        // [IMP-01] Theo dõi thay đổi trên lưới — đánh dấu isDirty
        model.addTableModelListener(e -> isDirty = true);

        // [BUG-02 FIX] Hiện cảnh báo SAU khi dialog đã visible (windowOpened)
        if (bangGia.isHoatDong() && isEditMode
                && !priceController.isBangGiaComplete(bangGia.getMaBangGia())) {
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowOpened(java.awt.event.WindowEvent e) {
                    JOptionPane.showMessageDialog(PriceMasterDialog.this,
                            "<html><b style='color:orange'>⚠ Cảnh báo: Bảng giá đang thiếu món mới!</b><br>"
                                    + "Hệ thống phát hiện có món mới trong thực đơn nhưng chưa có trong bảng giá này.<br>"
                                    + "Hãy nhấn nút <b>'Đồng bộ thực đơn'</b> ở góc trên bên phải để bổ sung ngay.</html>",
                            "Thiếu dữ liệu giá", JOptionPane.WARNING_MESSAGE);
                    removeWindowListener(this); // Chỉ show 1 lần
                }
            });
        }
    }

    // [IMP-01] Xác nhận khi đóng mà chưa lưu
    @Override
    public void dispose() {
        if (isDirty && !confirmed) {
            int r = JOptionPane.showConfirmDialog(this,
                    "Bạn có thay đổi giá chưa được lưu.\nBạn chắc chắn muốn thoát không?",
                    "Xác nhận thoát", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }
        super.dispose();
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

        JPanel topWrapper = new JPanel(new GridLayout(1, 2, 20, 0));
        topWrapper.setOpaque(false);
        topWrapper.add(createGeneralInfoPanel());
        topWrapper.add(createBatchToolsPanel());

        body.add(topWrapper, BorderLayout.NORTH);

        body.add(createTablePanel(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createGeneralInfoPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 20, 15, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblMa = new JLabel("Mã bảng giá:");
        lblMa.setFont(new Font("Roboto", Font.BOLD, 12));
        pnl.add(lblMa, gbc);
        
        gbc.gridx = 1;
        JLabel lblTen = new JLabel("Tên bảng giá (*):");
        lblTen.setFont(new Font("Roboto", Font.BOLD, 12));
        pnl.add(lblTen, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        txtMa = new JTextField();
        txtMa.setEditable(false);
        txtMa.setPreferredSize(new Dimension(0, 32));
        pnl.add(txtMa, gbc);

        gbc.gridx = 1;
        txtTen = new JTextField();
        txtTen.setPreferredSize(new Dimension(0, 32));
        pnl.add(txtTen, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.insets = new Insets(10, 5, 4, 5);
        JLabel lblBD = new JLabel("Ngày bắt đầu:");
        lblBD.setFont(new Font("Roboto", Font.BOLD, 12));
        pnl.add(lblBD, gbc);

        gbc.gridx = 1;
        JLabel lblKT = new JLabel("Ngày kết thúc:");
        lblKT.setFont(new Font("Roboto", Font.BOLD, 12));
        pnl.add(lblKT, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.insets = new Insets(4, 5, 4, 5);
        dcBatDau = new JDateChooser();
        dcBatDau.setPreferredSize(new Dimension(0, 32));
        dcBatDau.setDateFormatString("yyyy-MM-dd");
        pnl.add(dcBatDau, gbc);

        gbc.gridx = 1;
        dcKetThuc = new JDateChooser();
        dcKetThuc.setPreferredSize(new Dimension(0, 32));
        dcKetThuc.setDateFormatString("yyyy-MM-dd");
        pnl.add(dcKetThuc, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 0, 5);
        chkStatus = new JCheckBox("Kích hoạt ngay (Áp dụng cho POS)");
        chkStatus.setFont(new Font("Roboto", Font.ITALIC, 12));
        chkStatus.setOpaque(false);
        pnl.add(chkStatus, gbc);

        // Logic giới hạn ngày
        dcBatDau.addPropertyChangeListener("date", evt -> {
            java.util.Date minD = dcBatDau.getDate();
            if (minD != null) {
                dcKetThuc.setMinSelectableDate(minD);
            }
        });

        if (!bangGia.isHoatDong()) {
            txtTen.setEditable(false); dcBatDau.setEnabled(false);
            dcKetThuc.setEnabled(false); chkStatus.setEnabled(false);
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
                        new Font("Roboto", Font.BOLD, 13), PRIMARY_COLOR)));

        // Cột ẩn: col 3=maSize, col 4=isDishActive, col 5=isSizeActive
        String[] cols = { "Tên món ăn", "Kích thước", "Giá bán (VNĐ)", "maSize", "isDishActive", "isSizeActive" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return bangGia.isHoatDong() && c == 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4 || columnIndex == 5) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                // [FIX] Col 2 (giá bán): luôn lưu dưới dạng Double
                // JTable cell editor mặc định trả về String — nếu lưu String thì filter/renderer sẽ ClassCastException
                if (column == 2) {
                    if (aValue instanceof String) {
                        String s = ((String) aValue).trim().replace(",", "").replace(".", "");
                        if (s.isEmpty()) {
                            aValue = null;
                        } else {
                            try {
                                aValue = Double.parseDouble(s);
                            } catch (NumberFormatException ex) {
                                aValue = null; // Giá không hợp lệ → xóa
                            }
                        }
                    }
                }
                super.setValueAt(aValue, row, column);
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
        chkShowInactive.setSelected(true);
        chkShowInactive.setOpaque(false);
        chkShowInactive.setFont(new Font("Roboto", Font.PLAIN, 12));
        chkShowInactive.setForeground(new Color(120, 130, 140));

        chkMissingPrice = new JCheckBox("Lọc món chưa có giá");
        chkMissingPrice.setOpaque(false);
        chkMissingPrice.setFont(new Font("Roboto", Font.BOLD, 12));
        chkMissingPrice.setForeground(new Color(231, 76, 60)); // Màu đỏ cảnh báo

        JPanel leftTool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTool.setOpaque(false);
        leftTool.add(new JLabel(" Tìm kiếm: "));
        leftTool.add(txtSearchDish);
        leftTool.add(chkShowInactive);
        leftTool.add(chkMissingPrice);
        hdr.add(leftTool, BorderLayout.WEST);

        // Nút Đồng bộ thực đơn (Sync)
        JButton btnSync = new JButton(" Làm mới menu");
        btnSync.setIcon(IconFontSwing.buildIcon(FontAwesome.REFRESH, 14, PRIMARY_COLOR));
        btnSync.setFocusPainted(false);
        btnSync.addActionListener(e -> syncNewMenuItems());
        if (!bangGia.isHoatDong())
            btnSync.setEnabled(false);
        hdr.add(btnSync, BorderLayout.EAST);

        // [BUG-07 FIX] Gán vào field thay vì local variable
        updateFilter = () -> {
            String kw = txtSearchDish.getText().trim().toLowerCase();
            boolean showHidden = chkShowInactive.isSelected();
            boolean showMissingOnly = chkMissingPrice.isSelected();

            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String name = entry.getStringValue(0).toLowerCase();
                    if (!kw.isEmpty() && !name.contains(kw))
                        return false;

                    if (!showHidden) {
                        Boolean dishActive = (Boolean) entry.getModel().getValueAt(entry.getIdentifier(), 4);
                        Boolean sizeActive = (Boolean) entry.getModel().getValueAt(entry.getIdentifier(), 5);
                        if ((dishActive != null && !dishActive) || (sizeActive != null && !sizeActive))
                            return false;
                    }

                    if (showMissingOnly) {
                        Object priceObj = entry.getModel().getValueAt(entry.getIdentifier(), 2);
                        // [FIX] Dùng helper an toàn — tránh ClassCastException khi priceObj là String
                        if (parsePrice(priceObj) > 0)
                            return false; // Có giá hợp lệ → ẩn khỏi bộ lọc "chưa có giá"
                    }

                    return true;
                }
            });
        };

        txtSearchDish.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateFilter.run();
            }
        });
        chkShowInactive.addActionListener(e -> updateFilter.run());
        chkMissingPrice.addActionListener(e -> updateFilter.run());
        updateFilter.run();

        pnlTable.add(hdr, BorderLayout.NORTH);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        pnlTable.add(new JScrollPane(table), BorderLayout.CENTER);
        return pnlTable;
    }

    private JPanel createBatchToolsPanel() {
        JPanel toolBox = new JPanel(new GridBagLayout());
        toolBox.setBackground(Color.WHITE);
        toolBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230)),
                BorderFactory.createTitledBorder(
                        new EmptyBorder(10, 20, 10, 20),
                        "CÔNG CỤ NHANH",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Roboto", Font.BOLD, 13), PRIMARY_COLOR)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        // Clone row
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblCl = new JLabel("1. Sao chép giá từ:");
        lblCl.setFont(new Font("Roboto", Font.BOLD, 12));
        toolBox.add(lblCl, gbc);

        gbc.gridy = 1;
        JPanel pnlClone = new JPanel(new BorderLayout(10, 0));
        pnlClone.setOpaque(false);
        cbCloneSource = new JComboBox<>();
        List<BangGia> lists = priceController.getAllBangGia();
        for (BangGia b : lists) {
            if (!b.getMaBangGia().equals(bangGia.getMaBangGia()) && b.isHoatDong()) cbCloneSource.addItem(b);
        }
        cbCloneSource.setPreferredSize(new Dimension(0, 32));
        pnlClone.add(cbCloneSource, BorderLayout.CENTER);

        JButton btnClone = createStyledBtn("Sao chép", FontAwesome.FILES_O, PRIMARY_COLOR);
        btnClone.setPreferredSize(new Dimension(100, 32));
        btnClone.addActionListener(e -> applyMemoryClone());
        pnlClone.add(btnClone, BorderLayout.EAST);
        toolBox.add(pnlClone, gbc);

        // Spacing
        gbc.gridy = 2;
        toolBox.add(Box.createVerticalStrut(15), gbc);

        // Adjust row
        gbc.gridy = 3;
        JLabel lblAdj = new JLabel("2. Điều chỉnh giá hàng loạt:");
        lblAdj.setFont(new Font("Roboto", Font.BOLD, 12));
        toolBox.add(lblAdj, gbc);

        gbc.gridy = 4;
        JPanel pnlAdj = new JPanel(new GridBagLayout());
        pnlAdj.setOpaque(false);
        GridBagConstraints gbcAdj = new GridBagConstraints();
        gbcAdj.fill = GridBagConstraints.HORIZONTAL;
        gbcAdj.insets = new Insets(0, 0, 0, 10);
        
        gbcAdj.weightx = 0.2;
        pnlAdj.add(new JLabel("Tăng/Giảm %:"), gbcAdj);
        gbcAdj.weightx = 0.3;
        txtPercent = new JTextField("0");
        txtPercent.setPreferredSize(new Dimension(0, 32));
        pnlAdj.add(txtPercent, gbcAdj);

        gbcAdj.weightx = 0.2;
        pnlAdj.add(new JLabel("+VNĐ:"), gbcAdj);
        gbcAdj.weightx = 0.3;
        gbcAdj.insets = new Insets(0, 0, 0, 0);
        txtFixed = new JTextField("0");
        txtFixed.setPreferredSize(new Dimension(0, 32));
        pnlAdj.add(txtFixed, gbcAdj);
        
        toolBox.add(pnlAdj, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(10, 5, 5, 5);
        JButton btnAdjust = createStyledBtn("Áp dụng tính toán hàng loạt", FontAwesome.MAGIC, new Color(39, 174, 96));
        btnAdjust.setPreferredSize(new Dimension(0, 35));
        btnAdjust.addActionListener(e -> applyMemoryBatchAdjust());
        toolBox.add(btnAdjust, gbc);

        if (!bangGia.isHoatDong()) {
            cbCloneSource.setEnabled(false); btnClone.setEnabled(false);
            txtPercent.setEditable(false); txtFixed.setEditable(false);
            btnAdjust.setEnabled(false);
        } else if (cbCloneSource.getItemCount() == 0) {
            // [IMP-03] Disable nút Sao chép khi không có bảng giá nguồn nào
            btnClone.setEnabled(false);
            cbCloneSource.setEnabled(false);
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
        if (bangGia.getNgayBatDau() != null)
            dcBatDau.setDate(java.sql.Date.valueOf(bangGia.getNgayBatDau()));
        if (bangGia.getNgayKetThuc() != null)
            dcKetThuc.setDate(java.sql.Date.valueOf(bangGia.getNgayKetThuc()));
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
                Double price = null;

                // Ưu tiên 1: giá thật của chính bảng này (edit mode)
                for (BangGiaChiTiet d : currentDetails) {
                    if (d.getMaSize().equals(s.getMaSize())) {
                        price = d.getGiaBan();
                        break;
                    }
                }

                // Ưu tiên 2: giá nguồn (clone khi tạo mới)
                if (price == null && sourceDetails != null) {
                    for (BangGiaChiTiet sd : sourceDetails) {
                        if (sd.getMaSize().equals(s.getMaSize())) {
                            if (sd.getGiaBan() > 0) price = sd.getGiaBan();
                            break;
                        }
                    }
                }

                // isTrangThai() luôn true trong TARGET (không có DB column)
                model.addRow(new Object[] { m.getTenMon(), s.getTenSize(), price, s.getMaSize(), m.isTrangThai(),
                        s.isTrangThai() });
            }
        }
    }

    // --- ACTION LOGIC (Memory-first, lưu thật khi nhấn LƯU TOÀN BỘ) ---

    private void applyMemoryClone() {
        BangGia source = (BangGia) cbCloneSource.getSelectedItem();
        if (source == null)
            return;

        int opt = JOptionPane.showConfirmDialog(this,
                "Lấy giá từ [" + source.getTenBangGia() + "]?\nThao tác này sẽ áp mảng giá lên lưới hiện tại.",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            List<BangGiaChiTiet> srcData = priceController.getDetailsOf(source.getMaBangGia());
            for (int i = 0; i < model.getRowCount(); i++) {
                String tbSize = (String) model.getValueAt(i, 3);
                for (BangGiaChiTiet s : srcData) {
                    if (s.getMaSize().equals(tbSize)) {
                        // [BUG-04 FIX] Chỉ ghi đè khi giá nguồn > 0 — tránh xoá giá đang có
                        if (s.getGiaBan() > 0) {
                            model.setValueAt(s.getGiaBan(), i, 2);
                        }
                        break;
                    }
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Đã sao chép lên bảng. Hãy nhấn LƯU TOÀN BỘ để ghi đè dữ liệu thật!");
        }
    }

    private void applyMemoryBatchAdjust() {
        if (table.isEditing())
            table.getCellEditor().stopCellEditing();

        try {
            double p = Double.parseDouble(txtPercent.getText().trim()) / 100.0;
            double f = Double.parseDouble(txtFixed.getText().trim());

            for (int i = 0; i < model.getRowCount(); i++) {
                Object val = model.getValueAt(i, 2);
                if (val == null)
                    continue;
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
        if (table.isEditing())
            table.getCellEditor().stopCellEditing();

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
                    model.addRow(new Object[] { m.getTenMon(), s.getTenSize(), 0.0, s.getMaSize(), m.isTrangThai(),
                            s.isTrangThai() });
                    count++;
                }
            }
        }

        if (count > 0) {
            // [BUG-07 FIX] Refresh filter để hiện món mới trong bộ lọc đang bật
            if (updateFilter != null) updateFilter.run();
            JOptionPane.showMessageDialog(this,
                    "Đã tìm thấy và bổ sung " + count + " món/size mới vào cuối danh sách.");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Thực đơn hiện tại đã đầy đủ, không có món mới cần đồng bộ.");
        }
    }

    private void handleSaveAll() {
        if (table.isEditing())
            table.getCellEditor().stopCellEditing();

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

            // 2. Kiểm tra tính toàn vẹn (Validation All-or-Nothing trước)
            for (int i = 0; i < model.getRowCount(); i++) {
                Object val = model.getValueAt(i, 2);
                Boolean dishActive = (Boolean) model.getValueAt(i, 4);
                Boolean sizeActive = (Boolean) model.getValueAt(i, 5);

                // Nếu món đang hoạt động mà chưa nhập giá thì CHẶN LUÔN
                if (dishActive != null && dishActive && sizeActive != null && sizeActive) {
                    if (parsePrice(val) <= 0) {
                        String tenMon = (String) model.getValueAt(i, 0);
                        String sizeMon = (String) model.getValueAt(i, 1);
                        
                        // Bật bộ lọc Missing Price để sếp nhìn thấy ngay
                        chkMissingPrice.setSelected(true);
                        
                        JOptionPane.showMessageDialog(this,
                                "<html><b style='color:red'>LỖI DỮ LIỆU: TỒN TẠI MÓN CHƯA ĐỊNH GIÁ!</b><br><br>"
                                        + "Món <b>[" + tenMon + " - " + sizeMon + "]</b> chưa được thiết lập giá bán.<br>"
                                        + "Hệ thống sẽ <b>không lưu</b> bất kỳ dữ liệu nào cho đến khi toàn bộ thực đơn được điền giá đầy đủ (Tránh bán 0đ).</html>",
                                "Stop - Yêu cầu nhập giá", JOptionPane.ERROR_MESSAGE);
                        
                        // Scroll tới item lỗi nếu vẫn còn hiển thị trên lưới
                        try {
                            int viewRow = table.convertRowIndexToView(i);
                            if (viewRow >= 0) {
                                table.setRowSelectionInterval(viewRow, viewRow);
                                table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                            }
                        } catch (Exception ignored) {}
                        
                        return; // Ngừng quá trình lưu
                    }
                }
            }

            // Lưu thông tin bảng giá chính (master)
            boolean isSavedMaster = priceController.saveBangGia(bangGia, isEditMode);
            if (!isSavedMaster) {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu Bảng Giá chung!");
                return;
            }

            // 3. Lưu chi tiết giá (detail) sau khi đã validate an toàn
            List<BangGiaChiTiet> currentDbDetails = priceController.getDetailsOf(bangGia.getMaBangGia());

            for (int i = 0; i < model.getRowCount(); i++) {
                String maSize = (String) model.getValueAt(i, 3);
                Object val = model.getValueAt(i, 2);
                Boolean dishActive = (Boolean) model.getValueAt(i, 4);
                Boolean sizeActive = (Boolean) model.getValueAt(i, 5);
                
                // Bỏ qua nếu món bị ẩn VÀ không có giá
                if ((dishActive == null || !dishActive || sizeActive == null || !sizeActive) && parsePrice(val) <= 0) {
                    continue;
                }

                double guiPrice = parsePrice(val);

                // Tìm xem đã có trong DB chưa
                BangGiaChiTiet existing = null;
                for (BangGiaChiTiet dbD : currentDbDetails) {
                    if (dbD.getMaSize().equals(maSize)) {
                        existing = dbD;
                        break;
                    }
                }

                if (existing != null) {
                    // [BUG-05 FIX] Dùng Math.abs thay vì != để tránh floating-point trap
                    if (Math.abs(existing.getGiaBan() - guiPrice) > 0.01) {
                        existing.setGiaBan(guiPrice);
                        priceController.saveDetail(existing, true);
                    }
                } else {
                    BangGiaChiTiet nw = new BangGiaChiTiet(
                            priceController.generateNextMaBGCT(),
                            guiPrice, maSize, bangGia.getMaBangGia());
                    priceController.saveDetail(nw, false);
                }
            }

            isDirty   = false; // [IMP-01] Reset dirty flag sau khi lưu thành công
            confirmed = true;
            JOptionPane.showMessageDialog(this, "Đã lưu thành công toàn bộ Bảng giá & Chi tiết!");
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu: " + e.getMessage());
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    // --- RENDERER ---
    private class PriceRowRenderer extends DefaultTableCellRenderer {
        private final Color COLOR_INACTIVE    = new Color(150, 160, 170);
        private final Color COLOR_BG_INACTIVE = new Color(242, 242, 242);
        private final Color COLOR_FG_WARN     = new Color(192, 57, 43);
        private final Color COLOR_BG_WARN     = new Color(255, 235, 238);
        private final Font  FONT_ITALIC = new Font("Roboto", Font.ITALIC, 14);
        private final Font  FONT_NORMAL = new Font("Roboto", Font.PLAIN, 14);
        private final Font  FONT_BOLD   = new Font("Roboto", Font.BOLD, 14);

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
            comp.setFont(FONT_NORMAL);
            comp.setForeground(isS ? t.getSelectionForeground() : t.getForeground());
            comp.setBackground(isS ? t.getSelectionBackground() : t.getBackground());
            setHorizontalAlignment(c == 2 ? RIGHT : LEFT);

            int modelRow = t.convertRowIndexToModel(r);
            Boolean dishActive = (Boolean) t.getModel().getValueAt(modelRow, 4);
            Boolean sizeActive = (Boolean) t.getModel().getValueAt(modelRow, 5);
            boolean isInactive = (dishActive != null && !dishActive) || (sizeActive != null && !sizeActive);

            Object priceObject = t.getModel().getValueAt(modelRow, 2);
            // [FIX] Dùng parsePrice() an toàn — xử lý cả null, Double, String
            boolean isMissingPrice = parsePrice(priceObject) <= 0;

            if (isInactive) {
                comp.setFont(FONT_ITALIC);
                if (!isS) {
                    comp.setForeground(COLOR_INACTIVE);
                    comp.setBackground(COLOR_BG_INACTIVE);
                }
            } else if (isMissingPrice) {
                comp.setFont(FONT_BOLD);
                if (!isS) {
                    comp.setForeground(COLOR_FG_WARN);
                    comp.setBackground(COLOR_BG_WARN);
                }
            }
            // else: đã reset về mặc định ở đầu method rồi

            // Bước 4: Format text cho cột Giá bán
            if (c == 2) {
                if (isMissingPrice && !isInactive) {
                    setText(" ⚠ Chưa có giá ");
                } else if (v instanceof Double && (Double) v > 0) {
                    setText(String.format("%,.0f", (Double) v));
                } else {
                    setText("");
                }
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

    /**
     * [HELPER] Parse giá bán an toàn từ Object (có thể là Double, String, hoặc null).
     * Dùng cho RowFilter và bất kỳ chỗ nào cần đọc giá từ model.
     * @return giá dạng double, 0.0 nếu null / không hợp lệ
     */
    private static double parsePrice(Object priceObj) {
        if (priceObj == null) return 0.0;
        if (priceObj instanceof Double) return (Double) priceObj;
        if (priceObj instanceof Number) return ((Number) priceObj).doubleValue();
        try {
            String s = priceObj.toString().trim().replace(",", "").replace(".", "");
            return s.isEmpty() ? 0.0 : Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}
