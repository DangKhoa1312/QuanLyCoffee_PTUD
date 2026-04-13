package ui.panel.admin;

import dao.CaLamViecDAO;
import dao.NhanVienDAO;
import dao.impl.CaLamViecDAOImpl;
import dao.impl.NhanVienDAOImpl;
import entity.CaLamViec;
import entity.NhanVien;
import ui.dialog.ShiftRegistrationDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Giao diện Lịch sử ca làm việc dành cho Quản lý.
 */
public class ShiftManagementPanel extends JPanel {

    private final CaLamViecDAO caDAO;
    private final NhanVienDAO nvDAO;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbFilter;
    
    // Lưu trữ danh sách ca làm việc khớp với các hàng trong bảng
    private java.util.List<CaLamViec> currentTableData = new java.util.ArrayList<>();

    public ShiftManagementPanel() {
        this.caDAO = new CaLamViecDAOImpl();
        this.nvDAO = new NhanVienDAOImpl();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initUI();
        loadData();
    }

    private void initUI() {
        // --- PHẦN NORTH: Tiêu đề và Nút Đăng ký ---
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);
        pnlNorth.setPreferredSize(new Dimension(0, 100));

        JLabel lblTitle = new JLabel("Lịch sử ca làm");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 50));
        lblTitle.setForeground(new Color(26, 26, 46));
        pnlNorth.add(lblTitle, BorderLayout.WEST);

        JButton btnRegister = new JButton("Đăng ký ca");
        btnRegister.setFont(new Font("Roboto", Font.BOLD, 16));
        btnRegister.setBackground(new Color(59, 130, 246));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusable(false);
        btnRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> openRegistrationDialog());
        
        JPanel pnlBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 20));
        pnlBtnWrap.setOpaque(false);
        pnlBtnWrap.add(btnRegister);
        pnlNorth.add(pnlBtnWrap, BorderLayout.EAST);

        add(pnlNorth, BorderLayout.NORTH);

        // --- PHẦN CENTER: Bộ lọc và Bảng ---
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 20));
        pnlCenter.setOpaque(false);

        // 1. Toolbar (Tìm kiếm & Lọc)
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlToolbar.setOpaque(false);

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setFont(new Font("Roboto", Font.BOLD, 14));
        pnlToolbar.add(btnSearch);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(200, 35));
        pnlToolbar.add(txtSearch);

        pnlToolbar.add(Box.createHorizontalStrut(20));

        JLabel lblFilter = new JLabel("Lọc:");
        lblFilter.setFont(new Font("Roboto", Font.BOLD, 14));
        pnlToolbar.add(lblFilter);

        cbFilter = new JComboBox<>(new String[]{"Tất cả", "Ca sáng", "Ca chiều"});
        cbFilter.setFont(new Font("Roboto", Font.PLAIN, 14));
        cbFilter.setPreferredSize(new Dimension(150, 35));
        cbFilter.addActionListener(e -> filterData());
        pnlToolbar.add(cbFilter);

        pnlCenter.add(pnlToolbar, BorderLayout.NORTH);

        // 2. Bảng hiển thị
        String[] columns = {"Mã ca", "Mã nhân viên", "Họ và tên", "Số điện thoại", "Vai trò", "Ngày làm", "Doanh thu", "Ca làm"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        
        // Sự kiện Double-click
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetail();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        pnlCenter.add(scroll, BorderLayout.CENTER);

        add(pnlCenter, BorderLayout.CENTER);
        
        btnSearch.addActionListener(e -> loadData());
    }

    private void openRegistrationDialog() {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win instanceof JFrame) {
            ShiftRegistrationDialog dlg = new ShiftRegistrationDialog((JFrame) win);
            dlg.setVisible(true);
        }
    }

    private void showDetail() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentTableData.size()) return;

        CaLamViec ca = currentTableData.get(row);
        NhanVien nv = nvDAO.findById(ca.getMaNV());
        
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win instanceof JFrame) {
            ui.dialog.ShiftDetailDialog dlg = new ui.dialog.ShiftDetailDialog((JFrame) win, ca, nv);
            dlg.setVisible(true);
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        currentTableData.clear();
        
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        List<CaLamViec> dsCa = caDAO.findAll();
        String keyword = txtSearch.getText().trim().toLowerCase();

        for (CaLamViec ca : dsCa) {
            NhanVien nv = nvDAO.findById(ca.getMaNV());
            if (nv == null) continue;

            // Bộ lọc từ khóa (Tên, SĐT, ID NV, ID Ca)
            boolean matchKeyword = keyword.isEmpty() 
                || nv.getTenNV().toLowerCase().contains(keyword)
                || nv.getSoDienThoai().contains(keyword)
                || nv.getMaNV().toLowerCase().contains(keyword)
                || ca.getMaCa().toLowerCase().contains(keyword);

            if (matchKeyword) {
                String caLam = xacDinhCaLam(ca.getGioBatDau());
                
                // Áp dụng bộ lọc ComboBox
                String filterValue = (String) cbFilter.getSelectedItem();
                if ("Tất cả".equals(filterValue) || caLam.equals(filterValue)) {
                    tableModel.addRow(new Object[]{
                        ca.getMaCa(),
                        nv.getMaNV(),
                        nv.getTenNV(),
                        nv.getSoDienThoai(),
                        nv.getVaiTro().name(),
                        ca.getNgayLam().format(dateFormatter),
                        nf.format(ca.getTongDoanhThu()) + "đ",
                        caLam
                    });
                    currentTableData.add(ca);
                }
            }
        }
    }

    private void filterData() {
        loadData();
    }

    private String xacDinhCaLam(LocalTime start) {
        // Sáng: 6:30 - 14:30
        // Chiều: 14:31 - 22:30
        LocalTime morningLimit = LocalTime.of(14, 30);
        
        if (!start.isAfter(morningLimit)) {
            return "Ca sáng";
        } else {
            return "Ca chiều";
        }
    }
}
