package cyberdetective.model;

public class Caso {

    private int id;
    private String tipoAcoso;
    private String descripcion;
    private String[] evidencias;
    private String leyColombia;
    private String penaAplicable;
    private int gravedad; // para el AVL
    private int ordenCronologico; // 1 = más antiguo, 10 = más reciente

    public Caso(int id, String tipoAcoso, String descripcion,
                String[] evidencias, String leyColombia,
                String penaAplicable, int gravedad, int ordenCronologico) {
        this.id = id;
        this.tipoAcoso = tipoAcoso;
        this.descripcion = descripcion;
        this.evidencias = evidencias;
        this.leyColombia = leyColombia;
        this.penaAplicable = penaAplicable;
        this.gravedad = gravedad;
        this.ordenCronologico = ordenCronologico;
    }

    public int getOrdenCronologico() { return ordenCronologico; }

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