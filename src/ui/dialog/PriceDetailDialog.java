package ui.dialog;

import controller.MenuController;
import controller.PriceController;
import entity.BangGia;
import entity.BangGiaChiTiet;
import entity.Mon;
import entity.Size;
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
 * PriceDetailDialog: Chi ti\u1EBFt gi\u00E1 b\u00E1n c\u1EE7a t\u1EEBng m\u00F3n trong b\u1EA3ng gi\u00E1.
 */
public class PriceDetailDialog extends JDialog {

    private final PriceController priceController = new PriceController();
    private final MenuController menuController = new MenuController();
    private final BangGia bangGia;

    private JTable table;
    private DefaultTableModel model;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);

    public PriceDetailDialog(Frame parent, BangGia bg) {
        super(parent, "Thi\u1EBFt l\u1EADp chi ti\u1EBFt gi\u00E1: " + bg.getTenBangGia(), true);
        this.bangGia = bg;

        initUI();
        loadData();
    }

    private void initUI() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(PRIMARY_COLOR);
        h.setPreferredSize(new Dimension(0, 50));
        h.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel title = new JLabel(" \u0110I\u1EC0U CH\u1EC8NH GI\u00C1 T\u1EEBNG M\u00D3N");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Roboto", Font.BOLD, 15));
        title.setIcon(IconFontSwing.buildIcon(FontAwesome.LIST_OL, 20, Color.WHITE));
        h.add(title, BorderLayout.WEST);
        add(h, BorderLayout.NORTH);

        // Table
        String[] cols = {"T\u00EAn m\u00F3n \u0103 n", "K\u00ED ch th\u01B0\u1EDB c", "Gi\u00E1 b\u00E1n (\u0111)", "maSize"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2; }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(3)); // \u1EA8n c\u1ED9t maSize

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Footer
        JPanel f = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnSave = new JButton(" HO\u00C0N T\u1EA4T");
        btnSave.setPreferredSize(new Dimension(120, 35));
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> handleSave());
        
        JButton btnCancel = new JButton(" H\u1EE6Y");
        btnCancel.addActionListener(e -> dispose());
        
        f.add(btnCancel);
        f.add(btnSave);
        add(f, BorderLayout.SOUTH);
    }

    private void loadData() {
        model.setRowCount(0);
        List<Mon> dishes = menuController.getAllMon();
        for (Mon m : dishes) {
            List<Size> sizes = menuController.getSizeOfMon(m.getMaMon());
            for (Size s : sizes) {
                double price = findPriceInList(s.getMaSize());
                model.addRow(new Object[]{m.getTenMon(), s.getTenSize(), price, s.getMaSize()});
            }
        }
    }

    private double findPriceInList(String maSize) {
        List<BangGiaChiTiet> details = priceController.getDetailsOf(bangGia.getMaBangGia());
        for (BangGiaChiTiet d : details) {
            if (d.getMaSize().equals(maSize)) return d.getGiaBan();
        }
        return 0.0;
    }

    private void handleSave() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        
        try {
            // Lấy danh sách chi tiết hiện có một lần để so sánh
            List<BangGiaChiTiet> currentDetails = priceController.getDetailsOf(bangGia.getMaBangGia());
            
            for (int i = 0; i < model.getRowCount(); i++) {
                String maSize = (String) model.getValueAt(i, 3);
                Object val = model.getValueAt(i, 2);
                if (val == null) continue;
                
                double newPrice = Double.parseDouble(val.toString());
                
                // Tìm xem đã có bản ghi cho Size này trong bảng giá chưa
                BangGiaChiTiet existing = null;
                for (BangGiaChiTiet d : currentDetails) {
                    if (d.getMaSize().equals(maSize)) {
                        existing = d;
                        break;
                    }
                }

                if (existing != null) {
                    if (existing.getGiaBan() != newPrice) { // Chỉ update nếu giá thay đổi
                        existing.setGiaBan(newPrice);
                        priceController.saveDetail(existing, true);
                    }
                } else if (newPrice > 0) {
                    BangGiaChiTiet nw = new BangGiaChiTiet(
                        priceController.generateNextMaBGCT(),
                        newPrice, maSize, bangGia.getMaBangGia()
                    );
                    priceController.saveDetail(nw, false);
                }
            }
            JOptionPane.showMessageDialog(this, " \u2705 \u0110\u00E3 l\u01B0u chi ti\u1EBFt b\u1EA3ng gi\u00E1 th\u00E0nh c\u00F4ng!");
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, " \u26A0\uFE0F L\u1ED7i: Gi\u00E1 b\u00E1n ph\u1EA3i l\u00E0 s\u1ED1 h\u1EE3p l\u1EC7!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, " \u274C L\u1ED7i l\u01B0u d\u1EEF li\u1EC7u: " + e.getMessage());
        }
    }
}
