package delivery;
import java.util.*;
import java.util.concurrent.*;
import pcd.util.ColoresConsola;
import pcd.util.Traza;

// Utilizar RecursiveTask<Objeto> si queremos devolver un objeto o lista en esta tarea 
// En este caso devolvemos una Lista con los pedidos que superen un precio de 12 euros sumando sus productos
public class ForkJoinTask extends RecursiveTask<List<Pedido>>{
	private static final long serialVersionUID = 1L;

	/*En esta lista se almacenaran los pedidos que superen el precio umbral (en este caso 12 euros)*/
	private List<Pedido> pedidos;

	public ForkJoinTask(List<Pedido> pedidos) {
		this.pedidos = pedidos;
	}

	@Override
	protected List<Pedido> compute() {
		List<Pedido> pedidosCaros = new ArrayList<>();

		if (pedidos.size() <= 10) { // CASO TRIVIAL: quedan 10 o menos pedidos en la lista de pedidos
			for (Pedido pedido : pedidos) {
				if (calcularPrecioTotal(pedido) > 12) {
					Traza.traza(ColoresConsola.RED_UNDERLINED, 6, "AÑADIENDO PEDIDO FORKJOIN: " + pedido.getId());
					pedidosCaros.add(pedido);
				}
			}
		} else {
			// divide y venceras recursivo: DIVIDIMOS EL PROBLEMA EN DOS Y LO UNIMOS MÁS ADELANTE CON UN JOIN
			int mitad = pedidos.size() / 2;
			ForkJoinTask izquierda = new ForkJoinTask(pedidos.subList(0, mitad));
			ForkJoinTask derecha = new ForkJoinTask(pedidos.subList(mitad, pedidos.size()));

			invokeAll(izquierda, derecha);
			
			// AQUI ESTA EL JOIN 
			pedidosCaros.addAll(izquierda.join());
			pedidosCaros.addAll(derecha.join());
		}

		return pedidosCaros;
	}

	private double calcularPrecioTotal(Pedido pedido) {
		double precioTotal = 0.0;
		for(Producto producto : pedido.getProductos()) {
			precioTotal += producto.getPrecio();
		}
		return precioTotal;
	}
	
}
