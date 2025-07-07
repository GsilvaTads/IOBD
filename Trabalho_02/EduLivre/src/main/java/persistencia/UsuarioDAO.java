package persistencia;

import java.sql.Connection; 
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import negocio.PerfilUsuario;
import negocio.Usuario;

public class UsuarioDAO {

    public void inserir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (id, nome, email, senha, perfil) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, usuario.getId());
            stmt.setString(2, usuario.getNome());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getSenha());
            stmt.setString(5, usuario.getPerfil().toString()); // Salva o Enum como String
            int linhaAfetada = stmt.executeUpdate();

            if (linhaAfetada > 0) {
                System.out.println("Usuário inserido: " + usuario.getNome() + ". ID: " + usuario.getId());
            } else {
                System.out.println("Nenhum usuário foi inserido.");
            }
        }
    }

    public void inserirUsuarioCompleto(String nome, String email, String senha, String perfilStr) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);

        try {
            PerfilUsuario perfil = PerfilUsuario.valueOf(perfilStr.toUpperCase());
            usuario.setPerfil(perfil);
            inserir(usuario);
        } catch (IllegalArgumentException e) {
            System.out.println("Perfil de usuário inválido: " + perfilStr + ". Use 'aluno', 'professor' ou 'admin'.");
            
        }
    }

    public Usuario buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId((UUID) rs.getObject("id"));
                    usuario.setNome(rs.getString("nome"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setSenha(rs.getString("senha"));
                    usuario.setPerfil(PerfilUsuario.valueOf(rs.getString("perfil"))); // Converte a String de volta para Enum
                    return usuario;
                }
                return null;
            }
        }
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY nome";
        System.out.println("\n--- LISTA DE USUÁRIOS ---");
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("Nenhum usuário cadastrado.");
                return usuarios;
            }

            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId((UUID) rs.getObject("id"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setSenha(rs.getString("senha"));
                 usuario.setPerfil(PerfilUsuario.valueOf(rs.getString("perfil")));
                System.out.println(usuario); 
            }
        }
        return usuarios;
    }

    public void atualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nome = ?, email = ?, senha = ?, perfil = ? WHERE id = ?";
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getPerfil().toString()); 
            stmt.setObject(5, usuario.getId());
            stmt.executeUpdate();
        }
    }

    public void editarUsuarioCompleto(UUID idUsuario, String novoNome, String novoEmail, String novaSenha, String novoPerfilStr) throws SQLException {
        Usuario usuario = buscarPorId(idUsuario);

        if (usuario != null) {
            if (!novoNome.isEmpty()) usuario.setNome(novoNome);
            if (!novoEmail.isEmpty()) usuario.setEmail(novoEmail);
            if (!novaSenha.isEmpty()) usuario.setSenha(novaSenha);

            if (!novoPerfilStr.isEmpty()) {
                try {
                    PerfilUsuario novoPerfil = PerfilUsuario.valueOf(novoPerfilStr.toUpperCase());
                    usuario.setPerfil(novoPerfil);
                } catch (IllegalArgumentException e) {
                    System.out.println("Perfil de usuário inválido: " + novoPerfilStr + ". O perfil permanecerá inalterado.");
                    
                }
            }

            atualizar(usuario);
            System.out.println("Usuário atualizado com sucesso!");
        } else {
            System.out.println("Usuário com ID " + idUsuario + " não encontrado.");
        }
    }

    public void deletar(UUID id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Usuário deletado.");
            } else {
                System.out.println("Nenhum usuário encontrado com o ID: " + id);
            }
        }
    }
}