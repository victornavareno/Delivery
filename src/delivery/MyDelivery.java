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
		lp = Pedido.pedidosDesdeFichero(Config.nombreFichero); // Pon aqui la ruta y nombre de tu fichero

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

		// Lanzamos los pedidos con streams (2)
		if (Config.lanzarPedidos == 2) {
			lp.stream().parallel().forEach(a -> listaRestaurantes.get(a.getRestaurante()).tramitarPedido(a));
		}

		// Callable para sacar el pedido mas caro
		System.out.println();
		PrecioMasAlto precioMasAlto = new PrecioMasAlto(lp); // callable para encontrar el pedido mas caro
		ThreadPoolExecutor executor2;
		executor2 = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Future<Double> future = executor2.submit(precioMasAlto);
		try {
			Double precioMasCaro = future.get();
			Traza.traza(ColoresConsola.PURPLE_BOLD_BRIGHT, 4,
					"EL PEDIDO MAS CARO (Utilizando Callable) ES: " + precioMasCaro);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		executor2.shutdown();

		// Streams para mostrar el importe del pedido mas caro:
		OptionalDouble precioMasCaroOptional = lp.stream()
				.parallel()
				.mapToDouble(Pedido::getPrecioPedido) // debemos convertirlo a un mapa de Doubles para encontrar el mas alto despues
				.max();
		double pedidoMasCaro = precioMasCaroOptional.orElse(0.0);
		Traza.traza(ColoresConsola.PURPLE_BOLD_BRIGHT, 4,
					"EL PEDIDO MAS CARO (Utilizando Streams) ES: " + pedidoMasCaro);

		// Streams para obtener una lista con todos los pedidos que tengan un precio
		// menor de 7
		System.out.println();
		Traza.traza(ColoresConsola.WHITE_BACKGROUND_BRIGHT, 4, "LA LISTA DE PEDIDOS QUE NO LLEGAN A 7 EUROS ES: ");
		lp.stream().filter(a -> a.getPrecioPedido() < 7).forEach(a -> System.out.println(a.getId()));

		// Forkjoin para obtener lista con los pedidos por encima de 12€ -- REVISAR
		List<Pedido> pedidosSuperan12Euros = new LinkedList<Pedido>(); // Aqui almacenaremos los pedidos caros
		for (Pedido pedido : lp) {
			ForkJoinTask task = new ForkJoinTask(pedido.getProductos(), 0, Config.numeroProductos);
			ForkJoinPool forkjoin = new ForkJoinPool();
			forkjoin.execute(task);
			if (task.superaPrecio()) {
				pedidosSuperan12Euros.add(pedido);
			}
			forkjoin.shutdown();
		}

		Traza.traza(ColoresConsola.RED_UNDERLINED, 6, "PEDIDOS QUE SUPERAN 12 EUROS: ");
		for (Pedido pedido : pedidosSuperan12Euros) {
			Traza.traza(ColoresConsola.RED_UNDERLINED, 6, pedido.getId() + " " + pedido.getPrecioPedido());
		}

		// AUDITORIAS
		// Streams para lanzar auditorias de los restaurantes (Utilizo forEachOrdered
		// para mostrarlos en orden original)
		System.out.println();
		System.out.println("REALIZANDO AUDITORIAS DE LOS RESTAURANTES (Usando streams)... ");
		listaRestaurantes.stream()
				.parallel()
				.forEachOrdered(
						r -> System.out.println("Auditoria Restaurante " + r.getNombre() + " " + r.getBalance()));

		System.out.println("\nAuditoria Cadena: " + cadenaRestaurantes.getBank().audit(0, Config.numeroRestaurantes));

		System.out.println("Tiempo total invertido en la tramitacion: " + (new Date().getTime() - initialTime));

		// Imprimir si hay pedido en una calle en concreto, utilizando streams
		String calle = "Berna, 11"; // introducir aqui el nombre de la calle a buscar
		System.out.println();
		Traza.traza(ColoresConsola.WHITE_BACKGROUND, 3, "Buscando un pedido realizado en la calle " + calle + "...");
		if (lp.stream().anyMatch(a -> a.getDireccion().equals(calle))) {
			Traza.traza(ColoresConsola.GREEN_BOLD_BRIGHT, 3, "Pedido encontrado");
		} else
			Traza.traza(ColoresConsola.RED_BOLD_BRIGHT, 3, "Pedido no encontrado");
		System.out.println();

	}

	public static void main(String[] args) {
		new MyDelivery();
	}
}
