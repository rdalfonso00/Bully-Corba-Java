
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTextArea;

/**
 * Implementación de las funciones del Peer a traves del POA
 *
 * @author poncho
 */
public class BullyImpl extends PeerPOA {

    private JTextArea areaMensajes;
    private JTextArea areaListaPeers;
    private String idPeer;
    private String mensaje;

    public BullyImpl(String idPeer) {
        this.idPeer = idPeer;
        mensaje = "";
    }

    @Override
    public void enviar_Mensaje(String mensaje) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        this.mensaje += dateFormat.format(date) + " | : "+ mensaje + "\n";
        areaMensajes.setText(this.mensaje);
    }

    @Override
    public void actualizar_Lista_Peers(String peers) {
        areaListaPeers.setText(peers);
    }

    @Override
    public void recibir_Mensaje() {
        //To do
    }

    public JTextArea getAreaMensajes() {
        return areaMensajes;
    }

    public void setAreaMensajes(JTextArea areaMensajes) {
        this.areaMensajes = areaMensajes;
    }

    public JTextArea getAreaListaPeers() {
        return areaListaPeers;
    }

    public void setAreaListaPeers(JTextArea areaListaPeers) {
        this.areaListaPeers = areaListaPeers;
    }

    public String getIdPeer() {
        return idPeer;
    }

    public void setIdPeer(String idPeer) {
        this.idPeer = idPeer;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
