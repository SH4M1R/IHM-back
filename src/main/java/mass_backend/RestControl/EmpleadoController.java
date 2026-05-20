package mass_backend.RestControl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import mass_backend.Entidad.Empleado;
import mass_backend.Service.EmpleadoService;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping
    public ResponseEntity<List<Empleado>> listar() {
        return ResponseEntity.ok(empleadoService.listarEmpleados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empleado> obtenerPorId(@PathVariable Integer id) {
        Empleado empleado = empleadoService.obtenerEmpleadoPorId(id);
        if (empleado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(empleado);
    }

    @PostMapping
    public ResponseEntity<Empleado> crear(@RequestBody Empleado empleado) {
        Empleado creado = empleadoService.crearEmpleado(empleado);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empleado> actualizar(
            @PathVariable Integer id,
            @RequestBody Empleado empleado
    ) {
        Empleado existente = empleadoService.obtenerEmpleadoPorId(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        empleado.setIdEmpleado(id);
        return ResponseEntity.ok(empleadoService.actualizarEmpleado(empleado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Empleado existente = empleadoService.obtenerEmpleadoPorId(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        empleadoService.eliminarEmpleado(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Empleado> login(@RequestBody Empleado request) {
        Empleado empleado = empleadoService.autenticarEmpleado(
                request.getUsername(),
                request.getPassword()
        );
        if (empleado == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(empleado);
    }

}