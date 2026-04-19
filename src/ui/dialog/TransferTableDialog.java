package ui.dialog;

import controller.TableController;
import entity.Ban;
import entity.DonHang;

import javax.swing.*;
import java.awt.*;

/**
 * Thực hiện Chuyển Bàn hoặc Gộp Bàn qua TablePickerDialog sơ đồ.
 * Không hiển thị UI của chính mình — chỉ dùng làm wrapper logic + kết quả.
 */
public class TransferTableDialog extends JDialog {

    private final DonHang donHangHienTai;
    private final Ban banNguon;
    private final TableController tableController;
    private final int mode; // 1: Chuyển Bàn, 2: Ghép Bàn
    private final JFrame parentFrame;
    private boolean success = false;

    public TransferTableDialog(JFrame parent, Ban banNguon, DonHang donHang, int mode) {
        super(parent, mode == 1 ? "Chuyển Bàn" : "Gộp Bàn", true);
        this.parentFrame = parent;
        this.banNguon = banNguon;
        this.donHangHienTai = donHang;
        this.mode = mode;
        this.tableController = new TableController();

        // Không build UI — dialog này chỉ lưu trạng thái
        setUndecorated(true);
        setSize(1, 1);
        // Đặt ở giữa màn hình nhưng invisible (1x1 px, không thấy được)
        setLocationRelativeTo(parent);
    }

    /**
     * Override setVisible để chạy luồng chọn bàn ngay khi được gọi.
     * Điều này tránh dialog hiện ra briefly ở vị trí (0,0).
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            // Dùng invokeLater để đảm bảo dialog đã được packed trước khi mở picker
            SwingUtilities.invokeLater(this::openTablePicker);
            super.setVisible(true); // Block (modal) cho đến khi dispose() được gọi
        } else {
            super.setVisible(false);
        }
    }

    private void openTablePicker() {
        String title = mode == 1 ? "Chọn bàn trống để chuyển tới" : "Chọn bàn có khách để gộp vào";
        int pickerMode = mode == 1 ? TablePickerDialog.MODE_TRONG : TablePickerDialog.MODE_CO_KHACH;

        TablePickerDialog picker = new TablePickerDialog(parentFrame, title, pickerMode, banNguon.getMaBan());
        picker.setVisible(true);

        Ban banDich = picker.getSelectedBan();
        if (banDich == null) {
            // Người dùng hủy → đóng dialog này luôn
            dispose();
            return;
        }

        // Xác nhận thực hiện
        String confirmMsg = mode == 1
            ? "Xác nhận chuyển từ bàn \"" + banNguon.getSoBan() + "\" sang bàn \"" + banDich.getSoBan() + "\"?"
            : "Xác nhận gộp bàn \"" + banNguon.getSoBan() + "\" vào bàn \"" + banDich.getSoBan() + "\"?";

        int xn = JOptionPane.showConfirmDialog(parentFrame, confirmMsg,
            mode == 1 ? "Xác nhận Chuyển Bàn" : "Xác nhận Gộp Bàn",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (xn != JOptionPane.YES_OPTION) {
            dispose();
            return;
        }

        try {
            if (mode == 1) {
                tableController.chuyenBan(donHangHienTai.getMaDonHang(), banNguon.getMaBan(), banDich.getMaBan());
                JOptionPane.showMessageDialog(parentFrame, "Đã CHUYỂN BÀN thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                DonHang dhDich = tableController.getDonHangDangMo(banDich.getMaBan());
                if (dhDich == null) {
                    JOptionPane.showMessageDialog(parentFrame, "Bàn đích không có đơn đang mở!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    dispose();
                    return;
                }
                tableController.gopBan(donHangHienTai.getMaDonHang(), dhDich.getMaDonHang(), banNguon.getMaBan(), banDich.getMaBan());
                JOptionPane.showMessageDialog(parentFrame, "Đã GỘP BÀN thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
            success = true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentFrame, "Lỗi thao tác: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        dispose(); // Đóng dialog này sau khi xử lý xong
    }

    public boolean isSuccess() {
        return success;
    }
}
