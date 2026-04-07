package cyberdetective.model;

public class Caso {

    private int id;
    private String tipoAcoso;
    private String descripcion;
    private String[] evidencias;
    private String leyColombia;
    private String penaAplicable;
    private int gravedad; // criterio de ordenamiento en el AVL (1 = leve, 10 = grave)

    public Caso(int id, String tipoAcoso, String descripcion,
                String[] evidencias, String leyColombia,
                String penaAplicable, int gravedad) {
        this.id = id;
        this.tipoAcoso = tipoAcoso;
        this.descripcion = descripcion;
        this.evidencias = evidencias;
        this.leyColombia = leyColombia;
        this.penaAplicable = penaAplicable;
        this.gravedad = gravedad;
    }

    // El árbol AVL se ordena por gravedad del delito
    public int getGravedad() { return gravedad; }
    public int getId() { return id; }
    public String getTipoAcoso() { return tipoAcoso; }
    public String getDescripcion() { return descripcion; }
    public String[] getEvidencias() { return evidencias; }
    public String getLeyColombia() { return leyColombia; }
    public String getPenaAplicable() { return penaAplicable; }

    @Override
    public String toString() {
        return "Caso #" + id + " | " + tipoAcoso + " | Gravedad: " + gravedad;
    }
}