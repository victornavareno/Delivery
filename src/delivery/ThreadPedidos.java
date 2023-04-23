package delivery;

public class ThreadPedidos implements Runnable{
    private Pedido pedido;
    private Restaurante restaurante;

    public ThreadPedidos(Pedido pedido, Restaurante restaurante){
        this.pedido = pedido;
        this.restaurante = restaurante;
    }

    @Override
    public void run() {
        restaurante.tramitarPedido(pedido);
    }
    
    
}
