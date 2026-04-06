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

    private JSpinner spinSoLuong;
    private JTextField txtGhiChu;
    private JLabel lblTotal;

    private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    public ItemOptionDialog(JFrame parent, Mon mon, MenuController menuController) {
        super(parent, "Tu\u1EF3 Ch\u1ECDn: " + mon.getTenMon(), true);
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
            "Ch\u1ECDn Size", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Roboto", Font.BOLD, 13), new Color(100, 100, 100)
        ));

        sizeGroup = new ButtonGroup();
        if (sizeList == null || sizeList.isEmpty()) {
            sizePanel.add(new JLabel("  Kh\u00F4ng c\u00F3 Size"));
        } else {
            boolean first = true;
            for (Size size : sizeList) {
                double gia = menuController.getGiaBan(size.getMaSize());
                String text = size.getTenSize() + " (" + nf.format(gia) + "\u0111)";
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
            "Topping (C\u00F3 th\u1EC3 ch\u1ECDn nhi\u1EC1u)", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Roboto", Font.BOLD, 13), new Color(100, 100, 100)
        ));

        if (toppingList == null || toppingList.isEmpty()) {
            toppPanel.add(new JLabel("  Kh\u00F4ng c\u00F3 Topping"));
        } else {
            for (Topping top : toppingList) {
                String text = top.getTenTopping() + " (+" + nf.format(top.getGiaTopping()) + "\u0111)";
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

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel lblSL = new JLabel("S\u1ED1 l\u01B0\u1EE3ng:");
        lblSL.setFont(new Font("Roboto", Font.BOLD, 13));
        botPanel.add(lblSL, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 0.3;
        spinSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spinSoLuong.setFont(new Font("Roboto", Font.BOLD, 15));
        spinSoLuong.setPreferredSize(new Dimension(80, 32));
        spinSoLuong.addChangeListener(e -> updateTotal());
        botPanel.add(spinSoLuong, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel lblGC = new JLabel("Ghi ch\u00FA:");
        lblGC.setFont(new Font("Roboto", Font.BOLD, 13));
        botPanel.add(lblGC, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
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

        lblTotal = new JLabel("T\u1ED5ng: 0\u0111");
        lblTotal.setFont(new Font("Roboto", Font.BOLD, 18));
        lblTotal.setForeground(new Color(231, 76, 60));
        actionPanel.add(lblTotal, BorderLayout.WEST);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);

        JButton btnCancel = new JButton("H\u1EE6Y");
        btnCancel.setFont(new Font("Roboto", Font.BOLD, 13));
        btnCancel.setBackground(new Color(240, 240, 240));
        btnCancel.setForeground(new Color(100, 100, 100));
        btnCancel.setFocusable(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());

        JButton btnAdd = new JButton("TH\u00CAM V\u00C0O GI\u1ECE");
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
    }

    private void updateTotal() {
        if (sizeBtnMap.isEmpty()) return;

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
                toppingPrice += toppingBtnMap.get(cb).getGiaTopping();
            }
        }

        int sl = (Integer) spinSoLuong.getValue();
        double total = (sizePrice + toppingPrice) * sl;
        lblTotal.setText("T\u1ED5ng: " + nf.format(total) + "\u0111");
    }

    private void commitAndClose() {
        if (sizeBtnMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "M\u00F3n n\u00E0y ch\u01B0a c\u00F3 Size n\u00E0o!", "L\u1ED7i", JOptionPane.WARNING_MESSAGE);
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

        int sl = (Integer) spinSoLuong.getValue();
        String gc = txtGhiChu.getText().trim();

        result = new CartItem(mon, selectedSize, sl, sizePrice, gc);

        for (JCheckBox cb : toppingBtnMap.keySet()) {
            if (cb.isSelected()) {
                result.addTopping(toppingBtnMap.get(cb), 1); // fixed sl topping = 1 per drink
            }
        }

        dispose();
    }

    public CartItem getResult() {
        return result;
    }
}
