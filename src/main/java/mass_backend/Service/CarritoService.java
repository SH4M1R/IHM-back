package mass_backend.Service;

import mass_backend.Entidad.Carrito;
import mass_backend.Entidad.CarritoItem;

public interface CarritoService {
    Carrito obtenerPorId(Integer id);
    Carrito agregarItem(Integer idCarrito, CarritoItem item);
    Carrito removerItem(Integer idCarrito, Integer idItem);
    Carrito limpiarCarrito(Integer idCarrito);
}