package mass_backend.Data;

import mass_backend.Entidad.Empleado;
import mass_backend.Entidad.Usuario;
import mass_backend.DAO.EmpleadoDAO;
import mass_backend.DAO.UsuarioDAO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final EmpleadoDAO empleadoDAO;
    private final UsuarioDAO usuarioDAO;

    public DataLoader(EmpleadoDAO empleadoDAO, UsuarioDAO usuarioDAO) {
        this.empleadoDAO = empleadoDAO;
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public void run(String... args) throws Exception {
        cargarDatosEmpleado();
        cargarDatosUsuario();
    }

    private void cargarDatosEmpleado() {
        if (empleadoDAO.findByUsername("admin@gmail.com").isEmpty()) {
            Empleado admin = new Empleado();
            admin.setNombre("Administrador Central");
            admin.setUsername("admin@gmail.com");
            admin.setPassword("admin123");

            empleadoDAO.save(admin);
            System.out.println(">> DataLoader: Empleado 'admin@gmail.com' insertado correctamente.");
        } else {
            System.out.println(">> DataLoader: El empleado 'admin@gmail.com' ya existe. Se omitió la inserción.");
        }
    }

    private void cargarDatosUsuario() {
        if (usuarioDAO.findByCorreo("juan.perez@mail.com").isEmpty()) {
            Usuario user1 = new Usuario();
            user1.setNombre("Juan");
            user1.setApellido("Pérez");
            user1.setDni(12345678L);
            user1.setCorreo("juan.perez@mail.com");
            user1.setPassword("juan123");
            usuarioDAO.save(user1);
            System.out.println(">> DataLoader: Usuario 'juan.perez@mail.com' insertado.");
        }

        if (usuarioDAO.findByCorreo("maria.lopez@mail.com").isEmpty()) {
            Usuario user2 = new Usuario();
            user2.setNombre("María");
            user2.setApellido("López");
            user2.setDni(87654321L);
            user2.setCorreo("maria.lopez@mail.com");
            user2.setPassword("maria2026");
            usuarioDAO.save(user2);
            System.out.println(">> DataLoader: Usuario 'maria.lopez@mail.com' insertado.");
        }

        if (usuarioDAO.findByCorreo("carlos.mendoza@mail.com").isEmpty()) {
            Usuario user3 = new Usuario();
            user3.setNombre("Carlos");
            user3.setApellido("Mendoza");
            user3.setDni(45678912L);
            user3.setCorreo("carlos.mendoza@mail.com");
            user3.setPassword("carlos123");
            usuarioDAO.save(user3);
            System.out.println(">> DataLoader: Usuario 'carlos.mendoza@mail.com' insertado.");
        }
    }
}