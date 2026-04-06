package ui.panel.admin;

import controller.PriceController;
import entity.BangGia;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

/**
 * PriceManagementPanel: Quản lý danh mục các Bảng giá.
 */
public class PriceManagementPanel extends JPanel {

    private final PriceController controller = new PriceController();
    private JTable table;
    private DefaultTableModel tableModel;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public PriceManagementPanel() {
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
        pnlBreadcrumb.add(new JLabel("Admin / Thi\u1EBFt l\u1EADp / "));
        JLabel lblCurrent = new JLabel("B\u1EA3 ng gi\u00E1");
        lblCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCurrent.setForeground(PRIMARY_COLOR);
        pnlBreadcrumb.add(lblCurrent);
        pnlHeader.add(pnlBreadcrumb);
        pnlHeader.add(Box.createVerticalStrut(10));

        // 2. Title & Add Button
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("DANH S\u00C1CH B\u1EA2NG GI\u00C1");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlTitle.add(lblTitle, BorderLayout.WEST);

        JButton btnAdd = new JButton(" T\u1EA1o B\u1EA3ng Gi\u00E1 M\u1EDBi");
        btnAdd.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 14, Color.WHITE));
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(200, 40));
        btnAdd.addActionListener(e -> handleAdd());
        pnlTitle.add(btnAdd, BorderLayout.EAST);
        pnlHeader.add(pnlTitle);

        add(pnlHeader, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] cols = {"M\u00E3 b\u1EA3ng gi\u00E1", "T\u00EAn b\u1EA3ng gi\u00E1", "Ng\u00E0y b\u1EAFt \u0111\u1EA7u", "Ng\u00E0y k\u1EBFt th\u00FAc", "Tr\u1EA1ng th\u00E1i", "Thao t\u00E1c"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(50);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.setShowVerticalLines(false);
        
        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(5).setPreferredWidth(180);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(230,230,230)));
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        controller.autoUpdateStatus(); // Ki\u1EC3m tra t\u1EF1 \u0111\u1ED9ng tr\u01B0\u1EDBc khi load
        List<BangGia> list = controller.getAllBangGia();
        for (BangGia bg : list) {
            tableModel.addRow(new Object[]{
                bg.getMaBangGia(),
                bg.getTenBangGia(),
                bg.getNgayBatDau(),
                bg.getNgayKetThuc() != null ? bg.getNgayKetThuc() : "V\u00F4 th\u1EDDi h\u1EA1n",
                bg.isTrangThai(),
                bg
            });
        }
    }

    private void handleAdd() {
        BangGia bg = new BangGia();
        bg.setMaBangGia(controller.generateNextMaBG());
        bg.setNgayBatDau(java.time.LocalDate.now());
        bg.setTrangThai(true);
        // build dialog s\u1EBD l\u00E0 b\u01B0\u1EDBc ti\u1EBFp theo
        ui.dialog.PriceDialog dlg = new ui.dialog.PriceDialog((Frame) SwingUtilities.getWindowAncestor(this), bg, false);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) loadData();
    }

    // --- RENDERERS & EDITORS ---

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
            lbl.setForeground(active ? new Color(39, 174, 96) : Color.GRAY);
            lbl.setText(active ? "\u25CF \u0110ang \u00E1p d\u1EE5ng" : "\u25CF T\u1EA1m ng\u1EEBng");
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
        private BangGia current;
        public ActionEditor() {
            p = createActionPanel();
            JButton btnEdit = (JButton) p.getComponent(0);
            JButton btnConfig = (JButton) p.getComponent(1);
            btnEdit.addActionListener(e -> { stopCellEditing(); handleEdit(current); });
            btnConfig.addActionListener(e -> { stopCellEditing(); handleConfigPrices(current); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean isS, int r, int c) {
            current = (BangGia) v;
            p.setBackground(t.getSelectionBackground());
            return p;
        }
        @Override public Object getCellEditorValue() { return current; }
    }

    private JPanel createActionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        p.setOpaque(true);
        p.add(createBtn(FontAwesome.COG, new Color(52, 152, 219))); // S\u1EEDa th\u00F4ng tin
        p.add(createBtn(FontAwesome.LIST_ALT, new Color(155, 89, 182))); // Thi\u1EBFt l\u1EADp gi\u00E1
        return p;
    }

    private JButton createBtn(FontAwesome icon, Color color) {
        JButton b = new JButton(IconFontSwing.buildIcon(icon, 18, color));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void handleEdit(BangGia bg) {
        ui.dialog.PriceDialog dlg = new ui.dialog.PriceDialog((Frame) SwingUtilities.getWindowAncestor(this), bg, true);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) loadData();
    }

    private void handleConfigPrices(BangGia bg) {
        // H\u1ED9p tho\u1EA1i ch\u1EC9 nh s\u1EEDa Gi\u00E1 h\u00E0ng lo\u1EA1t
        ui.dialog.PriceDetailDialog dlg = new ui.dialog.PriceDetailDialog((Frame) SwingUtilities.getWindowAncestor(this), bg);
        dlg.setVisible(true);
        loadData();
    }
}
