package mass_backend.DAO;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import mass_backend.Entidad.Empleado;

public interface EmpleadoDAO extends JpaRepository <Empleado, Integer>{
    Optional<Empleado> findByUsername(String username);
}
