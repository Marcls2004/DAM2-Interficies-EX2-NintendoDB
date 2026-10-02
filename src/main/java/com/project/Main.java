package com.project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    final int WINDOW_WIDTH = 600;
    final int WINDOW_HEIGHT = 400;
    
    // Quitamos los límites mínimos estrictos para que te deje encoger la ventana a tamaño móvil
    final int MIN_WIDTH = 350; 
    final int MIN_HEIGHT = 500;

    private static final double PRAGMA_WIDTH = 600;
    private String vistaActual = "";
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        // Configuración inicial del contenedor de tu plantilla
        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
        
        // Primera carga de la vista según el tamaño inicial
        actualitzarVistaDinamica();

        // Escuchador dinámico: Cambia entre móvil y escritorio en tiempo real al estirar la ventana
        stage.widthProperty().addListener((obs, oldVal, newVal) -> actualitzarVistaDinamica());

        Scene scene = new Scene(UtilsViews.parentContainer);
        stage.setScene(scene);
        stage.setTitle("Nintendo DB");
        stage.setMinWidth(MIN_WIDTH);
        stage.setWidth(WINDOW_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setHeight(WINDOW_HEIGHT);
        stage.show();

        // Añade el icono si no es Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            try {
                Image icon = new Image("file:icons/icon.png");
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("No se pudo cargar el icono del sistema.");
            }
        }
    }

    /**
     * Gestiona qué FXML inyectar en el contenedor compartido de tu plantilla
     */
    private void actualitzarVistaDinamica() {
        double width = stage.getWidth();
        String layoutNecesario;

        if (Double.isNaN(width) || width < PRAGMA_WIDTH) {
            layoutNecesario = "Mobile";
        } else {
            layoutNecesario = "Desktop";
        }

        // Evitamos recargar el archivo FXML cíclicamente si ya estamos en esa vista
        if (!vistaActual.equals(layoutNecesario)) {
            vistaActual = layoutNecesario;
            try {
                // Limpiamos las vistas previas del contenedor antes de meter la nueva
                UtilsViews.parentContainer.getChildren().clear();

                if (layoutNecesario.equals("Desktop")) {
                    // Carga el archivo modificado layout.fxml (Escritorio)
                    UtilsViews.addView(getClass(), "Desktop", "/assets/layout.fxml");
                } else {
                    // Carga tu vista_mobil.fxml
                    UtilsViews.addView(getClass(), "Mobile", "/assets/vista_mobil.fxml");
                }
            } catch (Exception e) {
                System.err.println("Error al conmutar vistas en UtilsViews: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
