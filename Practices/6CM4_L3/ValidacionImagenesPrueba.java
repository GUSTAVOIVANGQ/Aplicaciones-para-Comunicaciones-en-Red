import java.io.*;
import java.util.Scanner;

public class ValidacionImagenesPrueba {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Prueba de validación de imágenes ===");
        
        try {
            validarImagen(scanner);
        } catch (Exception e) {
            System.err.println("Error en la prueba: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    private static void validarImagen(Scanner scanner) {
        boolean archivoValido = false;
        String ruta = "";
        File archivo = null;
        
        do {
            System.out.print("Ingrese la ruta de la imagen a enviar (o 'salir' para cancelar): ");
            ruta = scanner.nextLine().trim();
            
            // Verificar si el usuario desea cancelar
            if (ruta.equalsIgnoreCase("salir")) {
                System.out.println("Operación cancelada por el usuario.");
                return;
            }
            
            // 1. Validación de la ruta y existencia del archivo
            archivo = new File(ruta);
            if (!archivo.exists()) {
                System.out.println("Error: El archivo no existe.");
                continue;
            }
            
            if (archivo.isDirectory()) {
                System.out.println("Error: La ruta especificada es un directorio, no un archivo.");
                continue;
            }
            
            // 2. Validación del formato del archivo
            String nombreArchivo = archivo.getName().toLowerCase();
            if (!nombreArchivo.endsWith(".jpg") && !nombreArchivo.endsWith(".jpeg") && 
                !nombreArchivo.endsWith(".png") && !nombreArchivo.endsWith(".gif") && 
                !nombreArchivo.endsWith(".bmp")) {
                System.out.println("Error: El archivo debe tener una extensión de imagen válida (.jpg, .jpeg, .png, .gif, .bmp).");
                continue;
            }
            
            // 3. Validación de archivo no vacío
            if (archivo.length() == 0) {
                System.out.println("Error: El archivo está vacío.");
                continue;
            }
            
            // Validación adicional: Verificar que el archivo sea realmente una imagen
            try {
                FileInputStream testInput = new FileInputStream(archivo);
                byte[] header = new byte[8]; // Leemos solo unos bytes para verificar la "firma" del archivo
                int bytesRead = testInput.read(header);
                testInput.close();
                
                if (bytesRead < 4) {
                    System.out.println("Error: El archivo es demasiado pequeño para ser una imagen válida.");
                    continue;
                }
                
                // Verificación básica de firmas de archivo para formatos comunes
                boolean formatoValido = false;
                
                // JPEG: comienza con FF D8 FF
                if (header[0] == (byte)0xFF && header[1] == (byte)0xD8 && header[2] == (byte)0xFF) {
                    formatoValido = true;
                    System.out.println("Formato detectado: JPEG");
                }
                // PNG: comienza con 89 50 4E 47 (‰PNG)
                else if (header[0] == (byte)0x89 && header[1] == (byte)0x50 && 
                        header[2] == (byte)0x4E && header[3] == (byte)0x47) {
                    formatoValido = true;
                    System.out.println("Formato detectado: PNG");
                }
                // GIF: comienza con 47 49 46 38 (GIF8)
                else if (header[0] == (byte)0x47 && header[1] == (byte)0x49 && 
                        header[2] == (byte)0x46 && header[3] == (byte)0x38) {
                    formatoValido = true;
                    System.out.println("Formato detectado: GIF");
                }
                // BMP: comienza con 42 4D (BM)
                else if (header[0] == (byte)0x42 && header[1] == (byte)0x4D) {
                    formatoValido = true;
                    System.out.println("Formato detectado: BMP");
                }
                
                if (!formatoValido) {
                    System.out.println("Error: El archivo no parece ser una imagen válida a pesar de su extensión.");
                    
                    System.out.println("Bytes de cabecera: ");
                    for (int i = 0; i < bytesRead; i++) {
                        System.out.printf("%02X ", header[i]);
                    }
                    System.out.println();
                    
                    continue;
                }
            } catch (IOException e) {
                System.err.println("Error al leer el archivo para verificación: " + e.getMessage());
                continue;
            }
            
            archivoValido = true;
            System.out.println("¡Imagen válida!");
            System.out.println("Nombre del archivo: " + archivo.getName());
            System.out.println("Tamaño: " + archivo.length() + " bytes (" + (archivo.length() / 1024.0) + " KB)");
            System.out.println("Ruta absoluta: " + archivo.getAbsolutePath());
            
        } while (!archivoValido);
        
        System.out.println("Validación completada con éxito.");
    }
}
