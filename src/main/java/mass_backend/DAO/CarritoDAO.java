package mass_backend.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import mass_backend.Entidad.Carrito;

public interface CarritoDAO extends JpaRepository <Carrito, Integer>{

}
