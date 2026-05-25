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
        super(parent, "Tổng Kết & Đăng Xuất", true);
        this.shiftController = shiftController;

        setSize(460, 420);
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
        int tongDon = shiftController.getTongSoDon(maCa);
        int donHoanThanh = shiftController.getSoDonHoanThanh(maCa);
        double doanhThu = shiftController.getTongDoanhThuCa(maCa);
        int banDangPhucVu = shiftController.getSoBanDangPhucVu();
        
        String gioBatDau = ca.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm"));
        String gioHienTai = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // ── Build UI ──
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(25, 35, 25, 35));

        // Title
        JLabel lblTitle = new JLabel("TỔNG KẾT CUỐI CA", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblCa = new JLabel("Ca: " + maCa + "  |  " + gioBatDau + " → " + gioHienTai, SwingConstants.CENTER);
        lblCa.setFont(new Font("Roboto", Font.PLAIN, 13));
        lblCa.setForeground(new Color(130, 130, 130));
        lblCa.setAlignmentX(CENTER_ALIGNMENT);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Report table
        JPanel reportPanel = new JPanel(new GridBagLayout());
        reportPanel.setOpaque(false);
        // Set alignment to CENTER to match other components in BoxLayout, 
        // but it will fill the width so internal alignment works.
        reportPanel.setAlignmentX(CENTER_ALIGNMENT);
        reportPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180)); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        addReportRow(reportPanel, gbc, 0, "Mã NV:", SessionManager.getCurrentUser().getMaNV(), false);
        addReportRow(reportPanel, gbc, 1, "Tổng số đơn:", String.valueOf(tongDon), false);
        addReportRow(reportPanel, gbc, 2, "Đơn hoàn thành:", String.valueOf(donHoanThanh), false);
        addReportRow(reportPanel, gbc, 3, "Doanh thu trong ca:", nf.format(doanhThu) + " đ", false);
        addReportRow(reportPanel, gbc, 4, "Bàn đang phục vụ:", String.valueOf(banDangPhucVu), true);

        // Warning Label
        JLabel lblWarning = new JLabel("");
        lblWarning.setFont(new Font("Roboto", Font.ITALIC, 12));
        lblWarning.setForeground(new Color(231, 76, 60));
        lblWarning.setAlignmentX(CENTER_ALIGNMENT);
        if (banDangPhucVu > 0) {
            lblWarning.setText("<html><center>Cảnh báo: Bạn vẫn còn " + banDangPhucVu + " bàn chưa thanh toán!<br>Hệ thống vẫn cho phép đăng xuất.</center></html>");
        }

        // Button
        JButton btnLogout = new JButton("XÁC NHẬN ĐĂNG XUẤT");
        btnLogout.setFont(new Font("Roboto", Font.BOLD, 15));
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnLogout.setAlignmentX(CENTER_ALIGNMENT);
        btnLogout.setBackground(new Color(44, 62, 80));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusable(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnLogout.addActionListener(e -> {
            try {
                // Đóng ca với doanh thu thực tế = doanh thu hệ thống (vì ẩn nhập tiền)
                shiftController.dongCa(doanhThu);
                shiftClosed = true;
                dispose();
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Layout
        main.add(lblTitle);
        main.add(Box.createVerticalStrut(4));
        main.add(lblCa);
        main.add(Box.createVerticalStrut(20));
        main.add(sep);
        main.add(Box.createVerticalStrut(20));
        main.add(reportPanel);
        main.add(Box.createVerticalStrut(20));
        if (banDangPhucVu > 0) {
            main.add(lblWarning);
            main.add(Box.createVerticalStrut(15));
        }
        main.add(Box.createVerticalGlue());
        main.add(btnLogout);

        setContentPane(main);
    }

    private void addReportRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value, boolean isWarning) {
        gbc.gridy = row;
        
        // Label
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Roboto", isWarning ? Font.BOLD : Font.PLAIN, 13));
        lbl.setForeground(isWarning ? new Color(231, 76, 60) : new Color(100, 100, 100));
        lbl.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(lbl, gbc);

        // Value
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel val = new JLabel(value);
        val.setFont(new Font("Roboto", Font.BOLD, isWarning ? 15 : 13));
        val.setForeground(isWarning ? new Color(231, 76, 60) : new Color(44, 62, 80));
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(val, gbc);
    }

    public boolean isShiftClosed() {
        return shiftClosed;
    }
}
