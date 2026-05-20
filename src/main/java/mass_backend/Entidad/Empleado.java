package mass_backend.Entidad;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "empleado")
@Data
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEmpleado;

    private String nombre;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
}