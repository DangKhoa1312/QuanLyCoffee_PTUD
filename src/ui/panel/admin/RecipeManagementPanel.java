package ui.panel.admin;

import controller.RecipeController;
import entity.DinhMucNguyenLieu;
import entity.Mon;
import entity.NguyenLieu;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class RecipeManagementPanel extends JPanel {

    private final RecipeController controller = new RecipeController();
    private final Color PRIMARY_COLOR = new Color(52, 73, 94);
    private final Color BG_COLOR      = new Color(245, 247, 250);

    // Master - Left
    private JTextField txtSearchMon;
    private JTable tblMon;
    private DefaultTableModel modMon;

    // Detail - Right
    private JLabel lblCurrentMon;
    private JTable tblDinhMuc;
    private DefaultTableModel modDinhMuc;

    // Form
    private JComboBox<NLItem> cbNguyenLieu;
    private JTextField txtSoLuong;
    private JLabel lblDonViTinh;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private Mon selectedMon = null;
    private DinhMucNguyenLieu selectedDinhMuc = null;

    // Wrapper for Combo Box
    class NLItem {
        NguyenLieu nl;
        NLItem(NguyenLieu nl) { this.nl = nl; }
        @Override public String toString() { return nl.getTenNL(); }
    }

    public RecipeManagementPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(15, 25, 20, 25));

        add(buildHeader(), BorderLayout.NORTH);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildMasterPanel(), buildDetailPanel());
        split.setDividerLocation(380);
        split.setDividerSize(0);
        split.setOpaque(false);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);

        loadMasterData();
        loadComboNguyenLieu();
    }

    // ── HEADER ────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);

        JPanel bc = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bc.setOpaque(false);
        JLabel lbcGray = new JLabel("Admin / Thiết Lập / Ngành Hàng / ");
        lbcGray.setForeground(Color.GRAY);
        lbcGray.setFont(new Font("Roboto", Font.PLAIN, 13));
        JLabel lbcCurrent = new JLabel("Công Thức (Công thức chuẩn)");
        lbcCurrent.setFont(new Font("Roboto", Font.BOLD, 13));
        lbcCurrent.setForeground(PRIMARY_COLOR);
        bc.add(lbcGray); bc.add(lbcCurrent);
        pnl.add(bc);
        pnl.add(Box.createVerticalStrut(8));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel title = new JLabel("QUẢN TRỊ ĐỊNH MỨC NGUYÊN LIỆU");
        title.setFont(new Font("Roboto", Font.BOLD, 24));
        title.setForeground(new Color(44, 62, 80));
        title.setIcon(IconFontSwing.buildIcon(FontAwesome.FLASK, 28, PRIMARY_COLOR));
        title.setIconTextGap(12);
        titleBar.add(title, BorderLayout.WEST);
        pnl.add(titleBar);
        return pnl;
    }

    // ── MASTER PANEL (LEFT) ───────────────────────────────────────────
    private JPanel buildMasterPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lblTop = new JLabel("Danh Sách Món");
        lblTop.setFont(new Font("Roboto", Font.BOLD, 16));
        lblTop.setForeground(PRIMARY_COLOR);
        
        txtSearchMon = new JTextField();
        txtSearchMon.setPreferredSize(new Dimension(0, 36));
        txtSearchMon.putClientProperty("JTextField.placeholderText", "Tìm món theo tên...");
        txtSearchMon.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadMasterData(); }
        });
        
        top.add(lblTop, BorderLayout.NORTH);
        top.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        top.add(txtSearchMon, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        modMon = new DefaultTableModel(new String[]{"Mã Món", "Tên Món", "Obj"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblMon = buildStyledTable(modMon);
        tblMon.setRowHeight(40);
        tblMon.removeColumn(tblMon.getColumnModel().getColumn(2)); // hide Obj
        tblMon.getColumnModel().getColumn(0).setMaxWidth(80);

        tblMon.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                if (col == 1) {
                    l.setIcon(IconFontSwing.buildIcon(FontAwesome.COFFEE, 16, isSelected ? Color.WHITE : new Color(148, 163, 184)));
                    l.setIconTextGap(10);
                } else l.setIcon(null);
                return l;
            }
        });

        tblMon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = tblMon.getSelectedRow();
                if (r >= 0) {
                    selectedMon = (Mon) modMon.getValueAt(r, 2); // hidden col 2
                    lblCurrentMon.setText("Cấu hình công thức cho: " + selectedMon.getTenMon());
                    clearForm();
                    loadDetailData();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblMon);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // ── DETAIL PANEL (RIGHT) ──────────────────────────────────────────
    private JPanel buildDetailPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 15, 0, 0));

        lblCurrentMon = new JLabel("Vui lòng chọn 1 Món từ danh sách bên trái");
        lblCurrentMon.setFont(new Font("Roboto", Font.BOLD, 18));
        lblCurrentMon.setForeground(new Color(16, 185, 129)); // emerald-500
        p.add(lblCurrentMon, BorderLayout.NORTH);

        modDinhMuc = new DefaultTableModel(new String[]{"ID", "Mã NL", "Tên Nguyên Liệu", "Định Mức Tiêu Hao", "ĐVT", "Obj"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDinhMuc = buildStyledTable(modDinhMuc);
        tblDinhMuc.removeColumn(tblDinhMuc.getColumnModel().getColumn(5)); // hide Obj
        tblDinhMuc.removeColumn(tblDinhMuc.getColumnModel().getColumn(0)); // hide ID (maDinhMuc)

        tblDinhMuc.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = tblDinhMuc.getSelectedRow();
                if (r >= 0) {
                    selectedDinhMuc = (DinhMucNguyenLieu) modDinhMuc.getValueAt(r, 5); // hidden col Obj
                    bindDinhMucToForm(selectedDinhMuc);
                } else {
                    clearForm();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblDinhMuc);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);

        // Form nhập liệu
        p.add(buildFormPanel(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 20, 15, 20)
        ));
        form.setBackground(Color.WHITE);
        form.setOpaque(true);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 5, 5, 5);

        // Row 1: Lables
        g.gridx = 0; g.gridy = 0; g.weightx = 0.5;
        form.add(new JLabel("Nguyên Liệu Cấu Thành:"), g);
        g.gridx = 1; g.gridy = 0; g.weightx = 0.4;
        form.add(new JLabel("Số Lượng Tiêu Hao:"), g);
        g.gridx = 2; g.gridy = 0; g.weightx = 0.1;
        form.add(new JLabel("Đơn Vị:"), g);

        // Row 2: Inputs
        cbNguyenLieu = new JComboBox<>();
        cbNguyenLieu.setPreferredSize(new Dimension(0, 36));
        cbNguyenLieu.addActionListener(e -> {
            NLItem item = (NLItem) cbNguyenLieu.getSelectedItem();
            if (item != null) lblDonViTinh.setText(item.nl.getDonViTinh());
        });
        g.gridx = 0; g.gridy = 1;
        form.add(cbNguyenLieu, g);

        txtSoLuong = new JTextField();
        txtSoLuong.setPreferredSize(new Dimension(0, 36));
        g.gridx = 1; g.gridy = 1;
        form.add(txtSoLuong, g);

        lblDonViTinh = new JLabel("---");
        lblDonViTinh.setFont(new Font("Roboto", Font.BOLD, 14));
        lblDonViTinh.setForeground(new Color(100, 116, 139));
        g.gridx = 2; g.gridy = 1;
        form.add(lblDonViTinh, g);

        // Row 3: Buttons
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);
        btnAdd    = createBtn("Thêm NL", new Color(16, 185, 129));
        btnUpdate = createBtn("Cập Nhật", new Color(245, 158, 11));
        btnDelete = createBtn("Xóa NL", new Color(239, 68, 68));
        btnClear  = createBtn("Làm Mới", new Color(148, 163, 184));
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        btnAdd.addActionListener(e -> actionAdd());
        btnUpdate.addActionListener(e -> actionUpdate());
        btnDelete.addActionListener(e -> actionDelete());
        btnClear.addActionListener(e -> clearForm());

        pnlBtns.add(btnAdd);
        pnlBtns.add(btnUpdate);
        pnlBtns.add(btnDelete);
        pnlBtns.add(btnClear);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 3;
        g.insets = new Insets(15, 5, 0, 5);
        form.add(pnlBtns, g);

        return form;
    }

    private JButton createBtn(String txt, Color bg) {
        JButton btn = new JButton(txt);
        btn.setFont(new Font("Roboto", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── LOGIC ─────────────────────────────────────────────────────────

    private void loadMasterData() {
        modMon.setRowCount(0);
        String txt = txtSearchMon.getText();
        List<Mon> list = controller.getAllMon(txt);
        for (Mon m : list) {
            modMon.addRow(new Object[]{m.getMaMon(), m.getTenMon(), m});
        }
    }

    private void loadComboNguyenLieu() {
        cbNguyenLieu.removeAllItems();
        List<NguyenLieu> list = controller.getAllNguyenLieu();
        for (NguyenLieu nl : list) cbNguyenLieu.addItem(new NLItem(nl));
    }

    private void loadDetailData() {
        modDinhMuc.setRowCount(0);
        if (selectedMon == null) return;
        List<DinhMucNguyenLieu> list = controller.getDinhMucByMon(selectedMon.getMaMon());
        for (DinhMucNguyenLieu dm : list) {
            NguyenLieu nl = controller.getNguyenLieuById(dm.getMaNL());
            if (nl != null) {
                modDinhMuc.addRow(new Object[]{
                    dm.getMaDinhMuc(), nl.getMaNL(), nl.getTenNL(), dm.getSoLuong(), nl.getDonViTinh(), dm
                });
            }
        }
    }

    private void bindDinhMucToForm(DinhMucNguyenLieu dm) {
        if (dm == null) return;
        txtSoLuong.setText(String.valueOf(dm.getSoLuong()));
        for (int i = 0; i < cbNguyenLieu.getItemCount(); i++) {
            NLItem item = cbNguyenLieu.getItemAt(i);
            if (item.nl.getMaNL().equals(dm.getMaNL())) {
                cbNguyenLieu.setSelectedIndex(i);
                break;
            }
        }
        cbNguyenLieu.setEnabled(false); // Can't change NL type during update
        btnAdd.setEnabled(false);
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        selectedDinhMuc = null;
        txtSoLuong.setText("");
        cbNguyenLieu.setEnabled(true);
        if (cbNguyenLieu.getItemCount() > 0) cbNguyenLieu.setSelectedIndex(0);
        tblDinhMuc.clearSelection();
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    // ── ACTIONS ───────────────────────────────────────────────────────

    private void actionAdd() {
        if (selectedMon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 Món trước!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        NLItem item = (NLItem) cbNguyenLieu.getSelectedItem();
        if (item == null) return;

        double sl = 0;
        try {
            sl = Double.parseDouble(txtSoLuong.getText());
            if (sl <= 0) throw new Exception();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Định mức tiêu hao phải là chữ số lớn hơn 0!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = controller.addDinhMuc(selectedMon.getMaMon(), item.nl.getMaNL(), sl);
        if (success) {
            loadDetailData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Nguyên liệu này đã tồn tại trong công thức. Vui lòng bấm Cập Nhật thay vì Thêm!", "Lỗi Trùng Lặp", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actionUpdate() {
        if (selectedDinhMuc == null) return;
        double sl = 0;
        try {
            sl = Double.parseDouble(txtSoLuong.getText());
            if (sl <= 0) throw new Exception();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Định mức tiêu hao phải là chữ số lớn hơn 0!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        NLItem item = (NLItem) cbNguyenLieu.getSelectedItem();
        boolean ok = controller.updateDinhMuc(selectedDinhMuc.getMaDinhMuc(), selectedMon.getMaMon(), item.nl.getMaNL(), sl);
        if (ok) {
            loadDetailData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actionDelete() {
        if (selectedDinhMuc == null) return;
        int cf = JOptionPane.showConfirmDialog(this, "Chắc chắn muốn loại bỏ nguyên liệu này khỏi công thức?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (cf == JOptionPane.YES_OPTION) {
            if (controller.deleteDinhMuc(selectedDinhMuc.getMaDinhMuc())) {
                loadDetailData();
                clearForm();
            }
        }
    }

    // ── STYLING JTABLE ────────────────────────────────────────────────
    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(241, 245, 249)); // slate-100
        table.getTableHeader().setForeground(new Color(71, 85, 105));   // slate-600
        table.getTableHeader().setPreferredSize(new Dimension(100, 40));
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(224, 242, 254)); // sky-100
        table.setSelectionForeground(new Color(15, 23, 42));    // slate-900
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(false);
        return table;
    }
}
