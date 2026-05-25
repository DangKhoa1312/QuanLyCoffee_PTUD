package ui.panel;

import dao.KhuyenMaiDAO;
import dao.impl.KhuyenMaiDAOImpl;
import entity.KhuyenMai;
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

public class PromotionManagementPanel extends JPanel {

    private final KhuyenMaiDAO dao = new KhuyenMaiDAOImpl();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterLoai;
    private JComboBox<String> cbFilterTrangThai;
    private JButton btnAdd;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PromotionManagementPanel() {
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
        JLabel lblCurrent = new JLabel("Khuyến Mãi");
        lblCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCurrent.setForeground(PRIMARY_COLOR);
        pnlBreadcrumb.add(lblBreadcrumb);
        pnlBreadcrumb.add(lblCurrent);
        pnlHeader.add(pnlBreadcrumb);
        pnlHeader.add(Box.createVerticalStrut(10));

        // 2. Title Section
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlTitle.add(lblTitle, BorderLayout.WEST);

        btnAdd = new JButton(" Thêm Khuyến Mãi");
        btnAdd.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 14, Color.WHITE));
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(190, 40));
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
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(0, 34));
        txtSearch.setToolTipText("Tìm theo mã hoặc tên (Ctrl+F)");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) txtSearch.setText("");
                performSearch();
            }
        });
        pnlFilter.add(txtSearch);

        JSeparator sep1 = new JSeparator(SwingConstants.VERTICAL);
        sep1.setPreferredSize(new Dimension(1, 26));
        pnlFilter.add(sep1);

        pnlFilter.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.TAG, 14, new Color(100, 120, 140))));
        cbFilterLoai = new JComboBox<>(new String[]{"Tất cả loại", "PHAN_TRAM", "TIEN_MAT"});
        cbFilterLoai.setPreferredSize(new Dimension(140, 34));
        cbFilterLoai.addActionListener(e -> performSearch());
        pnlFilter.add(cbFilterLoai);

        JSeparator sep2 = new JSeparator(SwingConstants.VERTICAL);
        sep2.setPreferredSize(new Dimension(1, 26));
        pnlFilter.add(sep2);

        pnlFilter.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.TOGGLE_ON, 14, new Color(100, 120, 140))));
        cbFilterTrangThai = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đang hoạt động", "Tạm dừng"});
        cbFilterTrangThai.setPreferredSize(new Dimension(160, 34));
        cbFilterTrangThai.addActionListener(e -> performSearch());
        pnlFilter.add(cbFilterTrangThai);

        JSeparator sep3 = new JSeparator(SwingConstants.VERTICAL);
        sep3.setPreferredSize(new Dimension(1, 26));
        pnlFilter.add(sep3);

        JButton btnReset = new JButton(" Xóa bộ lọc");
        btnReset.setIcon(IconFontSwing.buildIcon(FontAwesome.TIMES_CIRCLE, 13, new Color(180, 80, 80)));
        btnReset.setFont(new Font("Roboto", Font.PLAIN, 12));
        btnReset.setForeground(new Color(180, 80, 80));
        btnReset.setContentAreaFilled(false);
        btnReset.setBorderPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbFilterLoai.setSelectedIndex(0);
            cbFilterTrangThai.setSelectedIndex(0);
            performSearch();
        });
        pnlFilter.add(btnReset);

        pnlHeader.add(pnlFilter);
        add(pnlHeader, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] cols = { "Mã KM", "Tên KM", "Loại", "Giá trị", "ĐH Tối thiểu", "Giảm Tối đa", "Bắt đầu", "Kết thúc", "Trạng thái", "Thao tác" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9;
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
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(9).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(9).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(9).setPreferredWidth(120);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && !table.isEditing()) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        KhuyenMai km = (KhuyenMai) tableModel.getValueAt(table.convertRowIndexToModel(row), 9);
                        if (km != null) handleEdit(km);
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
        List<KhuyenMai> list = dao.findAll();
        for (KhuyenMai km : list) {
            addRow(km);
        }
    }

    private void addRow(KhuyenMai km) {
        tableModel.addRow(new Object[] {
            km.getMaKhuyenMai(),
            km.getTenKhuyenMai(),
            km.getLoaiKhuyenMai(),
            km.getGiaTri(),
            km.getDonHangToiThieu(),
            km.getGiamToiDa(),
            km.getNgayBatDau() != null ? km.getNgayBatDau().format(formatter) : "",
            km.getNgayKetThuc() != null ? km.getNgayKetThuc().format(formatter) : "",
            "DANG_HOAT_DONG".equals(km.getTrangThai()),
            km
        });
    }

    private void performSearch() {
        String keyword = txtSearch.getText().toLowerCase().trim();
        int fLoai = cbFilterLoai.getSelectedIndex();
        int fStatus = cbFilterTrangThai.getSelectedIndex();
        
        tableModel.setRowCount(0);
        List<KhuyenMai> list = dao.findAll();
        int found = 0;
        for (KhuyenMai km : list) {
            boolean matchKey = keyword.isEmpty() || km.getMaKhuyenMai().toLowerCase().contains(keyword) || km.getTenKhuyenMai().toLowerCase().contains(keyword);
            boolean matchLoai = (fLoai == 0) || (fLoai == 1 && "PHAN_TRAM".equals(km.getLoaiKhuyenMai())) || (fLoai == 2 && "TIEN_MAT".equals(km.getLoaiKhuyenMai()));
            boolean matchStatus = (fStatus == 0) || (fStatus == 1 && "DANG_HOAT_DONG".equals(km.getTrangThai())) || (fStatus == 2 && "TAM_DUNG".equals(km.getTrangThai()));
            
            if (matchKey && matchLoai && matchStatus) {
                addRow(km);
                found++;
            }
        }
        if (found == 0 && (!keyword.isEmpty() || fLoai > 0 || fStatus > 0)) {
            tableModel.addRow(new Object[] { "", "Không tìm thấy", "", "", "", "", "", "", false, null });
        }
    }

    private void handleAdd() {
        ui.dialog.PromotionDialog dlg = new ui.dialog.PromotionDialog((Frame) SwingUtilities.getWindowAncestor(this), null, false);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) loadData();
    }

    private void handleEdit(KhuyenMai km) {
        ui.dialog.PromotionDialog dlg = new ui.dialog.PromotionDialog((Frame) SwingUtilities.getWindowAncestor(this), km, true);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) loadData();
    }

    private void handleToggle(KhuyenMai km) {
        String current = km.getTrangThai();
        boolean isToActive = "TAM_DUNG".equals(current);
        String msg = isToActive ? "Bạn muốn Bật lại KM này?" : "Bạn muốn Tạm Dừng KM này?";
        int opt = JOptionPane.showConfirmDialog(this, msg, "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            km.setTrangThai(isToActive ? "DANG_HOAT_DONG" : "TAM_DUNG");
            if (dao.update(km)) loadData();
        }
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

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
            lbl.setHorizontalAlignment(CENTER);
            if (v == null) {
                lbl.setText(""); lbl.setForeground(Color.GRAY);
                return lbl;
            }
            boolean active = (boolean) v;
            if (active) {
                lbl.setForeground(new Color(39, 174, 96));
                lbl.setText("● Hoạt động");
            } else {
                lbl.setForeground(new Color(149, 165, 166));
                lbl.setText("● Tạm dừng");
            }
            return lbl;
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
            KhuyenMai km = (KhuyenMai) v;
            boolean active = "DANG_HOAT_DONG".equals(km.getTrangThai());
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
            p.setOpaque(true);
            p.setBackground(isS ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255)));
            
            JButton btnEdit = createBtn(FontAwesome.PENCIL, new Color(52, 152, 219), "Chỉnh sửa");
            JButton btnToggle = createBtn(active ? FontAwesome.TOGGLE_ON : FontAwesome.TOGGLE_OFF,
                                          active ? new Color(39, 174, 96) : new Color(180, 185, 190),
                                          active ? "Tạm dừng" : "Bật lại");
            p.add(btnEdit); p.add(btnToggle);
            return p;
        }
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private KhuyenMai current;

        public ActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
            panel.setOpaque(true);
            JButton btnEdit = createBtn(FontAwesome.PENCIL, new Color(52, 152, 219), "Chỉnh sửa");
            JButton btnToggle = createBtn(FontAwesome.TOGGLE_ON, Color.GRAY, "Đổi trạng thái");
            panel.add(btnEdit); panel.add(btnToggle);

            btnEdit.addActionListener(e -> { stopCellEditing(); handleEdit(current); });
            btnToggle.addActionListener(e -> { stopCellEditing(); handleToggle(current); });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean isS, int r, int c) {
            current = (KhuyenMai) v;
            panel.setBackground(t.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return current; }
    }

    private JButton createBtn(FontAwesome icon, Color color, String tooltip) {
        JButton b = new JButton(IconFontSwing.buildIcon(icon, 20, color));
        b.setToolTipText(tooltip);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusable(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}
