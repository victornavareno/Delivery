package delivery;

public class Robot extends Thread {

    Contenedor contenedor;
    boolean lechuga;

    public Robot(Contenedor contenedor, boolean lechuga) {
        this.contenedor = contenedor;
        this.lechuga = lechuga;
    }

    public void run() {
        for (;;) {
            if (this.lechuga == true) {
                contenedor.insertarLechuga();
            } else contenedor.insertar();
        }
    }

}
