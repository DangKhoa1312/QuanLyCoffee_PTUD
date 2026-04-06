package ui.panel;

import controller.MenuController;
import controller.OrderController;
import dto.CartItem;
import entity.Ban;
import entity.DonHang;
import entity.Mon;
import enums.LoaiMon;
import ui.dialog.ItemOptionDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình Gọi Món: Split-pane trái MENU, phải GIỎ HÀNG.
 */
public class OrderPanel extends JPanel {

    private final MenuController menuController;
    private final OrderController orderController;

    private Ban currentBan;
    private DonHang currentDonHang;
    private List<CartItem> cartData = new ArrayList<>();

    // UI Components
    private JLabel lblHeaderTitle;
    private JPanel menuGrid;
    private JTabbedPane tabCategories;
    
    // Khai báo bảng giỏ hàng
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel lblTotalCart;

    private Runnable onBackAction;

    private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    public OrderPanel() {
        this.menuController = new MenuController();
        this.orderController = new OrderController();

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        initUI();
        loadCategories();
    }

    public void setOnBackAction(Runnable r) {
        this.onBackAction = r;
    }

    private void initUI() {
        // ── 1. Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton btnBack = new JButton("← Quay lại thẻ Bàn");
        btnBack.setFont(new Font("Roboto", Font.BOLD, 14));
        btnBack.setFocusable(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.setBackground(new Color(220, 220, 225));
        btnBack.addActionListener(e -> {
            if (onBackAction != null) onBackAction.run();
        });
        header.add(btnBack, BorderLayout.WEST);

        lblHeaderTitle = new JLabel("CHƯA CHỌN BÀN", SwingConstants.CENTER);
        lblHeaderTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblHeaderTitle.setForeground(new Color(44, 62, 80));
        header.add(lblHeaderTitle, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // ── 2. Split Pane (Trái: Menu  |  Phải: Giỏ) ──
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        splitPane.setLeftComponent(createMenuPanel());
        splitPane.setRightComponent(createCartPanel());

        add(splitPane, BorderLayout.CENTER);
    }

    // ── GIAO DIỆN TRÁI (MENU) ──
    private JPanel createMenuPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 20, 20, 10));

        tabCategories = new JTabbedPane();
        tabCategories.setFont(new Font("Roboto", Font.BOLD, 14));
        tabCategories.addChangeListener(e -> loadMenuToGrid());

        menuGrid = new JPanel(new utils.WrapLayout(FlowLayout.LEFT, 15, 15));
        menuGrid.setBackground(Color.WHITE);
        menuGrid.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scroll = new JScrollPane(menuGrid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().addChangeListener(e -> {
            menuGrid.revalidate();
            menuGrid.repaint();
        });

        p.add(tabCategories, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // ── GIAO DIỆN PHẢI (GIỎ HÀNG) ──
    private JPanel createCartPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(0, 10, 20, 20),
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1)
        ));

        // Tiêu đề
        JPanel topCart = new JPanel(new BorderLayout());
        topCart.setOpaque(true);
        topCart.setBackground(new Color(245, 245, 245));
        topCart.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel title = new JLabel(" Giỏ Hàng", SwingConstants.LEFT);
        title.setFont(new Font("Roboto", Font.BOLD, 18));
        topCart.add(title, BorderLayout.CENTER);

        JButton btnTuyChon = new JButton("⇄ ĐỔI/GỘP BÀN");
        btnTuyChon.setFont(new Font("Roboto", Font.BOLD, 12));
        btnTuyChon.setBackground(new Color(220, 220, 220));
        btnTuyChon.setForeground(new Color(44, 62, 80));
        btnTuyChon.setFocusable(false);
        btnTuyChon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTuyChon.addActionListener(e -> moTuyChonBan());
        topCart.add(btnTuyChon, BorderLayout.EAST);

        p.add(topCart, BorderLayout.NORTH);

        // Bảng
        String[] cols = {"Món (Size)", "SL", "Giá", "Tổng"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(40);
        cartTable.setFont(new Font("Roboto", Font.PLAIN, 14));
        cartTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));

        // Căn chỉnh cột
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(220); // Tên món
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(45);  // SL
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(85);  // Giá
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(95);  // Tổng

        // Căn lề cho cột SL (Center), Giá & Tổng (Right)
        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        rightRenderer.setVerticalAlignment(SwingConstants.TOP); // Để khớp hàng với cột Tên món khi dùng HTML
        
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setVerticalAlignment(SwingConstants.TOP);

        cartTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setBorder(null);
        p.add(scroll, BorderLayout.CENTER);

        // Bot: Total + Nút action
        JPanel bot = new JPanel(new BorderLayout());
        bot.setBackground(Color.WHITE);
        bot.setBorder(new EmptyBorder(15, 15, 15, 15));

        lblTotalCart = new JLabel("Tổng tiền: 0đ");
        lblTotalCart.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTotalCart.setForeground(new Color(231, 76, 60));
        bot.add(lblTotalCart, BorderLayout.NORTH);

        JPanel pnlBtns = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlBtns.setOpaque(false);
        pnlBtns.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnClear = new JButton("XÓA MÓN");
        btnClear.setFont(new Font("Roboto", Font.BOLD, 13));
        btnClear.setBackground(new Color(231, 76, 60)); // Đỏ
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusable(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> xoaMonKhoiGio());

        JButton btnHuyDon = new JButton("HỦY ĐƠN");
        btnHuyDon.setFont(new Font("Roboto", Font.BOLD, 13));
        btnHuyDon.setBackground(new Color(192, 57, 43)); // Đỏ đậm
        btnHuyDon.setForeground(Color.WHITE);
        btnHuyDon.setFocusable(false);
        btnHuyDon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHuyDon.addActionListener(e -> huyDonHangAction());

        JButton btnLuu = new JButton("LƯU VÀO BẾP");
        btnLuu.setFont(new Font("Roboto", Font.BOLD, 13));
        btnLuu.setBackground(new Color(243, 156, 18)); // Cam
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setFocusable(false);
        btnLuu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLuu.addActionListener(e -> saveDonHang());

        JButton btnThanhToan = new JButton("THANH TOÁN");
        btnThanhToan.setFont(new Font("Roboto", Font.BOLD, 13));
        btnThanhToan.setBackground(new Color(39, 174, 96)); // Xanh
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setFocusable(false);
        btnThanhToan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnThanhToan.addActionListener(e -> moThanhToan());

        pnlBtns.add(btnClear);
        pnlBtns.add(btnHuyDon);
        pnlBtns.add(btnLuu);
        pnlBtns.add(btnThanhToan);

        bot.add(pnlBtns, BorderLayout.SOUTH);
        p.add(bot, BorderLayout.SOUTH);

        return p;
    }

    // ── XỬ LÝ LOGIC UI ──
    private void loadCategories() {
        tabCategories.removeAll();
        tabCategories.addTab("Tất cả", null);
        tabCategories.putClientProperty("cat_0", null);

        LoaiMon[] cats = menuController.getDanhMuc();
        for (int i = 0; i < cats.length; i++) {
            tabCategories.addTab(cats[i].name(), null);
            tabCategories.putClientProperty("cat_" + (i + 1), cats[i]);
        }
    }

    private void loadMenuToGrid() {
        menuGrid.removeAll();
        int idx = tabCategories.getSelectedIndex();
        if (idx < 0) return;

        LoaiMon loai = (LoaiMon) tabCategories.getClientProperty("cat_" + idx);
        List<Mon> dsMon = menuController.getMon(loai);

        for (Mon m : dsMon) {
            menuGrid.add(createItemCard(m));
        }

        // WrapLayout tự động xuống dòng và dồn trái, không cần add(empty) nữa.

        menuGrid.revalidate();
        menuGrid.repaint();
    }

    private JPanel createItemCard(Mon m) {
        boolean isHet = menuController.isHetHang(m.getMaMon());

        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(160, 220));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        
        // Image Area (Top) - Placeholder 160x160
        JLabel lblImage = new JLabel();
        lblImage.setPreferredSize(new Dimension(160, 160));
        lblImage.setOpaque(true);
        lblImage.setBackground(isHet ? new Color(240, 240, 240) : new Color(248, 249, 250));
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        
        jiconfont.IconCode iconCode = isHet ? FontAwesome.BAN : FontAwesome.PICTURE_O;
        Color iconColor = isHet ? new Color(200, 150, 150) : new Color(200, 200, 200);
        lblImage.setIcon(jiconfont.swing.IconFontSwing.buildIcon(iconCode, 50, iconColor));
        
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBackground(card.getBackground());
        pnlTop.add(lblImage, BorderLayout.CENTER);

        // Info Area (Bottom)
        JPanel pnlInfo = new JPanel();
        pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(new EmptyBorder(8, 8, 8, 8));

        String nameHtml = "<html><div style='text-align: center; width: 130px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'>" 
                        + m.getTenMon() + "</div></html>";
        JLabel lblName = new JLabel(nameHtml, SwingConstants.CENTER);
        lblName.setFont(new Font("Roboto", Font.BOLD, 14));
        lblName.setForeground(isHet ? Color.GRAY : new Color(44, 62, 80));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String phuDe = isHet ? "(Hết hàng)" : "Chọn size...";
        JLabel lblPrice = new JLabel(phuDe, SwingConstants.CENTER);
        lblPrice.setFont(new Font("Roboto", isHet ? Font.PLAIN : Font.BOLD, 13));
        lblPrice.setForeground(isHet ? new Color(200, 50, 50) : new Color(39, 174, 96));
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlInfo.add(lblName);
        pnlInfo.add(Box.createVerticalStrut(4));
        pnlInfo.add(lblPrice);

        card.add(pnlTop, BorderLayout.CENTER);
        card.add(pnlInfo, BorderLayout.SOUTH);

        if (!isHet) {
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Color hoverBg = new Color(250, 252, 255);
            Color hoverBorder = new Color(52, 152, 219);
            Color defaultBorder = new Color(230, 230, 230);
            
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBorder(BorderFactory.createLineBorder(hoverBorder, 1));
                    pnlInfo.setBackground(hoverBg);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBorder(BorderFactory.createLineBorder(defaultBorder, 1));
                    pnlInfo.setBackground(Color.WHITE);
                }
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    showOptionDialog(m);
                }
            });
        }
        return card;
    }

    private void showOptionDialog(Mon mon) {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win instanceof JFrame) {
            ItemOptionDialog dlg = new ItemOptionDialog((JFrame) win, mon, menuController);
            dlg.setVisible(true);

            CartItem result = dlg.getResult();
            if (result != null) {
                cartData.add(result);
                renderCartTable();
            }
        }
    }

    private void xoaMonKhoiGio() {
        int selected = cartTable.getSelectedRow();
        if (selected >= 0) {
            cartData.remove(selected);
            renderCartTable();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món cần xóa trong Giỏ hàng!");
        }
    }

    private void renderCartTable() {
        cartModel.setRowCount(0);
        double total = 0;

        for (CartItem item : cartData) {
            int totalSubRows = 1 + item.getToppings().size() + (item.getGhiChu().isEmpty() ? 0 : 1);
            
            // --- Khởi tạo 3 luồng HTML đồng bộ ---
            StringBuilder nameHtml = new StringBuilder("<html><table width='100%' border='0' cellspacing='0' cellpadding='1'>");
            StringBuilder priceHtml = new StringBuilder("<html><table width='100%' border='0' cellspacing='0' cellpadding='1'>");
            StringBuilder amountHtml = new StringBuilder("<html><table width='100%' border='0' cellspacing='0' cellpadding='1'>");

            // 1. Dòng chính (Món + Size)
            String mainName = "<b>" + item.getMon().getTenMon() + "</b> (" + item.getSize().getTenSize() + ")";
            nameHtml.append("<tr><td style='font-family:Roboto; font-size:13px;'>").append(mainName).append("</td></tr>");
            priceHtml.append("<tr><td align='right' style='font-family:Roboto; font-size:13px;'>").append(nf.format(item.getDonGiaSize())).append("đ</td></tr>");
            amountHtml.append("<tr><td align='right' style='font-family:Roboto; font-size:13px;'>").append(nf.format(item.getDonGiaSize() * item.getSoLuong())).append("đ</td></tr>");

            // 2. Các dòng Topping
            for (dto.CartItem.CartTopping ctx : item.getToppings()) {
                String topName = "&nbsp;&nbsp;&nbsp;+ " + ctx.topping.getTenTopping() + " (x" + ctx.soLuong + ")";
                nameHtml.append("<tr><td style='font-family:Roboto; font-size:11px; color:gray;'>").append(topName).append("</td></tr>");
                
                String topPrice = "+ " + nf.format(ctx.giaTopping * ctx.soLuong) + "đ";
                priceHtml.append("<tr><td align='right' style='font-family:Roboto; font-size:11px; color:gray;'>").append(topPrice).append("</td></tr>");
                
                double topTotal = ctx.giaTopping * ctx.soLuong * item.getSoLuong();
                amountHtml.append("<tr><td align='right' style='font-family:Roboto; font-size:11px; color:gray;'>+ ").append(nf.format(topTotal)).append("đ</td></tr>");
            }

            // 3. Dòng Ghi chú (nếu có)
            if (!item.getGhiChu().isEmpty()) {
                nameHtml.append("<tr><td style='font-family:Roboto; font-size:11px; color:orange;'>(").append(item.getGhiChu()).append(")</td></tr>");
                priceHtml.append("<tr><td>&nbsp;</td></tr>"); // Dòng trống để khớp hàng
                amountHtml.append("<tr><td>&nbsp;</td></tr>");
            }

            nameHtml.append("</table></html>");
            priceHtml.append("</table></html>");
            amountHtml.append("</table></html>");

            // Cột SL cũng dùng HTML để ép căn TOP đồng bộ
            String slHtml = "<html><div style='padding-top:2px;'>" + item.getSoLuong() + "</div></html>";

            cartModel.addRow(new Object[]{
                nameHtml.toString(),
                slHtml,
                priceHtml.toString(),
                amountHtml.toString()
            });

            // Chiều cao hàng: mỗi dòng khoảng 18-20px
            cartTable.setRowHeight(cartModel.getRowCount() - 1, totalSubRows * 20 + 15);
            
            total += item.getThanhTien();
        }

        lblTotalCart.setText("Tổng tiền: " + nf.format(total) + "đ");
    }

    // ── PUBLIC API ĐỂ MAINFRAME GỌI ──

    /**
     * Mở OrderPanel cho một Bàn và Đơn Hàng (có thể null nếu order mới)
     */
    public void loadDonHangForTable(Ban ban, DonHang dh) {
        this.currentBan = ban;
        this.currentDonHang = dh;

        // Set tiêu đề header
        if (ban == null || "MANG_VE".equals(ban.getMaBan())) {
            lblHeaderTitle.setText("🛍 Bán Mang Về");
        } else {
            lblHeaderTitle.setText("🏠 Bàn số " + ban.getSoBan());
        }

        // Load cart
        if (dh != null) {
            cartData = orderController.loadCart(dh.getMaDonHang());
        } else {
            cartData.clear();
        }

        renderCartTable();
        loadMenuToGrid();
    }

    private boolean performSaveOrder() {
        if (cartData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống, không thể lưu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            currentDonHang = orderController.saveOrder(currentDonHang, currentBan, cartData);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lưu đơn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void saveDonHang() {
        if (performSaveOrder()) {
            JOptionPane.showMessageDialog(this, "Đã lưu order vào bếp thành công!");
            // Quay lại màn hình bàn
            if (onBackAction != null)
                onBackAction.run();
        }
    }

    private void moThanhToan() {
        // FAST CHECKOUT: Luôn tự động lưu giỏ hàng hiện tại trước khi mở thanh toán
        if (!performSaveOrder()) return; 

        Window win = SwingUtilities.getWindowAncestor(this);
        if (win instanceof JFrame) {
            ui.dialog.PaymentDialog dlg = new ui.dialog.PaymentDialog((JFrame) win, currentDonHang);
            dlg.setVisible(true);

            if (dlg.isPaid()) {
                if (onBackAction != null)
                    onBackAction.run(); // Quay về sơ đồ bàn
            }
        }
    }

    private void huyDonHangAction() {
        if (currentDonHang == null) {
            JOptionPane.showMessageDialog(this, "Đơn hàng mới chưa được lưu, chỉ cần xóa đồ trong giỏ!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int xn = JOptionPane.showConfirmDialog(this,
                "Bạn có CHẮC CHẮN muốn hủy đơn hàng [" + currentDonHang.getMaDonHang() + "] không?",
                "Xác nhận hủy đơn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (xn == JOptionPane.YES_OPTION) {
            try {
                orderController.huyDonHang(currentDonHang.getMaDonHang());
                JOptionPane.showMessageDialog(this, "Đã hủy đơn hàng thành công!");
                if (onBackAction != null) onBackAction.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi hủy đơn", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void moTuyChonBan() {
        if (currentDonHang == null || !"DANG_PHUC_VU".equals(currentDonHang.getTrangThai().name())) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể đổi/gộp bàn cho đơn hàng đã [Lưu vào Bếp]!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("MANG_VE".equals(currentBan.getMaBan())) {
            JOptionPane.showMessageDialog(this, "Không cho phép chuyển/gộp đối với đơn Mang về!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win instanceof JFrame) {
            ui.dialog.TransferTableDialog dlg = new ui.dialog.TransferTableDialog((JFrame) win, currentBan, currentDonHang);
            dlg.setVisible(true);
            if (dlg.isSuccess()) {
                if (onBackAction != null) onBackAction.run();
            }
        }
    }
}