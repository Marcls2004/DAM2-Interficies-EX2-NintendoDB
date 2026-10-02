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

/**
 * Classe controladora encarregada d'enllaçar els elements de la interfície FXML 
 * amb les llistes de dades JSON subministrades. Gestiona tant la vista d'escriptori com la mòbil.
 */
public class Controller {

    // COMPONENTS MAESTRES DE LA VISTA D'ESCRIPTORI (layout.fxml)
    @FXML private ComboBox<String> selectorCategoria;
    @FXML private ListView<HBox> llistaLateral;
    @FXML private VBox zonaDetall;

    // COMPONENTS DE LA PANTALLA 1 MÒBIL: MENÚ PRINCIPAL (vista_mobil_menu.fxml)
    @FXML private ListView<String> llistaCategoriesMobil;

    // COMPONENTS DE LA PANTALLA 2 MÒBIL: LLISTAT DE CATEGORIA (vista_mobil.fxml)
    @FXML private Button btnBack;
    @FXML private ListView<HBox> llistaElements;

    // COMPONENTS DE LA PANTALLA 3 MÒBIL: FITXA DE DETALL (vista_mobil_detall.fxml)
    @FXML private Button btnBackDetall;
    @FXML private Label lblTitolDetallMobil;
    @FXML private VBox zonaDetallMobil;

    // INSTÀNCIES ESTÀTIQUES EN MEMÒRIA: Emmagatzemen la informació parsejada dels JSON reals
    private static JSONArray dataPersonatges;
    private static JSONArray dataJocs;
    private static JSONArray dataConsoles;
    private static String categoriaActualMobil = "Personatges";

    /**
     * Mètode d'inicialització automàtica executat per JavaFX al carregar qualsevol fitxer FXML.
     */
    @FXML
    public void initialize() {
        // Carrega els fluxos JSON cap a la memòria cau del programa
        carregarDadesJSON();

        // 1. Configuració inicial exclusiva per al mode ESCRIPTORI
        if (selectorCategoria != null && llistaLateral != null && zonaDetall != null) {
            selectorCategoria.getItems().clear();
            selectorCategoria.getItems().addAll("Personatges", "Jocs", "Consoles");
            selectorCategoria.getSelectionModel().selectFirst();
            
            // Acció reactiva en canviar l'ítem triat al ComboBox desplegable
            selectorCategoria.setOnAction(e -> actualitzarLlistaEscriptori(selectorCategoria.getValue()));
            
            // Renderitzat inicial per defecte (Personatges)
            actualitzarLlistaEscriptori("Personatges");
        }

        // 2. Configuració per a la Pantalla 1 del Mòbil: Navegació inicial del Menú
        if (llistaCategoriesMobil != null) {
            llistaCategoriesMobil.getItems().clear();
            llistaCategoriesMobil.getItems().addAll("Personatges", "Jocs", "Consoles");
            
            // Al clicar una categoria, actualitza el llistat mòbil i canvia de finestra
            llistaCategoriesMobil.setOnMouseClicked(e -> {
                String seleccionada = llistaCategoriesMobil.getSelectionModel().getSelectedItem();
                if (seleccionada != null) {
                    categoriaActualMobil = seleccionada;
                    // Recuperem el controlador associat a la llista mòbil per forçar la injecció de celdes
                    Controller ctrlLista = (Controller) UtilsViews.getController("MobileList");
                    if (ctrlLista != null) ctrlLista.actualitzarLlistaMobil(seleccionada);
                    UtilsViews.setView("MobileList");
                }
            });
        }

        // 3. Configuració per a la Pantalla 2 del Mòbil: Llistat de fitxes compactes
        if (btnBack != null && llistaElements != null) {
            // El botó Back d'aquesta subpantalla recula cap al menú de categories inicial
            btnBack.setOnAction(e -> UtilsViews.setView("MobileMenu"));
        }

        // 4. Configuració per a la Pantalla 3 del Mòbil: Visualització de la Fitxa de detalls
        if (btnBackDetall != null) {
            // El botó Back del detall et retorna a la llista de la categoria corresponent
            btnBackDetall.setOnAction(e -> UtilsViews.setView("MobileList"));
        }
    }

    /**
     * Obre i llegeix els fluxos de text dels 3 fitxers JSON independents allotjats a resources/assets/
     */
    private void carregarDadesJSON() {
        if (dataPersonatges != null) return; // Evita re-lectures innecessàries del disc dur si ja estan en memòria
        
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

    /**
     * Mètode de conveniència que extreu la llista JSONArray adient segons el String passat
     */
    private JSONArray obtenirArrayPerCategoria(String categoria) {
        if (categoria == null) return new JSONArray();
        switch (categoria.toLowerCase()) {
            case "personatges": return dataPersonatges != null ? dataPersonatges : new JSONArray();
            case "jocs": return dataJocs != null ? dataJocs : new JSONArray();
            case "consoles": return dataConsoles != null ? dataConsoles : new JSONArray();
            default: return new JSONArray();
        }
    }

    /**
     * Neteja i reconstrueix la barra de navegació lateral de la interfície d'escriptori
     */
    private void actualitzarLlistaEscriptori(String categoria) {
        if (llistaLateral == null || zonaDetall == null) return;
        llistaLateral.getItems().clear();
        zonaDetall.getChildren().clear();

        JSONArray array = obtenirArrayPerCategoria(categoria);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            HBox fila = new HBox(10);
            fila.setAlignment(Pos.CENTER_LEFT);
            
            // Creem una fila horitzontal afegint la foto de 30px i el text descrit per la clau 'name'
            fila.getChildren().addAll(obtenirImatge(obj.optString("image", ""), 30, true), new Label(obj.optString("name", "")));
            llistaLateral.getItems().add(fila);
            
            // Escoltador d'esdeveniments click per dibuixar la informació a la dreta
            fila.setOnMouseClicked(e -> carregarDetallEscriptori(obj, zonaDetall));
        }
        
        // Autoselecciona automàticament el primer element del catàleg per no deixar el panell buit de sortida
        if (!llistaLateral.getItems().isEmpty()) {
            carregarDetallEscriptori(array.getJSONObject(0), zonaDetall);
            llistaLateral.getSelectionModel().select(0);
        }
    }

    /**
     * Neteja i repobla el llistat mòbil incorporant paddings d'estil mòbil natiu
     */
    public void actualitzarLlistaMobil(String categoria) {
        if (llistaElements == null) return;
        llistaElements.getItems().clear();

        JSONArray array = obtenirArrayPerCategoria(categoria);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            HBox fila = new HBox(15);
            fila.setAlignment(Pos.CENTER_LEFT);
            // Revestim la celda amb un marge interior (padding) de separació mòbil elegant
            fila.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));
            fila.getChildren().addAll(obtenirImatge(obj.optString("image", ""), 40, true), new Label(obj.optString("name", "")));
            llistaElements.getItems().add(fila);
            
            // Al fer clic, registrem la posició al Main per a la memòria adaptativa i canviem de finestra
            fila.setOnMouseClicked(e -> {
                Main.registrarElementoActivoMobil(categoria, obj);

                Controller ctrlDetall = (Controller) UtilsViews.getController("MobileDetail");
                if (ctrlDetall != null) {
                    ctrlDetall.carregarDetallMobilFisic(obj);
                }
                UtilsViews.setView("MobileDetail");
            });
        }
    }

    /**
     * Canvia dinàmicament el text del Label de la capçalera de la pantalla de detalls del mòbil
     */
    public void carregarDetallMobilFisic(JSONObject obj) {
        if (zonaDetallMobil == null || lblTitolDetallMobil == null) return;
        lblTitolDetallMobil.setText(obj.optString("name", "Detall"));
        carregarDetallEscriptori(obj, zonaDetallMobil);
    }

    /**
     * MÈTODE ARQUITECTÒNIC CENTRAL: Genera el panell d'informació detallat i centra absolutament tots els nodes apilats
     */
    private void carregarDetallEscriptori(JSONObject obj, VBox panelDestino) {
        if (panelDestino == null || obj == null) return;
        panelDestino.getChildren().clear();
        panelDestino.setAlignment(Pos.CENTER); // Centrat vertical i horitzontal absolut obligatori

        // 1. Il·lustració i títol del personatge/joc/consola passat
        ImageView imgGran = obtenirImatge(obj.optString("image", ""), 180, false);
        Label nom = new Label(obj.optString("name", ""));
        nom.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        // Contenidor vertical complementari on anirem apilant l'estat condicional de dades
        VBox contenedorInfo = new VBox(10);
        contenedorInfo.setAlignment(Pos.CENTER);

        // --- ATRIBUTS CONDICIONALS SEGONS DOMINI ---
        if (obj.has("game")) {
        // Clau específica del llistat de Personatges
        Label lbl = new Label("Joc Principal: " + obj.getString("game"));
        lbl.setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold; -fx-font-size: 16px;");
        contenedorInfo.getChildren().add(lbl);
        }
        if (obj.has("type")) {
        // Clau específica del llistat de Videojocs
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
        // Clau específica del llistat de Consoles
        contenedorInfo.getChildren().add(new Label("Procesador: " + obj.getString("procesador")));
        }
        if (obj.has("date")) {
        contenedorInfo.getChildren().add(new Label("Llançament: " + obj.getString("date")));
        }
        if (obj.has("units_sold")) {
        // Formateja de forma intel·ligent les xifres de vendes afegint puntuació numèrica de milers
        Label lbl = new Label("Unitats venudes: " + String.format("%,d", obj.optLong("units_sold", 0)));
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #27AE60;");
        contenedorInfo.getChildren().add(lbl);
        }
        // --- DIBUIX DEL CUADRADET DE COLOR DINÀMIC AL COSTAT DEL TEXT ---
        if (obj.has("color")) {
        HBox filaColor = new HBox(8);
        filaColor.setAlignment(Pos.CENTER);
        // Instanciem una forma Rectangles geomètrica de 14x14 píxeles a joc amb la font
        Rectangle cuadradito = new Rectangle(14, 14);
        cuadradito.setArcWidth(3);
        cuadradito.setArcHeight(3); // Suavitza les cantonades del quadrat (bordes arrodonits)
        // Li apliquem mitjançant CSS el color exacte extret en el registre del fitxer JSON
        cuadradito.setStyle("-fx-fill: " + obj.getString("color").toLowerCase() + "; -fx-stroke: #BDC3C7;");
        Label lblColor = new Label("Color: " + obj.getString("color"));
        lblColor.setStyle("-fx-font-style: italic; -fx-text-fill: #5D6D7E;");
        filaColor.getChildren().addAll(cuadradito, lblColor);
        contenedorInfo.getChildren().add(filaColor);
        }
        // Ajunta de forma endreçada la foto gran, el nom i les dades filtrades
        panelDestino.getChildren().addAll(imgGran, nom, contenedorInfo);
    }
    // =========================================================================
    // MÈTODES ACCESSORS OBLIGATORIS: CONTROL DE MEMÒRIA PER AL FILTRE DE FINESTRA DEL MAIN
    // =========================================================================
    public JSONObject obtenirElementSeleccionatEscriptori() {
        if (llistaLateral == null || selectorCategoria == null) return null;
        int index = llistaLateral.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            JSONArray array = obtenirArrayPerCategoria(selectorCategoria.getValue());
            return array.optJSONObject(index);
        }
        return null;
    }
    public String obtenirCategoriaActualEscriptori() {
        return selectorCategoria != null ? selectorCategoria.getValue() : "Personatges";
    }
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
    /**
    * Carrega de forma eficient els fitxers d'imatge des d'assets/images reflectits pel JSON
    */
    private ImageView obtenirImatge(String nomImatge, double mida, boolean quadrada) {
        ImageView iv = new ImageView();
        iv.setFitHeight(mida);
        if (quadrada) iv.setFitWidth(mida); else iv.setPreserveRatio(true);
        if (nomImatge == null || nomImatge.isEmpty()) return iv;
        try {
            InputStream is = getClass().getResourceAsStream("/assets/images/" + nomImatge);
            if (is != null) iv.setImage(new Image(is));
        } catch (Exception e) { /*  Ignorat de forma silenciosa per evitar congelacions gráficos */ }
        return iv;
    }
}