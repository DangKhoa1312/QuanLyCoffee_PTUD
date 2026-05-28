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
        
        gridPanel.add(createTopicCard("Hướng dẫn sử dụng", FontAwesome.BOOK, "Các thao tác toàn hệ thống...",
            "<h2>Hướng dẫn sử dụng hệ thống toàn diện</h2>" +
            "<h3>1. Đăng nhập</h3>" +
            "<ul>" +
            "<li><b>Đăng nhập:</b> Sử dụng tên đăng nhập và mật khẩu được cấp bởi quản lý.</li>" +
            "<li><b>Đăng xuất:</b> Bấm vào góc phải trên cùng có nút đăng xuất, chọn <i>Đăng xuất</i>.</li>" +
            "</ul>" +
            "<h3>2. Tổng quan (Dashboard)</h3>" +
            "<ul>" +
            "<li>Xem nhanh các chỉ số kinh doanh trong ngày: Doanh thu, số lượng đơn hàng, số bàn đang phục vụ.</li>" +
            "</ul>" +
            "<h3>3. Nhóm Vận Hành</h3>" +
            "<ul>" +
            "<li><b>Bán Hàng:</b> Hỗ trợ đầy đủ các nghiệp vụ tại bàn:" +
            "   <ul>" +
            "   <li><i>Gọi món mới:</i> Chọn Khu vực -> Chọn Bàn trống -> Chọn món -> Thêm ghi chú/tuỳ chỉnh Size, Topping -> Bấm <i>Gọi món</i> hoặc <i>Thanh toán</i>.</li>" +
            "   <li><i>Gọi thêm món:</i> Chọn bàn khách đang phục vụ -> Chọn các món gọi thêm -> Bấm <i>Gọi món</i> để báo bếp chế biến.</li>" +
            "   <li><i>Chuyển bàn:</i> Chọn bàn đang phục vụ -> Chọn <i>Chuyển bàn</i> -> Chọn khu vực và bàn khách muốn chuyển đến.</li>" +
            "   <li><i>Ghép bàn:</i> Chọn bàn đang phục vụ -> Chọn <i>Ghép bàn</i> -> Chọn khu vực và bàn khách muốn gộp chung.</li>" +
            "   <li><i>Tách món:</i> Chọn bàn cần xử lý -> Chọn <i>Tách món</i> -> Chọn món và số lượng cần tách -> Chọn khu vực và bàn cần chuyển đến.</li>" +
            "   <li><i>Xoá món:</i> (Chỉ áp dụng cho món chưa báo bếp) Chọn món trong đơn hàng -> Nhấn <i>Xoá món</i>.</li>" +
            "   <li><i>Xử lý sự cố sai món/khách không nhận:</i> Dùng <i>Tách món</i> đưa các món sai sang 1 bàn riêng -> Sang bàn đó chọn <i>Huỷ đơn</i>. Các món này sẽ được đẩy vào <b>Kho Lưu Tạm (Bàn Ma)</b> để nhân viên có thể dễ dàng lấy ra bán lại nếu có khách gọi.</li>" +
            "   </ul>" +
            "</li>" +
            "<li><b>Đặt Bàn:</b> Nhấn <i>+ Tạo Đặt Bàn Mới</i>, nhập thông tin khách, chọn thời gian đến và chọn bàn phù hợp. Hệ thống sẽ giữ bàn (màu vàng). Khi khách hàng đến, chọn phiếu đặt bàn đó và chọn <i>Khách đã tới</i>, sau đó chọn <i>Mở bàn</i> để bắt đầu thực hiện order cho khách.</li>" +
            "<li><b>Hoá Đơn:</b> Tra cứu lịch sử hoá đơn đã thanh toán. Chọn vào 1 hoá đơn cần xem, hệ thống hiển thị chi tiết hoá đơn và có thể chọn <i>In hoá đơn</i> khi cần xuất hoá đơn lại cho đơn hàng đó.</li>" +
            "</ul>" +
            "<h3>4. Nhóm Thiết Lập</h3>" +
            "<ul>" +
            "<li><b>Món & Size / Topping:</b>" +
            "   <ul>" +
            "   <li><i>Tìm kiếm & Lọc:</i> Có thể tìm kiếm món, lọc theo danh mục món (Coffee, tea,...), trạng thái (đang bán/tạm ngưng).</li>" +
            "   <li><i>Thêm mới:</i> Chọn nút <i>Thêm món mới</i> -> thiết lập tên món, loại món, thêm ảnh, mô tả và các size của món đó -> Chọn <i>Lưu món</i> để cập nhật.</li>" +
            "   <li><i>Tuỳ chỉnh:</i> Chọn món cần điều chỉnh -> chọn biểu tượng bút chì -> Hiển thị thông tin chi tiết món để điều chỉnh -> Bấm <i>Lưu</i> để cập nhật lại món sau khi thay đổi.</li>" +
            "   <li><i>Tạm ngưng:</i> Chọn món cần điều chỉnh -> chọn biểu tượng công tắc -> Xác nhận để có thể tạm ngưng bán món đó.</li>" +
            "   <li><i>Lưu ý:</i> Thao tác cho Topping hoàn toàn tương tự như quản lý Món & Size.</li>" +
            "   </ul>" +
            "</li>" +
            "<li><b>Công Thức:</b> Định lượng nguyên vật liệu cho từng món uống." +
            "   <ul>" +
            "   <li><i>Tìm kiếm:</i> Tìm kiếm món cần điều chỉnh hoặc thiết lập công thức mới.</li>" +
            "   <li><i>Thiết lập nguyên liệu:</i> Chọn món cần xét nguyên liệu -> Chọn nguyên liệu cấu thành từ kho -> Nhập khối lượng -> Chọn <i>Thêm nguyên liệu</i>.</li>" +
            "   <li><i>Điều chỉnh định mức:</i> Chọn nguyên liệu của món cần điều chỉnh -> Tuỳ chỉnh lại định mức -> Nhấn <i>Cập nhật</i> để lưu lại thay đổi.</li>" +
            "   <li><i>Xoá nguyên liệu:</i> Chọn nguyên liệu đó trong công thức và nhấn <i>Xoá nguyên liệu</i>.</li>" +
            "   </ul>" +
            "</li>" +
            "<li><b>Bảng Giá:</b> Cài đặt giá bán sản phẩm." +
            "   <ul>" +
            "   <li><i>Cập nhật giá:</i> Chọn bảng giá cần thay đổi -> Có thể sao chép bảng giá gốc để tăng hoặc giảm giá đồng loạt các món thuận tiện.</li>" +
            "   <li>Cách dùng: Chọn Tạo bảng giá mới -> Chọn Copy từ bảng giá cũ -> Nhập % muốn tăng hoặc giảm -> Lưu.</li>" +
            "   </ul>" +
            "</li>" +
            "<li><b>Sơ Đồ Bàn:</b> Tổ chức không gian quán." +
            "   <ul>" +
            "   <li><i>Tạo khu vực:</i> Tạo các khu vực và mô tả khu vực ấy bằng cách: Chọn <i>Tạo khu vực mới</i> -> Nhập tên khu vực và mô tả -> Nhấn <i>Tạo</i> để xuất hiện khu vực mới.</li>" +
            "   <li><i>Quản lý khu vực & Bàn:</i> Trong khu vực có thể tạo nhiều bàn, cập nhật lại khu vực, hoặc tạm ngưng hoạt động khu vực đó.</li>" +
            "   </ul>" +
            "</li>" +
            "<li><b>Cấu Hình:</b> Thông số chung hệ thống." +
            "   <ul>" +
            "   <li><i>Thông tin bill:</i> Cập nhật Tên quán, Địa chỉ, SĐT, Mật khẩu Wifi để tự động in lên hoá đơn.</li>" +
            "   <li><i>Chính sách:</i> Cài đặt mức thuế VAT, cấu hình tỷ lệ quy đổi điểm tích luỹ thành viên.</li>" +
            "   </ul>" +
            "</li>" +
            "</ul>" +
            "<h3>5. Nhóm Quản Trị</h3>" +
            "<ul>" +
            "<li><b>Nhân Viên:</b> Cấp tài khoản, phân quyền (Nhân viên / Quản lý) và đặt lại mật khẩu.</li>" +
            "<li><b>Khách Hàng:</b> Quản lý danh sách thành viên, xem điểm tích luỹ và lịch sử mua hàng.</li>" +
            "<li><b>Khuyến Mãi:</b> Tạo mã giảm giá (Voucher), cài đặt thời hạn và điều kiện tối thiểu.</li>" +
            "<li><b>Kho Hàng:</b> Quản lý nhập/xuất nguyên vật liệu, theo dõi lượng tồn kho hiện tại.</li>" +
            "<li><b>Thống Kê:</b> Biểu đồ phân tích doanh thu, lợi nhuận và thống kê top các món ăn bán chạy nhất.</li>" +
            "</ul>"
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
            "<li>Nhân viên phải thanh toán hết các đơn hàng trước khi đăng xuất.</li>" +
            "<li>Tiền thối lại cho khách phải chính xác. Tiền hao hụt cuối ngày nhân viên tự bù tiền.</li>" +
            "<li>Những ly nước cuối ca còn lại trong bàn ma sẽ do nhân viên order ngày hôm đó chịu trách nhiệm và tự bù tiền.</li>" +
            "<li>Giữ bàn cho khách đặt trước tối đa 15 phút. Quá giờ tự động huỷ.</li>" +
            "</ul>"
        ));
        
        gridPanel.add(createTopicCard("Liên hệ hỗ trợ", FontAwesome.PHONE, "Số điện thoại, Email kỹ thuật",
            "<h2>Liên hệ bộ phận Kỹ thuật / Quản lý</h2>" +
            "<p>Nếu hệ thống gặp sự cố nghiêm trọng không thể tự khắc phục, vui lòng liên hệ:</p>" +
            "<ul>" +
            "<li><b>Hotline Kỹ thuật:</b> 0909 123 456 (Mr. Khoa)</li>" +
            "<li><b>Quản lý cửa hàng:</b> 0909 987 654 (Ms. Hùng)</li>" +
            "<li><b>Email:</b> hotro@coffee1101.com</li>" +
            "</ul>"
        ));
        
        gridPanel.add(createTopicCard("Về ứng dụng", FontAwesome.INFO_CIRCLE, "Phiên bản và bản quyền",
            "<h2>Hệ thống Quản lý COFFEE 11:01</h2>" +
            "<p><b>Phiên bản:</b> 1.0.0 (Bản phát hành ổn định)</p>" +
            "<p><b>Phát triển bởi:</b> Công ty trách nhiệm hữu hạn 5 anh em</p>" +
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
