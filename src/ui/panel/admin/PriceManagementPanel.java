package ui.panel.admin;

import controller.PriceController;
import entity.BangGia;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

/**
 * PriceManagementPanel: Quản lý danh mục các Bảng giá.
 * Hỗ trợ: tìm kiếm, lọc trạng thái, hiện/ẩn bảng giá đã ẩn, Soft Delete.
 */
public class PriceManagementPanel extends JPanel {

    private final PriceController controller = new PriceController();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterStatus;
    private JCheckBox chkShowHidden;
    private BangGia cachedWinner = null; // [IMP-05] Cache winner để tránh N DB queries trong renderer

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public PriceManagementPanel() {
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
        pnlBreadcrumb.add(new JLabel("Admin / Thiết lập / "));
        JLabel lblCurrent = new JLabel("Bảng giá");
        lblCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCurrent.setForeground(PRIMARY_COLOR);
        pnlBreadcrumb.add(lblCurrent);
        pnlHeader.add(pnlBreadcrumb);
        pnlHeader.add(Box.createVerticalStrut(10));

        // 2. Title & Add Button
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("DANH SÁCH BẢNG GIÁ");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlTitle.add(lblTitle, BorderLayout.WEST);

        JButton btnAdd = new JButton(" Tạo Bảng Giá Mới");
        btnAdd.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 14, Color.WHITE));
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(200, 40));
        btnAdd.addActionListener(e -> handleAdd());
        pnlTitle.add(btnAdd, BorderLayout.EAST);
        pnlHeader.add(pnlTitle);
        pnlHeader.add(Box.createVerticalStrut(15));

        // 3. Filter Card
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pnlFilter.setBackground(Color.WHITE);
        pnlFilter.setBorder(new LineBorder(new Color(230, 230, 230), 1));

        JLabel lblSearchIcon = new JLabel(IconFontSwing.buildIcon(FontAwesome.SEARCH, 16, Color.GRAY));
        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(0, 35));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { performSearch(); }
        });

        cbFilterStatus = new JComboBox<>(new String[]{"Tất cả", "Hiệu lực", "Hết hiệu lực"});
        cbFilterStatus.setPreferredSize(new Dimension(150, 35));
        cbFilterStatus.addActionListener(e -> performSearch());

        pnlFilter.add(lblSearchIcon);
        pnlFilter.add(txtSearch);
        pnlFilter.add(new JLabel("Trạng thái: "));
        pnlFilter.add(cbFilterStatus);
        pnlFilter.add(new JLabel("  |  "));

        chkShowHidden = new JCheckBox("Hiện bảng giá đã ẩn");
        chkShowHidden.setOpaque(false);
        chkShowHidden.setFont(new Font("Roboto", Font.PLAIN, 13));
        chkShowHidden.addActionListener(e -> performSearch());
        pnlFilter.add(chkShowHidden);

        pnlHeader.add(pnlFilter);

        add(pnlHeader, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] cols = {"Mã bảng giá", "Tên bảng giá", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái", "Thao tác"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(50);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.setShowVerticalLines(false);

        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(5).setPreferredWidth(180);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        add(scroll, BorderLayout.CENTER);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    BangGia current = (BangGia) tableModel.getValueAt(table.getSelectedRow(), 5);
                    handleEdit(current);
                }
            }
        });
    }

    private void loadData() {
        performSearch();
    }

    private void performSearch() {
        tableModel.setRowCount(0);
        // [IMP-05] Cache winner 1 lần duy nhất — tránh gọi DB trong mỗi render cell
        cachedWinner = controller.getWinningPriceList();
        List<BangGia> list = controller.getAllBangGia();

        String kw = txtSearch != null ? txtSearch.getText().toLowerCase() : "";
        int filterStatus = cbFilterStatus != null ? cbFilterStatus.getSelectedIndex() : 0;
        boolean showHidden = chkShowHidden != null && chkShowHidden.isSelected();

        for (BangGia bg : list) {
            // 1. Lọc theo Soft Delete
            if (!showHidden && !bg.isHoatDong()) continue;

            // 2. [BUG-06 FIX] Null-safe tìm kiếm — tránh NPE khi tenBangGia = null
            String tenBG = bg.getTenBangGia() != null ? bg.getTenBangGia().toLowerCase() : "";
            String maBG  = bg.getMaBangGia()  != null ? bg.getMaBangGia().toLowerCase()  : "";
            if (!kw.isEmpty() && !tenBG.contains(kw) && !maBG.contains(kw)) continue;

            // 3. Lọc theo trạng thái hiệu lực (trangThai)
            if (filterStatus == 1 && !bg.isTrangThai()) continue;
            if (filterStatus == 2 &&  bg.isTrangThai()) continue;

            tableModel.addRow(new Object[]{
                bg.getMaBangGia(),
                bg.getTenBangGia(),
                bg.getNgayBatDau(),
                bg.getNgayKetThuc() != null ? bg.getNgayKetThuc() : "Vô thời hạn",
                bg,   // col 4 → StatusRenderer dùng BangGia object
                bg    // col 5 → ActionRenderer/Editor dùng BangGia object
            });
        }
    }

    private void handleAdd() {
        BangGia win = controller.getWinningPriceList();
        String sourceMaBG = null;

        if (win != null) {
            String[] options = {"Sử dụng giá hiện tại (Khuyên dùng)", "Bắt đầu từ bảng rỗng (0đ)"};
            int choice = JOptionPane.showOptionDialog(this,
                "Bạn muốn khởi tạo giá như thế nào?",
                "Tạo Bảng Giá Mới",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == 0) sourceMaBG = win.getMaBangGia();
            else if (choice == -1) return; // Hủy
        }

        BangGia bg = new BangGia();
        bg.setMaBangGia(controller.generateNextMaBG());
        bg.setNgayBatDau(java.time.LocalDate.now());
        bg.setTrangThai(false);
        bg.setHoatDong(true);

        ui.dialog.PriceMasterDialog dlg = new ui.dialog.PriceMasterDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), bg, false, sourceMaBG);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) loadData();
    }

    // [IMP-05] Tính trạng thái dùng cachedWinner — không truy vấn DB mỗi lần render
    private String getVisualStatusCached(BangGia bg) {
        if (!bg.isHoatDong()) return "Đã ẩn";
        if (!bg.isTrangThai()) return "Tạm ngưng";

        java.time.LocalDate today = java.time.LocalDate.now();
        if (bg.getNgayKetThuc() != null && today.isAfter(bg.getNgayKetThuc())) return "Hết hạn";
        if (bg.getNgayBatDau() != null && today.isBefore(bg.getNgayBatDau()))  return "Đang chờ";

        if (cachedWinner != null && bg.getMaBangGia().equals(cachedWinner.getMaBangGia())) {
            return "Đang áp dụng";
        }
        return "Dự phòng";
    }

    // --- RENDERERS & EDITORS ---

    class ZebraRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);

            // Lấy BangGia từ cột 4
            Object raw = t.getModel().getValueAt(r, 4);
            if (raw instanceof BangGia) {
                BangGia bg = (BangGia) raw;
                if (!bg.isHoatDong()) {
                    comp.setForeground(new Color(160, 160, 160));
                    if (!isS) comp.setBackground(new Color(245, 245, 245));
                } else {
                    comp.setForeground(t.getForeground());
                    if (!isS) comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
                }
            } else {
                if (!isS) comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            }
            return comp;
        }
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
            lbl.setHorizontalAlignment(CENTER);
            if (!(v instanceof BangGia)) return lbl;

            BangGia bg = (BangGia) v;
            // [IMP-05] Dùng cachedWinner — không gọi DB trong mỗi cell render
            String status = getVisualStatusCached(bg);
            lbl.setText("● " + status);

            switch (status) {
                case "Đã ẩn":
                    lbl.setForeground(new Color(150, 150, 150));
                    lbl.setFont(new Font("Roboto", Font.ITALIC, 13));
                    break;
                case "Đang áp dụng":
                    lbl.setForeground(new Color(39, 174, 96)); // Xanh lá
                    lbl.setFont(new Font("Roboto", Font.BOLD, 13));
                    break;
                case "Tạm ngưng":
                    lbl.setForeground(new Color(189, 195, 199)); // Xám tro
                    lbl.setFont(new Font("Roboto", Font.BOLD, 13));
                    break;
                case "Đang chờ":
                    lbl.setForeground(new Color(52, 152, 219)); // Xanh dương
                    lbl.setFont(new Font("Roboto", Font.PLAIN, 13));
                    break;
                case "Hết hạn":
                    lbl.setForeground(new Color(231, 76, 60)); // Đỏ
                    lbl.setFont(new Font("Roboto", Font.PLAIN, 13));
                    break;
                case "Dự phòng":
                    lbl.setForeground(new Color(230, 126, 34)); // Cam
                    lbl.setFont(new Font("Roboto", Font.PLAIN, 13));
                    break;
            }
            return lbl;
        }
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            Object raw = t.getModel().getValueAt(r, 4);
            JPanel p = createActionPanel();
            p.setBackground(isS ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(252, 253, 255)));

            if (raw instanceof BangGia) {
                BangGia bg = (BangGia) raw;
                if (!bg.isHoatDong()) {
                    p.getComponent(0).setEnabled(false);
                    p.getComponent(1).setEnabled(false);
                    p.getComponent(2).setEnabled(false);
                } else {
                    JButton btnToggle = (JButton) p.getComponent(2);
                    if (bg.isTrangThai()) {
                        btnToggle.setIcon(IconFontSwing.buildIcon(FontAwesome.TOGGLE_ON, 16, Color.WHITE));
                        btnToggle.setBackground(new Color(46, 204, 113));
                        btnToggle.setToolTipText("Bảng giá đang bật. Nhấn để Tạm ngưng");
                    } else {
                        btnToggle.setIcon(IconFontSwing.buildIcon(FontAwesome.TOGGLE_OFF, 16, Color.WHITE));
                        btnToggle.setBackground(new Color(150, 150, 150));
                        btnToggle.setToolTipText("Bảng giá đang tắt. Nhấn để Kích hoạt");
                    }
                }
            }
            return p;
        }
    }

    private JPanel createActionPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        p.add(createBtn(FontAwesome.PENCIL, new Color(41, 128, 185)), gbc);
        p.add(createBtn(FontAwesome.TRASH, new Color(231, 76, 60)), gbc);
        p.add(createBtn(FontAwesome.TOGGLE_OFF, new Color(150, 150, 150)), gbc); // Nút Kích hoạt / Tạm ngưng
        return p;
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel p;
        private BangGia current;

        public ActionEditor() {
            p = createActionPanel();
            JButton btnEdit = (JButton) p.getComponent(0);
            btnEdit.addActionListener(e -> { stopCellEditing(); handleEdit(current); });

            JButton btnDel = (JButton) p.getComponent(1);
            btnDel.addActionListener(e -> { stopCellEditing(); handleDelete(current); });

            JButton btnToggle = (JButton) p.getComponent(2);
            btnToggle.addActionListener(e -> { stopCellEditing(); handleToggleStatus(current); });
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean isS, int r, int c) {
            current = (BangGia) v;
            p.setBackground(t.getSelectionBackground());
            boolean active = current.isHoatDong();
            p.getComponent(0).setEnabled(active);
            p.getComponent(1).setEnabled(active);
            
            JButton btnToggle = (JButton) p.getComponent(2);
            btnToggle.setEnabled(active);
            if (active) {
                if (current.isTrangThai()) {
                    btnToggle.setIcon(IconFontSwing.buildIcon(FontAwesome.TOGGLE_ON, 16, Color.WHITE));
                    btnToggle.setBackground(new Color(46, 204, 113));
                    btnToggle.setToolTipText("Bảng giá đang bật. Nhấn để Tạm ngưng");
                } else {
                    btnToggle.setIcon(IconFontSwing.buildIcon(FontAwesome.TOGGLE_OFF, 16, Color.WHITE));
                    btnToggle.setBackground(new Color(150, 150, 150));
                    btnToggle.setToolTipText("Bảng giá đang tắt. Nhấn để Kích hoạt");
                }
            }
            return p;
        }

        @Override public Object getCellEditorValue() { return current; }
    }

    private JButton createBtn(FontAwesome icon, Color color) {
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
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(5, 8, 5, 8));
        b.setFocusable(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void handleEdit(BangGia bg) {
        ui.dialog.PriceMasterDialog dlg = new ui.dialog.PriceMasterDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), bg, true, null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) loadData();
    }

    private void handleDelete(BangGia bg) {
        if (!bg.isHoatDong()) return;

        // GUARD: không cho ẩn bảng giá duy nhất đang hoạt động
        if (controller.countActivePriceLists() <= 1) {
            JOptionPane.showMessageDialog(this,
                "<html><b>Không thể ẩn bảng giá này!</b><br>" +
                "Hệ thống bắt buộc phải có ít nhất một bảng giá hoạt động để bán hàng.</html>",
                "Lỗi bảo mật", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this,
            "<html>Ẩn bảng giá '<b>" + bg.getTenBangGia() + "</b>'?<br>" +
            "Dữ liệu lịch sử vẫn được bảo toàn nhưng bảng giá sẽ không còn áp dụng được nữa.</html>",
            "Xác nhận Ẩn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            if (controller.deleteBangGia(bg.getMaBangGia())) {
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi ẩn bảng giá!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleToggleStatus(BangGia bg) {
        boolean newState = !bg.isTrangThai();
        
        if (!newState) {
            // Check Business Rule before suspending
            boolean hasFallback = false;
            java.time.LocalDate today = java.time.LocalDate.now();
            List<BangGia> all = controller.getAllBangGia();
            for (BangGia other : all) {
                if (!other.getMaBangGia().equals(bg.getMaBangGia()) 
                    && other.isHoatDong() && other.isTrangThai()
                    && !today.isBefore(other.getNgayBatDau())
                    && (other.getNgayKetThuc() == null || !today.isAfter(other.getNgayKetThuc()))) {
                    hasFallback = true;
                    break;
                }
            }
            
            if (!hasFallback) {
                if (!today.isBefore(bg.getNgayBatDau()) 
                    && (bg.getNgayKetThuc() == null || !today.isAfter(bg.getNgayKetThuc()))) {
                    JOptionPane.showMessageDialog(this, "<html><b style='color:red'>LỖI NGHIỆP VỤ: KHÔNG THỂ TẠM NGƯNG!</b><br><br>"
                            + "Hệ thống yêu cầu phải luôn có ít nhất 1 bảng giá đang áp dụng cho hôm nay.<br>"
                            + "Nếu bạn tạm ngưng bảng giá này, thu ngân sẽ không thể bán hàng.</html>", "Chặn Thao Tác", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            int opt = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn Tạm Ngưng bảng giá '" + bg.getTenBangGia() + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
        } else {
            int opt = JOptionPane.showConfirmDialog(this, "Kích hoạt bảng giá '" + bg.getTenBangGia() + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
        }
        
        bg.setTrangThai(newState);
        if (controller.saveBangGia(bg, true)) {
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi đổi trạng thái!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            bg.setTrangThai(!newState); // revert local
        }
    }
}
