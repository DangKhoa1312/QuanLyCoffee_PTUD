package ui.panel;

import controller.ReservationController;
import entity.DatBan;
import enums.TrangThaiDatBan;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import ui.dialog.DatBanDialog;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * ReservationManagementPanel — Quản lý đặt bàn.
 *
 * Layout:
 * NORTH: breadcrumb + title + nút [Xem đã ẩn]
 * NORTH-SUB: toolbar [+ Thêm] [Sửa] [Xoá] [↻ Làm mới]
 * CENTER: JTable danh sách
 */
public class ReservationManagementPanel extends JPanel {

    private final ReservationController controller = new ReservationController();

    // ── Table ─────────────────────────────────────────────────────────────────
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // ── Toolbar buttons ───────────────────────────────────────────────────────
    private JButton btnThem, btnSua, btnXoa, btnLamMoi, btnXemAn;
    private JTextField txtSearch;

    // ── Timer auto-check expired ──────────────────────────────────────────────
    private final Timer autoExpireTimer;

    // ── Chế độ xem ────────────────────────────────────────────────────────────
    private boolean dangXemDaAn = false;
    /**
     * Callback từ MainFrame — gọi khi Xác nhận thành công để chuyển sang trang Bàn
     */
    private Consumer<String> navigationCallback;

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color PRIMARY = new Color(113, 76, 52);
    private static final Color BG = new Color(245, 247, 250);
    private static final Color SUCCESS = new Color(46, 204, 113);
    private static final Color DANGER = new Color(231, 76, 60);
    private static final Color WARNING = new Color(243, 156, 18);
    private static final Color INFO = new Color(41, 128, 185);
    private static final Color GRAY = new Color(149, 165, 166);

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy");

    // ══════════════════════════════════════════════════════════════════════════
    public ReservationManagementPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(18, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);

        // Timer 30 giây auto-detect hết hạn
        autoExpireTimer = new Timer(30_000, e -> {
            controller.autoCheckExpired();
            loadData();
        });
        autoExpireTimer.start();

        loadData();
    }

    // ══ HEADER ════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        // 1. Breadcrumb
        JPanel pnlBC = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBC.setOpaque(false);
        JLabel lblBC = new JLabel("Bán Hàng / ");
        lblBC.setForeground(Color.GRAY);
        lblBC.setFont(new Font("Roboto", Font.PLAIN, 13));
        JLabel lblCur = new JLabel("Đặt Bàn");
        lblCur.setFont(new Font("Roboto", Font.BOLD, 13));
        lblCur.setForeground(PRIMARY);
        pnlBC.add(lblBC);
        pnlBC.add(lblCur);
        wrapper.add(pnlBC);
        wrapper.add(Box.createVerticalStrut(6));

        // 2. Tiêu đề + nút Xem đã ẩn
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        JLabel lblTitle = new JLabel("QUẢN LÝ ĐẶT BÀN");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlTitle.add(lblTitle, BorderLayout.WEST);

        btnXemAn = new JButton("  Xem đã ẩn", IconFontSwing.buildIcon(FontAwesome.EYE, 13, INFO));
        btnXemAn.setFont(new Font("Roboto", Font.PLAIN, 13));
        btnXemAn.setForeground(INFO);
        btnXemAn.setContentAreaFilled(false);
        btnXemAn.setBorder(new LineBorder(INFO, 1));
        btnXemAn.setPreferredSize(new Dimension(140, 34));
        btnXemAn.setFocusable(false);
        btnXemAn.addActionListener(e -> toggleXemAn());
        pnlTitle.add(btnXemAn, BorderLayout.EAST);

        wrapper.add(pnlTitle);
        wrapper.add(Box.createVerticalStrut(12));

        // 3. Toolbar
        wrapper.add(buildToolbar());
        wrapper.add(Box.createVerticalStrut(10));

        return wrapper;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 225, 225), 1),
                new EmptyBorder(2, 5, 2, 5)));

        btnThem = makeBtn("  Thêm", FontAwesome.PLUS, SUCCESS);
        btnSua = makeBtn("  Sửa", FontAwesome.PENCIL, INFO);
        btnXoa = makeBtn("  Xoá", FontAwesome.TRASH, DANGER);
        btnLamMoi = makeBtn("  Làm mới", FontAwesome.REFRESH, PRIMARY);

        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", " 🔍 Tìm tên hoặc SĐT...");
        txtSearch.putClientProperty("JTextField.showClearButton", true);
        txtSearch.setPreferredSize(new Dimension(200, 32));
        txtSearch.setFont(new Font("Roboto", Font.PLAIN, 13));
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            private void doSearch() {
                if (rowSorter == null) return;
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1, 2));
                }
            }
        });

        btnThem.addActionListener(e -> handleThem());
        btnSua.addActionListener(e -> handleSua());
        btnXoa.addActionListener(e -> handleXoa());
        btnLamMoi.addActionListener(e -> handleLamMoi());

        toolbar.add(btnThem);
        toolbar.add(mkSep());
        toolbar.add(btnSua);
        toolbar.add(btnXoa);
        toolbar.add(mkSep());
        toolbar.add(btnLamMoi);
        toolbar.add(Box.createHorizontalStrut(40));
        toolbar.add(txtSearch);

        return toolbar;
    }

    // ══ TABLE ════════════════════════════════════════════════════════════════
    private JScrollPane buildTableArea() {
        String[] cols = {
                "Mã ĐB", "Tên Khách", "SĐT", "Số Người",
                "Số Bàn", "Giờ Đến", "Giờ Đặt", "Trạng Thái", "_OBJ"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        rowSorter = new TableRowSorter<>(tableModel);

        table = new JTable(tableModel);
        table.setRowSorter(rowSorter);
        table.setRowHeight(48);
        table.setFont(new Font("Roboto", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.getTableHeader().setForeground(new Color(44, 62, 80));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(232, 245, 253));
        table.setSelectionForeground(new Color(30, 30, 30));

        // Zebra renderer
        table.setDefaultRenderer(Object.class, new ZebraRenderer());

        // Status renderer cho cột Trạng Thái (index 7)
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());

        // Ẩn cột object ẩn (index 8)
        table.removeColumn(table.getColumnModel().getColumn(8));

        // Độ rộng cột
        int[] widths = { 80, 160, 110, 80, 80, 140, 140, 130 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Double-click → sửa
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    handleSua();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(225, 225, 225)));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // ══ LOAD DATA ════════════════════════════════════════════════════════════
    public void refresh() {
        controller.autoCheckExpired();
        loadData();
    }

    /** MainFrame đăng ký để chuyển trang Bàn khi nhân viên Xác nhận đặt bàn */
    public void setNavigationCallback(Consumer<String> cb) {
        this.navigationCallback = cb;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<DatBan> list = dangXemDaAn ? controller.getDanhSachDaAn() : controller.getDanhSachHienThi();
        for (DatBan db : list) {
            tableModel.addRow(new Object[] {
                    db.getMaDatBan(),
                    db.getTenKhach(),
                    db.getSoDienThoai(),
                    db.getSoLuongNguoi() + " người",
                    db.getSoBan() != null ? db.getSoBan() : db.getMaBan(),
                    db.getThoiGianDen() != null ? DT_FMT.format(db.getThoiGianDen()) : "—",
                    db.getThoiGianDat() != null ? DT_FMT.format(db.getThoiGianDat()) : "—",
                    db.getTrangThai(),
                    db // object ẩn
            });
        }
    }

    private DatBan getSelectedDatBan() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0)
            return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (DatBan) tableModel.getValueAt(modelRow, 8);
    }

    // ══ ACTIONS ══════════════════════════════════════════════════════════════

    private void handleThem() {
        DatBan newDb = new DatBan();
        DatBanDialog dlg = new DatBanDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                newDb, DatBanDialog.Mode.ADD, controller);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            boolean ok = controller.them(newDb);
            if (ok) {
                loadData();
                JOptionPane.showMessageDialog(this,
                        "✅ Đặt bàn cho " + newDb.getTenKhach() + " đã được thêm thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "❌ Không thể thêm đặt bàn. Vui lòng thử lại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSua() {
        DatBan db = getSelectedDatBan();
        if (db == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đặt bàn cần sửa.",
                    "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maBanCu = db.getMaBan();
        DatBanDialog dlg = new DatBanDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                db, DatBanDialog.Mode.EDIT, controller);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            if (dlg.isSaved()) {
                boolean ok = controller.sua(db, maBanCu);
                if (ok && !dlg.isNavigationRequested()) {
                    JOptionPane.showMessageDialog(this, "✅ Cập nhật thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            loadData();
            // Sau Xác nhận → chuyển sang trang Bàn Hàng
            if (dlg.isNavigationRequested() && navigationCallback != null) {
                navigationCallback.accept(db.getMaBan());
            }
        }
    }

    private void handleXoa() {
        DatBan db = getSelectedDatBan();
        if (db == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đặt bàn cần xoá.",
                    "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (db.getTrangThai() != TrangThaiDatBan.HET_HAN &&
                db.getTrangThai() != TrangThaiDatBan.DA_HUY &&
                db.getTrangThai() != TrangThaiDatBan.DA_DEN) {
            JOptionPane.showMessageDialog(this,
                    "<html>⚠ Chỉ có thể ẩn đặt bàn có trạng thái <b>Hết hạn</b>, <b>Đã huỷ</b> hoặc <b>Đã đến</b>.<br>"
                            +
                            "Mã đặt bàn <b>" + db.getMaDatBan() + "</b> đang ở trạng thái: <b>"
                            + db.getTrangThai().displayName() + "</b></html>",
                    "Không thể xoá", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int xn = JOptionPane.showConfirmDialog(this,
                "<html>Ẩn đặt bàn mã <b>" + db.getMaDatBan() + "</b> (Khách: " + db.getTenKhach() + ")?<br>" +
                        "Dữ liệu vẫn lưu trên hệ thống, có thể xem lại bằng nút \"Xem đã ẩn\".</html>",
                "Xác nhận ẩn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (xn == JOptionPane.YES_OPTION) {
            if (controller.an(db.getMaDatBan())) {
                loadData();
                JOptionPane.showMessageDialog(this, "Đã ẩn đặt bàn " + db.getMaDatBan() + ".", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Không thể ẩn đặt bàn mã " + db.getMaDatBan() + ". Hãy thử lưu lại File Controller hoặc kiểm tra kết nối CSDL.", "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleLamMoi() {
        controller.autoCheckExpired();
        loadData();
        JOptionPane.showMessageDialog(this, "Đã làm mới danh sách.", "Làm mới", JOptionPane.INFORMATION_MESSAGE);
    }

    private void toggleXemAn() {
        dangXemDaAn = !dangXemDaAn;
        if (dangXemDaAn) {
            btnXemAn.setText("  Quay lại danh sách");
            btnXemAn.setIcon(IconFontSwing.buildIcon(FontAwesome.ARROW_LEFT, 13, PRIMARY));
            btnXemAn.setForeground(PRIMARY);
            btnXemAn.setBorder(new LineBorder(PRIMARY, 1));
            btnThem.setEnabled(false);
            btnSua.setEnabled(false);
            btnXoa.setEnabled(false);
        } else {
            btnXemAn.setText("  Xem đã ẩn");
            btnXemAn.setIcon(IconFontSwing.buildIcon(FontAwesome.EYE, 13, INFO));
            btnXemAn.setForeground(INFO);
            btnXemAn.setBorder(new LineBorder(INFO, 1));
            btnThem.setEnabled(true);
            btnSua.setEnabled(true);
            btnXoa.setEnabled(true);
        }
        loadData();
    }

    // ══ HELPERS ══════════════════════════════════════════════════════════════
    private JButton makeBtn(String text, FontAwesome icon, Color color) {
        JButton btn = new JButton(text, IconFontSwing.buildIcon(icon, 13, color));
        btn.setFont(new Font("Roboto", Font.BOLD, 13));
        btn.setForeground(color);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(6, 14, 6, 14)));
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel mkSep() {
        JLabel sep = new JLabel("|");
        sep.setForeground(new Color(210, 210, 210));
        return sep;
    }

    // ══ RENDERERS ════════════════════════════════════════════════════════════

    class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            if (!sel)
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return c;
        }
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            lbl.setHorizontalAlignment(CENTER);
            lbl.setFont(new Font("Roboto", Font.BOLD, 12));
            lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
            if (!sel)
                lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));

            if (val instanceof TrangThaiDatBan) {
                TrangThaiDatBan tt = (TrangThaiDatBan) val;
                String display;
                Color fg;
                switch (tt) {
                    case CHO_XAC_NHAN:
                        display = "Chờ xác nhận";
                        fg = WARNING;
                        break;
                    case DA_XAC_NHAN:
                        display = "Đã xác nhận";
                        fg = INFO;
                        break;
                    case DA_DEN:
                        display = "Đã đến";
                        fg = SUCCESS;
                        break;
                    case HET_HAN:
                        display = "Hết hạn";
                        fg = DANGER;
                        break;
                    case DA_HUY:
                        display = "Đã huỷ";
                        fg = GRAY;
                        break;
                    default:
                        display = tt.displayName();
                        fg = Color.DARK_GRAY;
                }
                lbl.setText(display);
                lbl.setForeground(sel ? Color.WHITE : fg);
            }
            return lbl;
        }
    }
}
