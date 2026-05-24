package ui.dialog;

import com.toedter.calendar.JDateChooser;
import controller.MenuController;
import controller.PriceController;
import entity.BangGia;
import entity.BangGiaChiTiet;
import entity.Mon;
import entity.Size;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * PriceDetailDialog: Consolidated dialog for Price List information and item
 * pricing.
 */
public class PriceDetailDialog extends JDialog {

    private final PriceController priceController = new PriceController();
    private final MenuController menuController = new MenuController();
    private final BangGia bangGia;
    private final boolean isEditMode;

    private JTextField txtMa, txtTen;
    private JDateChooser dcBatDau, dcKetThuc;

    private JComboBox<BangGia> cbCloneSource;
    private JButton btnClone;
    private JTextArea taBatch;
    private JTextField txtPercent, txtFixed;
    private JButton btnBatchApply;

    private JComboBox<Mon> cbDishes;
    private JTable table;
    private DefaultTableModel model;

    private JButton btnSave, btnClose;
    private boolean isDirty = false;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);

    public PriceDetailDialog(Frame parent, BangGia bg, boolean isEditMode) {
        super(parent, (isEditMode ? "Cập nhật" : "Thêm mới") + " Bảng giá: " + bg.getMaBangGia(), true);
        this.bangGia = bg;
        this.isEditMode = isEditMode;

        initUI();
        fillData();
        checkDirty();
    }

    private void initUI() {
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- NORTH: Header Split Pane ---
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setPreferredSize(new Dimension(0, 280));

        JSplitPane splitHeader = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitHeader.setDividerLocation(500);
        splitHeader.setResizeWeight(0.5);

        // North-Left: Thông tin chung
        JPanel pnlGeneral = new JPanel(new GridBagLayout());
        pnlGeneral.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), " THÔNG TIN CHUNG ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Times New Roman", Font.BOLD, 14), PRIMARY_COLOR));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 15, 5, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1: Mã bảng giá
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlGeneral.add(new JLabel("MÃ bảng giá (*)"), gbc);
        gbc.gridx = 1;
        txtMa = new JTextField(20);
        txtMa.setEditable(false);
        pnlGeneral.add(txtMa, gbc);

        // Row 2: Tên bảng giá
        gbc.gridx = 0;
        gbc.gridy = 1;
        pnlGeneral.add(new JLabel("Tên bảng giá (*)"), gbc);
        gbc.gridx = 1;
        txtTen = new JTextField(20);
        txtTen.getDocument().addDocumentListener(createDirtyListener());
        pnlGeneral.add(txtTen, gbc);

        // Row 3: Ngày bắt đầu
        gbc.gridx = 0;
        gbc.gridy = 2;
        pnlGeneral.add(new JLabel("Ngày bắt đầu"), gbc);
        gbc.gridx = 1;
        dcBatDau = new JDateChooser();
        dcBatDau.setDateFormatString("yyyy-MM-dd");
        dcBatDau.addPropertyChangeListener("date", e -> checkDirty());
        pnlGeneral.add(dcBatDau, gbc);

        // Row 4: Ngày kết thúc
        gbc.gridx = 0;
        gbc.gridy = 3;
        pnlGeneral.add(new JLabel("Ngày kết thúc"), gbc);
        gbc.gridx = 1;
        dcKetThuc = new JDateChooser();
        dcKetThuc.setDateFormatString("yyyy-MM-dd");
        dcKetThuc.addPropertyChangeListener("date", e -> checkDirty());
        pnlGeneral.add(dcKetThuc, gbc);

        // North-Right: Công cụ nhanh
        JPanel pnlQuickTools = new JPanel(new GridBagLayout());
        pnlQuickTools.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), " CÔNG CỤ NHANH ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Times New Roman", Font.BOLD, 14), PRIMARY_COLOR));
        GridBagConstraints gbcR = new GridBagConstraints();
        gbcR.insets = new Insets(5, 15, 2, 15);
        gbcR.fill = GridBagConstraints.HORIZONTAL;
        gbcR.anchor = GridBagConstraints.WEST;
        gbcR.weightx = 1.0;

        // Label Sao chép
        gbcR.gridx = 0;
        gbcR.gridy = 0;
        JLabel lblCopy = new JLabel("Sao chép giá từ");
        pnlQuickTools.add(lblCopy, gbcR);

        // ComboBox Sao chép
        gbcR.gridy = 1;
        cbCloneSource = new JComboBox<>();
        cbCloneSource.addItem(null); // Option "Bảng giá mới"
        List<BangGia> otherLists = priceController.getAllBangGia();
        for (BangGia b : otherLists) {
            if (!b.getMaBangGia().equals(bangGia.getMaBangGia()))
                cbCloneSource.addItem(b);
        }
        cbCloneSource.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null)
                    l.setText("--- Bảng giá mới (Không sao chép) ---");
                else
                    l.setText(((BangGia) value).getTenBangGia());
                return l;
            }
        });
        pnlQuickTools.add(cbCloneSource, gbcR);
        cbCloneSource.addActionListener(e -> handleCloneSourceChange());

        // Button Sao chép
        gbcR.gridy = 2;
        btnClone = new JButton(" Sao chép ngay");
        btnClone.setIcon(IconFontSwing.buildIcon(FontAwesome.CLIPBOARD, 14, Color.DARK_GRAY));
        btnClone.addActionListener(e -> handleClone());
        pnlQuickTools.add(btnClone, gbcR);

        // Label Bulk Adjust
        gbcR.gridy = 3;
        gbcR.insets = new Insets(10, 15, 2, 15);
        JLabel lblBatch = new JLabel("Điều chỉnh giá hàng loạt");
        pnlQuickTools.add(lblBatch, gbcR);

        // TextArea Bulk Adjust
        gbcR.gridy = 4;
        gbcR.insets = new Insets(2, 15, 5, 15);
        taBatch = new JTextArea(2, 20);
        taBatch.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        taBatch.setText("Ghi chú...");
        taBatch.setBorder(new LineBorder(Color.LIGHT_GRAY));
        pnlQuickTools.add(new JScrollPane(taBatch), gbcR);

        // Batch Fields row
        gbcR.gridy = 5;
        JPanel pnlBatchFields = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pnlBatchFields.setOpaque(false);
        pnlBatchFields.add(new JLabel("% Tăng:"));
        txtPercent = new JTextField("0", 4);
        pnlBatchFields.add(txtPercent);
        pnlBatchFields.add(new JLabel(" + "));
        txtFixed = new JTextField("0", 6);
        pnlBatchFields.add(txtFixed);
        pnlQuickTools.add(pnlBatchFields, gbcR);

        // Apply Batch button
        gbcR.gridy = 6;
        btnBatchApply = new JButton(" Áp dụng cho toàn bộ ");
        btnBatchApply.setIcon(IconFontSwing.buildIcon(FontAwesome.MAGIC, 14, Color.DARK_GRAY));
        btnBatchApply.addActionListener(e -> handleBatchAdjust());
        pnlQuickTools.add(btnBatchApply, gbcR);

        splitHeader.setLeftComponent(pnlGeneral);
        splitHeader.setRightComponent(pnlQuickTools);
        pnlNorth.add(splitHeader, BorderLayout.CENTER);
        add(pnlNorth, BorderLayout.NORTH);

        // --- CENTER: Chi tiết bảng giá ---
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 10));
        pnlCenter.setBorder(new EmptyBorder(0, 15, 0, 15));

        JPanel pnlAddDish = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlAddDish.add(new JLabel("Thêm món:"));
        cbDishes = new JComboBox<>();
        List<Mon> allMon = menuController.getAllMon();
        for (Mon m : allMon)
            cbDishes.addItem(m);
        cbDishes.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null)
                    l.setText(((Mon) value).getTenMon());
                return l;
            }
        });
        pnlAddDish.add(cbDishes);
        JButton btnAddDish = new JButton("Thêm");
        btnAddDish.addActionListener(e -> handleAddDish());
        pnlAddDish.add(btnAddDish);
        pnlCenter.add(pnlAddDish, BorderLayout.NORTH);
        // Table sẽ được add vào CENTER bên dưới

        String[] cols = { "Tên món ăn", "Kích thước", "Giá bán (đồng)", "maSize" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2;
            }
        };
        model.addTableModelListener(e -> checkDirty());

        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        table.removeColumn(table.getColumnModel().getColumn(3)); // Hidden maSize

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        pnlCenter.add(new JScrollPane(table), BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // --- SOUTH: Footer ---
        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        btnClose = new JButton(" ĐÓNG ");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addActionListener(e -> dispose());

        btnSave = new JButton(" LƯU ");
        btnSave.setPreferredSize(new Dimension(100, 35));
        btnSave.setBackground(SUCCESS_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Times New Roman", Font.BOLD, 13));
        btnSave.addActionListener(e -> handleSave());

        pnlSouth.add(btnClose);
        pnlSouth.add(btnSave);
        add(pnlSouth, BorderLayout.SOUTH);
    }

    private void fillData() {
        txtMa.setText(bangGia.getMaBangGia());
        txtTen.setText(bangGia.getTenBangGia());
        if (bangGia.getNgayBatDau() != null) {
            dcBatDau.setDate(Date.from(bangGia.getNgayBatDau().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (bangGia.getNgayKetThuc() != null) {
            dcKetThuc.setDate(Date.from(bangGia.getNgayKetThuc().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        loadPriceTable();
        isDirty = false;
        btnSave.setEnabled(false);
    }

    private void loadPriceTable() {
        model.setRowCount(0);
        if (!isEditMode) {
            // Bảng giá mới: để trống, người dùng tự thêm từng món
            return;
        }
        // Chế độ sửa: chỉ hiển thị các món đã có giá trong bảng này
        List<BangGiaChiTiet> existingDetails = priceController.getDetailsOf(bangGia.getMaBangGia());
        for (BangGiaChiTiet d : existingDetails) {
            Size s = menuController.getSizeById(d.getMaSize());
            if (s != null) {
                Mon m = menuController.getMonById(s.getMaMon());
                model.addRow(new Object[] {
                        m != null ? m.getTenMon() : "Unknown",
                        s.getTenSize(),
                        d.getGiaBan(),
                        s.getMaSize()
                });
            }
        }
    }

    private void handleAddDish() {
        Mon m = (Mon) cbDishes.getSelectedItem();
        if (m == null)
            return;

        List<Size> sizes = menuController.getSizeOfMon(m.getMaMon());
        for (Size s : sizes) {
            // Check if already in table
            boolean exists = false;
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 3).equals(s.getMaSize())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                model.addRow(new Object[] { m.getTenMon(), s.getTenSize(), null, s.getMaSize() });
                isDirty = true;
                checkDirty();
            }
        }
    }

    private void handleClone() {
        BangGia source = (BangGia) cbCloneSource.getSelectedItem();
        if (source == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bảng giá nguồn để sao chép!");
            return;
        }

        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập 'Tên bảng giá' trước khi sao chép!");
            txtTen.requestFocus();
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this,
                "Sao chép toàn bộ giá từ [" + source.getTenBangGia() + "] và LƯU ngay?\nCác dòng chi tiết hiện tại sẽ bị ghi đè.",
                "Xác nhận sao chép", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                // 1. Lưu thông tin chung bảng giá
                bangGia.setTenBangGia(ten);
                Date start = dcBatDau.getDate();
                Date end = dcKetThuc.getDate();
                bangGia.setNgayBatDau(start != null ? start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : LocalDate.now());
                bangGia.setNgayKetThuc(end != null ? end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null);
                priceController.saveBangGia(bangGia, isEditMode);

                // 2. Xóa chi tiết cũ (nếu có) và sao chép từ nguồn
                priceController.deleteAllDetailsOf(bangGia.getMaBangGia());
                priceController.clonePriceList(source.getMaBangGia(), bangGia.getMaBangGia());

                JOptionPane.showMessageDialog(this,
                        "✅ Đã sao chép và lưu bảng giá từ [" + source.getTenBangGia() + "] thành công!");
                isDirty = false;
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi khi sao chép: " + e.getMessage());
            }
        }
    }

    /**
     * Xem trước chi tiết bảng giá khi người dùng thay đổi combo box nguồn sao chép.
     * Chưa lưu vào cơ sở dữ liệu - chỉ hiển thị để tham khảo.
     */
    private void handleCloneSourceChange() {
        BangGia source = (BangGia) cbCloneSource.getSelectedItem();
        model.setRowCount(0);
        if (source == null) {
            // Chọn "― Bảng giá mới ―": khôi phục về trạng thái gốc
            if (isEditMode) {
                List<BangGiaChiTiet> existingDetails = priceController.getDetailsOf(bangGia.getMaBangGia());
                for (BangGiaChiTiet d : existingDetails) {
                    Size s = menuController.getSizeById(d.getMaSize());
                    if (s != null) {
                        Mon m = menuController.getMonById(s.getMaMon());
                        model.addRow(new Object[] {
                                m != null ? m.getTenMon() : "Unknown",
                                s.getTenSize(),
                                d.getGiaBan(),
                                s.getMaSize()
                        });
                    }
                }
            }
            // New mode: table stays empty
            isDirty = false;
        } else {
            // Xem trước chi tiết của bảng giá được chọn
            List<BangGiaChiTiet> sourceDetails = priceController.getDetailsOf(source.getMaBangGia());
            for (BangGiaChiTiet d : sourceDetails) {
                Size s = menuController.getSizeById(d.getMaSize());
                if (s != null) {
                    Mon m = menuController.getMonById(s.getMaMon());
                    model.addRow(new Object[] {
                            m != null ? m.getTenMon() : "Unknown",
                            s.getTenSize(),
                            d.getGiaBan(),
                            s.getMaSize()
                    });
                }
            }
            isDirty = true;
        }
        checkDirty();
    }

    private void handleBatchAdjust() {
        try {
            double p = Double.parseDouble(txtPercent.getText().trim()) / 100.0;
            double f = Double.parseDouble(txtFixed.getText().trim());

            if (p == 0 && f == 0)
                return;

            int opt = JOptionPane.showConfirmDialog(this, "Điều chỉnh GIÁ BÁN của TẤT CẢ món trong danh sách trên?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    Object val = model.getValueAt(i, 2);
                    if (val == null || val.toString().trim().isEmpty()) continue;
                    
                    double oldPrice = Double.parseDouble(val.toString());
                    double newPrice = oldPrice * (1 + p) + f;
                    // Round to nearest 1000
                    newPrice = Math.round(newPrice / 1000.0) * 1000.0;
                    if (newPrice < 0) newPrice = 0.0; // KHÔNG CHO PHÉP GIÁ ÂM
                    model.setValueAt(newPrice, i, 2);
                }
                isDirty = true;
                checkDirty();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Dữ liệu điều chỉnh không hợp lệ!");
        }
    }

    private void handleSave() {
        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên bảng giá không được để trống!");
            return;
        }

        Date start = dcBatDau.getDate();
        Date end = dcKetThuc.getDate();
        if (start != null && end != null && end.before(start)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc phải lớn hơn ngày bắt đầu!");
            return;
        }

        try {
            // 1. Save General Info
            bangGia.setTenBangGia(ten);
            bangGia.setNgayBatDau(
                    start != null ? start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : LocalDate.now());
            bangGia.setNgayKetThuc(end != null ? end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null);

            // [BUSINESS RULE] Không được phép làm mất "Winner" của hôm nay nếu hệ thống đang có
            BangGia currentWinner = priceController.getWinningPriceList();
            if (currentWinner != null) {
                boolean hasWinnerForToday = false;
                LocalDate today = LocalDate.now();
                
                // 1. Kiểm tra xem chính bảng giá đang lưu này có cover cho hôm nay không
                if (bangGia.isTrangThai() && bangGia.isHoatDong() 
                    && !today.isBefore(bangGia.getNgayBatDau()) 
                    && (bangGia.getNgayKetThuc() == null || !today.isAfter(bangGia.getNgayKetThuc()))) {
                    hasWinnerForToday = true;
                } else {
                    // 2. Nếu bảng này không cover, tìm xem có bảng NÀO KHÁC cover không
                    List<BangGia> all = priceController.getAllBangGia();
                    for (BangGia bg : all) {
                        if (!bg.getMaBangGia().equals(bangGia.getMaBangGia()) 
                            && bg.isHoatDong() && bg.isTrangThai()
                            && !today.isBefore(bg.getNgayBatDau())
                            && (bg.getNgayKetThuc() == null || !today.isAfter(bg.getNgayKetThuc()))) {
                            hasWinnerForToday = true;
                            break;
                        }
                    }
                }
                
                if (!hasWinnerForToday) {
                    JOptionPane.showMessageDialog(this, "<html><b style='color:red'>LỖI NGHIỆP VỤ: LỖ HỔNG BẢNG GIÁ!</b><br><br>"
                            + "Hệ thống hiện đang có bảng giá áp dụng cho hôm nay.<br>"
                            + "Việc thay đổi này (Đổi ngày áp dụng) sẽ khiến hệ thống <b>MẤT BẢNG GIÁ</b>.<br>"
                            + "Phải luôn có ít nhất 1 bảng giá đang hoạt động để thu ngân có thể bán hàng.</html>", "Chặn Thao Tác", JOptionPane.ERROR_MESSAGE);
                    return; // Chặn lưu
                }
            }

            priceController.saveBangGia(bangGia, isEditMode);

            // 2. Save Item Prices
            if (table.isEditing())
                table.getCellEditor().stopCellEditing();

            List<BangGiaChiTiet> currentDBDetails = priceController.getDetailsOf(bangGia.getMaBangGia());

            List<BangGiaChiTiet> toInsert = new java.util.ArrayList<>();
            List<BangGiaChiTiet> toUpdate = new java.util.ArrayList<>();

            for (int i = 0; i < model.getRowCount(); i++) {
                String maSize = (String) model.getValueAt(i, 3);
                Object val = model.getValueAt(i, 2);
                
                if (val == null || val.toString().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập giá cho tất cả các món!");
                    return;
                }
                
                double price = Double.parseDouble(val.toString());
                if (price < 0) {
                    JOptionPane.showMessageDialog(this, "Lỗi: Giá bán không được là số âm ở món " + model.getValueAt(i, 0));
                    return;
                }

                BangGiaChiTiet existing = currentDBDetails.stream()
                        .filter(d -> d.getMaSize().equals(maSize))
                        .findFirst().orElse(null);

                if (existing != null) {
                    if (existing.getGiaBan() != price) {
                        existing.setGiaBan(price);
                        toUpdate.add(existing);
                    }
                } else {
                    BangGiaChiTiet nw = new BangGiaChiTiet(priceController.generateNextMaBGCT(), price, maSize,
                            bangGia.getMaBangGia());
                    toInsert.add(nw);
                }
            }
            
            priceController.saveDetailsTransactionally(toInsert, toUpdate);

            JOptionPane.showMessageDialog(this, "✅ Đã lưu thông tin bảng giá!");
            isDirty = false;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi: " + e.getMessage());
        }
    }

    private void checkDirty() {
        boolean dirty = false;
        if (!txtTen.getText().equals(bangGia.getTenBangGia()))
            dirty = true;

        LocalDate dbStart = bangGia.getNgayBatDau();
        Date uiStart = dcBatDau.getDate();
        if (dbStart == null && uiStart != null)
            dirty = true;
        else if (dbStart != null && uiStart == null)
            dirty = true;
        else if (dbStart != null && uiStart != null) {
            LocalDate uiLocalDate = uiStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (!dbStart.isEqual(uiLocalDate))
                dirty = true;
        }

        LocalDate dbEnd = bangGia.getNgayKetThuc();
        Date uiEnd = dcKetThuc.getDate();
        if (dbEnd == null && uiEnd != null)
            dirty = true;
        else if (dbEnd != null && uiEnd == null)
            dirty = true;
        else if (dbEnd != null && uiEnd != null) {
            LocalDate uiLocalDate = uiEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (!dbEnd.isEqual(uiLocalDate))
                dirty = true;
        }

        if (isDirty)
            dirty = true; // For table changes or additions

        btnSave.setEnabled(dirty);
    }

    private DocumentListener createDirtyListener() {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkDirty();
            }

            public void removeUpdate(DocumentEvent e) {
                checkDirty();
            }

            public void changedUpdate(DocumentEvent e) {
                checkDirty();
            }
        };
    }
}
