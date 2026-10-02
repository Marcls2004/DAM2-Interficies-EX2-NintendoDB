package com.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;

public class NintendoApp extends Application {

    private JSONObject dataPersonatges;
    private JSONObject dataJocs;
    private JSONObject dataConsoles;
    
    private Stage primaryStage;
    private Scene escenaPrincipal;
    private static final double PRAGMA_WIDTH = 600;
    private String vistaActual = "";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        carregarDadesJSON();

        // Inicializamos la interfaz escuchando activamente el ancho de la ventana
        actualitzarVista();
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> actualitzarVista());
        
        primaryStage.setTitle("Nintendo DB");
        primaryStage.show();
    }

    /**
     * Carga de forma independiente los 3 archivos JSON desde resources/assets
     */
    private void carregarDadesJSON() {
        try (InputStream is = getClass().getResourceAsStream("/assets/personatges.json")) {
            if (is != null) dataPersonatges = new JSONObject(new JSONTokener(is));
        } catch (Exception e) { System.err.println("Error al cargar personatges.json"); }

        try (InputStream is = getClass().getResourceAsStream("/assets/jocs.json")) {
            if (is != null) dataJocs = new JSONObject(new JSONTokener(is));
        } catch (Exception e) { System.err.println("Error al cargar jocs.json"); }

        try (InputStream is = getClass().getResourceAsStream("/assets/consoles.json")) {
            if (is != null) dataConsoles = new JSONObject(new JSONTokener(is));
        } catch (Exception e) { System.err.println("Error al cargar consoles.json"); }
    }

    /**
     * Facilita las listas de datos extraídas de los JSON al controlador
     */
    public JSONArray obtenirArrayPerCategoria(String categoria) {
        try {
            switch (categoria.toLowerCase()) {
                case "personatges":
                    return dataPersonatges != null ? dataPersonatges.getJSONArray("personatges") : new JSONArray();
                case "jocs":
                    return dataJocs != null ? dataJocs.getJSONArray("jocs") : new JSONArray();
                case "consoles":
                    return dataConsoles != null ? dataConsoles.getJSONArray("consoles") : new JSONArray();
                default:
                    return new JSONArray();
            }
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    /**
     * Conmuta dinámicamente entre la vista FXML de escritorio o el menú móvil
     */
    private void actualitzarVista() {
        double width = primaryStage.getWidth();
        
        if (Double.isNaN(width) || width < PRAGMA_WIDTH) {
            // Si la ventana es pequeña y no estábamos en modo móvil, cargamos el menú principal móvil
            if (!vistaActual.equals("mobil")) {
                vistaActual = "mobil";
                mostrarMenuPrincipalMobil();
            }
        } else {
            // Si la ventana es grande, cargamos el archivo FXML de escritorio diseñado en Scene Builder
            if (!vistaActual.equals("escriptori")) {
                vistaActual = "escriptori";
                cargarFXMLFichero("/assets/vista_escriptori.fxml", null);
            }
        }
    }

    /**
     * Carga un archivo FXML genérico inyectándole esta instancia de la app principal
     */
    private void cargarFXMLFichero(String rutaFXML, String categoriaMobil) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent raiz = loader.load();

            Controller controlador = loader.getController();
            if (controlador != null) {
                if (categoriaMobil != null) {
                    controlador.setCategoriaActualMobil(categoriaMobil);
                }
                controlador.setMainApp(this);
            }

            establecerRaizEscena(raiz);

        } catch (Exception e) {
            System.err.println("Error al cargar FXML: " + rutaFXML);
            e.printStackTrace();
        }
    }

    // =========================================================================
    // NAVEGACIÓN EXCLUSIVA PARA MÓVIL (Nativa / FXML combinados)
    // =========================================================================

    public void mostrarMenuPrincipalMobil() {
        VBox arrel = new VBox();
        
        Label títol = new Label("Nintendo DB");
        títol.setStyle("-fx-background-color: #7FB3D5; -fx-text-fill: black; -fx-font-size: 24px; -fx-alignment: center;");
        títol.setMaxWidth(Double.MAX_VALUE);
        títol.setPrefHeight(60);

        ListView<String> opcionsMenu = new ListView<>();
        opcionsMenu.getItems().addAll("Personatges", "Jocs", "Consoles");
        VBox.setVgrow(opcionsMenu, Priority.ALWAYS);

        opcionsMenu.getSelectionModel().selectedItemProperty().addListener((obs, vell, nou) -> {
            if (nou != null) {
                // Al hacer clic, carga el FXML móvil pasándole la categoría seleccionada
                cargarFXMLFichero("/assets/vista_mobil.fxml", nou);
            }
        });

        arrel.getChildren().addAll(títol, opcionsMenu);
        establecerRaizEscena(arrel);
    }

    public void mostrarDetallMobil(String categoria, JSONObject obj) {
        VBox arrel = new VBox(20);
        arrel.setAlignment(Pos.TOP_CENTER);

        HBox capçalera = new HBox(15);
        capçalera.setStyle("-fx-background-color: #7FB3D5; -fx-padding: 10px; -fx-alignment: center-left;");
        
        Button btnBack = new Button();
        ImageView imgBack = obtenirImatge("arrow-back.png", 20, true);
        if (imgBack.getImage() != null) {
            btnBack.setGraphic(imgBack);
            btnBack.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        } else {
            btnBack.setText("←");
        }
        btnBack.setOnAction(e -> cargarFXMLFichero("/assets/vista_mobil.fxml", categoria));

        Label lblTítol = new Label(obj.getString("nom"));
        lblTítol.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");
        capçalera.getChildren().addAll(btnBack, lblTítol);

        ImageView imgGran = obtenirImatge(obj.getString("imatge"), 200, false);
        Label nomElement = new Label(obj.getString("nom"));
        nomElement.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label detallAdicional = new Label();
        detallAdicional.setWrapText(true);
        detallAdicional.setStyle("-fx-font-size: 14px; -fx-padding: 0 20px 0 20px; -fx-text-alignment: center;");
        
        if (obj.has("joc")) {
            detallAdicional.setText(obj.getString("joc"));
            detallAdicional.setStyle("-fx-text-fill: orange; -fx-font-weight: bold; -fx-font-size: 16px;");
        } else if (obj.has("descripcio")) {
            detallAdicional.setText(obj.getString("descripcio"));
        }

        arrel.getChildren().addAll(capçalera, imgGran, nomElement, detallAdicional);
        establecerRaizEscena(arrel);
    }

    // =========================================================================
    // CONTROLADORES AUXILIARES
    // =========================================================================

    public ImageView obtenirImatge(String nomImatge, double mida, boolean quadrada) {
        ImageView iv = new ImageView();
        try {
            InputStream is = getClass().getResourceAsStream("/assets/images/" + nomImatge);
            if (is != null) {
                iv.setImage(new Image(is));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen: " + nomImatge);
        }
        iv.setFitHeight(mida);
        if (quadrada) iv.setFitWidth(mida); else iv.setPreserveRatio(true);
        return iv;
    }

    private void establecerRaizEscena(Parent nodeArrel) {
        if (escenaPrincipal == null) {
            escenaPrincipal = new Scene(nodeArrel, 380, 700);
            primaryStage.setScene(escenaPrincipal);
        } else {
            escenaPrincipal.setRoot(nodeArrel);
        }

        // Vinculación automática del archivo CSS de estilos
        try {
            escenaPrincipal.getStylesheets().clear();
            escenaPrincipal.getStylesheets().add(getClass().getResource("/assets/estils.css").toExternalForm());
        } catch (Exception e) {
            // Silencioso si no se encuentra el CSS todavía
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
