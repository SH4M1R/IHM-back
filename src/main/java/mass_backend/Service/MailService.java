package mass_backend.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Value("${brevo.api-key}")
    private String apiKey;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Datos del remitente estáticos que ya tenías configurados
    private final Map<String, String> sender = Map.of(
        "name", "MassApp",
        "email", "ssoftware678@gmail.com"
    );

    /**
     * Envía una notificación al cliente sobre el cambio de estado de su pedido.
     */
    public void sendOrderStatus(String to, String clientName, Long idVenta, String nuevoEstado) {
        try {
            Map<String, Object> payload = Map.of(
                "sender", sender,
                "to", List.of(Map.of("email", to, "name", clientName)),
                "subject", "Actualización de tu Pedido Codigo N°" + idVenta + " - " + nuevoEstado.toUpperCase(),
                "htmlContent", buildOrderStatusHtml(clientName, idVenta, nuevoEstado)
            );

            String json = objectMapper.writeValueAsString(payload);

            RequestBody body = RequestBody.create(
                json, MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .post(body)
                .addHeader("api-key", apiKey)
                .addHeader("accept", "application/json")
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String responseBody = response.body() != null
                        ? response.body().string()
                        : "sin cuerpo de respuesta";
                    throw new RuntimeException(
                        "Brevo respondió " + response.code() + ": " + responseBody
                    );
                }
                log.info("Notificación de pedido #{} ({}) enviada correctamente a {}", idVenta, nuevoEstado, to);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error enviando estado de pedido a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error enviando correo de estado: " + e.getMessage(), e);
        }
    }

    /**
     * Mantiene tu método de OTP original intacto.
     */
    public void sendOtp(String to, String otp) {
        try {
            Map<String, Object> payload = Map.of(
                "sender", sender,
                "to", List.of(Map.of("email", to)),
                "subject", "Tu código de verificación - MassApp",
                "htmlContent", buildOtpHtml(otp)
            );

            String json = objectMapper.writeValueAsString(payload);

            RequestBody body = RequestBody.create(
                json, MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .post(body)
                .addHeader("api-key", apiKey)
                .addHeader("accept", "application/json")
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String responseBody = response.body() != null
                        ? response.body().string()
                        : "sin cuerpo de respuesta";
                    throw new RuntimeException(
                        "Brevo respondió " + response.code() + ": " + responseBody
                    );
                }
                log.info("OTP enviado correctamente a {}", to);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error enviando OTP a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error enviando correo: " + e.getMessage(), e);
        }
    }

    private String buildOtpHtml(String otp) {
        return """
            <div style="font-family:sans-serif;max-width:480px;margin:auto;\
            padding:32px;border:1px solid #e5e7eb;border-radius:16px">
              <h2 style="color:#FFDE21;margin-bottom:8px">MassApp</h2>
              <p style="color:#374151">Tu código de verificación es:</p>
              <div style="font-size:40px;font-weight:bold;letter-spacing:12px;\
            color:#FFDE21;margin:24px 0">%s</div>
              <p style="color:#6b7280;font-size:14px">Expira en 5 minutos.\
             Si no solicitaste esto, ignora este correo.</p>
            </div>
            """.formatted(otp);
    }

    private String buildOrderStatusHtml(String clientName, Long idVenta, String nuevoEstado) {
        String mensajeDetalle = switch (nuevoEstado.toLowerCase()) {
            case "pagado" -> "Hemos recibido tu pago correctamente y estamos procesando tu orden.";
            case "alistando" -> "¡Buenas noticias! Tu paquete ya se está preparando en nuestro almacén.";
            case "en_camino" -> "Tu pedido ya ha salido y va en camino a tu dirección.";
            case "entregado" -> "¡Tu paquete ha sido entregado con éxito! Gracias por confiar en nosotros.";
            default -> "Tu pedido ha pasado al estado: " + nuevoEstado;
        };

        return """
            <div style="font-family:sans-serif;max-width:520px;margin:auto;\
            padding:32px;border:1px solid #e5e7eb;border-radius:16px">
              <h2 style="color:#1e1b4b;margin-bottom:4px">MassApp</h2>
              <p style="color:#4b5563;font-size:16px">Hola <strong>%s</strong>,</p>
              <p style="color:#374151;font-size:14px;line-height:1.5">
                Te informamos que tu pedido de código <strong>N°%d</strong> se ha actualizado.
              </p>
              <div style="background-color:#f3f4f6;color:#1e1b4b;font-weight:bold;\
            text-align:center;padding:12px;border-radius:8px;margin:20px 0;\
            text-transform:uppercase;letter-spacing:1px;font-size:16px">
                %s
              </div>
              <p style="color:#4b5563;font-size:14px;line-height:1.5">%s</p>
              <hr style="border:0;border-top:1px solid #e5e7eb;margin:24px 0" />
              <p style="color:#9ca3af;font-size:12px;text-align:center;margin:0">
                Este es un correo automático, por favor no respondas a este mensaje.
              </p>
            </div>
            """.formatted(clientName, idVenta, nuevoEstado, mensajeDetalle);
    }
}