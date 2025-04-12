package conn;

import java.sql.*;
import java.util.*;

public class Database implements AutoCloseable {
    // Database credentials
    private String dbHost = "localhost";
    private String dbUser = "system";
    private String dbPass = "IamNahid";
    private String port = "1521";
    private String serviceName = "xe"; 
    
    private List<Object> result = new ArrayList<>();
    private String myQuery = "";
    private Connection conn = null;
    private Statement stmt = null;
    
    public String hostname = "http://localhost:8089/ShortUrlProject/v/";

    public Database() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String jdbcUrl = "jdbc:oracle:thin:@" + dbHost + ":" + port + "/" + serviceName;
            conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
            stmt = conn.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
            result.add(e.getMessage());
        }
    }

    public boolean insert(String table, String[] columns, Object[] values) {
        if (columns.length != values.length) {
            result.add("Columns and values arrays must be of the same length.");
            return false;
        }
        if (!tableExists(table)) return false;

        try {
            String columnsStr = String.join(", ", columns);
            String placeholders = String.join(", ", Collections.nCopies(columns.length, "?"));
            
            String sql = "INSERT INTO " + table + " (" + columnsStr + ") VALUES (" + placeholders + ")";
            myQuery = sql;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        result.add(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            result.add(e.getMessage());
        }
        return false;
    }

    public boolean update(String table, String[] columns, Object[] values, String whereClause, Object[] whereValues) {
        if (columns.length != values.length) {
            result.add("Columns and values arrays must be of the same length.");
            return false;
        }
        if (!tableExists(table)) return false;

        try {
            List<String> setClauses = new ArrayList<>();
            for (String column : columns) {
                setClauses.add(column + " = ?");
            }
            
            String sql = "UPDATE " + table + " SET " + String.join(", ", setClauses);
            if (whereClause != null && !whereClause.isEmpty()) {
                sql += " WHERE " + whereClause;
            }
            myQuery = sql;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int paramIndex = 1;
            
            for (Object value : values) {
                pstmt.setObject(paramIndex++, value);
            }
            
            if (whereClause != null && whereValues != null) {
                for (Object value : whereValues) {
                    pstmt.setObject(paramIndex++, value);
                }
            }
            
            int affectedRows = pstmt.executeUpdate();
            result.add(affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            result.add(e.getMessage());
        }
        return false;
    }

    public boolean delete(String table, String whereClause, Object[] whereValues) {
        if (!tableExists(table)) return false;

        try {
            String sql = "DELETE FROM " + table;
            if (whereClause != null && !whereClause.isEmpty()) {
                sql += " WHERE " + whereClause;
            }
            myQuery = sql;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            if (whereClause != null && whereValues != null) {
                for (int i = 0; i < whereValues.length; i++) {
                    pstmt.setObject(i + 1, whereValues[i]);
                }
            }
            
            int affectedRows = pstmt.executeUpdate();
            result.add(affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            result.add(e.getMessage());
        }
        return false;
    }

    public boolean select(String table, String rows, String join, String whereClause, Object[] whereValues, String order, Integer limit) {
        if (!tableExists(table)) return false;

        try {
            String sql = "SELECT " + rows + " FROM " + table;
            if (join != null && !join.isEmpty()) sql += " JOIN " + join; 
            if (whereClause != null && !whereClause.isEmpty()) sql += " WHERE " + whereClause; 
            if (order != null && !order.isEmpty()) sql += " ORDER BY " + order; 

            List<Object> parameters = new ArrayList<>();
            if (whereValues != null) {
                parameters.addAll(Arrays.asList(whereValues));
            }

            boolean hasLimit = limit != null;
            if (hasLimit) {
                sql = "SELECT * FROM (" + sql + ") WHERE ROWNUM <= ?";
                parameters.add(limit);
            }
            myQuery = sql;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = metaData.getColumnName(i + 1);
            }
            result.add(columnNames);
            
            List<Object[]> rowsList = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                rowsList.add(row);
            }
            result.add(rowsList);
            return true;
        } catch (SQLException e) {
            result.add(e.getMessage());
        }
        return false;
    }

    private boolean tableExists(String table) {
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, table.toUpperCase(), null);
            return tables.next();
        } catch (SQLException e) {
            result.add(e.getMessage());
        }
        return false;
    }
    
    public boolean checkIsEmpty(Object[] values) {
        boolean allValid = true;
        for (Object value : values) {
            if (value == null) {
                allValid = false;
            } else if (value instanceof String && ((String) value).trim().isEmpty()) {
                allValid = false;
            } else if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
                allValid = false;
            }
        }
        
        return allValid;
    }

    public List<Object> getResult() {
        List<Object> val = new ArrayList<>(result);
        result.clear();
        return val;
    }

    public String getSql() {
        String val = myQuery;
        myQuery = "";
        return val;
    }

    @Override
    public void close() {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            result.add(e.getMessage());
        }
    }
}