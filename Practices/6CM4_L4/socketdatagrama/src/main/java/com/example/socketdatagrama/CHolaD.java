package com.example.socketdatagrama;

import java.net.*;
import java.io.*;

public class CHolaD {
    // Tamaño máximo del buffer para cada datagrama
    private static final int BUFFER_SIZE = 1024;
    // Número máximo de datagramas para un solo mensaje
    private static final int MAX_PACKETS = 100;
    // Marca de fin de mensaje
    private static final String END_MARKER = "##END##";
    
    public static void main(String[] args){
        try{
            // Creamos el socket de datagrama del cliente
            DatagramSocket cl = new DatagramSocket();
            do{
                // Solicitar mensaje al usuario
                System.out.print("Cliente iniciado, escriba un mensaje de saludo: ");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String mensaje = br.readLine();
                
                // Enviar el mensaje al servidor, posiblemente en múltiples fragmentos
                enviarMensaje(cl, mensaje, InetAddress.getByName("127.0.0.1"), 2000);
                
                // Recibir respuesta del servidor
                System.out.println("Esperando respuesta del servidor...");
                StringBuilder messageBuilder = new StringBuilder();
                boolean endOfMessage = false;
                
                // Recibimos todos los fragmentos de la respuesta
                while (!endOfMessage) {
                    // Preparamos buffer para recibir datos
                    byte[] receiveBuffer = new byte[BUFFER_SIZE];
                    DatagramPacket p = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    
                    // Configuramos un tiempo límite para la recepción (5 segundos)
                    cl.setSoTimeout(5000);
                    
                    try {
                        // Esperamos recibir un datagrama
                        cl.receive(p);
                        
                        // Convertimos los bytes recibidos a String
                        String fragmento = new String(p.getData(), 0, p.getLength());
                        
                        // Verificamos si es el último fragmento del mensaje
                        if (fragmento.endsWith(END_MARKER)) {
                            // Eliminamos el marcador de final
                            fragmento = fragmento.substring(0, fragmento.length() - END_MARKER.length());
                            endOfMessage = true;
                        }
                        
                        // Agregamos el fragmento al mensaje completo
                        messageBuilder.append(fragmento);
                    } catch (SocketTimeoutException e) {
                        // Si pasa el tiempo límite sin recibir nada, asumimos que es el fin del mensaje
                        System.out.println("Tiempo de espera agotado, no hay más fragmentos");
                        endOfMessage = true;
                    }
                }
                
                // Restauramos el timeout a infinito
                cl.setSoTimeout(0);
                
                // Mostramos el mensaje recibido
                if (messageBuilder.length() > 0) {
                    System.out.println("Respuesta del servidor: " + messageBuilder.toString());
                } else {
                    System.out.println("No se recibió respuesta del servidor");
                }
                
                // Preguntamos si queremos enviar otro mensaje
                System.out.println("¿Desea enviar otro mensaje? (s/n)");
                String resp = br.readLine();
                if(resp.equals("n")){
                    break;
                }
            } while(true);
            
            // Cerramos el socket
            cl.close();
        } catch(Exception e){
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
