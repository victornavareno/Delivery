package delivery;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import pcd.util.ColoresConsola;
import pcd.util.Traza;
import pcd.util.Ventana;

//SERVIDOR CON SOCKET TCP
public class PasarelaPago {

	/* NOTAS SESION: 
	 - Dentro de un main->
	 -  BUCLE INFINITO ESPERANDO PETICIONES -> while (true) ServerSocket.accept 
	 -  Espero pedidos por un puerto x (por ejemplo 20.000) para eso creo un server pasivo ServerSocket (se queda escuchando en el while)
	 -   Me quedo con un pedido que tiene id y precio
	 -   Pregunto si el precio de ese pedido es menor que 12,mando KO), (si >12 mando OK) por el puerto
	 -   El cliente debe:  tramitarPedido() si recibe OK */
	public static void main(String[] args) {
		Traza.traza(6, " Sockets TCP - ESPERANDO A RECIBIR PEDIDO");

		Ventana v = new Ventana("PEDIDOS TRAMITADOS ", 50, 320); // EN LA VENTANA SOLO MOSTRARE LOS PEDIDOS ACEPTADOS (>12 LEUROS)
		v.addText("    #### SOCKETS TCP ####" );
		v.addText("PEDIDOS QUE SUPEREN 12 EUROS:");

		try (ServerSocket serverSocket = new ServerSocket(9999)) {
			while (true) {
				Socket socket = serverSocket.accept();
				try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
						BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
					DatosPagoPedido pedido = (DatosPagoPedido) input.readObject();

					if (pedido.getImporte() < 12 ) {
						Traza.traza(ColoresConsola.BLUE_BACKGROUND, 6, "PEDIDO RECHAZADO" + pedido.getId() + "NO LLEGA A LOS 12 EUROS");
						//v.addText(pedido.getId()); // mostramos el pedido en la ventana si no ha sido aceptado 
						output.write("KO\n"); // MANDO KO POR EL PUERTO SI <12
						output.flush(); // FLUSH PARA FORZAR ENVIO
					} else {
						Traza.traza(ColoresConsola.BLUE_BACKGROUND, 6, "PEDIDO ACEPTADO" + pedido.getId());
						v.addText(pedido.getId()); // mostramos el pedido en la ventana si ha sido aceptado
						output.write("OK\n"); // MANDO OK POR EL PUERTO SI >= 12
						output.flush();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					socket.close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
