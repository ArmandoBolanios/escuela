package com.bd.service;

import com.bd.jooq.tables.pojos.Alumno;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
public class ReporteService {

    // Fuentes estáticas para no recrearlas en cada llamada (Optimización)
    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);
    private static final Font FUENTE_HEADER = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, java.awt.Color.WHITE);
    private static final Font FUENTE_DATOS = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);

    /**
     * Genera el PDF y retorna la ruta absoluta del archivo creado.
     */
    public String generarReporteAlumnos(List<Alumno> listaAlumnos) throws Exception {
        // 1. Definir nombre y ruta
        String nombreArchivo = "Reporte_" + System.currentTimeMillis() + ".pdf";
        File archivoDestino = obtenerRutaDestino(nombreArchivo);

        // 2. Crear documento (Tamaño CARTA)
        Document documento = new Document(PageSize.LETTER.rotate()); // Horizontal para que quepa mejor
        PdfWriter.getInstance(documento, new FileOutputStream(archivoDestino));

        // 3. Abrir y construir
        documento.open();
        agregarEncabezado(documento);
        agregarTabla(documento, listaAlumnos);
        documento.close();

        return archivoDestino.getAbsolutePath();
    }

    private void agregarEncabezado(Document doc) throws DocumentException {
        Paragraph titulo = new Paragraph("LISTA DE ALUMNOS", FUENTE_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(10);
        doc.add(titulo);

        String fechaStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        Paragraph fecha = new Paragraph("Generado el: " + fechaStr, FUENTE_DATOS);
        fecha.setAlignment(Element.ALIGN_RIGHT);
        fecha.setSpacingAfter(20);
        doc.add(fecha);
    }

    private void agregarTabla(Document doc, List<Alumno> alumnos) throws DocumentException {
        // 4 columnas: ID, Tarea, Responsable, Estatus
        PdfPTable tabla = new PdfPTable(7);
        tabla.setWidthPercentage(100);
        // Anchos relativos: ID, Nombre, APaterno, AMaterno, CURP, edad, grado
        tabla.setWidths(new float[]{1f, 2.5f, 2.5f, 2.5f, 2.5f, 1f, 1f});

        // Encabezados
        crearCeldaEncabezado(tabla, "#");
        crearCeldaEncabezado(tabla, "NOMBRE");
        crearCeldaEncabezado(tabla, "A. PATERNO");
        crearCeldaEncabezado(tabla, "A. MATERNO");
        crearCeldaEncabezado(tabla, "CURP");
        crearCeldaEncabezado(tabla, "EDAD");
        crearCeldaEncabezado(tabla, "GRADO");
        // Datos
        if (alumnos == null || alumnos.isEmpty()) {
            PdfPCell celdaVacia = new PdfPCell(new Phrase("No hay datos para mostrar", FUENTE_DATOS));
            celdaVacia.setColspan(7);
            celdaVacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaVacia.setPadding(10);
            tabla.addCell(celdaVacia);
        } else {
            for (Alumno t : alumnos) {
                // Asegúrate de usar los getters correctos de tu POJO JOOQ
                crearCeldaDato(tabla, String.valueOf(t.getIdAlumno()));
                crearCeldaDato(tabla, t.getNombre());
                crearCeldaDato(tabla, t.getApellidoPaterno());
                crearCeldaDato(tabla, t.getApellidoMaterno());
                crearCeldaDato(tabla, t.getCurp());
                crearCeldaDato(tabla, String.valueOf(t.getEdad()));
                crearCeldaDato(tabla, t.getGrado());
            }
        }

        doc.add(tabla);
    }

    private void crearCeldaEncabezado(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_HEADER));
        celda.setBackgroundColor(java.awt.Color.decode("#1d3557")); // Tu azul corporativo
        celda.setPadding(8);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(celda);
    }

    private void crearCeldaDato(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "", FUENTE_DATOS));
        celda.setPadding(5);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tabla.addCell(celda);
    }

    /**
     * Lógica Cross-Platform para encontrar la carpeta de descargas.
     * Funciona en Linux (Fedora) y Windows.
     */
    private File obtenerRutaDestino(String nombreArchivo) {
        String userHome = System.getProperty("user.home");
        File descargas;

        // Intenta "Downloads" (Estándar Windows y Linux inglés)
        File opcionA = new File(userHome, "Downloads");
        // Intenta "Descargas" (Linux español)
        File opcionB = new File(userHome, "Descargas");

        if (opcionA.exists() && opcionA.isDirectory()) {
            descargas = opcionA;
        } else if (opcionB.exists() && opcionB.isDirectory()) {
            descargas = opcionB;
        } else {
            // Si falla todo, guardar en el Home del usuario
            descargas = new File(userHome);
        }

        return new File(descargas, nombreArchivo);
    }

    /**
     * Método estático para abrir archivos de forma compatible con Linux/Windows
     */
    public static void abrirArchivo(String ruta) {
        try {
            File archivo = new File(ruta);
            if (!archivo.exists()) return;

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Desktop.getDesktop().open(archivo);
            } else if (os.contains("nux") || os.contains("nix")) {
                // Comando universal para Linux (Gnome, XFCE, KDE)
                new ProcessBuilder("xdg-open", ruta).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", ruta).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
