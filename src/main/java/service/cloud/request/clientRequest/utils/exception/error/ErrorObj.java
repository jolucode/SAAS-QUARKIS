package service.cloud.request.clientRequest.utils.exception.error;

import java.util.List;

public class ErrorObj {

    private int id;

    private String message;

    private List<String> noteList;

    private Object attachment;

    private Throwable cause;

    /**
     * Constructor para la clase ErrorObj.
     *
     * @param id      El ID del error.
     * @param message El mensaje del error.
     */
    public ErrorObj(int id, String message) {
        this.id = id;
        this.message = message;
    } // ErrorObj

    /**
     * Constructor para la clase ErrorObj.
     *
     * @param id         El ID del error.
     * @param message    El mensaje del error.
     * @param attachment El objeto adjunto.
     */
    public ErrorObj(int id, String message, Object attachment) {
        this.id = id;
        this.message = message;
        this.attachment = attachment;
    } // ErrorObj

    /**
     * Constructor para la clase ErrorObj.
     *
     * @param id       El ID del error.
     * @param message  El mensaje del error.
     * @param Contiene la causa del error.
     */
    public ErrorObj(int id, String message, Throwable cause) {
        this.id = id;
        this.message = message;
        this.cause = cause;
    } // ErrorObj

    /**
     * Constructor para la clase ErrorObj.
     *
     * @param id       El ID del error.
     * @param message  El mensaje del error.
     * @param noteList La lista de notas agregadas al error.
     */
    public ErrorObj(int id, String message, List<String> noteList) {
        this.id = id;
        this.message = message;
        this.noteList = noteList;
    } // ErrorObj

    /**
     * Este metodo obtiene el ID del error.
     *
     * @return Retorna el ID del error.
     */
    public int getId() {
        return id;
    } // getId

    /**
     * Este metodo establece el ID del error.
     *
     * @param id El ID del error.
     */
    public void setId(int id) {
        this.id = id;
    } // setId

    /**
     * Este metodo obtiene el mensaje del error.
     *
     * @return Retorna el mensaje del error.
     */
    public String getMessage() {
        return message;
    } // getMessage

    /**
     * Este metodo establece el mensaje del error.
     *
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    } // setMessage

    /**
     * Este metodo obtiene la lista de notas agregadas al error.
     *
     * @return Retorna la lista de notas agregadas al error.
     */
    public List<String> getNoteList() {
        return noteList;
    }

    /**
     * Este metodo establece la lista de notas agregadas al error.
     *
     * @param noteList La lista de notas agregadas al error.
     */
    public void setNoteList(List<String> noteList) {
        this.noteList = noteList;
    }

    /**
     * Este metodo obtiene el objeto adjunto.
     *
     * @return Retorna el objeto adjunto.
     */
    public Object getAttachment() {
        return attachment;
    }

    /**
     * Este metodo establece el objeto adjunto.
     *
     * @param attachment El objeto adjunto.
     */
    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    /**
     * Este metodo obtiene la causa del error.
     *
     * @return Retorna la causa del error.
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Este metodo establece la causa del error.
     *
     * @param cause La causa del error.
     */
    public void setCause(Throwable cause) {
        this.cause = cause;
    }

}
