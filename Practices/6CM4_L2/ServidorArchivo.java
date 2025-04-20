import java.net.*;
import java.io.*;

public class ServidorArchivo {
    public static void main(String[] args){
        try{
            ServerSocket s = new ServerSocket(7000);
            System.out.println("Servidor iniciado en puerto 7000");
            // Iniciamos el ciclo infinito y esperamos una conexión
            for(;;){
                Socket cl = null;
                DataInputStream dis = null;
                
                try {
                    cl = s.accept();
                    System.out.println("Conexión establecida desde "+cl.getInetAddress()+":"+cl.getPort());
                    // Definimos un flujo de nivel de bits de entrada ligado al socket
                    dis = new DataInputStream(cl.getInputStream());
                    
                    // Primero leemos cuántos archivos se enviarán
                    int numArchivos = dis.readInt();
                    System.out.println("Se recibirán " + numArchivos + " archivo(s)");
                    
                    // Procesamos cada archivo
                    int archivosRecibidos = 0;
                    for(int i = 0; i < numArchivos; i++) {
                        DataOutputStream dos = null;
                        try {
                            //Leemos los datos principales del archivo
                            byte[] b = new byte[1024];
                            String nombre = dis.readUTF();
                            System.out.println("Recibiendo el archivo: "+nombre+" ("+(i+1)+" de "+numArchivos+")");
                            long tam = dis.readLong();
                            
                            // Creamos un flujo para escribir el archivo de salida
                            dos = new DataOutputStream(new FileOutputStream(nombre));
                            
                            // Preparamos los datos para recibir los paquetes de datos del archivo
                            long recibidos=0;
                            int n, porcentaje;
                            
                            // Definimos el ciclo donde estaremos recibiendo los datos enviados por el cliente
                            while(recibidos < tam){
                                n = dis.read(b);
                                if (n <= 0) {
                                    System.out.println("Error: Fin de archivo inesperado");
                                    break;
                                }
                                
                                dos.write(b, 0, n);
                                dos.flush();
                                recibidos = recibidos + n;
                                porcentaje = (int)((recibidos * 100) / tam);
                                // Limitamos el porcentaje a 100% máximo
                                porcentaje = Math.min(100, porcentaje);
                                System.out.print("Progreso: "+porcentaje+"%\r");
                            }//While
                            
                            System.out.println("\nArchivo "+(i+1)+" recibido.");
                            archivosRecibidos++;
                        } catch (EOFException e) {
                            System.err.println("Error: Se alcanzó el fin del flujo de datos durante la recepción del archivo " + (i+1));
                            // En lugar de imprimir toda la traza, solo mostrar un mensaje más amigable
                            System.out.println("El cliente posiblemente cerró la conexión prematuramente.");
                            break; // Salimos del bucle de archivos si hay un EOF
                        } catch (IOException e) {
                            System.err.println("Error de E/S al procesar el archivo " + (i+1) + ": " + e.getMessage());
                            break;
                        } finally {
                            if (dos != null) {
                                try { dos.close(); } catch (IOException e) { /* ignorar */ }
                            }
                        }
                    }//For
                    
                    System.out.println("Procesamiento de archivos finalizado. Recibidos " + archivosRecibidos + " de " + numArchivos + " archivos.");
                } catch (Exception e) {
                    System.err.println("Error en la conexión: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Cerramos los recursos de manera segura
                    try {
                        if (dis != null) dis.close();
                        if (cl != null && !cl.isClosed()) cl.close();
                    } catch (IOException e) {
                        System.err.println("Error al cerrar recursos: " + e.getMessage());
                    }
                }
            }
        }catch(Exception e){
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }//catch
    }//main
}