package com.sockets.server;

import java.net.*;
import java.io.*;

public class Cliente {
    public static void main(String args[]){
        try{
            BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
            System.out.printf("Escriba la direccion del servidor: ");
            String host = br1.readLine();
            System.out.printf("\n\n Escriba el puerto: ");
            int pto = Integer.parseInt(br1.readLine());
            Socket cl = new Socket(host,pto);
            BufferedReader br2 = new BufferedReader(new InputStreamReader(cl.getInputStream()));
            String mensaje = br2.readLine();
            System.out.println("Recibimos un mensaje desde el servidor: ");
            System.out.println("Mensaje: " + mensaje);
            br1.close();
            br2.close();
            cl.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
