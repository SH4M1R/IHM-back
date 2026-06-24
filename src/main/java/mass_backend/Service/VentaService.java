package mass_backend.Service;

import java.util.List;
import mass_backend.Entidad.Venta;

public interface VentaService {
    List<Venta> listarVentas();
    Venta obtenerPorId(Integer id);
    Venta registrarVenta(Venta venta);
    Venta asignarEmpleado(Integer idVenta, Integer idEmpleado);
    List<Venta> listarVentasPorEmpleado(Integer idEmpleado);
    Venta actualizarEstado(Integer idVenta, String nuevoEstado);
    public List<Venta> listarVentasPorUsuario(Integer idUsuario);
}