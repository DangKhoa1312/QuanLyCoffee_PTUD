package ui.panel;

import controller.InvoiceController;
import entity.HoaDon;
import enums.HinhThucThanhToan;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình Quản Lý Hoá Đơn — nâng cấp bộ lọc nâng cao.
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

    public InvoicePanel() {
        this.invoiceController = new InvoiceController();
        setLayout(new BorderLayout());
        setBackground(BG);
        initUI();
        loadData();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 30, 10, 30));

        JLabel lblTitle = new JLabel("📜  Lịch Sử Hóa Đơn");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = makeFilledBtn("↻ Làm Mới", SUCCESS);
        btnRefresh.addActionListener(e -> loadData());
        header.add(btnRefresh, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Filter panel
        JPanel filterWrapper = new JPanel(new BorderLayout());
        filterWrapper.setOpaque(false);
        filterWrapper.setBorder(new EmptyBorder(0, 30, 10, 30));
        filterWrapper.add(buildFilterPanel(), BorderLayout.CENTER);
        add(filterWrapper, BorderLayout.CENTER);

        // Table content (placeholder — sẽ được thay khi init xong)
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(0, 30, 30, 30));

        String[] cols = {
            "Mã Hóa Đơn",
            "Bàn",
            "Loại đơn",
            "Tổng Tiền",
            "T.Gian Th.Toán",
            "Hình Thức",
            "Trạng thái",
            "Thu Ngân"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(232, 245, 253));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        content.add(scroll, BorderLayout.CENTER);

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

        // Dùng BorderLayout để filter + table chồng nhau
        // Cần đặt lại layout tổng
        removeAll();
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        JPanel centerPane = new JPanel(new BorderLayout());
        centerPane.setOpaque(false);
        centerPane.add(buildFilterPanel(), BorderLayout.NORTH);
        centerPane.add(content, BorderLayout.CENTER);
        add(centerPane, BorderLayout.CENTER);
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            new EmptyBorder(4, 20, 4, 20)
        ));

        // Từ ngày
        panel.add(new JLabel("Từ:"));
        dcTuNgay = new JDateChooser();
        dcTuNgay.setDateFormatString("dd/MM/yyyy");
        dcTuNgay.setPreferredSize(new Dimension(120, 30));
        panel.add(dcTuNgay);

        panel.add(new JLabel("Đến:"));
        dcDenNgay = new JDateChooser();
        dcDenNgay.setDateFormatString("dd/MM/yyyy");
        dcDenNgay.setPreferredSize(new Dimension(120, 30));
        panel.add(dcDenNgay);

        // Hình thức thanh toán
        panel.add(new JLabel("Hình thức:"));
        cbHinhThuc = new JComboBox<>(new String[]{"Tất cả", "TIEN_MAT", "CHUYEN_KHOAN"});
        cbHinhThuc.setFont(new Font("Roboto", Font.PLAIN, 13));
        cbHinhThuc.setPreferredSize(new Dimension(155, 30));
        // Hiển thị label đẹp
        cbHinhThuc.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if ("TIEN_MAT".equals(value))          setText("Tiền mặt");
                else if ("CHUYEN_KHOAN".equals(value)) setText("Chuyển khoản");
                return this;
            }
        });
        panel.add(cbHinhThuc);

        // Bàn
        panel.add(new JLabel("Bàn:"));
        txtBan = new JTextField(8);
        txtBan.setFont(new Font("Roboto", Font.PLAIN, 13));
        txtBan.setPreferredSize(new Dimension(90, 30));
        txtBan.putClientProperty("JTextField.placeholderText", "Số bàn...");
        panel.add(txtBan);

        // Nhân viên
        panel.add(new JLabel("Nhân viên:"));
        txtNhanVien = new JTextField(8);
        txtNhanVien.setFont(new Font("Roboto", Font.PLAIN, 13));
        txtNhanVien.setPreferredSize(new Dimension(90, 30));
        txtNhanVien.putClientProperty("JTextField.placeholderText", "Tên NV...");
        panel.add(txtNhanVien);

        // Nút Tìm
        JButton btnSearch = makeFilledBtn("🔍 Tìm", INFO);
        btnSearch.addActionListener(e -> applyFilter());
        panel.add(btnSearch);

        // Nút Xoá bộ lọc
        JButton btnClear = makeFilledBtn("✕ Xoá lọc", DANGER);
        btnClear.addActionListener(e -> {
            dcTuNgay.setDate(null);
            dcDenNgay.setDate(null);
            cbHinhThuc.setSelectedIndex(0);
            txtBan.setText("");
            txtNhanVien.setText("");
            loadData();
        });
        panel.add(btnClear);

        return panel;
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
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        return btn;
    }
}
