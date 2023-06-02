package delivery;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import pcd.util.Traza;

/*Clase motero, encargada de repartir pedidos una vez han sido tramitados*/
public class Motero extends Thread{

	private int idMotero;
	private ControlMoteros controlMoteros;
	/*Implementacion con semaforos*/
	Semaphore semaforo; // para controlar la sincronizacion (todos los moteros listos)
	Semaphore mutex; // para controlar la exclusion mutua (que el mismo motero salga a la vez varias veces)

	/*Implementacion con CyclicBarrier (solucion mas limpia)*/
	private CyclicBarrier cyclicBarrier; 

	public Motero(int idMotero, ControlMoteros controlMoteros, CyclicBarrier cyclicBarrier){
		this.idMotero = idMotero;
		this.controlMoteros = controlMoteros;

		/*Inicializando semaforos*/
		this.semaforo = new Semaphore(0); // Lo inicializamos a 0
		this.mutex = new Semaphore(1); // Lo inicializamos a 1

		/*Inicializando el Cyclic Barrier*/
		this.cyclicBarrier = cyclicBarrier; // Se lo pasamos por parametros de controlMoteros (Los moteros comparten hilo en su ControlMoteros)
	}

	@Override
	public void run() {
		while(true){
			// Uso switch para utilizar semaforos o cyclicBarrier    0: UTILIZAR SEMAFOROS      DEFAULT: UTILIZAR CYCLIC BARRIER
			// Nota: No uso if-else por evitar el Warning de Dead code
			switch (Config.utilizarSemaforos) {

			// ################# UTILIZANDO SEMAFOROS ###############################
			case 1:
				Traza.traza(6, "SEMAFOROS - Esperando a que todos los moteros esten disponibles...");
				semaforo.release(); // Incrementamos el numero de semaforos ()
				try {
					mutex.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (semaforo.availablePermits() < Config.numeroMoteros) {
					mutex.release();
					try {
						semaforo.acquire();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					try {
						mutex.acquire();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
				mutex.release(); // Desbloqueamos el mutex para el siguiente set de moteros
				break;

				// ################# UTILIZANDO CYCLIC BARRIER ###############################
			default:
				try {
					Traza.traza(6, "CyclicBarrier - Esperando a que todos los moteros esten disponibles...");
					cyclicBarrier.await(); // espero a que todos los moteros de su ControlMoteros esten disponibles  (Solucion muuucho mas limpia)
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}

			// Despues de la espera al resto de moteros, reparten y regresan normalmente:
			controlMoteros.repartirPedido(idMotero, controlMoteros.getPedido());
			controlMoteros.regresarMotero();
		}
	}
}
