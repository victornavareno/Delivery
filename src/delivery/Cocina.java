package delivery;
import pcd.util.ColoresConsola;
import pcd.util.Traza;

public class Cocina {
	Restaurante r;
	Robot robot;
	Contenedor contenedorPan;
	Contenedor contenedorPollo;
	Contenedor contenedorLechuga;
	
	public Cocina (Restaurante _r) {
		r=_r;
		contenedorPan = new Contenedor(3);
		contenedorPollo = new Contenedor(1);
		contenedorLechuga = new Contenedor(2, true);
	}
	
	public void cocinar (Pedido p) {
		Traza.traza(ColoresConsola.GREEN, 4, "Cocinando el pedido: "+p.printConRetorno());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {e.printStackTrace();}

		// busco una hamburguesa de pollo entre los productos del pedido
		for(Producto producto : p.getProductos()){
			if(producto.getId().charAt(0) == '0'){
				Traza.traza(ColoresConsola.GREEN_UNDERLINED,  4, "Contiene hamburguesa de pollo con lechuga");
				contenedorPan.extraer();
				contenedorPollo.extraer();
				contenedorLechuga.extraerLechuga();
			}
		}
	}
}