package ui.panel;

import controller.InvoiceController;
import entity.HoaDon;
import enums.HinhThucThanhToan;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình Quản Lý Hoá Đơn — bộ lọc nâng cao, responsive mọi tỉ lệ màn hình.
 */
public class InvoicePanel extends JPanel {

    private final InvoiceController invoiceController;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<HoaDon> currentList;

    // Bộ lọc
    private JDateChooser dcTuNgay, dcDenNgay;
    private JComboBox<String> cbHinhThuc;
    private JTextField txtBan, txtNhanVien;

    private static final Color PRIMARY  = new Color(113, 76, 52);
    private static final Color SUCCESS  = new Color(46, 204, 113);
    private static final Color DANGER   = new Color(231, 76, 60);
    private static final Color INFO     = new Color(41, 128, 185);
    private static final Color BG       = new Color(245, 247, 250);

    private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Timer để chống dội (debounce) khi gõ phím
    private Timer debounceTimer;

    public InvoicePanel() {
        this.invoiceController = new InvoiceController();
        setLayout(new BorderLayout());
        setBackground(BG);
        initUI();
        loadData();
    }

    private void initUI() {
        // ── HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 30, 10, 30));

        JLabel lblTitle = new JLabel("\uD83D\uDCDC  Lịch Sử Hóa Đơn");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = makeFilledBtn("\u21BB Làm Mới", SUCCESS);
        btnRefresh.addActionListener(e -> loadData());
        header.add(btnRefresh, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── CENTER = Filter + Table ──
        JPanel centerPane = new JPanel(new BorderLayout(0, 0));
        centerPane.setOpaque(false);
        centerPane.setBorder(new EmptyBorder(0, 30, 25, 30));

        centerPane.add(buildFilterPanel(), BorderLayout.NORTH);

        // ── TABLE ──
        String[] cols = {
            "Mã Hóa Đơn", "Bàn", "Loại đơn", "Tổng Tiền",
            "T.Gian Th.Toán", "Hình Thức", "Trạng thái", "Thu Ngân"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(38);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(71, 85, 105));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(new Color(15, 23, 42));
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(false);

        // Căn phải cho cột Tổng Tiền
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);

        centerPane.add(scroll, BorderLayout.CENTER);
        add(centerPane, BorderLayout.CENTER);

        // Double-click to view detail
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && currentList != null) {
                        HoaDon hd = currentList.get(row);
                        Window win = SwingUtilities.getWindowAncestor(InvoicePanel.this);
                        if (win instanceof JFrame) {
                            ui.dialog.InvoiceDetailDialog dlg = new ui.dialog.InvoiceDetailDialog((JFrame) win, hd);
                            dlg.setVisible(true);
                        }
                    }
                }
            }
        });
    }

    /**
     * Filter bar dùng cấu trúc chuẩn: Nhãn nằm trên ô nhập liệu (Top-Bottom Labels).
     * Dùng FlowLayout để tự động wrap khi thu nhỏ cửa sổ, tránh vỡ Layout.
     */
    private JPanel buildFilterPanel() {
        // Khởi tạo Timer debounce cho TextFields (500ms)
        debounceTimer = new Timer(500, e -> applyFilter());
        debounceTimer.setRepeats(false);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);
        outer.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 10, 0, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;
        g.gridy = 0;

        // 1. Từ ngày
        g.gridx = 0; g.weightx = 1.2;
        dcTuNgay = new JDateChooser();
        dcTuNgay.setDateFormatString("dd/MM/yyyy");
        dcTuNgay.setPreferredSize(new Dimension(0, 36));
        dcTuNgay.addPropertyChangeListener("date", e -> applyFilter());
        outer.add(buildCol("Từ ngày", dcTuNgay), g);

        // 2. Đến ngày
        g.gridx = 1; g.weightx = 1.2;
        dcDenNgay = new JDateChooser();
        dcDenNgay.setDateFormatString("dd/MM/yyyy");
        dcDenNgay.setPreferredSize(new Dimension(0, 36));
        dcDenNgay.addPropertyChangeListener("date", e -> applyFilter());
        outer.add(buildCol("Đến ngày", dcDenNgay), g);

        // 3. Hình thức
        g.gridx = 2; g.weightx = 1.5;
        cbHinhThuc = new JComboBox<>(new String[]{"Tất cả", "TIEN_MAT", "CHUYEN_KHOAN"});
        cbHinhThuc.setFont(new Font("Roboto", Font.PLAIN, 13));
        cbHinhThuc.setPreferredSize(new Dimension(0, 36));
        cbHinhThuc.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if ("TIEN_MAT".equals(value))          setText("💵 Tiền mặt");
                else if ("CHUYEN_KHOAN".equals(value)) setText("💳 Chuyển khoản");
                else                                    setText("Tất cả");
                return this;
            }
        });
        cbHinhThuc.addActionListener(e -> applyFilter());
        outer.add(buildCol("Hình thức", cbHinhThuc), g);

        // Hàm tiện ích tạo DocumentListener cho TextFields
        javax.swing.event.DocumentListener docListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { debounceTimer.restart(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { debounceTimer.restart(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { debounceTimer.restart(); }
        };

        // 4. Bàn
        g.gridx = 3; g.weightx = 1.0;
        txtBan = new JTextField();
        txtBan.setFont(new Font("Roboto", Font.PLAIN, 13));
        txtBan.setPreferredSize(new Dimension(0, 36));
        txtBan.putClientProperty("JTextField.placeholderText", "Số bàn...");
        txtBan.getDocument().addDocumentListener(docListener);
        outer.add(buildCol("Bàn", txtBan), g);

        // 5. Thu ngân
        g.gridx = 4; g.weightx = 1.2;
        txtNhanVien = new JTextField();
        txtNhanVien.setFont(new Font("Roboto", Font.PLAIN, 13));
        txtNhanVien.setPreferredSize(new Dimension(0, 36));
        txtNhanVien.putClientProperty("JTextField.placeholderText", "Tên NV...");
        txtNhanVien.getDocument().addDocumentListener(docListener);
        outer.add(buildCol("Thu ngân", txtNhanVien), g);

        // 6. Khu vực Nút bấm (nằm góc phải)
        g.gridx = 5; g.weightx = 0; 
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.SOUTH; // Căn dưới cùng để thẳng hàng với input
        g.insets = new Insets(0, 10, 0, 0); // Bỏ gap phải

        JButton btnClear = makeFilledBtn("✕ Xoá bộ lọc", new Color(254, 226, 226)); 
        btnClear.setForeground(new Color(220, 38, 38)); 
        btnClear.setPreferredSize(new Dimension(130, 36));
        btnClear.addActionListener(e -> {
            dcTuNgay.setDate(null);
            dcDenNgay.setDate(null);
            cbHinhThuc.setSelectedIndex(0);
            txtBan.setText("");
            txtNhanVien.setText("");
        });
        
        outer.add(btnClear, g);

        return outer;
    }

    private JPanel buildCol(String labelText, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Roboto", Font.BOLD, 13));
        lbl.setForeground(new Color(71, 85, 105));
        p.add(lbl, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    public void loadData() {
        fillTable(invoiceController.getAllHoaDon());
    }

    private void applyFilter() {
        LocalDate tuNgay  = toLocalDate(dcTuNgay.getDate());
        LocalDate denNgay = toLocalDate(dcDenNgay.getDate());
        String hinhThuc   = "Tất cả".equals(cbHinhThuc.getSelectedItem()) ? null
                            : (String) cbHinhThuc.getSelectedItem();
        String maBan      = txtBan.getText().trim().isEmpty() ? null : txtBan.getText().trim();
        String maNV       = txtNhanVien.getText().trim().isEmpty() ? null : txtNhanVien.getText().trim();

        List<HoaDon> filtered = invoiceController.getHoaDonByFilter(tuNgay, denNgay, hinhThuc, maBan, maNV);
        fillTable(filtered);
    }

    private void fillTable(List<HoaDon> list) {
        tableModel.setRowCount(0);
        currentList = list;
        for (HoaDon hd : list) {
            String time = hd.getThoiGianThanhToan() != null
                          ? hd.getThoiGianThanhToan().format(dtf)
                          : hd.getThoiGianXuat().format(dtf);
            // Sử dụng getLabel() trực tiếp từ enum
            String hinhThucLabel = hd.getHinhThucThanhToan() != null
                                   ? hd.getHinhThucThanhToan().getLabel() : "";
            tableModel.addRow(new Object[]{
                hd.getMaHD(),
                hd.getSoBan() != null ? hd.getSoBan() : (hd.getMaBan() != null ? hd.getMaBan() : "Mang về"),
                hd.getLoaiDon() != null ? hd.getLoaiDon().getLabel() : "",
                nf.format(hd.getTongTienPhaiTra()) + " đ",
                time,
                hinhThucLabel,
                hd.getTrangThai().getLabel(),
                hd.getTenNV() != null ? hd.getTenNV() : (hd.getMaNV() != null ? hd.getMaNV() : "")
            });
        }
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private JButton makeFilledBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Roboto", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }
}
