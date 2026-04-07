package cyberdetective.view;

import cyberdetective.controller.JuegoController;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.ScrollPane;

public class MenuPrincipal {

    private Stage stage;

    public MenuPrincipal(Stage stage) {
        this.stage = stage;
    }

    public void mostrar() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("panel-principal");

        // Fondo con elementos decorativos geométricos
        StackPane fondo = construirFondo();
        root.setCenter(fondo);

        // Contenido central
        VBox contenido = construirContenido();
        fondo.getChildren().add(contenido);

        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );
        scene.setFill(Color.web("#0a0a0f"));

        stage.setTitle("CyberDetective – El Árbol de la Verdad");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();

        animarEntrada(contenido);
    }

    private StackPane construirFondo() {
        StackPane pane = new StackPane();
        pane.setStyle("-fx-background-color: #0a0a0f;");

        // Círculos decorativos difusos en el fondo
        Circle c1 = new Circle(340);
        c1.setFill(Color.web("#00d4ff04"));
        c1.setTranslateX(-280);
        c1.setTranslateY(180);

        Circle c2 = new Circle(220);
        c2.setFill(Color.web("#00d4ff06"));
        c2.setTranslateX(380);
        c2.setTranslateY(-160);

        // Línea decorativa fina
        Rectangle linea = new Rectangle(1, 120);
        linea.setFill(Color.web("#00d4ff30"));
        linea.setTranslateX(-360);
        linea.setTranslateY(-100);

        pane.getChildren().addAll(c1, c2, linea);
        return pane;
    }

    private VBox construirContenido() {
        VBox contenido = new VBox(0);
        contenido.setAlignment(Pos.CENTER);
        contenido.setMaxWidth(540);
        contenido.setPadding(new Insets(0));

        // Etiqueta superior
        Label etiqueta = new Label("ESTRUCTURAS DE DATOS · AVL");
        etiqueta.getStyleClass().add("titulo-app");

        VBox.setMargin(etiqueta, new Insets(0, 0, 32, 0));

        // Título principal
        Label titulo1 = new Label("Cyber");
        titulo1.getStyleClass().add("menu-titulo");

        Label titulo2 = new Label("Detective");
        titulo2.getStyleClass().add("menu-titulo-acento");

        HBox tituloBox = new HBox(0, titulo1, titulo2);
        tituloBox.setAlignment(Pos.CENTER);

        Label subtitulo = new Label("El Árbol de la Verdad");
        subtitulo.getStyleClass().add("menu-subtitulo");
        VBox.setMargin(subtitulo, new Insets(6, 0, 48, 0));

        // Historia de Valeria
        VBox historia = construirHistoria();
        VBox.setMargin(historia, new Insets(0, 0, 48, 0));

        // Botones
        Button btnJugar = new Button("Iniciar investigación");
        btnJugar.getStyleClass().add("btn-primario");
        btnJugar.setPrefWidth(240);
        btnJugar.setOnAction(e -> iniciarJuego());

        Button btnAyuda = new Button("¿Cómo funciona?");
        btnAyuda.getStyleClass().add("btn-secundario");
        btnAyuda.setPrefWidth(240);
        btnAyuda.setOnAction(e -> mostrarAyuda());

        VBox.setMargin(btnAyuda, new Insets(12, 0, 0, 0));

        contenido.getChildren().addAll(
                etiqueta, tituloBox, subtitulo,
                historia, btnJugar, btnAyuda
        );

        return contenido;
    }

    private VBox construirHistoria() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setStyle(
                "-fx-background-color: #12121c;" +
                        "-fx-border-color: #1e1e2e;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 24 28 24 28;"
        );
        box.setMaxWidth(480);

        Label contexto = new Label("EL CASO");
        contexto.getStyleClass().add("etiqueta-seccion");

        Label historia = new Label(
                "Valeria, estudiante de NetCity, comienza a recibir mensajes " +
                        "ofensivos en redes sociales. Lo que parece una broma escala " +
                        "hasta convertirse en ciberacoso grave.\n\n" +
                        "El detective Alex te necesita. Usa el árbol de investigación " +
                        "para reconstruir los hechos, identificar al agresor y conocer " +
                        "las consecuencias legales en Colombia."
        );
        historia.getStyleClass().add("texto-cuerpo");
        historia.setWrapText(true);
        historia.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        box.getChildren().addAll(contexto, historia);
        return box;
    }

    private void mostrarAyuda() {
        Stage ventanaAyuda = new Stage();
        ventanaAyuda.setTitle("Cómo funciona – CyberDetective");

        VBox contenido = new VBox(20);
        contenido.setPadding(new Insets(40));
        contenido.setStyle("-fx-background-color: #0a0a0f;");
        contenido.setMaxWidth(520);

        Label titulo = new Label("Cómo funciona el juego");
        titulo.getStyleClass().add("titulo-nivel");

        String[] secciones = {
                "EL ÁRBOL AVL",
                "Cada caso de ciberacoso se convierte en un nodo del árbol. " +
                        "El árbol se organiza por gravedad del delito (1–10) y se " +
                        "auto-balancea con rotaciones AVL cada vez que insertas un caso.",

                "LOS NIVELES",
                "El juego tiene 4 capas de investigación: mensajes ofensivos, " +
                        "rumores virales, suplantación de identidad y ataque coordinado. " +
                        "Cada nivel agrega un nodo al árbol.",

                "LAS PREGUNTAS",
                "Antes de cerrar cada caso debes responder preguntas sobre las " +
                        "evidencias y las leyes colombianas aplicables. Cada respuesta " +
                        "correcta suma puntos; cada error resta.",

                "EL REPORTE FINAL",
                "Al completar los 4 niveles, el árbol se recorre en inorden " +
                        "y se genera un reporte con todos los delitos, leyes y penas. " +
                        "El agresor queda identificado."
        };

        for (int i = 0; i < secciones.length; i += 2) {
            Label sec = new Label(secciones[i]);
            sec.getStyleClass().add("etiqueta-seccion");

            Label desc = new Label(secciones[i + 1]);
            desc.getStyleClass().add("texto-cuerpo");
            desc.setWrapText(true);

            VBox bloque = new VBox(6, sec, desc);
            contenido.getChildren().add(bloque);
        }

        Button cerrar = new Button("Entendido");
        cerrar.getStyleClass().add("btn-primario");
        cerrar.setOnAction(e -> ventanaAyuda.close());

        contenido.getChildren().addAll(new javafx.scene.control.Separator(), cerrar);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setStyle("-fx-background-color: #0a0a0f; -fx-background: #0a0a0f;");
        scroll.setFitToWidth(true);

        Scene scene = new Scene(scroll, 560, 520);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );
        scene.setFill(Color.web("#0a0a0f"));

        ventanaAyuda.setScene(scene);
        ventanaAyuda.show();
    }

    private void iniciarJuego() {
        PantallaInicio inicio = new PantallaInicio(stage);
        inicio.mostrar();
    }

    // Entrada suave al abrir el menú
    private void animarEntrada(VBox contenido) {
        contenido.setOpacity(0);
        contenido.setTranslateY(18);

        FadeTransition fade = new FadeTransition(Duration.millis(600), contenido);
        fade.setToValue(1);

        TranslateTransition move = new TranslateTransition(Duration.millis(600), contenido);
        move.setToY(0);

        fade.play();
        move.play();
    }
}