
/**
* PeerOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Peer.idl
* martes 17 de mayo de 2022 01:19:05 AM CDT
*/

public interface PeerOperations 
{
  void enviar_Mensaje (String mensaje);
  String getIdPeer ();
  void actualizar_Lista_Peers (String peers);
  void updateCoordinator (String coor);
  void startElection (String nodeId);
  void sendOk (String where, String to);
  void iWon (String node);
  boolean isalive ();
  String getCoordinator ();
} // interface PeerOperations
