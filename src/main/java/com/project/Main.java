package com.project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    final int WINDOW_WIDTH = 600;
    final int WINDOW_HEIGHT = 400;
    final int MIN_WIDTH = 350; 
    final int MIN_HEIGHT = 500;

    private static final double PRAGMA_WIDTH = 600;
    private String vistaActual = "";
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        // Configuración estética inicial de tu plantilla
        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
        
        // PRECARGA OBLIGATORIA: Añadimos las dos vistas a la lista en memoria de tu plantilla
        UtilsViews.addView(getClass(), "Desktop", "/assets/layout.fxml");
        UtilsViews.addView(getClass(), "Mobile", "/assets/vista_mobil.fxml");

        // Evaluamos el tamaño inicial de la pantalla para activar la vista correcta
        actualitzarVistaDinamica();

        // Listener dinámico: Conmuta entre las pantallas precargadas al estirar la ventana
        stage.widthProperty().addListener((obs, oldVal, newVal) -> actualitzarVistaDinamica());

        Scene scene = new Scene(UtilsViews.parentContainer);
        stage.setScene(scene);
        stage.setTitle("Nintendo DB");
        stage.setMinWidth(MIN_WIDTH);
        stage.setWidth(WINDOW_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setHeight(WINDOW_HEIGHT);
        stage.show();

        if (!System.getProperty("os.name").contains("Mac")) {
            try {
                Image icon = new Image("file:icons/icon.png");
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("No se pudo cargar el icono.");
            }
        }
    }

    /**
     * Activa una vista u otra usando el sistema nativo de tu plantilla (setView)
     */
    private void actualitzarVistaDinamica() {
        double width = stage.getWidth();
        String layoutNecesario = (Double.isNaN(width) || width < PRAGMA_WIDTH) ? "Mobile" : "Desktop";

        if (!vistaActual.equals(layoutNecesario)) {
            vistaActual = layoutNecesario;
            
            // Usamos el método oficial de tu plantilla para cambiar de pantalla de forma segura
            UtilsViews.setView(layoutNecesario);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
