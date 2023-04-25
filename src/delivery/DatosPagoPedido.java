package delivery;
import java.io.Serializable;

public class DatosPagoPedido implements Serializable {
	private static final long serialVersionUID = 1L; 
	private String id;
	private double importe;
	
	public DatosPagoPedido (String _id, double _importe) {
		id=_id;
		importe = _importe;
	}
	public String getId() {
		return id;
	}
	public double getImporte () {
		return importe;
	}
}

