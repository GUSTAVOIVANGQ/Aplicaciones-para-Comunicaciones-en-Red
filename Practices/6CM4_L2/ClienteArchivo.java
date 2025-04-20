import javax.swing.JFileChooser;
import java.net.*;
import java.io.*;

public class ClienteArchivo {
    public static void main(String[] args){
        Socket cl = null;
        DataOutputStream dos = null;
        
        try {
            // Se define un flujo de entrada para obtener los datos del servidor
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.printf("Escriba la dirección del servidor:");
            String host = br.readLine();
            System.out.printf("\n\nEscriba el puerto:");
            int pto = Integer.parseInt(br.readLine());
            
            // Se define el socket 
            cl = new Socket(host, pto);
            
            // Se usa un JFileChooser() para elegir múltiples archivos a enviar
            JFileChooser jf = new JFileChooser();
            jf.setMultiSelectionEnabled(true); // Habilitamos selección múltiple
            int r = jf.showOpenDialog(null);
            
            if (r == JFileChooser.APPROVE_OPTION) {
                File[] files = jf.getSelectedFiles();  // Obtenemos array de archivos
                
                // Enviamos primero la cantidad de archivos que vamos a transferir
                dos = new DataOutputStream(cl.getOutputStream());
                dos.writeInt(files.length);
                dos.flush();
                
                // Procesamos cada archivo seleccionado
                for(int i = 0; i < files.length; i++){
                    File f = files[i];
                    String archivo = f.getAbsolutePath(); //Dirección
                    String nombre = f.getName(); //Nombre
                    long tam = f.length();  //Tamaño
                    
                    System.out.println("\nPreparando archivo: " + nombre + " (" + (i+1) + " de " + files.length + ")");
                    
                    // Abrimos un nuevo flujo para leer el archivo
                    FileInputStream fis = null;
                    try {
                        // Enviamos los metadatos del archivo
                        dos.writeUTF(nombre);
                        dos.flush();               
                        dos.writeLong(tam);
                        dos.flush();
                        
                        System.out.println("Enviando archivo: " + nombre);
                        
                        // Leemos y enviamos el contenido del archivo en pequeños bloques
                        fis = new FileInputStream(archivo);
                        byte[] b = new byte[1024];
                        long enviados = 0;
                        int n;
                        
                        while (enviados < tam) {
                            // Leemos un bloque de datos
                            n = fis.read(b);
                            if (n <= 0) break;
                            
                            // Enviamos el bloque al servidor
                            dos.write(b, 0, n);
                            dos.flush();
                            
                            // Actualizamos la cantidad enviada y mostramos el progreso
                            enviados += n;
                            int porcentaje = (int)(enviados*100/tam);
                            System.out.print("Enviado: " + porcentaje + "%\r");
                            
                            // Pequeña pausa para dar tiempo al servidor de procesar
                            Thread.sleep(5);  // 5ms de pausa entre bloques
                        }
                        
                        System.out.println("\nArchivo " + (i+1) + " enviado completamente");
                        
                        // Esperamos un poco entre archivos para dar tiempo al servidor
                        // de prepararse para el siguiente archivo
                        if (i < files.length - 1) {
                            System.out.println("Esperando antes de enviar el siguiente archivo...");
                            Thread.sleep(200);  // 200ms de pausa entre archivos
                        }
                    } finally {
                        // Cerramos el flujo de entrada del archivo actual
                        if (fis != null) {
                            try { fis.close(); } catch (IOException e) { /* ignorar */ }
                        }
                    }
                } // for
                
                System.out.println("\nTodos los archivos han sido enviados.");
            } // if
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerramos los recursos al final
            try {
                if (dos != null) dos.close();
                if (cl != null && !cl.isClosed()) cl.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
}