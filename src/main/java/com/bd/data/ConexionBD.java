package com.bd.data;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ConexionBD {
    private static DSLContext dsl;
    private static Connection connection;

    public static DSLContext getDsl() {
        if (dsl == null) {
            try {
                String url = getDatabaseUrl();
                // SQLite no requiere usuario ni contraseña
                connection = DriverManager.getConnection(url);

                // Configurar jOOQ para SQLite
                DefaultConfiguration config = new DefaultConfiguration();
                config.set(connection);
                config.set(SQLDialect.SQLITE);
                dsl = DSL.using(config);

                // Crear tabla si no existe (para producción)
                crearTablaSiNoExiste();

                System.out.println("✅ Conectado a SQLite");
            } catch (Exception e) {
                throw new RuntimeException("Error al conectarse a SQLite", e);
            }
        }
        return dsl;
    }

    // Detecta automáticamente producción vs desarrollo:
    private static String getDatabaseUrl() {
        // Detectar si estamos ejecutando desde un JAR (producción)
        String classPath = ConexionBD.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath();

        boolean isProduction = classPath.endsWith(".jar")
                || classPath.contains(".exe")
                || System.getProperty("java.class.path").contains(".exe");

        if (isProduction) {
            // Asegurarnos de que la carpeta exista en producción
            String userHome = System.getProperty("user.home");
            // ej. C:\Users\Juan\escuela\alumnos.db

            File dir = new File(userHome, "escuela");
            if (!dir.exists()) dir.mkdirs();

            System.out.println("Modo PRODUCCIÓN - Base de datos en: " + dir.getAbsolutePath());
            return "jdbc:sqlite:" + dir.getAbsolutePath() + "/alumnos.db";
        } else {
            // Asegurarnos de que la carpeta exista en desarrollo. Guardar en el proyecto
            File dir = new File("./escuela");
            if (!dir.exists()) dir.mkdirs();

            System.out.println("Modo DESARROLLO - Base de datos en: ./escuela/alumnos.db");
            return "jdbc:sqlite:./escuela/alumnos.db";
        }
    }

    /*
    Cada vez que inicies tu aplicación y llames a ConexionBD.getDsl(), el sistema automáticamente
    creará el archivo alumnos.db y la tabla si no existen. No necesitas una clase separada para esto.
    * */
    private static void crearTablaSiNoExiste() {
        try (Statement stmt = connection.createStatement()) {
            // ATENCIÓN: En SQLite es INTEGER PRIMARY KEY AUTOINCREMENT
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS alumno (
                    id_alumno INTEGER AUTO_INCREMENT PRIMARY KEY,
                    nombre VARCHAR(100) NOT NULL,
                    apellido_paterno VARCHAR(100) NOT NULL,
                    apellido_materno VARCHAR(100),
                    edad INTEGER,
                    curp VARCHAR(18) UNIQUE NOT NULL,
                    grado VARCHAR(10),
                    grupo VARCHAR(10),
                    sexo VARCHAR(1)
                );
            """);
        } catch (Exception e) {
            System.err.println("Error creando tabla: " + e.getMessage());
        }
    }

    public static void cerrar() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) { System.err.println("Error cerrando conexión: " + e.getMessage()); }
    }
}
