package ui.panel.admin;

import controller.NhanVienController;
import entity.NhanVien;
import enums.TrangThaiNhanVien;
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
import java.util.List;

/**
 * StaffManagementPanel phiên bản Hybrid: Breadcrumb + Filter Card + Zebra
 * Table.
 */
public class StaffManagementPanel extends JPanel {

    private final NhanVienController controller = new NhanVienController();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterStatus;
    private JButton btnAdd;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public StaffManagementPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(15, 25, 25, 25));

        initHeader();
        initTable();
        loadData();
    }

    private void initHeader() {
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setOpaque(false);

        // 1. Breadcrumb
        JPanel pnlBreadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBreadcrumb.setOpaque(false);
        JLabel lblBreadcrumb = new JLabel("Admin / Quản trị / ");
        lblBreadcrumb.setForeground(Color.GRAY);
        lblBreadcrumb.setFont(new Font("Roboto", Font.PLAIN, 13));
        JLabel lblCurrent = new JLabel("Nhân viên");
        lblCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCurrent.setForeground(PRIMARY_COLOR);
        pnlBreadcrumb.add(lblBreadcrumb);
        pnlBreadcrumb.add(lblCurrent);
        pnlHeader.add(pnlBreadcrumb);
        pnlHeader.add(Box.createVerticalStrut(10));

        // 2. Title Section
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("DANH SÁCH NHÂN VIÊN");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlTitle.add(lblTitle, BorderLayout.WEST);

        btnAdd = new JButton(" Thêm");
        btnAdd.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 14, Color.WHITE));
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(100, 40));
        btnAdd.setFocusable(false);
        btnAdd.addActionListener(e -> handleAddEmployee());
        pnlTitle.add(btnAdd, BorderLayout.EAST);
        pnlHeader.add(pnlTitle);
        pnlHeader.add(Box.createVerticalStrut(15));

        // 3. Filter Card
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pnlFilter.setBackground(Color.WHITE);
        pnlFilter.setBorder(new LineBorder(new Color(230, 230, 230), 1));

        JLabel lblSearchIcon = new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY));
        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(0, 35));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                performSearch();
            }
        });

        cbFilterStatus = new JComboBox<>(new String[] { "Tất cả trạng thái", "Đang làm việc", "Đã nghỉ việc" });
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

    private void initTable() {
        String[] cols = { "ID", "Họ và tên", "Số điện thoại", "Vai trò",
                "Trạng thái", "Thao tác", "Object" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Chỉ cho phép edit cột Thao tác (để click button)
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(55);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Zebra & Status Renderer
        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        // Cột Thao tác
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        // Hide the 7th column (index 6) which holds the NhanVien object
        table.removeColumn(table.getColumnModel().getColumn(6));

        // Double-click row event
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        // The NhanVien object is stored in the 7th column (index 6 originally, now 5
                        // after removing)
                        // Actually, table.getModel() gives access to original columns
                        NhanVien nv = (NhanVien) tableModel.getValueAt(row, 6);
                        handleEdit(nv);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<NhanVien> list = controller.getAllEmployees();
        boolean isAdmin = utils.SessionManager.isAdmin();
        for (NhanVien nv : list) {
            if (!isAdmin && enums.VaiTro.ADMIN.equals(nv.getVaiTro()))
                continue;

            tableModel.addRow(new Object[] { nv.getMaNV(), nv.getTenNV(), nv.getSoDienThoai(), nv.getVaiTro(),
                    nv.getTrangThai(), "", nv });
        }

    }

    private void performSearch() {
        String keyword = txtSearch.getText();
        List<NhanVien> list = controller.searchEmployees(keyword);
        int filterIdx = cbFilterStatus.getSelectedIndex();
        tableModel.setRowCount(0);
        boolean isAdmin = utils.SessionManager.isAdmin();
        for (NhanVien nv : list) {
            if (!isAdmin && enums.VaiTro.ADMIN.equals(nv.getVaiTro()))
                continue;
            if (filterIdx == 1 && !nv.getTrangThai().equals(TrangThaiNhanVien.DANG_LAM_VIEC))
                continue;
            if (filterIdx == 2 && !nv.getTrangThai().equals(TrangThaiNhanVien.DA_NGHI))
                continue;

            tableModel.addRow(new Object[] { nv.getMaNV(), nv.getTenNV(), nv.getSoDienThoai(), nv.getVaiTro(),
                    nv.getTrangThai(), "", nv });
        }
    }

    private void handleAddEmployee() {
        NhanVien nv = new NhanVien();
        nv.setMaNV(controller.generateNextMaNV()); // Tự động sinh mã mới
        ui.dialog.StaffDialog dlg = new ui.dialog.StaffDialog((Frame) SwingUtilities.getWindowAncestor(this), nv,
                false);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            if (controller.addEmployee(nv)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
            }
        }
    }

    // --- CUSTOM RENDERERS ---

    class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            }
            return c;
        }
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(CENTER);
            if (value == TrangThaiNhanVien.DANG_LAM_VIEC) {
                lbl.setForeground(new Color(39, 174, 96));
                lbl.setText("● Đang làm");
            } else {
                lbl.setForeground(new Color(192, 57, 43));
                lbl.setText("● Đã nghỉ");
            }
            return lbl;
        }
    }

    private void handleEdit(NhanVien nv) {
        ui.dialog.StaffDialog dlg = new ui.dialog.StaffDialog((Frame) SwingUtilities.getWindowAncestor(this), nv, true);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            if (controller.updateEmployee(nv)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            }
        }
    }

    private void handleDeleteEmployee(NhanVien nv) {
        if (nv.getMaNV().equals(utils.SessionManager.getMaNVHienTai())) {
            JOptionPane.showMessageDialog(this, "Không thể tự xóa tài khoản đang đăng nhập!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (enums.VaiTro.ADMIN.equals(nv.getVaiTro())) {
            JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản Quản trị viên (ADMIN)!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Bạn có chắc muốn <b>xóa</b> nhân viên <b>" + nv.getTenNV() + "</b> khỏi danh sách?<br>"
                        + "Thông tin nhân viên vẫn được lưu lại trong cơ sở dữ liệu.</html>",
                "Xác nhận xóa nhân viên", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (controller.permanentlyDeleteEmployee(nv.getMaNV())) {
                    JOptionPane.showMessageDialog(this,
                            "Đã xóa nhân viên " + nv.getTenNV() + " khỏi danh sách!\n"
                                    + "Thông tin vẫn được lưu lại trong cơ sở dữ liệu.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class ActionPanel extends JPanel {
        JButton btnEdit = new JButton();
        JButton btnDelete = new JButton();

        public ActionPanel() {
            setLayout(new GridBagLayout()); // Căn giữa theo cả 2 chiều
            setOpaque(true);

            btnEdit = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); // Bo tròn
                    super.paintComponent(g2);
                    g2.dispose();
                }
            };
            btnEdit.setIcon(IconFontSwing.buildIcon(FontAwesome.PENCIL, 16, Color.WHITE));
            btnEdit.setBackground(new Color(41, 128, 185)); // Màu xanh dương
            btnEdit.setContentAreaFilled(false);
            btnEdit.setBorderPainted(false);
            btnEdit.setOpaque(false);
            btnEdit.setBorder(new EmptyBorder(5, 8, 5, 8));
            btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnDelete = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); // Bo tròn
                    super.paintComponent(g2);
                    g2.dispose();
                }
            };
            btnDelete.setIcon(IconFontSwing.buildIcon(FontAwesome.TRASH, 16, Color.WHITE));
            btnDelete.setBackground(new Color(231, 76, 60)); // Màu đỏ
            btnDelete.setContentAreaFilled(false);
            btnDelete.setBorderPainted(false);
            btnDelete.setOpaque(false);
            btnDelete.setBorder(new EmptyBorder(5, 8, 5, 8));
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 0, 5); // Khoảng cách giữa 2 nút
            add(btnEdit, gbc);
            add(btnDelete, gbc);
        }
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        private ActionPanel actionPanel = new ActionPanel();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            actionPanel.setBackground(isSelected ? table.getSelectionBackground()
                    : (row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255)));
            return actionPanel;
        }
    }

    class ActionEditor extends DefaultCellEditor {
        private ActionPanel actionPanel;
        private NhanVien currentNV;

        public ActionEditor() {
            super(new JCheckBox());
            actionPanel = new ActionPanel();

            actionPanel.btnEdit.addActionListener(e -> {
                fireEditingStopped();
                if (currentNV != null) {
                    handleEdit(currentNV);
                }
            });

            actionPanel.btnDelete.addActionListener(e -> {
                fireEditingStopped();
                if (currentNV != null) {
                    handleDeleteEmployee(currentNV);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            currentNV = (NhanVien) tableModel.getValueAt(row, 6); // Lấy object từ cột ẩn cuối cùng
            actionPanel.setBackground(table.getSelectionBackground());
            return actionPanel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentNV;
        }
    }
}
