package com.bd.repository;

import com.bd.data.ConexionBD;
import com.bd.jooq.tables.pojos.Alumno; // POJO
import static com.bd.jooq.tables.Alumno.ALUMNO; // estatico de la tabla

import org.jooq.DSLContext;
import java.util.List;

public class AlumnoRepository {

    public List<Alumno> listarTodos() {
        DSLContext dsl = ConexionBD.getDsl();
        return dsl.selectFrom(ALUMNO).fetchInto(Alumno.class); // Convierte Record a POJO

    }

    // gaurdar
    public void guardar(Alumno alumnoPojo) {
        DSLContext dsl = ConexionBD.getDsl();
        dsl.newRecord(ALUMNO, alumnoPojo).store();
    }

    // modificar
    public void modificar(Alumno alumnoPojo) {
        DSLContext dsl = ConexionBD.getDsl();
        dsl.newRecord(ALUMNO, alumnoPojo).update();
    }

    // eliminar
    public void eliminar(Integer id) {
        DSLContext dsl = ConexionBD.getDsl();
        dsl.deleteFrom(ALUMNO).where(ALUMNO.ID_ALUMNO.eq(id))
                .execute();
    }
}
