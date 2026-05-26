package ui.panel.admin;

import controller.MenuController;
import entity.Mon;
import entity.Size;
import enums.LoaiMon;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * MenuManagementPanel: Quản lý thực đơn phong cách Hybrid Card-based.
 */
public class MenuManagementPanel extends JPanel {

    private final MenuController controller = new MenuController();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterLoai;
    private JComboBox<String> cbFilterTrangThai;
    private JButton btnAdd;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public MenuManagementPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(15, 25, 25, 25));

        initHeader();
        initTable();
        loadData();
    }

    private void initHeader() {
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setOpaque(false);

        // 1. Breadcrumb
        JPanel pnlBreadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBreadcrumb.setOpaque(false);
        JLabel lblBreadcrumb = new JLabel("Admin / Thiết lập / ");
        lblBreadcrumb.setForeground(Color.GRAY);
        lblBreadcrumb.setFont(new Font("Roboto", Font.PLAIN, 13));
        JLabel lblCurrent = new JLabel("Thực đơn");
        lblCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCurrent.setForeground(PRIMARY_COLOR);
        pnlBreadcrumb.add(lblBreadcrumb);
        pnlBreadcrumb.add(lblCurrent);
        pnlHeader.add(pnlBreadcrumb);
        pnlHeader.add(Box.createVerticalStrut(10));

        // 2. Title Section
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("QUẢN LÝ THỰC ĐƠN");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlTitle.add(lblTitle, BorderLayout.WEST);

        btnAdd = new JButton(" Thêm Món Mới");
        btnAdd.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 14, Color.WHITE));
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(180, 40));
        btnAdd.setFocusable(false);
        btnAdd.addActionListener(e -> handleAddMon());
        pnlTitle.add(btnAdd, BorderLayout.EAST);
        pnlHeader.add(pnlTitle);
        pnlHeader.add(Box.createVerticalStrut(15));

        // 3. Filter Card
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlFilter.setBackground(Color.WHITE);
        pnlFilter.setBorder(new LineBorder(new Color(230, 230, 230), 1));

        // ── Tìm kiếm ──
        pnlFilter.add(new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 15, Color.GRAY)));
        txtSearch = new JTextField(22);
        txtSearch.setPreferredSize(new Dimension(0, 34));
        txtSearch.setToolTipText("Tìm theo tên món (Ctrl+F)");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    txtSearch.setText("");
                performSearch();
            }
        });
        pnlFilter.add(txtSearch);

        // ── Separator ──
        JSeparator sep1 = new JSeparator(SwingConstants.VERTICAL);
        sep1.setPreferredSize(new Dimension(1, 26));
        sep1.setForeground(new Color(210, 215, 220));
        pnlFilter.add(sep1);

        // ── Lọc loại món ──
        JLabel lblLoai = new JLabel(IconFontSwing.buildIcon(FontAwesome.TAG, 14, new Color(100, 120, 140)));
        lblLoai.setToolTipText("Lọc theo loại món");
        pnlFilter.add(lblLoai);
        cbFilterLoai = new JComboBox<>();
        cbFilterLoai.addItem("Tất cả loại");
        for(LoaiMon lm : LoaiMon.values()) {
            cbFilterLoai.addItem(lm.getTenLoai());
        }
        cbFilterLoai.setPreferredSize(new Dimension(140, 34));
        cbFilterLoai.setToolTipText("Lọc theo loại món");
        cbFilterLoai.addActionListener(e -> performSearch());
        pnlFilter.add(cbFilterLoai);

        // ── Separator ──
        JSeparator sep2 = new JSeparator(SwingConstants.VERTICAL);
        sep2.setPreferredSize(new Dimension(1, 26));
        sep2.setForeground(new Color(210, 215, 220));
        pnlFilter.add(sep2);

        // ── Lọc trạng thái ──
        JLabel lblStatus = new JLabel(IconFontSwing.buildIcon(FontAwesome.TOGGLE_ON, 14, new Color(100, 120, 140)));
        lblStatus.setToolTipText("Lọc theo trạng thái kinh doanh");
        pnlFilter.add(lblStatus);
        cbFilterTrangThai = new JComboBox<>(new String[] { "Tất cả trạng thái", "Đang bán", "Ngưng bán" });
        cbFilterTrangThai.setPreferredSize(new Dimension(160, 34));
        cbFilterTrangThai.setToolTipText("Lọc theo trạng thái kinh doanh");
        cbFilterTrangThai.addActionListener(e -> performSearch());
        pnlFilter.add(cbFilterTrangThai);

        // ── Separator ──
        JSeparator sep3 = new JSeparator(SwingConstants.VERTICAL);
        sep3.setPreferredSize(new Dimension(1, 26));
        sep3.setForeground(new Color(210, 215, 220));
        pnlFilter.add(sep3);

        // ── Nút Reset bộ lọc ──
        JButton btnReset = new JButton(" Xóa bộ lọc");
        btnReset.setIcon(IconFontSwing.buildIcon(FontAwesome.TIMES_CIRCLE, 13, new Color(180, 80, 80)));
        btnReset.setFont(new Font("Roboto", Font.PLAIN, 12));
        btnReset.setForeground(new Color(180, 80, 80));
        btnReset.setContentAreaFilled(false);
        btnReset.setBorderPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.setFocusable(false);
        btnReset.setToolTipText("Xóa tất cả bộ lọc, hiển thị toàn bộ thực đơn");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbFilterLoai.setSelectedIndex(0);
            cbFilterTrangThai.setSelectedIndex(0);
            performSearch();
            txtSearch.requestFocus();
        });
        pnlFilter.add(btnReset);

        pnlHeader.add(pnlFilter);
        add(pnlHeader, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] cols = { "Mã món", "Tên món", "Loại", "Khoảng giá", "Trạng thái", "Thao tác" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(55);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        // ── Keyboard shortcuts cho bảng ──
        table.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int row = table.getSelectedRow();
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && !table.isEditing()) {
                    if (row >= 0) {
                        Mon m = (Mon) tableModel.getValueAt(table.convertRowIndexToModel(row), 5);
                        handleEdit(m);
                    }
                    e.consume();
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE
                        || e.getKeyCode() == java.awt.event.KeyEvent.VK_F2) {
                    if (row >= 0) {
                        Mon m = (Mon) tableModel.getValueAt(table.convertRowIndexToModel(row), 5);
                        handleToggleStatus(m);
                    }
                    e.consume();
                }
            }
        });

        // Ctrl+N = Thêm món mới (toàn panel)
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N,
                java.awt.event.InputEvent.CTRL_DOWN_MASK), "addMon");
        am.put("addMon", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                handleAddMon();
            }
        });

        // Ctrl+F = Focus ô tìm kiếm
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F,
                java.awt.event.InputEvent.CTRL_DOWN_MASK), "focusSearch");
        am.put("focusSearch", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtSearch.requestFocus();
                txtSearch.selectAll();
            }
        });

        // ── Double-click vào row → mở dialog xem / chỉnh sửa ──
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && !table.isEditing()) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        // Đảm bảo không đang edit ô action
                        if (table.isEditing())
                            table.getCellEditor().stopCellEditing();
                        Mon m = (Mon) tableModel.getValueAt(table.convertRowIndexToModel(row), 5);
                        handleEdit(m);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Mon> list = controller.getAllMon();
        for (Mon m : list) {
            addMonToTable(m);
        }
    }

    private void addMonToTable(Mon m) {
        String priceRange = getPriceRange(m.getMaMon());
        String loai = m.getLoaiMon() != null ? m.getLoaiMon().getTenLoai() : "";
        tableModel.addRow(new Object[] { m.getMaMon(), m.getTenMon(), loai, priceRange, m.isTrangThai(), m });
    }

    private String getPriceRange(String maMon) {
        List<Size> sizes = controller.getSizeOfMon(maMon);
        if (sizes.isEmpty())
            return "Chưa có giá";
        double min = Double.MAX_VALUE;
        double max = 0;
        for (Size s : sizes) {
            double p = controller.getGiaBan(s.getMaSize());
            if (p > 0) {
                min = Math.min(min, p);
                max = Math.max(max, p);
            }
        }
        if (max == 0)
            return "Chưa có giá";
        if (min == max)
            return String.format("%,.0f đ", min);
        return String.format("%,.0f - %,.0f đ", min, max);
    }

    private void performSearch() {
        String keyword = txtSearch.getText().toLowerCase().trim();
        int filterLoai = cbFilterLoai.getSelectedIndex(); // 0=Tất cả, 1=Đồ uống, 2=Đồ ăn
        int filterStat = cbFilterTrangThai.getSelectedIndex(); // 0=Tất cả, 1=Đang bán, 2=Ngưng

        tableModel.setRowCount(0);
        List<Mon> list = controller.getAllMon();
        int found = 0;
        for (Mon m : list) {
            // ─ Lọc tên món
            boolean matchName = keyword.isEmpty() ||
                    m.getTenMon().toLowerCase().contains(keyword) ||
                    m.getMaMon().toLowerCase().contains(keyword);

            // ─ Lọc loại món
            boolean matchType = (filterLoai == 0);
            if (!matchType && m.getLoaiMon() != null) {
                matchType = m.getLoaiMon().getTenLoai().equals(cbFilterLoai.getItemAt(filterLoai));
            }

            // ─ Lọc trạng thái
            boolean matchStatus = (filterStat == 0) ||
                    (filterStat == 1 && m.isTrangThai()) ||
                    (filterStat == 2 && !m.isTrangThai());

            if (matchName && matchType && matchStatus) {
                addMonToTable(m);
                found++;
            }
        }

        // Hiển thị số kết quả nếu đang lọc
        boolean isFiltering = !keyword.isEmpty() || filterLoai > 0 || filterStat > 0;
        // Chúp đều bảng: placeholder khi không có kết quả
        if (found == 0 && isFiltering) {
            tableModel.addRow(new Object[] { "", "Không tìm thấy kết quả phù hợp", "", "", true, null });
        }
    }

    private void handleAddMon() {
        Mon m = new Mon();
        m.setMaMon(controller.generateNextMaMon());
        m.setTrangThai(true);
        // Mối quan hệ dialog tương tự StaffDialog sẽ build ở bước sau
        ui.dialog.MenuDialog dlg = new ui.dialog.MenuDialog((Frame) SwingUtilities.getWindowAncestor(this), m, false);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    // --- RENDERERS ---

    class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
            if (!isS)
                comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            return comp;
        }
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
            lbl.setHorizontalAlignment(CENTER);
            // Guard: null khi hàng placeholder "Không tìm thấy kết quả"
            if (v == null) {
                lbl.setText(""); lbl.setForeground(Color.GRAY);
                return lbl;
            }
            boolean active = (boolean) v;
            if (active) {
                lbl.setForeground(new Color(39, 174, 96));
                lbl.setText("● Đang bán");
            } else {
                lbl.setForeground(new Color(149, 165, 166));
                lbl.setText("● Ngưng bán");
            }
            return lbl;
        }
    }

    // ── Renderer: vẽ panel nút theo trạng thái của từng dòng ──────────────
    class ActionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            // Guard: null khi hàng placeholder "Không tìm thấy kết quả"
            if (v == null) {
                JPanel empty = new JPanel();
                empty.setBackground(isS ? t.getSelectionBackground()
                        : (r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255)));
                return empty;
            }
            Mon mon = (Mon) v;
            JPanel p = createActionPanel(mon.isTrangThai());
            p.setBackground(isS ? t.getSelectionBackground()
                    : (r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255)));
            return p;
        }
    }

    // ── Editor: xử lý sự kiện khi click vào cell ─────────────────────────────
    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton btnEdit;
        private JButton btnToggle;
        private Mon current;

        public ActionEditor() {
            // Khởi tạo panel rỗng, sẽ cập nhật màu ở getTableCellEditorComponent
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);

            btnEdit = createBtn(FontAwesome.PENCIL, new Color(41, 128, 185), "Chỉnh sửa thông tin món");
            btnToggle = createBtn(FontAwesome.TOGGLE_ON, Color.GRAY, "Bật / Tắt trạng thái bán");

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 0, 5);
            panel.add(btnEdit, gbc);
            panel.add(btnToggle, gbc);

            btnEdit.addActionListener(e -> {
                stopCellEditing();
                handleEdit(current);
            });
            btnToggle.addActionListener(e -> {
                stopCellEditing();
                handleToggleStatus(current);
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable t, Object v, boolean isS, int r, int c) {
            current = (Mon) v;
            panel.setBackground(t.getSelectionBackground());

            // Cập nhật màu nút toggle theo trạng thái thực của món
            applyToggleColor(btnToggle, current.isTrangThai());

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return current;
        }
    }

    // ── Tạo action panel có màu nút toggle theo trạng thái ───────────────────
    private JPanel createActionPanel(boolean isActive) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(true);

        JButton btnEdit = createBtn(FontAwesome.PENCIL,
                new Color(41, 128, 185), "Chỉnh sửa thông tin món");
        JButton btnToggle = createBtn(
                isActive ? FontAwesome.TOGGLE_ON : FontAwesome.TOGGLE_OFF,
                isActive ? new Color(39, 174, 96) : new Color(180, 185, 190),
                isActive ? "Đang bán — nhấn để ngưng" : "Đang ngưng bán — nhấn để bán lại");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        p.add(btnEdit, gbc);
        p.add(btnToggle, gbc);
        return p;
    }

    // ── Áp màu + icon cho nút toggle theo trạng thái ─────────────────────────
    private void applyToggleColor(JButton btn, boolean isActive) {
        Color bgColor = isActive ? new Color(39, 174, 96) : new Color(180, 185, 190);
        String tip = isActive ? "Đang bán — nhấn để ngưng" : "Đang ngưng bán — nhấn để bán lại";
        btn.setIcon(IconFontSwing.buildIcon(
                isActive ? FontAwesome.TOGGLE_ON : FontAwesome.TOGGLE_OFF, 16, Color.WHITE));
        btn.setBackground(bgColor);
        btn.setToolTipText(tip);
    }

    // ── Helper tạo nút icon ───────────────────────────────────────────────────
    private JButton createBtn(FontAwesome icon, Color color, String tooltip) {
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        b.setIcon(IconFontSwing.buildIcon(icon, 16, Color.WHITE));
        b.setBackground(color);
        b.setToolTipText(tooltip);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(5, 8, 5, 8));
        b.setFocusable(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Business logic ────────────────────────────────────────────────────────
    private void handleEdit(Mon m) {
        ui.dialog.MenuDialog dlg = new ui.dialog.MenuDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), m, true);
        dlg.setVisible(true);
        if (dlg.isConfirmed())
            loadData();
    }

    private void handleToggleStatus(Mon m) {
        boolean newStatus = !m.isTrangThai();
        String msg = newStatus
                ? "Bật lại trạng thái \"Đang bán\" cho món: " + m.getTenMon() + "?"
                : "Ngưng bán món: " + m.getTenMon() + "?";
        int opt = JOptionPane.showConfirmDialog(this, msg,
                "Xác nhận thay đổi trạng thái", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            m.setTrangThai(newStatus);
            if (controller.saveMon(m, true))
                loadData();
        }
    }
}