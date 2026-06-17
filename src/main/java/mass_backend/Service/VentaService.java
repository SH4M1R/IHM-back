package mass_backend.Service;

import java.util.List;
import mass_backend.Entidad.Venta;

public interface VentaService {
    List<Venta> listarVentas();
    Venta obtenerPorId(Integer id);
    Venta registrarVenta(Venta venta);
}