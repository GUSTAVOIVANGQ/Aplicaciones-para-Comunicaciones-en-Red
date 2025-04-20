import java.io.*;
import java.util.ArrayList;

public class GenerarCatalogo {
    public static void main(String[] args) {
        // Crear un catálogo de productos
        ArrayList<Producto> catalogo = new ArrayList<>();
        catalogo.add(new Producto(1, "Laptop", "Laptop de alta gama", 5, 1500.0, true));
        catalogo.add(new Producto(2, "Smartphone", "Teléfono inteligente con cámara de 108 MP", 10, 800.0, true));
        catalogo.add(new Producto(3, "Tablet", "Tablet para uso profesional", 7, 600.0, true));
        catalogo.add(new Producto(4, "Auriculares", "Auriculares inalámbricos con cancelación de ruido", 15, 200.0, true));
        catalogo.add(new Producto(5, "Monitor", "Monitor 4K UHD de 27 pulgadas", 8, 350.0, true));

        // Serializar el catálogo en un archivo
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("catalogo_productos.ser"))) {
            oos.writeObject(catalogo);
            System.out.println("Archivo 'catalogo_productos.ser' generado con éxito.");
        } catch (IOException e) {
            System.err.println("Error al generar el archivo: " + e.getMessage());
        }
    }
}

// Clase Producto
class Producto implements Serializable {
    private int id;
    private String nombre;
    private String descripcion;
    private int existencias;
    private double precio;
    private boolean disponible;

    public Producto(int id, String nombre, String descripcion, int existencias, double precio, boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.existencias = existencias;
        this.precio = precio;
        this.disponible = disponible;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getExistencias() {
        return existencias;
    }

    public void setExistencias(int existencias) {
        this.existencias = existencias;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", existencias=" + existencias +
                ", precio=" + precio +
                ", disponible=" + disponible +
                '}';
    }
}