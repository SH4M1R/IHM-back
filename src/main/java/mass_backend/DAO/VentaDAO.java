package mass_backend.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import mass_backend.Entidad.Venta;

public interface VentaDAO extends JpaRepository <Venta, Integer>{

}
