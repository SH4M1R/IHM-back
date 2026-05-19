package mass_backend.Entidad;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Data
@Entity
@Table(name="Venta")
public class Venta {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer idventa;
    private Double ventatotal;
    private LocalDateTime fecha;

    @OneToMany(mappedBy = "venta")
    private List<DetalleVenta> detalles;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}
