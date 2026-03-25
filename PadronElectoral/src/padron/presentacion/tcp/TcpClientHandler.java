package padron.presentacion.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import padron.dto.FormatoSalida;
import padron.dto.RespuestaPadron;
import padron.dto.SolicitudPadron;
import padron.logica.ServicioPadron;
import padron.util.Serializador;

/**
 * m,aneja una conexion tcp con un unico cliente.
 *
 * protocolo (una solicitud por linea hasta bye o cierre del socket):
 *   → get|123456789|json
 *   ← {"ok":true,"persona":{...},"direccion":{...}}
 *
 *   → get|123456789|xml
 *   ← <respuesta ok="true">...</respuesta>
 *
 *   → BYE
 *   ← (servidor cierra la conexion)
 *
 * errores de protocolo o de negocio se devuelven en el mismo
 * formato que la respuesta normal (ok: false / ok = "false").
 */
public class TcpClientHandler implements Runnable {

    private final Socket        cliente;
    private final ServicioPadron servicio;

    public TcpClientHandler(Socket cliente, ServicioPadron servicio) {
        this.cliente  = cliente;
        this.servicio = servicio;
    }

    // Runnable
    
    @Override
    public void run() {
        String remoto = cliente.getRemoteSocketAddress().toString();
        System.out.println("[TCP] Cliente conectado: " + remoto);

        try (
            BufferedReader in  = new BufferedReader(
                new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter    out = new PrintWriter(cliente.getOutputStream(), true,
                StandardCharsets.UTF_8)
        ) {
            String linea;

            // procesar lineas hasta bye o cierre del socket.
            while ((linea = in.readLine()) != null) {

                //  parsear la linea del protocolo tcp.
                TcpParseResult parseResult = TcpRequestParser.parsear(linea);

                //  bye es salir del bucle (el try with resources cierra el socket).
                if (parseResult.isBye()) {
                    System.out.println("[TCP] BYE recibido de " + remoto);
                    break;
                }

                //  error de protocolo, que responda con error y que continue.
                if (!parseResult.isOk()) {
                    String respuesta = serializarError(
                        "PROTOCOLO_INVALIDO",
                        parseResult.getErrorMensaje(),
                        FormatoSalida.JSON   // fallback: no sabemos el formato pedido.
                    );
                    out.println(respuesta);
                    continue;
                }

                // construir SolicitudPadron con cedula y formato.
                SolicitudPadron solicitud = new SolicitudPadron(
                    parseResult.getCedula(),
                    parseResult.getFormato()
                );

                // llamar a la logica de negocio.
                RespuestaPadron respuesta;
                try {
                    respuesta = servicio.atender(solicitud);
                } catch (Exception e) {
                    // error en la capa de negocio.
                    System.err.println("[TCP] Error en ServicioPadron: " + e.getMessage());
                    respuesta = RespuestaPadron.error(
                        "ERROR_INTERNO",
                        "Error interno del servidor: " + e.getMessage()
                    );
                }

                //  serializar segun el formato pedido.
                String cuerpo = serializar(respuesta, parseResult.getFormato());

                // enviar la respuesta.
                out.println(cuerpo);
            }

        } catch (IOException e) {
            // el cliente cerro la conexion abruptamente.
            System.err.println("[TCP] Conexión cerrada con " + remoto + ": " + e.getMessage());
        } finally {
            cerrarSocket();
            System.out.println("[TCP] Cliente desconectado: " + remoto);
        }
    }

     
     // serializa una RespuestaPadron al formato indicado.
     
    private String serializar(RespuestaPadron respuesta, FormatoSalida formato) {
        if (formato == FormatoSalida.XML) {
            return Serializador.toXml(respuesta);
        }
        return Serializador.toJson(respuesta);  // json por defecto.
    }

    /**
     * construye y serializa una respuesta de error puro
     * (cuando ni siquiera se llego a llamar a ServicioPadron).
     */
    private String serializarError(String codigo, String mensaje, FormatoSalida formato) {
        RespuestaPadron respError = RespuestaPadron.error(codigo, mensaje);
        return serializar(respError, formato);
    }

    // cierra el socket.
    private void cerrarSocket() {
        try {
            if (!cliente.isClosed()) {
                cliente.close();
            }
        } catch (IOException ignored) {}
    }
}