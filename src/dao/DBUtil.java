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

import pojos.Relationship;

@SuppressWarnings("unchecked")
public class DBUtil {

	private static final List<Relationship> relationships = new ArrayList<>();

	private DBUtil() {}

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

	public static List<Relationship> getRelationshipsBetweenTables(String dbName) {
		relationships.clear();
		try {
			Connection connection = DBConnection.getConnection();
			PreparedStatement ps = connection.prepareStatement(
					"SELECT `TABLE_NAME`, `COLUMN_NAME`, `REFERENCED_TABLE_NAME`,`REFERENCED_COLUMN_NAME` "
							+ "FROM `INFORMATION_SCHEMA`.`KEY_COLUMN_USAGE` "
							+ "WHERE `TABLE_SCHEMA` = ? AND `REFERENCED_TABLE_NAME` IS NOT NULL "
							+ "ORDER BY `TABLE_NAME`");
			ps.setString(1, dbName);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				relationships.add(new Relationship(rs.getString("TABLE_NAME"), rs.getString("COLUMN_NAME"),
						rs.getString("REFERENCED_TABLE_NAME"), rs.getString("REFERENCED_COLUMN_NAME")));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return relationships;
	}

	private static Set<String> getRelatedTablesNames(String dbName) {
		Set<String> names = new HashSet<>();
		relationships.forEach(r -> {
			names.add(r.getTableName());
			names.add(r.getReferencedTableName());
		});
		return names;
	}

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

	public static JSONArray getRelatedTablesAggregatedData(String dbName) {
		JSONArray data = new JSONArray();
		final Connection connection = DBConnection.getConnection();
		try {
			connection.createStatement().execute("USE `" + dbName + "`");
			for (Relationship r : relationships) {
				ResultSet resultSet = connection.prepareStatement("SELECT * FROM " + r.getTableName()).executeQuery();
				ResultSetMetaData metadata = resultSet.getMetaData();
				while (resultSet.next()) {
					JSONObject row = new JSONObject();
					for (int i = 1; i <= metadata.getColumnCount(); i++)
						if (r.getColumnName().equals(metadata.getColumnName(i))) {
							String sql = "SELECT * FROM " + r.getReferencedTableName() + " WHERE "
									+ r.getReferencedColumnName() + "=" + resultSet.getString(i);
							row.put(r.getColumnName(), getTableRowData(sql));
						} else
							row.put(metadata.getColumnName(i), resultSet.getString(i));
					data.add(row);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}

	private static JSONObject getTableRowData(final String sql) {
		JSONObject object = new JSONObject();
		try {
			ResultSet resultSet = DBConnection.getConnection().prepareStatement(sql).executeQuery();
			ResultSetMetaData metadata = resultSet.getMetaData();
			if (resultSet.next())
				for (int i = 1; i <= metadata.getColumnCount(); i++) 
					object.put(metadata.getColumnName(i), resultSet.getString(i));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return object;
	}

}
