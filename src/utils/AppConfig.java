package utils;

import dao.CauHinhDAO;
import dao.impl.CauHinhDAOImpl;
import entity.CauHinh;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton cache cho Bảng Cấu Hình Hệ Thống.
 * Giúp đọc cấu hình cực nhanh từ RAM thay vì query DB liên tục.
 */
public class AppConfig {

    private static AppConfig instance;
    private final Map<String, CauHinh> configMap;
    private final CauHinhDAO cauHinhDAO;

    private AppConfig() {
        configMap = new HashMap<>();
        cauHinhDAO = new CauHinhDAOImpl();
        reload();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /** Load lại toàn bộ cấu hình từ Database vào RAM */
    public void reload() {
        configMap.clear();
        List<CauHinh> configs = cauHinhDAO.findAll();
        for (CauHinh ch : configs) {
            configMap.put(ch.getMaCauHinh(), ch);
        }
    }

    public CauHinh get(String key) {
        return configMap.get(key);
    }

    public String getString(String key, String defaultValue) {
        CauHinh ch = configMap.get(key);
        if (ch != null && ch.getGiaTri() != null) {
            return ch.getGiaTri();
        }
        return defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        CauHinh ch = configMap.get(key);
        if (ch != null && ch.getGiaTri() != null) {
            try {
                return Integer.parseInt(ch.getGiaTri().trim());
            } catch (NumberFormatException e) {
                System.err.println("AppConfig parse int error for key " + key + ": " + e.getMessage());
            }
        }
        return defaultValue;
    }

    public double getDouble(String key, double defaultValue) {
        CauHinh ch = configMap.get(key);
        if (ch != null && ch.getGiaTri() != null) {
            try {
                return Double.parseDouble(ch.getGiaTri().trim());
            } catch (NumberFormatException e) {
                System.err.println("AppConfig parse double error for key " + key + ": " + e.getMessage());
            }
        }
        return defaultValue;
    }

    /** Cập nhật giá trị một cấu hình xuống DB và reload lại Cache */
    public boolean updateConfig(String key, String newValue) {
        CauHinh ch = configMap.get(key);
        if (ch != null) {
            ch.setGiaTri(newValue);
            if (cauHinhDAO.update(ch)) {
                configMap.put(key, ch); // Update cache
                return true;
            }
        }
        return false;
    }
}
