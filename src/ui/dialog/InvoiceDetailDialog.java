package ui.dialog;

import controller.InvoiceController;
import dto.CartItem;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import utils.PDFPrinter;

/**
 * Dialog hiển thị chi tiết của một Hóa Đơn đã thanh toán.
 */
public class InvoiceDetailDialog extends JDialog {

    private final HoaDon hoaDon;
    private final InvoiceController invoiceController;
    
    private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public InvoiceDetailDialog(JFrame parent, HoaDon hoaDon) {
        super(parent, "Chi Ti\u1EBFt H\u00F3a \u0110\u01A1n", true);
        this.hoaDon = hoaDon;
        this.invoiceController = new InvoiceController();

        setSize(550, 600);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ── Header: Thông tin Hóa Đơn ──
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("H\u00D3A \u0110\u01A1N THANH TO\u00C1N", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblTitle);
        pnlHeader.add(Box.createVerticalStrut(15));

        String time = hoaDon.getThoiGianThanhToan() != null 
                    ? hoaDon.getThoiGianThanhToan().format(dtf) 
                    : "";

        pnlHeader.add(createDetailRow("M\u00E3 h\u00F3a \u0111\u01A1n:", hoaDon.getMaHD()));
        pnlHeader.add(createDetailRow("B\u00E0n:", hoaDon.getMaBan() != null ? hoaDon.getMaBan() : "Mang v\u1EC1"));
        pnlHeader.add(createDetailRow("Lo\u1EA1i \u0111\u01A1n:", hoaDon.getLoaiDon() != null ? hoaDon.getLoaiDon().name() : "N/A"));
        pnlHeader.add(createDetailRow("Th\u1EDDi gian:", time));
        pnlHeader.add(createDetailRow("Thu ng\u00E2n:", hoaDon.getMaNV() != null ? hoaDon.getMaNV() : "N/A"));
        
        main.add(pnlHeader, BorderLayout.NORTH);

        // ── Center: Danh sách Món ──
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(15, 0, 15, 0));

        String[] cols = {"T\u00EAn M\u00F3n", "SL", "Gi\u00E1", "T\u1ED5ng"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Roboto", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));
        
        table.getColumnModel().getColumn(0).setPreferredWidth(210);
        table.getColumnModel().getColumn(1).setPreferredWidth(45);
        table.getColumnModel().getColumn(2).setPreferredWidth(85);
        table.getColumnModel().getColumn(3).setPreferredWidth(95);

        // Căn lề cho cột SL (Center), Giá & Tổng (Right)
        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        rightRenderer.setVerticalAlignment(SwingConstants.TOP);
        
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setVerticalAlignment(SwingConstants.TOP);

        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        // Lấy chi tiết đơn hàng
        List<CartItem> items = invoiceController.getChiTietHoaDon(hoaDon.getMaHD());
        for (CartItem item : items) {
            int totalSubRows = 1 + item.getToppings().size() + (item.getGhiChu().isEmpty() ? 0 : 1);
            
            // --- Khởi tạo 3 luồng HTML đồng bộ ---
            StringBuilder nameHtml = new StringBuilder("<html><table width='100%' border='0' cellspacing='0' cellpadding='1'>");
            StringBuilder priceHtml = new StringBuilder("<html><table width='100%' border='0' cellspacing='0' cellpadding='1'>");
            StringBuilder amountHtml = new StringBuilder("<html><table width='100%' border='0' cellspacing='0' cellpadding='1'>");

            // 1. Dòng chính (Món + Size)
            String mainName = "<b>" + item.getMon().getTenMon() + "</b> (" + item.getSize().getTenSize() + ")";
            nameHtml.append("<tr><td style='font-family:Roboto; font-size:12px;'>").append(mainName).append("</td></tr>");
            priceHtml.append("<tr><td align='right' style='font-family:Roboto; font-size:12px;'>").append(nf.format(item.getDonGiaSize())).append("đ</td></tr>");
            amountHtml.append("<tr><td align='right' style='font-family:Roboto; font-size:12px;'>").append(nf.format(item.getDonGiaSize() * item.getSoLuong())).append("đ</td></tr>");

            // 2. Các dòng Topping
            for (dto.CartItem.CartTopping ctx : item.getToppings()) {
                String topName = "&nbsp;&nbsp;&nbsp;+ " + ctx.topping.getTenTopping() + " (x" + ctx.soLuong + ")";
                nameHtml.append("<tr><td style='font-family:Roboto; font-size:10px; color:gray;'>").append(topName).append("</td></tr>");
                
                String topPrice = "+ " + nf.format(ctx.giaTopping * ctx.soLuong) + "đ";
                priceHtml.append("<tr><td align='right' style='font-family:Roboto; font-size:10px; color:gray;'>").append(topPrice).append("</td></tr>");
                
                double topTotal = ctx.giaTopping * ctx.soLuong * item.getSoLuong();
                amountHtml.append("<tr><td align='right' style='font-family:Roboto; font-size:10px; color:gray;'>+ ").append(nf.format(topTotal)).append("đ</td></tr>");
            }

            // 3. Dòng Ghi chú (nếu có)
            if (!item.getGhiChu().isEmpty()) {
                nameHtml.append("<tr><td style='font-family:Roboto; font-size:10px; color:orange;'>(").append(item.getGhiChu()).append(")</td></tr>");
                priceHtml.append("<tr><td>&nbsp;</td></tr>");
                amountHtml.append("<tr><td>&nbsp;</td></tr>");
            }

            nameHtml.append("</table></html>");
            priceHtml.append("</table></html>");
            amountHtml.append("</table></html>");

            String slHtml = "<html><div style='padding-top:2px;'>" + item.getSoLuong() + "</div></html>";

            model.addRow(new Object[]{
                nameHtml.toString(),
                slHtml,
                priceHtml.toString(),
                amountHtml.toString()
            });

            table.setRowHeight(model.getRowCount() - 1, totalSubRows * 18 + 15);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        pnlCenter.add(scroll, BorderLayout.CENTER);

        // Tính tổng
        JPanel pnlTotal = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlTotal.setOpaque(false);
        pnlTotal.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JLabel lblHinhThuc = new JLabel("H\u00ECnh th\u1EE9c TT: " + (hoaDon.getHinhThucThanhToan() != null ? hoaDon.getHinhThucThanhToan().name() : "N/A"));
        lblHinhThuc.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblHinhThuc.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JLabel lblTongTien = new JLabel("T\u1ED5ng Thanh To\u00E1n: " + nf.format(hoaDon.getTongTienPhaiTra()) + "\u0111");
        lblTongTien.setFont(new Font("Roboto", Font.BOLD, 18));
        lblTongTien.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongTien.setForeground(new Color(231, 76, 60));

        pnlTotal.add(lblHinhThuc);
        pnlTotal.add(lblTongTien);
        pnlCenter.add(pnlTotal, BorderLayout.SOUTH);

        main.add(pnlCenter, BorderLayout.CENTER);

        // ── Bot: Buttons ──
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pnlBot.setOpaque(false);

        JButton btnClose = new JButton("\u0110\u00D3NG");
        btnClose.setFont(new Font("Roboto", Font.BOLD, 13));
        btnClose.setPreferredSize(new Dimension(120, 40));
        btnClose.setFocusable(false);
        btnClose.addActionListener(e -> dispose());

        JButton btnPrint = new JButton("\uD83D\uDDA8 IN H\u00D3A \u0110\u01A0N");
        btnPrint.setFont(new Font("Roboto", Font.BOLD, 13));
        btnPrint.setBackground(new Color(41, 128, 185));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setPreferredSize(new Dimension(150, 40));
        btnPrint.setFocusable(false);
        btnPrint.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPrint.addActionListener(e -> {
            try {
                String pdfPath = PDFPrinter.exportBill(hoaDon, items);
                
                int ans = JOptionPane.showConfirmDialog(this,
                    "Xu\u1EA5t PDF th\u00E0nh c\u00F4ng t\u1EA1i th\u01B0 m\u1EE5c exports/\nB\u1EA1n c\u00F3 mu\u1ED1n m\u1EDF file l\u00EAn xem kh\u00F4ng?",
                    "In H\u00F3a \u0110\u01A1n", JOptionPane.YES_NO_OPTION);
                if (ans == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(new File(pdfPath));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "L\u1ED7i t\u1EA1o file PDF: " + ex.getMessage(), "L\u1ED7i", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlBot.add(btnClose);
        pnlBot.add(btnPrint);
        main.add(pnlBot, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JPanel createDetailRow(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(3, 0, 3, 0));
        
        JLabel t = new JLabel(title);
        t.setFont(new Font("Roboto", Font.PLAIN, 14));
        t.setForeground(Color.DARK_GRAY);
        
        JLabel v = new JLabel(value);
        v.setFont(new Font("Roboto", Font.BOLD, 14));
        
        p.add(t, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }
}
