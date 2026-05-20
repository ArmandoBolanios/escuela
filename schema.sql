
CREATE TABLE IF NOT EXISTS alumno (
    id_alumno INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    edad INT CHECK (edad >= 0 AND edad <= 120),
    curp VARCHAR(18) UNIQUE NOT NULL,
    grado VARCHAR(10),
    grupo VARCHAR(10),
    sexo VARCHAR(1) CHECK (sexo IN ('M', 'F')),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
