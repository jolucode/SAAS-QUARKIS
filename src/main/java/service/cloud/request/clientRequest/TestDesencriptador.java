package service.cloud.request.clientRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.apache.commons.codec.binary.Base64;

public class TestDesencriptador {

    /*public static void main(String[] args) {
        // Reemplaza esto con tu valor encriptado real
        String textoEncriptado = "ZnNXXVJyX11WW1M2VEZCQg==";

        String desencriptado = Criptor.Desencriptar(textoEncriptado);
        System.out.println("Texto desencriptado: " + desencriptado);
    }*/

    public static class Criptor {
        private static final int shift = 5;

        public static String Encriptar(String mensaje) {
            String msj = "";
            mensaje = Base64.encodeBase64String(mensaje.getBytes());
            for (int i = 0; i < mensaje.length(); i++) {
                msj += String.valueOf((char) (((int) mensaje.charAt(i)) + shift));
            }
            msj = Base64.encodeBase64String(msj.getBytes());
            return msj;
        }

        public static String Desencriptar(String mensaje) {
            String msj = "";
            try {
                mensaje = new String(Base64.decodeBase64(mensaje.getBytes()));
                for (int i = 0; i < mensaje.length(); i++) {
                    msj += String.valueOf((char) (((int) mensaje.charAt(i)) - shift));
                }
                msj = new String(Base64.decodeBase64(msj.getBytes()));
            } catch (Exception ex) {
                return mensaje;
            }
            return msj;
        }
    }
}

