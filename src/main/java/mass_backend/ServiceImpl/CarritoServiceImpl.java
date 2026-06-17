package mass_backend.ServiceImpl;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import mass_backend.DAO.CarritoDAO;
import mass_backend.DAO.ProductoDAO;
import mass_backend.Entidad.Carrito;
import mass_backend.Entidad.CarritoItem;
import mass_backend.Entidad.Producto;
import mass_backend.Service.CarritoService;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoDAO carritoDAO;

    @Autowired
    private ProductoDAO productoDAO;

    @Override
    public Carrito obtenerPorId(Integer id) {
        return carritoDAO.findById(id).orElse(null);
    }

    @Override
    public Carrito agregarItem(Integer idCarrito, CarritoItem item) {
        Carrito carrito = obtenerPorId(idCarrito);
        if (carrito != null) {
            Producto prod = productoDAO.findById(item.getProducto().getIdProducto()).orElse(null);
            if (prod != null) {
                item.setProducto(prod);
                item.setCarrito(carrito);
                
                // Comprobar si el producto ya existe en el carrito para sumar la cantidad
                boolean existe = false;
                for (CarritoItem itemExistente : carrito.getItems()) {
                    if (itemExistente.getProducto().getIdProducto().equals(prod.getIdProducto())) {
                        itemExistente.setCantidad(itemExistente.getCantidad() + item.getCantidad());
                        existe = true;
                        break;
                    }
                }
                
                if (!existe) {
                    carrito.getItems().add(item);
                }
                
                carrito.setFechaActualizacion(LocalDateTime.now());
                return carritoDAO.save(carrito);
            }
        }
        return null;
    }

    @Override
    public Carrito removerItem(Integer idCarrito, Integer idItem) {
        Carrito carrito = obtenerPorId(idCarrito);
        if (carrito != null) {
            carrito.getItems().removeIf(item -> item.getId().equals(idItem));
            carrito.setFechaActualizacion(LocalDateTime.now());
            return carritoDAO.save(carrito);
        }
        return null;
    }

    @Override
    public Carrito limpiarCarrito(Integer idCarrito) {
        Carrito carrito = obtenerPorId(idCarrito);
        if (carrito != null) {
            carrito.getItems().clear();
            carrito.setFechaActualizacion(LocalDateTime.now());
            return carritoDAO.save(carrito);
        }
        return null;
    }
}