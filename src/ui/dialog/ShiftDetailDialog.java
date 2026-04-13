package ui.dialog;

import entity.CaLamViec;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Dialog hiển thị chi tiết ca làm việc khi double-click vào bảng.
 */
public class ShiftDetailDialog extends JDialog {

    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ShiftDetailDialog(Frame owner, CaLamViec ca, NhanVien nv) {
        super(owner, "Chi tiết ca làm việc", true);
        setSize(500, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        initUI(ca, nv);
    }

    private void initUI(CaLamViec ca, NhanVien nv) {
        JPanel mainPnl = new JPanel();
        mainPnl.setLayout(new BoxLayout(mainPnl, BoxLayout.Y_AXIS));
        mainPnl.setBorder(new EmptyBorder(25, 30, 25, 30));
        mainPnl.setBackground(Color.WHITE);

        // 1. Header
        JLabel lblTitle = new JLabel("THÔNG TIN CHI TIẾT");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(26, 26, 46));
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);
        mainPnl.add(lblTitle);
        mainPnl.add(Box.createVerticalStrut(25));

        // 2. Section: Nhân viên
        mainPnl.add(createSectionHeader("Thông tin nhân viên"));
        mainPnl.add(createInfoRow("Mã nhân viên:", nv.getMaNV()));
        mainPnl.add(createInfoRow("Họ và tên:", nv.getTenNV()));
        mainPnl.add(createInfoRow("Số điện thoại:", nv.getSoDienThoai()));
        mainPnl.add(createInfoRow("Vai trò:", nv.getVaiTro().name()));
        mainPnl.add(Box.createVerticalStrut(20));

        // 3. Section: Ca làm việc
        mainPnl.add(createSectionHeader("Thông tin ca làm"));
        mainPnl.add(createInfoRow("Mã ca:", ca.getMaCa()));
        mainPnl.add(createInfoRow("Ngày làm:", ca.getNgayLam().format(dateFormatter)));
        
        String caLoai = xacDinhCaLam(ca.getGioBatDau());
        mainPnl.add(createInfoRow("Loại ca:", caLoai));
        
        mainPnl.add(createInfoRow("Giờ vào làm:", ca.getGioBatDau().format(timeFormatter)));
        mainPnl.add(createInfoRow("Giờ ra khỏi ca:", ca.getGioKetThuc() != null ? ca.getGioKetThuc().format(timeFormatter) : "--:--:--"));
        mainPnl.add(Box.createVerticalStrut(20));

        // 4. Section: Tài chính
        mainPnl.add(createSectionHeader("Doanh thu"));
        JLabel lblRevenue = new JLabel(nf.format(ca.getTongDoanhThu()) + " VNĐ");
        lblRevenue.setFont(new Font("Roboto", Font.BOLD, 20));
        lblRevenue.setForeground(new Color(39, 174, 96));
        lblRevenue.setAlignmentX(CENTER_ALIGNMENT);
        mainPnl.add(lblRevenue);

        add(new JScrollPane(mainPnl), BorderLayout.CENTER);

        // Footer
        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Roboto", Font.BOLD, 14));
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addActionListener(e -> dispose());
        
        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSouth.setBorder(new EmptyBorder(10, 10, 10, 30));
        pnlSouth.add(btnClose);
        add(pnlSouth, BorderLayout.SOUTH);
    }

    private JPanel createSectionHeader(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(500, 30));
        
        JLabel lbl = new JLabel(title.toUpperCase());
        lbl.setFont(new Font("Roboto", Font.BOLD, 13));
        lbl.setForeground(new Color(100, 116, 139));
        p.add(lbl, BorderLayout.WEST);
        
        JSeparator sep = new JSeparator();
        p.add(sep, BorderLayout.SOUTH);
        
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(0, 0, 10, 0));
        outer.add(p);
        return outer;
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(500, 25));
        
        JLabel lblL = new JLabel(label);
        lblL.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblL.setForeground(new Color(71, 85, 105));
        
        JLabel lblR = new JLabel(value);
        lblR.setFont(new Font("Roboto", Font.BOLD, 14));
        lblR.setForeground(new Color(15, 23, 42));
        
        p.add(lblL, BorderLayout.WEST);
        p.add(lblR, BorderLayout.EAST);
        return p;
    }

    private String xacDinhCaLam(java.time.LocalTime start) {
        java.time.LocalTime afternoonThreshold = java.time.LocalTime.of(14, 31);
        if (start.isBefore(afternoonThreshold)) {
            return "Ca sáng";
        } else {
            return "Ca chiều";
        }
    }
}
