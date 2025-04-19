import java.net.*;
import java.io.*;

public class SHola {
    public static void main(String args[]){
        try{
            ServerSocket s = new ServerSocket(1234);
            System.out.println("Esperando cliente ...");
            
            for(;;){
                Socket cl = s.accept();
                System.out.println("Conexión establecida desde " + 
                                  cl.getInetAddress()+":"+cl.getPort());
                
                String mensaje ="Hola mundo";
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                pw.println(mensaje);
                pw.flush();
                
                // Recibir el eco desde el cliente
                BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                String ecoRecibido = br.readLine();
                System.out.println("El cliente ha devuelto el eco: " + ecoRecibido);
                
                br.close();
                pw.close();
                cl.close();
            }//for
        }catch(Exception e){
            e.printStackTrace();
        }//catch
    }//main
}