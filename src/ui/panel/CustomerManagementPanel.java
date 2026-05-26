package ui.panel;

import dao.KhachHangDAO;
import dao.impl.KhachHangDAOImpl;
import entity.KhachHang;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomerManagementPanel extends JPanel {

    private final KhachHangDAO dao = new KhachHangDAOImpl();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CustomerManagementPanel() {
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
        JLabel lblBreadcrumb = new JLabel("Admin / Quản Trị / ");
        lblBreadcrumb.setForeground(Color.GRAY);
        lblBreadcrumb.setFont(new Font("Roboto", Font.PLAIN, 13));
        JLabel lblCurrent = new JLabel("Khách Hàng");
        lblCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCurrent.setForeground(PRIMARY_COLOR);
        pnlBreadcrumb.add(lblBreadcrumb);
        pnlBreadcrumb.add(lblCurrent);
        pnlHeader.add(pnlBreadcrumb);
        pnlHeader.add(Box.createVerticalStrut(10));

        // 2. Title Section
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlTitle.add(lblTitle, BorderLayout.WEST);

        btnAdd = new JButton(" Thêm Khách Hàng");
        btnAdd.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 14, Color.WHITE));
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(180, 40));
        btnAdd.setFocusable(false);
        btnAdd.addActionListener(e -> handleAdd());
        pnlTitle.add(btnAdd, BorderLayout.EAST);
        pnlHeader.add(pnlTitle);
        pnlHeader.add(Box.createVerticalStrut(15));

        // 3. Filter Card
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlFilter.setBackground(Color.WHITE);
        pnlFilter.setBorder(new LineBorder(new Color(230, 230, 230), 1));

        pnlFilter.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 15, Color.GRAY)));
        txtSearch = new JTextField(22);
        txtSearch.setPreferredSize(new Dimension(0, 34));
        txtSearch.setToolTipText("Tìm theo SĐT hoặc Tên (Ctrl+F)");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    txtSearch.setText("");
                performSearch();
            }
        });
        pnlFilter.add(txtSearch);

        JButton btnReset = new JButton(" Xóa bộ lọc");
        btnReset.setIcon(IconFontSwing.buildIcon(FontAwesome.TIMES_CIRCLE, 13, new Color(180, 80, 80)));
        btnReset.setFont(new Font("Roboto", Font.PLAIN, 12));
        btnReset.setForeground(new Color(180, 80, 80));
        btnReset.setContentAreaFilled(false);
        btnReset.setBorderPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            performSearch();
        });
        pnlFilter.add(btnReset);

        pnlHeader.add(pnlFilter);
        add(pnlHeader, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] cols = { "Số điện thoại", "Tên khách hàng", "Điểm tích luỹ", "Ngày tham gia", "Thao tác" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(55);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.setDefaultRenderer(Object.class, new ZebraRenderer());

        table.getColumnModel().getColumn(4).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(4).setPreferredWidth(120);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && !table.isEditing()) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        KhachHang kh = (KhachHang) tableModel.getValueAt(table.convertRowIndexToModel(row), 4);
                        if (kh != null) handleEdit(kh);
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
        List<KhachHang> list = dao.findAll();
        for (KhachHang kh : list) {
            addRow(kh);
        }
    }

    private void addRow(KhachHang kh) {
        tableModel.addRow(new Object[] {
            kh.getSoDienThoai(),
            kh.getTenKhachHang(),
            kh.getDiemTichLuy(),
            kh.getNgayThamGia() != null ? kh.getNgayThamGia().format(formatter) : "",
            kh
        });
    }

    private void performSearch() {
        String keyword = txtSearch.getText().toLowerCase().trim();
        tableModel.setRowCount(0);
        List<KhachHang> list = dao.findAll();
        int found = 0;
        for (KhachHang kh : list) {
            boolean match = keyword.isEmpty() ||
                            kh.getSoDienThoai().toLowerCase().contains(keyword) ||
                            kh.getTenKhachHang().toLowerCase().contains(keyword);
            if (match) {
                addRow(kh);
                found++;
            }
        }
        if (found == 0 && !keyword.isEmpty()) {
            tableModel.addRow(new Object[] { "", "Không tìm thấy kết quả phù hợp", "", "", null });
        }
    }

    private void handleAdd() {
        ui.dialog.CustomerDialog dlg = new ui.dialog.CustomerDialog((Frame) SwingUtilities.getWindowAncestor(this), null, false);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) loadData();
    }

    private void handleEdit(KhachHang kh) {
        ui.dialog.CustomerDialog dlg = new ui.dialog.CustomerDialog((Frame) SwingUtilities.getWindowAncestor(this), kh, true);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) loadData();
    }



    // --- RENDERERS & EDITORS ---

    class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
            if (!isS) comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            return comp;
        }
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            if (v == null) {
                JPanel empty = new JPanel();
                empty.setBackground(isS ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255)));
                return empty;
            }
            JPanel p = new JPanel(new GridBagLayout());
            p.setOpaque(true);
            p.setBackground(isS ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255)));
            
            JButton btnEdit = createBtn(FontAwesome.PENCIL, new Color(41, 128, 185), "Chỉnh sửa");
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 0, 5);
            p.add(btnEdit, gbc);
            return p;
        }
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private KhachHang current;

        public ActionEditor() {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            JButton btnEdit = createBtn(FontAwesome.PENCIL, new Color(41, 128, 185), "Chỉnh sửa");
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 0, 5);
            panel.add(btnEdit, gbc);

            btnEdit.addActionListener(e -> { stopCellEditing(); handleEdit(current); });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean isS, int r, int c) {
            current = (KhachHang) v;
            panel.setBackground(t.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return current; }
    }

    private JButton createBtn(FontAwesome icon, Color color, String tooltip) {
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        b.setIcon(IconFontSwing.buildIcon(icon, 16, Color.WHITE));
        b.setBackground(color);
        b.setToolTipText(tooltip);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(5, 8, 5, 8));
        b.setFocusable(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}
