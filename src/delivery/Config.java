package delivery;
public class Config {
	public final static String nombreFichero = "pedidos7.bin"; // Nombre del fichero a leer
	public final static int modoTraza = 4;				// nivel de profundidad de la traza (4 nivel normal -> NIVEL 5 PARA METHOD TESTING -> NIVEL 6 LOCURA MAXIMA)
	public final static int numeroRestaurantes = 7;		// numero de restaurantes a crear
	public final static int numeroPedidos = 5;			// numero de pedidos por canal a crear
	public final static int numeroMoteros = 2;			// numero de moteros por restaurante a crear
	public final static int numeroProductos = 3; 		// limite de cantidad de productos a crear en pedido
	public final static int maximoIdProducto = 5; 		// numero maximo de id de producto
	public final static int maximoPrecioProducto = 5;	// precio maximo de cada producto.
	
	// Anadidos por mi:
	public final static int lanzarPedidos = 1;			// 0: Lanzo los threads manualmente  
														// 1: Lanzo los threads con Executor
														// 2: Lanzo los threads con Streams
														// 3: Lanzo los threads con Observable
	
	public final static int utilizarSemaforos = 0;		// 0: Utilizamos CyclicBarrier para esperar a  que todos los moteros esten listos para repartir		
														// 1: Utilizamos  Semaforos para esperar a que todos los moteros esten listos para repartir

}
