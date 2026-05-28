package ui.dialog;

import controller.MenuController;
import dto.CartItem;
import entity.Mon;
import entity.Size;
import entity.Topping;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Dialog chọn Size, Topping, Số Lượng và Ghi Chú cho một món.
 * Trả về CartItem khi nhấn Xong, null khi Hủy.
 */
public class ItemOptionDialog extends JDialog {

    private final Mon mon;
    private final MenuController menuController;
    private CartItem result = null;

    private List<Size> sizeList;
    private List<Topping> toppingList;
    private ButtonGroup sizeGroup;
    private Map<JRadioButton, Size> sizeBtnMap = new HashMap<>();
    private Map<JCheckBox, Topping> toppingBtnMap = new HashMap<>();

    private JTextField txtSoLuong;
    private JTextField txtGhiChu;
    private JLabel lblTotal;

    private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    public ItemOptionDialog(JFrame parent, Mon mon, MenuController menuController) {
        super(parent, "Tuỳ Chọn: " + mon.getTenMon(), true);
        this.mon = mon;
        this.menuController = menuController;

        sizeList = menuController.getSizeOfMon(mon.getMaMon());
        toppingList = menuController.getToppingDangCungCap();

        setSize(480, 560);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
        updateTotal();
    }

    private void initUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(15, 20, 15, 20));

        // ── 1. Header (Tên món) ──
        JLabel lblName = new JLabel(mon.getTenMon());
        lblName.setFont(new Font("Roboto", Font.BOLD, 22));
        lblName.setForeground(new Color(26, 26, 46));
        lblName.setAlignmentX(CENTER_ALIGNMENT);
        main.add(lblName);
        main.add(Box.createVerticalStrut(20));

        // ── 2. Size Panel ──
        JPanel sizePanel = new JPanel(new GridLayout(0, 2, 10, 10));
        sizePanel.setBackground(Color.WHITE);
        sizePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Chọn Size", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Roboto", Font.BOLD, 13), new Color(100, 100, 100)));

        sizeGroup = new ButtonGroup();
        if (sizeList == null || sizeList.isEmpty()) {
            sizePanel.add(new JLabel("  Không có Size"));
        } else {
            boolean first = true;
            for (Size size : sizeList) {
                double gia = menuController.getGiaBan(size.getMaSize());
                String szName = size.getTenSize();
                boolean isNormal = szName.equalsIgnoreCase("Thường") || szName.contains("Thư?");
                String text = (isNormal ? "Mặc định" : szName) + " (" + nf.format(gia) + "đ)";
                JRadioButton rb = new JRadioButton(text);
                rb.setFont(new Font("Roboto", Font.PLAIN, 14));
                rb.setBackground(Color.WHITE);
                rb.setFocusable(false);
                rb.addActionListener(e -> updateTotal());

                sizeGroup.add(rb);
                sizePanel.add(rb);
                sizeBtnMap.put(rb, size);

                if (first) {
                    rb.setSelected(true);
                    first = false;
                }
            }
        }
        main.add(sizePanel);
        main.add(Box.createVerticalStrut(15));

        // ── 3. Topping Panel ──
        JPanel toppPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        toppPanel.setBackground(Color.WHITE);
        toppPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Topping (Có thể chọn nhiều)", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Roboto", Font.BOLD, 13), new Color(100, 100, 100)));

        if (toppingList == null || toppingList.isEmpty()) {
            toppPanel.add(new JLabel("  Không có Topping"));
        } else {
            for (Topping top : toppingList) {
                double topPrice = menuController.getGiaTopping(top.getMaTopping());
                String text = top.getTenTopping() + " (+" + nf.format(topPrice) + "đ)";
                JCheckBox cb = new JCheckBox(text);
                cb.setFont(new Font("Roboto", Font.PLAIN, 13));
                cb.setBackground(Color.WHITE);
                cb.setFocusable(false);
                cb.addActionListener(e -> updateTotal());

                toppPanel.add(cb);
                toppingBtnMap.put(cb, top);
            }
        }
        main.add(toppPanel);
        main.add(Box.createVerticalStrut(15));

        // ── 4. Số lượng + Ghi chú Panel ──
        JPanel botPanel = new JPanel(new GridBagLayout());
        botPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel lblSL = new JLabel("Số lượng:");
        lblSL.setFont(new Font("Roboto", Font.BOLD, 13));
        botPanel.add(lblSL, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        txtSoLuong = new JTextField("1");
        txtSoLuong.setFont(new Font("Roboto", Font.BOLD, 15));
        txtSoLuong.setPreferredSize(new Dimension(80, 32));
        txtSoLuong.setHorizontalAlignment(JTextField.LEFT); // Đổi về căn trái cho đồng bộ với ô Ghi chú
        
        txtSoLuong.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotal(); }
        });

        txtSoLuong.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                validateSoLuong(true);
            }
        });
        
        botPanel.add(txtSoLuong, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel lblGC = new JLabel("Ghi chú:");
        lblGC.setFont(new Font("Roboto", Font.BOLD, 13));
        botPanel.add(lblGC, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Roboto", Font.PLAIN, 14));
        txtGhiChu.setPreferredSize(new Dimension(0, 32));
        botPanel.add(txtGhiChu, gbc);

        main.add(botPanel);
        main.add(Box.createVerticalStrut(15));
        main.add(Box.createVerticalGlue());

        // ── 5. Total & Buttons ──
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(Color.WHITE);

        lblTotal = new JLabel("Tổng: 0đ");
        lblTotal.setFont(new Font("Roboto", Font.BOLD, 18));
        lblTotal.setForeground(new Color(231, 76, 60));
        actionPanel.add(lblTotal, BorderLayout.WEST);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);

        JButton btnCancel = new JButton("HỦY");
        btnCancel.setFont(new Font("Roboto", Font.BOLD, 13));
        btnCancel.setBackground(new Color(240, 240, 240));
        btnCancel.setForeground(new Color(100, 100, 100));
        btnCancel.setFocusable(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());

        JButton btnAdd = new JButton("THÊM");
        btnAdd.setFont(new Font("Roboto", Font.BOLD, 13));
        btnAdd.setBackground(new Color(39, 174, 96));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusable(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> commitAndClose());

        pnlBtns.add(btnCancel);
        pnlBtns.add(btnAdd);

        actionPanel.add(pnlBtns, BorderLayout.EAST);
        main.add(actionPanel);

        setContentPane(main);
        getRootPane().setDefaultButton(btnAdd);
    }

    private boolean validateSoLuong(boolean showDialog) {
        String text = txtSoLuong.getText().trim();
        if (text.isEmpty()) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "Số lượng không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
                txtSoLuong.setText("1");
            }
            return false;
        }
        try {
            int sl = Integer.parseInt(text);
            if (sl <= 0) {
                if (showDialog) {
                    JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
                    txtSoLuong.setText("1");
                }
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ (không được nhập chữ).", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
                txtSoLuong.setText("1");
            }
            return false;
        }
    }

    private void updateTotal() {
        if (sizeBtnMap.isEmpty())
            return;

        double sizePrice = 0;
        for (JRadioButton rb : sizeBtnMap.keySet()) {
            if (rb.isSelected()) {
                sizePrice = menuController.getGiaBan(sizeBtnMap.get(rb).getMaSize());
                break;
            }
        }

        double toppingPrice = 0;
        for (JCheckBox cb : toppingBtnMap.keySet()) {
            if (cb.isSelected()) {
                toppingPrice += menuController.getGiaTopping(toppingBtnMap.get(cb).getMaTopping());
            }
        }

        int sl = 1;
        try {
            sl = Integer.parseInt(txtSoLuong.getText().trim());
            if (sl < 0) sl = 0;
        } catch (NumberFormatException e) {
            sl = 0;
        }
        double total = (sizePrice + toppingPrice) * sl;
        lblTotal.setText("Tổng: " + nf.format(total) + "đ");
    }

    private void commitAndClose() {
        if (!validateSoLuong(true)) {
            txtSoLuong.requestFocus();
            return; // Dừng lại không thêm món
        }

        if (sizeBtnMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Món này chưa có Size nào!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Size selectedSize = null;
        double sizePrice = 0;
        for (JRadioButton rb : sizeBtnMap.keySet()) {
            if (rb.isSelected()) {
                selectedSize = sizeBtnMap.get(rb);
                sizePrice = menuController.getGiaBan(selectedSize.getMaSize());
                break;
            }
        }

        int sl = Integer.parseInt(txtSoLuong.getText().trim());
        String gc = txtGhiChu.getText().trim();

        result = new CartItem(mon, selectedSize, sl, sizePrice, gc);

        for (JCheckBox cb : toppingBtnMap.keySet()) {
            if (cb.isSelected()) {
                Topping top = toppingBtnMap.get(cb);
                double giaTop = menuController.getGiaTopping(top.getMaTopping());
                result.addTopping(top, 1, giaTop); // fixed sl topping = 1 per drink
            }
        }

        dispose();
    }

    public CartItem getResult() {
        return result;
    }
}
