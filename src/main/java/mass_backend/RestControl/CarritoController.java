package mass_backend.RestControl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import mass_backend.Entidad.Carrito;
import mass_backend.Entidad.CarritoItem;
import mass_backend.Service.CarritoService;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/{id}")
    public ResponseEntity<Carrito> obtenerPorId(@PathVariable Integer id) {
        Carrito carrito = carritoService.obtenerPorId(id);
        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/{id}/agregar")
    public ResponseEntity<Carrito> agregarItem(@PathVariable Integer id, @RequestBody CarritoItem item) {
        Carrito actualizado = carritoService.agregarItem(id, item);
        if (actualizado == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}/remover/{idItem}")
    public ResponseEntity<Carrito> removerItem(@PathVariable Integer id, @PathVariable Integer idItem) {
        Carrito actualizado = carritoService.removerItem(id, idItem);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}/limpiar")
    public ResponseEntity<Carrito> limpiarCarrito(@PathVariable Integer id) {
        Carrito actualizado = carritoService.limpiarCarrito(id);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<Carrito> obtenerPorUsuario(@PathVariable Integer idUsuario) {
        Carrito carrito = carritoService.obtenerPorUsuario(idUsuario);
        if (carrito == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/usuario/{idUsuario}/crear")
    public ResponseEntity<Carrito> crearCarrito(@PathVariable Integer idUsuario) {
        Carrito carrito = carritoService.crearCarritoParaUsuario(idUsuario);
        if (carrito == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(carrito);
    }

    @PutMapping("/{id}/items/{idItem}")
    public ResponseEntity<Carrito> actualizarCantidad(@PathVariable Integer id, @PathVariable Integer idItem, @RequestBody java.util.Map<String, Integer> body) {
        Integer nuevaCantidad = body.get("cantidad");
        Carrito actualizado = carritoService.actualizarCantidadItem(id, idItem, nuevaCantidad); 
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }
}