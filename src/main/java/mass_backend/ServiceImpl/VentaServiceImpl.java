package mass_backend.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mass_backend.DAO.VentaDAO;
import mass_backend.DAO.EmpleadoDAO;
import mass_backend.DAO.ProductoDAO;
import mass_backend.DAO.UsuarioDAO;
import mass_backend.Entidad.Venta;
import mass_backend.Entidad.Empleado;
import mass_backend.Entidad.Producto;
import mass_backend.Entidad.Usuario;
import mass_backend.Service.VentaService;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaDAO ventaDAO;
    
    @Autowired
    private ProductoDAO productoDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private EmpleadoDAO empleadoDAO;

    @Override
    public List<Venta> listarVentas() {
        return ventaDAO.findAll();
    }

    @Override
    public Venta obtenerPorId(Integer id) {
        return ventaDAO.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Venta registrarVenta(Venta venta) {
        venta.setFecha(LocalDateTime.now());

        if (venta.getUsuario() != null && venta.getUsuario().getIdUsuario() != null) {
            Usuario usr = usuarioDAO.findById(venta.getUsuario().getIdUsuario()).orElse(null);
            if (usr == null) {
                throw new RuntimeException("Usuario no encontrado en la base de datos");
            }
            venta.setUsuario(usr);
        } else {
            throw new RuntimeException("El ID del usuario es obligatorio para registrar la venta");
        }

        double totalVenta = 0.0;
        
        if (venta.getDetalles() != null) {
            for (var detalle : venta.getDetalles()) {
                detalle.setVenta(venta);

                Producto prod = productoDAO.findById(detalle.getProducto().getIdProducto()).orElse(null);
                if (prod != null) {
                    detalle.setProducto(prod);
                    double subtotal = prod.getPrecio() * detalle.getCantidad();
                    detalle.setSubtotal(subtotal);
                    totalVenta += subtotal;
                    int stockActual = prod.getStock() != null ? prod.getStock() : 0;
                    prod.setStock(stockActual - detalle.getCantidad());
                    productoDAO.save(prod);
                } else {
                    throw new RuntimeException("Producto no encontrado con el ID proporcionado");
                }
            }
        }
        
        venta.setTotal(totalVenta);
        return ventaDAO.save(venta);
    }

    public Venta asignarEmpleado(Integer idVenta, Integer idEmpleado) {
        java.util.Optional<Venta> optVenta = ventaDAO.findById(idVenta);
        if (!optVenta.isPresent()) {
            return null;
        }
        Venta venta = optVenta.get();
        java.util.Optional<Empleado> optEmpleado = empleadoDAO.findById(idEmpleado);
        if (!optEmpleado.isPresent()) {
        }
        Empleado empleado = optEmpleado.get();
        venta.setEmpleado(empleado); 
         return ventaDAO.save(venta);
    }

    @Override
    public List<Venta> listarVentasPorEmpleado(Integer idEmpleado) {
        return ventaDAO.findByEmpleadoIdEmpleado(idEmpleado);
    }

    @Override
    public Venta actualizarEstado(Integer idVenta, String nuevoEstado) {
        java.util.Optional<Venta> optVenta = ventaDAO.findById(idVenta);
        if (!optVenta.isPresent()) {
            return null;
        }
        
        Venta venta = optVenta.get();
        venta.setEstado(nuevoEstado);
        
        return ventaDAO.save(venta);
    }

    public List<Venta> listarVentasPorUsuario(Integer idUsuario) {
        return ventaDAO.findByUsuarioIdUsuario(idUsuario);
    }
}