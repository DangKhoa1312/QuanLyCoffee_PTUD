package ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import entity.Topping;

/**
 * Dialog Thêm / Sửa Topping.
 */
public class ToppingDialog extends JDialog {

    private JTextField txtMaTopping;
    private JTextField txtTenTopping;
    private JCheckBox  chkTrangThai;

    private boolean confirmed = false;
    private final Topping topping;
    private final boolean isEdit;

    private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    public ToppingDialog(Frame parent, Topping topping, boolean isEdit) {
        super(parent, isEdit ? "Cập Nhật Topping" : "Thêm Topping Mới", true);
        this.topping = topping;
        this.isEdit = isEdit;
        setSize(450, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(20, 25, 20, 25));

        // ── Title ──
        JLabel lblTitle = new JLabel(isEdit ? "✏️ Cập Nhật Topping" : "➕ Thêm Topping Mới");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 20));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        main.add(lblTitle, BorderLayout.NORTH);

        // ── Form ──
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Mã Topping
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(createLabel("Mã Topping:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtMaTopping = createTextField();
        txtMaTopping.setEditable(false);
        txtMaTopping.setBackground(new Color(245, 245, 245));
        form.add(txtMaTopping, gbc);

        // Row 1: Tên Topping
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(createLabel("Tên Topping:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTenTopping = createTextField();
        form.add(txtTenTopping, gbc);

        // Row 2: Trạng thái
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(createLabel("Trạng thái:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        chkTrangThai = new JCheckBox("Đang cung cấp");
        chkTrangThai.setFont(new Font("Roboto", Font.PLAIN, 14));
        chkTrangThai.setOpaque(false);
        chkTrangThai.setSelected(true);
        form.add(chkTrangThai, gbc);



        main.add(form, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        pnlButtons.setOpaque(false);

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Roboto", Font.BOLD, 14));
        btnCancel.setBackground(new Color(230, 230, 230));
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.setFocusable(false);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(isEdit ? "Cập nhật" : "Thêm mới");
        btnSave.setFont(new Font("Roboto", Font.BOLD, 14));
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(120, 40));
        btnSave.setFocusable(false);
        btnSave.addActionListener(e -> handleSave());

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSave);
        main.add(pnlButtons, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Roboto", Font.BOLD, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Roboto", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(250, 38));
        return tf;
    }

    private void loadData() {
        txtMaTopping.setText(topping.getMaTopping() != null ? topping.getMaTopping() : "");
        txtTenTopping.setText(topping.getTenTopping() != null ? topping.getTenTopping() : "");
        if (isEdit) {
            chkTrangThai.setSelected(topping.isTrangThai());
        } else {
            chkTrangThai.setSelected(true);
        }
    }

    private void handleSave() {
        // Validate tên
        String ten = txtTenTopping.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên Topping!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtTenTopping.requestFocus();
            return;
        }

        // Gán giá trị vào entity
        topping.setTenTopping(ten);
        topping.setGiaTopping(0.0); // Mặc định là 0 vì quản lý qua Bảng Giá
        topping.setTrangThai(chkTrangThai.isSelected());

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Topping getTopping() {
        return topping;
    }
}
