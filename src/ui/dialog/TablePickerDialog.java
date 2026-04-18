package ui.dialog;

import controller.TableController;
import entity.Ban;
import entity.KhuVuc;
import enums.TrangThaiBan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialog chọn bàn dạng sơ đồ khu vực (2 bước: Khu Vực → Bàn).
 * 
 * Mode:
 *   MODE_TRONG      = chỉ hiện bàn trống  (dùng cho Chuyển bàn)
 *   MODE_CO_KHACH   = chỉ hiện bàn đang có khách (dùng cho Gộp bàn, Tách món → bàn đích có khách)
 *   MODE_ALL        = hiện cả trống lẫn có khách (dùng cho Tách món)
 */
public class TablePickerDialog extends JDialog {

    public static final int MODE_TRONG    = 1;
    public static final int MODE_CO_KHACH = 2;
    public static final int MODE_ALL      = 3;

    private final TableController tableController;
    private final int mode;
    private final String excludeMaBan; // bàn nguồn – không cho chọn

    private Ban selectedBan = null;

    private CardLayout cardLayout;
    private JPanel cardContainer;
    private JPanel khuVucGrid;
    private JPanel banGrid;
    private JLabel lblBanHeader;

    // Màu sắc
    private static final Color C_BG     = new Color(245, 247, 250);
    private static final Color C_WHITE  = Color.WHITE;
    private static final Color C_BROWN  = new Color(113, 76, 52);
    private static final Color C_GREEN  = new Color(39, 174, 96);
    private static final Color C_RED    = new Color(231, 76, 60);
    private static final Color C_GRAY   = new Color(150, 150, 150);
    private static final Color C_BORDER = new Color(232, 232, 232);

    public TablePickerDialog(JFrame parent, String title, int mode, String excludeMaBan) {
        super(parent, title, true);
        this.tableController = new TableController();
        this.mode = mode;
        this.excludeMaBan = excludeMaBan;

        setSize(800, 560);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
        loadKhuVucView();
    }

    // ══════════════════════════════════════════════════════════════════════
    // BUILD UI
    // ══════════════════════════════════════════════════════════════════════

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);

        // Header dialog
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_BROWN);
        header.setPreferredSize(new Dimension(0, 54));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JButton btnBack = new JButton("⬅ Khu Vực");
        btnBack.setFont(new Font("Roboto", Font.BOLD, 13));
        btnBack.setBackground(new Color(90, 60, 40));
        btnBack.setForeground(C_WHITE);
        btnBack.putClientProperty("JButton.buttonArc", 8);
        btnBack.putClientProperty("JButton.borderWidth", 0);
        btnBack.setFocusable(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            loadKhuVucView();
            cardLayout.show(cardContainer, "KHU_VUC");
        });
        header.add(btnBack, BorderLayout.WEST);

        lblBanHeader = new JLabel(getTitle(), SwingConstants.CENTER);
        lblBanHeader.setFont(new Font("Roboto", Font.BOLD, 16));
        lblBanHeader.setForeground(C_WHITE);
        header.add(lblBanHeader, BorderLayout.CENTER);

        // Hướng dẫn chọn bàn
        String hint = switch (mode) {
            case MODE_TRONG    -> "Chọn bàn trống làm đích";
            case MODE_CO_KHACH -> "Chọn bàn đang có khách để gộp";
            default            -> "Chọn bàn đích";
        };
        JLabel lblHint = new JLabel(hint);
        lblHint.setFont(new Font("Roboto", Font.ITALIC, 12));
        lblHint.setForeground(new Color(220, 200, 180));
        header.add(lblHint, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // Card container
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setOpaque(false);

        // View 1: Khu Vực
        JPanel khuVucView = new JPanel(new BorderLayout());
        khuVucView.setOpaque(false);
        khuVucView.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblKV = new JLabel("Chọn Khu Vực");
        lblKV.setFont(new Font("Roboto", Font.BOLD, 18));
        lblKV.setForeground(C_BROWN);
        lblKV.setBorder(new EmptyBorder(0, 0, 10, 0));
        khuVucView.add(lblKV, BorderLayout.NORTH);

        khuVucGrid = new JPanel(new utils.WrapLayout(FlowLayout.LEFT, 20, 20));
        khuVucGrid.setOpaque(false);

        JScrollPane kvScroll = new JScrollPane(khuVucGrid);
        kvScroll.setBorder(null);
        kvScroll.setOpaque(false);
        kvScroll.getViewport().setOpaque(false);
        kvScroll.getVerticalScrollBar().setUnitIncrement(16);
        khuVucView.add(kvScroll, BorderLayout.CENTER);

        cardContainer.add(khuVucView, "KHU_VUC");

        // View 2: Bàn
        JPanel banView = new JPanel(new BorderLayout());
        banView.setOpaque(false);
        banView.setBorder(new EmptyBorder(15, 15, 15, 15));

        banGrid = new JPanel(new utils.WrapLayout(FlowLayout.LEFT, 15, 15));
        banGrid.setOpaque(false);

        JScrollPane banScroll = new JScrollPane(banGrid);
        banScroll.setBorder(null);
        banScroll.setOpaque(false);
        banScroll.getViewport().setOpaque(false);
        banScroll.getVerticalScrollBar().setUnitIncrement(16);
        banView.add(banScroll, BorderLayout.CENTER);

        cardContainer.add(banView, "BAN");

        root.add(cardContainer, BorderLayout.CENTER);

        // Footer: nút Hủy
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.setBackground(C_WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Roboto", Font.PLAIN, 13));
        btnCancel.setFocusable(false);
        btnCancel.addActionListener(e -> dispose());
        footer.add(btnCancel);

        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ══════════════════════════════════════════════════════════════════════
    // LOAD DATA
    // ══════════════════════════════════════════════════════════════════════

    private void loadKhuVucView() {
        khuVucGrid.removeAll();
        List<KhuVuc> dsKV = tableController.getDanhSachKhuVuc();
        for (KhuVuc kv : dsKV) {
            khuVucGrid.add(createKhuVucCard(kv));
        }
        khuVucGrid.revalidate();
        khuVucGrid.repaint();
    }

    private void loadBanView(KhuVuc kv) {
        lblBanHeader.setText(kv.getTenKhuVuc());
        banGrid.removeAll();
        List<Ban> dsBan = tableController.getBanByKhuVuc(kv.getMaKhuVuc());
        int count = 0;
        for (Ban b : dsBan) {
            if (enums.TrangThaiBan.TAM_NGUNG.equals(b.getTrangThai())) continue;
            if (b.getMaBan().equals(excludeMaBan)) continue;
            if (!isBanPhuHopMode(b)) continue;
            banGrid.add(createBanCard(b));
            count++;
        }
        if (count == 0) {
            JLabel lbl = new JLabel("<html><center>Không có bàn phù hợp<br>trong khu vực này.</center></html>", SwingConstants.CENTER);
            lbl.setFont(new Font("Roboto", Font.ITALIC, 14));
            lbl.setForeground(C_GRAY);
            lbl.setPreferredSize(new Dimension(400, 80));
            banGrid.add(lbl);
        }
        banGrid.revalidate();
        banGrid.repaint();
    }

    private boolean isBanPhuHopMode(Ban b) {
        return switch (mode) {
            case MODE_TRONG    -> b.getTrangThai() == TrangThaiBan.TRONG;
            case MODE_CO_KHACH -> b.getTrangThai() == TrangThaiBan.CO_KHACH;
            default            -> b.getTrangThai() == TrangThaiBan.TRONG
                               || b.getTrangThai() == TrangThaiBan.CO_KHACH;
        };
    }

    // ══════════════════════════════════════════════════════════════════════
    // CARDS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel createKhuVucCard(KhuVuc kv) {
        int totalBan = tableController.countBanByKhuVuc(kv.getMaKhuVuc());
        int banTrong = tableController.countBanTrongByKhuVuc(kv.getMaKhuVuc());

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(C_WHITE);
        card.setPreferredSize(new Dimension(210, 130));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1),
            new EmptyBorder(14, 16, 14, 16)));
        card.putClientProperty("JComponent.arc", 14);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblName = new JLabel(kv.getTenKhuVuc());
        lblName.setFont(new Font("Roboto", Font.BOLD, 17));
        lblName.setForeground(new Color(26, 26, 26));

        JLabel lblStat = new JLabel(banTrong + " trống / " + totalBan + " bàn");
        lblStat.setFont(new Font("Roboto", Font.PLAIN, 12));
        lblStat.setForeground(C_GRAY);

        card.add(lblName, BorderLayout.CENTER);
        card.add(lblStat, BorderLayout.SOUTH);

        Color borderDefault = C_BORDER;
        Color borderHover = C_BROWN;

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderHover, 2),
                    new EmptyBorder(13, 15, 13, 15)));
                card.setBackground(new Color(253, 250, 247));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderDefault, 1),
                    new EmptyBorder(14, 16, 14, 16)));
                card.setBackground(C_WHITE);
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                loadBanView(kv);
                cardLayout.show(cardContainer, "BAN");
            }
        });

        return card;
    }

    private JPanel createBanCard(Ban ban) {
        boolean isTrong = ban.getTrangThai() == TrangThaiBan.TRONG;
        Color dotColor    = isTrong ? C_GREEN : C_RED;
        String statusText = isTrong ? "Trống" : "Đang phục vụ";
        Color borderColor = isTrong ? new Color(200, 240, 210) : new Color(250, 210, 210);
        Color bgColor     = isTrong ? new Color(250, 255, 253) : new Color(255, 252, 252);

        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(150, 110));
        card.setBackground(bgColor);
        card.putClientProperty("JComponent.arc", 14);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            new EmptyBorder(12, 12, 12, 12)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Top: chấm trạng thái + tên bàn
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlTop.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor);
                g2.fillOval(0, 4, 10, 10);
            }
        };
        dot.setPreferredSize(new Dimension(10, 18));
        dot.setOpaque(false);

        JLabel lblName = new JLabel(ban.getSoBan());
        lblName.setFont(new Font("Roboto", Font.BOLD, 16));
        lblName.setForeground(new Color(26, 26, 26));
        pnlTop.add(dot);
        pnlTop.add(lblName);

        JLabel lblStatus = new JLabel(statusText);
        lblStatus.setFont(new Font("Roboto", Font.BOLD, 11));
        lblStatus.setForeground(dotColor);

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(lblStatus, BorderLayout.CENTER);

        String borderHex = String.format("#%02x%02x%02x", borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue());

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_BROWN, 2),
                    new EmptyBorder(11, 11, 11, 11)));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, 1),
                    new EmptyBorder(12, 12, 12, 12)));
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedBan = ban;
                dispose(); // Đóng dialog, trả về selectedBan
            }
        });

        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    // RESULT
    // ══════════════════════════════════════════════════════════════════════

    /** Trả về bàn được chọn, null nếu user hủy */
    public Ban getSelectedBan() {
        return selectedBan;
    }
}
