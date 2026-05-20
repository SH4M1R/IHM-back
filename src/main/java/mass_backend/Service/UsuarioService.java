package mass_backend.Service;

import java.util.List;
import mass_backend.Entidad.Usuario;

public interface UsuarioService {
List<Usuario> listarUsuarios();
    Usuario crearUsuario(Usuario usuario);
    Usuario obtenerUsuarioPorId(Integer idUsuario);
    Usuario actualizarUsuario(Usuario usuario);
    void eliminarUsuario(Integer idUsuario);
    Usuario autenticarUsuario(String usuario, String contrasena);
}
