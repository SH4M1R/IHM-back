package mass_backend.ServiceImpl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import mass_backend.DAO.UsuarioDAO;
import mass_backend.Entidad.Usuario;
import mass_backend.Service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioDAO.findAll();
    }

    @Override
    public Usuario crearUsuario(Usuario usuario) {
        return usuarioDAO.save(usuario);
    }

    @Override
    public Usuario obtenerUsuarioPorId(Integer idUsuario) {
        return usuarioDAO.findById(idUsuario).orElse(null);
    }

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        return usuarioDAO.save(usuario);
    }

    @Override
    public void eliminarUsuario(Integer idUsuario) {
        usuarioDAO.deleteById(idUsuario);
    }

    @Override
    public Usuario autenticarUsuario(String correo, String password) {

        Usuario usuario = usuarioDAO.findByCorreo(correo)
                .orElse(null);
        if (usuario == null) {
            return null;
        }
        if (!usuario.getPassword().equals(password)) {
            return null;
        }
        return usuario;
    }
    
}