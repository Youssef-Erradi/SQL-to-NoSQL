package pojos;

public class Relationship {
	private String tableName, columnName, referencedTableName, referencedColumnName;

	public Relationship() {
	}

	public Relationship(String tableName, String columnName, String referencedTableName, String referencedColumnName) {
		this.tableName = tableName;
		this.columnName = columnName;
		this.referencedTableName = referencedTableName;
		this.referencedColumnName = referencedColumnName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getReferencedTableName() {
		return referencedTableName;
	}

	public void setReferencedTableName(String referencedTableName) {
		this.referencedTableName = referencedTableName;
	}

	public String getReferencedColumnName() {
		return referencedColumnName;
	}

	public void setReferencedColumnName(String referencedColumnName) {
		this.referencedColumnName = referencedColumnName;
	}

	@Override
	public String toString() {
		return String.format("`%s` de `%s` fait référence à `%s` de `%s`", columnName, tableName, referencedColumnName,
				referencedTableName);
	}

}
