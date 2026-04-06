package ui.dialog;

import controller.ShiftController;
import entity.CaLamViec;
import exception.AppException;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Dialog đóng ca: hiển thị báo cáo tổng kết ca làm việc,
 * cho nhân viên nhập tiền thực đếm, tính chênh lệch.
 */
public class ShiftCloseDialog extends JDialog {

    private final ShiftController shiftController;
    private boolean shiftClosed = false;

    public ShiftCloseDialog(JFrame parent, ShiftController shiftController) {
        super(parent, "\u0110\u00F3ng Ca L\u00E0m Vi\u1EC7c", true);
        this.shiftController = shiftController;

        setSize(460, 520);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        CaLamViec ca = SessionManager.getCurrentCa();
        if (ca == null) {
            dispose();
            return;
        }

        String maCa = ca.getMaCa();
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

        // Lấy dữ liệu báo cáo
        double tienDauCa = ca.getTongDoanhThu(); // Tiền mặt đầu ca
        int tongDon = shiftController.getTongSoDon(maCa);
        int donHoanThanh = shiftController.getSoDonHoanThanh(maCa);
        double doanhThu = shiftController.getTongDoanhThuCa(maCa);
        String gioBatDau = ca.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm"));
        String gioHienTai = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // ── Build UI ──
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(25, 35, 25, 35));

        // Title
        JLabel lblTitle = new JLabel("B\u00C1O C\u00C1O CU\u1ED0I CA", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(231, 76, 60));
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblCa = new JLabel("Ca: " + maCa + "  |  " + gioBatDau + " \u2192 " + gioHienTai, SwingConstants.CENTER);
        lblCa.setFont(new Font("Roboto", Font.PLAIN, 13));
        lblCa.setForeground(new Color(130, 130, 130));
        lblCa.setAlignmentX(CENTER_ALIGNMENT);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Report table
        JPanel reportPanel = new JPanel(new GridLayout(0, 2, 10, 12));
        reportPanel.setOpaque(false);
        reportPanel.setAlignmentX(LEFT_ALIGNMENT);

        addReportRow(reportPanel, "Nh\u00E2n vi\u00EAn:", SessionManager.getCurrentUser().getTenNV());
        addReportRow(reportPanel, "Ti\u1EC1n \u0111\u1EA7u ca:", nf.format(tienDauCa) + " \u0111");
        addReportRow(reportPanel, "T\u1ED5ng s\u1ED1 \u0111\u01A1n:", String.valueOf(tongDon));
        addReportRow(reportPanel, "\u0110\u01A1n ho\u00E0n th\u00E0nh:", String.valueOf(donHoanThanh));
        addReportRow(reportPanel, "Doanh thu (h\u1EC7 th\u1ED1ng):", nf.format(doanhThu) + " \u0111");

        double tienCanCo = tienDauCa + doanhThu;
        addReportRow(reportPanel, "Ti\u1EC1n c\u1EA7n c\u00F3 trong k\u00E9t:", nf.format(tienCanCo) + " \u0111");

        // Separator 2
        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Input tiền thực đếm
        JLabel lblThucDem = new JLabel("Ti\u1EC1n m\u1EB7t th\u1EF1c \u0111\u1EBFm (VN\u0110):");
        lblThucDem.setFont(new Font("Roboto", Font.BOLD, 14));
        lblThucDem.setForeground(new Color(44, 62, 80));
        lblThucDem.setAlignmentX(LEFT_ALIGNMENT);

        JTextField txtThucDem = new JTextField(nf.format(tienCanCo));
        txtThucDem.setFont(new Font("Roboto", Font.BOLD, 20));
        txtThucDem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        txtThucDem.setHorizontalAlignment(JTextField.CENTER);

        // Chênh lệch label
        JLabel lblChenhLech = new JLabel("Ch\u00EAnh l\u1EC7ch: 0 \u0111");
        lblChenhLech.setFont(new Font("Roboto", Font.BOLD, 15));
        lblChenhLech.setForeground(new Color(39, 174, 96));
        lblChenhLech.setAlignmentX(CENTER_ALIGNMENT);

        // Auto update chênh lệch
        txtThucDem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                try {
                    String raw = txtThucDem.getText().trim().replace(".", "").replace(",", "");
                    double thucDemVal = Double.parseDouble(raw);
                    double chenhLech = thucDemVal - tienCanCo;
                    lblChenhLech.setText("Ch\u00EAnh l\u1EC7ch: " + nf.format(chenhLech) + " \u0111");
                    if (chenhLech < 0) {
                        lblChenhLech.setForeground(new Color(231, 76, 60)); // Thiếu = Đỏ
                    } else if (chenhLech > 0) {
                        lblChenhLech.setForeground(new Color(243, 156, 18)); // Thừa = Cam
                    } else {
                        lblChenhLech.setForeground(new Color(39, 174, 96)); // Khớp = Xanh
                    }
                } catch (NumberFormatException ignore) {
                    lblChenhLech.setText("Ch\u00EAnh l\u1EC7ch: --");
                    lblChenhLech.setForeground(Color.GRAY);
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        // Button
        JButton btnDongCa = new JButton("X\u00C1C NH\u1EACN \u0110\u00D3NG CA");
        btnDongCa.setFont(new Font("Roboto", Font.BOLD, 15));
        btnDongCa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnDongCa.setAlignmentX(CENTER_ALIGNMENT);
        btnDongCa.setBackground(new Color(231, 76, 60));
        btnDongCa.setForeground(Color.WHITE);
        btnDongCa.setFocusable(false);
        btnDongCa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnDongCa.addActionListener(e -> {
            try {
                String raw = txtThucDem.getText().trim().replace(".", "").replace(",", "");
                double thucDem = Double.parseDouble(raw);
                shiftController.dongCa(thucDem);
                shiftClosed = true;
                JOptionPane.showMessageDialog(this,
                    "\u0110\u00E3 \u0111\u00F3ng ca th\u00E0nh c\u00F4ng!\nDoanh thu: " + nf.format(doanhThu) + " \u0111",
                    "Th\u00E0nh c\u00F4ng", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui l\u00F2ng nh\u1EADp s\u1ED1 h\u1EE3p l\u1EC7!", "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Layout
        main.add(lblTitle);
        main.add(Box.createVerticalStrut(4));
        main.add(lblCa);
        main.add(Box.createVerticalStrut(16));
        main.add(sep);
        main.add(Box.createVerticalStrut(16));
        main.add(reportPanel);
        main.add(Box.createVerticalStrut(16));
        main.add(sep2);
        main.add(Box.createVerticalStrut(16));
        main.add(lblThucDem);
        main.add(Box.createVerticalStrut(6));
        main.add(txtThucDem);
        main.add(Box.createVerticalStrut(10));
        main.add(lblChenhLech);
        main.add(Box.createVerticalStrut(20));
        main.add(btnDongCa);

        setContentPane(main);
    }

    private void addReportRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Roboto", Font.PLAIN, 13));
        lbl.setForeground(new Color(100, 100, 100));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Roboto", Font.BOLD, 13));
        val.setForeground(new Color(44, 62, 80));
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(lbl);
        panel.add(val);
    }

    public boolean isShiftClosed() {
        return shiftClosed;
    }
}
