
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import javax.swing.JOptionPane;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 *
 * @author poncho
 */
public class BullyPrincipal {

    BullyImpl peer;
    NamingContextExt ncRef;
    PeerView vista;

    public BullyPrincipal(String[] args) {
        try {
            vista = new PeerView();
            int min = 1;
            int max = 1000;
            Random random = new Random();
            int id = random.nextInt(max + min) + min;
            String idPeer = "Peer " + id;
            iniciarORB(args, idPeer);
            peer.setAreaMensajes(vista.mensajesTextArea);
            peer.setAreaListaPeers(vista.peersTextArea);
            setListeners();
            vista.mensajeTextField.requestFocus();
            vista.setTitle(idPeer);
            vista.setVisible(true);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void iniciarORB(String[] args, String idPeer) throws InvalidName, AdapterInactive, ServantNotActive, WrongPolicy, NotFound, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed {
        try {
            ORB orb = ORB.init(args, null);
            // Referencia al POA raiz y activa el manejador de POA
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
            peer = new BullyImpl(idPeer);
            // Obtiene la referencia al objeto del servidor
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(peer);
            Peer href = PeerHelper.narrow(ref);

            // Obtiene el namingcontext de la raiz
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Usa el NamingContextExt el cual es parte de Interoperable
            ncRef = NamingContextExtHelper.narrow(objRef);

            // Enlaza la referencia al objeto en nombre del servicio
            NameComponent path[] = ncRef.to_name(idPeer);
            ncRef.rebind(path, href);
            System.out.println("OK: " + peer.getIdPeer());
            //actualizarListaPeers();
            // Espera la invocación remota del cliente
            new Runnable() {
                @Override
                public void run() {
                    orb.run();
                }
            };
        } catch (Exception exception) {
            System.out.println("Error initORB: " + exception.getMessage());
            System.out.println(exception);
            System.exit(1);
        }
    }

    private void actualizarListaPeers() {
        try {
            String peerList = "";
            BindingListHolder bList = new BindingListHolder();
            BindingIteratorHolder bIterator = new BindingIteratorHolder();
            ncRef.list(1000, bList, bIterator);
            //recuperar la lista de peers 
            for (Binding v : bList.value) {
                peerList += v.binding_name[0].id + "\t@@OK@@\n";
            }
            //y hacer el update en cada peer con su referencia por nombre
            for (Binding v : bList.value) {
                Peer aux = PeerHelper.narrow(ncRef.resolve_str(v.binding_name[0].id));
                aux.actualizar_Lista_Peers(peerList);
            }
        } catch (Exception e) {
            System.out.println("ERROR : " + e);
        }
    }

    private void setListeners() {
        vista.enviarMensajeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String msg = vista.mensajeTextField.getText();
                enviar_Mensaje(msg);
            }
        });
        vista.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cerrarConexionPeer();
            }

        });
    }

    private void enviar_Mensaje(String msg) {
        try {
            BindingListHolder bList = new BindingListHolder();
            BindingIteratorHolder bIterator = new BindingIteratorHolder();
            ncRef.list(1000, bList, bIterator);
            for (Binding v : bList.value) {
                Peer aux = PeerHelper.narrow(ncRef.resolve_str(v.binding_name[0].id));
                aux.enviar_Mensaje(msg);
            }
        } catch (Exception e) { //esta exepcion se puede mejorar eliminando el peer de la lista en caso de error
            System.out.println("Error al enviar mensaje " + e);
            e.printStackTrace();
        }
    }

    private void cerrarConexionPeer() {
        try {
            System.out.println("F en el chat");
            ncRef.unbind( ncRef.to_name(peer.getIdPeer()));
        } catch (Exception e) {
            System.out.println(e);
        }
        actualizarListaPeers();
        System.exit(0);
    }

    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    BullyPrincipal pmain = new BullyPrincipal(args);
                    pmain.actualizarListaPeers();
                } catch (Exception e) {
                    System.out.println("Error de inicializacion" + e.getMessage() + "\n\n\n");
                    e.printStackTrace();
                }
            }
        });
    }
}
