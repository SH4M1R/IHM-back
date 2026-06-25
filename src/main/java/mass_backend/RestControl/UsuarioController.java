package mass_backend.RestControl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import mass_backend.Entidad.Usuario;
import mass_backend.Service.MailService;
import mass_backend.Service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MailService mailService;

    private final java.util.Map<String, String> otpStore = new java.util.concurrent.ConcurrentHashMap<>();

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

    @PostMapping("/enviar-otp")
    public ResponseEntity<?> enviarOtp(@RequestBody java.util.Map<String, String> body) {
        String correo = body.get("correo");
        if (correo == null || correo.isBlank()) {
            return ResponseEntity.badRequest().body("Correo requerido");
        }

        // Verificar si el correo ya está registrado
        if (usuarioService.existePorCorreo(correo)) {
            return ResponseEntity.status(409).body("El correo ya está registrado");
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        otpStore.put(correo, otp);

        // Programar expiración del OTP a los 5 minutos
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override public void run() { otpStore.remove(correo); }
        }, 5 * 60 * 1000);

        try {
            mailService.sendOtp(correo, otp);
            return ResponseEntity.ok().body("OTP enviado");
        } catch (Exception e) {
            // Si Brevo rechaza el correo (no existe), lanzará excepción
            return ResponseEntity.status(400).body("Correo no válido o no existe");
        }
    }

    @PostMapping("/verificar-otp")
    public ResponseEntity<?> verificarOtp(@RequestBody java.util.Map<String, String> body) {
        String correo = body.get("correo");
        String otp = body.get("otp");

        String otpGuardado = otpStore.get(correo);

        if (otpGuardado == null) {
            return ResponseEntity.status(410).body("OTP expirado o no solicitado");
        }
        if (!otpGuardado.equals(otp)) {
            return ResponseEntity.status(400).body("Código incorrecto");
        }

        otpStore.remove(correo);
        return ResponseEntity.ok().body("OTP válido");
    }
}