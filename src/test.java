import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class test {
    String critical_count;
    String high_count;
    String medium_count;
    String low_count;

    public void getCriticalPriorityCountFromDB(){
        
        String sql = "SELECT COUNT(severity) AS critical_count FROM projects WHERE severity='Critical';";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                critical_count = rs.getString("critical_count");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getHighPriorityCountFromDB(){
        
        String sql = "SELECT COUNT(severity) AS high_count FROM projects WHERE severity='High';";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                high_count = rs.getString("high_count");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMediumPriorityCountFromDB(){
        
        String sql = "SELECT COUNT(severity) AS medium_count FROM projects WHERE severity='Medium';";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medium_count = rs.getString("medium_count");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLowPriorityCountFromDB(){
        
        String sql = "SELECT COUNT(severity) AS low_count FROM projects WHERE severity='Low';";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                low_count = rs.getString("low_count");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        test t = new test();
        t.getHighPriorityCountFromDB();
        t.getCriticalPriorityCountFromDB();
        t.getMediumPriorityCountFromDB();
        t.getLowPriorityCountFromDB();
        System.out.println("Critical: "+t.critical_count);
        System.out.println("High: "+t.high_count);
        System.out.println("Medium: "+t.medium_count);
        System.out.println("Low: "+t.low_count);
    }
}
