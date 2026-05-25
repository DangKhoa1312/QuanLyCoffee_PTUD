package ui.panel;

import dao.CauHinhDAO;
import dao.impl.CauHinhDAOImpl;
import entity.CauHinh;
import utils.AppConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemConfigPanel extends JPanel {

    private final CauHinhDAO cauHinhDAO;
    private final Map<String, JTextField> inputMap;

    public SystemConfigPanel() {
        this.cauHinhDAO = new CauHinhDAOImpl();
        this.inputMap = new HashMap<>();

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        
        initUI();
        loadData();
    }

    private void initUI() {
        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 30, 10, 30));

        JLabel lblTitle = new JLabel("⚙ Cấu Hình Hệ Thống");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnSave = new JButton("Lưu Cấu Hình");
        btnSave.setFont(new Font("Roboto", Font.BOLD, 14));
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveConfig());
        header.add(btnSave, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- Center Content ---
        JPanel centerPane = new JPanel(new BorderLayout());
        centerPane.setOpaque(false);
        centerPane.setBorder(new EmptyBorder(0, 30, 25, 30));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Nhóm Cửa Hàng
        formPanel.add(createSectionTitle("Thông Tin Cửa Hàng"));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createInputRow("Tên quán", "TEN_QUAN", "Tên hiển thị trên hóa đơn"));
        formPanel.add(createInputRow("Địa chỉ", "DIA_CHI", "Địa chỉ in trên hóa đơn"));
        
        formPanel.add(Box.createVerticalStrut(20));

        // Nhóm Vận Hành
        formPanel.add(createSectionTitle("Vận Hành & Đặt Bàn"));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createInputRow("Thời gian giữ bàn (phút)", "THOI_GIAN_HUY_BAN", "Tự động hủy nếu khách đến trễ"));
        formPanel.add(createInputRow("Khoảng cách đặt bàn (phút)", "KHOANG_CACH_DAT_BAN", "Thời gian an toàn giữa 2 lượt khách"));
        formPanel.add(createInputRow("Chu kỳ thống kê mặc định (ngày)", "THOI_GIAN_THONG_KE", "Số ngày hiển thị trên biểu đồ"));
        
        formPanel.add(Box.createVerticalStrut(20));

        // Nhóm Khách Hàng
        formPanel.add(createSectionTitle("Khách Hàng & Khuyến Mãi"));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createInputRow("Tỷ lệ tích điểm (VNĐ / Điểm)", "TY_LE_TICH_DIEM", "Ví dụ: 10000đ = 1 điểm"));
        formPanel.add(createInputRow("Giá trị quy đổi (VNĐ / Điểm)", "GIA_TRI_DIEM", "Ví dụ: 1 điểm = 1000đ khi thanh toán"));

        JScrollPane scroll = new JScrollPane(formPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        centerPane.add(scroll, BorderLayout.CENTER);

        add(centerPane, BorderLayout.CENTER);
    }

    private JLabel createSectionTitle(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Roboto", Font.BOLD, 16));
        lbl.setForeground(new Color(41, 128, 185));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel createInputRow(String label, String key, String desc) {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(800, 60));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftPanel = new JPanel(new GridLayout(2, 1));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(250, 40));
        
        JLabel lblName = new JLabel(label);
        lblName.setFont(new Font("Roboto", Font.BOLD, 13));
        lblName.setForeground(new Color(44, 62, 80));
        
        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Roboto", Font.ITALIC, 11));
        lblDesc.setForeground(new Color(127, 140, 141));
        
        leftPanel.add(lblName);
        leftPanel.add(lblDesc);
        p.add(leftPanel, BorderLayout.WEST);

        JTextField txtInput = new JTextField();
        txtInput.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtInput.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        inputMap.put(key, txtInput);
        
        p.add(txtInput, BorderLayout.CENTER);
        
        // Spacer bottom
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(p, BorderLayout.CENTER);
        wrapper.setBorder(new EmptyBorder(0, 0, 15, 0));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrapper;
    }

    private void loadData() {
        List<CauHinh> list = cauHinhDAO.findAll();
        for (CauHinh ch : list) {
            JTextField txt = inputMap.get(ch.getMaCauHinh());
            if (txt != null) {
                txt.setText(ch.getGiaTri());
            }
        }
    }

    private void saveConfig() {
        boolean hasError = false;
        
        for (Map.Entry<String, JTextField> entry : inputMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getText().trim();
            
            if (value.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các trường!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            CauHinh ch = cauHinhDAO.findById(key);
            if (ch != null) {
                // Validate Number
                if ("NUMBER".equals(ch.getKieuDuLieu())) {
                    try {
                        Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Giá trị của '" + ch.getTenCauHinh() + "' phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                ch.setGiaTri(value);
                if (!cauHinhDAO.update(ch)) {
                    hasError = true;
                }
            }
        }
        
        if (hasError) {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu một số cấu hình!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            // Update RAM Cache
            AppConfig.getInstance().reload();
            
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof ui.MainFrame) {
                ((ui.MainFrame) window).refreshUI();
            }
            
            JOptionPane.showMessageDialog(this, "Lưu cấu hình thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
