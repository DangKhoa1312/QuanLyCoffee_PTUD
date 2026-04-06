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
        super(parent, "T\u00F9y Ch\u1ECDn Chuy\u1EC3n / G\u1ED9p B\u00E0n", true);
        this.banNguon = banNguon;
        this.donHangHienTai = donHang;
        this.tableController = new TableController();

        setSize(400, 250);
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
        JLabel lblTitle = new JLabel("B\u00E0n hi\u1EC7n t\u1EA1i: " + banNguon.getSoBan(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 18));
        main.add(lblTitle, BorderLayout.NORTH);

        // ── Khung giữa: Thao tác ──
        JPanel pnlCenter = new JPanel(new GridLayout(2, 1, 0, 10));
        pnlCenter.setOpaque(false);

        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlRadio.setOpaque(false);

        rbChuyenBan = new JRadioButton("Chuy\u1EC3n sang B\u00E0n m\u1EDBi");
        rbGopBan = new JRadioButton("G\u1ED9p v\u00E0o B\u00E0n kh\u00E1c");
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
        pnlCombo.add(new JLabel("Ch\u1ECDn B\u00E0n \u0110\u00EDch:"));
        
        cbDanhSachBan = new JComboBox<>();
        cbDanhSachBan.setPreferredSize(new Dimension(150, 30));
        cbDanhSachBan.setFont(new Font("Roboto", Font.PLAIN, 14));
        pnlCombo.add(cbDanhSachBan);

        pnlCenter.add(pnlCombo);
        main.add(pnlCenter, BorderLayout.CENTER);

        // ── Bot Buttons ──
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBot.setOpaque(false);

        JButton btnHuy = new JButton("H\u1EE7y");
        btnHuy.addActionListener(e -> dispose());

        JButton btnXacNhan = new JButton("X\u00E1c Nh\u1EADn");
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
                    cbDanhSachBan.addItem(new BanItem(b, " (Tr\u1ED1ng)"));
                }
            }
        } else {
            List<Ban> banCoKhach = tableController.getBanDangCoKhach();
            for (Ban b : banCoKhach) {
                if (!b.getMaBan().equals(banNguon.getMaBan()) && !"MANG_VE".equals(b.getMaBan())) {
                    cbDanhSachBan.addItem(new BanItem(b, " (\u0110ang kh\u00E1ch)"));
                }
            }
        }
    }

    private void commitAction() {
        BanItem selected = (BanItem) cbDanhSachBan.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui l\u00F2ng ch\u1ECDn b\u00E0n \u0111\u00EDch!");
            return;
        }

        try {
            if (rbChuyenBan.isSelected()) {
                tableController.chuyenBan(donHangHienTai.getMaDonHang(), banNguon.getMaBan(), selected.ban.getMaBan());
                JOptionPane.showMessageDialog(this, "\u0110\u00E3 CHUY\u1EC2N B\u00C0N th\u00E0nh c\u00F4ng!");
            } else {
                DonHang dhDich = tableController.getDonHangDangMo(selected.ban.getMaBan());
                if (dhDich == null) {
                    JOptionPane.showMessageDialog(this, "B\u00E0n \u0111\u00EDch kh\u00F4ng c\u00F3 \u0111\u01A1n \u0111ang m\u1EDF!");
                    return;
                }
                tableController.gopBan(donHangHienTai.getMaDonHang(), dhDich.getMaDonHang(), banNguon.getMaBan(), selected.ban.getMaBan());
                JOptionPane.showMessageDialog(this, "\u0110\u00E3 G\u1ED8P B\u00C0N th\u00E0nh c\u00F4ng!");
            }
            success = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "L\u1ED7i thao t\u00E1c: " + ex.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return success;
    }

    private static class BanItem {
        Ban ban;
        String desc;
        BanItem(Ban ban, String desc) { this.ban = ban; this.desc = desc; }
        @Override public String toString() { return "B\u00E0n " + ban.getSoBan() + desc; }
    }
}
