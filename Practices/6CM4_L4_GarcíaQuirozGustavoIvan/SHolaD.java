import java.net.*;
import java.io.*;
import java.util.*;

public class SHolaD {
    // Tamaño máximo del buffer para cada datagrama
    private static final int BUFFER_SIZE = 8; // Cambiado de 1024 a 8 para pruebas
    // Número máximo de datagramas para un solo mensaje
    private static final int MAX_PACKETS = 100;
    // Marca de fin de mensaje
    private static final String END_MARKER = "##END##";

    public static void main(String[] args) {
        try {
            // Creamos un socket de datagrama que escucha en el puerto 2000
            DatagramSocket s = new DatagramSocket(2000);
            System.out.println("Servidor iniciado, esperando cliente");
            
            // Mantenemos el servidor activo indefinidamente
            for (;;) {
                // Preparamos para recibir mensajes
                InetAddress clientAddress = null;
                int clientPort = 0;
                StringBuilder messageBuilder = new StringBuilder();
                boolean endOfMessage = false;
                
                // Recibimos todos los fragmentos del mensaje
                while (!endOfMessage) {
                    // Preparamos un buffer para recibir datos
                    byte[] receiveBuffer = new byte[BUFFER_SIZE];
                    DatagramPacket p = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    
                    // Esperamos a recibir un datagrama
                    s.receive(p);
                    
                    // Guardamos la dirección y puerto del cliente para responder después
                    clientAddress = p.getAddress();
                    clientPort = p.getPort();
                    
                    // Convertimos los bytes recibidos a String
                    String fragmento = new String(p.getData(), 0, p.getLength());
                    
                    // Verificamos si es el último fragmento del mensaje
                    if (fragmento.endsWith(END_MARKER)) {
                        // Eliminamos el marcador de final y agregamos el contenido
                        fragmento = fragmento.substring(0, fragmento.length() - END_MARKER.length());
                        endOfMessage = true;
                    }
                    
                    // Agregamos el fragmento al mensaje completo
                    messageBuilder.append(fragmento);
                }
                
                // Mensaje completo recibido
                String mensajeCompleto = messageBuilder.toString();
                System.out.println("Datagrama recibido desde " + clientAddress + ":" + clientPort);
                System.out.println("Con el mensaje: " + mensajeCompleto);
                
                // Preparamos para enviar respuesta
                System.out.print("Servidor: Escriba un mensaje de respuesta: ");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String respuesta = br.readLine();
                  // Enviamos la respuesta, posiblemente en múltiples fragmentos
                enviarMensaje(s, respuesta, clientAddress, clientPort);
                
                // El servidor continuará recibiendo mensajes automáticamente
                System.out.println("Esperando nuevo mensaje...");
                  }
            
            // Este código nunca se alcanzará, pero lo mantenemos por completitud
            // s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Método para enviar un mensaje grande dividiéndolo en múltiples datagramas si es necesario
     * @param socket Socket de datagrama para enviar
     * @param mensaje Mensaje completo a enviar
     * @param destino Dirección IP de destino
     * @param puerto Puerto de destino
     * @throws IOException Si ocurre un error al enviar
     */
    private static void enviarMensaje(DatagramSocket socket, String mensaje, InetAddress destino, int puerto) 
            throws IOException {
        // Convertimos el mensaje a bytes
        byte[] mensajeBytes = mensaje.getBytes();
        int longitudMensaje = mensajeBytes.length;
        
        // Calculamos cuántos fragmentos necesitamos
        int numFragmentos = (int) Math.ceil((double) longitudMensaje / (BUFFER_SIZE - END_MARKER.length()));
        
        // Verificamos que no exceda el número máximo de fragmentos
        if (numFragmentos > MAX_PACKETS) {
            System.out.println("Error: El mensaje es demasiado grande");
            return;
        }
        
        // Enviamos los fragmentos uno por uno
        for (int i = 0; i < numFragmentos; i++) {
            // Calculamos posición inicial y final del fragmento actual
            int inicio = i * (BUFFER_SIZE - END_MARKER.length());
            int fin = Math.min(inicio + (BUFFER_SIZE - END_MARKER.length()), longitudMensaje);
            int longitudFragmento = fin - inicio;
            
            // Preparamos el buffer para este fragmento
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Copiamos la parte correspondiente del mensaje
            outputStream.write(mensajeBytes, inicio, longitudFragmento);
            
            // Si es el último fragmento, agregamos marcador de fin
            if (i == numFragmentos - 1) {
                outputStream.write(END_MARKER.getBytes());
            }
            
            // Creamos y enviamos el paquete
            byte[] fragmentoBytes = outputStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(
                fragmentoBytes, 
                fragmentoBytes.length, 
                destino, 
                puerto
            );
            socket.send(packet);
            
            // Pequeña pausa para evitar saturación
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("Mensaje enviado en " + numFragmentos + " fragmentos");
    }
}
