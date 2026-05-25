package ui.dialog;

import controller.ShiftController;
import exception.AppException;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dialog bắt buộc mở ca trước khi vào hệ thống.
 * Chỉ cần nhập số tiền đầu ca, không cần chọn khu vực.
 */
public class ShiftOpenDialog extends JDialog {

    private final ShiftController shiftController;
    private boolean shiftOpened = false;

    public ShiftOpenDialog(JFrame parent, ShiftController shiftController) {
        super(parent, "Mở Ca Làm Việc", true);
        this.shiftController = shiftController;

        setSize(420, 340);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shiftOpened = false;
                dispose();
            }
        });

        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Icon + Title
        JLabel lblIcon = new JLabel("🕰", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Roboto Emoji", Font.PLAIN, 36));
        lblIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("Mở Ca Làm Việc", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(39, 174, 96));
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        // Info
        String maNV = SessionManager.getCurrentUser().getMaNV();
        String ngay = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        JLabel lblInfo = new JLabel("Mã NV: " + maNV + "  |  Ngày: " + ngay);
        lblInfo.setFont(new Font("Roboto", Font.PLAIN, 13));
        lblInfo.setForeground(new Color(120, 120, 120));
        lblInfo.setAlignmentX(CENTER_ALIGNMENT);

        // Money input
        JLabel lblTien = new JLabel("Số tiền đầu ca (VNĐ)");
        lblTien.setFont(new Font("Roboto", Font.BOLD, 13));
        lblTien.setForeground(new Color(80, 80, 80));
        lblTien.setAlignmentX(LEFT_ALIGNMENT);

        JTextField txtTien = new JTextField("0");
        txtTien.setFont(new Font("Roboto", Font.BOLD, 18));
        txtTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        txtTien.setHorizontalAlignment(JTextField.CENTER);

        JLabel lblNote = new JLabel("Kiểm đẽm tiền mặt trong két trước khi bắt đầu.");
        lblNote.setFont(new Font("Roboto", Font.ITALIC, 11));
        lblNote.setForeground(new Color(160, 160, 160));
        lblNote.setAlignmentX(LEFT_ALIGNMENT);

        // Button
        JButton btnStart = new JButton("BẮT ĐẦU CA");
        btnStart.setFont(new Font("Roboto", Font.BOLD, 15));
        btnStart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnStart.setAlignmentX(CENTER_ALIGNMENT);
        btnStart.setBackground(new Color(39, 174, 96));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusable(false);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnStart.addActionListener(e -> {
            try {
                double tien = Double.parseDouble(txtTien.getText().trim());
                // Mở ca không cần khu vực (truyền null)
                shiftController.moCa(tien, null);
                shiftOpened = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Layout
        main.add(lblIcon);
        main.add(Box.createVerticalStrut(6));
        main.add(lblTitle);
        main.add(Box.createVerticalStrut(4));
        main.add(lblInfo);
        main.add(Box.createVerticalStrut(24));
        main.add(lblTien);
        main.add(Box.createVerticalStrut(4));
        main.add(txtTien);
        main.add(Box.createVerticalStrut(4));
        main.add(lblNote);
        main.add(Box.createVerticalStrut(20));
        main.add(btnStart);

        setContentPane(main);
    }

    public boolean isShiftOpened() {
        return shiftOpened;
    }
}