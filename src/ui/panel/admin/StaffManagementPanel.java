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
        JLabel lblBreadcrumb = new JLabel("Admin / Qu\u1EA3n tr\u1ECB / ");
        lblBreadcrumb.setForeground(Color.GRAY);
        lblBreadcrumb.setFont(new Font("Roboto", Font.PLAIN, 13));
        JLabel lblCurrent = new JLabel("Nh\u00E2n vi\u00EAn");
        lblCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCurrent.setForeground(PRIMARY_COLOR);
        pnlBreadcrumb.add(lblBreadcrumb);
        pnlBreadcrumb.add(lblCurrent);
        pnlHeader.add(pnlBreadcrumb);
        pnlHeader.add(Box.createVerticalStrut(10));

        // 2. Title Section
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("DANH S\u00C1CH NH\u00C2N VI\u00CAN");
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
        pnlFilter.add(new JLabel("Tr\u1EA1ng th\u00E1i: "));
        pnlFilter.add(cbFilterStatus);

        pnlHeader.add(pnlFilter);
        add(pnlHeader, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] cols = { "ID", "Họ và tên", "Số điện thoại", "Vai trò",
                "Trạng thái", "Object" };
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

        // Zebra & Status Renderer
        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        // Hide the 6th column (index 5) which holds the NhanVien object
        table.removeColumn(table.getColumnModel().getColumn(5));

        // Double-click row event
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        // The NhanVien object is stored in the 6th column (index 5)
                        NhanVien nv = (NhanVien) tableModel.getValueAt(row, 5);
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
        for (NhanVien nv : list) {
            tableModel.addRow(new Object[] { nv.getMaNV(), nv.getTenNV(), nv.getSoDienThoai(), nv.getVaiTro(),
                    nv.getTrangThai(), nv });
        }
        // Since I removed "Thao tác" column from 'cols' array, but I still want to keep
        // the NhanVien object
        // in the model for easy access on double click, I'll keep the object in column
        // 5 (last column).
        // I need to adjust the column count or hide the last column.

    }

    private void performSearch() {
        String keyword = txtSearch.getText();
        List<NhanVien> list = controller.searchEmployees(keyword);
        int filterIdx = cbFilterStatus.getSelectedIndex();
        tableModel.setRowCount(0);
        for (NhanVien nv : list) {
            if (filterIdx == 1 && !nv.getTrangThai().equals(TrangThaiNhanVien.DANG_LAM_VIEC))
                continue;
            if (filterIdx == 2 && !nv.getTrangThai().equals(TrangThaiNhanVien.DA_NGHI))
                continue;
            tableModel.addRow(new Object[] { nv.getMaNV(), nv.getTenNV(), nv.getSoDienThoai(), nv.getVaiTro(),
                    nv.getTrangThai(), nv });
        }
    }

    private void handleAddEmployee() {
        NhanVien nv = new NhanVien();
        nv.setMaNV(controller.generateNextMaNV()); // T\u1EF1 \u0111\u1ED9ng sinh m\u00E3 m\u1EDBi
        ui.dialog.StaffDialog dlg = new ui.dialog.StaffDialog((Frame) SwingUtilities.getWindowAncestor(this), nv,
                false);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            if (controller.addEmployee(nv)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Th\u00EA m th\u00E0nh c\u00F4ng!");
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
                lbl.setText("\u25CF \u0110ang l\u00E0m");
            } else {
                lbl.setForeground(new Color(192, 57, 43));
                lbl.setText("\u25CF \u0110\u00E3 ngh\u1EC9");
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
}
