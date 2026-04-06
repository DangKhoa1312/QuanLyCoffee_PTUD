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
        super(parent, "M\u1EDF Ca L\u00E0m Vi\u1EC7c", true);
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
        JLabel lblIcon = new JLabel("\uD83D\uDD70", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Roboto Emoji", Font.PLAIN, 36));
        lblIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("M\u1EDF Ca L\u00E0m Vi\u1EC7c", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(39, 174, 96));
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        // Info
        String tenNV = SessionManager.getCurrentUser().getTenNV();
        String ngay = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        JLabel lblInfo = new JLabel("Nh\u00E2n vi\u00EAn: " + tenNV + "  |  Ng\u00E0y: " + ngay);
        lblInfo.setFont(new Font("Roboto", Font.PLAIN, 13));
        lblInfo.setForeground(new Color(120, 120, 120));
        lblInfo.setAlignmentX(CENTER_ALIGNMENT);

        // Money input
        JLabel lblTien = new JLabel("S\u1ED1 ti\u1EC1n \u0111\u1EA7u ca (VN\u0110)");
        lblTien.setFont(new Font("Roboto", Font.BOLD, 13));
        lblTien.setForeground(new Color(80, 80, 80));
        lblTien.setAlignmentX(LEFT_ALIGNMENT);

        JTextField txtTien = new JTextField("0");
        txtTien.setFont(new Font("Roboto", Font.BOLD, 18));
        txtTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        txtTien.setHorizontalAlignment(JTextField.CENTER);

        JLabel lblNote = new JLabel("Ki\u1EC3m \u0111\u1EBDm ti\u1EC1n m\u1EB7t trong k\u00E9t tr\u01B0\u1EDBc khi b\u1EAFt \u0111\u1EA7u.");
        lblNote.setFont(new Font("Roboto", Font.ITALIC, 11));
        lblNote.setForeground(new Color(160, 160, 160));
        lblNote.setAlignmentX(LEFT_ALIGNMENT);

        // Button
        JButton btnStart = new JButton("B\u1EAET \u0110\u1EA6U CA");
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
                JOptionPane.showMessageDialog(this, "Vui l\u00F2ng nh\u1EADp s\u1ED1 h\u1EE3p l\u1EC7!", "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "L\u1ED7i", JOptionPane.WARNING_MESSAGE);
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