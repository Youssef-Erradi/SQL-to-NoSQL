package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	
	private static Connection connection;
	private static final String URL = "jdbc:mysql://localhost:3306";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "";
	
	private DBConnection() {}
	
	public static Connection getConnection() {
		if( connection == null)
			try {
				connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return connection;
	}

}
