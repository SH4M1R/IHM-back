package mass_backend.ServiceImpl;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import mass_backend.DAO.CarritoDAO;
import mass_backend.DAO.ProductoDAO;
import mass_backend.DAO.UsuarioDAO;
import mass_backend.Entidad.Carrito;
import mass_backend.Entidad.CarritoItem;
import mass_backend.Entidad.Producto;
import mass_backend.Entidad.Usuario;
import mass_backend.Service.CarritoService;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoDAO carritoDAO;

    @Autowired
    private ProductoDAO productoDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

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

    @Override
    public Carrito obtenerPorUsuario(Integer idUsuario) {
        return carritoDAO.findByUsuario_IdUsuario(idUsuario);
    }

    @Override
    public Carrito crearCarritoParaUsuario(Integer idUsuario) {
        Usuario usuario = usuarioDAO.findById(idUsuario).orElse(null);
        if (usuario == null) return null;
        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario);
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoDAO.save(carrito);
    }

    // NUEVO MÉTODO IMPLEMENTADO
    @Override
    public Carrito actualizarCantidadItem(Integer idCarrito, Integer idItem, Integer nuevaCantidad) {
        Carrito carrito = obtenerPorId(idCarrito);
        if (carrito != null && carrito.getItems() != null) {
            boolean modificado = false;
            
            for (CarritoItem item : carrito.getItems()) {
                if (item.getId().equals(idItem)) {
                    item.setCantidad(nuevaCantidad);
                    modificado = true;
                    break;
                }
            }
            
            if (modificado) {
                carrito.setFechaActualizacion(LocalDateTime.now());
                return carritoDAO.save(carrito);
            }
        }
        return null;
    }
}