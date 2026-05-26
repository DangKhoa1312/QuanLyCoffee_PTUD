package ui.panel;

import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HelpPanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    // View 1: Grid
    private JPanel gridPanel;
    
    // View 2: Detail
    private JPanel detailPanel;
    private JLabel lblDetailTitle;
    private JEditorPane htmlContentPane;

    public HelpPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setOpaque(false);
        
        initGridView();
        initDetailView();
        
        add(mainContainer, BorderLayout.CENTER);
        
        // Show grid by default
        cardLayout.show(mainContainer, "GRID");
    }

    private void initGridView() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 30, 10, 30));

        JLabel lblTitle = new JLabel("Trợ Giúp & Hướng Dẫn");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 28));
        lblTitle.setForeground(new Color(113, 76, 52));
        
        JLabel lblSub = new JLabel("Chọn một chủ đề để xem chi tiết.");
        lblSub.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblSub.setForeground(new Color(150, 150, 150));
        
        JPanel pnlTitleText = new JPanel();
        pnlTitleText.setLayout(new BoxLayout(pnlTitleText, BoxLayout.Y_AXIS));
        pnlTitleText.setOpaque(false);
        pnlTitleText.add(lblTitle);
        pnlTitleText.add(Box.createVerticalStrut(5));
        pnlTitleText.add(lblSub);
        
        header.add(pnlTitleText, BorderLayout.WEST);
        wrapper.add(header, BorderLayout.NORTH);
        
        // Grid
        gridPanel = new JPanel(new utils.WrapLayout(FlowLayout.LEFT, 25, 25));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(10, 25, 25, 25));
        
        gridPanel.add(createTopicCard("Hướng dẫn sử dụng", FontAwesome.BOOK, "Các thao tác bán hàng, đặt bàn...",
            "<h2>Hướng dẫn sử dụng hệ thống</h2>" +
            "<ul>" +
            "<li><b>Bán hàng:</b> Chọn bàn -> Thêm món -> Thanh toán.</li>" +
            "<li><b>Đặt bàn:</b> Nhập thông tin khách, chọn thời gian đến, chọn bàn. Hệ thống sẽ giữ bàn.</li>" +
            "<li><b>Quản lý:</b> Quản lý có thể thêm/sửa/xoá món ăn, danh mục, cấu hình hệ thống.</li>" +
            "</ul>" +
            "<p><i>Lưu ý: Mọi thao tác đều được hệ thống ghi log để đối soát.</i></p>"
        ));
        
        gridPanel.add(createTopicCard("FAQ", FontAwesome.QUESTION_CIRCLE, "Các câu hỏi thường gặp",
            "<h2>Các câu hỏi thường gặp (FAQ)</h2>" +
            "<p><b>Q: Khách huỷ bàn thì làm sao?</b><br/>" +
            "A: Vào mục Đặt bàn, chọn phiếu đặt bàn đó và nhấn nút Huỷ.</p>" +
            "<p><b>Q: In hoá đơn bị lỗi?</b><br/>" +
            "A: Kiểm tra lại máy in đã bật chưa, giấy in còn không. Khởi động lại phần mềm nếu cần.</p>" +
            "<p><b>Q: Đổi mật khẩu ở đâu?</b><br/>" +
            "A: Bấm vào Tên của bạn ở góc dưới cùng bên trái thanh Menu -> Chọn Hồ sơ -> Đổi mật khẩu.</p>"
        ));
        
        gridPanel.add(createTopicCard("Phím tắt", FontAwesome.KEYBOARD_O, "Thao tác nhanh trên bàn phím",
            "<h2>Danh sách Phím tắt</h2>" +
            "<table border='1' cellpadding='5' style='border-collapse: collapse;'>" +
            "<tr><th>Phím tắt</th><th>Chức năng</th></tr>" +
            "<tr><td>F1</td><td>Mở màn hình Trợ giúp này</td></tr>" +
            "<tr><td>F2</td><td>Chuyển nhanh qua màn hình Bán hàng</td></tr>" +
            "<tr><td>F5</td><td>Làm mới dữ liệu (Refresh)</td></tr>" +
            "<tr><td>Esc</td><td>Đóng hộp thoại hiện tại</td></tr>" +
            "</table>"
        ));
        
        gridPanel.add(createTopicCard("Quy định nghiệp vụ", FontAwesome.GAVEL, "Các quy định của quán",
            "<h2>Quy định nghiệp vụ cửa hàng</h2>" +
            "<ul>" +
            "<li>Nhân viên phải chốt ca trước khi đăng xuất.</li>" +
            "<li>Tiền thối lại cho khách phải chính xác. Tiền hao hụt cuối ngày nhân viên tự bù.</li>" +
            "<li>Giữ bàn cho khách đặt trước tối đa 15 phút. Quá giờ tự động huỷ.</li>" +
            "</ul>"
        ));
        
        gridPanel.add(createTopicCard("Liên hệ hỗ trợ", FontAwesome.PHONE, "Số điện thoại, Email kỹ thuật",
            "<h2>Liên hệ bộ phận Kỹ thuật / Quản lý</h2>" +
            "<p>Nếu hệ thống gặp sự cố nghiêm trọng không thể tự khắc phục, vui lòng liên hệ:</p>" +
            "<ul>" +
            "<li><b>Hotline Kỹ thuật:</b> 0909 123 456 (Mr. A)</li>" +
            "<li><b>Quản lý cửa hàng:</b> 0909 987 654 (Ms. B)</li>" +
            "<li><b>Email:</b> hotro@coffee1101.com</li>" +
            "</ul>"
        ));
        
        gridPanel.add(createTopicCard("Về ứng dụng", FontAwesome.INFO_CIRCLE, "Phiên bản và bản quyền",
            "<h2>Hệ thống Quản lý COFFEE 11:01</h2>" +
            "<p><b>Phiên bản:</b> 1.0.0 (Bản phát hành ổn định)</p>" +
            "<p><b>Phát triển bởi:</b> Nhóm PTUD</p>" +
            "<p>Bản quyền © 2026 COFFEE 11:01. Mọi quyền được bảo lưu.</p>"
        ));
        
        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        wrapper.add(scroll, BorderLayout.CENTER);
        mainContainer.add(wrapper, "GRID");
    }

    private void initDetailView() {
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setOpaque(false);
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 30, 20, 30));

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlTitle.setOpaque(false);
        
        JButton btnBack = new JButton("⬅ Quay lại");
        btnBack.setFont(new Font("Roboto", Font.BOLD, 14));
        btnBack.putClientProperty("JButton.buttonArc", 10);
        btnBack.putClientProperty("JButton.margin", new java.awt.Insets(8, 15, 8, 15));
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "GRID"));
        
        JLabel lblDivider = new JLabel("|");
        lblDivider.setFont(new Font("Roboto", Font.PLAIN, 28));
        lblDivider.setForeground(new Color(200, 200, 200));

        lblDetailTitle = new JLabel("Chi Tiết");
        lblDetailTitle.setFont(new Font("Roboto", Font.BOLD, 28));
        lblDetailTitle.setForeground(new Color(113, 76, 52));
        
        pnlTitle.add(btnBack);
        pnlTitle.add(lblDivider);
        pnlTitle.add(lblDetailTitle);
        
        header.add(pnlTitle, BorderLayout.WEST);
        
        detailPanel.add(header, BorderLayout.NORTH);
        
        // Content
        htmlContentPane = new JEditorPane();
        htmlContentPane.setContentType("text/html");
        htmlContentPane.setEditable(false);
        htmlContentPane.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JScrollPane scrollPane = new JScrollPane(htmlContentPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(false);
        
        detailPanel.add(scrollPane, BorderLayout.CENTER);
        mainContainer.add(detailPanel, "DETAIL");
    }

    private JPanel createTopicCard(String title, FontAwesome icon, String desc, String htmlContent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(300, 150));
        setCardStyleWithPadding(card, 20, "#e8e8e8", 20, 20);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel pnlTop = new JPanel(new BorderLayout(15, 0));
        pnlTop.setOpaque(false);
        
        JLabel lblIcon = new JLabel(IconFontSwing.buildIcon(icon, 32, new Color(59, 130, 246)));
        
        JLabel lblName = new JLabel(title);
        lblName.setFont(new Font("Roboto", Font.BOLD, 20));
        lblName.setForeground(new Color(26, 26, 26));
        
        pnlTop.add(lblIcon, BorderLayout.WEST);
        pnlTop.add(lblName, BorderLayout.CENTER);

        JLabel lblDesc = new JLabel("<html><div style='width: 240px;'>" + desc + "</div></html>");
        lblDesc.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblDesc.setForeground(new Color(150, 150, 150));
        lblDesc.setBorder(new EmptyBorder(15, 5, 0, 0));
        
        card.add(pnlTop, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);
        
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setCardStyleWithPadding(card, 20, "#714c34", 20, 20);
                card.setBackground(new Color(252, 250, 248));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setCardStyleWithPadding(card, 20, "#e8e8e8", 20, 20);
                card.setBackground(Color.WHITE);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                lblDetailTitle.setText(title);
                htmlContentPane.setText("<html><body style='font-family: Arial, sans-serif; font-size: 15px; color: #333; line-height: 1.6;'>" + htmlContent + "</body></html>");
                htmlContentPane.setCaretPosition(0); // Scroll to top
                cardLayout.show(mainContainer, "DETAIL");
            }
        });
        
        return card;
    }

    private void setCardStyleWithPadding(JPanel p, int arc, String hexColor, int padV, int padH) {
        p.putClientProperty("JComponent.arc", arc);
        p.setBorder(BorderFactory.createLineBorder(Color.decode(hexColor), 1));
        p.setBorder(BorderFactory.createCompoundBorder(
            p.getBorder(),
            new EmptyBorder(padV, padH, padV, padH)
        ));
    }
}
