package ui.panel.admin;

import controller.KhoController;
import entity.ChiTietPhieuNhap;
import entity.Kho;
import entity.NguyenLieu;
import entity.NhaCungCap;
import entity.PhieuNhap;
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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreatePhieuNhapPanel extends JPanel {

    private final KhoController controller = new KhoController();
    private Runnable onBackAction;

    // Left Panel (Menu)
    private JTable nlTable;
    private DefaultTableModel nlModel;
    private JTextField txtSearch;
    
    // Left Form
    private JLabel lblSelectedNL;
    private JTextField txtSoLuong;
    private JTextField txtDonGia;
    private JDateChooser dateChooser;
    private NguyenLieu currentSelectedNL;

    // Right Panel (Cart)
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JComboBox<String> cbNCC;
    private JLabel lblTotal;

    private List<NguyenLieu> listNL;
    private List<NhaCungCap> listNCC;
    private final List<ChiTietPhieuNhap> chiTietList = new ArrayList<>();
    private final List<LocalDate> ngayHetHanList = new ArrayList<>();
    private int tempCounter = 0;

    public CreatePhieuNhapPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        initUI();
        loadData();
    }

    public void setOnBackAction(Runnable onBackAction) {
        this.onBackAction = onBackAction;
    }

    public void refresh() {
        chiTietList.clear();
        ngayHetHanList.clear();
        refreshCartTable();
        loadData();
        clearForm();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton btnBack = new JButton("⬅ Quay lại");
        btnBack.setFont(new Font("Roboto", Font.BOLD, 15));
        btnBack.setBackground(null);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            if (onBackAction != null) onBackAction.run();
        });
        header.add(btnBack, BorderLayout.WEST);

        JLabel lblTitle = new JLabel("TẠO PHIẾU NHẬP KHO", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 22));
        lblTitle.setForeground(new Color(41, 128, 185));
        header.add(lblTitle, BorderLayout.CENTER);

        // Right side of header spacer
        JLabel spacer = new JLabel();
        spacer.setPreferredSize(new Dimension(100, 10));
        header.add(spacer, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        splitPane.setLeftComponent(createLeftPanel());
        splitPane.setRightComponent(createRightPanel());

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBorder(new EmptyBorder(0, 20, 20, 20));
        centerContainer.setOpaque(false);
        centerContainer.add(splitPane, BorderLayout.CENTER);
        
        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 10));

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm NL:"), BorderLayout.WEST);
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(0, 35));
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterNL(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterNL(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterNL(); }
        });
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Table
        nlModel = new DefaultTableModel(new String[]{"Mã", "Tên NL", "ĐV Đóng Gói", "Tồn"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        nlTable = new JTable(nlModel);
        nlTable.setRowHeight(35);
        nlTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && nlTable.getSelectedRow() != -1) {
                selectNL(nlTable.getSelectedRow());
            }
        });
        JScrollPane scroll = new JScrollPane(nlTable);
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        // Form Add to Cart
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Đang chọn:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        lblSelectedNL = new JLabel("-- Chưa chọn --");
        lblSelectedNL.setFont(new Font("Roboto", Font.BOLD, 14));
        lblSelectedNL.setForeground(new Color(41, 128, 185));
        formPanel.add(lblSelectedNL, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Số lượng nhập (Gói/Hộp):"), gbc);
        gbc.gridx = 1;
        txtSoLuong = new JTextField();
        txtSoLuong.setPreferredSize(new Dimension(0, 30));
        formPanel.add(txtSoLuong, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Đơn giá nhập:"), gbc);
        gbc.gridx = 1;
        txtDonGia = new JTextField();
        txtDonGia.setPreferredSize(new Dimension(0, 30));
        formPanel.add(txtDonGia, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Hạn sử dụng:"), gbc);
        gbc.gridx = 1;
        dateChooser = new JDateChooser();
        dateChooser.setPreferredSize(new Dimension(0, 30));
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setMinSelectableDate(new Date());
        formPanel.add(dateChooser, gbc);

        // === Nút "Thêm vào phiếu" ===
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton btnAdd = new JButton("Thêm vào phiếu \u2794");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 14));
        btnAdd.setPreferredSize(new Dimension(0, 40));
        btnAdd.addActionListener(e -> addToCart());
        formPanel.add(btnAdd, gbc);

        // === Nút "Nhập từ file CSV" ===
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        JButton btnCSV = new JButton("\uD83D\uDCC2 Nhập từ file CSV");
        btnCSV.setBackground(new Color(52, 152, 219));
        btnCSV.setForeground(Color.WHITE);
        btnCSV.setFont(new Font("Roboto", Font.BOLD, 14));
        btnCSV.setPreferredSize(new Dimension(0, 40));
        btnCSV.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCSV.addActionListener(e -> importFromCSV());
        formPanel.add(btnCSV, gbc);

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 10, 0, 0));

        // Info Header
        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("Chọn Nhà cung cấp:"), BorderLayout.WEST);
        cbNCC = new JComboBox<>();
        cbNCC.setPreferredSize(new Dimension(0, 35));
        infoPanel.add(cbNCC, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.NORTH);

        // Cart Table
        cartModel = new DefaultTableModel(new String[]{"Mã", "Tên", "SL", "Đơn Giá", "Thành Tiền", "Xóa"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(40);
        cartTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = cartTable.columnAtPoint(e.getPoint());
                int row = cartTable.rowAtPoint(e.getPoint());
                if (col == 5 && row >= 0) {
                    chiTietList.remove(row);
                    ngayHetHanList.remove(row);
                    refreshCartTable();
                }
            }
        });
        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230)),
            new EmptyBorder(15, 15, 15, 15)
        ));

        lblTotal = new JLabel("Tổng tiền: 0 đ");
        lblTotal.setFont(new Font("Roboto", Font.BOLD, 18));
        lblTotal.setForeground(new Color(231, 76, 60));
        footerPanel.add(lblTotal, BorderLayout.WEST);

        JButton btnConfirm = new JButton("Xác nhận Lưu Phiếu");
        btnConfirm.setBackground(new Color(41, 128, 185));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Roboto", Font.BOLD, 15));
        btnConfirm.setPreferredSize(new Dimension(180, 45));
        btnConfirm.addActionListener(e -> savePhieuNhap());
        footerPanel.add(btnConfirm, BorderLayout.EAST);

        panel.add(footerPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadData() {
        listNL = controller.getAllNguyenLieu();
        filterNL(); // populate left table

        listNCC = controller.getAllNhaCungCap();
        cbNCC.removeAllItems();
        for (NhaCungCap ncc : listNCC) {
            cbNCC.addItem(ncc.getMaNCC() + " - " + ncc.getTenNCC());
        }
    }

    private void filterNL() {
        nlModel.setRowCount(0);
        String kw = txtSearch.getText().toLowerCase().trim();
        List<entity.TonKho> allTK = controller.getAllTonKho();
        
        for (NguyenLieu nl : listNL) {
            if (kw.isEmpty() || nl.getTenNL().toLowerCase().contains(kw) || nl.getMaNL().toLowerCase().contains(kw)) {
                double ton = 0;
                for (entity.TonKho tk : allTK) {
                    if (tk.getMaNL().equals(nl.getMaNL())) {
                        ton = tk.getSoLuongTon();
                        break;
                    }
                }
                double packagesQty = ton;
                if (nl.getKhoiLuongDongGoi() > 0) {
                    packagesQty = ton / nl.getKhoiLuongDongGoi();
                }
                String displayTon = String.format("%.1f (%s %s)", packagesQty, String.format("%.1f", ton), nl.getDonViTinh() != null ? nl.getDonViTinh() : "");
                nlModel.addRow(new Object[]{
                    nl.getMaNL(), nl.getTenNL(), nl.getDonViDongGoi(), displayTon
                });
            }
        }
    }

    private void selectNL(int row) {
        String maNL = (String) nlModel.getValueAt(row, 0);
        for (NguyenLieu nl : listNL) {
            if (nl.getMaNL().equals(maNL)) {
                currentSelectedNL = nl;
                lblSelectedNL.setText(nl.getTenNL());
                txtDonGia.setText(String.valueOf((long) nl.getDonGiaNhap()));
                txtSoLuong.setText("1");
                txtSoLuong.requestFocus();
                txtSoLuong.selectAll();
                break;
            }
        }
    }

    private void clearForm() {
        currentSelectedNL = null;
        lblSelectedNL.setText("-- Chưa chọn --");
        txtSoLuong.setText("");
        txtDonGia.setText("");
        dateChooser.setDate(null);
        nlTable.clearSelection();
    }

    private void addToCart() {
        if (currentSelectedNL == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nguyên liệu từ bảng bên trên.");
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

        Date selectedDate = dateChooser.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày hết hạn.");
            return;
        }
        LocalDate ngayHH = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Check duplicate
        for (ChiTietPhieuNhap ct : chiTietList) {
            if (ct.getMaNL().equals(currentSelectedNL.getMaNL())) {
                JOptionPane.showMessageDialog(this, "Nguyên liệu đã có trong phiếu. Vui lòng xóa dòng cũ để thêm lại.");
                return;
            }
        }

        tempCounter++;
        ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
        ct.setMaCTPN("TEMP_" + tempCounter);
        ct.setSoLuong(soLuong);
        ct.setDonGia(donGia);
        ct.setThanhTien(soLuong * donGia);
        ct.setMaNL(currentSelectedNL.getMaNL());
        
        chiTietList.add(ct);
        ngayHetHanList.add(ngayHH);

        refreshCartTable();
        clearForm();
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
                // cols[1] = TenNguyenLieu (chỉ để tham khảo, không dùng để lưu)
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

                // Đã hợp lệ, đưa vào danh sách chờ
                validItems.add(new Object[]{nlFound.getMaNL(), soLuong, donGia, ngayHetHan});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi đọc file CSV:\n" + ex.getMessage(),
                "Lỗi đọc file", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 6. Hiển thị cảnh báo lỗi (nếu có) nhưng vẫn tiếp tục với các dòng hợp lệ
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

        // 7. Đẩy dữ liệu vào danh sách chi tiết phiếu nhập (chiTietList)
        int addedCount = 0;
        for (Object[] item : validItems) {
            String maNL = (String) item[0];
            double sl   = (Double) item[1];
            double dg   = (Double) item[2];
            LocalDate ngayHH = (LocalDate) item[3];

            tempCounter++;
            ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
            ct.setMaCTPN("TEMP_" + tempCounter);
            ct.setSoLuong(sl);
            ct.setDonGia(dg);
            ct.setThanhTien(sl * dg);
            ct.setMaNL(maNL);

            chiTietList.add(ct);
            ngayHetHanList.add(ngayHH);
            addedCount++;
        }

        // 8. Cập nhật bảng giỏ hàng
        refreshCartTable();
        clearForm();

        // 9. Thông báo kết quả
        String msg = String.format("\u2705 Nhập CSV thành công!\n- Đã thêm mới: %d dòng", addedCount);
        JOptionPane.showMessageDialog(this, msg, "Kết quả nhập CSV", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        double tongTien = 0;
        for (ChiTietPhieuNhap ct : chiTietList) {
            NguyenLieu nl = controller.getNguyenLieuById(ct.getMaNL());
            String tenNL = nl != null ? nl.getTenNL() : ct.getMaNL();
            cartModel.addRow(new Object[]{
                ct.getMaNL(), tenNL,
                String.format("%.1f", ct.getSoLuong()),
                CurrencyUtils.formatNoUnit(ct.getDonGia()),
                CurrencyUtils.formatNoUnit(ct.getThanhTien()),
                "\u274C Xóa"
            });
            tongTien += ct.getThanhTien();
        }
        lblTotal.setText("Tổng tiền: " + CurrencyUtils.formatNoUnit(tongTien) + " đ");
    }

    /**
     * Parse một dòng CSV, hỗ trợ trường có dấu ngoặc kép (quoted fields).
     * Ví dụ: NL001,"Cà phê hạt",Bao/Bịch,10,200 → ["NL001", "Cà phê hạt", "Bao/Bịch", "10", "200"]
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    // Kiểm tra escaped quote ("")
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++; // bỏ qua ký tự quote tiếp theo
                    } else {
                        inQuotes = false; // kết thúc quoted field
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true; // bắt đầu quoted field
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0); // reset buffer
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString()); // thêm cột cuối cùng
        return fields.toArray(new String[0]);
    }

    private void savePhieuNhap() {
        if (chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!");
            return;
        }
        if (cbNCC.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Nhà cung cấp.");
            return;
        }

        NhaCungCap ncc = listNCC.get(cbNCC.getSelectedIndex());
        List<Kho> listKho = controller.getAllKho();
        if (listKho.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có kho nào trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double tongTien = 0;
        for (ChiTietPhieuNhap ct : chiTietList) tongTien += ct.getThanhTien();

        for (int i = 0; i < chiTietList.size(); i++) {
            chiTietList.get(i).setMaCTPN(controller.generateNextMaCTPN());
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
        pn.setMaKho(listKho.get(0).getMaKho());

        if (controller.processNhapKho(pn, chiTietList)) {
            JOptionPane.showMessageDialog(this, "✅ Lưu Phiếu Nhập thành công!\nMã phiếu: " + pn.getMaPN());
            if (onBackAction != null) onBackAction.run();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Lỗi khi lưu phiếu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
