package mass_backend.RestControl;

import java.io.PrintWriter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import mass_backend.Entidad.DetalleVenta;
import mass_backend.Entidad.Venta;
import mass_backend.Service.VentaService;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public ResponseEntity<List<Venta>> listar() {
        return ResponseEntity.ok(ventaService.listarVentas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerPorId(@PathVariable Integer id) {
        Venta venta = ventaService.obtenerPorId(id);
        if (venta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(venta);
    }

    @PostMapping
    public ResponseEntity<Venta> registrar(@RequestBody Venta venta) {
        Venta nuevaVenta = ventaService.registrarVenta(venta);
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
                String linea = String.format("%d,%d,\"%s\",\"%s\",%.2f",
                    venta.getIdVenta(),
                    detalle.getProducto().getIdProducto(),
                    escaparCSV(detalle.getProducto().getNombre()),
                    escaparCSV(detalle.getProducto().getCategoria()),
                    detalle.getProducto().getPrecio()
                );
                writer.println(linea);
            }
        }

        writer.flush();
    }

    @GetMapping("/exportar/historial-usuario")
    public void exportarHistorialUsuario(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=historial_usuario.csv");

        List<Venta> ventas = ventaService.listarVentas();

        PrintWriter writer = response.getWriter();
        writer.println("idUsuario,idVenta,fecha,idProducto,nombreProducto,categoria,precio,cantidad");

        for (Venta venta : ventas) {
            if (venta.getUsuario() == null || venta.getDetalles() == null) continue;
            for (DetalleVenta detalle : venta.getDetalles()) {
                if (detalle.getProducto() == null) continue;
                String linea = String.format("%d,%d,%s,%d,\"%s\",\"%s\",%.2f,%d",
                    venta.getUsuario().getIdUsuario(),
                    venta.getIdVenta(),
                    venta.getFecha().toLocalDate().toString(),
                    detalle.getProducto().getIdProducto(),
                    escaparCSV(detalle.getProducto().getNombre()),
                    escaparCSV(detalle.getProducto().getCategoria()),
                    detalle.getProducto().getPrecio(),
                    detalle.getCantidad()
                );
                writer.println(linea);
            }
        }

        writer.flush();
    }

    @PutMapping("/{idVenta}/asignar")
    public ResponseEntity<Venta> asignarEmpleado(
            @PathVariable Integer idVenta, 
            @RequestBody java.util.Map<String, Integer> body) {
        
        Integer idEmpleado = body.get("idEmpleado");
        if (idEmpleado == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Venta ventaActualizada = ventaService.asignarEmpleado(idVenta, idEmpleado);
        if (ventaActualizada == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ventaActualizada);
    }

    @GetMapping("/empleado/{idEmpleado}")
    public ResponseEntity<List<Venta>> listarPorEmpleado(@PathVariable Integer idEmpleado) {
        return ResponseEntity.ok(ventaService.listarVentasPorEmpleado(idEmpleado));
    }

    @PutMapping("/{idVenta}/estado")
    public ResponseEntity<Venta> actualizarEstado(
            @PathVariable Integer idVenta, 
            @RequestBody java.util.Map<String, String> body) {
        
        String nuevoEstado = body.get("estado");
        if (nuevoEstado == null) {
            return ResponseEntity.badRequest().build();
        }

        Venta ventaActualizada = ventaService.actualizarEstado(idVenta, nuevoEstado);
        if (ventaActualizada == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ventaActualizada);
    }

    private String escaparCSV(String valor) {
        if (valor == null) return "";
        return valor.replace("\"", "\"\"");
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Venta>> listarPorUsuario(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(ventaService.listarVentasPorUsuario(idUsuario));
    }

    
}