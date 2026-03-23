package padron.entidades;

public class Direccion {

    private final String codElec;
    private final String provincia;
    private final String canton;
    private final String distrito;
    private final String recinto;

    public Direccion(String codElec, String provincia, String canton,
                     String distrito, String recinto) {
        this.codElec   = codElec;
        this.provincia = provincia;
        this.canton    = canton;
        this.distrito  = distrito;
        this.recinto   = recinto;
    }

    public String getCodElec()   { return codElec; }
    public String getProvincia() { return provincia; }
    public String getCanton()    { return canton; }
    public String getDistrito()  { return distrito; }
    public String getRecinto()   { return recinto; }
}