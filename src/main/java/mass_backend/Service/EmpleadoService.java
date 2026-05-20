package mass_backend.Service;

import java.util.List;
import mass_backend.Entidad.Empleado;

public interface EmpleadoService {
    List<Empleado> listarEmpleados();
    Empleado crearEmpleado(Empleado empleado);
    Empleado obtenerEmpleadoPorId(Integer idEmpleado);
    Empleado actualizarEmpleado(Empleado empleado);
    void eliminarEmpleado(Integer idEmpleado);
    Empleado autenticarEmpleado(String usuario, String contrasena);
}
