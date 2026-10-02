package com.project;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.InputStream;

public class Controller {

    // ESCRITORIO
    @FXML private ComboBox<String> selectorCategoria;
    @FXML private ListView<HBox> llistaLateral;
    @FXML private VBox zonaDetall;

    // MÓVIL PANTALLA 1: MENÚ
    @FXML private ListView<String> llistaCategoriesMobil;

    // MÓVIL PANTALLA 2: LISTA
    @FXML private Button btnBack;
    @FXML private ListView<HBox> llistaElements;

    // MÓVIL PANTALLA 3: DETALLE
    @FXML private Button btnBackDetall;
    @FXML private Label lblTitolDetallMobil;
    @FXML private VBox zonaDetallMobil;

    private static JSONArray dataPersonatges;
    private static JSONArray dataJocs;
    private static JSONArray dataConsoles;
    private static String categoriaActualMobil = "Personatges";

    @FXML
    public void initialize() {
        carregarDadesJSON();

        // 1. Configuración Escritorio
        if (selectorCategoria != null && llistaLateral != null && zonaDetall != null) {
            selectorCategoria.getItems().clear();
            selectorCategoria.getItems().addAll("Personatges", "Jocs", "Consoles");
            selectorCategoria.getSelectionModel().selectFirst();
            selectorCategoria.setOnAction(e -> actualitzarLlistaEscriptori(selectorCategoria.getValue()));
            actualitzarLlistaEscriptori("Personatges");
        }

        // 2. Configuración Pantalla 1 Móvil: Menú de Categorías
        if (llistaCategoriesMobil != null) {
            llistaCategoriesMobil.getItems().clear();
            llistaCategoriesMobil.getItems().addAll("Personatges", "Jocs", "Consoles");
            llistaCategoriesMobil.setOnMouseClicked(e -> {
                String seleccionada = llistaCategoriesMobil.getSelectionModel().getSelectedItem();
                if (seleccionada != null) {
                    categoriaActualMobil = seleccionada;
                    // Buscamos el controlador de la pantalla de la lista para forzar su refresco
                    Controller ctrlLista = (Controller) UtilsViews.getController("MobileList");
                    if (ctrlLista != null) ctrlLista.actualitzarLlistaMobil(seleccionada);
                    UtilsViews.setView("MobileList");
                }
            });
        }

        // 3. Configuración Pantalla 2 Móvil: Lista de Elementos
        if (btnBack != null && llistaElements != null) {
            btnBack.setOnAction(e -> UtilsViews.setView("MobileMenu"));
        }

        // 4. Configuración Pantalla 3 Móvil: Detalle del Elemento
        if (btnBackDetall != null) {
            btnBackDetall.setOnAction(e -> UtilsViews.setView("MobileList"));
        }
    }

    private void carregarDadesJSON() {
        if (dataPersonatges != null) return;
        try (InputStream is = getClass().getResourceAsStream("/assets/characters.json")) {
            if (is != null) dataPersonatges = new JSONArray(new JSONTokener(is));
        } catch (Exception e) { System.err.println("Error characters.json"); }
        try (InputStream is = getClass().getResourceAsStream("/assets/games.json")) {
            if (is != null) dataJocs = new JSONArray(new JSONTokener(is));
        } catch (Exception e) { System.err.println("Error games.json"); }
        try (InputStream is = getClass().getResourceAsStream("/assets/consoles.json")) {
            if (is != null) dataConsoles = new JSONArray(new JSONTokener(is));
        } catch (Exception e) { System.err.println("Error consoles.json"); }
    }

    private JSONArray obtenirArrayPerCategoria(String categoria) {
        if (categoria == null) return new JSONArray();
        switch (categoria.toLowerCase()) {
            case "personatges": return dataPersonatges != null ? dataPersonatges : new JSONArray();
            case "jocs": return dataJocs != null ? dataJocs : new JSONArray();
            case "consoles": return dataConsoles != null ? dataConsoles : new JSONArray();
            default: return new JSONArray();
        }
    }

    private void actualitzarLlistaEscriptori(String categoria) {
        if (llistaLateral == null || zonaDetall == null) return;
        llistaLateral.getItems().clear();
        zonaDetall.getChildren().clear();

        JSONArray array = obtenirArrayPerCategoria(categoria);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            HBox fila = new HBox(10);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getChildren().addAll(obtenirImatge(obj.optString("image", ""), 30, true), new Label(obj.optString("name", "")));
            llistaLateral.getItems().add(fila);
            fila.setOnMouseClicked(e -> carregarDetallEscriptori(obj, zonaDetall));
        }
        if (!llistaLateral.getItems().isEmpty()) {
            carregarDetallEscriptori(array.getJSONObject(0), zonaDetall);
            llistaLateral.getSelectionModel().select(0);
        }
    }

        public void actualitzarLlistaMobil(String categoria) {
        if (llistaElements == null) return;
        llistaElements.getItems().clear();

        JSONArray array = obtenirArrayPerCategoria(categoria);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            HBox fila = new HBox(15);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));
            fila.getChildren().addAll(obtenirImatge(obj.optString("image", ""), 40, true), new Label(obj.optString("name", "")));
            llistaElements.getItems().add(fila);
            
            fila.setOnMouseClicked(e -> {
                // --- REGISTRO DE MEMORIA ANTES DEL CAMBIO ---
                Main.registrarElementoActivoMobil(categoria, obj);

                Controller ctrlDetall = (Controller) UtilsViews.getController("MobileDetail");
                if (ctrlDetall != null) {
                    ctrlDetall.carregarDetallMobilFisic(obj);
                }
                UtilsViews.setView("MobileDetail");
            });
        }
    }


    public void carregarDetallMobilFisic(JSONObject obj) {
        if (zonaDetallMobil == null || lblTitolDetallMobil == null) return;
        lblTitolDetallMobil.setText(obj.optString("name", "Detall"));
        carregarDetallEscriptori(obj, zonaDetallMobil);
    }

    private void carregarDetallEscriptori(JSONObject obj, VBox panelDestino) {
        if (panelDestino == null || obj == null) return;
        panelDestino.getChildren().clear();
        panelDestino.setAlignment(Pos.CENTER);

        ImageView imgGran = obtenirImatge(obj.optString("image", ""), 180, false);
        Label nom = new Label(obj.optString("name", ""));
        nom.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        VBox contenedorInfo = new VBox(10);
        contenedorInfo.setAlignment(Pos.CENTER);

        if (obj.has("game")) {
            Label lbl = new Label("Joc Principal: " + obj.getString("game"));
            lbl.setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold; -fx-font-size: 16px;");
            contenedorInfo.getChildren().add(lbl);
        }
        if (obj.has("type")) {
            Label lbl = new Label("Gènere: " + obj.getString("type"));
            lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2980B9;");
            contenedorInfo.getChildren().add(lbl);
        }
        if (obj.has("year")) {
            contenedorInfo.getChildren().add(new Label("Any de llançament: " + obj.get("year").toString()));
        }
        if (obj.has("plot")) {
            Label lbl = new Label(obj.getString("plot"));
            lbl.setWrapText(true);
            lbl.setMaxWidth(340);
            lbl.setStyle("-fx-font-size: 14px; -fx-text-alignment: center; -fx-text-fill: #34495E;");
            contenedorInfo.getChildren().add(lbl);
        }
        if (obj.has("procesador")) {
            contenedorInfo.getChildren().add(new Label("Procesador: " + obj.getString("procesador")));
        }
        if (obj.has("date")) {
            contenedorInfo.getChildren().add(new Label("Llançament: " + obj.getString("date")));
        }
        if (obj.has("units_sold")) {
            Label lbl = new Label("Unitats venudes: " + String.format("%,d", obj.optLong("units_sold", 0)));
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #27AE60;");
            contenedorInfo.getChildren().add(lbl);
        }
        if (obj.has("color")) {
            HBox filaColor = new HBox(8);
            filaColor.setAlignment(Pos.CENTER);
            Rectangle cuadradito = new Rectangle(14, 14);
            cuadradito.setArcWidth(3); cuadradito.setArcHeight(3);
            cuadradito.setStyle("-fx-fill: " + obj.getString("color").toLowerCase() + "; -fx-stroke: #BDC3C7;");
            Label lblColor = new Label("Color: " + obj.getString("color"));
            lblColor.setStyle("-fx-font-style: italic; -fx-text-fill: #5D6D7E;");
            filaColor.getChildren().addAll(cuadradito, lblColor);
            contenedorInfo.getChildren().add(filaColor);
        }

        panelDestino.getChildren().addAll(imgGran, nom, contenedorInfo);
    }

    private ImageView obtenirImatge(String nomImatge, double mida, boolean quadrada) {
        ImageView iv = new ImageView();
        iv.setFitHeight(mida);
        if (quadrada) iv.setFitWidth(mida); else iv.setPreserveRatio(true);
        if (nomImatge == null || nomImatge.isEmpty()) return iv;
        try {
            InputStream is = getClass().getResourceAsStream("/assets/images/" + nomImatge);
            if (is != null) iv.setImage(new Image(is));
        } catch (Exception e) { /* Silencioso */ }
        return iv;
    }

        /**
     * Permite saber qué elemento está seleccionado actualmente en la lista de escritorio
     */
    public JSONObject obtenirElementSeleccionatEscriptori() {
        if (llistaLateral == null || selectorCategoria == null) return null;
        int index = llistaLateral.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            JSONArray array = obtenirArrayPerCategoria(selectorCategoria.getValue());
            return array.optJSONObject(index);
        }
        return null;
    }

    /**
     * Permite saber qué categoría está activa en el ComboBox de escritorio
     */
    public String obtenirCategoriaActualEscriptori() {
        return selectorCategoria != null ? selectorCategoria.getValue() : "Personatges";
    }

    /**
     * Permite forzar la selección de un elemento concreto en la lista lateral
     */
    public void seleccionarElementEnEscriptori(String categoria, String nombreElemento) {
        if (selectorCategoria == null || llistaLateral == null) return;
        selectorCategoria.getSelectionModel().select(categoria);
        actualitzarLlistaEscriptori(categoria);
        
        JSONArray array = obtenirArrayPerCategoria(categoria);
        for (int i = 0; i < array.length(); i++) {
            if (array.getJSONObject(i).optString("name", "").equals(nombreElemento)) {
                llistaLateral.getSelectionModel().select(i);
                carregarDetallEscriptori(array.getJSONObject(i), zonaDetall);
                break;
            }
        }
    }
    
}