import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ServidorCarritoExtendido {
    private static final String CATALOGO_FILE = "catalogo_productos.ser";
    private static final String IMAGES_DIR = "imagenes/";

    public static void main(String[] args) {
        ArrayList<Producto> catalogo = cargarCatalogo();

        try (ServerSocket serverSocket = new ServerSocket(7000)) {
            System.out.println("Servidor iniciado en el puerto 7000...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Cliente conectado desde " + clientSocket.getInetAddress());

                    // Enviar catálogo al cliente
                    enviarCatalogo(clientSocket, catalogo);

                    // Recibir archivos (productos o imágenes)
                    recibirArchivos(clientSocket);

                } catch (Exception e) {
                    System.err.println("Error al manejar la conexión: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    private static ArrayList<Producto> cargarCatalogo() {
        ArrayList<Producto> catalogo = new ArrayList<>();
        File file = new File(CATALOGO_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                catalogo = (ArrayList<Producto>) ois.readObject();
                System.out.println("Catálogo cargado con éxito.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error al cargar el catálogo: " + e.getMessage());
            }
        } else {
            System.out.println("No se encontró un catálogo existente. Se creará uno nuevo.");
        }
        return catalogo;
    }

    private static void guardarCatalogo(ArrayList<Producto> catalogo) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CATALOGO_FILE))) {
            oos.writeObject(catalogo);
            System.out.println("Catálogo guardado con éxito.");
        } catch (IOException e) {
            System.err.println("Error al guardar el catálogo: " + e.getMessage());
        }
    }

    private static void enviarCatalogo(Socket clientSocket, ArrayList<Producto> catalogo) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        oos.writeObject(catalogo);
        oos.flush();
        System.out.println("Catálogo de productos enviado al cliente.");
    }

    private static void recibirArchivos(Socket clientSocket) throws IOException {
        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
        int numArchivos = dis.readInt();
        System.out.println("Se recibirán " + numArchivos + " archivo(s).");

        for (int i = 0; i < numArchivos; i++) {
            String nombreArchivo = dis.readUTF();
            long tamArchivo = dis.readLong();
            System.out.println("Recibiendo archivo: " + nombreArchivo + " (" + (i + 1) + " de " + numArchivos + ")");

            File archivoSalida;
            if (nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".png")) {
                File dir = new File(IMAGES_DIR);
                if (!dir.exists()) dir.mkdir();
                archivoSalida = new File(IMAGES_DIR + nombreArchivo);
            } else {
                archivoSalida = new File(nombreArchivo);
            }

            try (FileOutputStream fos = new FileOutputStream(archivoSalida)) {
                byte[] buffer = new byte[1024];
                long recibido = 0;
                int n;
                while (recibido < tamArchivo) {
                    n = dis.read(buffer);
                    fos.write(buffer, 0, n);
                    recibido += n;
                }
                System.out.println("Archivo " + nombreArchivo + " recibido correctamente.");
            }
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