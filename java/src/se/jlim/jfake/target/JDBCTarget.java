/*
 * The MIT License
 *
 * Copyright 2016 Jonas Lund.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.jlim.jfake.target;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author Jonas Lund
 */
public class JDBCTarget implements JFakeTarget, AutoCloseable {

	Connection conn;

	public JDBCTarget(DataSource ds) throws SQLException {
		conn = ds.getConnection();
	}

	public JDBCTarget(String connectionString, String username, String password) throws SQLException {
		if (username!=null && password!=null)
			conn = DriverManager.getConnection(connectionString,username,password);
		else
			conn = DriverManager.getConnection(connectionString);
	}

	@Override
	public String findNonEmpty(Set<String> tables) {
		try (Statement stmt = conn.createStatement()) {
			for (String tableName : tables) {
				try (ResultSet res = stmt.executeQuery("SELECT * FROM " + tableName + ";")) {
					if (res.next()) {
						return tableName;
					}
				}
			}
		} catch (SQLException ex) {
			throw new RuntimeException("XFake JDBC problem", ex);
		}
		return null;
	}

	@Override
	public void begin() {
		try {
			conn.setAutoCommit(false);
		} catch (SQLException ex) {
			try {
				ex.printStackTrace();
				conn.close();
			} catch (SQLException ex1) {}
		}
	}
	
	@Override
	public void pushTable(String table, String[] columns, Object[][] data) {
		try {
			String query="INSERT INTO "+table+" ("
					+Arrays.stream(columns).reduce((a,b)->a+","+b).get()
					+") VALUES ("
					+Arrays.stream(columns).map(cn->"?").reduce((a,b)->a+","+b).get()
					+");";
			try (PreparedStatement pst=conn.prepareStatement(query)) {
				for (int i=0;i<data[0].length;i++) {
					for (int j=0;j<data.length;j++)
						pst.setObject(1+j, data[j][i]);
					pst.execute();
				}
			}
		} catch (SQLException ex) {
			try {
				ex.printStackTrace();
				conn.close();
			} catch (SQLException ex1) {}
		}
	}

	@Override
	public void commit() {
		try {
			conn.commit();
		} catch (SQLException ex) {
			try {
				ex.printStackTrace();
				conn.close();
			} catch (SQLException ex1) {}
		}
	}
	
	@Override
	public void close() throws SQLException {
		conn.close();
	}
}
