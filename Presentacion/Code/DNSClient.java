import java.net.*;
import java.io.*;

/**
 * Cliente DNS básico.
 * Solicita al usuario un nombre de dominio, lo envía al servidor y muestra la IP recibida.
 */
public class DNSClient {
    public static void main(String[] args) throws IOException {
        String server = "localhost"; // Dirección del servidor (puede ser "localhost" para pruebas locales)
        int port = 5353; // Puerto del servidor (debe coincidir con el del servidor DNS)

        // Solicita al usuario el nombre de dominio a consultar
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Ingrese el dominio a consultar: ");
        String domain = reader.readLine();

        // Conecta con el servidor DNS usando sockets
        Socket socket = new Socket(server, port);

        // Prepara los flujos para enviar y recibir datos del servidor
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.println(domain); // Envía el dominio al servidor

        // Espera y recibe la respuesta del servidor (dirección IP o mensaje de error)
        String response = in.readLine();
        System.out.println("Respuesta del servidor DNS: " + response);

        socket.close(); // Cierra la conexión
    }
}
