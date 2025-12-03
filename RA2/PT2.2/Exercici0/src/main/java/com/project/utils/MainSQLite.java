package com.project.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class MainSQLite {

    public static void main(String[] args) {
        String basePath = System.getProperty("user.dir") + "/data/";
        String filePath = basePath + "database.db";

        try (Connection conn = UtilsSQLite.connect(filePath)) {

            if (conn == null) {
                System.out.println("No s'ha pogut establir connexió amb la base de dades.");
                return;
            }

            List<String> taules = UtilsSQLite.listTables(conn);
            System.out.println("Taules trobades: " + taules);
            System.out.println("--------------------------------------------------");

            for (String nomTaula : taules) {
                
                if (nomTaula.startsWith("sqlite_")) continue;

                System.out.println("TAULA: " + nomTaula);

                try (ResultSet rs = UtilsSQLite.querySelect(conn, "SELECT * FROM " + nomTaula)) {
                    
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int numColumnes = rsmd.getColumnCount();

                    System.out.println("  Columnes:");
                    for (int i = 1; i <= numColumnes; i++) {
                        String label = rsmd.getColumnLabel(i);
                        String typeName = rsmd.getColumnTypeName(i);
                        System.out.println("    - " + label + " (" + typeName + ")");
                    }

                    System.out.println("  Dades:");
                    boolean teDades = false;
                    
                    while (rs.next()) {
                        teDades = true;
                        StringBuilder fila = new StringBuilder("    [");
                        
                        for (int i = 1; i <= numColumnes; i++) {
                            String nomColumna = rsmd.getColumnName(i);
                            int tipusColumna = rsmd.getColumnType(i);

                            if (i > 1) fila.append(", ");

                            switch (tipusColumna) {
                                case java.sql.Types.INTEGER:
                                case java.sql.Types.TINYINT:
                                case java.sql.Types.SMALLINT:
                                    fila.append(rs.getInt(nomColumna));
                                    break;
                                case java.sql.Types.VARCHAR:
                                case java.sql.Types.CHAR:
                                case java.sql.Types.LONGVARCHAR:
                                    String valor = rs.getString(nomColumna);
                                    fila.append(valor != null ? "'" + valor + "'" : "NULL");
                                    break;
                                case java.sql.Types.REAL:
                                case java.sql.Types.FLOAT:
                                case java.sql.Types.DOUBLE:
                                    fila.append(rs.getDouble(nomColumna));
                                    break;
                                case java.sql.Types.BOOLEAN:
                                case java.sql.Types.BIT:
                                    fila.append(rs.getBoolean(nomColumna));
                                    break;
                                case java.sql.Types.BIGINT:
                                    fila.append(rs.getLong(nomColumna));
                                    break;
                                default:
                                    Object obj = rs.getObject(nomColumna);
                                    fila.append(obj != null ? obj.toString() : "NULL");
                                    break;
                            }
                        }
                        fila.append("]");
                        System.out.println(fila.toString());
                    }
                    
                    if (!teDades) {
                        System.out.println("    (Sense dades)");
                    }
                    System.out.println("--------------------------------------------------");
                    
                } catch (SQLException e) {
                    System.err.println("Error llegint la taula " + nomTaula + ": " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.err.println("Error general a la base de dades: " + e.getMessage());
            e.printStackTrace();
        }
    }
}