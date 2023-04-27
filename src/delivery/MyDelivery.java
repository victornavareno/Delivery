package delivery;

import java.util.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

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

		// Lanzamos los pedidos de forma concurrente normal (0)
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

		//  ################### OBSERVABLES ################################
		
		// Lanzamos los pedidos con observable(3)
		if(Config.lanzarPedidos == 3) {

		}

		//FILTRO LOS PEDIDOS DE PRECIO > 12 CON UN OBSERVABLE
		System.out.println();
		Traza.traza(ColoresConsola.WHITE_BOLD_BRIGHT, 4, " Observable - PEDIDOS QUE TIENEN UN PRECIO DE MAS DE 12 EUROS:");
		Observable<Pedido> obsPrecio = Observable.fromIterable(lp); // Create an observable from the list of pedidos
		obsPrecio.filter(a -> a.getPrecioPedido() > 12)
		.subscribeOn(Schedulers.computation()) 
		.subscribe(b -> Traza.traza(ColoresConsola.WHITE_BOLD_BRIGHT, 4, b.getId()));
	

		//CALCULO LA SUMA DEL PRECIO DE LOS PEDIDOS CON OBSERVABLE PARALELO AL ANTERIOR
		Observable<Pedido> obsSuma = Observable.fromIterable(lp);
		obsSuma.map(Pedido::getPrecioPedido) // Creamos un mapa donde cada Pedido tendra un precio asignado
		.reduce((subtotal, precio) -> subtotal + precio) // Calculamos la suma con reduce()
		.subscribeOn(Schedulers.io())
		.subscribe(sum -> Traza.traza(ColoresConsola.YELLOW_BOLD_BRIGHT, 4," Observable - LA SUMA TOTAL DE IMPORTES DE PEDIDOS ES: " + sum)); 
		
		// ##########################################################

		
		// CALLABLE  para sacar el pedido mas caro
		System.out.println();
		PrecioMasAlto precioMasAlto = new PrecioMasAlto(lp); // callable para encontrar el pedido mas caro
		ThreadPoolExecutor executor2;
		executor2 = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Future<Double> future = executor2.submit(precioMasAlto);
		try {
			Double precioMasCaro = future.get();
			Traza.traza(ColoresConsola.PURPLE_BOLD_BRIGHT, 4,
					" Callable -EL PEDIDO MAS CARO ES: " + precioMasCaro);
		} catch (InterruptedException e) {
			e.printStackTrace();

		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		executor2.shutdown();

		// STREAMS para mostrar el importe del pedido mas caro:
		OptionalDouble precioMasCaroOptional = lp.stream()
				.parallel()
				.mapToDouble(Pedido::getPrecioPedido) // debemos convertirlo a un mapa de Doubles para encontrar el mas alto despues
				.max();
		double pedidoMasCaro = precioMasCaroOptional.orElse(0.0);
		Traza.traza(ColoresConsola.PURPLE_BOLD_BRIGHT, 4,
				" Streams - EL PEDIDO MAS CARO ES: " + pedidoMasCaro);

		// STREAMS para obtener una lista con todos los pedidos que tengan un precio menor de 7
		System.out.println();
		Traza.traza(ColoresConsola.BLACK_BACKGROUND_BRIGHT, 4, " Streams - LA LISTA DE PEDIDOS QUE NO LLEGAN A 7 EUROS ES: ");
		lp.stream().filter(a -> a.getPrecioPedido() < 7).forEach(a -> System.out.println(a.getId()));

		// FORKJOIN para obtener lista con los pedidos por encima de 12€ -- REVISAR
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

		Traza.traza(ColoresConsola.RED_UNDERLINED, 6, " ForkJoin - PEDIDOS QUE SUPERAN 12 EUROS: ");
		for (Pedido pedido : pedidosSuperan12Euros) {
			Traza.traza(ColoresConsola.RED_UNDERLINED, 6, pedido.getId() + " " + pedido.getPrecioPedido());
		}

		// AUDITORIAS
		// Streams para lanzar auditorias de los restaurantes (Utilizo forEachOrdered
		// para mostrarlos en orden original)
		System.out.println();
		System.out.println(" Streams - REALIZANDO AUDITORIAS DE LOS RESTAURANTES... ");
		listaRestaurantes.stream()
		.parallel()
		.forEachOrdered(
				r -> System.out.println("Auditoria Restaurante " + r.getNombre() + " " + r.getBalance()));

		System.out.println("\nAuditoria Cadena: " + cadenaRestaurantes.getBank().audit(0, Config.numeroRestaurantes));

		System.out.println("Tiempo total invertido en la tramitacion: " + (new Date().getTime() - initialTime));

		
		// Imprimir si hay pedido en una calle en concreto, utilizando streams
		String calle = "Berna, 11"; // introducir aqui el nombre de la calle a buscar
		System.out.println();
		Traza.traza(ColoresConsola.BLUE_BACKGROUND_BRIGHT, 3, " Streams - Buscando un pedido realizado en la calle " + calle + "...");
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
