package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	
	private static Connection connection;
	private static String url = "jdbc:mysql://localhost:3306";
	private static String username = "root";
	private static String password = "";
	
	private DBConnection() {}
	
	public static Connection getConnection() {
		if( connection == null)
			try {
				connection = DriverManager.getConnection(url, username, password);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return connection;
	}

}
