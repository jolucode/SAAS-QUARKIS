package service.cloud.request.clientRequest.model.model;

public class RequestDeclare {

    private String nomArchivo;
    private String arcGreZip;
    private String hashZip;

    public RequestDeclare(String nomArchivo, String arcGreZip, String hashZip) {
        this.nomArchivo = nomArchivo;
        this.arcGreZip = arcGreZip;
        this.hashZip = hashZip;
    }

    public String getNomArchivo() {
        return nomArchivo;
    }

    public void setNomArchivo(String nomArchivo) {
        this.nomArchivo = nomArchivo;
    }

    public String getArcGreZip() {
        return arcGreZip;
    }

    public void setArcGreZip(String arcGreZip) {
        this.arcGreZip = arcGreZip;
    }

    public String getHashZip() {
        return hashZip;
    }

    public void setHashZip(String hashZip) {
        this.hashZip = hashZip;
    }

    @Override
    public String toString() {
        return "RequestDeclare{" +
                "nomArchivo='" + nomArchivo + '\'' +
                ", arcGreZip='" + arcGreZip + '\'' +
                ", hashZip='" + hashZip + '\'' +
                '}';
    }
}
