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
        pnlBreadcrumb.add(new JLabel("Admin / Thiết lập / "));
        JLabel lblCurrent = new JLabel("Bảng giá");
        lblCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCurrent.setForeground(PRIMARY_COLOR);
        pnlBreadcrumb.add(lblCurrent);
        pnlHeader.add(pnlBreadcrumb);
        pnlHeader.add(Box.createVerticalStrut(10));

        // 2. Title & Add Button
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("DANH SÁCH BẢNG GIÁ");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlTitle.add(lblTitle, BorderLayout.WEST);

        JButton btnAdd = new JButton(" Tạo Bảng Giá Mới");
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
        String[] cols = {"Mã bảng giá", "Tên bảng giá", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(50);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.setShowVerticalLines(false);
        
        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int row = table.getSelectedRow();
                    // Re-fetch to ensure freshest data
                    BangGia fresh = controller.getAllBangGia().stream()
                        .filter(b -> b.getMaBangGia().equals(table.getValueAt(row, 0)))
                        .findFirst().orElse(null);
                    if (fresh != null) handleConfigPrices(fresh);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(230,230,230)));
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        controller.autoUpdateStatus(); // Kiểm tra tự động trước khi load
        List<BangGia> list = controller.getAllBangGia();
        for (BangGia bg : list) {
            tableModel.addRow(new Object[]{
                bg.getMaBangGia(),
                bg.getTenBangGia(),
                bg.getNgayBatDau(),
                bg.getNgayKetThuc() != null ? bg.getNgayKetThuc() : "Vô thời hạn",
                bg.isTrangThai()
            });
        }
    }

    private void handleAdd() {
        BangGia bg = new BangGia();
        bg.setMaBangGia(controller.generateNextMaBG());
        bg.setNgayBatDau(java.time.LocalDate.now());
        bg.setTrangThai(true);
        ui.dialog.PriceDetailDialog dlg = new ui.dialog.PriceDetailDialog((Frame) SwingUtilities.getWindowAncestor(this), bg, false);
        dlg.setVisible(true);
        loadData();
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
            lbl.setText(active ? "● Đang áp dụng" : "● Tạm ngừng");
            return lbl;
        }
    }

    private void handleConfigPrices(BangGia bg) {
        ui.dialog.PriceDetailDialog dlg = new ui.dialog.PriceDetailDialog((Frame) SwingUtilities.getWindowAncestor(this), bg, true);
        dlg.setVisible(true);
        loadData();
    }
}
