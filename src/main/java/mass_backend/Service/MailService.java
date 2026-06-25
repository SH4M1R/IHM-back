package mass_backend.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final DeviceRgb DARK_BLUE  = new DeviceRgb(0x1e, 0x1b, 0x4b);
    private static final DeviceRgb YELLOW     = new DeviceRgb(0xfa, 0xcc, 0x15);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(0xf3, 0xf4, 0xf6);
    private static final DeviceRgb MID_GRAY   = new DeviceRgb(0x6b, 0x72, 0x80);

    @Value("${brevo.api-key}")
    private String apiKey;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> sender = Map.of(
        "name", "MassApp",
        "email", "ssoftware678@gmail.com"
    );

    public void sendBoleta(String to, String clientName, Long idVenta,
                           String fecha, String direccion,
                           List<Map<String, Object>> items,
                           double subtotal, double total) {
        try {
            String numeroBoleta = "B001-" + String.format("%06d", idVenta);

            byte[] pdfBytes = buildBoletaPdf(
                clientName, to, idVenta, numeroBoleta, fecha, direccion, items, subtotal, total
            );
            String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

            String htmlNotif = """
                <div style="font-family:sans-serif;max-width:520px;margin:auto;
                            padding:32px;border:1px solid #e5e7eb;border-radius:16px">
                  <h2 style="color:#1e1b4b">MassApp</h2>
                  <p>Hola <strong>%s</strong>, adjunto encontrarás tu boleta electrónica
                     <strong>%s</strong> correspondiente a tu pedido del <strong>%s</strong>.</p>
                  <p style="color:#6b7280;font-size:13px">
                    Gracias por tu compra. Este correo fue generado automáticamente.
                  </p>
                </div>
                """.formatted(clientName, numeroBoleta, fecha);

            Map<String, Object> payload = Map.of(
                "sender",      sender,
                "to",          List.of(Map.of("email", to, "name", clientName)),
                "subject",     "Tu Boleta Electrónica " + numeroBoleta + " - MassApp",
                "htmlContent", htmlNotif,
                "attachment",  List.of(Map.of(
                    "content", pdfBase64,
                    "name",    numeroBoleta + ".pdf"
                ))
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
                    String rb = response.body() != null ? response.body().string() : "";
                    throw new RuntimeException("Brevo error " + response.code() + ": " + rb);
                }
                log.info("Boleta PDF {} enviada correctamente a {}", numeroBoleta, to);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error enviando boleta PDF a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error enviando boleta PDF: " + e.getMessage(), e);
        }
    }

    private byte[] buildBoletaPdf(String clientName, String correo, Long idVenta,
                                   String numeroBoleta, String fecha, String direccion,
                                   List<Map<String, Object>> items,
                                   double subtotal, double total) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdfDoc, PageSize.A4);
        doc.setMargins(36, 48, 36, 48);

        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        float pageWidth = PageSize.A4.getWidth() - 96;

        Table header = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
            .setWidth(UnitValue.createPercentValue(100));

        Cell logoCell = new Cell().setBorder(Border.NO_BORDER)
            .setBackgroundColor(DARK_BLUE)
            .setPadding(14);
        logoCell.add(new Paragraph("MassApp")
            .setFont(bold).setFontSize(22).setFontColor(YELLOW));
        logoCell.add(new Paragraph("RUC: 20123456789")
            .setFont(regular).setFontSize(9).setFontColor(ColorConstants.WHITE));
        logoCell.add(new Paragraph("Av. Ejemplo 123, Lima, Perú")
            .setFont(regular).setFontSize(9).setFontColor(ColorConstants.WHITE));

        Cell tipoCell = new Cell().setBorder(Border.NO_BORDER)
            .setBackgroundColor(DARK_BLUE)
            .setPadding(14)
            .setTextAlignment(TextAlignment.RIGHT);
        tipoCell.add(new Paragraph("BOLETA DE VENTA")
            .setFont(bold).setFontSize(11).setFontColor(YELLOW));
        tipoCell.add(new Paragraph("N° " + numeroBoleta)
            .setFont(bold).setFontSize(10).setFontColor(ColorConstants.WHITE));
        tipoCell.add(new Paragraph("Fecha: " + fecha)
            .setFont(regular).setFontSize(9).setFontColor(ColorConstants.WHITE));

        header.addCell(logoCell).addCell(tipoCell);
        doc.add(header);

        doc.add(new Paragraph(" "));

        Table clienteTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
            .setWidth(UnitValue.createPercentValue(100))
            .setBackgroundColor(new DeviceRgb(0xfe, 0xfc, 0xe8))
            .setBorder(new SolidBorder(YELLOW, 1.5f))
            .setPadding(10);

        addClientRow(clienteTable, "Cliente:", clientName, bold, regular);
        addClientRow(clienteTable, "Correo:",  correo,      bold, regular);
        addClientRow(clienteTable, "Dirección:", direccion, bold, regular);
        doc.add(clienteTable);

        doc.add(new Paragraph(" "));

        Table productos = new Table(UnitValue.createPercentArray(new float[]{4, 1, 1.5f, 1.5f}))
            .setWidth(UnitValue.createPercentValue(100));

        String[] headers = {"Producto", "Cant.", "Precio Unit.", "Subtotal"};
        TextAlignment[] aligns = {TextAlignment.LEFT, TextAlignment.CENTER,
                                  TextAlignment.RIGHT, TextAlignment.RIGHT};
        for (int i = 0; i < headers.length; i++) {
            productos.addHeaderCell(
                new Cell().setBackgroundColor(DARK_BLUE)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(8)
                    .add(new Paragraph(headers[i])
                        .setFont(bold).setFontSize(10)
                        .setFontColor(YELLOW)
                        .setTextAlignment(aligns[i]))
            );
        }

        boolean shade = false;
        for (Map<String, Object> item : items) {
            String nombre   = (String) item.get("nombre");
            int    cantidad = ((Number) item.get("cantidad")).intValue();
            double precio   = ((Number) item.get("precio")).doubleValue();
            double sub      = precio * cantidad;
            DeviceRgb rowBg = shade ? LIGHT_GRAY : new DeviceRgb(255, 255, 255);

            addProductRow(productos, nombre, cantidad, precio, sub, rowBg, bold, regular);
            shade = !shade;
        }

        doc.add(productos);

        doc.add(new Paragraph(" "));

        Table totales = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
            .setWidth(UnitValue.createPercentValue(60))
            .setHorizontalAlignment(HorizontalAlignment.RIGHT);

        addTotalRow(totales, "Subtotal:", String.format("S/ %.2f", subtotal), bold, regular, false);
        addTotalRow(totales, "Delivery:", "Gratis",                            bold, regular, false);
        addTotalRow(totales, "IGV (18%):", "Incluido",                         bold, regular, false);

        totales.addCell(new Cell(1, 2)
            .setBorderTop(new SolidBorder(MID_GRAY, 1))
            .setBorderBottom(Border.NO_BORDER)
            .setBorderLeft(Border.NO_BORDER)
            .setBorderRight(Border.NO_BORDER)
            .setHeight(6));

        addTotalRow(totales, "TOTAL:", String.format("S/ %.2f", total), bold, bold, true);

        doc.add(totales);

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(
                "Método de pago: Tarjeta de crédito/débito (Stripe) · Estado: Aprobado")
            .setFont(regular).setFontSize(9).setFontColor(MID_GRAY)
            .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph(
                "¡Gracias por tu compra en MassApp! Este es un comprobante electrónico generado automáticamente.")
            .setFont(bold).setFontSize(9).setFontColor(DARK_BLUE)
            .setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }

    private void addClientRow(Table t, String label, String value,
                               PdfFont bold, PdfFont regular) {
        t.addCell(new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(4)
            .add(new Paragraph(label).setFont(bold).setFontSize(10)
                .setFontColor(MID_GRAY)));
        t.addCell(new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(4)
            .add(new Paragraph(value).setFont(regular).setFontSize(10)
                .setFontColor(DARK_BLUE)));
    }

    private void addProductRow(Table t, String nombre, int cantidad,
                                double precio, double sub,
                                DeviceRgb bg, PdfFont bold, PdfFont regular) {
        t.addCell(cellP(nombre, bg, regular, TextAlignment.LEFT));
        t.addCell(cellP(String.valueOf(cantidad), bg, regular, TextAlignment.CENTER));
        t.addCell(cellP(String.format("S/ %.2f", precio), bg, regular, TextAlignment.RIGHT));
        t.addCell(cellP(String.format("S/ %.2f", sub), bg, bold, TextAlignment.RIGHT));
    }

    private Cell cellP(String text, DeviceRgb bg, PdfFont font, TextAlignment align) {
        return new Cell()
            .setBackgroundColor(bg)
            .setBorder(Border.NO_BORDER)
            .setPadding(7)
            .add(new Paragraph(text).setFont(font).setFontSize(10)
                .setTextAlignment(align));
    }

    private void addTotalRow(Table t, String label, String value,
                              PdfFont labelFont, PdfFont valueFont, boolean highlight) {
        t.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(4)
            .add(new Paragraph(label)
                .setFont(labelFont).setFontSize(highlight ? 12 : 10)
                .setFontColor(highlight ? DARK_BLUE : MID_GRAY)));

        Cell valCell = new Cell().setBorder(Border.NO_BORDER).setPadding(4)
            .setTextAlignment(TextAlignment.RIGHT);
        if (highlight) {
            valCell.setBackgroundColor(DARK_BLUE)
                .add(new Paragraph(value).setFont(valueFont).setFontSize(12)
                    .setFontColor(YELLOW).setTextAlignment(TextAlignment.RIGHT));
        } else {
            valCell.add(new Paragraph(value).setFont(valueFont).setFontSize(10)
                .setFontColor(MID_GRAY).setTextAlignment(TextAlignment.RIGHT));
        }
        t.addCell(valCell);
    }

    public void sendOrderStatus(String to, String clientName, Long idVenta, String nuevoEstado) {
        try {
            Map<String, Object> payload = Map.of(
                "sender", sender,
                "to", List.of(Map.of("email", to, "name", clientName)),
                "subject", "Actualización de tu Pedido Codigo N°" + idVenta + " - " + nuevoEstado.toUpperCase(),
                "htmlContent", buildOrderStatusHtml(clientName, idVenta, nuevoEstado)
            );
            String json = objectMapper.writeValueAsString(payload);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .post(body)
                .addHeader("api-key", apiKey)
                .addHeader("accept", "application/json")
                .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String rb = response.body() != null ? response.body().string() : "sin cuerpo";
                    throw new RuntimeException("Brevo respondió " + response.code() + ": " + rb);
                }
                log.info("Notificación de pedido #{} ({}) enviada a {}", idVenta, nuevoEstado, to);
            }
        } catch (RuntimeException e) { throw e; }
        catch (Exception e) {
            log.error("Error enviando estado de pedido a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error enviando correo de estado: " + e.getMessage(), e);
        }
    }

    public void sendOtp(String to, String otp) {
        try {
            Map<String, Object> payload = Map.of(
                "sender", sender,
                "to", List.of(Map.of("email", to)),
                "subject", "Tu código de verificación - MassApp",
                "htmlContent", buildOtpHtml(otp)
            );
            String json = objectMapper.writeValueAsString(payload);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .post(body)
                .addHeader("api-key", apiKey)
                .addHeader("accept", "application/json")
                .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String rb = response.body() != null ? response.body().string() : "sin cuerpo";
                    throw new RuntimeException("Brevo respondió " + response.code() + ": " + rb);
                }
                log.info("OTP enviado correctamente a {}", to);
            }
        } catch (RuntimeException e) { throw e; }
        catch (Exception e) {
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
            case "pagado"    -> "Hemos recibido tu pago correctamente y estamos procesando tu orden.";
            case "alistando" -> "¡Buenas noticias! Tu paquete ya se está preparando en nuestro almacén.";
            case "en_camino" -> "Tu pedido ya ha salido y va en camino a tu dirección.";
            case "entregado" -> "¡Tu paquete ha sido entregado con éxito! Gracias por confiar en nosotros.";
            default          -> "Tu pedido ha pasado al estado: " + nuevoEstado;
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