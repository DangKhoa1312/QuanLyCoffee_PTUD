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
        super(parent, "Đóng Ca Làm Việc", true);
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
        JLabel lblTitle = new JLabel("BÁO CÁO CUỐI CA", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(231, 76, 60));
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblCa = new JLabel("Ca: " + maCa + "  |  " + gioBatDau + " → " + gioHienTai, SwingConstants.CENTER);
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

        addReportRow(reportPanel, "Nhân viên:", SessionManager.getCurrentUser().getTenNV());
        addReportRow(reportPanel, "Tiền đầu ca:", nf.format(tienDauCa) + " đ");
        addReportRow(reportPanel, "Tổng số đơn:", String.valueOf(tongDon));
        addReportRow(reportPanel, "Đơn hoàn thành:", String.valueOf(donHoanThanh));
        addReportRow(reportPanel, "Doanh thu (hệ thống):", nf.format(doanhThu) + " đ");

        double tienCanCo = tienDauCa + doanhThu;
        addReportRow(reportPanel, "Tiền cần có trong két:", nf.format(tienCanCo) + " đ");

        // Separator 2
        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Input tiền thực đếm
        JLabel lblThucDem = new JLabel("Tiền mặt thực đếm (VNĐ):");
        lblThucDem.setFont(new Font("Roboto", Font.BOLD, 14));
        lblThucDem.setForeground(new Color(44, 62, 80));
        lblThucDem.setAlignmentX(LEFT_ALIGNMENT);

        JTextField txtThucDem = new JTextField(nf.format(tienCanCo));
        txtThucDem.setFont(new Font("Roboto", Font.BOLD, 20));
        txtThucDem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        txtThucDem.setHorizontalAlignment(JTextField.CENTER);

        // Chênh lệch label
        JLabel lblChenhLech = new JLabel("Chênh lệch: 0 đ");
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
                    lblChenhLech.setText("Chênh lệch: " + nf.format(chenhLech) + " đ");
                    if (chenhLech < 0) {
                        lblChenhLech.setForeground(new Color(231, 76, 60)); // Thiếu = Đỏ
                    } else if (chenhLech > 0) {
                        lblChenhLech.setForeground(new Color(243, 156, 18)); // Thừa = Cam
                    } else {
                        lblChenhLech.setForeground(new Color(39, 174, 96)); // Khớp = Xanh
                    }
                } catch (NumberFormatException ignore) {
                    lblChenhLech.setText("Chênh lệch: --");
                    lblChenhLech.setForeground(Color.GRAY);
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        // Button
        JButton btnDongCa = new JButton("XÁC NHẬN ĐÓNG CA");
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
                    "Đã đóng ca thành công!\nDoanh thu: " + nf.format(doanhThu) + " đ",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
