package com.bd.controller;

import com.bd.service.ReporteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.bd.repository.AlumnoRepository;
import com.bd.jooq.tables.pojos.Alumno;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EscuelaController {

    @FXML
    private TableView<Alumno> alumnoTabla;
    @FXML
    private TableColumn<Alumno, Integer> idAlumno;
    @FXML
    private TableColumn<Alumno, String> nombreAlumno;
    @FXML
    private TableColumn<Alumno, String> aPaterno;
    @FXML
    private TableColumn<Alumno, String> aMaterno;
    @FXML
    private TableColumn<Alumno, String> curp;
    @FXML
    private TableColumn<Alumno, Integer> edad;
    @FXML
    private TableColumn<Alumno, String> grado;

    @FXML
    private TextField txtNombreAlumno;
    @FXML
    private TextField txtAPaterno;
    @FXML
    private TextField txtAMaterno;
    @FXML
    private TextField txtCurp;
    @FXML
    private TextField txtEdad;
    @FXML
    private TextField txtGrado;

    private final AlumnoRepository repositorio = new AlumnoRepository();

    // lista observable de POJOS
    private final ObservableList<Alumno> listarAlumnos = FXCollections.observableArrayList();
    private Integer idAlumnoSeleccionado = null; // para saber si estamos editando

    private final ReporteService reporteService = new ReporteService();

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatosTabla();
    }

    private void configurarColumnas() {
        idAlumno.setCellValueFactory(new PropertyValueFactory<>("idAlumno"));
        nombreAlumno.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        aPaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoPaterno"));
        aMaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoMaterno"));
        curp.setCellValueFactory(new PropertyValueFactory<>("curp"));
        edad.setCellValueFactory(new PropertyValueFactory<>("edad"));
        grado.setCellValueFactory(new PropertyValueFactory<>("grado"));
    }

    private void cargarDatosTabla() {
        listarAlumnos.clear();
        listarAlumnos.addAll(repositorio.listarTodos());
        alumnoTabla.setItems(listarAlumnos);
    }

    @FXML
    public void agregarAlumno() {
        if(txtNombreAlumno.getText().isEmpty() || txtCurp.getText().isEmpty() || txtEdad.getText().isEmpty()) {
            mostrarAlerta("Validación", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }
        if(idAlumnoSeleccionado == null) {
            // Creamos elobjeto POJO y lo llenamos
            Alumno nuevo = new Alumno();
            nuevo.setNombre(txtNombreAlumno.getText());
            nuevo.setApellidoPaterno(txtAPaterno.getText());
            nuevo.setApellidoMaterno(txtAMaterno.getText());
            nuevo.setCurp(txtCurp.getText());
            nuevo.setEdad(Integer.parseInt(txtEdad.getText()));
            nuevo.setGrado(txtGrado.getText());

            repositorio.guardar(nuevo);

            limpiarFormulario();
            cargarDatosTabla();
            mostrarAlerta("Éxito", "Alumno registrado correctamente", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Información", "Ya existe un alumno, Use modificar", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void modificarAlumno() {
        if(idAlumnoSeleccionado == null) {
            mostrarAlerta("Error", "Debes seleccionar un alumno de la tabla", Alert.AlertType.WARNING);
            return;
        }

        Alumno modificado = new Alumno();
        modificado.setIdAlumno(idAlumnoSeleccionado);
        modificado.setNombre(txtNombreAlumno.getText());
        modificado.setApellidoPaterno(txtAPaterno.getText());
        modificado.setApellidoMaterno(txtAMaterno.getText());
        modificado.setCurp(txtCurp.getText());
        modificado.setEdad(Integer.parseInt(txtEdad.getText()));
        modificado.setGrado(txtGrado.getText());

        repositorio.modificar(modificado);
        limpiarFormulario();
        cargarDatosTabla();
        mostrarAlerta("Éxito", "Se modificaron los datos del alumno", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void eliminarAlumno() {
        if(idAlumnoSeleccionado == null) {
            mostrarAlerta("Error", "Selecciona un alumno para eliminar", Alert.AlertType.ERROR);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText(null);
        alert.setContentText("¿Está seguro de que desea eliminar este alumno?");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            try {
                repositorio.eliminar(idAlumnoSeleccionado);
                limpiarFormulario();
                cargarDatosTabla();
                mostrarAlerta("Éxito", "Alumno eliminado correctamente", Alert.AlertType.INFORMATION);
            } catch(Exception e) {
                System.out.print(e.getMessage());
            }
        }
    }

    @FXML
    public void generarReporte(){
        // 1. Obtener datos ACTUALES de la tabla (lo que ve el usuario)
        List<Alumno> datosTabla = alumnoTabla.getItems();

        if (datosTabla.isEmpty()) {
            mostrarAlerta("Sin datos", "No hay tareas en la tabla para generar el reporte.", Alert.AlertType.INFORMATION);
            return;
        }

        // 2. Crear una COPIA de los datos para pasar al otro hilo (Thread-safe)
        List<Alumno> copiaDatos = new ArrayList<>(datosTabla);

        // 3. Crear la Tarea en segundo plano (Background Task)
        Task<String> tareaGenerarPdf = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Esto se ejecuta fuera del hilo de JavaFX (No congela la app)
                return reporteService.generarReporteAlumnos(copiaDatos);
            }
        };

        // 4. Qué hacer cuando termine con éxito
        tareaGenerarPdf.setOnSucceeded(e -> {
            String rutaArchivo = tareaGenerarPdf.getValue();
            mostrarDialogoExito(rutaArchivo);
        });

        // 5. Qué hacer si falla
        tareaGenerarPdf.setOnFailed(e -> {
            Throwable error = tareaGenerarPdf.getException();
            mostrarAlerta("Error", "No se pudo generar el reporte: ", Alert.AlertType.ERROR);
            error.printStackTrace();
        });

        // 6. Iniciar el hilo
        new Thread(tareaGenerarPdf).start();
    }

    @FXML
    public void limpiarFormulario() {
        txtNombreAlumno.clear();
        txtAPaterno.clear();
        txtAMaterno.clear();
        txtCurp.clear();
        txtGrado.clear();
        txtEdad.clear();
        idAlumnoSeleccionado = null;
        alumnoTabla.getSelectionModel().clearSelection();
    }

    @FXML
    public void cargarAlumnoFormulario() {
        // Obtenemos un POJO de la seleccion
        Alumno alumno = alumnoTabla.getSelectionModel().getSelectedItem();
        if(alumno != null) {
            idAlumnoSeleccionado = alumno.getIdAlumno();
            txtNombreAlumno.setText(alumno.getNombre());
            txtAPaterno.setText(alumno.getApellidoPaterno());
            txtAMaterno.setText(alumno.getApellidoMaterno());
            txtCurp.setText(alumno.getCurp());
            txtEdad.setText(alumno.getEdad().toString());
            txtGrado.setText(alumno.getGrado());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarDialogoExito(String ruta) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reporte Generado");
        alert.setHeaderText("¡Reporte PDF creado exitosamente!");
        alert.setContentText("Guardado en:\n" + ruta + "\n\n¿Deseas abrirlo ahora?");

        ButtonType btnAbrir = new ButtonType("Abrir PDF");
        ButtonType btnCerrar = new ButtonType("Cerrar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnAbrir, btnCerrar);

        alert.showAndWait().ifPresent(tipo -> {
            if (tipo == btnAbrir) {
                // Llamamos al método estático del servicio para abrirlo
                ReporteService.abrirArchivo(ruta);
            }
        });
    }
}
