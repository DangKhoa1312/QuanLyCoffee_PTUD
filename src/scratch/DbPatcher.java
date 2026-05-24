package scratch;

import connectDB.DatabaseConnection;
import java.sql.*;

public class DbPatcher {
    public static void main(String[] args) {
        try {
            Connection con = DatabaseConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            
            // 1. Drop CHK_Ca_Gio
            try {
                stmt.execute("ALTER TABLE CaLamViec DROP CONSTRAINT CHK_Ca_Gio");
                System.out.println("Dropped CHK_Ca_Gio");
            } catch (Exception e) { System.out.println("CHK_Ca_Gio error: " + e.getMessage()); }
            
            // 2. Drop CHK_Ca_TrangThai
            try {
                stmt.execute("ALTER TABLE CaLamViec DROP CONSTRAINT CHK_Ca_TrangThai");
                System.out.println("Dropped CHK_Ca_TrangThai");
            } catch (Exception e) { System.out.println("CHK_Ca_TrangThai error: " + e.getMessage()); }
            
            // 3. Drop default constraint for trangThai
            try {
                ResultSet rs = stmt.executeQuery("SELECT name FROM sys.default_constraints WHERE parent_object_id = OBJECT_ID('CaLamViec') AND parent_column_id = (SELECT column_id FROM sys.columns WHERE object_id = OBJECT_ID('CaLamViec') AND name = 'trangThai')");
                if (rs.next()) {
                    String defName = rs.getString(1);
                    stmt.execute("ALTER TABLE CaLamViec DROP CONSTRAINT " + defName);
                    System.out.println("Dropped default constraint: " + defName);
                }
            } catch (Exception e) { System.out.println("Default constraint error: " + e.getMessage()); }
            
            // 4. Update data
            stmt.execute("UPDATE CaLamViec SET trangThai='DANG_MO' WHERE trangThai='DANG_LAM'");
            System.out.println("Updated data to DANG_MO");
            
            // 5. Re-add constraints
            stmt.execute("ALTER TABLE CaLamViec ADD CONSTRAINT CHK_Ca_TrangThai CHECK (trangThai IN ('DANG_MO','DA_DONG'))");
            stmt.execute("ALTER TABLE CaLamViec ADD DEFAULT 'DANG_MO' FOR trangThai");
            System.out.println("Re-added constraints");
            
            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
