package ui;

import dao.DatBanDAO;
import dao.impl.DatBanDAOImpl;
import entity.NhanVien;
import enums.TrangThaiDatBan;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccordionSidebar extends JPanel {

    // ── LIGHT THEME COLORS (Tailwind inspired) ──────────────────────────
    static final Color C_BG       = new Color(255, 255, 255);
    static final Color C_HOVER    = new Color(248, 250, 252); // slate-50
    static final Color C_ACTIVE   = new Color(239, 246, 255); // blue-50
    static final Color C_ACCENT   = new Color(59, 130, 246);  // blue-500
    static final Color C_TEXT     = new Color(30, 41, 59);    // slate-800
    static final Color C_MUTED    = new Color(100, 116, 139); // slate-500
    static final Color C_GROUP    = new Color(148, 163, 184); // slate-400
    static final Color C_DIVIDER  = new Color(241, 245, 249); // slate-100
    static final Color C_BADGE    = new Color(239, 68, 68);   // red-500

    public interface NavListener { void navigate(String cardKey); }

    private final JPanel contentBox;
    private final Map<String, SidebarItem> itemMap = new HashMap<String, AccordionSidebar.SidebarItem>();
    
    private JLabel lblName; // Để cập nhật tên sau khi sửa hồ sơ

    private NavListener navListener;
    private String activePage = "";
    private final List<SidebarItem> allItems = new ArrayList<>();

    private BadgePanel pnlBadgeDatBan;
    private Timer badgeTimer;

    public AccordionSidebar() {
        this.contentBox = new JPanel();
		jiconfont.swing.IconFontSwing.register(jiconfont.icons.FontAwesome.getIconFont());

        setLayout(new BorderLayout()); // CHIA 3 VÙNG: NORTH, CENTER, SOUTH
        setBackground(C_BG);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, C_DIVIDER)); 
        setPreferredSize(new Dimension(250, 0)); 
        build();
        startBadgeTimer();
    }

    public void setNavListener(NavListener l) { this.navListener = l; }

    public void setActivePage(String key) {
        this.activePage = key;
        for (SidebarItem item : allItems) {
            item.setActive(key.equals(item.cardKey));
        }
    }



    public void refreshBadges() {
        try {
            DatBanDAO dao = new DatBanDAOImpl();
            long n = dao.findConHieuLuc().stream().filter(d -> TrangThaiDatBan.CHO_XAC_NHAN.equals(d.getTrangThai())).count();
            if (pnlBadgeDatBan != null) pnlBadgeDatBan.setCount((int) n);
        } catch (Exception ignored) {}
    }

    private void build() {
        boolean isQL = SessionManager.isQuanLy();
        boolean isAdmin = SessionManager.isAdmin();

        // 1. NORTH: Brand & Avatar (Ghim đỉnh)
        JPanel pnlNorth = new JPanel();
        pnlNorth.setLayout(new BoxLayout(pnlNorth, BoxLayout.Y_AXIS));
        pnlNorth.setOpaque(false);
        pnlNorth.add(buildBrand());
        pnlNorth.add(buildUserPanel());
        pnlNorth.add(createDivider());
        add(pnlNorth, BorderLayout.NORTH);

        // 2. CENTER: Scrollable Menu
        JPanel pnlMenu = new JPanel();
        pnlMenu.setLayout(new BoxLayout(pnlMenu, BoxLayout.Y_AXIS));
        pnlMenu.setOpaque(false);

        pnlMenu.add(Box.createVerticalStrut(12)); 
        pnlMenu.add(makeItem("Tổng Quan", FontAwesome.HOME, "HOME", 0));
        pnlMenu.add(Box.createVerticalStrut(8)); 
        pnlMenu.add(createDivider());

        AccordionGroup gVH = new AccordionGroup("Vận Hành", true, 0);
        gVH.addItem(makeItem("Bán Hàng", FontAwesome.SHOPPING_BAG, "BAN_HANG", 1));
        SidebarItem itmDatBan = makeItem("Đặt Bàn", FontAwesome.CALENDAR_CHECK_O, "DAT_BAN", 1);
        pnlBadgeDatBan = new BadgePanel();
        itmDatBan.attachBadge(pnlBadgeDatBan);
        gVH.addItem(itmDatBan);
        gVH.addItem(makeItem("Hoá Đơn", FontAwesome.FILE_TEXT_O, "HOA_DON", 1));
        pnlMenu.add(gVH);

        if (isQL) {
            pnlMenu.add(createDivider());
            AccordionGroup gTL = new AccordionGroup("Thiết Lập", true, 0);
            AccordionGroup gTD = new AccordionGroup("Thực Đơn", true, 1);
            gTD.addItem(makeItem("Món & Size", FontAwesome.CUBE,         "ADMIN_MON", 2));
            gTD.addItem(makeItem("Topping",    FontAwesome.PUZZLE_PIECE, "ADMIN_TOPPING", 2));
            gTD.addItem(makeItem("Công Thức",  FontAwesome.FLASK,        "ADMIN_CONG_THUC", 2));
            gTL.addNestedGroup(gTD);

            if (isAdmin) {
                gTL.addItem(makeItem("Bảng Giá",  FontAwesome.MONEY,    "ADMIN_GIA", 1));
            }
            gTL.addItem(makeItem("Sơ Đồ Bàn", FontAwesome.TH_LARGE, "ADMIN_BAN", 1));
            gTL.addItem(makeItem("Cấu Hình", FontAwesome.COGS, "ADMIN_CAU_HINH", 1));
            pnlMenu.add(gTL);

            pnlMenu.add(createDivider());
            AccordionGroup gQT = new AccordionGroup("Quản Trị", true, 0);
            gQT.addItem(makeItem("Nhân Viên",   FontAwesome.USERS,     "ADMIN_NHAN_VIEN", 1));
            gQT.addItem(makeItem("Khách Hàng",  FontAwesome.ID_CARD_O, "KHACH_HANG", 1));
            if (isAdmin) {
                gQT.addItem(makeItem("Khuyến Mãi",  FontAwesome.GIFT,      "ADMIN_KHUYEN_MAI", 1));
            }
            gQT.addItem(makeItem("Quản Lý Kho", FontAwesome.ARCHIVE,   "ADMIN_KHO", 1));
            gQT.addItem(makeItem("Thống Kê",    FontAwesome.PIE_CHART, "THONG_KE", 1));
            pnlMenu.add(gQT);
        }

        pnlMenu.add(Box.createVerticalGlue()); // Hấp thụ khoảng trống dưới cùng

        JScrollPane scroll = new JScrollPane(pnlMenu);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(C_BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0)); 
        add(scroll, BorderLayout.CENTER);
    }

    private JLabel lblBrand;

    public void updateBrandName(String newName) {
        if (lblBrand != null) {
            lblBrand.setText(newName.toUpperCase());
        }
    }

    private JPanel buildBrand() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        p.setOpaque(false);
        fixHeight(p, 64);
        p.setBorder(new EmptyBorder(16, 24, 0, 24)); 
        
        JLabel icn = new JLabel(IconFontSwing.buildIcon(FontAwesome.COFFEE, 24, C_ACCENT));
        String tenQuan = utils.AppConfig.getInstance().getString("TEN_QUAN", "COFFEE 11:01");
        lblBrand = new JLabel(tenQuan.toUpperCase());
        lblBrand.setFont(new Font("Montserrat", Font.BOLD, 22));
        lblBrand.setForeground(new Color(15, 23, 42)); 
        p.add(icn);
        p.add(lblBrand);
        return p;
    }

    private JPanel buildUserPanel() {
        JPanel p = new JPanel(new BorderLayout(14, 0)) {
            private boolean hovered = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; setCursor(new Cursor(Cursor.HAND_CURSOR)); repaint(); }
                    @Override public void mouseExited(java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                    @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (navListener != null) navListener.navigate("ACTION_PROFILE");
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                if (hovered) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(241, 245, 249)); // hover bg
                    g2.fillRoundRect(12, 6, getWidth() - 24, getHeight() - 12, 12, 12);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 24, 16, 24)); 
        fixHeight(p, 70);

        NhanVien nv = SessionManager.getCurrentUser();
        String ten  = nv != null ? nv.getTenNV() : "User";

        JPanel ava = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(226, 232, 240)); 
                g2.fillRoundRect(0, 0, 42, 42, 12, 12);
                g2.dispose();
            }
        };
        ava.setOpaque(false);
        ava.setPreferredSize(new Dimension(42, 42));
        JLabel lblAvaIcon = new JLabel(IconFontSwing.buildIcon(FontAwesome.USER, 20, new Color(71, 85, 105)), SwingConstants.CENTER);
        ava.add(lblAvaIcon, BorderLayout.CENTER);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        lblName = new JLabel(ten);
        lblName.setFont(new Font("Roboto", Font.BOLD, 14));
        lblName.setForeground(C_TEXT);
        
        text.add(Box.createVerticalStrut(10)); 
        text.add(lblName);

        p.add(ava, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    public void updateUserName(String newName) {
        if (lblName != null) {
            lblName.setText(newName);
            lblName.revalidate();
            lblName.repaint();
        }
    }

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(8, 24, 16, 24)); 

        if (SessionManager.isCaDangMo()) {
            p.add(makeBottomBtn("Đóng Ca", FontAwesome.LOCK, new Color(254, 242, 242), new Color(220, 38, 38), "ACTION_DONG_CA"));
            p.add(Box.createVerticalStrut(8));
        }
        p.add(makeBottomBtn("Đăng Xuất", FontAwesome.SIGN_OUT, new Color(248, 250, 252), new Color(100, 116, 139), "ACTION_LOGOUT"));
        return p;
    }

    private JComponent createDivider() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(4, 24, 4, 24));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9)); // Chặn giãn nở!
        p.setPreferredSize(new Dimension(250, 9));
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(C_DIVIDER);
        sep.setBackground(C_BG);
        p.add(sep, BorderLayout.CENTER);
        return p;
    }

    private static void fixHeight(JComponent c, int h) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        c.setMinimumSize(new Dimension(250, h));
        c.setPreferredSize(new Dimension(250, h));
    }

    private SidebarItem makeItem(String text, FontAwesome icon, String key, int level) {
        SidebarItem item = new SidebarItem(text, icon, key, level);
        allItems.add(item);
        item.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { doNav(key); } });
        return item;
    }

    private BottomBtn makeBottomBtn(String text, FontAwesome icon, Color bg, Color fg, String actionKey) {
        BottomBtn btn = new BottomBtn(text, icon, bg, fg);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (navListener != null) navListener.navigate(actionKey); }
        });
        return btn;
    }

    private void doNav(String key) {
        setActivePage(key);
        if (navListener != null) navListener.navigate(key);
    }

    private void startBadgeTimer() {
        badgeTimer = new Timer(30_000, e -> refreshBadges());
        badgeTimer.setInitialDelay(800);
        badgeTimer.start();
    }

    class AccordionGroup extends JPanel {
        private final JPanel pnlContent;
        private final JLabel lblArrow;
        private final JPanel pnlHeader;
        private boolean expanded;
        private Timer animTimer;

        AccordionGroup(String title, boolean startExpanded, int level) {
            this.expanded = startExpanded;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            // KHÔNG setMaximumSize để nó không bị dãn dọc!

            pnlHeader = new JPanel(new BorderLayout());
            pnlHeader.setOpaque(false);
            pnlHeader.setBorder(new EmptyBorder(4, 24 + (level * 24), 4, 24)); 
            fixHeight(pnlHeader, 38);

            JLabel lblTitle = new JLabel(title.toUpperCase());
            lblTitle.setFont(new Font("Roboto", Font.BOLD, 13));
            lblTitle.setForeground(new Color(51, 65, 85)); // slate-700 (sậm hơn)

            lblArrow = new JLabel(startExpanded ? "▾" : "▸");
            lblArrow.setFont(new Font("Roboto", Font.BOLD, 14));
            lblArrow.setForeground(new Color(100, 116, 139));

            pnlHeader.add(lblTitle, BorderLayout.WEST);
            pnlHeader.add(lblArrow, BorderLayout.EAST);
            pnlHeader.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pnlHeader.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { toggle(); }
                @Override public void mouseEntered(MouseEvent e) { lblTitle.setForeground(C_TEXT); } 
                @Override public void mouseExited(MouseEvent e) { lblTitle.setForeground(C_GROUP); }
            });

            pnlContent = new JPanel();
            pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
            pnlContent.setOpaque(false);
            pnlContent.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (!startExpanded) {
                pnlContent.setPreferredSize(new Dimension(0, 0));
                pnlContent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
                pnlContent.setVisible(false);
            }

            add(pnlHeader);
            add(pnlContent);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        void addItem(SidebarItem item) { pnlContent.add(item); }
        void addNestedGroup(AccordionGroup nested) { pnlContent.add(nested); }

        void toggle() {
            expanded = !expanded;
            lblArrow.setText(expanded ? "▾" : "▸");
            int fullH = 0;
            for (Component c : pnlContent.getComponents()) fullH += c.getPreferredSize().height;
            final int targetH = expanded ? fullH : 0;
            final boolean opening = expanded;

            if (animTimer != null) animTimer.stop();
            if (opening) pnlContent.setVisible(true);

            animTimer = new Timer(13, ev -> {
                int cur = pnlContent.getPreferredSize().height;
                int diff = targetH - cur;
                int step = Math.max(2, Math.abs(diff) / 4);
                int next = opening ? Math.min(cur + step, targetH) : Math.max(cur - step, targetH);
                pnlContent.setPreferredSize(new Dimension(0, next));
                pnlContent.setMaximumSize(new Dimension(Integer.MAX_VALUE, next));
                revalidate();
                if (next == targetH) {
                    if (opening) {
                        pnlContent.setPreferredSize(null); // Giải phóng chiều cao cho phép group lồng nhau nở ra tự nhiên
                        pnlContent.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                    } else pnlContent.setVisible(false);
                    animTimer.stop();
                }
            });
            animTimer.start();
        }
    }

    class SidebarItem extends JPanel {
        final String cardKey;
        private boolean active = false;
        private Color currBg;
        private final JLabel lblText;
        private JLabel lblIcon;
        private final FontAwesome faIcon;

        SidebarItem(String text, FontAwesome icon, String cardKey, int level) {
            this.cardKey = cardKey;
            this.currBg  = C_BG; 
            this.faIcon  = icon;

            int h = level > 0 ? 40 : 46; 
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(0, 24 + (level * 20), 0, 24)); // Mỗi level lùi 20px
            fixHeight(this, h);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel inner = new JPanel(new GridBagLayout()); 
            inner.setOpaque(false);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 0, 16); 

            if (icon != null) {
                lblIcon = new JLabel(IconFontSwing.buildIcon(icon, 16, C_MUTED), SwingConstants.CENTER);
                lblIcon.setPreferredSize(new Dimension(20, 20)); 
                inner.add(lblIcon, gbc);
                gbc.gridx++;
            }

            gbc.insets = new Insets(0, 0, 0, 0); 
            lblText = new JLabel(text);
            lblText.setFont(new Font("Roboto", level > 0 ? Font.PLAIN : Font.BOLD, 14)); 
            lblText.setForeground(level > 0 ? C_MUTED : C_TEXT);
            inner.add(lblText, gbc);
            
            add(inner, BorderLayout.WEST);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if(!active) { currBg = C_HOVER; repaint(); } }
                @Override public void mouseExited(MouseEvent e)  { if(!active) { currBg = C_BG; repaint(); } }
            });
        }
        
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        void attachBadge(BadgePanel badge) {
            JPanel right = new JPanel(new GridBagLayout());
            right.setOpaque(false);
            right.add(badge);
            add(right, BorderLayout.EAST);
        }

        void setActive(boolean a) {
            this.active = a;
            this.currBg = a ? C_ACTIVE : C_BG;
            if (active) {
                lblText.setForeground(C_ACCENT);
                if (lblIcon != null && faIcon != null) lblIcon.setIcon(IconFontSwing.buildIcon(faIcon, 16, C_ACCENT));
            } else {
                lblText.setForeground(C_TEXT);
                if (lblIcon != null && faIcon != null) lblIcon.setIcon(IconFontSwing.buildIcon(faIcon, 16, C_MUTED));
            }
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (currBg != C_BG) {
                g2.setColor(currBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            if (active) {
                g2.setColor(C_ACCENT);
                g2.fillRect(0, 0, 4, getHeight());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class BadgePanel extends JPanel {
        private int count = 0;
        BadgePanel() { setOpaque(false); setPreferredSize(new Dimension(22, 22)); setVisible(false); }
        void setCount(int n) { this.count = n; setVisible(n > 0); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            if (count <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(C_BADGE);
            g2.fillRoundRect(0, 0, 22, 22, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Roboto", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            String s = count > 9 ? "9+" : String.valueOf(count);
            g2.drawString(s, (22 - fm.stringWidth(s)) / 2, 1 + (22 + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    class BottomBtn extends JPanel {
        private Color currBg;
        private final Color hoverBg;
        BottomBtn(String text, FontAwesome icon, Color bg, Color fg) {
            this.currBg = bg;
            this.hoverBg = new Color(Math.max(0, bg.getRed()-6), Math.max(0, bg.getGreen()-6), Math.max(0, bg.getBlue()-6));
            
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
            fixHeight(this, 42); 
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            add(new JLabel(IconFontSwing.buildIcon(icon, 16, fg)));
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("Roboto", Font.BOLD, 14));
            lbl.setForeground(fg);
            add(lbl);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { currBg = hoverBg; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { currBg = bg;  repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
        }
    }
}
