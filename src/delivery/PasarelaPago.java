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
        Traza.traza(5, "ESPERANDO A RECIBIR PEDIDO");
        Ventana v = new Ventana("Pedidos recibidos: ", 50, 10);
        try {
            ServerSocket serverSocket = new ServerSocket(10000); // me creo un server socket por el puerto 10000

            while (true) { // esperamos a que llegue una petición de forma indefinida
                Socket socket = serverSocket.accept();
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                DatosPagoPedido pedido = (DatosPagoPedido) input.readObject();

                if (pedido.getImporte() < 12) {
                    Traza.traza(ColoresConsola.BLUE_BACKGROUND, 5, "PEDIDO RECHAZADO");
                    output.write("KO\n"); // este pedido será rechazado
                    output.flush();
                }

                else {
                    Traza.traza(ColoresConsola.BLUE_BACKGROUND, 5, "PEDIDO ACEPTADO");
                    v.addText(pedido.getId());
                    output.write("OK\n"); // este pedido será aceptado, enviamos OK
                    output.flush();
                }

                // CERRAMOS AMBOS SOCKETS
                socket.close();
                serverSocket.close();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
