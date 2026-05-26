package ui.panel.admin;

import controller.ToppingController;
import entity.Topping;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Panel quản lý Topping: CRUD + Tìm kiếm + Lọc trạng thái.
 * Pattern theo StaffManagementPanel: Breadcrumb + Filter Card + Zebra Table.
 */
public class ToppingManagementPanel extends JPanel {

    private final ToppingController controller = new ToppingController();
    private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterStatus;

    private final Color PRIMARY_COLOR = new Color(113, 76, 52);   // Nâu cafe
    private final Color BG_COLOR      = new Color(245, 247, 250);

    public ToppingManagementPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(15, 25, 25, 25));

        initHeader();
        initTable();
        loadData();
    }

    // ══════════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════════
    private void initHeader() {
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setOpaque(false);

        // 1. Breadcrumb
        JPanel pnlBreadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBreadcrumb.setOpaque(false);
        JLabel lblBreadcrumb = new JLabel("Thiết Lập / Thực Đơn / ");
        lblBreadcrumb.setForeground(Color.GRAY);
        lblBreadcrumb.setFont(new Font("Roboto", Font.PLAIN, 13));
        JLabel lblCurrent = new JLabel("Topping");
        lblCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCurrent.setForeground(PRIMARY_COLOR);
        pnlBreadcrumb.add(lblBreadcrumb);
        pnlBreadcrumb.add(lblCurrent);
        pnlHeader.add(pnlBreadcrumb);
        pnlHeader.add(Box.createVerticalStrut(10));

        // 2. Title + Nút Thêm
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);

        JPanel pnlTitleText = new JPanel();
        pnlTitleText.setLayout(new BoxLayout(pnlTitleText, BoxLayout.Y_AXIS));
        pnlTitleText.setOpaque(false);

        JLabel lblTitle = new JLabel("DANH SÁCH TOPPING");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlTitleText.add(lblTitle);

        JLabel lblSub = new JLabel("Quản lý các loại topping trong thực đơn.");
        lblSub.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblSub.setForeground(new Color(150, 150, 150));
        pnlTitleText.add(Box.createVerticalStrut(4));
        pnlTitleText.add(lblSub);

        pnlTitle.add(pnlTitleText, BorderLayout.WEST);

        JButton btnAdd = new JButton(" Thêm Topping");
        btnAdd.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 14, Color.WHITE));
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(160, 40));
        btnAdd.setFocusable(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> handleAdd());
        pnlTitle.add(btnAdd, BorderLayout.EAST);

        pnlHeader.add(pnlTitle);
        pnlHeader.add(Box.createVerticalStrut(15));

        // 3. Filter Card
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pnlFilter.setBackground(Color.WHITE);
        pnlFilter.setBorder(new LineBorder(new Color(230, 230, 230), 1));

        JLabel lblSearchIcon = new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY));
        txtSearch = new JTextField(25);
        txtSearch.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(0, 35));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                performSearch();
            }
        });

        cbFilterStatus = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đang cung cấp", "Tạm ngưng"});
        cbFilterStatus.setFont(new Font("Roboto", Font.PLAIN, 14));
        cbFilterStatus.setPreferredSize(new Dimension(180, 35));
        cbFilterStatus.addActionListener(e -> performSearch());

        pnlFilter.add(lblSearchIcon);
        pnlFilter.add(txtSearch);
        pnlFilter.add(new JLabel("  |  "));
        pnlFilter.add(new JLabel("Trạng thái: "));
        pnlFilter.add(cbFilterStatus);

        pnlHeader.add(pnlFilter);
        add(pnlHeader, BorderLayout.NORTH);
    }

    // ══════════════════════════════════════════════════════════════════
    //  TABLE
    // ══════════════════════════════════════════════════════════════════
    private void initTable() {
        String[] cols = {"Mã Topping", "Tên Topping", "Trạng thái", "Object"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(55);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Zebra renderer
        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        // Status renderer cho cột 2
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusRenderer());

        // Ẩn cột Object (index 3)
        table.removeColumn(table.getColumnModel().getColumn(3));

        // Độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(450);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        // Double-click để sửa
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        Topping t = (Topping) tableModel.getValueAt(table.convertRowIndexToModel(row), 3);
                        handleEdit(t);
                    }
                }
            }
        });

        // Right-click context menu
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miEdit = new JMenuItem("Sửa thông tin");
        miEdit.setIcon(IconFontSwing.buildIcon(FontAwesome.PENCIL, 14, new Color(52, 152, 219)));
        miEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                Topping t = (Topping) tableModel.getValueAt(table.convertRowIndexToModel(row), 3);
                handleEdit(t);
            }
        });

        JMenuItem miToggle = new JMenuItem("Đổi trạng thái");
        miToggle.setIcon(IconFontSwing.buildIcon(FontAwesome.REFRESH, 14, new Color(243, 156, 18)));
        miToggle.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                Topping t = (Topping) tableModel.getValueAt(table.convertRowIndexToModel(row), 3);
                handleToggle(t);
            }
        });

        popup.add(miEdit);
        popup.addSeparator();
        popup.add(miToggle);

        table.setComponentPopupMenu(popup);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════
    //  DATA
    // ══════════════════════════════════════════════════════════════════
    private void loadData() {
        tableModel.setRowCount(0);
        List<Topping> list = controller.getAllToppings();
        for (Topping t : list) {
            tableModel.addRow(new Object[]{
                t.getMaTopping(),
                t.getTenTopping(),
                t.isTrangThai(),
                t
            });
        }
    }

    private void performSearch() {
        String keyword = txtSearch.getText();
        List<Topping> list = controller.searchToppings(keyword);
        int filterIdx = cbFilterStatus.getSelectedIndex();

        tableModel.setRowCount(0);
        for (Topping t : list) {
            if (filterIdx == 1 && !t.isTrangThai()) continue;  // Chỉ Đang cung cấp
            if (filterIdx == 2 && t.isTrangThai()) continue;    // Chỉ Tạm ngưng
            tableModel.addRow(new Object[]{
                t.getMaTopping(),
                t.getTenTopping(),
                t.isTrangThai(),
                t
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════════════════════
    private void handleAdd() {
        Topping t = new Topping();
        t.setMaTopping(controller.generateNextMaTopping());
        t.setTrangThai(true);

        ui.dialog.ToppingDialog dlg = new ui.dialog.ToppingDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), t, false);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            // Kiểm tra trùng tên
            if (controller.isTenToppingDuplicate(t.getTenTopping(), null)) {
                JOptionPane.showMessageDialog(this,
                    "Tên Topping \"" + t.getTenTopping() + "\" đã tồn tại!",
                    "Trùng tên", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (controller.addTopping(t)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Thêm Topping thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm Topping!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEdit(Topping t) {
        ui.dialog.ToppingDialog dlg = new ui.dialog.ToppingDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), t, true);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            // Kiểm tra trùng tên (bỏ qua chính nó)
            if (controller.isTenToppingDuplicate(t.getTenTopping(), t.getMaTopping())) {
                JOptionPane.showMessageDialog(this,
                    "Tên Topping \"" + t.getTenTopping() + "\" đã tồn tại!",
                    "Trùng tên", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (controller.updateTopping(t)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Cập nhật Topping thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleToggle(Topping t) {
        String action = t.isTrangThai() ? "TẠM NGƯNG" : "KÍCH HOẠT LẠI";
        int xn = JOptionPane.showConfirmDialog(this,
            "Bạn muốn " + action + " topping \"" + t.getTenTopping() + "\"?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (xn == JOptionPane.YES_OPTION) {
            if (controller.toggleTrangThai(t)) {
                loadData();
                JOptionPane.showMessageDialog(this, action + " thành công!");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  RENDERERS
    // ══════════════════════════════════════════════════════════════════
    class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            }
            return c;
        }
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(CENTER);
            if (!isSelected) {
                lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            }
            if (Boolean.TRUE.equals(value)) {
                lbl.setForeground(new Color(39, 174, 96));
                lbl.setText("● Đang cung cấp");
            } else {
                lbl.setForeground(new Color(192, 57, 43));
                lbl.setText("● Tạm ngưng");
            }
            return lbl;
        }
    }
}
