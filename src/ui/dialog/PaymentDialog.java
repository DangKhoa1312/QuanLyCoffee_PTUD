package ui.dialog;

import controller.PaymentController;
import dto.CartItem;
import entity.DonHang;
import entity.HoaDon;
import enums.HinhThucThanhToan;
import exception.AppException;
import utils.OrderManager;
import utils.PDFPrinter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Giao diện Thanh toán cho một Đơn hàng.
 * Hỗ trợ chọn hình thức (Tiền mặt/Chuyển khoản), nhập tiền khách đưa, tính tiền thừa.
 */
public class PaymentDialog extends JDialog {

    private final DonHang donHang;
    private final PaymentController paymentController;
    private boolean isPaid = false;

    private JRadioButton rbTienMat;
    private JRadioButton rbChuyenKhoan;
    private JTextField txtKhachDua;
    private JLabel lblTienThua;
    private JLabel lblTongPhaiTra;

    private double tongTienDon; // t\u1EEB DonHang
    private double tongPhaiTra;
    
    private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    public PaymentDialog(JFrame parent, DonHang donHang) {
        super(parent, "Thanh To\u00E1n \u0110\u01A1n H\u00E0ng", true);
        this.donHang = donHang;
        this.paymentController = new PaymentController();
        this.tongTienDon = donHang.getTongTienTamTinh();
        this.tongPhaiTra = tongTienDon;

        setSize(700, 450);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
        updateTienThua();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 247, 250));
        main.setBorder(new EmptyBorder(15, 20, 15, 20));

        // ── Header ──
        JLabel lblTitle = new JLabel("Thanh To\u00E1n", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        main.add(lblTitle, BorderLayout.NORTH);

        // ── Body Split ──
        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 0, 20, 0));

        // 1. Cột trái: Tóm tắt đơn hàng
        JPanel pnlBill = new JPanel();
        pnlBill.setLayout(new BoxLayout(pnlBill, BoxLayout.Y_AXIS));
        pnlBill.setBackground(Color.WHITE);
        pnlBill.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Th\u00F4ng tin h\u00F3a \u0111\u01A1n", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Roboto", Font.BOLD, 14), new Color(100, 100, 100)
        ));

        pnlBill.add(Box.createVerticalStrut(15));
        addDetailRow(pnlBill, "M\u00E3 \u0111\u01A1n:", donHang.getMaDonHang());
        addDetailRow(pnlBill, "B\u00E0n:", donHang.getMaBan() == null ? "MANG V\u1EC0" : donHang.getMaBan());
        pnlBill.add(Box.createVerticalStrut(15));
        
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        pnlBill.add(sep);
        pnlBill.add(Box.createVerticalStrut(15));

        addDetailRow(pnlBill, "T\u1ED5ng ti\u1EC1n m\u00F3n:", nf.format(tongTienDon) + " \u0111");
        addDetailRow(pnlBill, "Khuy\u1EBFn m\u00E3i (0%):", "-0 \u0111");
        addDetailRow(pnlBill, "Thu\u1EBF (0%):", "+0 \u0111");
        
        pnlBill.add(Box.createVerticalStrut(20));
        pnlBill.add(Box.createVerticalGlue());

        JPanel pnlTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlTotal.setOpaque(false);
        lblTongPhaiTra = new JLabel("C\u1EA7n TT: " + nf.format(tongPhaiTra) + " \u0111");
        lblTongPhaiTra.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTongPhaiTra.setForeground(new Color(231, 76, 60));
        pnlTotal.add(lblTongPhaiTra);
        pnlBill.add(pnlTotal);
        pnlBill.add(Box.createVerticalStrut(10));

        body.add(pnlBill);

        // 2. Cột phải: Thanh toán
        JPanel pnlPay = new JPanel();
        pnlPay.setLayout(new BoxLayout(pnlPay, BoxLayout.Y_AXIS));
        pnlPay.setBackground(Color.WHITE);
        pnlPay.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Ti\u1EC1n kh\u00E1ch tr\u1EA3", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Roboto", Font.BOLD, 14), new Color(100, 100, 100)
        ));

        // Hình thức
        JPanel pnlMethod = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlMethod.setOpaque(false);
        rbTienMat = new JRadioButton("Ti\u1EC1n M\u1EB7t");
        rbChuyenKhoan = new JRadioButton("Chuy\u1EC3n Kho\u1EA3n \uD83D\uDCF1");
        rbTienMat.setFont(new Font("Roboto", Font.BOLD, 13));
        rbChuyenKhoan.setFont(new Font("Roboto", Font.BOLD, 13));
        rbTienMat.setOpaque(false);
        rbChuyenKhoan.setOpaque(false);
        rbTienMat.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(rbTienMat);
        group.add(rbChuyenKhoan);
        pnlMethod.add(rbTienMat);
        pnlMethod.add(rbChuyenKhoan);

        // Listeners toggle input tien mat
        rbTienMat.addActionListener(e -> {
            txtKhachDua.setEnabled(true);
            updateTienThua();
        });
        rbChuyenKhoan.addActionListener(e -> {
            txtKhachDua.setText(nf.format(tongPhaiTra));
            txtKhachDua.setEnabled(false);
            updateTienThua();
        });

        // Input
        JPanel pnlInput = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlInput.setOpaque(false);
        pnlInput.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel lblKD = new JLabel("S\u1ED1 ti\u1EC1n kh\u00E1ch \u0111\u01B0a (VN\u0110):");
        lblKD.setFont(new Font("Roboto", Font.PLAIN, 14));
        pnlInput.add(lblKD);

        txtKhachDua = new JTextField(nf.format(tongPhaiTra));
        txtKhachDua.setFont(new Font("Roboto", Font.BOLD, 22));
        txtKhachDua.setHorizontalAlignment(JTextField.RIGHT);
        
        // Auto update tiền thừa
        txtKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateTienThua(); }
            public void removeUpdate(DocumentEvent e) { updateTienThua(); }
            public void changedUpdate(DocumentEvent e) { updateTienThua(); }
        });
        pnlInput.add(txtKhachDua);

        // Tiền thừa label
        JPanel pnlThua = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlThua.setOpaque(false);
        lblTienThua = new JLabel("Ti\u1EC1n th\u1EEBa tr\u1EA3 kh\u00E1ch: 0 \u0111");
        lblTienThua.setFont(new Font("Roboto", Font.BOLD, 16));
        lblTienThua.setForeground(new Color(39, 174, 96));
        pnlThua.add(lblTienThua);

        pnlPay.add(Box.createVerticalStrut(10));
        pnlPay.add(pnlMethod);
        pnlPay.add(pnlInput);
        pnlPay.add(pnlThua);

        body.add(pnlPay);
        main.add(body, BorderLayout.CENTER);

        // ── Bot: Buttons ──
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bot.setOpaque(false);

        JButton btnHuy = new JButton("H\u1EE6Y");
        btnHuy.setFont(new Font("Roboto", Font.BOLD, 14));
        btnHuy.setPreferredSize(new Dimension(100, 42));
        btnHuy.setFocusable(false);
        btnHuy.addActionListener(e -> dispose());

        JButton btnPay = new JButton("X\u00C1C NH\u1EACN THANH TO\u00C1N");
        btnPay.setFont(new Font("Roboto", Font.BOLD, 15));
        btnPay.setBackground(new Color(39, 174, 96));
        btnPay.setForeground(Color.WHITE);
        btnPay.setPreferredSize(new Dimension(220, 42));
        btnPay.setFocusable(false);
        btnPay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPay.addActionListener(e -> handleThanhToan());

        bot.add(btnHuy);
        bot.add(btnPay);
        main.add(bot, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void addDetailRow(JPanel p, String title, String val) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 15, 0, 15));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Roboto", Font.PLAIN, 14));
        t.setForeground(new Color(80, 80, 80));
        
        JLabel v = new JLabel(val);
        v.setFont(new Font("Roboto", Font.BOLD, 14));
        v.setForeground(new Color(44, 62, 80));

        row.add(t, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        p.add(row);
    }

    private void updateTienThua() {
        if (!rbTienMat.isSelected()) {
            lblTienThua.setText("Ti\u1EC1n th\u1EEBa tr\u1EA3 kh\u00E1ch: 0 \u0111");
            return;
        }

        try {
            String raw = txtKhachDua.getText().trim().replace(".", "").replace(",", "");
            if (raw.isEmpty()) raw = "0";
            double kd = Double.parseDouble(raw);
            double thua = kd - tongPhaiTra;
            
            if (thua < 0) {
                lblTienThua.setText("Kh\u00E1ch \u0111\u01B0a thi\u1EBFu!");
                lblTienThua.setForeground(new Color(231, 76, 60));
            } else {
                lblTienThua.setText("Ti\u1EC1n th\u1EEBa: " + nf.format(thua) + " \u0111");
                lblTienThua.setForeground(new Color(39, 174, 96));
            }
        } catch (NumberFormatException ignored) {
            lblTienThua.setText("S\u1ED1 ti\u1EC1n kh\u00F4ng h\u1EE3p l\u1EC7");
            lblTienThua.setForeground(Color.GRAY);
        }
    }

    private void handleThanhToan() {
        try {
            String raw = txtKhachDua.getText().trim().replace(".", "").replace(",", "");
            double kd = Double.parseDouble(raw);
            if (kd < tongPhaiTra) {
                JOptionPane.showMessageDialog(this, "S\u1ED1 ti\u1EC1n kh\u00E1ch \u0111\u01B0a ch\u01B0a \u0111\u1EE7!", "L\u1ED7i", JOptionPane.WARNING_MESSAGE);
                return;
            }

            HinhThucThanhToan ht = rbTienMat.isSelected() ? HinhThucThanhToan.TIEN_MAT : HinhThucThanhToan.CHUYEN_KHOAN;
            
            List<CartItem> cart = OrderManager.getInstance().getCart(donHang.getMaDonHang());

            HoaDon hd = paymentController.thanhToan(donHang, cart, tongPhaiTra, ht);
            isPaid = true;

            JOptionPane.showMessageDialog(this, "\u0110\u00E3 thanh to\u00E1n th\u00E0nh c\u00F4ng m\u00E3 " + hd.getMaHD() + "!", "Th\u00E0nh C\u00F4ng", JOptionPane.INFORMATION_MESSAGE);
            dispose();

            int inBill = JOptionPane.showConfirmDialog(this.getParent(), "B\u1EA1n c\u00F3 mu\u1ED1n In H\u00F3a \u0110\u01A1n kh\u00F4ng?", "In Bill", JOptionPane.YES_NO_OPTION);
            if (inBill == JOptionPane.YES_OPTION) {
                try {
                    String pdfPath = PDFPrinter.exportBill(hd, cart);
                    Desktop.getDesktop().open(new File(pdfPath));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this.getParent(), "L\u1ED7i in H\u00F3a \u0111\u01A1n: " + ex.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "S\u1ED1 ti\u1EC1n kh\u00F4ng h\u1EE3p l\u1EC7!", "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
        } catch (AppException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "L\u1ED7i Thanh To\u00E1n", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isPaid() {
        return isPaid;
    }
}
