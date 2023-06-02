package delivery;
import java.util.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.concurrent.*;
import pcd.util.*;

// 			  PROYECTO myDelivery PCD 2023 
//         ALUMNO: VICTOR ANDRES NAVARENO MOZA
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


		//#########################################################################################
		// DIFERENTES FORMAS DE LANZAR LOS PEDIDOS DE FORMA CONCURRENTE:
		// 0: RUN()        1: EXECUTORS         2: STREAMS          3: OBSERVABLE
		switch(Config.lanzarPedidos) {

		case 0:		// Lanzamos los pedidos de forma normal - la primera aprendida en clase (run si runnable) (start si hereda de Thread) (0)
			Traza.traza(ColoresConsola.GREEN_BOLD, 1, "Lanzando pedidos... - ejecutando cada hilo con .run()");

			for (Pedido p : lp) {
				ThreadPedidos tP = new ThreadPedidos(p, listaRestaurantes.get(p.getRestaurante()));
				tP.run();
			}
			break;

		case 1:		// Lanzamos los pedidos con EXECUTORS (1)
			Traza.traza(ColoresConsola.GREEN_BOLD, 1, "Lanzando pedidos... - utilizando EXECUTORS");

			ThreadPoolExecutor executor;
			// inicializo el executor con un threadpool de numeroProcesadores threads
			executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			for (Pedido pedido : lp) {
				ThreadPedidos threadPedido = new ThreadPedidos(pedido, listaRestaurantes.get(pedido.getRestaurante()));
				executor.execute(threadPedido);
			}
			try {
				executor.awaitTermination(5, TimeUnit.SECONDS); // el executor espera 5 segundos para asegurar que se han completado las tareas
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor.shutdown(); // cierro el executor
			break;

		case 2:		// Lanzamos los pedidos con STREAMS (2) 
			Traza.traza(ColoresConsola.GREEN_BOLD, 1, "Lanzando pedidos... - utilizando STREAMS");

			lp.stream().parallel().forEach(a -> listaRestaurantes.get(a.getRestaurante()).tramitarPedido(a)); // Solucion bastante limpia, en una linea lanzamos concurrente
			break;

		case 3:		// Lanzamos los pedidos con observable (3)
			Traza.traza(ColoresConsola.GREEN_BOLD, 1, "Lanzando pedidos... - utilizando OBSERVABLE");

			Observable<Pedido> pedidosObservable = Pedido.pedidosDesdeFicheroObservable(Config.nombreFichero);
			// En este caso el flatmap combina varios Observables y los transforma en un solo observable (Flattening)
			// El observable pedidosObservable emite Observable de pedidos, transformamos cada uno de esos Observables a pedidos utilizando el flatmap
			// Con flatmap:
			Observable<Pedido> pedidosIndividualesObservable = pedidosObservable   
					.flatMap(pedido -> Observable.just(pedido));   

			pedidosIndividualesObservable // .subscribeOn(Schedulers.computation())   // No es recomendable suscribirse a este hilo... repasar
			.subscribe(pedido -> listaRestaurantes.get(pedido.getRestaurante()).tramitarPedido(pedido)); // TRAMITAMOS LOS PEDIDOS SEGUN LLEGAN EN EL OBSERVABLE (subscribe()))

			// Implementacion con Observable sin flatmap:
			// pedidosObservable.subscribe(pedido -> listaRestaurantes.get(pedido.getRestaurante()).tramitarPedido(pedido)); // No hacemos flattening 
			break;

		default:	// ERROR: NO SE LANZAN LOS PEDIDOS SI Config.lanzarPedidos esta fuera de rango 
			Traza.traza(ColoresConsola.RED_BACKGROUND, 1, "### ERROR - LOS PEDIDOS NO SE HAN LANZADO DE FORMA CONCURRENTE. ### ");
			Traza.traza(ColoresConsola.RED_BACKGROUND, 1, "CONSEJO: Prueba con un valor de Config.LanzarPedidos valido (0,1,2 o 3)");
			break;
		}
		//#########################################################################################


		//#########################################################################################
		// OBSERVABLE - FILTRO LOS PEDIDOS DE PRECIO > 12 CON UN OBSERVABLE
		System.out.println();
		System.out.println(" Observable - LOS PEDIDOS QUE SUPERAN EL IMPORTE DE 12 EUROS SON: ");
		Observable<Pedido> obsPrecio = Observable.fromIterable(lp); // Create an observable from the list of pedidos
		obsPrecio.filter(a -> a.getPrecioPedido() > 12)
		.subscribeOn(Schedulers.computation()) 
		.subscribe(b -> System.out.println(b.getId()));		 // para ver en que hilo se esta ejecutando: Thread.currentThread().getName();

		// OBSERVABLE - CALCULO LA SUMA DE TODOS LOS PEDIDOS (ACEPTADOS Y RECHAZADOS)
		Observable<Pedido> obsSuma = Observable.fromIterable(lp);
		obsSuma.map(Pedido::getPrecioPedido) // Creamos un mapa, donde cada elemento será un Pedido y su precio
		.reduce((subtotal, precio) -> subtotal + precio) // Calculamos la suma usando reduce() en el mapa
		.subscribeOn(Schedulers.computation())
		.subscribe(sum -> Traza.traza(ColoresConsola.PURPLE_BOLD_BRIGHT, 4,
				" Observable - La suma total de los importes de pedidos (TRAMITADOS Y NO TRAMITADOS) es:  " + sum));
		//#########################################################################################


		//#########################################################################################
		// CALLABLE para sacar el pedido mas caro
		System.out.println();
		PrecioMasAlto precioMasAlto = new PrecioMasAlto(lp); //  definimos el callable para encontrar el pedido mas caro
		ThreadPoolExecutor executorPrecioMasAlto;
		executorPrecioMasAlto = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Future<Double> future = executorPrecioMasAlto.submit(precioMasAlto); // Future es una especie de "contrato" para tener esa variable una vez la calculemos
		try {
			Double precioMasCaro = future.get(); // Cogemos el valor que habiamos prometido en el "contrato" de Future
			Traza.traza(ColoresConsola.PURPLE_BOLD_BRIGHT, 4,
					" Callable - EL PRECIO DEL PEDIDO MAS CARO ES: " + precioMasCaro);
		} catch (InterruptedException e) {
			e.printStackTrace();

		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		try {
			executorPrecioMasAlto.awaitTermination(2, TimeUnit.SECONDS); // esperamos a que este executor termine 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		executorPrecioMasAlto.shutdown();
		//#########################################################################################


		//#########################################################################################
		// STREAMS para mostrar el importe del pedido mas caro:
		OptionalDouble precioMasCaroOptional = lp.stream()
				.parallel()
				.mapToDouble(Pedido::getPrecioPedido) // debemos convertirlo a un mapa de Doubles para encontrar el mas alto despues
				.max();
		double pedidoMasCaro = precioMasCaroOptional.orElse(0.0);
		Traza.traza(ColoresConsola.PURPLE_BOLD_BRIGHT, 4,
				" Streams - EL PEDIDO MAS CARO ES: " + pedidoMasCaro);

		// STREAMS para obtener una lista con todos los pedidos que tengan un precio < 7
		System.out.println();
		System.out.println(" Streams - LA LISTA DE PEDIDOS QUE NO LLEGAN A 7 EUROS ES: ");
		lp.stream().filter(a -> a.getPrecioPedido() < 7).forEach(a -> System.out.println( "   " + a.getId()));
		//#########################################################################################


		//#########################################################################################
		// FORKJOIN PARA OBTENER LA LISTA DE PEDIDOS POR ENCIMA DE 12 EUROS:
		ForkJoinPool forkJoinPool = new ForkJoinPool(); // Pool de hilos que se encargaran de ejecutar esta tarea
		ForkJoinTask forkJoin= new ForkJoinTask(lp);
		List <Pedido> listaPedidosCarosForkJoin = forkJoinPool.invoke(forkJoin); // asignamos la lista una vez se calcule en el forkjoin- metodo invoke ejecuta compute

		System.out.println();
		Traza.traza(ColoresConsola.PURPLE_BOLD_BRIGHT, 4, " ForkJoin - Lista de pedidos que superan 12 euros: ");
		for(Pedido pedido: listaPedidosCarosForkJoin) {
			Traza.traza(ColoresConsola.PURPLE_BOLD_BRIGHT, 4, pedido.getId());
		}
		//#########################################################################################


		//#########################################################################################
		// TRAMITAR LAS AUDITORIAS 
		// STREAMS para lanzar auditorias de los restaurantes (Utilizo forEachOrdered para mostrarlos en orden original)
		System.out.println();
		System.out.println(" Streams - REALIZANDO AUDITORIAS DE LOS RESTAURANTES... ");
		listaRestaurantes.stream()
		.parallel()
		.forEachOrdered(
				r -> System.out.println("    Auditoria Restaurante " + r.getNombre() + " " + r.getBalance()));

		System.out.println("\n  Auditoria Cadena: " + cadenaRestaurantes.getBank().audit(0, Config.numeroRestaurantes));

		System.out.println("   Tiempo total invertido en la tramitacion: " + (new Date().getTime() - initialTime));
		//#########################################################################################


		//#########################################################################################
		// STREAMS para buscar un pedido en una calle en concreto
		String calle = "Berna, 11"; // <--- Introduce aquí el nombre de la calle a buscar

		System.out.println();
		System.out.println("Buscando un pedido realizado en la calle: " + calle + "...");
		if (lp.stream().anyMatch(a -> a.getDireccion().equals(calle))) { // Operador anyMatch + lambda para encontrar igualdades en un set de datos
			Traza.traza(ColoresConsola.GREEN_BOLD, 3, "   Pedido encontrado en esa calle!");
		} 
		else
			Traza.traza(ColoresConsola.RED_BOLD_BRIGHT, 3, "   Pedido no encontrado en esa calle");
		System.out.println();
		//#########################################################################################
	}

	public static void main(String[] args) {
		new MyDelivery();
	}
}
