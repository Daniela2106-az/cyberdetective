package cyberdetective.minijuego;

import javafx.animation.FadeTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Minijuego "Escombros":
 * 20 objetos de basura cubren una evidencia central.
 * El jugador los arrastra fuera del área central.
 * Cuando el área queda despejada, la evidencia se vuelve clicable y se completa el juego.
 */
public class EscombroMinijuego extends Minijuego {

    private static final double W = 640;
    private static final double H = 460;

    // Área de la evidencia (rectángulo central absoluto)
    private static final double EV_X = W / 2 - 90;
    private static final double EV_Y = H / 2 - 65;
    private static final double EV_W = 180;
    private static final double EV_H = 130;

    private final List<StackPane> basuras = new ArrayList<>();
    private StackPane evidencia;
    private boolean completado = false;

    private static final String[] TEXTOS = {
        "PAPEL", "LATA", "BASURA", "TRAPO", "CAJA",
        "BOTELLA", "PLÁSTICO", "CARTÓN", "FOLIO", "BOLSA"
    };
    private static final Color[] COLORES = {
        Color.web("#3a5f3a"), Color.web("#5f3a3a"), Color.web("#3a3a5f"),
        Color.web("#5f5a3a"), Color.web("#4a3a5f"), Color.web("#3a5a5f")
    };

    public EscombroMinijuego() {
        setPrefSize(W, H);
        setMaxSize(W, H);
        setStyle("-fx-background-color: #0d0d1a; -fx-border-color: #1e1e3f; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
    }

    @Override
    public void iniciar() {
        getChildren().clear();
        basuras.clear();
        completado = false;

        // ── Instrucción ──────────────────────────────
        Label instruccion = new Label("🔍  Arrastra los escombros para encontrar la evidencia");
        instruccion.setStyle("-fx-text-fill:#00d4ff;-fx-font-size:12px;-fx-font-weight:600;");
        instruccion.setLayoutX(10);
        instruccion.setLayoutY(8);
        getChildren().add(instruccion);

        // ── Evidencia central (oculta inicialmente) ──
        evidencia = construirEvidencia();
        evidencia.setLayoutX(EV_X);
        evidencia.setLayoutY(EV_Y);
        evidencia.setOpacity(0.15);
        evidencia.setMouseTransparent(true);
        getChildren().add(evidencia);

        // ── Basuras ───────────────────────────────────
        Random rnd = new Random();
        int intentos = 0;
        int creadas = 0;
        while (creadas < 20 && intentos < 500) {
            intentos++;
            double bw = 55 + rnd.nextInt(40);
            double bh = 25 + rnd.nextInt(20);
            double bx = 20 + rnd.nextDouble() * (W - bw - 40);
            double by = 35 + rnd.nextDouble() * (H - bh - 45);

            StackPane basura = construirBasura(rnd, bw, bh);
            basura.setLayoutX(bx);
            basura.setLayoutY(by);
            basuras.add(basura);
            getChildren().add(basura);
            habilitarArrastre(basura);
            creadas++;
        }

        tiempoInicio = System.currentTimeMillis();
    }

    @Override
    public void finalizar() {
        completado = true;
    }

    // ── Builder helpers ───────────────────────────────────────────────────

    private StackPane construirEvidencia() {
        Rectangle bg = new Rectangle(EV_W, EV_H);
        bg.setArcWidth(12); bg.setArcHeight(12);
        bg.setFill(Color.web("#1a1a3a"));
        bg.setStroke(Color.web("#00d4ff"));
        bg.setStrokeWidth(2);

        Label icon = new Label("🗂");
        icon.setStyle("-fx-font-size:32px;");

        Label lbl = new Label("EVIDENCIA");
        lbl.setStyle("-fx-text-fill:#00d4ff;-fx-font-size:11px;-fx-font-weight:700;");

        VBox content = new VBox(6, icon, lbl);
        content.setAlignment(javafx.geometry.Pos.CENTER);

        StackPane sp = new StackPane(bg, content);
        sp.setPrefSize(EV_W, EV_H);
        return sp;
    }

    private StackPane construirBasura(Random rnd, double bw, double bh) {
        Color color = COLORES[rnd.nextInt(COLORES.length)];
        Rectangle bg = new Rectangle(bw, bh);
        bg.setArcWidth(8); bg.setArcHeight(8);
        bg.setFill(color.deriveColor(0, 1, 1, 0.9));
        bg.setStroke(color.brighter());
        bg.setStrokeWidth(1);

        String texto = TEXTOS[rnd.nextInt(TEXTOS.length)];
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill:#e0e0e0;-fx-font-size:9px;-fx-font-weight:600;");

        StackPane sp = new StackPane(bg, lbl);
        sp.setPrefSize(bw, bh);
        return sp;
    }

    // ── Arrastre ─────────────────────────────────────────────────────────

    private void habilitarArrastre(StackPane basura) {
        final double[] dragOffset = new double[2];

        basura.setOnMousePressed(e -> {
            dragOffset[0] = e.getX();
            dragOffset[1] = e.getY();
            basura.toFront();
            e.consume();
        });

        basura.setOnMouseDragged(e -> {
            double nx = basura.getLayoutX() + e.getX() - dragOffset[0];
            double ny = basura.getLayoutY() + e.getY() - dragOffset[1];
            basura.setLayoutX(nx);
            basura.setLayoutY(ny);
            e.consume();
        });

        basura.setOnMouseReleased(e -> {
            verificarArea();
            e.consume();
        });
    }

    // ── Verificación ──────────────────────────────────────────────────────

    private void verificarArea() {
        if (completado) return;

        boolean centroLibre = basuras.stream().allMatch(b -> {
            double bx = b.getLayoutX();
            double by = b.getLayoutY();
            double bw = b.getPrefWidth();
            double bh = b.getPrefHeight();
            // Sin intersección con el área de evidencia
            return bx + bw < EV_X || bx > EV_X + EV_W
                || by + bh < EV_Y || by > EV_Y + EV_H;
        });

        if (centroLibre) {
            revelarEvidencia();
        }
    }

    private void revelarEvidencia() {
        evidencia.setMouseTransparent(false);

        FadeTransition ft = new FadeTransition(Duration.millis(300), evidencia);
        ft.setFromValue(0.15);
        ft.setToValue(1.0);
        ft.play();

        DropShadow glow = new DropShadow(20, Color.web("#00d4ff"));
        glow.setInput(new Glow(0.6));
        evidencia.setEffect(glow);

        evidencia.setOnMouseClicked(e -> {
            if (!completado) {
                completado = true;
                completar();
            }
        });
        evidencia.setCursor(javafx.scene.Cursor.HAND);
    }
}
