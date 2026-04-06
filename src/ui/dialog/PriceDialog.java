package ui.dialog;

import controller.PriceController;
import entity.BangGia;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * PriceDialog: Thi\u1EBFt l\u1EADp th\u00F4ng tin b\u1EA3ng gi\u00E1 v\u00E0 v\u00E0 c\u00E1c t\u00EDnh n\u0103ng nhanh.
 */
public class PriceDialog extends JDialog {

    private final PriceController controller = new PriceController();
    private final BangGia bangGia;
    private final boolean isEditMode;
    private boolean confirmed = false;

    private JTextField txtMa, txtTen, txtBatDau, txtKetThuc;
    private JCheckBox chkStatus;
    private JComboBox<BangGia> cbCloneSource;
    private JTextField txtPercent, txtFixed;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color BG_COLOR = new Color(245, 247, 250);

    public PriceDialog(Frame parent, BangGia bg, boolean isEditMode) {
        super(parent, isEditMode ? "C\u1EADp nh\u1EADt B\u1EA3ng gi\u00E1" : "Th\u00EA m B\u1EA3ng Gi\u00E1 M\u1EDBi", true);
        this.bangGia = bg;
        this.isEditMode = isEditMode;

        initUI();
        fillData();
    }

    private void initUI() {
        setSize(850, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // 1. Header
        add(createHeader(), BorderLayout.NORTH);

        // 2. Body (2 Cards)
        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        body.add(createGeneralInfoCard());
        body.add(createBatchToolsCard());

        add(body, BorderLayout.CENTER);

        // 3. Footer
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(PRIMARY_COLOR);
        h.setPreferredSize(new Dimension(0, 60));
        h.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel title = new JLabel(" TH\u00D4NG TIN B\u1EA2NG GI\u00C1");
        title.setFont(new Font("Roboto", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setIcon(IconFontSwing.buildIcon(FontAwesome.MONEY, 24, Color.WHITE));
        h.add(title, BorderLayout.WEST);

        return h;
    }

    private JPanel createGeneralInfoCard() {
        JPanel card = createCardPanel("TH\u00D4NG TIN CHUNG");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = createGBC();

        txtMa = addInputRow(form, gbc, "M\u00E3 b\u1EA3ng gi\u00E1:", FontAwesome.ID_CARD_O);
        txtMa.setEditable(false);

        txtTen = addInputRow(form, gbc, "T\u00EAn b\u1EA3ng gi\u00E1*:", FontAwesome.PENCIL);
        txtBatDau = addInputRow(form, gbc, "Ng\u00E0y b\u1EAFt \u0111\u1EA7u (yyyy-mm-dd)*:", FontAwesome.CALENDAR);
        txtKetThuc = addInputRow(form, gbc, "Ng\u00E0y k\u1EBFt th\u00FAc (T\u00F9y ch\u1ECDn):", FontAwesome.CALENDAR_CHECK_O);

        chkStatus = new JCheckBox(" \u0110ang k\u00EDch ho\u1EA1t");
        chkStatus.setOpaque(false);
        form.add(chkStatus, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBatchToolsCard() {
        JPanel card = createCardPanel("C\u00D4NG C\u1EE4 NHANH");
        JPanel toolBox = new JPanel();
        toolBox.setLayout(new BoxLayout(toolBox, BoxLayout.Y_AXIS));
        toolBox.setOpaque(false);

        // Cloner
        toolBox.add(new JLabel("1. Sao ch\u00E9p gi\u00E1 t\u1EEB:"));
        cbCloneSource = new JComboBox<>();
        List<BangGia> lists = controller.getAllBangGia();
        for (BangGia b : lists) if (!b.getMaBangGia().equals(bangGia.getMaBangGia())) cbCloneSource.addItem(b);
        toolBox.add(cbCloneSource);
        
        JButton btnClone = new JButton(" Sao ch\u00E9p ngay");
        btnClone.setIcon(IconFontSwing.buildIcon(FontAwesome.FILES_O, 14, Color.GRAY));
        btnClone.addActionListener(e -> handleClone());
        toolBox.add(Box.createVerticalStrut(5));
        toolBox.add(btnClone);

        toolBox.add(Box.createVerticalStrut(25));
        toolBox.add(new JSeparator());
        toolBox.add(Box.createVerticalStrut(15));

        // Batch Adjuster
        toolBox.add(new JLabel("2. \u0110i\u1EC1u ch\u1EC9nh gi\u00E1 h\u00E0ng lo\u1EA1t:"));
        JPanel adjRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        adjRow.setOpaque(false);
        adjRow.add(new JLabel("% T\u0103ng:"));
        txtPercent = new JTextField("0", 5);
        adjRow.add(txtPercent);
        adjRow.add(new JLabel("+ S\u1ED1 ti\u1EC1n:"));
        txtFixed = new JTextField("0", 7);
        adjRow.add(txtFixed);
        toolBox.add(adjRow);

        JButton btnAdjust = new JButton(" \u00C1p d\u1EE5ng \u0111i\u1EC1u ch\u1EC9nh");
        btnAdjust.setIcon(IconFontSwing.buildIcon(FontAwesome.MAGIC, 14, Color.GRAY));
        btnAdjust.addActionListener(e -> handleBatchPriceAdjust());
        toolBox.add(btnAdjust);

        card.add(toolBox, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        f.setOpaque(false);

        JButton btnSave = new JButton(" L\u01AFU D\u1EE0 LI\u1EC6U");
        btnSave.setPreferredSize(new Dimension(140, 40));
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Roboto", Font.BOLD, 12));
        btnSave.addActionListener(e -> handleSave());

        JButton btnClose = new JButton(" \u0110\u00D3NG");
        btnClose.addActionListener(e -> dispose());
        f.add(btnClose);
        f.add(btnSave);
        return f;
    }

    private void fillData() {
        txtMa.setText(bangGia.getMaBangGia());
        txtTen.setText(bangGia.getTenBangGia());
        txtBatDau.setText(bangGia.getNgayBatDau().toString());
        txtKetThuc.setText(bangGia.getNgayKetThuc() != null ? bangGia.getNgayKetThuc().toString() : "");
        chkStatus.setSelected(bangGia.isTrangThai());
    }

    private void handleSave() {
        if (ValidationUtils.isEmpty(txtTen.getText())) {
            JOptionPane.showMessageDialog(this, "T\u00EAn b\u1EA3ng gi\u00E1 kh\u00F4ng \u0111\u01B0\u1EE3c \u0111\u1EC3 tr\u1ED1ng!");
            return;
        }
        try {
            bangGia.setTenBangGia(txtTen.getText().trim());
            bangGia.setNgayBatDau(LocalDate.parse(txtBatDau.getText().trim()));
            if (!txtKetThuc.getText().trim().isEmpty()) {
                bangGia.setNgayKetThuc(LocalDate.parse(txtKetThuc.getText().trim()));
            } else {
                bangGia.setNgayKetThuc(null);
            }
            bangGia.setTrangThai(chkStatus.isSelected());

            if (controller.saveBangGia(bangGia, isEditMode)) {
                confirmed = true;
                dispose();
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ng\u00E0y sai \u0111\u1ECBnh d\u1EA1ng (yyyy-mm-dd)!");
        }
    }

    private void handleClone() {
        if (!isEditMode) {
            JOptionPane.showMessageDialog(this, "Vui l\u00F2ng l\u01B0u b\u1EA3ng gi\u00E1 tr\u01B0\u1EDBc khi sao ch\u00E9p chi ti\u1EBFt gi\u00E1!");
            return;
        }
        BangGia source = (BangGia) cbCloneSource.getSelectedItem();
        if (source != null) {
            int opt = JOptionPane.showConfirmDialog(this, "Sao ch\u00E9p gi\u00E1 t\u1EEB [" + source.getTenBangGia() + "]? Vi\u1EC7c n\u00E0y s\u1EBD ghi \u0111\u00E8 c\u00E1c gi\u00E1 hi\u1EC7n c\u00F3.");
            if (opt == JOptionPane.YES_OPTION) {
                controller.clonePriceList(source.getMaBangGia(), bangGia.getMaBangGia());
                JOptionPane.showMessageDialog(this, "Sao ch\u00E9p th\u00E0nh c\u00F4ng!");
            }
        }
    }

    private void handleBatchPriceAdjust() {
        if (!isEditMode) {
            JOptionPane.showMessageDialog(this, "Vui l\u00F2ng l\u01B0u b\u1EA3ng gi\u00E1 tr\u01B0\u1EDBc khi \u0111i\u1EC1u ch\u1EC9nh!");
            return;
        }
        try {
            double p = Double.parseDouble(txtPercent.getText()) / 100.0;
            double f = Double.parseDouble(txtFixed.getText());
            controller.batchAdjustPrice(bangGia.getMaBangGia(), p, f);
            JOptionPane.showMessageDialog(this, "\u0110\u00E3 \u0111i\u1EC1u ch\u1EC9nh gi\u00E1 to\u00E0n b\u1ED9 th\u1EF1c \u0111\u01A1 n!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "D\u1EEF li\u1EC7u t\u1EC9 l\u1EC7/s\u1ED1 ti\u1EC1n kh\u00F4ng h\u1EE3p l\u1EC7!");
        }
    }

    // --- HELPERS ---
    private JPanel createCardPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(230,230,230), 1), new EmptyBorder(20, 20, 20, 20)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Roboto", Font.BOLD, 14));
        lbl.setForeground(PRIMARY_COLOR);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240,240,240)));
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    private JTextField addInputRow(JPanel p, GridBagConstraints gbc, String label, FontAwesome icon) {
        JLabel l = new JLabel(" " + label);
        l.setIcon(IconFontSwing.buildIcon(icon, 14, Color.GRAY));
        l.setFont(new Font("Roboto", Font.BOLD, 12));
        gbc.insets = new Insets(10,0,5,0);
        p.add(l, gbc); gbc.gridy++;
        gbc.insets = new Insets(0,0,0,0);
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(0, 35));
        p.add(txt, gbc); gbc.gridy++;
        return txt;
    }

    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        return gbc;
    }

    public boolean isConfirmed() { return confirmed; }
}
