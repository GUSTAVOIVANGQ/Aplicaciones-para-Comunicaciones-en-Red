import java.net.*;
import java.io.*;

public class Cliente {
    public static void main(String args[]){
        try{
            BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
            
            System.out.printf("Escriba la dirección del servidor: ");
            String host = br1.readLine();
            System.out.printf("\n\nEscriba el puerto: ");
            int pto = Integer.parseInt(br1.readLine());
            
            Socket cl = new Socket(host, pto);
            
            BufferedReader br2 = new BufferedReader(new InputStreamReader(cl.getInputStream()));
            
            String mensaje = br2.readLine();
            System.out.println("Recibimos un mensaje desde el servidor");
            System.out.println("Mensaje: "+mensaje);
            
            // Enviar eco del mensaje al servidor
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
            mensaje = "Nombre: García Quiroz Gustavo Ivan, Grupo: 6CM4, Materia: Aplicaciones para Comunicaciones en Red";
            pw.println(mensaje);
            pw.flush();
            System.out.println("Eco enviado al servidor: " + mensaje);
            
            pw.close();
            br1.close();
            br2.close();
            cl.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}