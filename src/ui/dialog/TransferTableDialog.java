package ui.dialog;

import controller.TableController;
import entity.Ban;
import entity.DonHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Giao diện thực hiện Chuyển Bàn hoặc Gộp Bàn.
 */
public class TransferTableDialog extends JDialog {

    private final DonHang donHangHienTai;
    private final Ban banNguon;
    private final TableController tableController;
    private final int mode; // 1: Chuyển Bàn, 2: Ghép Bàn
    private boolean success = false;

    private JComboBox<BanItem> cbDanhSachBan;

    public TransferTableDialog(JFrame parent, Ban banNguon, DonHang donHang, int mode) {
        super(parent, mode == 1 ? "Tùy Chọn Chuyển Bàn" : "Tùy Chọn Ghép Bàn", true);
        this.banNguon = banNguon;
        this.donHangHienTai = donHang;
        this.mode = mode;
        this.tableController = new TableController();

        setSize(450, 220);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
        loadComboData();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(15, 20, 15, 20));

        // ── Tiêu đề ──
        JLabel lblTitle = new JLabel("Bàn hiện tại: " + banNguon.getSoBan(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 18));
        main.add(lblTitle, BorderLayout.NORTH);

        // ── Khung giữa: Thao tác ──
        JPanel pnlCenter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(10, 0, 10, 0));

        pnlCenter.add(new JLabel("Chọn Bàn Đích:"));
        
        cbDanhSachBan = new JComboBox<>();
        cbDanhSachBan.setPreferredSize(new Dimension(250, 32));
        cbDanhSachBan.setFont(new Font("Roboto", Font.PLAIN, 14));
        pnlCenter.add(cbDanhSachBan);

        main.add(pnlCenter, BorderLayout.CENTER);

        // ── Bot Buttons ──
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBot.setOpaque(false);

        JButton btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dispose());

        JButton btnXacNhan = new JButton("Xác Nhận");
        btnXacNhan.setBackground(new Color(41, 128, 185));
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
        if (mode == 1) { // Chuyển Bàn -> Bàn đích là bàn trống
            List<Ban> banTrongs = tableController.getBanTrong();
            for (Ban b : banTrongs) {
                if (!"MANG_VE".equals(b.getMaBan())) {
                    cbDanhSachBan.addItem(new BanItem(b, " (Trống)"));
                }
            }
        } else if (mode == 2) { // Ghép bàn -> Bàn đích là bàn có khách
            List<Ban> banCoKhach = tableController.getBanDangCoKhach();
            for (Ban b : banCoKhach) {
                if (!b.getMaBan().equals(banNguon.getMaBan()) && !"MANG_VE".equals(b.getMaBan())) {
                    cbDanhSachBan.addItem(new BanItem(b, " (Đang khách)"));
                }
            }
        }
    }

    private void commitAction() {
        BanItem selected = (BanItem) cbDanhSachBan.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn đích!");
            return;
        }

        try {
            if (mode == 1) {
                tableController.chuyenBan(donHangHienTai.getMaDonHang(), banNguon.getMaBan(), selected.ban.getMaBan());
                JOptionPane.showMessageDialog(this, "Đã CHUYỂN BÀN thành công!");
            } else if (mode == 2) {
                DonHang dhDich = tableController.getDonHangDangMo(selected.ban.getMaBan());
                if (dhDich == null) {
                    JOptionPane.showMessageDialog(this, "Bàn đích không có đơn đang mở!");
                    return;
                }
                tableController.gopBan(donHangHienTai.getMaDonHang(), dhDich.getMaDonHang(), banNguon.getMaBan(), selected.ban.getMaBan());
                JOptionPane.showMessageDialog(this, "Đã GỘP BÀN thành công!");
            }
            success = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thao tác: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
