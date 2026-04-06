package ui.panel.admin;

import controller.MenuController;
import entity.Mon;
import entity.Size;
import enums.LoaiMon;
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
import java.util.List;

/**
 * MenuManagementPanel: Quản lý thực đơn phong cách Hybrid Card-based.
 */
public class MenuManagementPanel extends JPanel {

    private final MenuController controller = new MenuController();
    
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterLoai;
    private JButton btnAdd;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public MenuManagementPanel() {
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
        JLabel lblBreadcrumb = new JLabel("Admin / Thiết lập / ");
        lblBreadcrumb.setForeground(Color.GRAY);
        lblBreadcrumb.setFont(new Font("Roboto", Font.PLAIN, 13));
        JLabel lblCurrent = new JLabel("Thực đơn");
        lblCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCurrent.setForeground(PRIMARY_COLOR);
        pnlBreadcrumb.add(lblBreadcrumb);
        pnlBreadcrumb.add(lblCurrent);
        pnlHeader.add(pnlBreadcrumb);
        pnlHeader.add(Box.createVerticalStrut(10));

        // 2. Title Section
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("QUẢN LÝ THỰC ĐƠN");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlTitle.add(lblTitle, BorderLayout.WEST);

        btnAdd = new JButton(" Thêm Món Mới");
        btnAdd.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 14, Color.WHITE));
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(180, 40));
        btnAdd.setFocusable(false);
        btnAdd.addActionListener(e -> handleAddMon());
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
            @Override public void keyReleased(KeyEvent e) { performSearch(); }
        });

        cbFilterLoai = new JComboBox<>(new String[]{"Tất cả loại", "Đồ uống", "Đồ ăn"});
        cbFilterLoai.setPreferredSize(new Dimension(150, 35));
        cbFilterLoai.addActionListener(e -> performSearch());

        pnlFilter.add(lblSearchIcon);
        pnlFilter.add(txtSearch);
        pnlFilter.add(new JLabel("  |  "));
        pnlFilter.add(new JLabel("Loại món: "));
        pnlFilter.add(cbFilterLoai);

        pnlHeader.add(pnlFilter);
        add(pnlHeader, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] cols = {"Mã món", "Tên món", "Loại", "Khoảng giá", "Trạng thái", "Thao tác"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 5; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(55);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Mon> list = controller.getAllMon();
        for (Mon m : list) {
            addMonToTable(m);
        }
    }

    private void addMonToTable(Mon m) {
        String priceRange = getPriceRange(m.getMaMon());
        tableModel.addRow(new Object[]{m.getMaMon(), m.getTenMon(), m.getLoaiMon(), priceRange, m.isTrangThai(), m});
    }

    private String getPriceRange(String maMon) {
        List<Size> sizes = controller.getSizeOfMon(maMon);
        if (sizes.isEmpty()) return "Chưa có giá";
        double min = Double.MAX_VALUE;
        double max = 0;
        for (Size s : sizes) {
            double p = controller.getGiaBan(s.getMaSize());
            if (p > 0) {
                min = Math.min(min, p);
                max = Math.max(max, p);
            }
        }
        if (max == 0) return "Chưa có giá";
        if (min == max) return String.format("%,.0f đ", min);
        return String.format("%,.0f - %,.0f đ", min, max);
    }

    private void performSearch() {
        String keyword = txtSearch.getText().toLowerCase();
        int filterIdx = cbFilterLoai.getSelectedIndex();
        tableModel.setRowCount(0);
        List<Mon> list = controller.getAllMon();
        for (Mon m : list) {
            boolean matchName = m.getTenMon().toLowerCase().contains(keyword);
            boolean matchType = (filterIdx == 0) || 
                               (filterIdx == 1 && m.getLoaiMon() == LoaiMon.DO_UONG) || 
                               (filterIdx == 2 && m.getLoaiMon() == LoaiMon.DO_AN);
            if (matchName && matchType) {
                addMonToTable(m);
            }
        }
    }

    private void handleAddMon() {
        Mon m = new Mon();
        m.setMaMon(controller.generateNextMaMon());
        m.setTrangThai(true);
        // Mối quan hệ dialog tương tự StaffDialog sẽ build ở bước sau
        ui.dialog.MenuDialog dlg = new ui.dialog.MenuDialog((Frame) SwingUtilities.getWindowAncestor(this), m, false);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    // --- RENDERERS ---

    class ZebraRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
            if (!isS) comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            return comp;
        }
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
            lbl.setHorizontalAlignment(CENTER);
            boolean active = (boolean) v;
            if (active) {
                lbl.setForeground(new Color(39, 174, 96));
                lbl.setText("● Đang bán");
            } else {
                lbl.setForeground(new Color(149, 165, 166));
                lbl.setText("● Ngưng bán");
            }
            return lbl;
        }
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            JPanel p = createActionPanel();
            p.setBackground(isS ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255)));
            return p;
        }
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel p;
        private Mon current;
        public ActionEditor() {
            p = createActionPanel();
            JButton btnEdit = (JButton) p.getComponent(0);
            JButton btnDel  = (JButton) p.getComponent(1);
            btnEdit.addActionListener(e -> { stopCellEditing(); handleEdit(current); });
            btnDel.addActionListener(e -> { stopCellEditing(); handleToggleStatus(current); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean isS, int r, int c) {
            current = (Mon) v;
            p.setBackground(t.getSelectionBackground());
            return p;
        }
        @Override public Object getCellEditorValue() { return current; }
    }

    private JPanel createActionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        p.setOpaque(true);
        p.add(createBtn(FontAwesome.PENCIL, new Color(52, 152, 219)));
        p.add(createBtn(FontAwesome.TOGGLE_ON, new Color(149, 165, 166)));
        return p;
    }

    private JButton createBtn(FontAwesome icon, Color color) {
        JButton b = new JButton(IconFontSwing.buildIcon(icon, 18, color));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void handleEdit(Mon m) {
        ui.dialog.MenuDialog dlg = new ui.dialog.MenuDialog((Frame) SwingUtilities.getWindowAncestor(this), m, true);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) loadData();
    }

    private void handleToggleStatus(Mon m) {
        m.setTrangThai(!m.isTrangThai());
        if (controller.saveMon(m, true)) loadData();
    }
}