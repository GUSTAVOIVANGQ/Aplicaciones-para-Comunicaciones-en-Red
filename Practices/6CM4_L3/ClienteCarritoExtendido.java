import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

// Librerias para generar PDF
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClienteCarritoExtendido {
    private static ArrayList<Producto> carrito = new ArrayList<>();

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 7000)) {
            System.out.println("Conectado al servidor.");

            // Recibir catálogo de productos
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ArrayList<Producto> catalogo = (ArrayList<Producto>) ois.readObject();
            System.out.println("Catálogo recibido:");
            mostrarCatalogo(catalogo);

            // Menú principal
            Scanner scanner = new Scanner(System.in);
            int opcion;

            do {
                System.out.println("\n--- Menú Principal ---");
                System.out.println("1. Agregar producto al carrito");
                System.out.println("2. Eliminar producto del carrito");
                System.out.println("3. Modificar cantidad en el carrito");
                System.out.println("4. Consultar carrito");
                System.out.println("5. Enviar imágenes al servidor");
                System.out.println("6. Finalizar compra y generar ticket en PDF");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea

                switch (opcion) {
                    case 1:
                        agregarProductoAlCarrito(scanner, catalogo);
                        break;
                    case 2:
                        eliminarProductoDelCarrito(scanner);
                        break;
                    case 3:
                        modificarCantidadEnCarrito(scanner);
                        break;
                    case 4:
                        consultarCarrito();
                        break;
                    case 5:
                        enviarImagenes(socket);
                        break;
                    case 6:
                        finalizarCompraYGenerarTicket();
                        generarTicketPDF();
                        break;
                    case 0:
                        System.out.println("Saliendo...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } while (opcion != 0);

        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void mostrarCatalogo(ArrayList<Producto> catalogo) {
        for (Producto producto : catalogo) {
            System.out.println(producto);
        }
    }

    private static void agregarProductoAlCarrito(Scanner scanner, ArrayList<Producto> catalogo) {
        System.out.print("Ingrese el ID del producto a agregar: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consumir el salto de línea

        for (Producto producto : catalogo) {
            if (producto.getId() == id) {
                System.out.print("Ingrese la cantidad: ");
                int cantidad = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea

                Producto productoEnCarrito = new Producto(
                        producto.getId(), producto.getNombre(), producto.getDescripcion(),
                        cantidad, producto.getPrecio(), producto.isDisponible()
                );
                carrito.add(productoEnCarrito);
                System.out.println("Producto agregado al carrito.");
                return;
            }
        }
        System.out.println("Producto no encontrado en el catálogo.");
    }

    private static void eliminarProductoDelCarrito(Scanner scanner) {
        System.out.print("Ingrese el ID del producto a eliminar del carrito: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consumir el salto de línea

        carrito.removeIf(producto -> producto.getId() == id);
        System.out.println("Producto eliminado del carrito (si existía).");
    }

    private static void modificarCantidadEnCarrito(Scanner scanner) {
        System.out.print("Ingrese el ID del producto a modificar: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consumir el salto de línea

        for (Producto producto : carrito) {
            if (producto.getId() == id) {
                System.out.print("Ingrese la nueva cantidad: ");
                int cantidad = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea
                producto.setExistencias(cantidad);
                System.out.println("Cantidad actualizada.");
                return;
            }
        }
        System.out.println("Producto no encontrado en el carrito.");
    }

    private static void consultarCarrito() {
        if (carrito.isEmpty()) {
            System.out.println("El carrito está vacío.");
            return;
        }

        System.out.println("\n--- Carrito de Compras ---");
        for (Producto producto : carrito) {
            System.out.println(producto);
        }
    }

    private static void enviarImagenes(Socket socket) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Ingrese la ruta de la imagen a enviar: ");
            String ruta = scanner.nextLine();

            File archivo = new File(ruta);
            if (!archivo.exists() || archivo.isDirectory()) {
                System.out.println("Archivo no válido.");
                return;
            }

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(1); // Número de archivos a enviar
            dos.writeUTF(archivo.getName());
            dos.writeLong(archivo.length());

            FileInputStream fis = new FileInputStream(archivo);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, n);
            }
            fis.close();
            System.out.println("Imagen enviada correctamente.");

        } catch (IOException e) {
            System.err.println("Error al enviar la imagen: " + e.getMessage());
        }
    }

    private static void finalizarCompraYGenerarTicket() {
        if (carrito.isEmpty()) {
            System.out.println("El carrito está vacío. No se puede generar un ticket.");
            return;
        }

        try (PrintWriter writer = new PrintWriter("ticket.txt")) {
            writer.println("----- TICKET DE COMPRA -----");
            double total = 0.0;

            for (Producto producto : carrito) {
                double subtotal = producto.getExistencias() * producto.getPrecio();
                writer.printf("Producto: %s | Cantidad: %d | Precio Unitario: %.2f | Subtotal: %.2f\n",
                        producto.getNombre(), producto.getExistencias(), producto.getPrecio(), subtotal);
                total += subtotal;
            }

            writer.printf("\nTOTAL: %.2f\n", total);
            System.out.println("Ticket generado en 'ticket.txt'.");
        } catch (FileNotFoundException e) {
            System.err.println("Error al generar el ticket: " + e.getMessage());
        }
    }
    private static void generarTicketPDF() {
        if (carrito.isEmpty()) {
            System.out.println("El carrito está vacío. No se puede generar un ticket.");
            return;
        }

        try {
            // Crear documento
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream("ticket.pdf"));
            document.open();

            // Añadir metadata
            document.addTitle("Ticket de Compra");
            document.addSubject("Detalles de la compra");
            document.addCreator("Sistema de Carrito de Compras");

            // Título
            Font fontTitulo = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph titulo = new Paragraph("TICKET DE COMPRA", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            
            // Fecha y hora
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Paragraph fecha = new Paragraph("Fecha: " + formatter.format(new Date()));
            fecha.setAlignment(Element.ALIGN_RIGHT);
            document.add(fecha);
            
            // Separador
            document.add(new Paragraph(" "));
            document.add(new Paragraph("---------------------------------------------------"));
            document.add(new Paragraph(" "));
            
            // Tabla para productos
            PdfPTable tabla = new PdfPTable(5); // 5 columnas
            tabla.setWidthPercentage(100);
            float[] columnWidths = {1, 3, 1, 1, 1};
            tabla.setWidths(columnWidths);
            
            // Encabezados de tabla
            Font fontHeader = new Font(Font.HELVETICA, 12, Font.BOLD);
            String[] headers = {"ID", "Producto", "Cantidad", "Precio", "Subtotal"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, fontHeader));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new Color(220, 220, 220));
                cell.setPadding(5);
                tabla.addCell(cell);
            }
            
            // Contenido de la tabla
            double total = 0.0;
            for (Producto producto : carrito) {
                double subtotal = producto.getExistencias() * producto.getPrecio();
                total += subtotal;
                
                tabla.addCell(String.valueOf(producto.getId()));
                
                PdfPCell cellNombre = new PdfPCell();
                cellNombre.addElement(new Phrase(producto.getNombre()));
                Paragraph desc = new Paragraph(producto.getDescripcion(), new Font(Font.HELVETICA, 8));
                cellNombre.addElement(desc);
                tabla.addCell(cellNombre);
                
                tabla.addCell(String.valueOf(producto.getExistencias()));
                tabla.addCell(String.format("$%.2f", producto.getPrecio()));
                tabla.addCell(String.format("$%.2f", subtotal));
            }
            
            document.add(tabla);
            
            // Total
            document.add(new Paragraph(" "));
            Paragraph totalParagraph = new Paragraph(
                "TOTAL: $" + String.format("%.2f", total),
                new Font(Font.HELVETICA, 14, Font.BOLD)
            );
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalParagraph);
            
            // Nota de agradecimiento
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            Paragraph gracias = new Paragraph("¡Gracias por su compra!");
            gracias.setAlignment(Element.ALIGN_CENTER);
            document.add(gracias);
            
            // Cerrar documento
            document.close();
            System.out.println("Ticket PDF generado en 'ticket.pdf'.");
            
        } catch (Exception e) {
            System.err.println("Error al generar el ticket PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

// Clase Producto (idéntica a la del servidor)
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

    // Getters y Setters aquí...
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