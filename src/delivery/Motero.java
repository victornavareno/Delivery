package delivery;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import pcd.util.Traza;

public class Motero extends Thread{

    private int idMotero;
    private ControlMoteros controlMoteros;
    private CyclicBarrier cyclicBarrier;

    public Motero(int idMotero, ControlMoteros controlMoteros, CyclicBarrier cyclicBarrier){
        this.idMotero = idMotero;
        this.controlMoteros = controlMoteros;
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        while(true){
            try {
                Traza.traza(6, "Esperando a que todos los moteros esten disponibles");
                cyclicBarrier.await(); // espero a que todos los moteros esten disponibles
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
            controlMoteros.repartirPedido(idMotero, controlMoteros.getPedido());
            controlMoteros.regresarMotero();
        }
    }
}
