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
 * Sử dụng TablePickerDialog dạng sơ đồ để chọn bàn đích.
 */
public class TransferItemsDialog extends JDialog {

    private final Ban banNguon;
    private final DonHang donHangHienTai;
    private final List<CartItem> cartData;
    private final TableController tableController;
    private boolean success = false;

    private JTable itemTable;
    private DefaultTableModel tableModel;

    private Ban banDich = null; // Bàn đích đã chọn
    private JLabel lblBanDich;
    private JButton btnChonBan;

    public TransferItemsDialog(JFrame parent, Ban banNguon, DonHang donHang, List<CartItem> cartData) {
        super(parent, "Tách Món Từ Bàn " + banNguon.getSoBan(), true);
        this.banNguon = banNguon;
        this.donHangHienTai = donHang;
        this.cartData = cartData;
        this.tableController = new TableController();

        setSize(600, 500);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(15, 20, 15, 20));

        // ── Top: Chọn bàn đích bằng sơ đồ ──
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTop.setOpaque(false);

        pnlTop.add(new JLabel("Bàn đích:"));

        lblBanDich = new JLabel("─── Chưa chọn ───");
        lblBanDich.setFont(new Font("Roboto", Font.BOLD, 14));
        lblBanDich.setForeground(new Color(180, 60, 60));
        pnlTop.add(lblBanDich);

        btnChonBan = new JButton("📍 Chọn bàn từ sơ đồ...");
        btnChonBan.setFont(new Font("Roboto", Font.BOLD, 13));
        btnChonBan.setBackground(new Color(41, 128, 185));
        btnChonBan.setForeground(Color.WHITE);
        btnChonBan.putClientProperty("JButton.buttonArc", 8);
        btnChonBan.putClientProperty("JButton.borderWidth", 0);
        btnChonBan.setFocusable(false);
        btnChonBan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnChonBan.addActionListener(e -> chonBanDich());
        pnlTop.add(btnChonBan);

        main.add(pnlTop, BorderLayout.NORTH);

        // ── Center: Danh sách món ──
        String[] cols = {"Món", "SL Hiện Tại", "SL Tách"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };
        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(35);
        itemTable.setFont(new Font("Roboto", Font.PLAIN, 14));
        itemTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));

        loadTableData();

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            "Danh sách món — Nhập số lượng muốn tách vào cột \"SL Tách\""));
        main.add(scrollPane, BorderLayout.CENTER);

        // ── Bot: Buttons ──
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBot.setOpaque(false);

        JButton btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dispose());

        JButton btnXacNhan = new JButton("✔ Xác Nhận Tách Món");
        btnXacNhan.setBackground(new Color(231, 76, 60));
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setFont(new Font("Roboto", Font.BOLD, 13));
        btnXacNhan.putClientProperty("JButton.buttonArc", 8);
        btnXacNhan.putClientProperty("JButton.borderWidth", 0);
        btnXacNhan.addActionListener(e -> commitAction());

        pnlBot.add(btnHuy);
        pnlBot.add(btnXacNhan);
        main.add(pnlBot, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void chonBanDich() {
        Window win = SwingUtilities.getWindowAncestor(this);
        JFrame frame = (win instanceof JFrame) ? (JFrame) win : null;

        TablePickerDialog picker = new TablePickerDialog(
            frame,
            "Chọn bàn đích để tách món sang",
            TablePickerDialog.MODE_ALL,
            banNguon.getMaBan()
        );
        picker.setVisible(true);

        Ban chon = picker.getSelectedBan();
        if (chon != null) {
            banDich = chon;
            String trangThai = banDich.getTrangThai() == enums.TrangThaiBan.TRONG ? "(Trống)" : "(Đang khách)";
            lblBanDich.setText("Bàn " + banDich.getSoBan() + " " + trangThai);
            lblBanDich.setForeground(new Color(39, 174, 96));
        }
    }

    private void loadTableData() {
        for (CartItem item : cartData) {
            String sizeStr = item.getSize().getTenSize().equalsIgnoreCase("Thường")
                ? "" : " (" + item.getSize().getTenSize() + ")";
            String tenMon = item.getMon().getTenMon() + sizeStr;
            tableModel.addRow(new Object[]{tenMon, item.getSoLuong(), 0});
        }
    }

    private void commitAction() {
        if (itemTable.isEditing()) {
            itemTable.getCellEditor().stopCellEditing();
        }

        if (banDich == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn đích từ sơ đồ!", "Chưa chọn bàn", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Bạn chưa nhập số lượng món nào để tách!");
            return;
        }

        try {
            tableController.tachMon(donHangHienTai.getMaDonHang(), banNguon.getMaBan(), banDich.getMaBan(), transferData);
            JOptionPane.showMessageDialog(this, "Đã TÁCH MÓN thành công sang Bàn " + banDich.getSoBan() + "!");
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
}
