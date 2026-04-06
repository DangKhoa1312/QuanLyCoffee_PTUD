package exception;

/**
 * Custom exception cho lỗi nghiệp vụ của ứng dụng.
 * Ném khi vi phạm business rule (không phải lỗi kỹ thuật).
 *
 * Ví dụ:
 *   throw new AppException("Bàn đang có khách, không thể đặt trước!");
 *   throw new AppException("Ca chưa được mở, không thể tạo đơn hàng!");
 */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
