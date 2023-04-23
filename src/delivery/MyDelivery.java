package delivery;

import java.util.*;
import java.util.concurrent.*;
import pcd.util.*;

public class MyDelivery {

	public MyDelivery() {
		// para facilitar las trazas
		Traza.setNivel(Config.modoTraza);

		// Creando los restaurantes
		CadenaRestaurantes cadenaRestaurantes = null;
		cadenaRestaurantes = new CadenaRestaurantes(Config.numeroRestaurantes);
		cadenaRestaurantes.crearRestaurantes();

		// CARGAR PEDIDOS DE FICHERO
		// Obtenemos una lista de pedidos
		List<Pedido> lp;
		lp = new LinkedList<>();
		lp = Pedido.pedidosDesdeFichero("pedidos7.bin"); // Pon aqui la ruta y nombre de tu fichero

		// LANZAR PEDIDOS
		long initialTime = new Date().getTime();
		LinkedList<Restaurante> listaRestaurantes = cadenaRestaurantes.getRestaurantes();

		// Lanzamos los pedidos de forma normal (0)
		if (Config.lanzarPedidos == 0) {
			for (Pedido p : lp) {
				ThreadPedidos tP = new ThreadPedidos(p, listaRestaurantes.get(p.getRestaurante()));
				tP.run();
			}
		}

		// Lanzamos los pedidos con executors (1)
		if (Config.lanzarPedidos == 1) {
			ThreadPoolExecutor executor;
			// inicializo el executor con un threadpool de numeroProcesadores threads
			executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			for (Pedido pedido : lp) {
				ThreadPedidos threadPedido = new ThreadPedidos(pedido, listaRestaurantes.get(pedido.getRestaurante()));
				executor.execute(threadPedido);
			}

			try {
				executor.awaitTermination(5, TimeUnit.SECONDS); // el executor espera 5 segundos
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor.shutdown(); // cierro el executor
		}

		// AUDITORIAS
		for (Restaurante r : listaRestaurantes)
			System.out.print("\nAuditoria Restaurante " + r.getNombre() + " " + r.getBalance());

		System.out.println("\nAuditoria Cadena: " + cadenaRestaurantes.getBank().audit(0, Config.numeroRestaurantes));

		System.out.println("Tiempo total invertido en la tramitacion: " + (new Date().getTime() - initialTime));
	}

	public static void main(String[] args) {
		new MyDelivery();
	}
}
