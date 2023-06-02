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

	/* Variables de condicion (Cerrojos) */
	final Condition lleno = lock.newCondition();
	final Condition vacio = lock.newCondition();

	LinkedBlockingQueue<Integer> listaLechugas; // Version 6 (para lechugas)
	boolean lechuga; // True si el contenedor es de lechugas

	public Contenedor (int numeroMax) {
		this.numeroMax = numeroMax; 
		this.lechuga = false;

		robot = new Robot(this, lechuga);
		Thread threadRobot = new Thread(robot);
		threadRobot.start();
	}
	
	// Este constructor lo usamos cuando queramos crear un contenedor de lechugas. Usar LinkedBlockingQueue y boolean lechuga debe ser true
	public Contenedor (int numeroMax, boolean lechuga) {
		this.numeroMax = numeroMax; 
		this.lechuga = lechuga;
		
		listaLechugas = new LinkedBlockingQueue<Integer>(numeroMax);
		
		robot = new Robot(this, lechuga);
		Thread threadRobot = new Thread(robot);
		threadRobot.start();
	}

	// Metodo que intenta meter objetos continuamente en el contenedor mientras no este lleno
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

	// Metodo que intenta extraer continuamente objetos del contenedor mientras haya algun objeto en el container
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
		finally {
			lock.unlock();
		}
	}

	//Inserta lechugas en la linkedBlockingQueue
	public void insertarLechuga() {
		cuantos++; // inserto, entonces incremento 
		Integer y = 1;
		try {
			listaLechugas.put(y);
			Traza.traza(6, "Insertando lechuga en el contenedor");
		} catch (InterruptedException e) {e.printStackTrace();}
	}
	
	// Extrae lechugas de la linkedBlockingQueue
	public void extraerLechuga() {
		cuantos--; // extraigo, asi que decremento 
		try {
			listaLechugas.take();
			Traza.traza(6, "Sacando lechuga del contenedor");
		} catch (InterruptedException e) {e.printStackTrace();}
	}

}
