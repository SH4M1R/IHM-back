package mass_backend.RestControl;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import mass_backend.Entidad.DetalleVenta;
import mass_backend.Entidad.Venta;
import mass_backend.Service.MailService;
import mass_backend.Service.UsuarioService;
import mass_backend.Service.VentaService;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private static final Logger log = LoggerFactory.getLogger(VentaController.class);

    @Autowired
    private VentaService ventaService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Venta>> listar() {
        return ResponseEntity.ok(ventaService.listarVentas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerPorId(@PathVariable Integer id) {
        Venta venta = ventaService.obtenerPorId(id);
        if (venta == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(venta);
    }

    @PostMapping
    public ResponseEntity<Venta> registrar(@RequestBody Venta venta) {
        Venta nuevaVenta = ventaService.registrarVenta(venta);

        try {
            Integer idUsuario = nuevaVenta.getUsuario() != null
                ? nuevaVenta.getUsuario().getIdUsuario()
                : null;

            if (idUsuario == null && venta.getUsuario() != null) {
                idUsuario = venta.getUsuario().getIdUsuario();
            }

            if (idUsuario != null) {
                // Traer usuario completo con correo desde BD
                mass_backend.Entidad.Usuario usuarioCompleto =
                    usuarioService.obtenerUsuarioPorId(idUsuario);

                if (usuarioCompleto != null && usuarioCompleto.getCorreo() != null) {

                    String correo    = usuarioCompleto.getCorreo();
                    String nombre    = usuarioCompleto.getNombre() != null
                                        ? usuarioCompleto.getNombre() : "Cliente";
                    String direccion = usuarioCompleto.getDireccion() != null
                                        ? usuarioCompleto.getDireccion() : "No registrada";
                    String fecha     = nuevaVenta.getFecha()
                                        .toString().replace("T", " ").substring(0, 16);

                    List<Map<String, Object>> items = nuevaVenta.getDetalles().stream()
                        .map(d -> {
                            Map<String, Object> item = new HashMap<>();
                            item.put("nombre",   d.getProducto().getNombre());
                            item.put("cantidad", d.getCantidad());
                            item.put("precio",   d.getProducto().getPrecio());
                            return item;
                        })
                        .toList();

                    double subtotal = items.stream()
                        .mapToDouble(i -> ((Number) i.get("precio")).doubleValue()
                                    * ((Number) i.get("cantidad")).intValue())
                        .sum();

                    log.info("Enviando boleta #{} a {}", nuevaVenta.getIdVenta(), correo);

                    mailService.sendBoleta(
                        correo, nombre,
                        nuevaVenta.getIdVenta().longValue(),
                        fecha, direccion,
                        items, subtotal, nuevaVenta.getTotal()
                    );

                    log.info("Boleta enviada correctamente a {}", correo);
                } else {
                    log.warn("Usuario {} no tiene correo registrado", idUsuario);
                }
            } else {
                log.warn("No se encontró idUsuario en la venta #{}", nuevaVenta.getIdVenta());
            }

        } catch (Exception e) {
            // El correo no debe fallar la venta
            log.error("Error enviando boleta para venta #{}: {}",
                nuevaVenta.getIdVenta(), e.getMessage(), e);
        }

        return ResponseEntity.ok(nuevaVenta);
    }

    @GetMapping("/exportar/market-basket")
    public void exportarMarketBasket(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=market_basket.csv");

        List<Venta> ventas = ventaService.listarVentas();
        PrintWriter writer = response.getWriter();
        writer.println("idVenta,idProducto,nombreProducto,categoria,precio");

        for (Venta venta : ventas) {
            if (venta.getDetalles() == null) continue;
            for (DetalleVenta detalle : venta.getDetalles()) {
                if (detalle.getProducto() == null) continue;
                writer.println(String.format("%d,%d,\"%s\",\"%s\",%.2f",
                    venta.getIdVenta(),
                    detalle.getProducto().getIdProducto(),
                    escaparCSV(detalle.getProducto().getNombre()),
                    escaparCSV(detalle.getProducto().getCategoria()),
                    detalle.getProducto().getPrecio()
                ));
            }
        }
        writer.flush();
    }

    @GetMapping("/exportar/historial-usuario")
    public void exportarHistorialUsuario(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
            "attachment; filename=historial_usuario.csv");

        List<Venta> ventas = ventaService.listarVentas();
        PrintWriter writer = response.getWriter();
        writer.println("idUsuario,idVenta,fecha,idProducto,nombreProducto,categoria,precio,cantidad");

        for (Venta venta : ventas) {
            if (venta.getUsuario() == null || venta.getDetalles() == null) continue;
            for (DetalleVenta detalle : venta.getDetalles()) {
                if (detalle.getProducto() == null) continue;
                writer.println(String.format("%d,%d,%s,%d,\"%s\",\"%s\",%.2f,%d",
                    venta.getUsuario().getIdUsuario(),
                    venta.getIdVenta(),
                    venta.getFecha().toLocalDate().toString(),
                    detalle.getProducto().getIdProducto(),
                    escaparCSV(detalle.getProducto().getNombre()),
                    escaparCSV(detalle.getProducto().getCategoria()),
                    detalle.getProducto().getPrecio(),
                    detalle.getCantidad()
                ));
            }
        }
        writer.flush();
    }

    @PutMapping("/{idVenta}/asignar")
    public ResponseEntity<Venta> asignarEmpleado(
            @PathVariable Integer idVenta,
            @RequestBody Map<String, Integer> body) {

        Integer idEmpleado = body.get("idEmpleado");
        if (idEmpleado == null) return ResponseEntity.badRequest().build();

        Venta ventaActualizada = ventaService.asignarEmpleado(idVenta, idEmpleado);
        if (ventaActualizada == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(ventaActualizada);
    }

    @GetMapping("/empleado/{idEmpleado}")
    public ResponseEntity<List<Venta>> listarPorEmpleado(@PathVariable Integer idEmpleado) {
        return ResponseEntity.ok(ventaService.listarVentasPorEmpleado(idEmpleado));
    }

    @PutMapping("/{idVenta}/estado")
    public ResponseEntity<Venta> actualizarEstado(
            @PathVariable Integer idVenta,
            @RequestBody Map<String, String> body) {

        String nuevoEstado = body.get("estado");
        if (nuevoEstado == null) return ResponseEntity.badRequest().build();

        Venta ventaActualizada = ventaService.actualizarEstado(idVenta, nuevoEstado);
        if (ventaActualizada == null) return ResponseEntity.notFound().build();

        if (ventaActualizada.getUsuario() != null) {
            String emailCliente  = ventaActualizada.getUsuario().getCorreo();
            String nombreCliente = ventaActualizada.getUsuario().getNombre();

            if (emailCliente != null && !emailCliente.isEmpty()) {
                mailService.sendOrderStatus(
                    emailCliente,
                    nombreCliente != null ? nombreCliente : "Cliente",
                    idVenta.longValue(),
                    nuevoEstado
                );
            }
        }

        return ResponseEntity.ok(ventaActualizada);
    }

    @PutMapping("/{idVenta}/evidencia")
    public ResponseEntity<Venta> guardarEvidencia(
            @PathVariable Integer idVenta,
            @RequestBody Map<String, String> body) {

        String evidencia = body.get("evidencia");
        if (evidencia == null || evidencia.isBlank()) return ResponseEntity.badRequest().build();

        Venta venta = ventaService.obtenerPorId(idVenta);
        if (venta == null) return ResponseEntity.notFound().build();

        venta.setEvidencia(evidencia);
        ventaService.registrarVenta(venta);
        return ResponseEntity.ok(venta);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Venta>> listarPorUsuario(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(ventaService.listarVentasPorUsuario(idUsuario));
    }

    private String escaparCSV(String valor) {
        if (valor == null) return "";
        return valor.replace("\"", "\"\"");
    }
}