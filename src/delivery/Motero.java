package delivery;

public class Motero extends Thread{

    private int idMotero;
    private ControlMoteros controlMoteros;

    public Motero(int idMotero, ControlMoteros controlMoteros){
        this.idMotero = idMotero;
        this.controlMoteros = controlMoteros;
    }

    @Override
    public void run() {
        while(true){
            controlMoteros.repartirPedido(idMotero, controlMoteros.getPedido());
            controlMoteros.regresarMotero();
        }
    }
}
