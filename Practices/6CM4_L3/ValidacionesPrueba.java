import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ValidacionesPrueba {
    private static ArrayList<Producto> carrito = new ArrayList<>();

    public static void main(String[] args) {
        // Crear catálogo de prueba
        ArrayList<Producto> catalogo = new ArrayList<>();
        catalogo.add(new Producto(1, "Laptop", "Laptop de alta gama", 5, 1500.0, true));
        catalogo.add(new Producto(2, "Smartphone", "Teléfono inteligente con cámara de 108 MP", 10, 800.0, true));
        catalogo.add(new Producto(3, "Tablet", "Tablet para uso profesional", 7, 600.0, true));
        
        System.out.println("Catálogo de productos:");
        for (Producto p : catalogo) {
            System.out.println(p);
        }
        
        // Prueba de validaciones
        Scanner scanner = new Scanner(System.in);
        
        // Probar validación 1.1 (ID del producto)
        System.out.println("\nProbando validación de ID del producto:");
        agregarProductoAlCarrito(scanner, catalogo);
        
        // Ver el carrito
        System.out.println("\nContenido del carrito después de agregar:");
        for (Producto p : carrito) {
            System.out.println(p);
        }
        
        // Probar validación 1.2 (Cantidad al modificar)
        System.out.println("\nProbando validación de cantidad al modificar:");
        if (!carrito.isEmpty()) {
            modificarCantidadEnCarrito(scanner, catalogo);
            
            // Ver el carrito modificado
            System.out.println("\nContenido del carrito después de modificar:");
            for (Producto p : carrito) {
                System.out.println(p);
            }
        }
        
        scanner.close();
    }
    
    private static void agregarProductoAlCarrito(Scanner scanner, ArrayList<Producto> catalogo) {
        int id;
        boolean idValido = false;
        
        do {
            System.out.print("Ingrese el ID del producto a agregar: ");
            try {
                if (scanner.hasNextInt()) {
                    id = scanner.nextInt();
                    if (id <= 0) {
                        System.out.println("Error: El ID debe ser un número positivo.");
                        scanner.nextLine(); // Consumir el salto de línea
                        continue;
                    }
                    
                    // Buscar el producto en el catálogo
                    boolean encontrado = false;
                    for (Producto producto : catalogo) {
                        if (producto.getId() == id) {
                            encontrado = true;
                            if (!producto.isDisponible()) {
                                System.out.println("Error: El producto no está disponible actualmente.");
                                break;
                            }
                            
                            boolean cantidadValida = false;
                            do {
                                System.out.print("Ingrese la cantidad: ");
                                try {
                                    if (scanner.hasNextInt()) {
                                        int cantidad = scanner.nextInt();
                                        scanner.nextLine(); // Consumir el salto de línea
                                        
                                        // Validar que sea un número positivo
                                        if (cantidad <= 0) {
                                            System.out.println("Error: La cantidad debe ser un número positivo.");
                                        } 
                                        // Validar que no exceda las existencias disponibles
                                        else if (cantidad > producto.getExistencias()) {
                                            System.out.println("Error: La cantidad solicitada (" + cantidad + 
                                                ") excede las existencias disponibles (" + producto.getExistencias() + ").");
                                        } 
                                        else {
                                            Producto productoEnCarrito = new Producto(
                                                    producto.getId(), producto.getNombre(), producto.getDescripcion(),
                                                    cantidad, producto.getPrecio(), producto.isDisponible()
                                            );
                                            carrito.add(productoEnCarrito);
                                            System.out.println("Producto agregado al carrito.");
                                            cantidadValida = true;
                                            idValido = true;
                                        }
                                    } else {
                                        System.out.println("Error: Debe ingresar un número entero válido para la cantidad.");
                                        scanner.next(); // Consumir la entrada inválida
                                        scanner.nextLine(); // Consumir el salto de línea
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al ingresar la cantidad: " + e.getMessage());
                                    scanner.nextLine(); // Consumir la entrada inválida
                                }
                                
                                if (!cantidadValida) {
                                    System.out.print("¿Desea intentar con otra cantidad? (S/N): ");
                                    String respuesta = scanner.next().trim().toUpperCase();
                                    scanner.nextLine(); // Consumir el salto de línea
                                    if (!respuesta.equals("S")) {
                                        break; // Salir del bucle de cantidad
                                    }
                                }
                            } while (!cantidadValida);
                            
                            if (cantidadValida) {
                                break; // Salir del bucle principal si se agregó correctamente
                            }
                        }
                    }
                    
                    if (!encontrado) {
                        System.out.println("Error: El producto con ID " + id + " no existe en el catálogo.");
                        // Mostrar los IDs disponibles para ayudar al usuario
                        System.out.println("IDs disponibles en el catálogo:");
                        for (Producto p : catalogo) {
                            System.out.println("- ID: " + p.getId() + ", Nombre: " + p.getNombre());
                        }
                    }
                } else {
                    System.out.println("Error: Debe ingresar un número entero válido.");
                    scanner.next(); // Consumir la entrada inválida
                }
            } catch (Exception e) {
                System.err.println("Error de entrada: " + e.getMessage());
                scanner.nextLine(); // Consumir la entrada inválida
            }
            
            if (!idValido) {
                System.out.print("¿Desea intentarlo de nuevo? (S/N): ");
                String respuesta = scanner.next().trim().toUpperCase();
                scanner.nextLine(); // Consumir el salto de línea
                if (!respuesta.equals("S")) {
                    return;
                }
            }
            
        } while (!idValido);
    }
    
    private static void modificarCantidadEnCarrito(Scanner scanner, ArrayList<Producto> catalogo) {
        if (carrito.isEmpty()) {
            System.out.println("El carrito está vacío. No hay productos para modificar.");
            return;
        }
        
        boolean idValido = false;
        
        do {
            System.out.print("Ingrese el ID del producto a modificar: ");
            try {
                if (scanner.hasNextInt()) {
                    final int idProducto = scanner.nextInt();
                    if (idProducto <= 0) {
                        System.out.println("Error: El ID debe ser un número positivo.");
                    } else {
                        // Buscar el producto en el carrito
                        boolean encontrado = false;
                        for (Producto producto : carrito) {
                            if (producto.getId() == idProducto) {
                                encontrado = true;
                                try {
                                    System.out.print("Ingrese la nueva cantidad: ");
                                    if (scanner.hasNextInt()) {
                                        int cantidad = scanner.nextInt();
                                        if (cantidad <= 0) {
                                            System.out.println("Error: La cantidad debe ser un número positivo.");
                                        } else {
                                            // Buscar el producto en el catálogo para verificar existencias disponibles
                                            boolean excededStock = false;
                                            for (Producto productoCatalogo : catalogo) {
                                                if (productoCatalogo.getId() == idProducto) {
                                                    if (cantidad > productoCatalogo.getExistencias()) {
                                                        System.out.println("Error: La cantidad solicitada (" + cantidad + 
                                                            ") excede las existencias disponibles (" + 
                                                            productoCatalogo.getExistencias() + ").");
                                                        excededStock = true;
                                                    }
                                                    break;
                                                }
                                            }
                                            
                                            if (!excededStock) {
                                                producto.setExistencias(cantidad);
                                                System.out.println("Cantidad actualizada con éxito.");
                                                idValido = true;
                                            }
                                        }
                                    } else {
                                        System.out.println("Error: Debe ingresar un número entero válido para la cantidad.");
                                        scanner.next(); // Consumir la entrada inválida
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error al ingresar la cantidad: " + e.getMessage());
                                    scanner.nextLine(); // Consumir la entrada inválida
                                }
                                break;
                            }
                        }
                        
                        if (!encontrado) {
                            System.out.println("Error: No existe ningún producto con ID " + idProducto + " en el carrito.");
                            // Mostrar los productos en el carrito para ayudar al usuario
                            System.out.println("Productos en el carrito:");
                            for (Producto p : carrito) {
                                System.out.println("- ID: " + p.getId() + ", Nombre: " + p.getNombre());
                            }
                        }
                    }
                } else {
                    System.out.println("Error: Debe ingresar un número entero válido para el ID.");
                    scanner.next(); // Consumir la entrada inválida
                }
            } catch (Exception e) {
                System.err.println("Error de entrada: " + e.getMessage());
                scanner.nextLine(); // Consumir la entrada inválida
            }
            
            if (!idValido) {
                scanner.nextLine(); // Consumir el salto de línea si es necesario
                System.out.print("¿Desea intentarlo de nuevo? (S/N): ");
                String respuesta = scanner.next().trim().toUpperCase();
                scanner.nextLine(); // Consumir el salto de línea
                if (!respuesta.equals("S")) {
                    return;
                }
            }
        } while (!idValido);
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
