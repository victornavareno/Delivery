package delivery;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.*;
import pcd.util.*;

public class Contenedor {
	private int cuantos = 0;
	private int numeroMax;
	private Robot robot;

	/* Para la exclusion mutua */
	final Lock lock = new ReentrantLock(true);

	/* Variables de condicion */
	final Condition lleno = lock.newCondition();
	final Condition vacio = lock.newCondition();

	LinkedBlockingQueue<Integer> listaLechugas;
	boolean lechuga;

	public Contenedor (int numeroMax) {
		this.numeroMax = numeroMax; 
		this.lechuga = false;

		robot = new Robot(this, lechuga);
		Thread threadRobot = new Thread(robot);
		threadRobot.start();
	}
	
	public Contenedor (int numeroMax, boolean lechuga) {
		this.numeroMax = numeroMax; 
		this.lechuga = lechuga;
		
		listaLechugas = new LinkedBlockingQueue<Integer>(numeroMax);
		
		robot = new Robot(this, lechuga);
		Thread threadRobot = new Thread(robot);
		threadRobot.start();
	}

	public void insertar() {
		lock.lock();
		try {
			while (cuantos == numeroMax) {
				try {
					lleno.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			cuantos++;
			Traza.traza(6, "Metiendo elemento en el contenedor");
			vacio.signal();
		}

		finally  {
			lock.unlock();
		}
	}

	public void extraer() {
		lock.lock();
		try {
			while (cuantos == 0)
				try {
					vacio.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			cuantos--;
			Traza.traza(6, "Sacando elemento del contenedor");

			lleno.signal();
		} 
		/* Salida de la s.c. */
		finally {
			lock.unlock();
		}
	}

	//asd
	public void insertarLechuga() {
		cuantos++; // inserto
		Integer y = 1;
		try {
			listaLechugas.put(y);
			Traza.traza(6, "Insertando lechuga en el contenedor");
		} catch (InterruptedException e) {e.printStackTrace();}
	}
	public void extraerLechuga() {
		cuantos--;
		try {
			listaLechugas.take();
			Traza.traza(6, "Sacando lechuga del contenedor");
		} catch (InterruptedException e) {e.printStackTrace();}
	}

}
