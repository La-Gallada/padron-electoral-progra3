package padron.dto;

public class DireccionDTO {

    private final String provincia;
    private final String canton;
    private final String distrito;
    private final String recinto;

    public DireccionDTO(String provincia, String canton, String distrito, String recinto) {
        this.provincia = provincia;
        this.canton = canton;
        this.distrito = distrito;
        this.recinto = recinto;
    }

    public String getProvincia() { return provincia; }
    public String getCanton() { return canton; }
    public String getDistrito() { return distrito; }
    public String getRecinto() { return recinto; }
}