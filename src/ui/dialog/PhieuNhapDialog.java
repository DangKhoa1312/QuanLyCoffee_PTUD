package ui.dialog;

import controller.KhoController;
import entity.*;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import utils.CurrencyUtils;
import utils.SessionManager;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog tạo phiếu nhập kho.
 * Cho phép chọn NCC, thêm nhiều dòng nguyên liệu (với ngày hết hạn) và xác nhận nhập.
 */
public class PhieuNhapDialog extends JDialog {

    private final KhoController controller = new KhoController();

    private JComboBox<String> cbNCC;
    private JComboBox<String> cbNguyenLieu;
    private JTextField txtSoLuong, txtDonGia;
    private JLabel lblDonViTinh, lblKLDongGoi;
    private JDateChooser dateChooserHetHan;
    private JTable tableItems;
    private DefaultTableModel modelItems;
    private JLabel lblTongTien;

    private List<NhaCungCap> listNCC;
    private List<NguyenLieu> listNL;
    private final List<ChiTietPhieuNhap> chiTietList = new ArrayList<>();
    private final List<LocalDate> ngayHetHanList = new ArrayList<>();

    private boolean confirmed = false;
    private int tempCTPNCounter = 0;

    private final Color PRIMARY  = new Color(41, 128, 185);
    private final Color SUCCESS  = new Color(46, 204, 113);

    public PhieuNhapDialog(Frame owner) {
        super(owner, "Tạo Phiếu Nhập Kho", true);
        setSize(1150, 780);
        setLocationRelativeTo(owner);
        setResizable(false);

        initUI();
        loadComboData();
    }

    public boolean isConfirmed() { return confirmed; }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // ===== TOP: Thông tin phiếu nhập =====
        JPanel pnlInfo = new JPanel(new GridBagLayout());
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1: NCC
        gbc.gridx = 0; gbc.gridy = 0;
        pnlInfo.add(createLabel("Nhà cung cấp:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.gridwidth = 3;
        cbNCC = new JComboBox<>();
        cbNCC.setPreferredSize(new Dimension(400, 32));
        pnlInfo.add(cbNCC, gbc);
        gbc.gridwidth = 1;

        mainPanel.add(pnlInfo, BorderLayout.NORTH);

        // ===== CENTER: Thêm dòng + Bảng =====
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        // Dòng thêm nguyên liệu (2 hàng)
        JPanel pnlAddRow = new JPanel();
        pnlAddRow.setLayout(new BoxLayout(pnlAddRow, BoxLayout.Y_AXIS));
        pnlAddRow.setBackground(Color.WHITE);
        pnlAddRow.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(8, 10, 8, 10)
        ));

        // Row 1: Nguyên liệu + SL + Đơn giá
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row1.setOpaque(false);

        row1.add(createLabel("Nguyên liệu:"));
        cbNguyenLieu = new JComboBox<>();
        cbNguyenLieu.setPreferredSize(new Dimension(220, 32));
        row1.add(cbNguyenLieu);

        row1.add(createLabel("  Số lượng:"));
        txtSoLuong = new JTextField(6);
        txtSoLuong.setPreferredSize(new Dimension(0, 32));
        row1.add(txtSoLuong);

        lblDonViTinh = new JLabel("");
        lblDonViTinh.setFont(new Font("Roboto", Font.BOLD, 13));
        lblDonViTinh.setForeground(new Color(41, 128, 185));
        lblDonViTinh.setPreferredSize(new Dimension(80, 32));
        row1.add(lblDonViTinh);

        row1.add(createLabel("  KL Đóng Gói:"));
        lblKLDongGoi = new JLabel("");
        lblKLDongGoi.setFont(new Font("Roboto", Font.BOLD, 13));
        lblKLDongGoi.setForeground(new Color(39, 174, 96));
        lblKLDongGoi.setPreferredSize(new Dimension(80, 32));
        row1.add(lblKLDongGoi);

        row1.add(createLabel("  Đơn giá:"));
        txtDonGia = new JTextField(8);
        txtDonGia.setPreferredSize(new Dimension(0, 32));
        row1.add(txtDonGia);

        pnlAddRow.add(row1);

        // Row 2: Ngày hết hạn (JDateChooser) + Nút thêm
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row2.setOpaque(false);

        row2.add(createLabel("Ngày hết hạn:"));
        dateChooserHetHan = new JDateChooser();
        dateChooserHetHan.setPreferredSize(new Dimension(180, 32));
        dateChooserHetHan.setDateFormatString("dd/MM/yyyy");
        dateChooserHetHan.setFont(new Font("Roboto", Font.PLAIN, 13));
        // Đặt ngày tối thiểu là ngày hiện tại
        dateChooserHetHan.setMinSelectableDate(new Date());
        row2.add(dateChooserHetHan);

        JButton btnAddLine = new JButton("Thêm dòng");
        btnAddLine.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 15, Color.WHITE));
        btnAddLine.setBackground(SUCCESS);
        btnAddLine.setForeground(Color.WHITE);
        btnAddLine.setFont(new Font("Roboto", Font.BOLD, 14));
        btnAddLine.setPreferredSize(new Dimension(180, 42));
        btnAddLine.setFocusable(false);
        btnAddLine.addActionListener(e -> addItemRow());
        row2.add(btnAddLine);

        // === Nút "Nhập từ file CSV" ===
        JButton btnCSV = new JButton("\uD83D\uDCC2 Nhập từ file CSV");
        btnCSV.setBackground(new Color(52, 152, 219));
        btnCSV.setForeground(Color.WHITE);
        btnCSV.setFont(new Font("Roboto", Font.BOLD, 14));
        btnCSV.setPreferredSize(new Dimension(200, 42));
        btnCSV.setFocusable(false);
        btnCSV.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCSV.addActionListener(e -> importFromCSV());
        row2.add(btnCSV);

        pnlAddRow.add(row2);

        centerPanel.add(pnlAddRow, BorderLayout.NORTH);

        // Bảng chi tiết
        String[] cols = {"Mã NL", "Tên NL", "Số lượng", "Đơn giá", "Thành tiền", "Ngày hết hạn", "Xoá"};
        modelItems = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableItems = new JTable(modelItems);
        tableItems.setRowHeight(45);
        tableItems.setFont(new Font("Roboto", Font.PLAIN, 14));
        tableItems.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 13));
        tableItems.getTableHeader().setBackground(new Color(236, 240, 241));

        // Nút xoá dòng
        tableItems.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tableItems.columnAtPoint(e.getPoint());
                int row = tableItems.rowAtPoint(e.getPoint());
                if (col == 6 && row >= 0) {
                    chiTietList.remove(row);
                    ngayHetHanList.remove(row);
                    refreshTable();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableItems);
        scroll.setBorder(new LineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scroll, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ===== BOTTOM: Tổng tiền + Nút =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblTongTien = new JLabel("Tổng tiền: 0 đ");
        lblTongTien.setFont(new Font("Roboto", Font.BOLD, 16));
        lblTongTien.setForeground(new Color(44, 62, 80));
        bottom.add(lblTongTien, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = new JButton("Huỷ");
        btnCancel.setFont(new Font("Roboto", Font.BOLD, 13));
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = new JButton("  Xác nhận Nhập Kho");
        btnConfirm.setIcon(IconFontSwing.buildIcon(FontAwesome.CHECK, 14, Color.WHITE));
        btnConfirm.setBackground(PRIMARY);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Roboto", Font.BOLD, 14));
        btnConfirm.setPreferredSize(new Dimension(220, 38));
        btnConfirm.setFocusable(false);
        btnConfirm.addActionListener(e -> handleConfirm());

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        bottom.add(btnPanel, BorderLayout.EAST);

        mainPanel.add(bottom, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void loadComboData() {
        listNCC = controller.getAllNhaCungCap();
        for (NhaCungCap ncc : listNCC) {
            cbNCC.addItem(ncc.getMaNCC() + " - " + ncc.getTenNCC());
        }

        listNL = controller.getAllNguyenLieu();
        for (NguyenLieu nl : listNL) {
            cbNguyenLieu.addItem(nl.getMaNL() + " - " + nl.getTenNL());
        }

        // Auto-fill đơn giá khi chọn nguyên liệu
        cbNguyenLieu.addActionListener(e -> autoFillDonGia());

        // Set initial value
        if (!listNL.isEmpty()) {
            autoFillDonGia();
        }
    }

    private void autoFillDonGia() {
        int idx = cbNguyenLieu.getSelectedIndex();
        if (idx >= 0 && idx < listNL.size()) {
            txtDonGia.setText(String.valueOf((long) listNL.get(idx).getDonGiaNhap()));
            String dvdg = listNL.get(idx).getDonViDongGoi();
            lblDonViTinh.setText(dvdg != null ? dvdg : "");
            double klDG = listNL.get(idx).getKhoiLuongDongGoi();
            lblKLDongGoi.setText(klDG > 0 ? String.valueOf((long) klDG) : "");
        } else {
            lblDonViTinh.setText("");
        }
    }

    private void addItemRow() {
        int nlIdx = cbNguyenLieu.getSelectedIndex();
        if (nlIdx < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nguyên liệu.");
            return;
        }

        double soLuong, donGia;
        try {
            soLuong = Double.parseDouble(txtSoLuong.getText().trim());
            donGia = Double.parseDouble(txtDonGia.getText().trim());
            if (soLuong <= 0 || donGia < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng và đơn giá phải là số hợp lệ > 0.");
            return;
        }

        // Validate ngày hết hạn bằng JDateChooser
        Date selectedDate = dateChooserHetHan.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày hết hạn.");
            return;
        }
        LocalDate ngayHH = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (ngayHH.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Ngày hết hạn không được nhỏ hơn ngày hiện tại (" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").");
            return;
        }

        NguyenLieu nl = listNL.get(nlIdx);

        // Kiểm tra trùng
        for (ChiTietPhieuNhap ct : chiTietList) {
            if (ct.getMaNL().equals(nl.getMaNL())) {
                JOptionPane.showMessageDialog(this,
                        "Nguyên liệu " + nl.getTenNL() + " đã có trong danh sách.\nVui lòng xoá dòng cũ trước khi thêm lại.");
                return;
            }
        }

        // Dùng counter tạm thay vì gọi DB (tránh trùng mã khi chưa lưu)
        tempCTPNCounter++;
        ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
        ct.setMaCTPN("TEMP_" + tempCTPNCounter);
        ct.setSoLuong(soLuong);
        ct.setDonGia(donGia);
        ct.setThanhTien(soLuong * donGia);
        ct.setMaNL(nl.getMaNL());
        chiTietList.add(ct);
        ngayHetHanList.add(ngayHH);

        refreshTable();
        txtSoLuong.setText("");
        txtDonGia.setText("");
        dateChooserHetHan.setDate(null);
    }

    private void refreshTable() {
        modelItems.setRowCount(0);
        double tongTien = 0;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int i = 0; i < chiTietList.size(); i++) {
            ChiTietPhieuNhap ct = chiTietList.get(i);
            NguyenLieu nl = controller.getNguyenLieuById(ct.getMaNL());
            String tenNL = nl != null ? nl.getTenNL() : ct.getMaNL();
            String ngayHH = i < ngayHetHanList.size() ? ngayHetHanList.get(i).format(fmt) : "";
            modelItems.addRow(new Object[]{
                ct.getMaNL(), tenNL,
                String.format("%.1f", ct.getSoLuong()),
                CurrencyUtils.formatNoUnit(ct.getDonGia()),
                CurrencyUtils.formatNoUnit(ct.getThanhTien()),
                ngayHH,
                "❌ Xoá"
            });
            tongTien += ct.getThanhTien();
        }
        lblTongTien.setText("Tổng tiền: " + CurrencyUtils.formatNoUnit(tongTien) + " đ");
    }

    private void handleConfirm() {
        if (chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có nguyên liệu nào trong phiếu nhập!");
            return;
        }
        if (cbNCC.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Nhà Cung Cấp.");
            return;
        }

        NhaCungCap ncc = listNCC.get(cbNCC.getSelectedIndex());

        // Lấy kho đầu tiên làm mặc định
        List<Kho> listKho = controller.getAllKho();
        Kho kho = listKho.isEmpty() ? null : listKho.get(0);

        if (kho == null) {
            JOptionPane.showMessageDialog(this, "Chưa có kho nào trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double tongTien = 0;
        for (ChiTietPhieuNhap ct : chiTietList) tongTien += ct.getThanhTien();

        // Gán mã CTPN thật từ DB cho tất cả items
        for (int i = 0; i < chiTietList.size(); i++) {
            String realMaCTPN = controller.generateNextMaCTPN();
            chiTietList.get(i).setMaCTPN(realMaCTPN);
        }

        // Cập nhật ngày hết hạn cho từng nguyên liệu
        for (int i = 0; i < chiTietList.size(); i++) {
            if (i < ngayHetHanList.size()) {
                NguyenLieu nl = controller.getNguyenLieuById(chiTietList.get(i).getMaNL());
                if (nl != null) {
                    nl.setNgayHetHan(ngayHetHanList.get(i));
                    controller.updateNguyenLieu(nl);
                }
            }
        }

        PhieuNhap pn = new PhieuNhap();
        pn.setMaPN(controller.generateNextMaPN());
        pn.setNgayNhap(LocalDateTime.now());
        pn.setTongTien(tongTien);
        pn.setMaNV(SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getMaNV() : "NV001");
        pn.setMaNCC(ncc.getMaNCC());
        pn.setMaKho(kho.getMaKho());

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Xác nhận nhập kho?\n\nMã PN: %s\nNCC: %s\nTổng tiền: %s đ\nSố dòng: %d",
                        pn.getMaPN(), ncc.getTenNCC(),
                        CurrencyUtils.formatNoUnit(tongTien), chiTietList.size()),
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.processNhapKho(pn, chiTietList)) {
                confirmed = true;
                JOptionPane.showMessageDialog(this,
                        "✅ Nhập kho thành công!\nMã phiếu: " + pn.getMaPN(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Lỗi khi nhập kho! Kiểm tra lại dữ liệu.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ====================================================================
    // CHỨC NĂNG NHẬP TỪ FILE CSV
    // ====================================================================

    /**
     * Mở hộp thoại chọn file CSV, đọc và parse dữ liệu,
     * validate từng dòng rồi đẩy vào danh sách chi tiết phiếu nhập.
     *
     * Cấu trúc file CSV yêu cầu (có header):
     *   MaNguyenLieu, TenNguyenLieu, DonViDongGoi, KLDongGoi, DonGia, NgayHetHan
     *
     * Quy tắc:
     *  - Mã NL phải tồn tại trong hệ thống.
     *  - KLDongGoi (= Số lượng nhập) và DonGia phải > 0.
     *  - NgayHetHan phải đúng định dạng dd/MM/yyyy.
     */
    private void importFromCSV() {
        // 1. Mở JFileChooser – chỉ cho phép chọn file .csv
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file CSV nguyên liệu nhập kho");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return; // Người dùng huỷ
        }

        File csvFile = chooser.getSelectedFile();

        // 2. Đọc và parse file CSV
        List<Object[]> validItems = new ArrayList<>();
        StringBuilder errors = new StringBuilder();
        int lineNum = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.trim();

                // Bỏ qua BOM nếu có ở dòng đầu
                if (lineNum == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                // Bỏ qua dòng trống
                if (line.isEmpty()) continue;

                // Bỏ qua dòng header (dòng đầu tiên không trống)
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // 3. Tách các cột bằng dấu phẩy (hỗ trợ trường có dấu ngoặc kép)
                String[] cols = parseCSVLine(line);
                if (cols.length < 6) {
                    errors.append("Dòng ").append(lineNum).append(": Thiếu cột (cần 6 cột).\n");
                    continue;
                }

                String maNL       = cols[0].trim();
                // cols[1] = TenNguyenLieu (chỉ để tham khảo)
                // cols[2] = DonViDongGoi  (tham khảo)
                String klStr      = cols[3].trim();
                String donGiaStr  = cols[4].trim();
                String ngayHHStr  = cols[5].trim();

                // 4. Validate: Mã NL phải tồn tại trong danh mục hệ thống
                NguyenLieu nlFound = null;
                for (NguyenLieu nl : listNL) {
                    if (nl.getMaNL().equalsIgnoreCase(maNL)) {
                        nlFound = nl;
                        break;
                    }
                }
                if (nlFound == null) {
                    errors.append("Dòng ").append(lineNum)
                          .append(": Mã NL '").append(maNL)
                          .append("' không tồn tại trong hệ thống.\n");
                    continue;
                }

                // 5. Validate: Số lượng và Đơn giá phải là số > 0
                double soLuong, donGia;
                try {
                    soLuong = Double.parseDouble(klStr);
                    if (soLuong <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    errors.append("Dòng ").append(lineNum)
                          .append(": Số lượng '").append(klStr)
                          .append("' không hợp lệ (phải > 0).\n");
                    continue;
                }
                try {
                    donGia = Double.parseDouble(donGiaStr);
                    if (donGia <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    errors.append("Dòng ").append(lineNum)
                          .append(": Đơn giá '").append(donGiaStr)
                          .append("' không hợp lệ (phải > 0).\n");
                    continue;
                }
                
                LocalDate ngayHetHan;
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    ngayHetHan = LocalDate.parse(ngayHHStr, formatter);
                } catch (Exception ex) {
                    errors.append("Dòng ").append(lineNum)
                          .append(": Ngày hết hạn '").append(ngayHHStr)
                          .append("' không hợp lệ (định dạng dd/MM/yyyy).\n");
                    continue;
                }

                validItems.add(new Object[]{nlFound.getMaNL(), soLuong, donGia, ngayHetHan});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi đọc file CSV:\n" + ex.getMessage(),
                "Lỗi đọc file", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 6. Hiển thị cảnh báo lỗi (nếu có)
        if (errors.length() > 0) {
            JTextArea ta = new JTextArea(errors.toString());
            ta.setEditable(false);
            ta.setRows(10);
            ta.setColumns(50);
            JScrollPane sp = new JScrollPane(ta);
            JOptionPane.showMessageDialog(this, sp,
                "\u26A0 Cảnh báo: Một số dòng bị lỗi", JOptionPane.WARNING_MESSAGE);
        }

        if (validItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Không có dòng hợp lệ nào trong file CSV.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 7. Đẩy dữ liệu vào danh sách chi tiết phiếu nhập
        int addedCount = 0;
        for (Object[] item : validItems) {
            String maNL = (String) item[0];
            double sl   = (Double) item[1];
            double dg   = (Double) item[2];
            LocalDate ngayHH = (LocalDate) item[3];

            tempCTPNCounter++;
            ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
            ct.setMaCTPN("TEMP_" + tempCTPNCounter);
            ct.setSoLuong(sl);
            ct.setDonGia(dg);
            ct.setThanhTien(sl * dg);
            ct.setMaNL(maNL);

            chiTietList.add(ct);
            ngayHetHanList.add(ngayHH);
            addedCount++;
        }

        // 8. Cập nhật bảng
        refreshTable();

        // 9. Thông báo kết quả
        String msg = String.format("\u2705 Nhập CSV thành công!\n- Đã thêm mới: %d dòng", addedCount);
        JOptionPane.showMessageDialog(this, msg, "Kết quả nhập CSV", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Parse một dòng CSV, hỗ trợ trường có dấu ngoặc kép (quoted fields).
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", Font.BOLD, 13));
        lbl.setForeground(new Color(44, 62, 80));
        return lbl;
    }
}
