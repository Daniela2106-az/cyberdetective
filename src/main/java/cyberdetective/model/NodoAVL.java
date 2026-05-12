package cyberdetective.model;

public class NodoAVL implements java.io.Serializable {

    Caso caso;
    NodoAVL izquierdo;
    NodoAVL derecho;
    int altura;

    public NodoAVL(Caso caso) {
        this.caso = caso;
        this.altura = 1;
        this.izquierdo = null;
        this.derecho = null;
    }

    public Caso getCaso() { return caso; }
    public int getAltura() { return altura; }
    public NodoAVL getIzquierdo() { return izquierdo; }
    public NodoAVL getDerecho() { return derecho; }
}