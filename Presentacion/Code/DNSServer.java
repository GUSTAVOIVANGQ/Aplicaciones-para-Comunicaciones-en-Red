import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Servidor DNS básico simulado.
 * Escucha peticiones de clientes, recibe un nombre de dominio y responde con una IP predefinida.
 */
public class DNSServer {
    public static void main(String[] args) throws IOException {
        int port = 5353; // Puerto donde el servidor escuchará (53 es el estándar DNS, 5353 es alternativo para pruebas)
        ServerSocket serverSocket = new ServerSocket(port); // Crea el socket del servidor
        System.out.println("DNS Server iniciado en el puerto " + port);

        // Tabla DNS simulada: asocia nombres de dominio con direcciones IP
        Map<String, String> dnsTable = new HashMap<>();
        dnsTable.put("www.ejemplo.com", "192.168.1.10");
        dnsTable.put("www.google.com", "8.8.8.8");
        dnsTable.put("www.github.com", "140.82.112.3");

        // Bucle infinito: el servidor siempre está esperando conexiones de clientes
        while (true) {
            // Espera y acepta la conexión de un cliente
            Socket clientSocket = serverSocket.accept();

            // Prepara para leer y escribir datos con el cliente
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Recibe el nombre de dominio enviado por el cliente
            String domain = in.readLine();
            System.out.println("Consulta recibida: " + domain);

            // Busca la IP correspondiente al dominio en la tabla, o responde "Dominio no encontrado"
            String ip = dnsTable.getOrDefault(domain, "Dominio no encontrado");
            out.println(ip); // Envía la IP (o mensaje de error) al cliente

            clientSocket.close(); // Cierra la conexión con el cliente
        }
    }
}
