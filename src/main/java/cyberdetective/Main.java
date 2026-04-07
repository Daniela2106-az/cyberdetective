package cyberdetective;

import cyberdetective.view.MenuPrincipal;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada del juego CyberDetective.
 * Inicializa JavaFX y lanza el menú principal.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        MenuPrincipal menu = new MenuPrincipal(stage);
        menu.mostrar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}