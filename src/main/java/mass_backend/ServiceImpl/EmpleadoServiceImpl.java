package mass_backend.ServiceImpl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import mass_backend.DAO.EmpleadoDAO;
import mass_backend.Entidad.Empleado;
import mass_backend.Service.EmpleadoService;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    @Autowired
    private EmpleadoDAO empleadoDAO;

    @Override
    public List<Empleado> listarEmpleados() { 
        return empleadoDAO.findAll(); 
    }

    @Override
    public Empleado crearEmpleado(Empleado empleado) { 
        return empleadoDAO.save(empleado); 
    }

    @Override
    public Empleado obtenerEmpleadoPorId(Integer idEmpleado) { 
        return empleadoDAO.findById(idEmpleado).orElse(null); 
    }

    @Override
    public Empleado actualizarEmpleado(Empleado empleado) { 
        return empleadoDAO.save(empleado); 
    }

    @Override
    public void eliminarEmpleado(Integer idEmpleado) { 
        empleadoDAO.deleteById(idEmpleado); 
    }

    @Override
    public Empleado autenticarEmpleado(String username, String password) {
        Empleado empleado = empleadoDAO.findByUsername(username)
                .orElse(null);
        if (empleado == null) {
            return null;
        }
        if (!empleado.getPassword().equals(password)) {
            return null;
        }
        return empleado;
    }

}