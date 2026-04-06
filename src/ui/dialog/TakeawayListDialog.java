package ui.dialog;

import entity.DonHang;
import controller.OrderController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TakeawayListDialog extends JDialog {

    private DonHang selectedOrder = null;
    private boolean isCreateNew = false;
    private final List<DonHang> listOrders;
    private final OrderController orderController;

    public TakeawayListDialog(JFrame parent, List<DonHang> listOrders, OrderController orderController) {
        super(parent, "Danh s\u00E1ch \u0110\u01A1n Mang V\u1EC1 Đang Chờ", true);
        this.listOrders = listOrders;
        this.orderController = orderController;
        setSize(450, 600); // Tăng chiều cao một chút để hiển thị thêm thông tin món
        setLocationRelativeTo(parent);
        initComponents();
    }

    private void initComponents() {
        JPanel pMain = new JPanel(new BorderLayout());
        pMain.setBackground(Color.WHITE);
        pMain.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JLabel lblTitle = new JLabel("\uD83D\uDECD DANH S\u00C1CH MANG V\u1EC0 \u0110ANG CH\u1EDC");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 18));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        pMain.add(lblTitle, BorderLayout.NORTH);

        // Center Content
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(Color.WHITE);

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

        if (listOrders == null || listOrders.isEmpty()) {
            JLabel lblEmpty = new JLabel("(Ch\u01B0a c\u00F3 \u0111\u01A1n ch\u1EDD n\u00E0o)");
            lblEmpty.setFont(new Font("Roboto", Font.ITALIC, 14));
            lblEmpty.setForeground(Color.GRAY);
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            bodyPanel.add(Box.createVerticalStrut(50));
            bodyPanel.add(lblEmpty);
        } else {
            for (DonHang dh : listOrders) {
                bodyPanel.add(createOrderCard(dh, nf, dtf));
                bodyPanel.add(Box.createVerticalStrut(10));
            }
        }

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        pMain.add(scrollPane, BorderLayout.CENTER);

        // Footer buttons
        JPanel pFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pFooter.setBackground(Color.WHITE);

        JButton btnNew = new JButton("+ T\u1EA0O \u0110\u01A1N M\u1EDAI");
        btnNew.setFont(new Font("Roboto", Font.BOLD, 14));
        btnNew.setBackground(new Color(46, 204, 113));
        btnNew.setForeground(Color.WHITE);
        btnNew.setFocusPainted(false);
        btnNew.setPreferredSize(new Dimension(180, 40));
        btnNew.addActionListener(e -> {
            isCreateNew = true;
            selectedOrder = null;
            dispose();
        });

        JButton btnCancel = new JButton("\u0110\u00F3ng");
        btnCancel.setFont(new Font("Roboto", Font.BOLD, 14));
        btnCancel.setBackground(new Color(230, 230, 230));
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.addActionListener(e -> dispose());

        pFooter.add(btnNew);
        pFooter.add(btnCancel);

        pMain.add(pFooter, BorderLayout.SOUTH);
        add(pMain);
    }

    private JPanel createOrderCard(DonHang dh, NumberFormat nf, DateTimeFormatter dtf) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(250, 252, 255));
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        card.setMaximumSize(new Dimension(1000, 110)); // Tăng chiều cao thẻ để chứa summary
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String time = dh.getThoiGianMo() != null ? dh.getThoiGianMo().format(dtf) : "--:--";
        JLabel lblLeft = new JLabel(String.format("<html><b>[%s]</b> - l\u00FAc %s</html>", dh.getMaDonHang(), time));
        lblLeft.setFont(new Font("Roboto", Font.PLAIN, 15));
        lblLeft.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblRight = new JLabel("T\u1ED5ng: " + nf.format(dh.getTongTienTamTinh()) + "\u0111", SwingConstants.RIGHT);
        lblRight.setFont(new Font("Roboto", Font.BOLD, 14));
        lblRight.setForeground(new Color(231, 76, 60));
        lblRight.setBorder(new EmptyBorder(10, 15, 10, 15));

        card.add(lblLeft, BorderLayout.NORTH);

        // Thêm tóm tắt món ăn ở giữa
        String summary = orderController.getOrderSummary(dh.getMaDonHang());
        JLabel lblSummary = new JLabel("<html><body style='width: 300px; padding-left: 15px; color: #555; font-style: italic;'>" + summary + "</body></html>");
        lblSummary.setFont(new Font("Roboto", Font.PLAIN, 12));
        card.add(lblSummary, BorderLayout.CENTER);

        card.add(lblRight, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(235, 245, 255));
                card.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 1));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(250, 252, 255));
                card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                isCreateNew = false;
                selectedOrder = dh;
                dispose();
            }
        });

        return card;
    }

    public DonHang getSelectedOrder() {
        return selectedOrder;
    }

    public boolean isCreateNew() {
        return isCreateNew;
    }
}
