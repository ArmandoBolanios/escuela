//package com.bd.data;
//
//import java.io.File;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.Statement;
//
//public class GeneradorBD {
//    static void main() {
//        try {
//            // 1. Crear la carpeta si no existe
//            File dir = new File("./escuela");
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            // 2. Conectar a SQLite (esto crea el archivo alumnos.db automáticamente)
//            String url = "jdbc:sqlite:./escuela/alumnos.db";
//            Connection conn = DriverManager.getConnection(url);
//            Statement stmt = conn.createStatement();
//
//            // 3. Crear la tabla
//            stmt.execute("""
//                CREATE TABLE IF NOT EXISTS alumno (
//                    id_alumno INTEGER PRIMARY KEY AUTOINCREMENT,
//                    nombre VARCHAR(100) NOT NULL,
//                    apellido_paterno VARCHAR(100) NOT NULL,
//                    apellido_materno VARCHAR(100),
//                    edad INTEGER,
//                    curp VARCHAR(18) UNIQUE NOT NULL,
//                    grado VARCHAR(10),
//                    grupo VARCHAR(10),
//                    sexo VARCHAR(1)
//                );
//            """);
//
//            System.out.println("✅ Base de datos SQLite creada con éxito en: ./escuela/alumnos.db");
//            conn.close();
//        }  catch (Exception e) {
//            System.out.println("❌ Error: " + e.getMessage());
//        }
//    }
//}
