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
 * Màn hình Gọi Món hiện đại.
 * Layout 3 cột: Sidebar (Trái) | Menu (Giữa) | Cart (Phải).
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
    private JPanel categorySidebar;
    private ButtonGroup bgCategories;
    
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel lblTotalCart;

    private Runnable onBackAction;
    private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    public OrderPanel() {
        this.menuController = new MenuController();
        this.orderController = new OrderController();

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250)); // Nền xám nhạt

        initUI();
    }

    public void setOnBackAction(Runnable r) {
        this.onBackAction = r;
    }

    private void initUI() {
        // ── 1. Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton btnBack = new JButton("⬅ Quay lại");
        btnBack.setFont(new Font("Roboto", Font.BOLD, 15));
        btnBack.putClientProperty("FlatLaf.style", "borderWidth:0; background:null; foreground: #666666");
        btnBack.setFocusable(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            if (onBackAction != null) onBackAction.run();
        });
        header.add(btnBack, BorderLayout.WEST);

        lblHeaderTitle = new JLabel("CHƯA CHỌN BÀN", SwingConstants.CENTER);
        lblHeaderTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblHeaderTitle.setForeground(new Color(113, 76, 52));
        header.add(lblHeaderTitle, BorderLayout.CENTER);

        // Placeholder for right balance
        JLabel spacer = new JLabel();
        spacer.setPreferredSize(new Dimension(100, 10));
        header.add(spacer, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── 2. Split Pane Center ──
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);

        // -- Sidebar (Loại Món) --
        categorySidebar = new JPanel();
        categorySidebar.setLayout(new BoxLayout(categorySidebar, BoxLayout.Y_AXIS));
        categorySidebar.setOpaque(false);
        categorySidebar.setBorder(new EmptyBorder(0, 20, 20, 10));
        categorySidebar.setPreferredSize(new Dimension(180, 0));
        
        loadCategoriesSidebar();
        
        centerContainer.add(categorySidebar, BorderLayout.WEST);

        // -- Split (Menu | Cart) --
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(550);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        splitPane.setLeftComponent(createMenuGridPanel());
        splitPane.setRightComponent(createCartPanel());

        centerContainer.add(splitPane, BorderLayout.CENTER);
        
        add(centerContainer, BorderLayout.CENTER);
    }

    // ── GIAO DIỆN TRÁI (DANH MỤC) ──
    private void loadCategoriesSidebar() {
        categorySidebar.removeAll();
        bgCategories = new ButtonGroup();

        // Nút Tất cả
        JToggleButton btnAll = createCategoryButton("Tất cả", null);
        btnAll.setSelected(true); 

        LoaiMon[] cats = menuController.getDanhMuc();
        for (LoaiMon cat : cats) {
            String title = cat.getTenLoai();
            createCategoryButton(title, cat);
        }
        categorySidebar.revalidate();
        categorySidebar.repaint();
    }

    private JToggleButton createCategoryButton(String title, LoaiMon cat) {
        JToggleButton btn = new JToggleButton(title);
        btn.setFont(new Font("Roboto", Font.BOLD, 15));
        
        btn.putClientProperty("FlatLaf.style", "arc: 15; margin: 12,20,12,20; borderWidth: 0;" +
            "selectedBackground: #714c34; selectedForeground: #ffffff;" + 
            "background: null; foreground: #4a3628; hoverBackground: #e8ecef; focusWidth: 0");
            
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(160, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btn.addActionListener(e -> {
            loadMenuToGrid(cat);
        });

        bgCategories.add(btn);
        categorySidebar.add(btn);
        categorySidebar.add(Box.createVerticalStrut(10)); 
        
        return btn;
    }

    // ── GIAO DIỆN GIỮA (LƯỚI MÓN) ──
    private JPanel createMenuGridPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        
        menuGrid = new JPanel(new utils.WrapLayout(FlowLayout.LEFT, 15, 15));
        menuGrid.setOpaque(false);
        menuGrid.setBorder(new EmptyBorder(0, 10, 20, 10));

        JScrollPane scroll = new JScrollPane(menuGrid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }
    
    private void loadMenuToGrid(LoaiMon cat) {
        menuGrid.removeAll();
        // Nếu cat == null, controller sẽ lấy tất cả do param trong SQL pattern 
        List<Mon> dsMon = menuController.getMon(cat); 

        for (Mon m : dsMon) {
            menuGrid.add(createItemCard(m));
        }

        menuGrid.revalidate();
        menuGrid.repaint();
    }
    
    private JPanel createItemCard(Mon m) {
        boolean isHet = menuController.isHetHang(m.getMaMon());

        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(165, 230));
        card.setBackground(Color.WHITE);
        card.putClientProperty("FlatLaf.style", "arc: 20; border: 1,1,1,1,#e8e8e8;"); 
        
        // Image Area
        JLabel lblImage = new JLabel();
        lblImage.setPreferredSize(new Dimension(165, 130));
        lblImage.setOpaque(true);
        lblImage.setBackground(isHet ? new Color(240, 240, 240) : new Color(248, 249, 250));
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        
        jiconfont.IconCode iconCode = isHet ? FontAwesome.BAN : FontAwesome.COFFEE; 
        Color iconColor = isHet ? new Color(200, 150, 150) : new Color(139, 90, 43);
        lblImage.setIcon(jiconfont.swing.IconFontSwing.buildIcon(iconCode, 50, iconColor));
        lblImage.putClientProperty("FlatLaf.style", "arc: 20");

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        pnlTop.add(lblImage, BorderLayout.CENTER);

        // Info Area
        JPanel pnlInfo = new JPanel(new BorderLayout());
        pnlInfo.setOpaque(false);
        pnlInfo.setBorder(new EmptyBorder(10, 10, 10, 10));

        String nameHtml = "<html><div style='text-align: center; width: 130px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'>" 
                        + m.getTenMon() + "</div></html>";
        JLabel lblName = new JLabel(nameHtml, SwingConstants.CENTER);
        lblName.setFont(new Font("Roboto", Font.BOLD, 15));
        lblName.setForeground(isHet ? Color.GRAY : new Color(26, 26, 26));

        String subText = isHet ? "(Hết hàng)" : "Tuỳ chọn size...";
        JLabel lblPrice = new JLabel(subText, SwingConstants.CENTER);
        lblPrice.setFont(new Font("Roboto", Font.BOLD, 12));
        lblPrice.setForeground(isHet ? new Color(200, 50, 50) : new Color(39, 174, 96));

        JPanel pnlText = new JPanel();
        pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
        pnlText.setOpaque(false);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlText.add(lblName);
        pnlText.add(Box.createVerticalStrut(5));
        pnlText.add(lblPrice);

        // Nút Thêm
        JButton btnAdd = new JButton("+ Thêm");
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 13));
        btnAdd.putClientProperty("FlatLaf.style", "arc: 15; background: #f0f0f0; foreground: #333333; borderWidth: 0;");
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.setFocusable(false);
        
        pnlInfo.add(pnlText, BorderLayout.CENTER);
        pnlInfo.add(btnAdd, BorderLayout.SOUTH);

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(pnlInfo, BorderLayout.CENTER);

        if (!isHet) {
            Color hoverBg = new Color(250, 252, 255);
            Color hoverBorder = new Color(52, 152, 219);
            Color defaultBorder = new Color(230, 230, 230);
            
            java.awt.event.MouseAdapter clickHandler = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBorder(BorderFactory.createLineBorder(hoverBorder, 1));
                    pnlInfo.setBackground(hoverBg);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBorder(BorderFactory.createLineBorder(defaultBorder, 1));
                    pnlInfo.setBackground(null);
                }
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    showOptionDialog(m);
                }
            };
            
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(clickHandler);
            btnAdd.addActionListener(e -> showOptionDialog(m));
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

    // ── GIAO DIỆN PHẢI (GIỎ HÀNG) ──
    private JPanel createCartPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.putClientProperty("FlatLaf.style", "arc: 20"); 
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Tiêu đề
        JPanel topCart = new JPanel(new BorderLayout());
        topCart.setOpaque(false);
        topCart.setBorder(new EmptyBorder(10, 10, 15, 10));
        
        JLabel title = new JLabel("🛒 Đơn hàng", SwingConstants.LEFT);
        title.setFont(new Font("Roboto", Font.BOLD, 18));
        title.setForeground(new Color(113, 76, 52));
        topCart.add(title, BorderLayout.CENTER);

        JButton btnTuyChonTop = new JButton("Chuyển / Ghép bàn");
        btnTuyChonTop.setFont(new Font("Roboto", Font.BOLD, 12));
        btnTuyChonTop.setBackground(new Color(245, 245, 245));
        btnTuyChonTop.putClientProperty("FlatLaf.style", "arc: 10; margin: 5,12,5,12; borderWidth:1; borderColor: #e0e0e0");
        btnTuyChonTop.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTuyChonTop.addActionListener(e -> moTuyChonBan());
        topCart.add(btnTuyChonTop, BorderLayout.EAST);

        p.add(topCart, BorderLayout.NORTH);

        // Bảng
        String[] cols = {"Món (Size)", "SL", "Giá", "Tổng", "Index"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(40);
        cartTable.setFont(new Font("Roboto", Font.PLAIN, 14));
        cartTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));
        cartTable.setShowGrid(false);

        // Ẩn cột Index (cột thứ 4 tính từ 0)
        cartTable.removeColumn(cartTable.getColumnModel().getColumn(4));

        cartTable.getColumnModel().getColumn(0).setPreferredWidth(200); 
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(45);  
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(85);  
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(95);  

        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        rightRenderer.setVerticalAlignment(SwingConstants.TOP); 
        
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setVerticalAlignment(SwingConstants.TOP);

        cartTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        p.add(scroll, BorderLayout.CENTER);

        // Bot: Total + Nút action
        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(15, 10, 10, 10));

        JPanel pnlTotalRow = new JPanel(new BorderLayout());
        pnlTotalRow.setOpaque(false);
        JLabel lblTotalText = new JLabel("Tổng cộng:");
        lblTotalText.setFont(new Font("Roboto", Font.BOLD, 16));
        lblTotalCart = new JLabel("0 đ");
        lblTotalCart.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTotalCart.setForeground(new Color(231, 76, 60)); 
        pnlTotalRow.add(lblTotalText, BorderLayout.WEST);
        pnlTotalRow.add(lblTotalCart, BorderLayout.EAST);
        
        bot.add(pnlTotalRow, BorderLayout.NORTH);

        JPanel pnlBtns = new JPanel(new GridLayout(2, 1, 0, 10)); // Dọc
        pnlBtns.setOpaque(false);
        pnlBtns.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton btnLuu = new JButton("Gọi Món (In phiếu)");
        btnLuu.setFont(new Font("Roboto", Font.BOLD, 15));
        btnLuu.setBackground(new Color(39, 174, 96)); // Xanh lá
        btnLuu.setForeground(Color.WHITE);
        btnLuu.putClientProperty("FlatLaf.style", "arc: 12; margin: 12,0,12,0; borderWidth:0");
        btnLuu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLuu.addActionListener(e -> saveDonHang());

        JButton btnThanhToan = new JButton("Thanh Toán");
        btnThanhToan.setFont(new Font("Roboto", Font.BOLD, 15));
        btnThanhToan.setBackground(new Color(74, 54, 40)); // Nâu
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.putClientProperty("FlatLaf.style", "arc: 12; margin: 12,0,12,0; borderWidth:0");
        btnThanhToan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnThanhToan.addActionListener(e -> moThanhToan());

        pnlBtns.add(btnLuu);
        pnlBtns.add(btnThanhToan);

        JPanel pnlUtils = new JPanel(new GridLayout(1, 2, 10, 0)); // Only 2 buttons now
        pnlUtils.setOpaque(false);
        pnlUtils.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnClear = new JButton("Xóa Món");
        btnClear.setFont(new Font("Roboto", Font.PLAIN, 12));
        btnClear.setBackground(new Color(250, 230, 230));
        btnClear.setForeground(new Color(200, 50, 50));
        btnClear.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,0,8,0; borderWidth:0");
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> xoaMonKhoiGio());

        JButton btnHuyDon = new JButton("Hủy Đơn");
        btnHuyDon.setFont(new Font("Roboto", Font.PLAIN, 12));
        btnHuyDon.setBackground(new Color(250, 230, 230));
        btnHuyDon.setForeground(new Color(200, 50, 50));
        btnHuyDon.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,0,8,0; borderWidth:0");
        btnHuyDon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHuyDon.addActionListener(e -> huyDonHangAction());

        pnlUtils.add(btnClear);
        pnlUtils.add(btnHuyDon);

        JPanel pnlBotGroup = new JPanel(new BorderLayout());
        pnlBotGroup.setOpaque(false);
        pnlBotGroup.add(pnlBtns, BorderLayout.CENTER);
        pnlBotGroup.add(pnlUtils, BorderLayout.SOUTH);

        bot.add(pnlBotGroup, BorderLayout.SOUTH);
        p.add(bot, BorderLayout.SOUTH);

        return p;
    }

    private void xoaMonKhoiGio() {
        int selected = cartTable.getSelectedRow();
        if (selected >= 0) {
            int dataIndex = (int) cartModel.getValueAt(cartTable.convertRowIndexToModel(selected), 4);
            cartData.remove(dataIndex);
            renderCartTable();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món cần xóa trong Giỏ hàng!");
        }
    }

    private void renderCartTable() {
        cartModel.setRowCount(0);
        double total = 0;

        for (int i = 0; i < cartData.size(); i++) {
            CartItem item = cartData.get(i);
            
            String sizeLabel = item.getSize().getTenSize();
            boolean isNormal = sizeLabel.equalsIgnoreCase("Thường") || sizeLabel.contains("Thư?");
            String statusHtml = item.isDaPhucVu() ? " <span style='color:#27ae60;font-size:10px;'>(Đã báo)</span>" : " <span style='color:#e74c3c;font-size:10px;'>(Mới)</span>";
            String mainName = "<html><div style='font-family:Roboto; font-size:13px; width:160px; word-wrap:break-word;'>" + 
                             "<b>" + item.getMon().getTenMon() + "</b>" + 
                             (isNormal ? "" : " (" + sizeLabel + ")") + statusHtml + "</div></html>";
                             
            String mainPrice = "<html><div style='font-family:Roboto; font-size:13px;'>" + nf.format(item.getDonGiaSize()) + "đ</div></html>";
            String slHtml = "<html><div style='font-family:Roboto; font-size:13px; font-weight:bold;'>" + item.getSoLuong() + "</div></html>";
            String amountHtml = "<html><div style='font-family:Roboto; font-size:14px; font-weight:bold; color:#2c3e50;'>" + nf.format(item.getThanhTien()) + "đ</div></html>";

            // Thêm dòng món chính
            cartModel.addRow(new Object[]{ mainName, slHtml, mainPrice, amountHtml, i });

            // Thêm cột topping
            for (dto.CartItem.CartTopping ctx : item.getToppings()) {
                String topName = "<html><div style='font-family:Roboto; font-size:11px; color:gray; padding-left:15px; width:150px;'>" +
                                 "+ " + ctx.topping.getTenTopping() + " (x" + ctx.soLuong + ")</div></html>";
                String topPrice = "<html><div style='font-family:Roboto; font-size:11px; color:gray;'>" + 
                                 "+ " + nf.format(ctx.giaTopping * ctx.soLuong) + "đ</div></html>";
                                 
                cartModel.addRow(new Object[]{ topName, "", topPrice, "", i });
            }

            // Thêm dòng ghi chú
            if (!item.getGhiChu().isEmpty()) {
                String noteName = "<html><div style='font-family:Roboto; font-size:11px; color:orange; padding-left:15px; width:150px;'>" +
                                  "* " + item.getGhiChu() + "</div></html>";
                cartModel.addRow(new Object[]{ noteName, "", "", "", i });
            }

            total += item.getThanhTien();
        }

        // Cập nhật cao độ từng dòng
        for (int r = 0; r < cartModel.getRowCount(); r++) {
            String nameVal = (String) cartModel.getValueAt(r, 0);
            if (nameVal != null && nameVal.contains("<b>")) {
                cartTable.setRowHeight(r, 45); // Dòng chính (có thể bọc chữ)
            } else {
                cartTable.setRowHeight(r, 22); // Dòng topping / Ghi chú
            }
        }

        lblTotalCart.setText(nf.format(total) + " đ");
    }

    // ── PUBLIC API ĐỂ MAINFRAME GỌI ──

    public void loadDonHangForTable(Ban ban, DonHang dh) {
        this.currentBan = ban;
        this.currentDonHang = dh;

        if (ban == null || "MANG_VE".equals(ban.getMaBan())) {
            lblHeaderTitle.setText("🛍 Bán Mang Về");
        } else {
            lblHeaderTitle.setText("Gọi Món - " + ban.getSoBan());
        }

        if (dh != null) {
            cartData = orderController.loadCart(dh.getMaDonHang());
        } else {
            cartData = new ArrayList<>();
        }

        renderCartTable();
        
        // Reset category to "Tat Ca"
        if(bgCategories != null) {
            java.util.Enumeration<AbstractButton> elements = bgCategories.getElements();
            if(elements.hasMoreElements()) {
                AbstractButton btn = elements.nextElement();
                btn.setSelected(true);
            }
        }
        loadMenuToGrid(null); // Load all
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
        if (cartData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        List<CartItem> newItems = new ArrayList<>();
        for (CartItem item : cartData) {
            if (!item.isDaPhucVu()) {
                newItems.add(item);
                item.setDaPhucVu(true);
            }
        }
        
        if (!newItems.isEmpty()) {
            showKitchenReceipt(newItems);
        }
        
        if (performSaveOrder()) {
            renderCartTable(); // Cập nhật lại UI để chuyển chữ (Mới) -> (Đã báo bếp)
            JOptionPane.showMessageDialog(this, "Lưu đơn hàng thành công!");
            if (onBackAction != null) onBackAction.run();
        }
    }

    private void showKitchenReceipt(List<CartItem> newItems) {
        StringBuilder sb = new StringBuilder();
        sb.append("============================\n");
        sb.append("     PHIẾU BÁO CHẾ BIẾN     \n");
        sb.append("      ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        if (currentBan != null && !"MANG_VE".equals(currentBan.getMaBan())) {
            sb.append(" Bàn: ").append(currentBan.getSoBan()).append("\n");
        } else {
            sb.append(" Bàn: MANG VỀ\n");
        }
        sb.append("============================\n");
        sb.append(String.format("%-20s %5s\n", "Tên món", "SL"));
        sb.append("----------------------------\n");
        
        for (CartItem item : newItems) {
            String sizeStr = item.getSize().getTenSize().equalsIgnoreCase("Thường") ? "" : " (" + item.getSize().getTenSize() + ")";
            sb.append(String.format("%-20s %5d\n", item.getMon().getTenMon() + sizeStr, item.getSoLuong()));
            for (dto.CartItem.CartTopping top : item.getToppings()) {
                sb.append(String.format("  + %-17s %5s\n", top.topping.getTenTopping(), "x" + top.soLuong));
            }
            if (!item.getGhiChu().isEmpty()) {
                sb.append("  * Ghi chú: ").append(item.getGhiChu()).append("\n");
            }
        }
        sb.append("============================\n");
        
        JTextArea txtReceipt = new JTextArea(sb.toString());
        txtReceipt.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtReceipt.setEditable(false);
        txtReceipt.setBackground(new Color(255, 250, 240));
        txtReceipt.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JOptionPane.showMessageDialog(this, new JScrollPane(txtReceipt), "Mô phỏng Máy In Bếp", JOptionPane.INFORMATION_MESSAGE);
    }

    private void moThanhToan() {
        if (!performSaveOrder()) return; 

        Window win = SwingUtilities.getWindowAncestor(this);
        if (win instanceof JFrame) {
            ui.dialog.PaymentDialog dlg = new ui.dialog.PaymentDialog((JFrame) win, currentDonHang);
            dlg.setVisible(true);

            if (dlg.isPaid()) {
                if (onBackAction != null) onBackAction.run(); 
            }
        }
    }

    private void huyDonHangAction() {
        if (currentDonHang == null) {
            JOptionPane.showMessageDialog(this, "Đơn hàng mới chưa được lưu, chỉ cần xóa đồ trong giỏ!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int xn = JOptionPane.showConfirmDialog(this,
                "Bạn có CHẮC CHẮN muốn hủy đơn hàng này không?",
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
            JOptionPane.showMessageDialog(this, "Chỉ có thể đổi/gộp bàn cho đơn hàng đã [Gửi Bếp]!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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