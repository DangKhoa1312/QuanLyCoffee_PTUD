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
import javax.imageio.ImageIO;

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
    private JTextField txtSearchMon;

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
        btnBack.putClientProperty("JButton.borderWidth", 0);
        btnBack.setBackground(null);
        btnBack.setForeground(new Color(102, 102, 102)); // #666666
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
        
        btn.putClientProperty("JButton.buttonArc", 15);
        btn.putClientProperty("JButton.margin", new java.awt.Insets(12, 20, 12, 20));
        btn.putClientProperty("JButton.borderWidth", 0);
        btn.putClientProperty("JButton.selectedBackground", new Color(113, 76, 52));
        btn.putClientProperty("JButton.selectedForeground", Color.WHITE);
        btn.putClientProperty("JButton.hoverBackground", new Color(232, 236, 239));
        btn.putClientProperty("JButton.focusWidth", 0);
        btn.putClientProperty("category", cat); // Lưu category để filterMenu() tra cứu
        btn.setBackground(null);
        btn.setForeground(new Color(74, 54, 40));
            
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(160, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btn.addActionListener(e -> {
            // Reset thanh tìm kiếm khi đổi danh mục
            if (txtSearchMon != null) txtSearchMon.setText("");
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

        // ── Thanh tìm kiếm món ──
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel lblSearch = new JLabel();
        lblSearch.setIcon(jiconfont.swing.IconFontSwing.buildIcon(
                jiconfont.icons.FontAwesome.SEARCH, 14, new Color(150, 150, 150)));
        searchBar.add(lblSearch, BorderLayout.WEST);

        txtSearchMon = new JTextField();
        txtSearchMon.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtSearchMon.putClientProperty("JTextField.placeholderText", "Tìm tên món...");
        txtSearchMon.putClientProperty("JComponent.arc", 8);
        txtSearchMon.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterMenu(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterMenu(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterMenu(); }
        });
        searchBar.add(txtSearchMon, BorderLayout.CENTER);
        p.add(searchBar, BorderLayout.NORTH);

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
        List<Mon> dsMon = menuController.getMon(cat);
        String keyword = (txtSearchMon != null) ? txtSearchMon.getText().trim().toLowerCase() : "";
        for (Mon m : dsMon) {
            if (keyword.isEmpty() || m.getTenMon().toLowerCase().contains(keyword)) {
                menuGrid.add(createItemCard(m));
            }
        }
        menuGrid.revalidate();
        menuGrid.repaint();
    }

    /** Lọc món theo từ khóa trong thanh tìm kiếm (giữ danh mục đang chọn) */
    private void filterMenu() {
        // Lấy loại món đang chọn từ category sidebar
        LoaiMon selectedCat = null;
        if (bgCategories != null) {
            java.util.Enumeration<AbstractButton> elements = bgCategories.getElements();
            while (elements.hasMoreElements()) {
                AbstractButton btn = elements.nextElement();
                if (btn.isSelected()) {
                    Object cat = btn.getClientProperty("category");
                    if (cat instanceof LoaiMon) selectedCat = (LoaiMon) cat;
                    break;
                }
            }
        }
        loadMenuToGrid(selectedCat);
    }
    
    /**
     * Giải quyết đường dẫn file ảnh bằng cách thử nhiều base path.
     * DB lưu path dạng "images/mon/xxx.jpg" (relative).
     * App có thể được chạy từ thư mục QuanLyCoffee hoặc thư mục cha.
     */
    private java.io.File resolveImageFile(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) return null;

        // 1. Thử trực tiếp (absolute path hoặc đúng CWD)
        java.io.File f = new java.io.File(storedPath);
        if (f.exists()) return f;

        // 2. Thử từ CWD hiện tại
        f = new java.io.File(System.getProperty("user.dir"), storedPath);
        if (f.exists()) return f;

        // 3. Thử từ thư mục cha của CWD (trường hợp CWD = .../Project/QuanLyCoffee)
        java.io.File parentDir = new java.io.File(System.getProperty("user.dir")).getParentFile();
        if (parentDir != null) {
            f = new java.io.File(parentDir, storedPath);
            if (f.exists()) return f;
        }

        // 4. Tên file thôi — tìm trong images/mon/ relative CWD
        String fileName = new java.io.File(storedPath).getName();
        f = new java.io.File(System.getProperty("user.dir"), "images/mon/" + fileName);
        if (f.exists()) return f;

        return null; // Không tìm thấy
    }

    private JPanel createItemCard(Mon m) {
        boolean isNgungBan = !m.isTrangThai();
        int soLuongConBan = menuController.getSoLuongConBanDuoc(m.getMaMon());
        boolean isHetNL = (soLuongConBan <= 0);
        boolean isSapHetNL = (soLuongConBan > 0 && soLuongConBan <= controller.InventoryController.NGUONG_CANH_BAO);
        boolean isHet = menuController.isHetHang(m.getMaMon()) || isHetNL;
        boolean isDisabled = isHet || isNgungBan;

        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(165, 230));
        card.setBackground(Color.WHITE);
        card.putClientProperty("JComponent.arc", 20);
        card.setBorder(BorderFactory.createLineBorder(
                isSapHetNL ? new Color(255, 180, 150) : new Color(232, 232, 232), 1));
        
        // Image Area — sử dụng LayeredPane để overlay badge cảnh báo
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblImage = new JLabel();
        lblImage.setPreferredSize(new Dimension(165, 130));
        lblImage.setOpaque(true);
        lblImage.setBackground(isDisabled ? new Color(240, 240, 240) : new Color(248, 249, 250));
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Load ảnh từ hinhAnh nếu có, ngược lại dùng icon FontAwesome
        if (isDisabled) {
            lblImage.setIcon(jiconfont.swing.IconFontSwing.buildIcon(FontAwesome.BAN, 50, new Color(200, 150, 150)));
        } else if (m.getHinhAnh() != null && !m.getHinhAnh().isBlank()) {
            java.io.File imgFile = resolveImageFile(m.getHinhAnh());
            if (imgFile != null && imgFile.exists()) {
                try {
                    java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(imgFile);
                    if (raw != null) {
                        java.awt.Image scaled = raw.getScaledInstance(165, 130, java.awt.Image.SCALE_SMOOTH);
                        lblImage.setIcon(new ImageIcon(scaled));
                    } else {
                        lblImage.setIcon(jiconfont.swing.IconFontSwing.buildIcon(FontAwesome.COFFEE, 50, new Color(139, 90, 43)));
                    }
                } catch (Exception imgEx) {
                    lblImage.setIcon(jiconfont.swing.IconFontSwing.buildIcon(FontAwesome.COFFEE, 50, new Color(139, 90, 43)));
                }
            } else {
                lblImage.setIcon(jiconfont.swing.IconFontSwing.buildIcon(FontAwesome.COFFEE, 50, new Color(139, 90, 43)));
            }
        } else {
            lblImage.setIcon(jiconfont.swing.IconFontSwing.buildIcon(FontAwesome.COFFEE, 50, new Color(139, 90, 43)));
        }
        lblImage.putClientProperty("JComponent.arc", 20);

        pnlTop.add(lblImage, BorderLayout.CENTER);

        // Badge cảnh báo ⚠ nguyên liệu sắp hết (overlay góc phải trên)
        if (isSapHetNL && !isDisabled) {
            JLabel lblWarningBadge = new JLabel("⚠") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(220, 38, 38)); // red-600
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblWarningBadge.setFont(new Font("Roboto", Font.BOLD, 14));
            lblWarningBadge.setForeground(Color.WHITE);
            lblWarningBadge.setHorizontalAlignment(SwingConstants.CENTER);
            lblWarningBadge.setPreferredSize(new Dimension(28, 24));
            lblWarningBadge.setToolTipText("Nguyên liệu sắp hết! Còn " + soLuongConBan + " phần");

            JPanel pnlBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
            pnlBadge.setOpaque(false);
            pnlBadge.add(lblWarningBadge);
            pnlTop.add(pnlBadge, BorderLayout.NORTH);
        }

        // Info Area
        JPanel pnlInfo = new JPanel(new BorderLayout());
        pnlInfo.setOpaque(false);
        pnlInfo.setBorder(new EmptyBorder(10, 10, 10, 10));

        String nameHtml = "<html><div style='text-align: center; width: 130px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'>" 
                        + m.getTenMon() + "</div></html>";
        JLabel lblName = new JLabel(nameHtml, SwingConstants.CENTER);
        lblName.setFont(new Font("Roboto", Font.BOLD, 15));
        lblName.setForeground(isDisabled ? Color.GRAY : new Color(26, 26, 26));

        // Xác định sub-text hiển thị
        String subText;
        if (isNgungBan) subText = "(Ngưng bán)";
        else if (isHetNL) subText = "(Hết nguyên liệu)";
        else if (isHet) subText = "(Hết hàng)";
        else if (isSapHetNL) subText = "⚠ Còn " + soLuongConBan + " phần";
        else subText = "Tuỳ chọn size...";

        JLabel lblPrice = new JLabel(subText, SwingConstants.CENTER);
        lblPrice.setFont(new Font("Roboto", Font.BOLD, 12));
        if (isSapHetNL && !isDisabled) {
            lblPrice.setForeground(new Color(220, 38, 38)); // Đỏ cảnh báo
        } else {
            lblPrice.setForeground(isDisabled ? new Color(200, 50, 50) : new Color(39, 174, 96));
        }

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
        btnAdd.putClientProperty("JButton.buttonArc", 15);
        btnAdd.setBackground(new Color(240, 240, 240)); // #f0f0f0
        btnAdd.setForeground(new Color(51, 51, 51)); // #333333
        btnAdd.putClientProperty("JButton.borderWidth", 0);
        btnAdd.setCursor(isDisabled ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.setFocusable(false);
        btnAdd.setEnabled(!isDisabled);
        
        pnlInfo.add(pnlText, BorderLayout.CENTER);
        pnlInfo.add(btnAdd, BorderLayout.SOUTH);

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(pnlInfo, BorderLayout.CENTER);

        if (!isDisabled) {
            Color hoverBg = new Color(250, 252, 255);
            Color hoverBorder = isSapHetNL ? new Color(220, 38, 38) : new Color(52, 152, 219);
            Color defaultBorder = isSapHetNL ? new Color(255, 180, 150) : new Color(230, 230, 230);
            
            java.awt.event.MouseAdapter clickHandler = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBorder(BorderFactory.createLineBorder(hoverBorder, 2));
                    pnlInfo.setBackground(hoverBg);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBorder(BorderFactory.createLineBorder(defaultBorder, 1));
                    pnlInfo.setBackground(null);
                }
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // Nếu sắp hết NL → cảnh báo trước khi cho gọi món
                    if (isSapHetNL) {
                        java.util.List<String> warnings = menuController.getCanhBaoNguyenLieu(m.getMaMon());
                        StringBuilder msg = new StringBuilder();
                        msg.append("<html><b style='color:#DC2626;font-size:13px;'>⚠ CẢNH BÁO: Nguyên liệu sắp hết!</b><br><br>");
                        msg.append("Món <b>").append(m.getTenMon()).append("</b> chỉ còn bán được <b>")
                           .append(soLuongConBan).append(" phần</b> nữa.<br><br>");
                        msg.append("<b>Chi tiết:</b><br>");
                        for (String w : warnings) {
                            msg.append("  • ").append(w).append("<br>");
                        }
                        msg.append("<br>Bạn vẫn muốn gọi món này?</html>");
                        
                        int xn = JOptionPane.showConfirmDialog(
                            SwingUtilities.getWindowAncestor(OrderPanel.this),
                            msg.toString(), "Cảnh báo nguyên liệu",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (xn != JOptionPane.YES_OPTION) return;
                    }
                    showOptionDialog(m);
                }
            };
            
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(clickHandler);
            btnAdd.addActionListener(e -> {
                // Trigger cùng logic cảnh báo khi ấn nút "Thêm"
                if (isSapHetNL) {
                    java.util.List<String> warnings = menuController.getCanhBaoNguyenLieu(m.getMaMon());
                    StringBuilder msg = new StringBuilder();
                    msg.append("<html><b style='color:#DC2626;'>⚠ Nguyên liệu sắp hết!</b><br>");
                    msg.append("Còn <b>").append(soLuongConBan).append("</b> phần. Tiếp tục?</html>");
                    int xn = JOptionPane.showConfirmDialog(
                        SwingUtilities.getWindowAncestor(OrderPanel.this),
                        msg.toString(), "Cảnh báo",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (xn != JOptionPane.YES_OPTION) return;
                }
                showOptionDialog(m);
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
                // Thử tạo một danh sách giỏ hàng mô phỏng để kiểm tra nguyên liệu
                List<CartItem> simulatedCart = new ArrayList<>();
                for (CartItem item : cartData) {
                    CartItem clonedItem = new CartItem(item.getMon(), item.getSize(), item.getSoLuong(), item.getDonGiaSize(), item.getGhiChu());
                    for (dto.CartItem.CartTopping ct : item.getToppings()) {
                        clonedItem.addTopping(ct.topping, ct.soLuong);
                    }
                    simulatedCart.add(clonedItem);
                }
                
                boolean mergedSim = false;
                for (CartItem item : simulatedCart) {
                    if (item.isIdentical(result)) {
                        item.setSoLuong(item.getSoLuong() + result.getSoLuong());
                        mergedSim = true;
                        break;
                    }
                }
                if (!mergedSim) {
                    simulatedCart.add(result);
                }

                // Kiểm tra xem với giỏ hàng này thì có đủ nguyên liệu không
                String missingInfo = new controller.InventoryController().checkDuNguyenLieuChoCart(simulatedCart);
                if (missingInfo != null) {
                    JOptionPane.showMessageDialog(this,
                            "Không đủ nguyên liệu để thêm món này vào giỏ:\n" + missingInfo,
                            "Thiếu nguyên liệu", JOptionPane.ERROR_MESSAGE);
                    return; // Chặn thêm vào giỏ
                }

                // Nếu OK thì thêm thật
                boolean merged = false;
                for (CartItem item : cartData) {
                    if (item.isIdentical(result)) {
                        item.setSoLuong(item.getSoLuong() + result.getSoLuong());
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    cartData.add(result);
                }
                renderCartTable();
            }
        }
    }

    // ── GIAO DIỆN PHẢI (GIỎ HÀNG) ──
    private JPanel createCartPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.putClientProperty("JComponent.arc", 20); 
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Tiêu đề
        JPanel topCart = new JPanel(new BorderLayout());
        topCart.setOpaque(false);
        topCart.setBorder(new EmptyBorder(10, 10, 15, 10));
        
        JLabel title = new JLabel("🛒 Đơn hàng", SwingConstants.LEFT);
        title.setFont(new Font("Roboto", Font.BOLD, 18));
        title.setForeground(new Color(113, 76, 52));
        topCart.add(title, BorderLayout.CENTER);

        JComboBox<String> cbTuyChonBan = new JComboBox<>(new String[] {"Tùy chọn Bàn...", "Chuyển bàn", "Ghép bàn", "Tách món"});
        cbTuyChonBan.setFont(new Font("Roboto", Font.BOLD, 13));
        cbTuyChonBan.setBackground(new Color(245, 245, 245));
        cbTuyChonBan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbTuyChonBan.setFocusable(false);
        cbTuyChonBan.addActionListener(e -> {
            int idx = cbTuyChonBan.getSelectedIndex();
            if (idx > 0) {
                handleMenuTuyChonBan(idx);
                SwingUtilities.invokeLater(() -> cbTuyChonBan.setSelectedIndex(0));
            }
        });
        topCart.add(cbTuyChonBan, BorderLayout.EAST);

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
        btnLuu.putClientProperty("JButton.buttonArc", 12);
        btnLuu.putClientProperty("JButton.margin", new java.awt.Insets(12, 0, 12, 0));
        btnLuu.putClientProperty("JButton.borderWidth", 0);
        btnLuu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLuu.addActionListener(e -> saveDonHang());

        JButton btnThanhToan = new JButton("Thanh Toán");
        btnThanhToan.setFont(new Font("Roboto", Font.BOLD, 15));
        btnThanhToan.setBackground(new Color(74, 54, 40)); // Nâu
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.putClientProperty("JButton.buttonArc", 12);
        btnThanhToan.putClientProperty("JButton.margin", new java.awt.Insets(12, 0, 12, 0));
        btnThanhToan.putClientProperty("JButton.borderWidth", 0);
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
        btnClear.putClientProperty("JButton.buttonArc", 10);
        btnClear.putClientProperty("JButton.margin", new java.awt.Insets(8, 0, 8, 0));
        btnClear.putClientProperty("JButton.borderWidth", 0);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> xoaMonKhoiGio());

        JButton btnHuyDon = new JButton("Hủy Đơn");
        btnHuyDon.setFont(new Font("Roboto", Font.PLAIN, 12));
        btnHuyDon.setBackground(new Color(250, 230, 230));
        btnHuyDon.setForeground(new Color(200, 50, 50));
        btnHuyDon.putClientProperty("JButton.buttonArc", 10);
        btnHuyDon.putClientProperty("JButton.margin", new java.awt.Insets(8, 0, 8, 0));
        btnHuyDon.putClientProperty("JButton.borderWidth", 0);
        btnHuyDon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHuyDon.addActionListener(e -> huyDonHangAction());

        pnlUtils.add(btnClear);
        pnlUtils.add(btnHuyDon);

        JPanel pnlBotGroup = new JPanel(new BorderLayout(0, 8));
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
            if (cartData.get(dataIndex).isDaPhucVu()) {
                JOptionPane.showMessageDialog(this, "Không thể xóa món đã thông báo cho bếp!\nVui lòng sử dụng tính năng Tách món nếu cần chuyển món qua bàn khác.", "Lỗi Xoá Món", JOptionPane.WARNING_MESSAGE);
                return;
            }
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

        // Đếm số món mới chưa gọi
        long soMonMoi = cartData.stream().filter(item -> !item.isDaPhucVu()).count();
        if (soMonMoi == 0) {
            JOptionPane.showMessageDialog(this, "Không có món mới nào cần gọi!\nTất cả các món đã được báo bếp trước đó.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Hiện xác nhận trước khi gọi món
        StringBuilder confirmMsg = new StringBuilder();
        confirmMsg.append("<html><b>Xác nhận gọi ").append(soMonMoi).append(" món mới:</b><br><br>");
        for (CartItem item : cartData) {
            if (!item.isDaPhucVu()) {
                String sizeStr = item.getSize().getTenSize().equalsIgnoreCase("Thường") ? "" : " (" + item.getSize().getTenSize() + ")";
                confirmMsg.append("  • ").append(item.getMon().getTenMon()).append(sizeStr)
                          .append(" x").append(item.getSoLuong()).append("<br>");
            }
        }
        confirmMsg.append("<br>Bạn có chắc chắn muốn gọi món không?<br>");
        confirmMsg.append("<i style='color:gray;'>Ấn 'No' để quay lại sửa đơn.</i></html>");

        int xacNhan = JOptionPane.showConfirmDialog(this,
                confirmMsg.toString(), "Xác nhận gọi món",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) {
            return; // Quay lại tiếp tục order / sửa
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

        // Thu gọn giỏ hàng: Gộp các món giống nhau (sau khi tất cả đều đã chuyển thành Đã Phục Vụ)
        List<CartItem> newCartData = new ArrayList<>();
        for (CartItem item : cartData) {
            boolean merged = false;
            for (CartItem existing : newCartData) {
                if (existing.isIdentical(item)) {
                    existing.setSoLuong(existing.getSoLuong() + item.getSoLuong());
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                newCartData.add(item);
            }
        }
        cartData = newCartData;
        
        if (performSaveOrder()) {
            renderCartTable(); // Cập nhật lại UI để chuyển chữ (Mới) -> (Đã báo bếp) và thể hiện sự được gộp
            JOptionPane.showMessageDialog(this, "Đã gọi món thành công!",
                    "Gọi Món", JOptionPane.INFORMATION_MESSAGE);
            // KHÔNG tự động back: nhân viên cần tiếp tục thao tác trên bàn này
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

        // Kiểm tra xem đã có đồ ăn gọi chưa
        for (CartItem item : cartData) {
            if (item.isDaPhucVu()) {
                JOptionPane.showMessageDialog(this, "Không thể hủy toàn bộ hóa đơn vì đã có món được chế biến (Đã báo bếp).\nVui lòng sử dụng tính năng Chuyển bàn hoặc Tách món nếu cần thiết.", "Lỗi Huỷ Đơn", JOptionPane.WARNING_MESSAGE);
                return;
            }
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

    private void handleMenuTuyChonBan(int actionType) {
        if (currentDonHang == null || !"DANG_PHUC_VU".equals(currentDonHang.getTrangThai().name())) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể đổi/gộp bàn cho đơn hàng đã [Gửi Bếp]!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window win = SwingUtilities.getWindowAncestor(this);
        if (!(win instanceof JFrame)) return;

        if (actionType == 1) { // Chuyển bàn
            ui.dialog.TransferTableDialog dlg = new ui.dialog.TransferTableDialog((JFrame) win, currentBan, currentDonHang, 1);
            dlg.setVisible(true);
            if (dlg.isSuccess()) {
                // Sau khi chuyển: refresh lại trạng thái rồi quay về sơ đồ bàn
                if (onBackAction != null) onBackAction.run();
            }
        } else if (actionType == 2) { // Ghép bàn
            ui.dialog.TransferTableDialog dlg = new ui.dialog.TransferTableDialog((JFrame) win, currentBan, currentDonHang, 2);
            dlg.setVisible(true);
            if (dlg.isSuccess()) {
                // Sau khi gộp: bàn nguồn bị xóa -> quay về sơ đồ
                if (onBackAction != null) onBackAction.run();
            }
        } else if (actionType == 3) { // Tách món
            ui.dialog.TransferItemsDialog dlg = new ui.dialog.TransferItemsDialog((JFrame) win, currentBan, currentDonHang, cartData);
            dlg.setVisible(true);
            if (dlg.isSuccess()) {
                // Sau khi tách: reload giỏ hàng của bàn nguồn
                cartData = orderController.loadCart(currentDonHang.getMaDonHang());
                renderCartTable();
                JOptionPane.showMessageDialog(this, "Tách món thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}