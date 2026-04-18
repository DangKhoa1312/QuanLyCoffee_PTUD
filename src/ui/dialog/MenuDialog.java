package ui.dialog;

import controller.MenuController;
import entity.Mon;
import entity.Size;
import enums.LoaiMon;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuDialog: Hộp thoại Thêm / Cập nhật món ăn.
 *
 * Layout 2 card:
 * - Trái: Hình ảnh (preview + nút chọn) + Form thông tin cơ bản
 * - Phải: Bảng quản lý Size (tên size, KHÔNG có giá — giá bán ở Bảng Giá)
 *
 * Keyboard shortcuts:
 * Ctrl+Enter = Lưu | Escape = Đóng | Insert = Thêm size
 * Delete/F2 trên bảng size = Xóa / Sửa
 */
public class MenuDialog extends JDialog {

    // ── Controller ─────────────────────────────────────────────────────────
    private final MenuController controller = new MenuController();

    // ── State ───────────────────────────────────────────────────────────────
    private boolean confirmed = false;
    private final Mon dish;
    private final boolean isEditMode;
    private String selectedImagePath; // lưu đường dẫn tương đối images/mon/xxx.jpg

    // ── Form fields ─────────────────────────────────────────────────────────
    private JTextField txtMaMon, txtTenMon;
    private JComboBox<LoaiMon> cbLoai;
    private JCheckBox chkTrangThai;
    private JTextArea txtMoTa;
    private JLabel lblImgPreview;

    // ── Size table ──────────────────────────────────────────────────────────
    private JTable tableSize;
    private DefaultTableModel modelSize;

    // ── Nút lưu (cần ref để enable/disable theo dirty) ──────────────────────
    private JButton btnSave;

    // ── Snapshot gốc để so sánh dirty (chỉ dùng ở edit mode) ───────────────
    private String origTenMon;
    private LoaiMon origLoaiMon;
    private boolean origTrangThai;
    private String origMoTa;
    private String origHinhAnh;
    private List<Object[]> origSizes;

    // ── Palette ─────────────────────────────────────────────────────────────
    private static final Color C_PRIMARY = new Color(41, 128, 185);
    private static final Color C_SUCCESS = new Color(46, 204, 113);
    private static final Color C_DANGER = new Color(231, 76, 60);
    private static final Color C_BG = new Color(245, 247, 250);
    private static final Color C_CARD = Color.WHITE;
    private static final Color C_BORDER = new Color(218, 224, 232);
    private static final Color C_LABEL = new Color(55, 68, 82);
    private static final Color C_FIELD_BG = new Color(252, 252, 253);
    private static final Color C_READONLY = new Color(242, 244, 246);

    // ── Image dir ───────────────────────────────────────────────────────────
    private static final String IMG_RELATIVE_PREFIX = "images/mon/";
    private static final File IMG_DIR = new File("images" + File.separator + "mon");

    // ─────────────────────────────────────────────────────────────────────────
    public MenuDialog(Frame parent, Mon dish, boolean isEditMode) {
        super(parent, isEditMode ? "Cập nhật Món ăn" : "Thêm Món ăn Mới", true);
        this.dish = dish;
        this.isEditMode = isEditMode;
        this.selectedImagePath = dish.getHinhAnh();

        buildUI();
        fillData();
        takeSnapshot(); // chụp trạng thái ban đầu
        setupDirtyListeners(); // lắng nghe thay đổi
        updateSaveBtn(); // set trạng thái ban đầu của nút
        registerShortcuts();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // BUILD UI
    // ═════════════════════════════════════════════════════════════════════════

    private void buildUI() {
        setSize(1000, 680);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(C_PRIMARY);
        hdr.setPreferredSize(new Dimension(0, 62));
        hdr.setBorder(new EmptyBorder(0, 24, 0, 24));

        JLabel lblTitle = new JLabel("  " + (isEditMode ? "CẬP NHẬT MÓN ĂN" : "THÊM MÓN ĂN MỚI"));
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setIcon(IconFontSwing.buildIcon(FontAwesome.COFFEE, 24, Color.WHITE));
        hdr.add(lblTitle, BorderLayout.WEST);

        JLabel lblHint = new JLabel("Ctrl+Enter = Lưu   |   Escape = Đóng   |   Insert = Thêm size");
        lblHint.setForeground(new Color(185, 215, 240));
        lblHint.setFont(new Font("Roboto", Font.ITALIC, 12));
        hdr.add(lblHint, BorderLayout.EAST);

        return hdr;
    }

    // ── Body (2 card) ────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(14, 20, 10, 20));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1.0;
        g.gridy = 0;

        // Card trái — 56 %
        g.gridx = 0;
        g.weightx = 0.56;
        g.insets = new Insets(0, 0, 0, 10);
        body.add(buildInfoCard(), g);

        // Card phải — 44 %
        g.gridx = 1;
        g.weightx = 0.44;
        g.insets = new Insets(0, 0, 0, 0);
        body.add(buildSizeCard(), g);

        return body;
    }

    // ── Card trái: Thông tin món ──────────────────────────────────────────────
    private JPanel buildInfoCard() {
        JPanel card = createCard("THÔNG TIN MÓN ĂN");

        // Bên trong card: [Hình ảnh (WEST, 140px)] | [Form (CENTER)]
        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setOpaque(false);
        content.add(buildImageBlock(), BorderLayout.WEST);
        content.add(buildFormBlock(), BorderLayout.CENTER);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // ── Block ảnh (trái) ─────────────────────────────────────────────────────
    private JPanel buildImageBlock() {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        block.setPreferredSize(new Dimension(140, 0));

        // Preview
        lblImgPreview = new JLabel("Chưa có ảnh", JLabel.CENTER);
        lblImgPreview.setFont(new Font("Roboto", Font.ITALIC, 11));
        lblImgPreview.setForeground(new Color(180, 185, 195));
        lblImgPreview.setBackground(new Color(246, 248, 250));
        lblImgPreview.setOpaque(true);
        lblImgPreview.setBorder(new LineBorder(C_BORDER, 1));
        lblImgPreview.setHorizontalAlignment(JLabel.CENTER);
        lblImgPreview.setVerticalAlignment(JLabel.CENTER);
        lblImgPreview.setPreferredSize(new Dimension(140, 140));
        lblImgPreview.setMaximumSize(new Dimension(140, 140));
        lblImgPreview.setMinimumSize(new Dimension(140, 140));
        lblImgPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Icon placeholder
        lblImgPreview.setIcon(IconFontSwing.buildIcon(FontAwesome.PICTURE_O, 42, new Color(200, 208, 218)));
        p(block, lblImgPreview);

        block.add(vgap(8));

        // Nút Chọn Ảnh
        JButton btnChoose = new JButton(" Chọn Ảnh");
        btnChoose.setIcon(IconFontSwing.buildIcon(FontAwesome.FOLDER_OPEN_O, 14, C_PRIMARY));
        btnChoose.setFont(new Font("Roboto", Font.PLAIN, 12));
        btnChoose.setMaximumSize(new Dimension(140, 32));
        btnChoose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnChoose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChoose.setFocusable(false);
        btnChoose.addActionListener(e -> chooseImage());
        p(block, btnChoose);

        return block;
    }

    // ── Block form (phải của ảnh) ─────────────────────────────────────────────
    private JPanel buildFormBlock() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Mã món
        form.add(lbl("Mã Món:"));
        txtMaMon = field();
        txtMaMon.setEditable(false);
        txtMaMon.setBackground(C_READONLY);
        txtMaMon.setForeground(new Color(130, 140, 150));
        txtMaMon.setFont(new Font("Roboto", Font.BOLD, 13));
        p(form, txtMaMon);
        form.add(vgap(8));

        // Tên món
        form.add(lbl("Tên Món Ăn *:"));
        txtTenMon = field();
        txtTenMon.addActionListener(e -> cbLoai.requestFocus()); // Enter → Loại món
        p(form, txtTenMon);
        form.add(vgap(8));

        // Loại món
        form.add(lbl("Loại Món:"));
        cbLoai = new JComboBox<>(LoaiMon.values());
        cbLoai.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LoaiMon) {
                    LoaiMon loai = (LoaiMon) value;
                    setText(loai.getTenLoai());
                }
                return this;
            }
        });
        cbLoai.setFont(new Font("Roboto", Font.PLAIN, 13));
        cbLoai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbLoai.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(cbLoai);
        form.add(vgap(8));

        // Trạng thái
        form.add(lbl("Trạng Thái:"));
        chkTrangThai = new JCheckBox("Đang kinh doanh (hiển thị trên menu)");
        chkTrangThai.setOpaque(false);
        chkTrangThai.setFont(new Font("Roboto", Font.PLAIN, 13));
        chkTrangThai.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(chkTrangThai);
        form.add(vgap(8));

        // Mô tả
        form.add(lbl("Mô Tả Sản Phẩm:"));
        txtMoTa = new JTextArea(5, 10);
        txtMoTa.setFont(new Font("Roboto", Font.PLAIN, 13));
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        JScrollPane scrollMoTa = new JScrollPane(txtMoTa);
        scrollMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollMoTa.setBorder(new LineBorder(C_BORDER));
        scrollMoTa.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        form.add(scrollMoTa);

        return form;
    }

    // ── Card phải: Quản lý Size ───────────────────────────────────────────────
    private JPanel buildSizeCard() {
        JPanel card = createCard("DANH SÁCH SIZE");

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setOpaque(false);

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JCheckBox chkShowHidden = new JCheckBox("Hiện size đã ẩn");
        chkShowHidden.setFont(new Font("Roboto", Font.PLAIN, 12));
        chkShowHidden.setOpaque(false);
        chkShowHidden.setForeground(new Color(100, 110, 120));
        chkShowHidden.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toolbar.add(chkShowHidden, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnRow.setOpaque(false);

        JButton btnAdd = smallBtn("Thêm Size", FontAwesome.PLUS_CIRCLE, C_SUCCESS);
        JButton btnDel = smallBtn("Xóa Size", FontAwesome.MINUS_CIRCLE, C_DANGER);
        btnAdd.addActionListener(e -> addNewSize());
        btnDel.addActionListener(e -> deleteSelectedSize());
        btnRow.add(btnAdd);
        btnRow.add(btnDel);
        toolbar.add(btnRow, BorderLayout.EAST);

        content.add(toolbar, BorderLayout.NORTH);

        // Bảng Size — 3 cột: Mã Size (readonly), Tên Size (editable), Hoạt động (editable)
        modelSize = new DefaultTableModel(new Object[] { "Mã Size", "Tên Size", "Hoạt động" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 1 || c == 2;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Boolean.class;
                return String.class;
            }
        };
        tableSize = new JTable(modelSize);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelSize);
        tableSize.setRowSorter(sorter);

        // Logic màng lọc RowFilter
        RowFilter<DefaultTableModel, Integer> filter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                if (chkShowHidden.isSelected()) return true;
                Boolean active = (Boolean) entry.getModel().getValueAt(entry.getIdentifier(), 2);
                return active != null && active;
            }
        };
        sorter.setRowFilter(filter);
        chkShowHidden.addActionListener(e -> sorter.setRowFilter(filter));

        tableSize.setRowHeight(36);
        tableSize.setFont(new Font("Roboto", Font.PLAIN, 13));
        tableSize.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));
        tableSize.getTableHeader().setBackground(new Color(236, 240, 241));
        tableSize.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableSize.setShowVerticalLines(false);
        tableSize.getColumnModel().getColumn(0).setPreferredWidth(70);
        tableSize.getColumnModel().getColumn(0).setMaxWidth(90);
        tableSize.getColumnModel().getColumn(2).setPreferredWidth(85);
        tableSize.getColumnModel().getColumn(2).setMaxWidth(100);

        tableSize.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if ((key == KeyEvent.VK_DELETE || key == KeyEvent.VK_BACK_SPACE) && !tableSize.isEditing()) {
                    deleteSelectedSize();
                    e.consume();
                } else if (key == KeyEvent.VK_INSERT) {
                    addNewSize();
                    e.consume();
                } else if ((key == KeyEvent.VK_F2 || key == KeyEvent.VK_ENTER) && !tableSize.isEditing()) {
                    int row = tableSize.getSelectedRow();
                    if (row >= 0)
                        startEditSize(row);
                    e.consume();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableSize);
        scroll.setBorder(new LineBorder(C_BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        content.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("Insert: Thêm | Enter: Sửa | Để xóa Size xin hãy bỏ dấu tích 'Hoạt động'");
        hint.setFont(new Font("Roboto", Font.ITALIC, 11));
        hint.setForeground(new Color(160, 165, 175));
        content.add(hint, BorderLayout.SOUTH);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(new Color(250, 251, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));

        JButton btnCancel = new JButton("HỦY BỎ");
        btnCancel.setPreferredSize(new Dimension(110, 40));
        btnCancel.setFont(new Font("Roboto", Font.PLAIN, 13));
        btnCancel.setFocusable(false);
        btnCancel.addActionListener(e -> dispose());

        btnSave = new JButton(isEditMode ? "  CẬP NHẬT" : "  LƯU MÓN");
        btnSave.setIcon(IconFontSwing.buildIcon(FontAwesome.FLOPPY_O, 16, Color.WHITE));
        btnSave.setPreferredSize(new Dimension(155, 40));
        btnSave.setBackground(isEditMode ? C_PRIMARY : C_SUCCESS);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Roboto", Font.BOLD, 13));
        btnSave.setFocusable(false);
        btnSave.addActionListener(e -> handleSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        getRootPane().setDefaultButton(btnSave);
        return footer;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DATA: Fill & Collect
    // ═════════════════════════════════════════════════════════════════════════

    private void fillData() {
        txtMaMon.setText(dish.getMaMon() != null ? dish.getMaMon() : "");
        txtTenMon.setText(dish.getTenMon() != null ? dish.getTenMon() : "");
        if (dish.getLoaiMon() != null)
            cbLoai.setSelectedItem(dish.getLoaiMon());
        chkTrangThai.setSelected(dish.isTrangThai());
        txtMoTa.setText(dish.getMoTa() != null ? dish.getMoTa() : "");

        // Load ảnh preview nếu có (path có thể là absolute hoặc relative)
        if (selectedImagePath != null && !selectedImagePath.isBlank()) {
            File imgFile = new File(selectedImagePath);
            if (!imgFile.exists()) {
                // Thử relative từ user.dir
                imgFile = new File(System.getProperty("user.dir"), selectedImagePath);
            }
            if (imgFile.exists()) {
                loadImagePreview(imgFile);
            }
        }

        // Load danh sách size (chỉ edit mode)
        if (isEditMode && dish.getMaMon() != null) {
            for (Size s : controller.getAllSizesOfMon(dish.getMaMon())) {
                modelSize.addRow(new Object[] { s.getMaSize(), s.getTenSize(), s.isTrangThai() });
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // IMAGE HANDLING
    // ═════════════════════════════════════════════════════════════════════════

    private void chooseImage() {
        if (!IMG_DIR.exists())
            IMG_DIR.mkdirs();

        JFileChooser chooser = new JFileChooser(IMG_DIR);
        chooser.setDialogTitle("Chọn hình ảnh cho món ăn — " + IMG_DIR.getAbsolutePath());
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Hình ảnh (*.jpg, *.jpeg, *.png, *.gif, *.webp)",
                "jpg", "jpeg", "png", "gif", "webp"));
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            // Lưu path tuyệt đối để app load được ảnh bất kể thư mục chạy
            selectedImagePath = selected.getAbsolutePath();
            loadImagePreview(selected);
            updateSaveBtn(); // ảnh thay đổi → check dirty
        }
    }

    private void loadImagePreview(File imgFile) {
        if (imgFile == null || !imgFile.exists()) {
            lblImgPreview
                    .setIcon(IconFontSwing.buildIcon(FontAwesome.EXCLAMATION_CIRCLE, 36, new Color(220, 100, 100)));
            lblImgPreview.setText("<html><center>Không<br>tìm thấy<br>ảnh</center></html>");
            return;
        }
        try {
            ImageIcon raw = new ImageIcon(imgFile.getAbsolutePath());
            Image scaled = raw.getImage().getScaledInstance(138, 138, Image.SCALE_SMOOTH);
            lblImgPreview.setIcon(new ImageIcon(scaled));
            lblImgPreview.setText(null);
        } catch (Exception ex) {
            lblImgPreview.setIcon(null);
            lblImgPreview.setText("Lỗi tải ảnh");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SIZE ACTIONS
    // ═════════════════════════════════════════════════════════════════════════

    private void addNewSize() {
        // Kiểm tra toàn bộ bảng xem có dòng nào chưa nhập tên không
        for (int i = 0; i < modelSize.getRowCount(); i++) {
            String ts = modelSize.getValueAt(i, 1).toString().trim();
            if (ts.isEmpty()) {
                warn("Dòng thứ " + (i + 1) + " chưa nhập tên size. Vui lòng hoàn tất trước khi thêm mới!", null);
                tableSize.setRowSelectionInterval(i, i);
                final int targetRow = i;
                SwingUtilities.invokeLater(() -> startEditSize(targetRow));
                return;
            }
        }

        String newId = controller.generateNextMaSize(); // Lấy mã tiếp theo từ DB (ví dụ SZ033)

        // Đề phòng trường hợp thêm nhiều size liên tiếp mà chưa lưu DB
        // Ta tìm mã lớn nhất hiện có trong bảng modelSize
        int maxInTable = 0;
        for (int i = 0; i < modelSize.getRowCount(); i++) {
            String mid = (String) modelSize.getValueAt(i, 0);
            if (mid != null && mid.startsWith("SZ")) {
                try {
                    int val = Integer.parseInt(mid.substring(2));
                    if (val > maxInTable)
                        maxInTable = val;
                } catch (Exception ignored) {
                }
            }
        }

        // So sánh với mã từ DB, nếu max trong bảng >= mã DB thì tăng tiếp
        try {
            int dbNum = Integer.parseInt(newId.substring(2));
            if (maxInTable >= dbNum) {
                newId = String.format("SZ%03d", maxInTable + 1);
            }
        } catch (Exception ignored) {
        }

        modelSize.addRow(new Object[] { newId, "", true });
        int row = modelSize.getRowCount() - 1;
        tableSize.setRowSelectionInterval(row, row);
        tableSize.scrollRectToVisible(tableSize.getCellRect(row, 1, true));
        SwingUtilities.invokeLater(() -> startEditSize(row));
    }

    private void startEditSize(int row) {
        tableSize.editCellAt(row, 1);
        Component ed = tableSize.getEditorComponent();
        if (ed != null) {
            ed.requestFocus();
            if (ed instanceof JTextField)
                ((JTextField) ed).selectAll();
        }
    }

    private void deleteSelectedSize() {
        if (tableSize.isEditing())
            tableSize.getCellEditor().stopCellEditing();
        int rowSelected = tableSize.getSelectedRow();
        if (rowSelected < 0)
            return;

        int rowModel = tableSize.convertRowIndexToModel(rowSelected);
        String maSize = (String) modelSize.getValueAt(rowModel, 0);

        boolean isFromDb = false;
        if (origSizes != null) {
            for (Object[] orig : origSizes) {
                if (orig[0].equals(maSize)) {
                    isFromDb = true; break;
                }
            }
        }

        if (isFromDb) {
            // Dữ liệu đã có ở Database -> Gỡ dấu tích Hoạt động
            modelSize.setValueAt(false, rowModel, 2);
        } else {
            // Dữ liệu vừa thêm mới chưa lưu -> Xóa hẳn dòng
            modelSize.removeRow(rowModel);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SAVE
    // ═════════════════════════════════════════════════════════════════════════

    private void handleSave() {
        // Guard: nút đang disabled thì không xử lý (kể cả khi dùng Ctrl+Enter)
        if (btnSave != null && !btnSave.isEnabled())
            return;

        if (tableSize.isEditing())
            tableSize.getCellEditor().stopCellEditing();

        // ── Validate tên món ──
        String tenMon = txtTenMon.getText().trim();
        if (ValidationUtils.isEmpty(tenMon)) {
            warn("Tên món không được để trống!", txtTenMon);
            return;
        }
        if (tenMon.length() < 2) {
            warn("Tên món phải có ít nhất 2 ký tự!", txtTenMon);
            return;
        }
        if (tenMon.length() > 100) {
            warn("Tên món không được vượt quá 100 ký tự!", txtTenMon);
            return;
        }
        // Kiểm tra trùng tên với DB (bỏ qua chính món đang sửa ở edit mode)
        String currentMaMon = isEditMode ? dish.getMaMon() : null;
        if (controller.isTenMonDuplicate(tenMon, currentMaMon)) {
            warn("<html>Tên món <b>\"" + tenMon + "\"</b> đã tồn tại trong hệ thống!<br>"
                + "Vui lòng đặt tên khác.</html>", txtTenMon);
            return;
        }

        // ── Validate size ──
        if (modelSize.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "<html>Vui lòng thêm ít nhất <b>1 size</b> cho món này.<br>"
                            + "<font color='gray'>(Ví dụ: S, M, L, Thường, Lớn...)</font></html>",
                    "Thiếu thông tin Size", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> names = new ArrayList<>();
        for (int i = 0; i < modelSize.getRowCount(); i++) {
            String ts = modelSize.getValueAt(i, 1).toString().trim();
            if (ValidationUtils.isEmpty(ts)) {
                warn("Tên size dòng " + (i + 1) + " không được để trống!", null);
                tableSize.setRowSelectionInterval(i, i);
                return;
            }
            if (ts.length() > 20) {
                warn("Tên size dòng " + (i + 1) + " tối đa 20 ký tự!", null);
                tableSize.setRowSelectionInterval(i, i);
                return;
            }
            if (names.contains(ts.toLowerCase())) {
                warn("Tên size \"" + ts + "\" bị trùng lặp!", null);
                tableSize.setRowSelectionInterval(i, i);
                return;
            }
            names.add(ts.toLowerCase());
        }

        // ── Gán dữ liệu vào entity ──
        dish.setTenMon(tenMon);
        dish.setLoaiMon((LoaiMon) cbLoai.getSelectedItem());
        dish.setTrangThai(chkTrangThai.isSelected());
        dish.setMoTa(txtMoTa.getText().trim());
        dish.setHinhAnh(selectedImagePath);

        // ── Build danh sách Size ──
        List<Size> sizes = new ArrayList<>();
        for (int i = 0; i < modelSize.getRowCount(); i++) {
            Boolean active = (Boolean) modelSize.getValueAt(i, 2);
            sizes.add(new Size(
                    (String) modelSize.getValueAt(i, 0),
                    modelSize.getValueAt(i, 1).toString().trim(),
                    dish.getMaMon(),
                    active != null ? active : true));
        }

        if (controller.saveMonAndSizes(dish, sizes, isEditMode)) {
            confirmed = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Lưu thất bại! Kiểm tra kết nối cơ sở dữ liệu.",
                    "Lỗi lưu dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DIRTY TRACKING — Chỉ enable nút Lưu khi dữ liệu thực sự thay đổi
    // ═════════════════════════════════════════════════════════════════════════

    /** Chụp snapshot trạng thái ban đầu sau khi fill data */
    private void takeSnapshot() {
        origTenMon = txtTenMon.getText().trim();
        origLoaiMon = (LoaiMon) cbLoai.getSelectedItem();
        origTrangThai = chkTrangThai.isSelected();
        origMoTa = txtMoTa.getText().trim();
        origHinhAnh = selectedImagePath != null ? selectedImagePath : "";

        origSizes = new ArrayList<>();
        for (int i = 0; i < modelSize.getRowCount(); i++) {
            Boolean curAct = (Boolean) modelSize.getValueAt(i, 2);
            origSizes.add(new Object[] {
                    modelSize.getValueAt(i, 0),
                    modelSize.getValueAt(i, 1).toString().trim(),
                    curAct != null ? curAct : true
            });
        }
    }

    /** So sánh trạng thái hiện tại với snapshot gốc */
    private boolean isDirty() {
        if (!txtTenMon.getText().trim().equals(origTenMon))
            return true;
        if (cbLoai.getSelectedItem() != origLoaiMon)
            return true;
        if (chkTrangThai.isSelected() != origTrangThai)
            return true;
        if (!txtMoTa.getText().trim().equals(origMoTa))
            return true;

        String curImg = selectedImagePath != null ? selectedImagePath : "";
        if (!curImg.equals(origHinhAnh))
            return true;

        if (modelSize.getRowCount() != origSizes.size())
            return true;
        for (int i = 0; i < modelSize.getRowCount(); i++) {
            Object[] orig = origSizes.get(i);
            String curMa = (String) modelSize.getValueAt(i, 0);
            String curTen = modelSize.getValueAt(i, 1).toString().trim();
            Boolean curAct = (Boolean) modelSize.getValueAt(i, 2);
            if (curAct == null) curAct = true;
            
            if (!curMa.equals(orig[0]) || !curTen.equals(orig[1]) || !curAct.equals(orig[2]))
                return true;
        }
        return false;
    }

    /** Gắn listeners trên tất cả các component để phát hiện thay đổi */
    private void setupDirtyListeners() {
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateSaveBtn();
            }

            public void removeUpdate(DocumentEvent e) {
                updateSaveBtn();
            }

            public void changedUpdate(DocumentEvent e) {
                updateSaveBtn();
            }
        };
        txtTenMon.getDocument().addDocumentListener(dl);
        txtMoTa.getDocument().addDocumentListener(dl);

        cbLoai.addItemListener(e -> updateSaveBtn());
        chkTrangThai.addItemListener(e -> updateSaveBtn());

        // Khi bảng size thay đổi (thêm / xóa / sửa)
        modelSize.addTableModelListener(e -> updateSaveBtn());
    }

    /**
     * Cập nhật trạng thái nút Lưu:
     * - Edit mode: chỉ enable khi có thay đổi thực sự (dirty)
     * - Add mode: enable khi tên món không rỗng
     */
    private void updateSaveBtn() {
        if (btnSave == null)
            return;
        if (isEditMode) {
            boolean dirty = isDirty();
            btnSave.setEnabled(dirty);
            btnSave.setBackground(dirty ? C_PRIMARY : new Color(180, 190, 200));
            btnSave.setToolTipText(dirty ? null : "Chưa có thay đổi nào cần lưu");
        } else {
            // Thêm mới: enable khi đã nhập tên món
            boolean hasName = !txtTenMon.getText().trim().isEmpty();
            btnSave.setEnabled(hasName);
            btnSave.setBackground(hasName ? C_SUCCESS : new Color(180, 200, 185));
            btnSave.setToolTipText(hasName ? null : "Vui lòng nhập tên món trước");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // KEYBOARD SHORTCUTS
    // ═════════════════════════════════════════════════════════════════════════

    private void registerShortcuts() {
        // Ctrl+Enter → Lưu
        getRootPane().registerKeyboardAction(e -> handleSave(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Escape → Đóng
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // COMPONENT HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    /** Tạo một Card (JPanel với border + title ở NORTH) */
    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(C_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER, 1),
                new EmptyBorder(15, 18, 16, 18)));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Roboto", Font.BOLD, 13));
        lbl.setForeground(C_PRIMARY);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(232, 236, 242)));
        lbl.setPreferredSize(new Dimension(0, 28));
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    /** JLabel nhãn form với font in đậm */
    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Roboto", Font.BOLD, 12));
        l.setForeground(C_LABEL);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 3, 0));
        return l;
    }

    /** JTextField chuẩn cho form */
    private JTextField field() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Roboto", Font.PLAIN, 13));
        tf.setBackground(C_FIELD_BG);
        tf.setPreferredSize(new Dimension(0, 36));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER), new EmptyBorder(2, 8, 2, 8)));
        return tf;
    }

    /** Khoảng cách dọc trong BoxLayout */
    private Component vgap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    /** Thêm component vào JPanel BoxLayout (đặt alignment) */
    private void p(JPanel container, JComponent comp) {
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(comp);
    }

    /** Nút nhỏ dùng trong toolbar */
    private JButton smallBtn(String text, FontAwesome icon, Color iconColor) {
        JButton btn = new JButton(" " + text);
        btn.setIcon(IconFontSwing.buildIcon(icon, 13, iconColor));
        btn.setFont(new Font("Roboto", Font.PLAIN, 12));
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Hiển thị cảnh báo + focus về field lỗi */
    private void warn(String msg, JComponent focus) {
        JOptionPane.showMessageDialog(this, msg, "Kiểm tra dữ liệu", JOptionPane.WARNING_MESSAGE);
        if (focus != null) {
            focus.requestFocus();
            if (focus instanceof JTextField)
                ((JTextField) focus).selectAll();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═════════════════════════════════════════════════════════════════════════

    public boolean isConfirmed() {
        return confirmed;
    }
}
