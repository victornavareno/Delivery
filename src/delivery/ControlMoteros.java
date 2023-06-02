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
	private List<Pedido> listaPedidos; // Almacena los pedidos a enviar por nuestros moteros disponibles
	
	// WAITSETS, DONDE LOS HILOS REALIZAN ESPERA ACTIVA HASTA PODER REALIZAR SU TAREA
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

		// Lanzando los moteros del restaurante (Para cada motero le paso un id, su control moteros y el cyclicBarrier por si lo usamos)
		for (int i = 0; i < Config.numeroMoteros; i++) {
			motero = new Motero(i, this, cyclicBarrier);
			motero.start();
		}
	}

	// ANADE UN PEDIDO A MI LISTA, LO QUE QUIERE DECIR QUE ESTA LISTO PARA ENVIARSE
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

	// METODO DE ESPERA HASTA QUE EXISTA ALGUN MOTERO DISPONIBLE
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

	// EN CUANTO HAY UN PEDIDO LISTO, LO COJO DE LA LISTA
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

	// DECREMENTA EL NUMERO DE MOTEROS DISPONIBLES
	public synchronized void repartirPedido(int idMotero, Pedido pedido) {
		v.addText("REPARTIENDO PEDIDO: " + pedido.getId());  //+ " MOTERO ENCARGADO: " + idMotero); 
		numeroMoteros--;
	}

	// INCREMENTA EL NUMERO DE MOTEROS DISPONIBLES
	public void regresarMotero() {
		synchronized (o1) {
			numeroMoteros++;
			o1.notify();
		}
	}
}
