package ui.dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog đăng ký ca làm việc (Tạm thời trống)
 */
public class ShiftRegistrationDialog extends JDialog {

    public ShiftRegistrationDialog(Frame owner) {
        super(owner, "Đăng ký ca làm việc", true);
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblInfo = new JLabel("Thông tin đăng ký ca sẽ được cập nhật ở phiên bản sau.");
        lblInfo.setFont(new Font("Roboto", Font.ITALIC, 14));
        content.add(lblInfo);

        add(content, BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());

        JPanel pnlSouth = new JPanel();
        pnlSouth.add(btnClose);
        add(pnlSouth, BorderLayout.SOUTH);
    }
}
