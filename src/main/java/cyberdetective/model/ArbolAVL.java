package cyberdetective.model;

import java.util.ArrayList;
import java.util.List;

public class ArbolAVL implements java.io.Serializable {

    private NodoAVL raiz;

    public ArbolAVL() {
        this.raiz = null;
    }

    // ---------------- Utilidades de altura ----------------

    private int altura(NodoAVL nodo) {
        if (nodo == null) return 0;
        return nodo.altura;
    }

    private void actualizarAltura(NodoAVL nodo) {
        nodo.altura = 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    // Factor de equilibrio: izquierda - derecha
    private int factorEquilibrio(NodoAVL nodo) {
        if (nodo == null) return 0;
        return altura(nodo.izquierdo) - altura(nodo.derecho);
    }

    // ---------------- Rotaciones ----------------

    // Rotación simple a la derecha (caso izquierda-izquierda)
    private NodoAVL rotarDerecha(NodoAVL y) {
        NodoAVL x = y.izquierdo;
        NodoAVL T2 = x.derecho;

        x.derecho = y;
        y.izquierdo = T2;

        actualizarAltura(y);
        actualizarAltura(x);

        return x;
    }

    // Rotación simple a la izquierda (caso derecha-derecha)
    private NodoAVL rotarIzquierda(NodoAVL x) {
        NodoAVL y = x.derecho;
        NodoAVL T2 = y.izquierdo;

        y.izquierdo = x;
        x.derecho = T2;

        actualizarAltura(x);
        actualizarAltura(y);

        return y;
    }

    // ---------------- Inserción ----------------

    public void insertar(Caso caso) {
        raiz = insertarRecursivo(raiz, caso);
    }

    /**
     * Llave compuesta: primero por gravedad, luego por id (siempre único).
     * Garantiza un lugar único por caso aunque compartan gravedad.
     */
    private int comparar(Caso a, Caso b) {
        if (a.getGravedad() != b.getGravedad()) {
            return Integer.compare(a.getGravedad(), b.getGravedad());
        }
        return Integer.compare(a.getId(), b.getId());
    }

    private NodoAVL insertarRecursivo(NodoAVL nodo, Caso caso) {
        if (nodo == null) return new NodoAVL(caso);

        int cmp = comparar(caso, nodo.caso);
        if (cmp < 0) {
            nodo.izquierdo = insertarRecursivo(nodo.izquierdo, caso);
        } else {
            nodo.derecho = insertarRecursivo(nodo.derecho, caso);
        }

        actualizarAltura(nodo);

        int fe = factorEquilibrio(nodo);

        // Caso izquierda-izquierda
        if (fe > 1 && comparar(caso, nodo.izquierdo.caso) < 0) {
            return rotarDerecha(nodo);
        }

        // Caso derecha-derecha
        if (fe < -1 && comparar(caso, nodo.derecho.caso) > 0) {
            return rotarIzquierda(nodo);
        }

        // Caso izquierda-derecha
        if (fe > 1 && comparar(caso, nodo.izquierdo.caso) > 0) {
            nodo.izquierdo = rotarIzquierda(nodo.izquierdo);
            return rotarDerecha(nodo);
        }

        // Caso derecha-izquierda
        if (fe < -1 && comparar(caso, nodo.derecho.caso) < 0) {
            nodo.derecho = rotarDerecha(nodo.derecho);
            return rotarIzquierda(nodo);
        }

        return nodo;
    }

    // ---------------- Recorridos ----------------

    // Inorden: devuelve los casos ordenados de menor a mayor gravedad
    public List<Caso> recorridoInorden() {
        List<Caso> lista = new ArrayList<>();
        inorden(raiz, lista);
        return lista;
    }

    private void inorden(NodoAVL nodo, List<Caso> lista) {
        if (nodo == null) return;
        inorden(nodo.izquierdo, lista);
        lista.add(nodo.caso);
        inorden(nodo.derecho, lista);
    }

    // Devuelve todos los nodos para poder dibujar el árbol visualmente
    public NodoAVL getRaiz() {
        return raiz;
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    // Busca un caso por su id recorriendo el árbol
    public Caso buscarPorId(int id) {
        return buscarId(raiz, id);
    }

    private Caso buscarId(NodoAVL nodo, int id) {
        if (nodo == null) return null;
        if (nodo.caso.getId() == id) return nodo.caso;

        Caso izq = buscarId(nodo.izquierdo, id);
        if (izq != null) return izq;

        return buscarId(nodo.derecho, id);
    }

    // Genera el reporte final con todos los casos ordenados por gravedad
    public String generarReporte() {
        List<Caso> casos = recorridoInorden();
        StringBuilder sb = new StringBuilder();
        sb.append("===== REPORTE FINAL – CASO VALERIA =====\n\n");

        for (Caso c : casos) {
            sb.append("Caso #").append(c.getId()).append("\n");
            sb.append("  Tipo de acoso : ").append(c.getTipoAcoso()).append("\n");
            sb.append("  Descripción   : ").append(c.getDescripcion()).append("\n");
            sb.append("  Ley aplicable : ").append(c.getLeyColombia()).append("\n");
            sb.append("  Pena posible  : ").append(c.getPenaAplicable()).append("\n");
            sb.append("  Gravedad      : ").append(c.getGravedad()).append("/10\n");
            sb.append("  Evidencias    : ");
            for (String e : c.getEvidencias()) sb.append(e).append(" | ");
            sb.append("\n\n");
        }

        sb.append("=========================================\n");
        sb.append("Total de delitos registrados: ").append(casos.size());
        return sb.toString();
    }
}