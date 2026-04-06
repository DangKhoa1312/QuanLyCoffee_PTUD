package config;

/**
 * Cau hinh ket noi database SQL Server
 * Chinh sua thong tin ket noi tai day truoc khi chay
 */
public class DatabaseConfig {
    public static final String SERVER   = "localhost";
    public static final String PORT     = "1433";
    public static final String DATABASE = "QuanLyQuanCafe";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "sapassword";

    public static final String URL =
        "jdbc:sqlserver://" + SERVER + ":" + PORT
        + ";databaseName=" + DATABASE
        + ";encrypt=false"
        + ";trustServerCertificate=true";
}
