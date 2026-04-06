package padron.logica;

import java.io.File;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class ServicioCorreo {

    private final String remitente;
    private final String clave;

    public ServicioCorreo(String remitente, String clave) {
        this.remitente = remitente;
        this.clave = clave;
    }

    public void enviarConAdjunto(String destino, String asunto, String mensaje, File archivo) throws Exception {
        enviarConAdjunto(destino, null, null, asunto, mensaje, archivo);
    }

    public void enviarConAdjunto(String destino, String cc, String asunto, String mensaje, File archivo) throws Exception {
        enviarConAdjunto(destino, cc, null, asunto, mensaje, archivo);
    }

    public void enviarConAdjunto(String destino, String cc, String cco, String asunto, String mensaje, File archivo) throws Exception {
        if (destino == null || destino.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo destino es obligatorio.");
        }

        if (archivo == null || !archivo.exists() || !archivo.isFile()) {
            throw new IllegalArgumentException("El archivo adjunto no existe o no es válido.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, clave);
            }
        });

        session.setDebug(true);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(remitente));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destino.trim(), false));

        if (cc != null && !cc.trim().isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc.trim(), false));
        }

        if (cco != null && !cco.trim().isEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(cco.trim(), false));
        }

        message.setSubject(asunto == null ? "" : asunto);

        MimeBodyPart texto = new MimeBodyPart();
        texto.setText(mensaje == null ? "" : mensaje);

        MimeBodyPart adjunto = new MimeBodyPart();
        adjunto.attachFile(archivo);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(texto);
        multipart.addBodyPart(adjunto);

        message.setContent(multipart);

        Transport.send(message);
    }
}