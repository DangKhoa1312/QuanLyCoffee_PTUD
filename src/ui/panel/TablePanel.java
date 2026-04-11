package ui.panel;

import controller.OrderController;
import controller.TableController;
import entity.Ban;
import entity.DonHang;
import entity.KhuVuc;
import enums.TrangThaiBan;
import ui.dialog.TakeawayListDialog;
import utils.OrderManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Giao diện 2 bước (Khu Vực & Bàn) nâng cấp FlatLaf.
 */
public class TablePanel extends JPanel {

    private final TableController tableController;
    private final OrderController orderController;

    private CardLayout cardLayout;
    private JPanel cardContainer;

    // View 1: Khu vực
    private JPanel khuVucGrid;

    // View 2: Bàn
    private JPanel banGrid;
    private JComboBox<KhuVucItem> cbKhuVucFilter;
    private boolean isUpdatingCombo = false;
    
    // Khu vực đang chọn
    private KhuVuc currentKhuVuc;

    private TableClickListener clickListener;

    public interface TableClickListener {
        void onTableClicked(Ban ban, DonHang donHangHienTai);
    }

    public void setTableClickListener(TableClickListener listener) {
        this.clickListener = listener;
    }

    public TablePanel() {
        tableController = new TableController();
        orderController = new OrderController();
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250)); // Nền xám nhạt theo design
        initUI();
        loadKhuVucView();
    }

    private void initUI() {
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setOpaque(false);

        // ═══════════════ VIEW 1: KHU VỰC ═══════════════
        JPanel khuVucView = new JPanel(new BorderLayout());
        khuVucView.setOpaque(false);
        khuVucView.add(createKhuVucHeader(), BorderLayout.NORTH);

        khuVucGrid = new JPanel(new utils.WrapLayout(FlowLayout.LEFT, 25, 25));
        khuVucGrid.setOpaque(false);
        khuVucGrid.setBorder(new EmptyBorder(10, 25, 25, 25));

        JScrollPane kvScroll = new JScrollPane(khuVucGrid);
        kvScroll.setBorder(BorderFactory.createEmptyBorder());
        kvScroll.getVerticalScrollBar().setUnitIncrement(16);
        kvScroll.setOpaque(false);
        kvScroll.getViewport().setOpaque(false);
        khuVucView.add(kvScroll, BorderLayout.CENTER);

        cardContainer.add(khuVucView, "KHU_VUC");

        // ═══════════════ VIEW 2: BÀN ═══════════════
        JPanel banView = new JPanel(new BorderLayout());
        banView.setOpaque(false);
        banView.add(createBanHeader(), BorderLayout.NORTH);

        banGrid = new JPanel(new utils.WrapLayout(FlowLayout.LEFT, 20, 20));
        banGrid.setOpaque(false);
        banGrid.setBorder(new EmptyBorder(10, 25, 25, 25));

        JScrollPane banScroll = new JScrollPane(banGrid);
        banScroll.setBorder(BorderFactory.createEmptyBorder());
        banScroll.getVerticalScrollBar().setUnitIncrement(16);
        banScroll.setOpaque(false);
        banScroll.getViewport().setOpaque(false);
        banView.add(banScroll, BorderLayout.CENTER);

        cardContainer.add(banView, "BAN");

        add(cardContainer, BorderLayout.CENTER);
    }

    // ── HEADERS ──

    private JPanel createKhuVucHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 30, 0, 30));

        JPanel pnlTitle = new JPanel();
        pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
        pnlTitle.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Quản Lý Khu Vực");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 28));
        lblTitle.setForeground(new Color(113, 76, 52)); // Nâu
        
        JLabel lblSub = new JLabel("Quản lý các khu vực phục vụ trong quán.");
        lblSub.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblSub.setForeground(new Color(150, 150, 150));
        
        pnlTitle.add(lblTitle);
        pnlTitle.add(Box.createVerticalStrut(5));
        pnlTitle.add(lblSub);
        
        JButton btnRefresh = new JButton(" ↻ Làm mới");
        btnRefresh.setFont(new Font("Roboto", Font.BOLD, 14));
        btnRefresh.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,15,8,15");
        btnRefresh.addActionListener(e -> loadKhuVucView());
        
        // Group refresh button on right side
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(btnRefresh);

        header.add(pnlTitle, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createBanHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 30, 0, 30));

        // Part Title and Legend
        JPanel pnlTitleArea = new JPanel(new BorderLayout(0, 15));
        pnlTitleArea.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý Bàn");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 28));
        lblTitle.setForeground(new Color(113, 76, 52));

        JLabel lblSub = new JLabel("Xem trạng thái bàn theo từng khu vực.");
        lblSub.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblSub.setForeground(new Color(150, 150, 150));
        
        JPanel pnlTitleText = new JPanel();
        pnlTitleText.setLayout(new BoxLayout(pnlTitleText, BoxLayout.Y_AXIS));
        pnlTitleText.setOpaque(false);
        pnlTitleText.add(lblTitle);
        pnlTitleText.add(Box.createVerticalStrut(5));
        pnlTitleText.add(lblSub);
        
        JPanel legendRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        legendRow.setOpaque(false);
        legendRow.add(createLegend("Trống", new Color(39, 174, 96))); // Xanh lá
        legendRow.add(createLegend("Có khách", new Color(231, 76, 60))); // Đỏ
        legendRow.add(createLegend("Đã đặt trước", new Color(243, 156, 18))); // Vàng
        
        pnlTitleArea.add(pnlTitleText, BorderLayout.NORTH);
        pnlTitleArea.add(legendRow, BorderLayout.SOUTH);
        
        // Part Controls
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlControls.setOpaque(false);

        JButton btnBack = new JButton("⬅ Quản Lý Khu Vực");
        btnBack.setFont(new Font("Roboto", Font.BOLD, 14));
        btnBack.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,15,8,15");
        btnBack.addActionListener(e -> {
            loadKhuVucView();
            cardLayout.show(cardContainer, "KHU_VUC");
        });
        
        cbKhuVucFilter = new JComboBox<>();
        cbKhuVucFilter.setFont(new Font("Roboto", Font.PLAIN, 14));
        cbKhuVucFilter.setPreferredSize(new Dimension(150, 40));
        cbKhuVucFilter.addActionListener(e -> {
            if (isUpdatingCombo) return;
            KhuVucItem item = (KhuVucItem) cbKhuVucFilter.getSelectedItem();
            if (item != null && item.khuVuc != null) {
                loadBanViewInternal(item.khuVuc);
            }
        });
        
        JButton btnRefresh = new JButton(" ↻ Làm mới");
        btnRefresh.setFont(new Font("Roboto", Font.BOLD, 14));
        btnRefresh.putClientProperty("FlatLaf.style", "arc: 10; margin: 8,15,8,15; background: #eef2fb");
        btnRefresh.addActionListener(e -> {
            if(currentKhuVuc != null) loadBanViewInternal(currentKhuVuc);
        });

        pnlControls.add(btnBack);
        pnlControls.add(cbKhuVucFilter);
        pnlControls.add(btnRefresh);

        header.add(pnlTitleArea, BorderLayout.WEST);
        header.add(pnlControls, BorderLayout.EAST);
        return header;
    }

    private JPanel createLegend(String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 2, 12, 12);
            }
        };
        dot.setPreferredSize(new Dimension(12, 16));
        dot.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", Font.PLAIN, 13));
        lbl.setForeground(new Color(100, 100, 100));

        p.add(dot);
        p.add(lbl);
        return p;
    }

    // ── KHU VỰC METHODS ──

    private void loadKhuVucView() {
        this.currentKhuVuc = null;
        khuVucGrid.removeAll();

        List<KhuVuc> dsKV = tableController.getDanhSachKhuVuc();
        for (KhuVuc kv : dsKV) {
            if ("KV003".equals(kv.getMaKhuVuc()) || kv.getTenKhuVuc().toLowerCase().contains("mang về")) {
                continue; 
            }
            khuVucGrid.add(createKhuVucCard(kv));
        }

        khuVucGrid.add(createMangVeCard());

        khuVucGrid.revalidate();
        khuVucGrid.repaint();
    }

    private JPanel createKhuVucCard(KhuVuc kv) {
        int totalBan = tableController.countBanByKhuVuc(kv.getMaKhuVuc());
        int banTrong = tableController.countBanTrongByKhuVuc(kv.getMaKhuVuc());
        int banCoKhach = totalBan - banTrong;

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(300, 190));
        setCardStyleWithPadding(card, 20, "#e8e8e8", 15, 20);

        // Top
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        JLabel lblName = new JLabel(kv.getTenKhuVuc());
        lblName.setFont(new Font("Roboto", Font.BOLD, 22));
        lblName.setForeground(new Color(26, 26, 26));

        // Format Badge HTML
        JLabel lblBadge = new JLabel("<html><div style='padding: 2px 8px; border-radius: 10px; background-color: #E3FCEF; color: #108043;'><b>Hoạt động</b></div></html>");
        
        pnlTop.add(lblName, BorderLayout.WEST);
        pnlTop.add(lblBadge, BorderLayout.EAST);

        // Middle
        JPanel pnlMid = new JPanel(new BorderLayout());
        pnlMid.setOpaque(false);
        
        String moTa = (kv.getMoTa() != null && !kv.getMoTa().isEmpty()) ? kv.getMoTa() : "Khu vực phục vụ khách";
        JLabel lblDesc = new JLabel("<html><div style='width: 250px;'>" + moTa + "</div></html>");
        lblDesc.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblDesc.setForeground(new Color(150, 150, 150));
        lblDesc.setBorder(new EmptyBorder(10, 0, 15, 0));
        
        JPanel pnlStats = new JPanel(new GridLayout(1, 2));
        pnlStats.setOpaque(false);
        
        JPanel pnlTotal = new JPanel(new BorderLayout());
        pnlTotal.setOpaque(false);
        JLabel lblTotalTitle = new JLabel("TỔNG BÀN");
        lblTotalTitle.setFont(new Font("Roboto", Font.BOLD, 11));
        lblTotalTitle.setForeground(new Color(150, 150, 150));
        JLabel lblTotalVal = new JLabel(String.valueOf(totalBan));
        lblTotalVal.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTotalVal.setForeground(new Color(26, 26, 26));
        pnlTotal.add(lblTotalTitle, BorderLayout.NORTH);
        pnlTotal.add(lblTotalVal, BorderLayout.CENTER);
        
        JPanel pnlOccupied = new JPanel(new BorderLayout());
        pnlOccupied.setOpaque(false);
        JLabel lblOccuTitle = new JLabel("CÓ KHÁCH");
        lblOccuTitle.setFont(new Font("Roboto", Font.BOLD, 11));
        lblOccuTitle.setForeground(new Color(150, 150, 150));
        JLabel lblOccuVal = new JLabel(String.valueOf(banCoKhach));
        lblOccuVal.setFont(new Font("Roboto", Font.BOLD, 22));
        lblOccuVal.setForeground(banCoKhach > 0 ? new Color(231, 76, 60) : new Color(26, 26, 26));
        pnlOccupied.add(lblOccuTitle, BorderLayout.NORTH);
        pnlOccupied.add(lblOccuVal, BorderLayout.CENTER);
        
        pnlStats.add(pnlTotal);
        pnlStats.add(pnlOccupied);

        pnlMid.add(lblDesc, BorderLayout.NORTH);
        pnlMid.add(pnlStats, BorderLayout.CENTER);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                setCardStyleWithPadding(card, 20, "#714c34", 15, 20);
                card.setBackground(new Color(252, 250, 248));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                setCardStyleWithPadding(card, 20, "#e8e8e8", 15, 20);
                card.setBackground(Color.WHITE);
            }
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                loadBanView(kv);
                cardLayout.show(cardContainer, "BAN");
            }
        });

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(pnlMid, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createMangVeCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(300, 190));
        setCardStyleWithPadding(card, 20, "#e8e8e8", 15, 20);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        JLabel lblName = new JLabel("Mang về");
        lblName.setFont(new Font("Roboto", Font.BOLD, 22));
        lblName.setForeground(new Color(26, 26, 26));

        JLabel lblBadge = new JLabel("<html><div style='padding: 2px 8px; border-radius: 10px; background-color: #E3FCEF; color: #108043;'><b>Hoạt động</b></div></html>");
        pnlTop.add(lblName, BorderLayout.WEST);
        pnlTop.add(lblBadge, BorderLayout.EAST);
        
        JPanel pnlMid = new JPanel(new BorderLayout());
        pnlMid.setOpaque(false);
        JLabel lblDesc = new JLabel("Khu vực phục vụ đơn mang về");
        lblDesc.setFont(new Font("Roboto", Font.PLAIN, 14));
        lblDesc.setForeground(new Color(150, 150, 150));
        lblDesc.setBorder(new EmptyBorder(10, 0, 15, 0));

        JPanel pnlStats = new JPanel(new GridLayout(1, 2));
        pnlStats.setOpaque(false);
        
        int countTakeaway = orderController.getOpenTakeawayOrders().size();
        
        JPanel pnlTotal = new JPanel(new BorderLayout());
        pnlTotal.setOpaque(false);
        JLabel lblTotalTitle = new JLabel("ĐƠN CHỜ");
        lblTotalTitle.setFont(new Font("Roboto", Font.BOLD, 11));
        lblTotalTitle.setForeground(new Color(150, 150, 150));
        JLabel lblTotalVal = new JLabel(String.valueOf(countTakeaway));
        lblTotalVal.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTotalVal.setForeground(countTakeaway > 0 ? new Color(231, 76, 60) : new Color(26, 26, 26));
        pnlTotal.add(lblTotalTitle, BorderLayout.NORTH);
        pnlTotal.add(lblTotalVal, BorderLayout.CENTER);
        
        pnlStats.add(pnlTotal);
        pnlStats.add(new JLabel()); // padding
        
        pnlMid.add(lblDesc, BorderLayout.NORTH);
        pnlMid.add(pnlStats, BorderLayout.CENTER);
        
        // Make the whole card clickable
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                setCardStyleWithPadding(card, 20, "#714c34", 15, 20);
                card.setBackground(new Color(252, 250, 248));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                setCardStyleWithPadding(card, 20, "#e8e8e8", 15, 20);
                card.setBackground(Color.WHITE);
            }
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (clickListener != null) {
                    Window win = SwingUtilities.getWindowAncestor(card);
                    if (win instanceof JFrame) {
                        Ban banMangVe = new Ban("MANG_VE", "MANG VỀ", null, 0, TrangThaiBan.TRONG);
                        List<DonHang> dsChoMangVe = orderController.getOpenTakeawayOrders();
                        TakeawayListDialog dlg = new TakeawayListDialog((JFrame) win, dsChoMangVe, orderController);
                        dlg.setVisible(true);

                        if (dlg.isCreateNew()) {
                            clickListener.onTableClicked(banMangVe, null);
                        } else if (dlg.getSelectedOrder() != null) {
                            clickListener.onTableClicked(banMangVe, dlg.getSelectedOrder());
                        }
                        loadKhuVucView(); 
                    }
                }
            }
        });
        
        card.add(pnlTop, BorderLayout.NORTH);
        card.add(pnlMid, BorderLayout.CENTER);
        
        return card;
    }

    // ── BÀN METHODS ──

    class KhuVucItem {
        KhuVuc khuVuc;
        KhuVucItem(KhuVuc k) { this.khuVuc = k; }
        @Override public String toString() { return khuVuc.getTenKhuVuc(); }
    }

    private void loadBanView(KhuVuc kv) {
        this.currentKhuVuc = kv;
        // Populate combo
        isUpdatingCombo = true;
        cbKhuVucFilter.removeAllItems();
        List<KhuVuc> dsKV = tableController.getDanhSachKhuVuc();
        for (KhuVuc k : dsKV) {
            if ("KV003".equals(k.getMaKhuVuc()) || k.getTenKhuVuc().toLowerCase().contains("mang về")) continue;
            KhuVucItem item = new KhuVucItem(k);
            cbKhuVucFilter.addItem(item);
            if (k.getMaKhuVuc().equals(kv.getMaKhuVuc())) {
                cbKhuVucFilter.setSelectedItem(item);
            }
        }
        isUpdatingCombo = false;
        
        loadBanViewInternal(kv);
    }
    
    private void loadBanViewInternal(KhuVuc kv) {
        this.currentKhuVuc = kv;
        banGrid.removeAll();
        
        List<Ban> dsBan = tableController.getBanByKhuVuc(kv.getMaKhuVuc());
        for (Ban b : dsBan) {
            if (TrangThaiBan.TAM_NGUNG.equals(b.getTrangThai())) {
                continue;
            }
            banGrid.add(createTableCard(b));
        }
        banGrid.revalidate();
        banGrid.repaint();
    }

    private JPanel createTableCard(Ban ban) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(190, 140));
        card.setBackground(new Color(250, 255, 253));
        
        Color dotColor;
        String statusText;
        Color borderColor;

        switch (ban.getTrangThai()) {
            case TRONG:
                dotColor = new Color(39, 174, 96);
                statusText = "Trống";
                borderColor = new Color(200, 240, 210);
                break;
            case CO_KHACH:
                dotColor = new Color(231, 76, 60);
                statusText = "Đang phục vụ";
                borderColor = new Color(250, 210, 210);
                card.setBackground(new Color(255, 252, 252));
                break;
            case DA_DAT_TRUOC:
                dotColor = new Color(243, 156, 18);
                statusText = "Đã đặt trước";
                borderColor = new Color(250, 240, 200);
                break;
            default:
                dotColor = Color.GRAY;
                statusText = "Khác";
                borderColor = Color.LIGHT_GRAY;
        }
        
        String hexColor = String.format("#%02x%02x%02x", borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue());
        setCardStyleWithPadding(card, 16, hexColor, 15, 15);

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTop.setOpaque(false);
        
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor);
                g2.fillOval(0, 5, 12, 12);
            }
        };
        dot.setPreferredSize(new Dimension(12, 20));
        dot.setOpaque(false);
        
        JLabel lblName = new JLabel(ban.getSoBan());
        lblName.setFont(new Font("Roboto", Font.BOLD, 18));
        lblName.setForeground(new Color(26, 26, 26));
        
        pnlTop.add(dot);
        pnlTop.add(lblName);
        
        JLabel lblStatus = new JLabel(statusText);
        lblStatus.setFont(new Font("Roboto", Font.BOLD, 13));
        lblStatus.setForeground(dotColor);
        lblStatus.setBorder(new EmptyBorder(5, 0, 10, 0));
        
        JPanel pnlBot = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlBot.setOpaque(false);
        
        JLabel lblSeats = new JLabel("👥 " + (ban.getSucChua() > 0 ? ban.getSucChua() : 4) + " người");
        lblSeats.setFont(new Font("Roboto", Font.PLAIN, 12));
        lblSeats.setForeground(new Color(120, 120, 120));
        
        JLabel lblArea = new JLabel("📍 " + (currentKhuVuc != null ? currentKhuVuc.getTenKhuVuc() : "Khu vực"));
        lblArea.setFont(new Font("Roboto", Font.PLAIN, 12));
        lblArea.setForeground(new Color(120, 120, 120));
        
        pnlBot.add(lblSeats);
        pnlBot.add(lblArea);

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(lblStatus, BorderLayout.CENTER);
        card.add(pnlBot, BorderLayout.SOUTH);
        
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                setCardStyleWithPadding(card, 16, "#714c34", 15, 15);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                setCardStyleWithPadding(card, 16, hexColor, 15, 15);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (clickListener == null) return;

                // Bug 2: Phân nhánh xử lý đúng theo trạng thái bàn
                if (ban.getTrangThai() == TrangThaiBan.CO_KHACH) {
                    // Bàn đang có khách → lấy đơn hàng đang mở và tiếp tục phục vụ
                    DonHang dh = tableController.getDonHangDangMo(ban.getMaBan());
                    if (dh != null) {
                        clickListener.onTableClicked(ban, dh);
                    } else {
                        // Không tìm được đơn trên RAM (có thể app restart) → tạo mới
                        int xn = JOptionPane.showConfirmDialog(
                            SwingUtilities.getWindowAncestor(card),
                            "Bàn đang có khách nhưng không tìm thấy đơn hàng đang mở.\nBạn có muốn tạo đơn mới cho bàn này không?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (xn == JOptionPane.YES_OPTION) {
                            clickListener.onTableClicked(ban, null);
                        }
                    }
                } else if (ban.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC) {
                    // Bàn đã đặt trước → thông báo, hỏi có mở không
                    int xn = JOptionPane.showConfirmDialog(
                        SwingUtilities.getWindowAncestor(card),
                        "Bàn \"" + ban.getSoBan() + "\" đã được đặt trước.\nBạn có muốn mở phục vụ cho bàn này không?",
                        "Bàn đã đặt trước", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (xn == JOptionPane.YES_OPTION) {
                        DonHang dh = tableController.getDonHangDangMo(ban.getMaBan());
                        clickListener.onTableClicked(ban, dh);
                    }
                } else {
                    // Bàn TRONG hoặc trạng thái khác → mở bình thường
                    DonHang dh = tableController.getDonHangDangMo(ban.getMaBan());
                    clickListener.onTableClicked(ban, dh);
                }
            }
        });

        return card;
    }

    public void refreshData() {
        if (currentKhuVuc != null) {
            loadBanView(currentKhuVuc);
            cardLayout.show(cardContainer, "BAN");
        } else {
            loadKhuVucView();
            cardLayout.show(cardContainer, "KHU_VUC");
        }
    }

    /**
     * Helper cực kỳ quan trọng: FlatLaf style 'border:' sẽ ghi đè hoàn toàn setBorder().
     * Phải dùng CompoundBorder để duy trì khoảng cách nội dung (padding) không bị nhảy khi hover.
     */
    private void setCardStyleWithPadding(JPanel p, int arc, String hexColor, int padV, int padH) {
        p.putClientProperty("FlatLaf.style", "arc: " + arc + "; border: 1,1,1,1," + hexColor + ";");
        p.setBorder(BorderFactory.createCompoundBorder(
            p.getBorder(), // Đây là FlatRoundBorder do FlatLaf vừa tạo ra
            new EmptyBorder(padV, padH, padV, padH)
        ));
    }
}
