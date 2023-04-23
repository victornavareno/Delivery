package delivery;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import pcd.util.Traza;

public class MyDelivery {
	
	public MyDelivery () {
		// para facilitar las trazas
		Traza.setNivel(Config.modoTraza);
		
		// Creando los restaurantes
		CadenaRestaurantes cadenaRestaurantes = null;
		cadenaRestaurantes = new CadenaRestaurantes (Config.numeroRestaurantes);
		cadenaRestaurantes.crearRestaurantes();

		// CARGAR PEDIDOS DE FICHERO
		// Obtenemos una lista de pedidos
		List<Pedido> lp;
		lp = new LinkedList<>();
		lp = Pedido.pedidosDesdeFichero ("pedidos7.bin"); // Pon aqui la ruta y nombre de tu fichero 
			
		// LANZAR PEDIDOS
		long initialTime = new Date().getTime();
		LinkedList<Restaurante> listaRestaurantes = cadenaRestaurantes.getRestaurantes();
		for (Pedido p:lp){
			ThreadPedidos tP = new ThreadPedidos(p, listaRestaurantes.get(p.getRestaurante()));
			tP.run();
		}

		// AUDITORIAS
		for (Restaurante r:listaRestaurantes) 
			System.out.print ("\nAuditoria Restaurante "+r.getNombre()+" "+r.getBalance());
		
		System.out.println ("\nAuditoria Cadena: "+ cadenaRestaurantes.getBank().audit(0, Config.numeroRestaurantes));
		
		System.out.println ("Tiempo total invertido en la tramitacion: "+(new Date().getTime() - initialTime));
	}

	public static void main(String[] args) {
		new MyDelivery();
	}
}
