package mass_backend.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import mass_backend.Entidad.Venta;

public interface VentaDAO extends JpaRepository <Venta, Integer>{
    List<Venta> findByEmpleadoIdEmpleado(Integer idEmpleado);
    List<Venta> findByUsuarioIdUsuario(Integer idUsuario);
}
