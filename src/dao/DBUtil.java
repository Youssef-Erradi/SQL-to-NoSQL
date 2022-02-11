package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
							+ "WHERE `TABLE_SCHEMA` = ? AND `REFERENCED_TABLE_NAME` IS NOT NULL "
							+ "ORDER BY `TABLE_NAME`");
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

	private static Set<String> getRelatedTablesNames(String dbName) {
		Set<String> names = new HashSet<>();
		try {
			Connection connection = DBConnection.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT `TABLE_NAME`, `REFERENCED_TABLE_NAME`"
					+ "FROM `INFORMATION_SCHEMA`.`KEY_COLUMN_USAGE` "
					+ "WHERE `TABLE_SCHEMA` = ? AND `REFERENCED_TABLE_NAME` IS NOT NULL " + "ORDER BY `TABLE_NAME`");
			ps.setString(1, dbName);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				names.add(rs.getString("TABLE_NAME"));
				names.add(rs.getString("REFERENCED_TABLE_NAME"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return names;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, JSONArray> getRelatedTablesData(String dbName) {
		Map<String, JSONArray> data = new TreeMap<>();
		for (String tableName : getRelatedTablesNames(dbName))
			try (ResultSet resultSet = DBConnection.getConnection()
					.prepareStatement("SELECT * FROM `" + dbName + "`.`" + tableName + "`").executeQuery()) {
				ResultSetMetaData metadata = resultSet.getMetaData();
				JSONArray rows = new JSONArray();
				while (resultSet.next()) {
					JSONObject row = new JSONObject();
					for (int i = 1; i <= metadata.getColumnCount(); i++)
						row.put(metadata.getColumnName(i), resultSet.getString(i));
					rows.add(row);
				}
				data.put(tableName, rows);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return data;
	}

}
