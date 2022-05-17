
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

/*
 * Implementación de las funciones del Peer a traves del POA
 *
 * @author poncho
 */
public class BullyImpl extends PeerPOA {

    private JTextArea areaMensajes;
    private JTextArea areaListaPeers;
    private JTextArea coordinadorTextArea;
    private String idPeer; // Peer + " " + id
    private String mensaje;
    private String coordinador;
    private boolean foundgreater;
    private boolean electionInProgress;
    NamingContextExt ncRef;

    public BullyImpl(String idPeer, ORB orb) throws org.omg.CORBA.ORBPackage.InvalidName {

        this.idPeer = idPeer;
        mensaje = "";
        coordinador = "";
        foundgreater = false;
        electionInProgress = false;
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        // Usa el NamingContextExt el cual es parte de Interoperable
        ncRef = NamingContextExtHelper.narrow(objRef);
    }

    @Override
    public void enviar_Mensaje(String mensaje) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        this.mensaje += dateFormat.format(date) + " | : " + mensaje + "\n";
        areaMensajes.setText(this.mensaje);
    }

    @Override
    public void actualizar_Lista_Peers(String peers) {
        areaListaPeers.setText(peers);
    }

    @Override
    public String getIdPeer() {
        return idPeer;
    }

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

    public void setIdPeer(String idPeer) {
        this.idPeer = idPeer;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    //----------------------------------------------------Nuevos Métodos :D---------------------------------------------------------//
    public void actualizarCoordinador(String coordinador) {
        try {
            BindingListHolder bList = new BindingListHolder();
            BindingIteratorHolder bIterator = new BindingIteratorHolder();
            ncRef.list(1000, bList, bIterator);
            for (Binding v : bList.value) {
                Peer aux = PeerHelper.narrow(ncRef.resolve_str(v.binding_name[0].id));
                aux.updateCoordinator(coordinador);
            }
        } catch (Exception e) { //esta exepcion se puede mejorar eliminando el peer de la lista en caso de error
            System.out.println("Error al enviar mensaje " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void updateCoordinator(String coor) {
        coordinadorTextArea.setText(coor);
    }

    @Override
    public void startElection(String idPeer) {
        electionInProgress = true;
        foundgreater = false;

        if (idPeer.equals(this.idPeer)) {
            System.out.println("Has iniciado elecciones" + idPeer);
            try {
                BindingListHolder bList = new BindingListHolder();
                BindingIteratorHolder bIterator = new BindingIteratorHolder();
                ncRef.list(1000, bList, bIterator);

                for (Binding valor : bList.value) {
                    Peer aux = PeerHelper.narrow(ncRef.resolve_str(valor.binding_name[0].id));
                    String nodeId = aux.getIdPeer();
                    String[] nameAndId = nodeId.split(" ");
                    String[] thisNameAndID = idPeer.split(" ");
                    
                    if (Integer.parseInt(nameAndId[1]) > Integer.parseInt(thisNameAndID[1])) {
                        System.out.println("Enviando solicitud de eleccion a " + aux.getIdPeer());
                        aux.startElection(idPeer);
                        foundgreater = true;
                    }
                }
            } catch (NotFound | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName ex) {
                Logger.getLogger(BullyPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
            if (!foundgreater) {
                iWon(this.idPeer);
            }
        } else {
            System.out.println("Solicitud de eleccion recibida de " + idPeer);
            sendOk(this.idPeer, idPeer);
        }
    }

    @Override
    public void sendOk(String where, String to) {
        if (!idPeer.equals(to)) {
            BindingListHolder bList = new BindingListHolder();
            BindingIteratorHolder bIterator = new BindingIteratorHolder();
            ncRef.list(1000, bList, bIterator);

            for (Binding valor : bList.value) {
                NameComponent[] nombre = {valor.binding_name[0]};
                if (valor.binding_type != BindingType.ncontext) {
                    try {
                        Peer aux = PeerHelper.narrow(ncRef.resolve_str(nombre[0].id));
                        if (aux.getIdPeer().equals(to)) {
                            System.out.println("Enviando ok a " + to);
                            aux.sendOk(where, to);

                            startElection(this.idPeer);
                        }
                    } catch (Exception ex) {

                        try {
                            ncRef.unbind(nombre);
                            System.out.println("F en el chat");
                        } catch (NotFound ex1) {
                            ex1.printStackTrace();
                        } catch (CannotProceed ex1) {
                            ex1.printStackTrace();
                        } catch (InvalidName ex1) {
                            ex1.printStackTrace();
                        }
                    }
                }
            }
        } else {
            System.out.println(where + "Responde con OK...");
        }
    }

    @Override
    public void iWon(String idPeer) {
        //actualizarCoordinador(idPeer);
        coordinador = idPeer;
        electionInProgress = false;
        //actualizarCoordinador(idPeer);

        if (idPeer.equals(this.idPeer)) {
            // send win
            System.out.println("Has ganado, Notificando a los otros nodos.....");
            try {
                BindingListHolder bList = new BindingListHolder();
                BindingIteratorHolder bIterator = new BindingIteratorHolder();
                ncRef.list(1000, bList, bIterator);
                ///*Registry*/ reg = LocateRegistry.getRegistry();
                for (Binding v : bList.value) {
                    //PeerOperations stub;//CAMBIO ------------------------
                    Peer aux = PeerHelper.narrow(ncRef.resolve_str(v.binding_name[0].id));
                    if (!aux.getIdPeer().equals(this.idPeer)) {
                        aux.iWon(idPeer);
                    }

                }
            } catch (NotFound | CannotProceed | InvalidName ex) {
                Logger.getLogger(BullyImpl.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
            //stub = (PeerOperations) app.registry.lookup(v);
        }
        // receive win
        System.out.println("Nodo " + idPeer + " gano la eleccion.\n");
        //updateCoordinator(idPeer);
        actualizarCoordinador(idPeer);

    }

    @Override
    public boolean isalive() {
        return true;
    }

    @Override
    public String getCoordinator() {
        return coordinador;
    }

    public boolean getelectionInProgress() {
        return electionInProgress;
    }

    public JTextArea getCoordinadorTextArea() {
        return coordinadorTextArea;
    }

    public void setCoordinadorTextArea(JTextArea coordinadorTextArea) {
        this.coordinadorTextArea = coordinadorTextArea;
    }
}
