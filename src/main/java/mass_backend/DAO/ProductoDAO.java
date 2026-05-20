package mass_backend.DAO;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import mass_backend.Entidad.Producto;

public interface ProductoDAO extends JpaRepository <Producto, Integer> {
    List<Producto> findByActivoTrue();
}
