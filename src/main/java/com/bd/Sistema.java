package com.bd;

import com.bd.data.ConexionBD;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;

public class Sistema extends Application {

    @Override
    public void start(Stage splashStage) throws IOException {
        // 1 Cargar y mostrar el Splah Screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/templates/splash-view.fxml"));
        Scene splashScene = new Scene(fxmlLoader.load());

        ProgressBar barra = (ProgressBar) splashScene.lookup("#barraProgreso");
        Label etiqueta = (Label) splashScene.lookup("#lblEstado");

        // Quitamos los bordes de ventana (Minimizar, Cerrar) para que parezca profesional
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setScene(splashScene);
        splashStage.setTitle("Cargando...");
        // Poner icono al splash
        cargarIcono(splashStage);
        splashStage.show();

        // 2 Cargar la tarea pesada (Conectar BD) con progreso
        Task<Void> tareaCarga = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // AQUÍ VA LO PESADO. Esto corre en otro hilo.
                // Paso 1: Inicio (0%)
                updateMessage("Iniciando motor...");
                updateProgress(20, 100);
                Thread.sleep(300); // Pequeña pausa para que se vea

                // Paso 2: Conectando (40%)
                updateMessage("Conectando a Base de Datos...");
                updateProgress(50, 100);

                // ESTO ES LO PESADO
                ConexionBD.getDsl();

                // Paso 3: Verificando tablas (80%)
                updateMessage("Verificando...");
                updateProgress(80, 100);
                Thread.sleep(300);

                // Paso 4: Finalizando (100%)
                updateMessage("¡Listo!");
                updateProgress(100, 100);
                // Thread.sleep(200);

                return null;
            }
        };

        // 3 VINCULAR LA VISTA CON LA TAREA (BINDING)
        // Esto hace que la barra se mueva sola cuando llamamos a updateProgress
        if (barra != null) {
            barra.progressProperty().bind(tareaCarga.progressProperty());
        }
        if (etiqueta != null) {
            etiqueta.textProperty().bind(tareaCarga.messageProperty());
        }

        // 4 ¿Qué hacer cuando termine la carga?
        tareaCarga.setOnSucceeded(event -> {
            try {
                mostrarVentanaPrincipal();
                splashStage.close(); // Cerramos el splash
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });

        tareaCarga.setOnFailed(event -> {
            Throwable error = tareaCarga.getException();
            System.err.println("Error crítico: " + error.getMessage());
            // Aquí podrías mostrar una alerta de error y cerrar
            splashStage.close();
        });

        // 5 Iniciar el hilo
        new Thread(tareaCarga).start();
    }

    private void cargarIcono(Stage stage) {
        try {
            InputStream iconStream = getClass().getResourceAsStream("/images/icono.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono.");
        }
    }

    private void mostrarVentanaPrincipal() throws IOException {
        Stage mainStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/templates/escuela.fxml")); // Tu vista principal

        // El tamaño con el que la ventana se abre por defecto
        double initialWidth = 1280; // 1200
        double initialHeight = 700; // 720

        Scene scene = new Scene(fxmlLoader.load(), initialWidth, initialHeight); // Ajusta tamaño si es necesario

        mainStage.setTitle("Escuela");
        cargarIcono(mainStage);
        mainStage.setScene(scene);

        // EL tamaño mínimo al que el usuario puede reducir la ventana
        mainStage.setMinWidth(1000); // 1024
        mainStage.setMinHeight(650); //600 - un poco el alto mínimo para que quepan los botones
        mainStage.show();
    }

    @Override
    public void stop() {
        ConexionBD.cerrar();
        Platform.exit();
    }

    // Este método debe ser estático para ser llamado desde Main
    public static void main(String[] args) {
        launch();
    }
}
