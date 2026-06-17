package mass_backend.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import mass_backend.DAO.VentaDAO;
import mass_backend.DAO.ProductoDAO;
import mass_backend.Entidad.Venta;
import mass_backend.Entidad.Producto;
import mass_backend.Service.VentaService;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaDAO ventaDAO;
    
    @Autowired
    private ProductoDAO productoDAO;

    @Override
    public List<Venta> listarVentas() {
        return ventaDAO.findAll();
    }

    @Override
    public Venta obtenerPorId(Integer id) {
        return ventaDAO.findById(id).orElse(null);
    }

    @Override
    public Venta registrarVenta(Venta venta) {
        venta.setFecha(LocalDateTime.now());
        double totalVenta = 0.0;

        if (venta.getDetalles() != null) {
            for (var detalle : venta.getDetalles()) {
                detalle.setVenta(venta);
                
                // Buscar precio real del producto para calcular subtotales de forma segura
                Producto prod = productoDAO.findById(detalle.getProducto().getIdProducto()).orElse(null);
                if (prod != null) {
                    detalle.setProducto(prod);
                    double subtotal = prod.getPrecio() * detalle.getCantidad();
                    detalle.setSubtotal(subtotal);
                    totalVenta += subtotal;
                    
                    // Opcional: Reducir stock del producto
                    if (prod.getStock() >= detalle.getCantidad()) {
                        prod.setStock(prod.getStock() - detalle.getCantidad());
                        productoDAO.save(prod);
                    }
                }
            }
        }
        venta.setTotal(totalVenta);
        return ventaDAO.save(venta);
    }
}