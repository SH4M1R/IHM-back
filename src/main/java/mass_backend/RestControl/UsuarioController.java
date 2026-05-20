package mass_backend.RestControl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import mass_backend.Entidad.Usuario;
import mass_backend.Service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Integer id) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);

        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
        Usuario creado = usuarioService.crearUsuario(usuario);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(
            @PathVariable Integer id,
            @RequestBody Usuario usuario
    ) {
        Usuario existente = usuarioService.obtenerUsuarioPorId(id);

        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        usuario.setIdUsuario(id);
        return ResponseEntity.ok(usuarioService.actualizarUsuario(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Usuario existente = usuarioService.obtenerUsuarioPorId(id);

        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Usuario request) {

        Usuario usuario = usuarioService.autenticarUsuario(
                request.getCorreo(),
                request.getPassword()
        );

        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(usuario);
    }
}