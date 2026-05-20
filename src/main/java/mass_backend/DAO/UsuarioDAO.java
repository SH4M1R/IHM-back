package mass_backend.DAO;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import mass_backend.Entidad.Usuario;

public interface UsuarioDAO extends JpaRepository <Usuario, Integer>{
        Optional<Usuario> findByCorreo(String correo);
}
