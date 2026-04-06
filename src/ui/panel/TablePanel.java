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
 * Sơ đồ lưới bàn: hiển thị các bàn theo khu vực với mã màu trạng thái.
 * Bao gồm: bàn ảo "Mang Về", tích hợp thông tin đặt bàn.
 */
public class TablePanel extends JPanel {

    private final TableController tableController;
    private final OrderController orderController;
    private JPanel gridPanel;
    private JTabbedPane tabbedKhuVuc;

    // Callback khi click vào bàn (để MainFrame mở OrderPanel)
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
        setBackground(new Color(245, 247, 250));
        initUI();
        loadData();
    }

    private void initUI() {
        // ── HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 25, 15, 25));

        JLabel lblTitle = new JLabel("\uD83C\uDFE0  S\u01A1 \u0110\u1ED3 B\u00E0n");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(26, 26, 46));
        header.add(lblTitle, BorderLayout.WEST);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        legend.setOpaque(false);
        legend.add(createLegend("Tr\u1ED1ng", new Color(39, 174, 96)));
        legend.add(createLegend("\u0110ang ph\u1EE5c v\u1EE5", new Color(231, 76, 60)));
        legend.add(createLegend("\u0110\u00E3 \u0111\u1EB7t", new Color(243, 156, 18)));
        header.add(legend, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── CONTENT ──
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 25, 25, 25));

        // Tabs
        tabbedKhuVuc = new JTabbedPane();
        tabbedKhuVuc.setFont(new Font("Roboto", Font.BOLD, 13));
        tabbedKhuVuc.addChangeListener(e -> {
            int idx = tabbedKhuVuc.getSelectedIndex();
            if (idx >= 0) {
                String maKV = (String) tabbedKhuVuc.getClientProperty("kv_" + idx);
                loadBanByKhuVuc(maKV);
            }
        });
        content.add(tabbedKhuVuc, BorderLayout.NORTH);

        // Grid
        gridPanel = new JPanel(new GridLayout(0, 5, 16, 16));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Wrap gridPanel in BorderLayout.NORTH to prevent vertical stretching
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private JPanel createLegend(String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);

        JPanel dot = new JPanel();
        dot.setBackground(color);
        dot.setPreferredSize(new Dimension(14, 14));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", Font.PLAIN, 12));
        lbl.setForeground(new Color(100, 100, 100));

        p.add(dot);
        p.add(lbl);
        return p;
    }

    private void loadData() {
        tabbedKhuVuc.removeAll();

        // Tab "Tất cả"
        tabbedKhuVuc.addTab("T\u1EA5t c\u1EA3", null);
        tabbedKhuVuc.putClientProperty("kv_0", "");

        List<KhuVuc> dsKV = tableController.getDanhSachKhuVuc();
        for (int i = 0; i < dsKV.size(); i++) {
            KhuVuc kv = dsKV.get(i);
            tabbedKhuVuc.addTab(kv.getTenKhuVuc(), null);
            tabbedKhuVuc.putClientProperty("kv_" + (i + 1), kv.getMaKhuVuc());
        }

        loadBanByKhuVuc("");
    }

    private void loadBanByKhuVuc(String maKhuVuc) {
        gridPanel.removeAll();

        // Bàn ảo "Mang Về" ở tab Tất cả
        if (maKhuVuc == null || maKhuVuc.isEmpty()) {
            Ban banMangVe = new Ban("MANG_VE", "MANG V\u1EC0", null, 0, TrangThaiBan.TRONG);
            gridPanel.add(createTableCard(banMangVe, true));
        }

        List<Ban> dsBan = tableController.getBanByKhuVuc(maKhuVuc);
        for (Ban b : dsBan) {
            gridPanel.add(createTableCard(b, false));
        }

        // Fill empty cells
        int total = dsBan.size() + ((maKhuVuc == null || maKhuVuc.isEmpty()) ? 1 : 0);
        int remainder = total % 5;
        if (remainder != 0) {
            for (int i = 0; i < (5 - remainder); i++) {
                JPanel empty = new JPanel();
                empty.setOpaque(false);
                gridPanel.add(empty);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JButton createTableCard(Ban ban, boolean isMangVe) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout(0, 4));
        btn.setPreferredSize(new Dimension(170, 120));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusable(false);
        btn.setBorderPainted(false);

        Color bgColor;
        String statusText;
        String topLine;

        if (isMangVe) {
            bgColor = new Color(52, 152, 219); // Xanh dương riêng cho Mang Về
            statusText = "MANG V\u1EC0";
            topLine = "\uD83D\uDECD"; // 🛍 icon
        } else {
            switch (ban.getTrangThai()) {
                case TRONG:
                    bgColor = new Color(39, 174, 96);
                    statusText = "TR\u1ED0NG";
                    break;
                case CO_KHACH:
                    bgColor = new Color(231, 76, 60);
                    statusText = "\u0110ANG PH\u1EE4C V\u1EE4";
                    break;
                case DA_DAT_TRUOC:
                    bgColor = new Color(243, 156, 18);
                    statusText = "\u0110\u00C3 \u0110\u1EB6T";
                    break;
                default:
                    bgColor = new Color(149, 165, 166);
                    statusText = "KH\u00C1C";
            }
            topLine = "B\u00E0n " + ban.getSoBan();
        }

        btn.setBackground(bgColor);

        // Tên bàn
        JLabel lblName = new JLabel(topLine, SwingConstants.CENTER);
        lblName.setFont(new Font("Roboto", Font.BOLD, isMangVe ? 28 : 20));
        lblName.setForeground(Color.WHITE);
        lblName.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Trạng thái + sức chứa
        String subText = statusText;
        if (!isMangVe && ban.getSucChua() > 0) {
            subText += " (" + ban.getSucChua() + " gh\u1EBF)";
        }

        // Nếu bàn CO_KHACH, hiển thị thêm tiền tạm tính
        if (!isMangVe && TrangThaiBan.CO_KHACH.equals(ban.getTrangThai())) {
            DonHang dh = OrderManager.getInstance().getOrderByBan(ban.getMaBan());
            if (dh != null && dh.getTongTienTamTinh() > 0) {
                NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
                subText = nf.format(dh.getTongTienTamTinh()) + "\u0111";
            }
        }

        JLabel lblStatus = new JLabel(subText, SwingConstants.CENTER);
        lblStatus.setFont(new Font("Roboto", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(255, 255, 255, 210));
        lblStatus.setBorder(new EmptyBorder(0, 0, 14, 0));

        btn.add(lblName, BorderLayout.CENTER);
        btn.add(lblStatus, BorderLayout.SOUTH);

        // Hover effect thủ công
        Color hoverColor = bgColor.darker();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        // Click
        btn.addActionListener(e -> {
            if (clickListener != null) {
                if (isMangVe) {
                    // Hi\u1ec7n dialog danh s\u00e1ch \u0111\u01a1n Mang V\u1ec1 \u0111ang ch\u1edd
                    Window win = SwingUtilities.getWindowAncestor(btn);
                    if (win instanceof JFrame) {
                        java.util.List<DonHang> dsChoMangVe = orderController.getOpenTakeawayOrders();
                        TakeawayListDialog dlg = new TakeawayListDialog((JFrame) win, dsChoMangVe, orderController);
                        dlg.setVisible(true);

                        if (dlg.isCreateNew()) {
                            clickListener.onTableClicked(ban, null);
                        } else if (dlg.getSelectedOrder() != null) {
                            clickListener.onTableClicked(ban, dlg.getSelectedOrder());
                        }
                    }
                } else {
                    DonHang dh = tableController.getDonHangDangMo(ban.getMaBan());
                    clickListener.onTableClicked(ban, dh);
                }
            } else {
                String msg = isMangVe
                        ? "M\u1edf \u0111\u01a1n MANG V\u1ec0..."
                        : "M\u1edf \u0111\u01a1n cho B\u00e0n " + ban.getSoBan() + "...";
                JOptionPane.showMessageDialog(this, msg, "Order", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        return btn;
    }

    /**
     * Gọi để refresh lại sơ đồ bàn (sau khi thanh toán, đổi trạng thái...).
     */
    public void refreshData() {
        int idx = tabbedKhuVuc.getSelectedIndex();
        if (idx >= 0) {
            String maKV = (String) tabbedKhuVuc.getClientProperty("kv_" + idx);
            loadBanByKhuVuc(maKV);
        }
    }
}
