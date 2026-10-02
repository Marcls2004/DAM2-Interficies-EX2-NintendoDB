package com.project;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Controller {

    // Componentes de la vista de ESCRITORIO
    @FXML private ComboBox<String> selectorCategoria;
    @FXML private ListView<HBox> llistaLateral;
    @FXML private VBox zonaDetall;

    // Componentes de la vista MÓVIL
    @FXML private Button btnBack;
    @FXML private ListView<HBox> llistaElements;

    private NintendoApp mainApp;
    private String categoriaActualMobil = "Personatges";

    /**
     * Este método lo ejecuta JavaFX automáticamente al cargar un archivo FXML
     */
    @FXML
    public void initialize() {
        // Configuración si estamos en la vista de ESCRITORIO
        if (selectorCategoria != null && llistaLateral != null && zonaDetall != null) {
            selectorCategoria.getItems().addAll("Personatges", "Jocs", "Consoles");
            selectorCategoria.getSelectionModel().selectFirst();

            selectorCategoria.valueProperty().addListener((obs, vell, novaCategoria) -> {
                actualitzarLlistaEscriptori(novaCategoria);
            });
        }

        // Configuración si estamos en la vista MÓVIL
        if (btnBack != null && llistaElements != null) {
            btnBack.setOnAction(e -> {
                // Si tuviéramos un menú principal previo, volvería allí. 
                // Por ahora, recarga la lista base o limpia la pantalla.
                mainApp.mostrarMenuPrincipalMobil();
            });
        }
    }

    /**
     * Vincula la aplicación principal con este controlador para poder acceder a los JSON
     */
    public void setMainApp(NintendoApp mainApp) {
        this.mainApp = mainApp;
        
        // Carga inicial según la vista en la que nos encontremos
        if (selectorCategoria != null) {
            actualitzarLlistaEscriptori(selectorCategoria.getValue());
        } else if (llistaElements != null) {
            actualitzarLlistaMobil(categoriaActualMobil);
        }
    }

    // =========================================================================
    // LÓGICA PARA LA VISTA DE ESCRITORIO
    // =========================================================================
    
    private void actualitzarLlistaEscriptori(String categoria) {
        llistaLateral.getItems().clear();
        zonaDetall.getChildren().clear();

        JSONArray array = mainApp.obtenirArrayPerCategoria(categoria);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            HBox fila = new HBox(10);
            fila.setAlignment(Pos.CENTER_LEFT);

            ImageView img = mainApp.obtenirImatge(obj.getString("imatge"), 30, true);
            Label nom = new Label(obj.getString("nom"));
            
            fila.getChildren().addAll(img, nom);
            llistaLateral.getItems().add(fila);

            fila.setOnMouseClicked(e -> carregarDetallEscriptori(obj));
        }
        
        if (!llistaLateral.getItems().isEmpty()) {
            carregarDetallEscriptori(array.getJSONObject(0));
        }
    }

    private void carregarDetallEscriptori(JSONObject obj) {
        zonaDetall.getChildren().clear();

        ImageView imgGran = mainApp.obtenirImatge(obj.getString("imatge"), 250, false);
        Label nom = new Label(obj.getString("nom"));
        nom.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label desc = new Label();
        desc.setWrapText(true);
        desc.setMaxWidth(400);
        desc.setStyle("-fx-font-size: 16px; -fx-text-alignment: center;");

        if (obj.has("joc")) {
            desc.setText(obj.getString("joc"));
            desc.setStyle("-fx-text-fill: orange; -fx-font-weight: bold; -fx-font-size: 18px;");
        } else if (obj.has("descripcio")) {
            desc.setText(obj.getString("descripcio"));
        }

        zonaDetall.getChildren().addAll(imgGran, nom, desc);
    }

    // =========================================================================
    // LÓGICA PARA LA VISTA MÓVIL
    // =========================================================================
    
    public void setCategoriaActualMobil(String categoria) {
        this.categoriaActualMobil = categoria;
    }

    public void actualitzarLlistaMobil(String categoria) {
        if (llistaElements == null) return;
        llistaElements.getItems().clear();

        JSONArray array = mainApp.obtenirArrayPerCategoria(categoria);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            HBox fila = new HBox(15);
            fila.setAlignment(Pos.CENTER_LEFT);

            ImageView imgView = mainApp.obtenirImatge(obj.getString("imatge"), 40, true);
            Label nom = new Label(obj.getString("nom"));
            nom.setStyle("-fx-font-size: 16px;");
            
            fila.getChildren().addAll(imgView, nom);
            llistaElements.getItems().add(fila);
            
            // Al pulsar una fila en el móvil, abrimos la pantalla de detalle completo
            fila.setOnMouseClicked(e -> mainApp.mostrarDetallMobil(categoria, obj));
        }
    }
}
