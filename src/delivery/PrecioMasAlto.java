package delivery;

import java.util.*;
import java.util.concurrent.Callable;

/* Devuelve el precio mas alto de un pedido incluido en la lista */
public class PrecioMasAlto implements Callable<Double> {
    private List<Pedido> listaPedidos;

    public PrecioMasAlto(List<Pedido> listaPedidos){
        this.listaPedidos = listaPedidos;
    }
    
    @Override
    public Double call() throws Exception {
        Double precioMasAlto = 0.0;
        for (Pedido pedido : listaPedidos) {
            if(pedido.getPrecioPedido() > precioMasAlto){
                precioMasAlto = pedido.getPrecioPedido();
            }
        }
        return precioMasAlto;
    }
    
}
