package ui.panel;

import controller.InvoiceController;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình Quản Lý Hoá Đơn (Lịch sử giao dịch).
 */
public class InvoicePanel extends JPanel {

    private final InvoiceController invoiceController;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<HoaDon> currentList;

    private final NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public InvoicePanel() {
        this.invoiceController = new InvoiceController();
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        
        initUI();
        loadData();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 30, 20, 30));

        JLabel lblTitle = new JLabel("\uD83D\uDCDC  L\u1ECBch S\u1EED H\u00F3a \u0110\u01A1n");
        lblTitle.setFont(new Font("Roboto", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("\u21BB L\u00E0m M\u1EDBi");
        btnRefresh.setFont(new Font("Roboto", Font.BOLD, 13));
        btnRefresh.setBackground(new Color(220, 220, 220));
        btnRefresh.setFocusable(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadData());
        header.add(btnRefresh, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(0, 30, 30, 30));

        String[] cols = {
            "M\u00E3 H\u00F3a \u0110\u01A1n", 
            "B\u00E0n", 
            "Lo\u1EA1i \u0111\u01A1n",
            "T\u1ED5ng Ti\u1EC1n", 
            "T.Gian Th.To\u00E1n",
            "H\u00ECnh Th\u1EE9c",
            "Tr\u1EA1ng th\u00E1i",
            "Thu Ng\u00E2n"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Roboto", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));

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

        add(content, BorderLayout.CENTER);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        currentList = invoiceController.getAllHoaDon();
        for (HoaDon hd : currentList) {
            String time = hd.getThoiGianThanhToan() != null 
                          ? hd.getThoiGianThanhToan().format(dtf) 
                          : hd.getThoiGianXuat().format(dtf);
            
            tableModel.addRow(new Object[]{
                hd.getMaHD(),
                hd.getMaBan() != null ? hd.getMaBan() : "Mang v\u1EC1",
                hd.getLoaiDon() != null ? hd.getLoaiDon().name() : "",
                nf.format(hd.getTongTienPhaiTra()) + " \u0111",
                time,
                hd.getHinhThucThanhToan() == null ? "" : hd.getHinhThucThanhToan().name(),
                hd.getTrangThai().name(),
                hd.getMaNV()
            });
        }
    }
}
