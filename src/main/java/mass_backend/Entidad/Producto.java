package mass_backend.Entidad;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "producto")
@Data
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    private String imagen;

    private Integer stock;

    private String categoria;

    @Column(nullable = false)
    private Boolean activo = true;
}