package ui.dialog;

import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * PasswordResetDialog nâng cấp: Giao diện thẻ (Card), có Icon minh họa
 * và tuân thủ các hằng số FontAwesome chính xác.
 */
public class PasswordResetDialog extends JDialog {

    private boolean confirmed = false;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public PasswordResetDialog(Frame parent, String maNV) {
        super(parent, "Đổi mật khẩu - " + maNV, true);
        initUI();
    }

    private void initUI() {
        setSize(400, 320);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // Header
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        pnlHeader.setBackground(PRIMARY_COLOR);
        JLabel lblTitle = new JLabel(" THIẾT LẬP MẬT KHẨU MỚI");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setIcon(IconFontSwing.buildIcon(FontAwesome.KEY, 20, Color.WHITE));
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Body (Card)
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel lblInfo = new JLabel("Nhập mật khẩu mới cho nhân viên:");
        lblInfo.setFont(new Font("Roboto", Font.ITALIC, 13));
        lblInfo.setForeground(Color.GRAY);
        card.add(lblInfo);
        card.add(Box.createVerticalStrut(15));

        addInputLabel(card, "Mật khẩu mới:", FontAwesome.LOCK);
        txtNewPassword = new JPasswordField();
        txtNewPassword.setPreferredSize(new Dimension(0, 35));
        txtNewPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        card.add(txtNewPassword);
        card.add(Box.createVerticalStrut(10));

        addInputLabel(card, "Xác nhận mật khẩu:", FontAwesome.SHIELD);
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setPreferredSize(new Dimension(0, 35));
        txtConfirmPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        card.add(txtConfirmPassword);

        body.add(card, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        footer.setOpaque(false);

        JButton btnSave = new JButton(" CẬP NHẬT");
        btnSave.setPreferredSize(new Dimension(130, 38));
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Roboto", Font.BOLD, 13));
        btnSave.setIcon(IconFontSwing.buildIcon(FontAwesome.FLOPPY_O, 16, Color.WHITE));
        btnSave.addActionListener(e -> handleSave());

        JButton btnCancel = new JButton("HỦY");
        btnCancel.setPreferredSize(new Dimension(80, 38));
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);
    }

    private void addInputLabel(JPanel container, String text, FontAwesome icon) {
        JLabel lbl = new JLabel(" " + text);
        lbl.setIcon(IconFontSwing.buildIcon(icon, 14, new Color(106, 90, 205)));
        lbl.setFont(new Font("Roboto", Font.BOLD, 12));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(lbl);
        container.add(Box.createVerticalStrut(5));
    }

    private void handleSave() {
        String pass = new String(txtNewPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        if (pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        confirmed = true;
        dispose();
    }

    public String getNewPassword() {
        return new String(txtNewPassword.getPassword());
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
