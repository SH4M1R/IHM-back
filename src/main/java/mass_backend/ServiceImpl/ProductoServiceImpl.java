package mass_backend.ServiceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mass_backend.DAO.ProductoDAO;
import mass_backend.Entidad.Producto;
import mass_backend.Service.ProductoService;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoDAO productoDAO;

    @Override
    public List<Producto> listarProductos() {
        return productoDAO.findAll();
    }

    @Override
    public Producto obtenerProductoPorId(Integer idProducto) {
        return productoDAO.findById(idProducto).orElse(null);
    }

    @Override
    public Producto crearProducto(Producto producto) {
        return productoDAO.save(producto);
    }

    @Override
    public Producto actualizarProducto(Producto producto) {
        return productoDAO.save(producto);
    }

    @Override
    public void eliminarProducto(Integer idProducto) {
        productoDAO.deleteById(idProducto);
    }

    @Override
    public List<Producto> listarProductosActivos() {
        return productoDAO.findByActivoTrue();
    }
}