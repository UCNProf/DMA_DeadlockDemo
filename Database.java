import java.sql.*;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

public abstract class Database {
	
	//private final String URL = "jdbc:sqlserver://no172047\\sqlexpress; databaseName=TheCoffeeBreak; integratedSecurity=true;";

	public static Connection getConnection(int isolationLevel) {
		
		Connection conn = null;
		
		try {
			SQLServerDataSource ds = new SQLServerDataSource();
			ds.setIntegratedSecurity(true);
			ds.setServerName("no172047\\sqlexpress");
			ds.setDatabaseName("TheCoffeeBreak");
			conn = ds.getConnection();

			conn.setTransactionIsolation(isolationLevel);
			
		} catch (SQLException e) {

			e.printStackTrace();
		}	
			
			
		return conn;
	}
	
	public static void printSessionInfo(Connection conn) {
		
		PreparedStatement selectSessionInfo = null;
		
		try {
			selectSessionInfo = conn.prepareStatement("select @@SPID");
			ResultSet sessionInfoRows = selectSessionInfo.executeQuery();
			sessionInfoRows.next();
			System.out.println(Thread.currentThread().getName()+ " - Session: "+ sessionInfoRows.getInt(1) + ", IsolationLevel: "+ conn.getTransactionIsolation());	
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
