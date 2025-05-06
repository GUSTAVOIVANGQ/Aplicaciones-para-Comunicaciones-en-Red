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
                System.out.print("Seleccione una opción: ");                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea
                
                switch (opcion) {
                    case 1:
                        agregarProductoAlCarrito(scanner, catalogo);
                        System.out.println("\n--- Carrito Actualizado ---");
                        consultarCarrito(); // Mostrar el carrito actualizado
                        System.out.println("\n--- Catálogo Actualizado ---");
                        mostrarCatalogo(catalogo);
                        break;                    case 2:
                        eliminarProductoDelCarrito(scanner, catalogo);
                        System.out.println("\n--- Carrito Actualizado ---");
                        consultarCarrito(); // Mostrar el carrito actualizado
                        System.out.println("\n--- Catálogo Actualizado ---");
                        mostrarCatalogo(catalogo);
                        break;
                    case 3:
                        modificarCantidadEnCarrito(scanner, catalogo);
                        System.out.println("\n--- Carrito Actualizado ---");
                        consultarCarrito(); // Mostrar el carrito actualizado
                        System.out.println("\n--- Catálogo Actualizado ---");
                        mostrarCatalogo(catalogo);
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
        System.out.println("\n--- Catálogo de Productos ---");
        System.out.println("+-------+---------------------+-----------------------------+------------+---------+---------------+");
        System.out.println("| ID    | Nombre              | Descripción                 | Existencias | Precio  | Disponible    |");
        System.out.println("+-------+---------------------+-----------------------------+------------+---------+---------------+");
        
        for (Producto producto : catalogo) {
            System.out.printf("| %-5d | %-19s | %-27s | %-10d | $%-6.2f | %-13s |\n",
                    producto.getId(),
                    limitarTexto(producto.getNombre(), 19),
                    limitarTexto(producto.getDescripcion(), 27),
                    producto.getExistencias(),
                    producto.getPrecio(),
                    producto.isDisponible() ? "Sí" : "No");
        }
        
        System.out.println("+-------+---------------------+-----------------------------+------------+---------+---------------+");
    }private static void agregarProductoAlCarrito(Scanner scanner, ArrayList<Producto> catalogo) {
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
                                        }                                        else {
                                            Producto productoEnCarrito = new Producto(
                                                    producto.getId(), producto.getNombre(), producto.getDescripcion(),
                                                    cantidad, producto.getPrecio(), producto.isDisponible()
                                            );
                                            carrito.add(productoEnCarrito);
                                            
                                            // Actualizar las existencias en el catálogo
                                            int nuevasExistencias = producto.getExistencias() - cantidad;
                                            producto.setExistencias(nuevasExistencias);
                                            
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
    }    private static void eliminarProductoDelCarrito(Scanner scanner, ArrayList<Producto> catalogo) {
        if (carrito.isEmpty()) {
            System.out.println("El carrito está vacío. No hay productos para eliminar.");
            return;
        }
        
        boolean idValido = false;
        
        do {
            System.out.print("Ingrese el ID del producto a eliminar del carrito: ");
            try {
                if (scanner.hasNextInt()) {
                    final int idProducto = scanner.nextInt();
                    if (idProducto <= 0) {
                        System.out.println("Error: El ID debe ser un número positivo.");
                    } else {
                        // Verificar si el producto existe en el carrito
                        boolean existeEnCarrito = false;
                        for (Producto producto : carrito) {
                            if (producto.getId() == idProducto) {
                                existeEnCarrito = true;
                                break;
                            }
                        }
                          if (existeEnCarrito) {
                            // Obtener la cantidad del producto en el carrito antes de eliminarlo
                            int cantidadEnCarrito = 0;
                            for (Producto p : carrito) {
                                if (p.getId() == idProducto) {
                                    cantidadEnCarrito += p.getExistencias();
                                }
                            }
                            
                            // Devolver la cantidad al catálogo
                            for (Producto p : catalogo) {
                                if (p.getId() == idProducto) {
                                    p.setExistencias(p.getExistencias() + cantidadEnCarrito);
                                    break;
                                }
                            }
                            
                            // Eliminar el producto del carrito
                            carrito.removeIf(producto -> producto.getId() == idProducto);
                            System.out.println("Producto con ID " + idProducto + " eliminado del carrito.");
                            idValido = true;
                        } else {
                            System.out.println("Error: No existe ningún producto con ID " + idProducto + " en el carrito.");
                            // Mostrar los productos en el carrito para ayudar al usuario
                            System.out.println("Productos en el carrito:");
                            for (Producto p : carrito) {
                                System.out.println("- ID: " + p.getId() + ", Nombre: " + p.getNombre());
                            }
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
                scanner.nextLine(); // Consumir el salto de línea si es necesario
                System.out.print("¿Desea intentarlo de nuevo? (S/N): ");
                String respuesta = scanner.next().trim().toUpperCase();
                scanner.nextLine(); // Consumir el salto de línea
                if (!respuesta.equals("S")) {
                    return;
                }
            }
        } while (!idValido);
    }    private static void modificarCantidadEnCarrito(Scanner scanner, ArrayList<Producto> catalogo) {
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
                                try {                                    System.out.print("Ingrese la nueva cantidad: ");
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
                                                // Calcular la diferencia de cantidad
                                                int cantidadAnterior = producto.getExistencias();
                                                int diferencia = cantidad - cantidadAnterior;
                                                
                                                // Actualizar la cantidad en el carrito
                                                producto.setExistencias(cantidad);
                                                
                                                // Actualizar el catálogo: restar la diferencia adicional o sumar si la cantidad disminuyó
                                                for (Producto p : catalogo) {
                                                    if (p.getId() == producto.getId()) {
                                                        p.setExistencias(p.getExistencias() - diferencia);
                                                        break;
                                                    }
                                                }
                                                
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

    private static void consultarCarrito() {
        if (carrito.isEmpty()) {
            System.out.println("El carrito está vacío.");
            return;
        }

        System.out.println("\n--- Carrito de Compras ---");
        System.out.println("+-------+---------------------+-----------------------------+------------+---------+------------+");
        System.out.println("| ID    | Nombre              | Descripción                 | Cantidad   | Precio  | Subtotal   |");
        System.out.println("+-------+---------------------+-----------------------------+------------+---------+------------+");
        
        double total = 0.0;
        for (Producto producto : carrito) {
            double subtotal = producto.getExistencias() * producto.getPrecio();
            total += subtotal;
            
            System.out.printf("| %-5d | %-19s | %-27s | %-10d | $%-6.2f | $%-10.2f |\n",
                    producto.getId(),
                    limitarTexto(producto.getNombre(), 19),
                    limitarTexto(producto.getDescripcion(), 27),
                    producto.getExistencias(),
                    producto.getPrecio(),
                    subtotal);
        }
        
        System.out.println("+-------+---------------------+-----------------------------+------------+---------+------------+");
        System.out.printf("| %63s | Total:    | $%-10.2f |\n", "", total);
        System.out.println("+-----------------------------------------------------------------------+---------+------------+");
    }
    
    private static String limitarTexto(String texto, int longitudMaxima) {
        if (texto == null) {
            return "";
        }
        if (texto.length() <= longitudMaxima) {
            return texto;
        } else {
            return texto.substring(0, longitudMaxima - 3) + "...";
        }
    }private static void enviarImagenes(Socket socket) {
        try {
            Scanner scanner = new Scanner(System.in);
            ArrayList<File> imagenesValidas = new ArrayList<>();
            boolean continuarEnviando = true;
            
            System.out.println("\n--- Envío de Imágenes al Servidor ---");
            System.out.println("Puede enviar múltiples imágenes. Ingrese 'finalizar' cuando termine.");
            
            while (continuarEnviando) {
                boolean archivoValido = false;
                String ruta = "";
                File archivo = null;
                
                do {
                    System.out.print("\nIngrese la ruta de la imagen a enviar (o 'salir' para cancelar, 'finalizar' para terminar): ");
                    ruta = scanner.nextLine().trim();
                    
                    // Verificar si el usuario desea cancelar o finalizar
                    if (ruta.equalsIgnoreCase("salir")) {
                        System.out.println("Operación cancelada por el usuario.");
                        return;
                    }
                    
                    if (ruta.equalsIgnoreCase("finalizar")) {
                        if (imagenesValidas.isEmpty()) {
                            System.out.println("No ha seleccionado ninguna imagen para enviar.");
                            System.out.print("¿Desea intentar agregar una imagen? (S/N): ");
                            String respuesta = scanner.nextLine().trim().toUpperCase();
                            if (respuesta.equals("S")) {
                                continue;
                            } else {
                                return;
                            }
                        } else {
                            continuarEnviando = false;
                            break;
                        }
                    }
                    
                    // 1. Validación de la ruta y existencia del archivo
                    archivo = new File(ruta);
                    if (!archivo.exists()) {
                        System.out.println("Error: El archivo no existe.");
                        continue;
                    }
                    
                    if (archivo.isDirectory()) {
                        System.out.println("Error: La ruta especificada es un directorio, no un archivo.");
                        continue;
                    }
                    
                    // 2. Validación del formato del archivo
                    String nombreArchivo = archivo.getName().toLowerCase();
                    if (!nombreArchivo.endsWith(".jpg") && !nombreArchivo.endsWith(".jpeg") && 
                        !nombreArchivo.endsWith(".png") && !nombreArchivo.endsWith(".gif") && 
                        !nombreArchivo.endsWith(".bmp")) {
                        System.out.println("Error: El archivo debe tener una extensión de imagen válida (.jpg, .jpeg, .png, .gif, .bmp).");
                        continue;
                    }
                    
                    // 3. Validación de archivo no vacío
                    if (archivo.length() == 0) {
                        System.out.println("Error: El archivo está vacío.");
                        continue;
                    }
                    
                    // Validación adicional: Verificar que el archivo sea realmente una imagen
                    try {
                        FileInputStream testInput = new FileInputStream(archivo);
                        byte[] header = new byte[8]; // Leemos solo unos bytes para verificar la "firma" del archivo
                        int bytesRead = testInput.read(header);
                        testInput.close();
                        
                        if (bytesRead < 4) {
                            System.out.println("Error: El archivo es demasiado pequeño para ser una imagen válida.");
                            continue;
                        }
                        
                        // Verificación básica de firmas de archivo para formatos comunes
                        boolean formatoValido = false;
                        
                        // JPEG: comienza con FF D8 FF
                        if (header[0] == (byte)0xFF && header[1] == (byte)0xD8 && header[2] == (byte)0xFF) {
                            formatoValido = true;
                        }
                        // PNG: comienza con 89 50 4E 47 (‰PNG)
                        else if (header[0] == (byte)0x89 && header[1] == (byte)0x50 && 
                                header[2] == (byte)0x4E && header[3] == (byte)0x47) {
                            formatoValido = true;
                        }
                        // GIF: comienza con 47 49 46 38 (GIF8)
                        else if (header[0] == (byte)0x47 && header[1] == (byte)0x49 && 
                                header[2] == (byte)0x46 && header[3] == (byte)0x38) {
                            formatoValido = true;
                        }
                        // BMP: comienza con 42 4D (BM)
                        else if (header[0] == (byte)0x42 && header[1] == (byte)0x4D) {
                            formatoValido = true;
                        }
                        
                        if (!formatoValido) {
                            System.out.println("Error: El archivo no parece ser una imagen válida a pesar de su extensión.");
                            continue;
                        }
                    } catch (IOException e) {
                        System.err.println("Error al leer el archivo para verificación: " + e.getMessage());
                        continue;
                    }
                    
                    archivoValido = true;
                    imagenesValidas.add(archivo);
                    System.out.println("Imagen '" + archivo.getName() + "' validada y agregada a la cola de envío.");
                    
                } while (!archivoValido);
            }
            
            // Enviar todas las imágenes validadas
            if (!imagenesValidas.isEmpty()) {
                System.out.println("\nEnviando " + imagenesValidas.size() + " imagen(es) al servidor...");
                
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(imagenesValidas.size()); // Número de archivos a enviar
                
                for (File imagen : imagenesValidas) {
                    System.out.println("\nEnviando imagen: " + imagen.getName() + " (" + (imagen.length() / 1024) + " KB)");
                    dos.writeUTF(imagen.getName());
                    dos.writeLong(imagen.length());
    
                    try (FileInputStream fis = new FileInputStream(imagen)) {
                        byte[] buffer = new byte[1024];
                        int n;
                        long bytesEnviados = 0;
                        long totalBytes = imagen.length();
                        
                        while ((n = fis.read(buffer)) > 0) {
                            dos.write(buffer, 0, n);
                            bytesEnviados += n;
                            
                            // Mostrar progreso
                            int porcentaje = (int)((bytesEnviados * 100) / totalBytes);
                            System.out.print("\rProgreso: " + porcentaje + "% completado");
                        }
                        System.out.println("\nImagen enviada correctamente.");
                    }
                }
                System.out.println("\nTodas las imágenes han sido enviadas correctamente.");
            }
            
        } catch (IOException e) {
            System.err.println("Error al enviar las imágenes: " + e.getMessage());
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