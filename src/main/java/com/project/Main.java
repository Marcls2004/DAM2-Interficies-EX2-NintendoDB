package com.project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.json.JSONObject;

public class Main extends Application {

    final int WINDOW_WIDTH = 600;
    final int WINDOW_HEIGHT = 400;
    final int MIN_WIDTH = 350; 
    final int MIN_HEIGHT = 500;

    private static final double PRAGMA_WIDTH = 600;
    private String vistaActual = "";
    private Stage stage;

    // Variables globales en memoria para recordar la última posición del usuario
    private static String categoriaGuardada = "Personatges";
    private static JSONObject elementoGuardado = null;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
        
        // Precarga obligatoria de las 4 vistas del sistema
        UtilsViews.addView(getClass(), "Desktop", "/assets/layout.fxml");
        UtilsViews.addView(getClass(), "MobileMenu", "/assets/vista_mobil_menu.fxml");
        UtilsViews.addView(getClass(), "MobileList", "/assets/vista_mobil.fxml");
        UtilsViews.addView(getClass(), "MobileDetail", "/assets/vista_mobil_detall.fxml");

        actualitzarVistaDinamica();
        stage.widthProperty().addListener((obs, oldVal, newVal) -> actualitzarVistaDinamica());

        Scene scene = new Scene(UtilsViews.parentContainer);
        stage.setScene(scene);
        stage.setTitle("Nintendo DB");
        stage.setMinWidth(MIN_WIDTH);
        stage.setWidth(WINDOW_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setHeight(WINDOW_HEIGHT);
        stage.show();
    }

    /**
     * Sincroniza el estado de las pantallas en tiempo real al estirar o encoger la ventana
     */
    private void actualitzarVistaDinamica() {
        double width = stage.getWidth();
        
        // MODO MÓVIL (Ventana pequeña)
        if (Double.isNaN(width) || width < PRAGMA_WIDTH) {
            if (!vistaActual.startsWith("Mobile")) {
                
                // 1. Antes de destruir la vista Desktop, le preguntamos qué estaba mirando el usuario
                Controller ctrlDesktop = (Controller) UtilsViews.getController("Desktop");
                if (ctrlDesktop != null) {
                    elementoGuardado = ctrlDesktop.obtenirElementSeleccionatEscriptori();
                    categoriaGuardada = ctrlDesktop.obtenirCategoriaActualEscriptori();
                }

                // 2. Decidimos a qué subpantalla móvil saltar de forma inteligente
                if (elementoGuardado != null) {
                    // Si estaba viendo los detalles de un personaje/juego, vamos directo al detalle móvil
                    Controller ctrlDetail = (Controller) UtilsViews.getController("MobileDetail");
                    if (ctrlDetail != null) ctrlDetail.carregarDetallMobilFisic(elementoGuardado);
                    
                    vistaActual = "MobileDetail";
                    UtilsViews.setView("MobileDetail");
                } else {
                    // Si no había nada seleccionado, va al menú principal móvil
                    vistaActual = "MobileMenu";
                    UtilsViews.setView("MobileMenu");
                }
            }
        } 
        // MODO ESCRITORIO (Ventana grande)
        else {
            if (!vistaActual.equals("Desktop")) {
                vistaActual = "Desktop";
                UtilsViews.setView("Desktop");

                // Recuperamos de forma inmediata el elemento donde se quedó el usuario en el móvil
                if (elementoGuardado != null) {
                    Controller ctrlDesktop = (Controller) UtilsViews.getController("Desktop");
                    if (ctrlDesktop != null) {
                        ctrlDesktop.seleccionarElementEnEscriptori(categoriaGuardada, elementoGuardado.optString("name", ""));
                    }
                }
            }
        }
    }

    /**
     * Método puente para que el controlador móvil avise al Main de qué elemento se ha pulsado
     */
    public static void registrarElementoActivoMobil(String categoria, JSONObject obj) {
        categoriaGuardada = categoria;
        elementoGuardado = obj;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
