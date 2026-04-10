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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuDialog: Hộp thoại Thêm/Sửa món ăn đa năng.
 * Hỗ trợ chỉnh sửa thông tin món và quản lý danh sách Size/Giá bán ngay lập
 * tức.
 */
public class MenuDialog extends JDialog {

    private final MenuController controller = new MenuController();
    private boolean confirmed = false;
    private final Mon dish;
    private final boolean isEditMode;

    private JTextField txtMaMon, txtTenMon;
    private JComboBox<LoaiMon> cbLoai;
    private JCheckBox chkTrangThai;
    private JTextArea txtMoTa;

    private JTable tableSize;
    private DefaultTableModel modelSize;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public MenuDialog(Frame parent, Mon dish, boolean isEditMode) {
        super(parent, isEditMode ? "Cập nhật Món ăn" : "Thêm Món ăn Mới", true);
        this.dish = dish;
        this.isEditMode = isEditMode;

        initUI();
        fillData();
    }

    private void initUI() {
        setSize(900, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // 1. Header
        add(createHeader(), BorderLayout.NORTH);

        // 2. Body (2 Cards)
        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        body.add(createDishInfoCard());
        body.add(createSizePriceCard());

        add(body, BorderLayout.CENTER);

        // 3. Footer
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(new EmptyBorder(0, 25, 0, 25));

        JLabel title = new JLabel(" CHI TIẾT SẢN PHẨM");
        title.setFont(new Font("Roboto", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setIcon(IconFontSwing.buildIcon(FontAwesome.COFFEE, 32, Color.WHITE));
        header.add(title, BorderLayout.WEST);

        JLabel breadcrumb = new JLabel("Thực đơn > " + (isEditMode ? "Cập nhật" : "Thêm mới"));
        breadcrumb.setForeground(new Color(236, 240, 241));
        breadcrumb.setFont(new Font("Roboto", Font.ITALIC, 13));
        header.add(breadcrumb, BorderLayout.EAST);

        return header;
    }

    private JPanel createDishInfoCard() {
        JPanel card = createCardPanel("THÔNG TIN CƠ BẢN");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = createGBC();

        txtMaMon = addInputRow(form, gbc, "Mã món ăn:", FontAwesome.TAG);
        txtMaMon.setEditable(false);

        txtTenMon = addInputRow(form, gbc, "Tên món ăn*:", FontAwesome.FONT);

        addLabelRow(form, gbc, "Loại món:", FontAwesome.LIST_UL);
        cbLoai = new JComboBox<>(LoaiMon.values());
        cbLoai.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LoaiMon) {
                    LoaiMon type = (LoaiMon) value;
                    setText(type.getTenLoai());
                }
                return this;
            }
        });
        form.add(cbLoai, gbc);
        gbc.gridy++;

        addLabelRow(form, gbc, "Trạng thái bán:", null);
        chkTrangThai = new JCheckBox("Đang kinh doanh");
        chkTrangThai.setOpaque(false);
        form.add(chkTrangThai, gbc);
        gbc.gridy++;

        addLabelRow(form, gbc, "Mô tả sản phẩm:", FontAwesome.ALIGN_LEFT);
        txtMoTa = new JTextArea(7, 20); // Tăng chiều cao thêm ~2 dòng (~30px)
        txtMoTa.setBorder(new LineBorder(new Color(230, 230, 230)));
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        form.add(new JScrollPane(txtMoTa), gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSizePriceCard() {
        JPanel card = createCardPanel("QUẢN LÝ SIZE & GIÁ");

        // Toolbar cho bảng size
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbar.setOpaque(false);
        JButton btnAddSize = new JButton("Thêm Size");
        btnAddSize.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS_CIRCLE, 14, Color.GRAY));
        btnAddSize.addActionListener(
                e -> modelSize.addRow(new Object[] { generateNextMaSizeInSession(), "Thường" }));

        JButton btnRemSize = new JButton("Xóa");
        btnRemSize.setIcon(IconFontSwing.buildIcon(FontAwesome.MINUS_CIRCLE, 14, Color.GRAY));
        btnRemSize.addActionListener(e -> {
            int row = tableSize.getSelectedRow();
            if (row >= 0)
                modelSize.removeRow(row);
        });

        toolbar.add(btnAddSize);
        toolbar.add(btnRemSize);
        card.add(toolbar, BorderLayout.NORTH);

        // Bảng Size
        String[] cols = { "ID", "Kích thước" };
        modelSize = new DefaultTableModel(cols, 0);
        tableSize = new JTable(modelSize);
        tableSize.setRowHeight(35);
        tableSize.getColumnModel().getColumn(0).setPreferredWidth(80);

        card.add(new JScrollPane(tableSize), BorderLayout.CENTER);

        JLabel lblNote = new JLabel("Nhập thông tin cần thay đổi");
        lblNote.setFont(new Font("Roboto", Font.ITALIC, 11));
        lblNote.setForeground(Color.GRAY);
        card.add(lblNote, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        footer.setOpaque(false);

        JButton btnSave = new JButton(" LƯU ");
        btnSave.setPreferredSize(new Dimension(160, 40));
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Roboto", Font.BOLD, 13));
        btnSave.setIcon(IconFontSwing.buildIcon(FontAwesome.FLOPPY_O, 16, Color.WHITE));
        btnSave.addActionListener(e -> handleSave());

        JButton btnCancel = new JButton("ĐÓNG");
        btnCancel.setPreferredSize(new Dimension(90, 40));
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    private void fillData() {
        txtMaMon.setText(dish.getMaMon());
        txtTenMon.setText(dish.getTenMon());
        cbLoai.setSelectedItem(dish.getLoaiMon());
        chkTrangThai.setSelected(dish.isTrangThai());
        txtMoTa.setText(dish.getMoTa());

        if (dish.getMaMon() != null) {
            List<Size> sizes = controller.getSizeOfMon(dish.getMaMon());
            for (Size s : sizes) {
                modelSize.addRow(new Object[] { s.getMaSize(), s.getTenSize() });
            }
        }
    }

    private void handleSave() {
        if (ValidationUtils.isEmpty(txtTenMon.getText())) {
            JOptionPane.showMessageDialog(this, "Tên món không được để trống!");
            return;
        }

        dish.setTenMon(txtTenMon.getText().trim());
        dish.setLoaiMon((LoaiMon) cbLoai.getSelectedItem());
        dish.setTrangThai(chkTrangThai.isSelected());
        dish.setMoTa(txtMoTa.getText().trim());

        if (controller.saveMon(dish, isEditMode)) {
            // Lưu danh sách Size và Giá
            for (int i = 0; i < modelSize.getRowCount(); i++) {
                String maS = (String) modelSize.getValueAt(i, 0);
                String tenS = (String) modelSize.getValueAt(i, 1);

                Size s = new Size(maS, tenS, dish.getMaMon());
                boolean isSizeEdit = dish.getMaMon() != null && controller.getSizeById(maS) != null;
                controller.saveSize(s, isSizeEdit);
            }
            confirmed = true;
            dispose();
        }
    }

    // --- HELPERS ---
    private JPanel createCardPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(20, 20, 20, 20)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Roboto", Font.BOLD, 14));
        lbl.setForeground(PRIMARY_COLOR);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    private JTextField addInputRow(JPanel p, GridBagConstraints gbc, String label, FontAwesome icon) {
        addLabelRow(p, gbc, label, icon);
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(0, 35));
        p.add(txt, gbc);
        gbc.gridy++;
        return txt;
    }

    private void addLabelRow(JPanel p, GridBagConstraints gbc, String text, FontAwesome icon) {
        JLabel lbl = new JLabel(" " + text);
        if (icon != null) {
            lbl.setIcon(IconFontSwing.buildIcon(icon, 14, Color.GRAY));
        }
        lbl.setFont(new Font("Roboto", Font.BOLD, 12));
        gbc.insets = new Insets(10, 0, 5, 0);
        p.add(lbl, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
    }

    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Tạo mã Size tiếp theo có kiểm tra các dòng hiện có trong bảng để tránh trùng lặp
     * trong cùng một phiên làm việc (khi chưa lưu vào DB).
     */
    private String generateNextMaSizeInSession() {
        String nextId = controller.generateNextMaSize();
        int currentMax = Integer.parseInt(nextId.substring(2)); // SZ018 -> 18

        // Kiểm tra trong table model xem có mã nào lớn hơn hoặc bằng không
        for (int i = 0; i < modelSize.getRowCount(); i++) {
            String tableId = (String) modelSize.getValueAt(i, 0);
            if (tableId.startsWith("SZ")) {
                try {
                    int tableNum = Integer.parseInt(tableId.substring(2));
                    if (tableNum >= currentMax) {
                        currentMax = tableNum + 1;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return String.format("SZ%03d", currentMax);
    }
}
