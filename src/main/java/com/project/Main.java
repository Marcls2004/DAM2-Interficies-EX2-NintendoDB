package com.project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.json.JSONObject;

/**
 * Classe principal que arranca l'aplicació JavaFX.
 * Es responsabilitza de vigilar la mida de la finestra i sincronitzar l'estat 
 * entre les vistes de mòbil i escriptori de forma bidireccional.
 */
public class Main extends Application {

    // Amplada i alçada inicials per defecte en obrir l'aplicació
    final int WINDOW_WIDTH = 600;
    final int WINDOW_HEIGHT = 400;
    
    // Mides mínimes permeses (evita que la finestra s'encongeixi fins a desaparèixer)
    final int MIN_WIDTH = 350; 
    final int MIN_HEIGHT = 500;

    // Amplada límit (en píxeles) per canviar dinàmicament entre disseny mòbil i escriptori
    private static final double PRAGMA_WIDTH = 600;
    
    // Guarda el nom o identificador de la pantalla activa en cada moment
    private String vistaActual = "";
    private Stage stage;

    // VARIABLES GLOBALS DE MEMÒRIA: Recorden on es trobava l'usuari abans de canviar la mida
    private static String categoriaGuardada = "Personatges";
    private static JSONObject elementoGuardado = null;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        
        // Aplica una tipografia i mida neta per defecte a tot el contenidor de la plantilla
        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
        
        // PRECARGA OBLIGATORIA: Registrem els 4 fitxers FXML en la memòria de l'estructura estàtica
        UtilsViews.addView(getClass(), "Desktop", "/assets/layout.fxml");
        UtilsViews.addView(getClass(), "MobileMenu", "/assets/vista_mobil_menu.fxml");
        UtilsViews.addView(getClass(), "MobileList", "/assets/vista_mobil.fxml");
        UtilsViews.addView(getClass(), "MobileDetail", "/assets/vista_mobil_detall.fxml");

        // Evaluem la mida de la finestra al mil·lisegon zero per carregar el layout correcte d'inici
        actualitzarVistaDinamica();
        
        // OYENTE ELÀSTIC: Escolta en temps real quan l'usuari estira o encongeix la finestra de Windows
        stage.widthProperty().addListener((obs, oldVal, newVal) -> actualitzarVistaDinamica());

        // Construcció de l'escena i muntatge del StackPane principal compartit
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
     * Commuta les pantalles de forma reactiva i sincronitza els detalls guardats del catàleg
     */
    private void actualitzarVistaDinamica() {
        double width = stage.getWidth();
        
        // ========================================================
        // CAS A: COMPORTAMENT MÒBIL (Finestra petita / estreta)
        // ========================================================
        if (Double.isNaN(width) || width < PRAGMA_WIDTH) {
            // Només actuem si veníem del disseny d'escriptori de pantalla gran
            if (!vistaActual.startsWith("Mobile")) {
                
                // 1. Interroguem el controlador d'escriptori per saber què mirava l'usuari abans de tancar
                Controller ctrlDesktop = (Controller) UtilsViews.getController("Desktop");
                if (ctrlDesktop != null) {
                    elementoGuardado = ctrlDesktop.obtenirElementSeleccionatEscriptori();
                    categoriaGuardada = ctrlDesktop.obtenirCategoriaActualEscriptori();
                }

                // 2. Navegació intel·ligent: Si hi havia un element triat, saltem directament al detall mòbil
                if (elementoGuardado != null) {
                    Controller ctrlDetail = (Controller) UtilsViews.getController("MobileDetail");
                    if (ctrlDetail != null) ctrlDetail.carregarDetallMobilFisic(elementoGuardado);
                    
                    vistaActual = "MobileDetail";
                    UtilsViews.setView("MobileDetail");
                } else {
                    // Si la llista lateral estava buida, enviem l'usuari al menú mòbil de categories inicial
                    vistaActual = "MobileMenu";
                    UtilsViews.setView("MobileMenu");
                }
            }
        } 
        // ========================================================
        // CAS B: COMPORTAMENT ESCRIPTORI (Finestra gran / ampla)
        // ========================================================
        else {
            if (!vistaActual.equals("Desktop")) {
                vistaActual = "Desktop";
                UtilsViews.setView("Desktop");

                // Restaurem immediatament la posició de l'usuari obtinguda durant el mode mòbil
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
     * Mètode pont utilitzat pel controlador de llistes del mòbil per desar la selecció activa
     */
    public static void registrarElementoActivoMobil(String categoria, JSONObject obj) {
        categoriaGuardada = categoria;
        elementoGuardado = obj;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
