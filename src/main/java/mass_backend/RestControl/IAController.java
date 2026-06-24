package mass_backend.RestControl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/ia")
public class IAController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ia.modelo.url}")
    private String modeloUrl;

    // ──────────────────────────────────────────────────────────────
    // POST /api/ia/recomendar
    // Body esperado desde el frontend:
    // {
    //   "idUsuario": 4,
    //   "productosEnCarrito": ["Lata Leche Gloria", "Fideos Bells 500g"]
    // }
    // ──────────────────────────────────────────────────────────────
    @PostMapping("/recomendar")
    public ResponseEntity<?> recomendar(@RequestBody Map<String, Object> body) {
        try {
            // Armar el body para el modelo Python
            Map<String, Object> payload = new HashMap<>();
            payload.put("idUsuario", String.valueOf(body.get("idUsuario")));
            payload.put("productosEnCarrito", body.getOrDefault("productosEnCarrito", List.of()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Llamar al modelo Python en puerto 5001
            ResponseEntity<Map> response = restTemplate.postForEntity(
                modeloUrl + "/recomendar/carrito",
                request,
                Map.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            // Si el modelo no está disponible, devolver respuesta vacía sin romper el chat
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("porCarrito", List.of());
            fallback.put("personalizadas", List.of());
            fallback.put("error", "Modelo de IA no disponible: " + e.getMessage());
            return ResponseEntity.ok(fallback);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/ia/recomendar/usuario/{idUsuario}
    // Recomendaciones solo por historial del usuario
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/recomendar/usuario/{idUsuario}")
    public ResponseEntity<?> recomendarPorUsuario(@PathVariable Integer idUsuario) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                modeloUrl + "/recomendar/usuario/" + idUsuario,
                Map.class
            );
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("recomendaciones", List.of());
            fallback.put("error", "Modelo de IA no disponible: " + e.getMessage());
            return ResponseEntity.ok(fallback);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // POST /api/ia/reentrenar
    // Llama al modelo Python para que vuelva a entrenar con los CSVs
    // Llamar después de acumular nuevas ventas
    // ──────────────────────────────────────────────────────────────
    @PostMapping("/reentrenar")
    public ResponseEntity<?> reentrenar() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                modeloUrl + "/reentrenar",
                request,
                Map.class
            );
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "Modelo no disponible: " + e.getMessage()));
        }
    }
}