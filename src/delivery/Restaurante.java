package delivery;

import java.io.*;
import java.net.*;

import bank.*;
import pcd.util.ColoresConsola;
import pcd.util.Traza;

public class Restaurante {
	private String nombre; // nombre del restaurante
	private Account account; // cuenta bancaria para registrar la recaudaci�n
	private Cocina cocina; // la cocina de este restaurante
	private ControlMoteros controlMoteros; // los moteros de este restaurante

	public Restaurante(Account _ac, String _nombre, int _numeroMoteros) {
		account = _ac;
		nombre = _nombre;
		controlMoteros = new ControlMoteros(this, Config.numeroMoteros);
		cocina = new Cocina(this);
		Traza.traza(ColoresConsola.GREEN_BOLD_BRIGHT, 1, "Creando restaurante: " + nombre);
	}

	public String getNombre() {
		return nombre;
	}

	public double getBalance() {
		return account.getBalance();
	}

	public Account getAccount() {
		return account;
	}

	public void cocinarPedido(Pedido _p) {
		Traza.traza(ColoresConsola.BLUE_BOLD_BRIGHT, 5, "TRAMITANDO PEDIDO");
		Pedido p = _p;
		// Tramitar un pedido es:
		account.deposit(p.getPrecioPedido()); // anadir la cantidad abonada a la cuenta del banco
		controlMoteros.moterosLibres(); // espero a que haya moteros libres
		cocina.cocinar(p); // mandar el pedido a cocina
		controlMoteros.enviarPedido(p); // una vez cocinado, mandarlo a los moteros para que uno lo coja
	}

	public void tramitarPedido(Pedido _p) {
		/*
		 * SI RECIBIMOS OK= Tramitamos Pedido
		 * SI RECIBIMOS KO= Enviamos DatosPagoPedido por socket UDP al servidor PedidosNoPagados
		 */
		DatosPagoPedido datosPago = new DatosPagoPedido(_p.getId(), _p.getPrecioPedido());
		try (Socket socket = new Socket("localhost", 9999)) {
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// enviamos el pedido
			output.writeObject(datosPago);
			output.flush();
			// recibimos respuesta
			String response = input.readLine();
			if (response.equals("OK")) {
				cocinarPedido(_p);
			} else if (response.equals("KO")) {
				DatagramSocket dSocket = new DatagramSocket(); // No necesitamos indicar puerto o ip aqui, eso ira incluido en el paquete

				byte[] sendData  = new byte[1024];
				InetAddress direccionIp = InetAddress.getLocalHost();
				ByteArrayOutputStream baos;
				ObjectOutputStream oos;
				DatagramPacket sendPacket;
				try {
					baos = new ByteArrayOutputStream ();
					oos = new ObjectOutputStream (baos);
					oos.writeObject(datosPago);
					sendData= new byte[baos.toByteArray().length];
					sendData= baos.toByteArray();
					sendPacket = new DatagramPacket(sendData, sendData.length, direccionIp, 10000); 
					dSocket.send(sendPacket); 
				} catch (IOException e) {
					e.printStackTrace();
				}
				dSocket.close();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
