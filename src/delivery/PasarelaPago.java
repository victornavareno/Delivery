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

//SERVIDOR
public class PasarelaPago {

    // MAIN
    // BUCLE INFINITO ESPERANDO PETICIONES
    // while (true){ ServerSocket.accept
    // Espero peticiones por un puerto x (por ejemplo 20.000)
    // para eso creo un server pasivo ServerSocket (se queda escuchando)
    // Me quedo con un pedido que tiene id y precio
    // Pregunto si el precio de ese pedido es menor que 12,mando KO, si >12 mando OK
    // modificar restaurante - tramitarPedido()
    public static void main(String[] args) {
        Traza.traza(6, " Sockets TCP - ESPERANDO A RECIBIR PEDIDO");
        
        Ventana v = new Ventana("PEDIDOS TRAMITADOS ", 50, 320);
        v.addText("    #### SOCKETS TCP ####" );
        v.addText("PEDIDOS QUE SUPEREN 12 EUROS:");
        
        try (ServerSocket serverSocket = new ServerSocket(9999)) { // try-with-resources block to ensure proper closing
            while (true) {
                Socket socket = serverSocket.accept();
                try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                    DatosPagoPedido pedido = (DatosPagoPedido) input.readObject();

                    if (pedido.getImporte() < 12) {
                        Traza.traza(ColoresConsola.BLUE_BACKGROUND, 6, "PEDIDO RECHAZADO" + pedido.getId());
                        output.write("KO\n");
                        output.flush();
                    } else {
                        Traza.traza(ColoresConsola.BLUE_BACKGROUND, 6, "PEDIDO ACEPTADO" + pedido.getId());
                        v.addText(pedido.getId()); // imprimimos el pedido en la ventana si ha sido aceptado
                        output.write("OK\n");
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
