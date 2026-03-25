package com.ryan.bartill.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

public final class Db {
    private static final String URL = "jdbc:sqlite:bar_till.db";
    private static boolean initialized = false;

    private Db() {}

    public static Connection getConnection() throws Exception {
        Connection conn = DriverManager.getConnection(URL);
        if (!initialized) {
            runSchema(conn);
            initialized = true;
        }
        return conn;
    }

    private static void runSchema(Connection conn) throws Exception {
        String sql;
        try (var in = Db.class.getResourceAsStream("/db/schema.sql")) {
            if (in == null) throw new IllegalStateException("Missing /db/schema.sql in resources");
            try (var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }
        }

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }

        ensureColumnExists(conn, "products", "category_id", "category_id INTEGER");


    }

    private static void ensureColumnExists(Connection conn, String table, String column, String columnSql) throws Exception {
        boolean exists = false;

        try (var ps = conn.prepareStatement("PRAGMA table_info(" + table + ")")) {
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("name");
                    if (column.equalsIgnoreCase(colName)) {
                        exists = true;
                        break;
                    }
                }
            }
        }

        if (!exists) {
            try (var st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + columnSql);
            }
        }
    }
}