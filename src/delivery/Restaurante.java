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
		 * SI RECIBIMOS KO= No hacemos nada con ese pedido
		 */
		DatosPagoPedido datosPago = new DatosPagoPedido(_p.getId(), _p.getPrecioPedido());
		try (Socket socket = new Socket("localhost", 10000)) {
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// enviamos el pedido
			output.writeObject(datosPago);
			output.flush();
			// recibimos respuesta
			if (input.readLine().equals("OK")) {
				cocinarPedido(_p);
			}

			else if (input.readLine().equals("KO")) {
				// mando un objeto DatosPagoPedido con el pedido
				// En PedidosNoPagados leemos del socket
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
