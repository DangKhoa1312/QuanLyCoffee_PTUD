package ui.dialog;

import controller.TableController;
import dto.CartItem;
import entity.Ban;
import entity.DonHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Tách Món (Chuyển 1 phần giỏ hàng sang bàn khác).
 */
public class TransferItemsDialog extends JDialog {

    private final Ban banNguon;
    private final DonHang donHangHienTai;
    private final List<CartItem> cartData;
    private final TableController tableController;
    private boolean success = false;

    private JComboBox<BanItem> cbDanhSachBan;
    private JTable itemTable;
    private DefaultTableModel tableModel;

    public TransferItemsDialog(JFrame parent, Ban banNguon, DonHang donHang, List<CartItem> cartData) {
        super(parent, "Tách Món Từ Bàn " + banNguon.getSoBan(), true);
        this.banNguon = banNguon;
        this.donHangHienTai = donHang;
        this.cartData = cartData;
        this.tableController = new TableController();

        setSize(600, 480);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(15, 20, 15, 20));

        // ── Top: Chọn bàn đích ──
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlTop.setOpaque(false);
        pnlTop.add(new JLabel("Chọn Bàn Đích (Để chuyển món sang): "));
        
        cbDanhSachBan = new JComboBox<>();
        cbDanhSachBan.setPreferredSize(new Dimension(250, 32));
        cbDanhSachBan.setFont(new Font("Roboto", Font.PLAIN, 14));
        pnlTop.add(cbDanhSachBan);
        loadComboData();
        
        main.add(pnlTop, BorderLayout.NORTH);

        // ── Center: Danh sách món ──
        String[] cols = {"Món", "SL Hiện Tại", "SL Tách Đổi"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Chỉ cho phép nhập cột "SL Tách Đổi"
            }
        };
        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(35);
        itemTable.setFont(new Font("Roboto", Font.PLAIN, 14));

        loadTableData();

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách món trong Đơn (Nhập số lượng muốn tách vào cột 3)"));
        main.add(scrollPane, BorderLayout.CENTER);

        // ── Bot: Buttons ──
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBot.setOpaque(false);

        JButton btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dispose());

        JButton btnXacNhan = new JButton("Xác Nhận Tách Món");
        btnXacNhan.setBackground(new Color(231, 76, 60));
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setFont(new Font("Roboto", Font.BOLD, 13));
        btnXacNhan.addActionListener(e -> commitAction());

        pnlBot.add(btnHuy);
        pnlBot.add(btnXacNhan);
        main.add(pnlBot, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void loadComboData() {
        cbDanhSachBan.removeAllItems();
        
        // Lấy bàn trống (tạo đơn mới)
        List<Ban> banTrongs = tableController.getBanTrong();
        for (Ban b : banTrongs) {
            if (!"MANG_VE".equals(b.getMaBan())) {
                cbDanhSachBan.addItem(new BanItem(b, " (Trống)"));
            }
        }
        // Lấy bàn có khách (gộp vào đơn hiện tại của bàn khách)
        List<Ban> banCoKhach = tableController.getBanDangCoKhach();
        for (Ban b : banCoKhach) {
            if (!b.getMaBan().equals(banNguon.getMaBan()) && !"MANG_VE".equals(b.getMaBan())) {
                cbDanhSachBan.addItem(new BanItem(b, " (Đang khách)"));
            }
        }
    }

    private void loadTableData() {
        for (CartItem item : cartData) {
            String sizeStr = item.getSize().getTenSize().equalsIgnoreCase("Thường") ? "" : " (" + item.getSize().getTenSize() + ")";
            String tenMon = item.getMon().getTenMon() + sizeStr;
            tableModel.addRow(new Object[]{tenMon, item.getSoLuong(), 0});
        }
    }

    private void commitAction() {
        // Dừng cell editing nếu người dùng đang nhập
        if (itemTable.isEditing()) {
            itemTable.getCellEditor().stopCellEditing();
        }

        BanItem selectedBan = (BanItem) cbDanhSachBan.getSelectedItem();
        if (selectedBan == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn đích!");
            return;
        }

        Map<CartItem, Integer> transferData = new HashMap<>();
        boolean hasItemToTransfer = false;

        for (int i = 0; i < cartData.size(); i++) {
            CartItem item = cartData.get(i);
            int currentQty = item.getSoLuong();
            int transferQty = 0;

            try {
                Object val = tableModel.getValueAt(i, 2);
                transferQty = Integer.parseInt(val.toString().trim());
            } catch (Exception ex) {
                transferQty = 0;
            }

            if (transferQty < 0) {
                JOptionPane.showMessageDialog(this, "Số lượng chuyển không hợp lệ tại món thứ " + (i + 1));
                return;
            }
            if (transferQty > currentQty) {
                JOptionPane.showMessageDialog(this, "Không thể chuyển quá số lượng hiện có tại món thứ " + (i + 1));
                return;
            }

            if (transferQty > 0) {
                transferData.put(item, transferQty);
                hasItemToTransfer = true;
            }
        }

        if (!hasItemToTransfer) {
            JOptionPane.showMessageDialog(this, "Bạn chưa chọn bất kỳ món nào để chuyển!");
            return;
        }

        try {
            tableController.tachMon(donHangHienTai.getMaDonHang(), banNguon.getMaBan(), selectedBan.ban.getMaBan(), transferData);
            JOptionPane.showMessageDialog(this, "Đã TÁCH MÓN thành công sang " + selectedBan.toString() + "!");
            success = true;
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tách món: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return success;
    }

    private static class BanItem {
        Ban ban;
        String desc;
        BanItem(Ban ban, String desc) { this.ban = ban; this.desc = desc; }
        @Override public String toString() { return "Bàn " + ban.getSoBan() + desc; }
    }
}
