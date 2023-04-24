package delivery;

import java.util.*;
import java.util.concurrent.CyclicBarrier;

import pcd.util.Traza;
import pcd.util.Ventana;

public class ControlMoteros {
	int numeroMoteros;
	Restaurante r;
	Ventana v;
	static int posicionVentana = 10;

	private Motero motero;
	private List<Pedido> listaPedidos;
	private Object o1; // waitset moteros
	private Object o2; // waitset pedidos

	// cyclic barrier:
	private CyclicBarrier cyclicBarrier;

	public ControlMoteros(Restaurante _r, int _numeroMoteros) {
		r = _r;
		numeroMoteros = _numeroMoteros;
		cyclicBarrier = new CyclicBarrier(numeroMoteros, motero);

		// Creamos una ventana para los mensajes de este objeto.
		v = new Ventana("Moteros de Rest." + r.getNombre(), posicionVentana, 10);
		posicionVentana += 250;

		listaPedidos = new ArrayList<Pedido>();
		o1 = new Object();
		o2 = new Object();

		// Lanzando los moteros del restaurante
		for (int i = 0; i < Config.numeroMoteros; i++) {
			motero = new Motero(i, this, cyclicBarrier);
			motero.start();
		}
	}

	public synchronized void enviarPedido(Pedido p) {
		try {
			Thread.sleep(500); // Simulamos que tarda 0,5 segundos en repartir
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		synchronized (o2) {
			listaPedidos.add(p); // anado el pedido a nuestra lista de pedidos
			o2.notify(); // notifico a mi waitset de pedidos
		}
	}

	public void moterosLibres() {
		synchronized (o1) {
			while (numeroMoteros == 0) {
				try {
					Traza.traza(6, "NO HAY MOTEROS DISPONIBLES");
					o1.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public Pedido getPedido() {
		synchronized (o2) {
			while (listaPedidos.isEmpty()) {
				try {
					o2.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return listaPedidos.remove(0); // devuelvo el primer pedido de mi lista
		}
	}

	public synchronized void repartirPedido(int idMotero, Pedido pedido) {
		v.addText("REPARTIENDO PEDIDO: " + pedido.getId() + " EL MOTERO: " + idMotero);
		numeroMoteros--;
	}

	public void regresarMotero() {
		synchronized (o1) {
			numeroMoteros++;
			o1.notify();
		}
	}
}
