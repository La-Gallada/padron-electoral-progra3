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

public class TcpClientHandler implements Runnable {

    private final Socket cliente;
    private final ServicioPadron servicio;

    public TcpClientHandler(Socket cliente, ServicioPadron servicio) {
        this.cliente = cliente;
        this.servicio = servicio;
    }

    @Override
    public void run() {
        String remoto = String.valueOf(cliente.getRemoteSocketAddress());
        System.out.println("[TCP] Cliente conectado: " + remoto);

        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(cliente.getOutputStream(), true)
        ) {
            String linea;
            while ((linea = in.readLine()) != null) {
                TcpParseResult parseResult = TcpRequestParser.parsear(linea);

                if (parseResult.isBye()) {
                    System.out.println("[TCP] BYE recibido de " + remoto);
                    break;
                }

                if (!parseResult.isOk()) {
                    String respuesta = serializarError(
                            "FORMATO_INVALIDO",
                            parseResult.getErrorMensaje(),
                            FormatoSalida.JSON
                    );
                    out.println(respuesta);
                    continue;
                }

                SolicitudPadron solicitud = new SolicitudPadron(
                        parseResult.getCedula(),
                        parseResult.getFormato()
                );

                RespuestaPadron respuesta;
                try {
                    respuesta = servicio.atender(solicitud);
                } catch (Exception e) {
                    System.err.println("[TCP] Error en ServicioPadron: " + e.getMessage());
                    respuesta = RespuestaPadron.error(
                            "ERROR_INTERNO",
                            "Error interno del servidor."
                    );
                }

                String cuerpo = serializar(respuesta, parseResult.getFormato());
                out.println(cuerpo);
            }

        } catch (IOException e) {
            System.err.println("[TCP] Conexión cerrada con " + remoto + ": " + e.getMessage());
        } finally {
            cerrarSocket();
            System.out.println("[TCP] Cliente desconectado: " + remoto);
        }
    }

    private String serializar(RespuestaPadron respuesta, FormatoSalida formato) {
        if (formato == FormatoSalida.XML) {
            return Serializador.toXml(respuesta);
        }
        return Serializador.toJson(respuesta);
    }

    private String serializarError(String codigo, String mensaje, FormatoSalida formato) {
        RespuestaPadron respError = RespuestaPadron.error(codigo, mensaje);
        return serializar(respError, formato);
    }

    private void cerrarSocket() {
        try {
            if (!cliente.isClosed()) {
                cliente.close();
            }
        } catch (IOException ignored) {
        }
    }
}