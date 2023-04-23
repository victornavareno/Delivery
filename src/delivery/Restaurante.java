package delivery;

import bank.*;
import pcd.util.ColoresConsola;
import pcd.util.Traza;

 public class Restaurante {
	private String nombre;					// nombre del restaurante
	private Account account;				// cuenta bancaria para registrar la recaudaci�n
	private Cocina cocina; 					// la cocina de este restaurante
	private ControlMoteros controlMoteros;  // los moteros de este restaurante

	
	public Restaurante (Account _ac, String _nombre, int _numeroMoteros) {
		account = _ac;
		nombre = _nombre;
		controlMoteros = new ControlMoteros (this, Config.numeroMoteros);
		cocina = new Cocina (this);
		Traza.traza(ColoresConsola.GREEN_BOLD_BRIGHT, 1,"Creando restaurante: "+nombre);
	}
	
	public String getNombre () {
		return nombre;
	}
	
	public double getBalance (){
		return account.getBalance();
	}
	
	public Account getAccount () {
		return account;
	}
	
	public void tramitarPedido (Pedido _p) {
		Pedido p =_p;
		// Tramitar un pedido es:
		account.deposit(p.getPrecioPedido()); 	// anadir la cantidad abonada a la cuenta del banco
		controlMoteros.moterosLibres();
		cocina.cocinar(p);						// mandar el pedido a cocina
		controlMoteros.enviarPedido(p);			// una vez cocinado, mandarlo a los moteros para que uno lo coja
	}
}
