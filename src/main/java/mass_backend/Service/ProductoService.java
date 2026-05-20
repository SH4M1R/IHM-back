package mass_backend.Service;

import java.util.List;
import mass_backend.Entidad.Producto;

public interface ProductoService {
    List<Producto> listarProductos();
    Producto crearProducto(Producto producto);
    Producto obtenerProductoPorId(Integer idProducto);
    Producto actualizarProducto(Producto producto);
    void eliminarProducto(Integer idProducto);
}
