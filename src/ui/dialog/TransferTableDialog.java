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
    private boolean success = false;

    private JRadioButton rbChuyenBan;
    private JRadioButton rbGopBan;
    private JComboBox<BanItem> cbDanhSachBan;

    public TransferTableDialog(JFrame parent, Ban banNguon, DonHang donHang) {
        super(parent, "Tùy Chọn Chuyển / Gộp Bàn", true);
        this.banNguon = banNguon;
        this.donHangHienTai = donHang;
        this.tableController = new TableController();

        setSize(520, 320);
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
        JPanel pnlCenter = new JPanel(new GridLayout(2, 1, 0, 10));
        pnlCenter.setOpaque(false);

        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlRadio.setOpaque(false);

        rbChuyenBan = new JRadioButton("Chuyển sang Bàn mới");
        rbGopBan = new JRadioButton("Gộp vào Bàn khác");
        rbChuyenBan.setOpaque(false);
        rbGopBan.setOpaque(false);
        rbChuyenBan.setFont(new Font("Roboto", Font.BOLD, 14));
        rbGopBan.setFont(new Font("Roboto", Font.BOLD, 14));

        ButtonGroup group = new ButtonGroup();
        group.add(rbChuyenBan);
        group.add(rbGopBan);
        pnlRadio.add(rbChuyenBan);
        pnlRadio.add(rbGopBan);
        rbChuyenBan.setSelected(true);

        rbChuyenBan.addActionListener(e -> loadComboData());
        rbGopBan.addActionListener(e -> loadComboData());

        pnlCenter.add(pnlRadio);

        JPanel pnlCombo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlCombo.setOpaque(false);
        pnlCombo.add(new JLabel("Chọn Bàn Đích:"));
        
        cbDanhSachBan = new JComboBox<>();
        cbDanhSachBan.setPreferredSize(new Dimension(250, 32));
        cbDanhSachBan.setFont(new Font("Roboto", Font.PLAIN, 14));
        pnlCombo.add(cbDanhSachBan);

        pnlCenter.add(pnlCombo);
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
        if (rbChuyenBan.isSelected()) {
            List<Ban> banTrongs = tableController.getBanTrong();
            for (Ban b : banTrongs) {
                if (!"MANG_VE".equals(b.getMaBan())) {
                    cbDanhSachBan.addItem(new BanItem(b, " (Trống)"));
                }
            }
        } else {
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
            if (rbChuyenBan.isSelected()) {
                tableController.chuyenBan(donHangHienTai.getMaDonHang(), banNguon.getMaBan(), selected.ban.getMaBan());
                JOptionPane.showMessageDialog(this, "Đã CHUYỂN BÀN thành công!");
            } else {
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
