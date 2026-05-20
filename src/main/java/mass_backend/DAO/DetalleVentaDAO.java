package mass_backend.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import mass_backend.Entidad.DetalleVenta;

public interface DetalleVentaDAO extends JpaRepository <DetalleVenta, Integer>{

}
