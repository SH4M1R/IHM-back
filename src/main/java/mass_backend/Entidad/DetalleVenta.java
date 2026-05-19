package mass_backend.Entidad;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "detalleventa")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer iddetalle;
    private Integer cantidad;
    private Double subtotal;
    private String metodopago;
}
