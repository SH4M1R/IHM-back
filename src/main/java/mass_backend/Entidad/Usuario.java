package mass_backend.Entidad;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuario")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    private String nombre;
    private String apellido;

    @Column(unique = true, nullable = false)
    private Long dni;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(nullable = false)
    private String password;
}