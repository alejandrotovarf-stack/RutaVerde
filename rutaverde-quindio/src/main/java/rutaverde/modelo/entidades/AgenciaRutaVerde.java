package rutaverde.modelo.entidades;

public class AgenciaRutaVerde {

    private final String nombreComercial;
    private final String nit;
    private final String rnt;
    private final String direccion;
    private final String telefono;
    private final String correo;
    private final String paginaWeb;

    public AgenciaRutaVerde(String nombreComercial, String nit, String rnt, String direccion,
                            String telefono, String correo, String paginaWeb) {
        this.nombreComercial = nombreComercial;
        this.nit = nit;
        this.rnt = rnt;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
        this.paginaWeb = paginaWeb;
    }

    public String getNombreComercial() { return nombreComercial; }
    public String getNit() { return nit; }
    public String getRnt() { return rnt; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getPaginaWeb() { return paginaWeb; }
}
