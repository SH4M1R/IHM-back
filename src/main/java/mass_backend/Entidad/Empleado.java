package mass_backend.Entidad;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table (name = "Empleado")
public class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEmpleado")
    private Integer idempleado;
    private String nombre;
    private String username;
    private String contrasena;
}
