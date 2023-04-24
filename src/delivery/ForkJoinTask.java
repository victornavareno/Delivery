package delivery;

import java.util.*;
import java.util.concurrent.*;

import pcd.util.ColoresConsola;
import pcd.util.Traza;

public class ForkJoinTask extends RecursiveAction{

	private List<Producto> productos;
	
	private int first;
	private int last;
	private double totalPrecios;
	private boolean superaPrecio = false;
	
	public ForkJoinTask(List<Producto> productos, int first, int last) {
		this.productos = productos;
		this.first = first;
		this.last = last;
		this.totalPrecios = 0.0;
		
	}
	
	@Override
	protected void compute() {
		//caso trivial= quedan menos de 10 productos en la lista de pedidos
		if((last - first) < 10) {
            if(totalPrecios >= 12) {
				this.superaPrecio = true;
			}
			totalPrecios =+ sumarPrecios();
			Traza.traza(ColoresConsola.RED_BOLD, 6 , "Tarea Forkjoin: sumando precios. Precio actual= " + totalPrecios);
			//DUDA FERNANDO: COMO INDICAR QUE EL PEDIDO ACTUAL HA SUPERADO EL PRECIO COTA 12 EUROS

		}
		else {
			//llamada divide y venceras (RECURSIVO):
			int middle = (last + first) / 2;
			ForkJoinTask t1 = new ForkJoinTask(productos, first, middle+1);
			ForkJoinTask t2 = new ForkJoinTask(productos, middle+1, last);
			invokeAll(t1,t2);
		}
	}
	
	private double sumarPrecios() {
		double precioTotal = 0.0;
		for(int i = first; i<last ; i++) {
			Producto producto = productos.get(i);
			precioTotal += producto.getPrecio();
		}
		return precioTotal;
	}
	
	public double getPrecio() {
		Traza.traza(ColoresConsola.RED_BOLD, 6 , "el precio actual es: " + totalPrecios);
		return this.totalPrecios;
	}

	public boolean superaPrecio() {
		return superaPrecio;
	}
}
