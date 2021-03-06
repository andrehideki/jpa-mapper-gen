package br.com.jpamappergen.infra;

import java.io.FileReader;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.h2.tools.RunScript;

import br.com.jpamappergen.domain.entity.DataColumn;
import br.com.jpamappergen.domain.entity.PrimaryKey;
import br.com.jpamappergen.domain.entity.Table;

public class JDBCAdapter {
	
	private Connection conn;
	
	public JDBCAdapter(String jdbcUrl, String user, String password) {
		try {
			this.conn = DriverManager.getConnection(jdbcUrl, user, password);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create connection", e);
		}
	}
	
	public void close() {
		try {
			conn.close();
		} catch (Exception e) {
			throw new RuntimeException("Failed to close connection", e);
		}
	}
	
	public void runScript(Path scriptPath) {
		try {
			RunScript.execute(conn, new FileReader(scriptPath.toFile()));
		} catch (Exception e) {
			throw new RuntimeException("Failed to run Script", e);
		}
	}
	
	public List<Table> getTables() {
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet result = metaData.getTables(null, null, "%", new String[]{ "TABLE" });
			List<Table> tables = new ArrayList<>();
			while (result.next()) {
				String tableName = result.getString("TABLE_NAME");
				Table table = this.getTable(tableName);
				tables.add(table);
			}
			return tables;
		} catch (Exception e) {
			throw new RuntimeException("Failed to get Tables", e);
		}
	}

	public Table getTable(String name) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + name);
			ResultSetMetaData metaData = resultSet.getMetaData();
			int i = 1;
			List<DataColumn> columns = new ArrayList<>();
			while (i != metaData.getColumnCount() + 1) {
				String label = metaData.getColumnLabel(i);
				String type = metaData.getColumnTypeName(i);
				columns.add(new DataColumn(label, type));
				i++;
			}
			stmt.close();
			return new Table(name, columns, getPrimaryKey(name));
		} catch (Exception e) {
			throw new RuntimeException("Failed to get Table", e);
		}
	}
	
	public PrimaryKey getPrimaryKey(String tableName) {
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet result = metaData.getPrimaryKeys(null, null, tableName);
			List<DataColumn> columns = new ArrayList<>();
			while (result.next()) {
				String label = result.getString("COLUMN_NAME");
				columns.add(new DataColumn(label, "INTEGER"));
			}
			return new PrimaryKey(columns);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get primary key", e);
		}
	}
}
