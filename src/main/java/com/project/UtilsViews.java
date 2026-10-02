package com.project;

import java.util.ArrayList;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Clase de utilidad encargada de gestionar el ciclo de vida, precarga y transiciones
 * animadas de las diferentes interfaces de usuario basadas en FXML dentro del proyecto.
 */
public class UtilsViews {

    // Contenedor gráfico principal (Pila) donde se superponen todas las vistas cargadas
    public static StackPane parentContainer = new StackPane();
    
    // Lista dinámica que almacena los controladores asociados a cada archivo FXML cargado
    public static ArrayList<Object> controllers = new ArrayList<>();

    /**
     * Carga un archivo FXML en memoria, le asigna un identificador único y lo añade al contenedor.
     * La primera vista añadida se establece automáticamente como la vista por defecto (visible).
     */
    public static void addView(Class<?> cls, String name, String path) throws Exception {
        
        boolean defaultView = false;
        
        // Inicializa el cargador de JavaFX pasándole la ruta del recurso local
        FXMLLoader loader = new FXMLLoader(cls.getResource(path));
        
        // Carga físicamente el árbol de componentes del FXML (el contenedor Pane)
        Pane view = loader.load();
        
        // Obtiene la lista oficial de hijos actualmente cargados en el StackPane raíz
        ObservableList<Node> children = parentContainer.getChildren();

        // Si es el primer elemento que se introduce en la app, será la pantalla inicial visible
        if (children.isEmpty()) {
            defaultView = true;
        }

        // Asigna el nombre de la vista como ID técnico del nodo para poder recuperarla luego
        view.setId(name);
        
        // Controla la visibilidad inicial de la pantalla
        view.setVisible(defaultView);
        view.setManaged(defaultView); // Si no es visible, el layout no le reserva espacio en pantalla

        // Inyecta el nodo visual en el StackPane central
        children.add(view);
        
        // Guarda de forma paralela la instancia de su controlador Java para poder consultarlo luego
        controllers.add(loader.getController());
    }

    /**
     * Busca y devuelve el controlador de una pantalla basándose en el ID de su vista.
     * Es crucial para poder comunicar datos entre pantallas independientes.
     */
    public static Object getController(String viewId) {
        int index = 0;
        // Recorre todos los nodos de la pila gráfica buscando la coincidencia del ID
        for (Node n : parentContainer.getChildren()) {
            if (n.getId().equals(viewId)) {
                // Si la vista coincide, retorna el controlador que ocupa la misma posición en la lista
                return controllers.get(index);
            }
            index++;
        }
        return null; // Retorna null si no se encuentra la vista especificada
    }

    /**
     * Devuelve el identificador único (ID) de la pantalla que se está mostrando actualmente.
     */
    public static String getActiveView() {
        for (Node n : parentContainer.getChildren()) {
            if (n.isVisible()) {
                return n.getId(); // Retorna el ID de la primera vista activa encontrada
            }
        }
        return null; // No hay ninguna vista activa en este momento
    }

    /**
     * Cambia de pantalla de forma instantánea sin animaciones (Modo Estático).
     * Muestra la vista solicitada y oculta y desgestiona de inmediato todas las demás.
     */
    public static void setView(String viewId) {

        ArrayList<Node> list = new ArrayList<>();
        // Crea una copia inmutable de la lista de nodos gráficos para poder operar sin interferencias
        list.addAll(parentContainer.getChildrenUnmodifiable());

        // Recorre la lista de pantallas: activa la solicitada y esconde el resto
        for (Node n : list) {
            if (n.getId().equals(viewId)) {
                n.setVisible(true);
                n.setManaged(true);
            } else {
                n.setVisible(false);
                n.setManaged(false);
            }
        }

        // Elimina el foco residual de los botones anteriores para evitar comportamientos extraños del teclado
        parentContainer.requestFocus();
    }

    /**
     * Cambia de pantalla utilizando un efecto de transición por deslizamiento horizontal (Modo Móvil).
     * Calcula de forma matemática si debe deslizar hacia la izquierda o derecha según la posición en la lista.
     */
    public static void setViewAnimating(String viewId) {

        ArrayList<Node> list = new ArrayList<>();
        list.addAll(parentContainer.getChildrenUnmodifiable());

        // 1. Localiza cuál es la vista actual en pantalla antes de iniciar la animación
        Node curView = null;
        for (Node n : list) {
            if (n.isVisible()) {
                curView = n;
            }
        }

        // Si el usuario intenta navegar a la misma pantalla donde ya está, aborta el proceso
        if (curView.getId().equals(viewId)) {
            return; 
        }

        // 2. Localiza cuál es el nodo de la pantalla de destino (nxtView)
        Node nxtView = null;
        for (Node n : list) {
            if (n.getId().equals(viewId)) {
                nxtView = n;
            }
        }

        // Activa la pantalla de destino para que JavaFX pueda empezar a pintarla de fondo durante la animación
        nxtView.setVisible(true);
        nxtView.setManaged(true);

        // Variables de posicionamiento de coordenadas X para el cálculo matemático del deslizamiento
        double width = parentContainer.getScene().getWidth(); // Captura el ancho actual de la ventana
        double xLeftStart = 0;
        double xLeftEnd = 0;
        double xRightStart = 0;
        double xRightEnd = 0;
        Node animatedViewLeft = null;
        Node animatedViewRight = null;

        // LÓGICA DE DIRECCIÓN: Determina el sentido según los índices del historial de la lista
        if (list.indexOf(curView) < list.indexOf(nxtView)) {

            // Caso A: Si avanzamos en el menú, la pantalla vieja va hacia la izquierda (-width) y la nueva entra desde la derecha
            xLeftStart = 0;
            xLeftEnd = -width;
            xRightStart = width;
            xRightEnd = 0;
            animatedViewLeft = curView;
            animatedViewRight = nxtView;

            // Posiciona los elementos en sus coordenadas de salida exactas
            curView.translateXProperty().set(xLeftStart);
            nxtView.translateXProperty().set(xRightStart);

        } else { 

            // Caso B: Si retrocedemos (Back), la pantalla nueva entra desde la izquierda (-width) y la vieja sale hacia la derecha
            xLeftStart = -width;
            xLeftEnd = 0;
            xRightStart = 0;
            xRightEnd = width;
            animatedViewLeft = nxtView;
            animatedViewRight = curView;

            curView.translateXProperty().set(xRightStart);
            nxtView.translateXProperty().set(xLeftStart);
        }

        // 3. EJECUCIÓN GRÁFICA DE LA ANIMACIÓN
        final double seconds = 0.4; // Duración exacta de la transición (0.4 segundos)
        
        // Define el desplazamiento del elemento que viaja hacia la izquierda con una aceleración suave (EASE_BOTH)
        KeyValue kvLeft = new KeyValue(animatedViewLeft.translateXProperty(), xLeftEnd, Interpolator.EASE_BOTH);
        KeyFrame kfLeft = new KeyFrame(Duration.seconds(seconds), kvLeft);
        Timeline timelineLeft = new Timeline();
        timelineLeft.getKeyFrames().add(kfLeft);
        timelineLeft.play(); // Ejecuta la animación del bloque izquierdo

        // Define el desplazamiento del elemento que viaja hacia la derecha
        KeyValue kvRight = new KeyValue(animatedViewRight.translateXProperty(), xRightEnd, Interpolator.EASE_BOTH);
        KeyFrame kfRight = new KeyFrame(Duration.seconds(seconds), kvRight);
        Timeline timelineRight = new Timeline();
        timelineRight.getKeyFrames().add(kfRight);
        
        // Oyente de fin de animación: Cuando termina el movimiento, limpia los estados y estabiliza el layout
        timelineRight.setOnFinished(t -> {
            // Oculta físicamente de la tarjeta gráfica todas las pantallas excepto la que ha quedado activa
            for (Node n : list) {
                if (!n.getId().equals(viewId)) {
                    n.setVisible(false);
                    n.setManaged(false);
                }
                // Resetea las coordenadas a 0 para que la pantalla responda bien si redimensionas la ventana de Windows
                n.translateXProperty().set(0);
            }
        });
        timelineRight.play(); // Ejecuta la animación del bloque derecho

        // Quita el foco para evitar parpadeos visuales en la interfaz
        parentContainer.requestFocus();
    }
}
