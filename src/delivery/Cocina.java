package delivery;
import pcd.util.ColoresConsola;
import pcd.util.Traza;

public class Cocina {
	Restaurante r;
	
	public Cocina (Restaurante _r) {
		r=_r;
	}
	
	public void cocinar (Pedido p) {
		Traza.traza(ColoresConsola.GREEN, 4, "Cocinando el pedido: "+p.printConRetorno());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}