package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBUtil {

	public static List<String> getDatabasesNames() {
		List<String> names = new ArrayList<>();
		try (ResultSet resultSet = DBConnection.getConnection().prepareStatement("SHOW DATABASES").executeQuery()) {
			while (resultSet.next())
				names.add(resultSet.getString("Database"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return names;
	}

	public static List<String> getRelationshipsBetweenTables(String dbName) {
		List<String> relationships = new ArrayList<>();
		try {
			Connection connection = DBConnection.getConnection();
			PreparedStatement ps = connection.prepareStatement(
					"SELECT `TABLE_NAME`, `COLUMN_NAME`, `REFERENCED_TABLE_NAME`,`REFERENCED_COLUMN_NAME` "
							+ "FROM `INFORMATION_SCHEMA`.`KEY_COLUMN_USAGE` "
							+ "WHERE `TABLE_SCHEMA` = ? AND `REFERENCED_TABLE_NAME` IS NOT NULL");
			ps.setString(1, dbName);
			ResultSet rs = ps.executeQuery();
			String message = "`%s` de `%s` fait référence à `%s` de `%s`";
			while (rs.next())
				relationships.add(String.format(message, rs.getString("COLUMN_NAME"), rs.getString("TABLE_NAME"),
						rs.getString("REFERENCED_COLUMN_NAME"), rs.getString("REFERENCED_TABLE_NAME")));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return relationships;
	}

}
