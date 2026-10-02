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

    // Componentes de la vista de ESCRITORIO (layout.fxml)
    @FXML private ComboBox<String> selectorCategoria;
    @FXML private ListView<HBox> llistaLateral;
    @FXML private VBox zonaDetall;

    // Componentes de la vista MÓVIL (vista_mobil.fxml)
    @FXML private Button btnBack;
    @FXML private ListView<HBox> llistaElements;

    // Estructuras de datos directas para tus listas JSON
    private static JSONArray dataPersonatges;
    private static JSONArray dataJocs;
    private static JSONArray dataConsoles;
    private static String categoriaActualMobil = "Personatges";

    @FXML
    public void initialize() {
        carregarDadesJSON();

        // Configuración para el entorno de ESCRITORIO
        if (selectorCategoria != null && llistaLateral != null && zonaDetall != null) {
            selectorCategoria.getItems().clear();
            selectorCategoria.getItems().addAll("Personatges", "Jocs", "Consoles");
            
            // Forzamos la selección inicial limpia
            selectorCategoria.getSelectionModel().selectFirst();
            
            // Vinculamos el cambio de categoría del ComboBox
            selectorCategoria.setOnAction(e -> {
                String seleccionada = selectorCategoria.getSelectionModel().getSelectedItem();
                if (seleccionada != null) {
                    actualitzarLlistaEscriptori(seleccionada);
                }
            });
            
            // Renderizado inicial por defecto
            actualitzarLlistaEscriptori("Personatges");
        }

        // Configuración para el entorno MÓVIL
        if (llistaElements != null) {
            actualitzarLlistaMobil(categoriaActualMobil);
        }
    }

    private void carregarDadesJSON() {
        if (dataPersonatges != null) return; // Evitamos lecturas duplicadas en memoria
        
        try (InputStream is = getClass().getResourceAsStream("/assets/characters.json")) {
            if (is != null) dataPersonatges = new JSONArray(new JSONTokener(is));
        } catch (Exception e) { System.err.println("Error cargando characters.json"); }

        try (InputStream is = getClass().getResourceAsStream("/assets/games.json")) {
            if (is != null) dataJocs = new JSONArray(new JSONTokener(is));
        } catch (Exception e) { System.err.println("Error cargando games.json"); }

        try (InputStream is = getClass().getResourceAsStream("/assets/consoles.json")) {
            if (is != null) dataConsoles = new JSONArray(new JSONTokener(is));
        } catch (Exception e) { System.err.println("Error cargando consoles.json"); }
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
            try {
                JSONObject obj = array.getJSONObject(i);
                HBox fila = new HBox(10);
                fila.setAlignment(Pos.CENTER_LEFT);

                ImageView img = obtenirImatge(obj.optString("image", ""), 30, true);
                Label nom = new Label(obj.optString("name", "Sense Nom"));
                
                fila.getChildren().addAll(img, nom);
                llistaLateral.getItems().add(fila);

                // Evento click para rellenar la ficha detallada
                fila.setOnMouseClicked(e -> carregarDetallEscriptori(obj));
            } catch (Exception e) {
                System.err.println("Error procesando fila " + i + " de " + categoria);
            }
        }
        
        // Autoseleccionamos el primer elemento disponible de la lista al cambiar de pestaña
        if (!llistaLateral.getItems().isEmpty()) {
            try {
                carregarDetallEscriptori(array.getJSONObject(0));
                llistaLateral.getSelectionModel().select(0);
            } catch (Exception e) {
                // Controlado
            }
        }
    }

    private void carregarDetallEscriptori(JSONObject obj) {
        if (zonaDetall == null || obj == null) return;
        zonaDetall.getChildren().clear();

        // Forzamos el centrado vertical y horizontal absoluto
        zonaDetall.setAlignment(Pos.CENTER);

        // 1. Imagen grande e Identificador
        ImageView imgGran = obtenirImatge(obj.optString("image", ""), 180, false);
        Label nom = new Label(obj.optString("name", ""));
        nom.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-text-alignment: center;");

        // 2. Contenedor de datos específicos centrado
        VBox contenedorInfo = new VBox(10);
        contenedorInfo.setAlignment(Pos.CENTER);

        // --- MAPEO DE PERSONAJES ---
        if (obj.has("game")) {
            Label lblJuego = new Label("Joc Principal: " + obj.getString("game"));
            lblJuego.setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold; -fx-font-size: 16px; -fx-text-alignment: center;");
            contenedorInfo.getChildren().add(lblJuego);
        }

        // --- MAPEO DE VIDEOJUEGOS (Texto Adaptativo Dinámico) ---
        if (obj.has("type")) {
            Label lblTipo = new Label("Gènere: " + obj.getString("type"));
            lblTipo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2980B9; -fx-text-alignment: center;");
            contenedorInfo.getChildren().add(lblTipo);
        }
        if (obj.has("year")) {
            Label lblAny = new Label("Any de llançament: " + obj.get("year").toString());
            lblAny.setStyle("-fx-font-size: 14px; -fx-text-fill: #7F8C8D; -fx-text-alignment: center;");
            contenedorInfo.getChildren().add(lblAny);
        }
        if (obj.has("plot")) {
            Label lblArgumento = new Label(obj.getString("plot"));
            lblArgumento.setWrapText(true); // Permitimos saltos de línea automáticos
            lblArgumento.setStyle("-fx-font-size: 14px; -fx-text-alignment: center; -fx-text-fill: #34495E;");
            
            // --- AQUÍ ESTÁ EL TRUCO ADAPTATIVO ---
            // Vinculamos el ancho máximo del texto al ancho real del panel de detalles menos un margen de seguridad (60px)
            lblArgumento.maxWidthProperty().bind(zonaDetall.widthProperty().subtract(60));
            
            contenedorInfo.getChildren().add(lblArgumento);
        }
        
        // --- MAPEO DE CONSOLAS ---
        if (obj.has("procesador")) {
            Label lblProc = new Label("Procesador: " + obj.getString("procesador"));
            lblProc.setStyle("-fx-font-size: 14px; -fx-text-fill: #7F8C8D; -fx-text-alignment: center;");
            contenedorInfo.getChildren().add(lblProc);
        }
        if (obj.has("date")) {
            Label lblFecha = new Label("Llançament: " + obj.getString("date"));
            lblFecha.setStyle("-fx-font-size: 14px; -fx-text-fill: #7F8C8D; -fx-text-alignment: center;");
            contenedorInfo.getChildren().add(lblFecha);
        }
        if (obj.has("units_sold")) {
            long unidades = obj.optLong("units_sold", 0);
            Label lblUnidades = new Label("Unitats venudes: " + String.format("%,d", unidades));
            lblUnidades.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27AE60; -fx-text-alignment: center;");
            contenedorInfo.getChildren().add(lblUnidades);
        }

        // --- FILA DINÁMICA DEL COLOR CON EL CUADRADO ---
        if (obj.has("color")) {
            String colorTxt = obj.getString("color");
            HBox filaColor = new HBox(8);
            filaColor.setAlignment(Pos.CENTER);

            javafx.scene.shape.Rectangle cuadradito = new javafx.scene.shape.Rectangle(14, 14);
            cuadradito.setArcWidth(3);
            cuadradito.setArcHeight(3);
            cuadradito.setStyle("-fx-fill: " + colorTxt.toLowerCase() + "; -fx-stroke: #BDC3C7; -fx-stroke-width: 1px;");

            Label lblColor = new Label("Color: " + colorTxt);
            lblColor.setStyle("-fx-font-size: 14px; -fx-text-fill: #5D6D7E; -fx-font-style: italic;");

            filaColor.getChildren().addAll(cuadradito, lblColor);
            contenedorInfo.getChildren().add(filaColor);
        }

        zonaDetall.getChildren().addAll(imgGran, nom, contenedorInfo);
    }


    public void actualitzarLlistaMobil(String categoria) {
    if (llistaElements == null) return;
    llistaElements.getItems().clear();

    JSONArray array = obtenirArrayPerCategoria(categoria);
    for (int i = 0; i < array.length(); i++) {
        JSONObject obj = array.getJSONObject(i);
        HBox fila = new HBox(15);
        fila.setAlignment(Pos.CENTER_LEFT);
        
        // --- AQUÍ ESTÁ EL AJUSTE MÓVIL ---
        // Añadimos relleno interior a la fila para que las celdas se vean proporcionales y limpias
        fila.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));

        ImageView imgView = obtenirImatge(obj.optString("image", ""), 40, true);
        Label nom = new Label(obj.optString("name", ""));
        nom.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        
        fila.getChildren().addAll(imgView, nom);
        llistaElements.getItems().add(fila);
    }
}

    private ImageView obtenirImatge(String nomImatge, double mida, boolean quadrada) {
        ImageView iv = new ImageView();
        iv.setFitHeight(mida);
        if (quadrada) iv.setFitWidth(mida); else iv.setPreserveRatio(true);
        if (nomImatge == null || nomImatge.isEmpty()) return iv;
        try {
            InputStream is = getClass().getResourceAsStream("/assets/images/" + nomImatge);
            if (is != null) {
                iv.setImage(new Image(is));
            }
        } catch (Exception e) {
        // Silencioso
        }
        return iv;
    }
}