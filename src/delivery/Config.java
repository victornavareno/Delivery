package delivery;
public class Config {
	public final static int modoTraza = 4;				// nivel de profundidad de la traza
	public final static int numeroRestaurantes = 7;		// numero de restaurantes a crear
	public final static int numeroPedidos = 5;			// numero de pedidos por canal a crear
	public final static int numeroMoteros = 2;			// numero de moteros por restaurante a crear
	public final static int numeroProductos = 3; 		// limite de cantidad de productos a crear en pedido
	public final static int maximoIdProducto = 5; 		// numero maximo de id de producto
	public final static int maximoPrecioProducto = 5;	// precio maximo de cada producto.
	
	// Anadidos por mi:
	public final static int lanzarPedidos = 1;			// 0: Lanzo los threads normalmente
														// 1: Lanzo los threads con executor
														// 2: Lanzo los threads con streams
}
