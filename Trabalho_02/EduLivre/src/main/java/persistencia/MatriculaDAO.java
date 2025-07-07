package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import negocio.Curso;
import negocio.Matricula;
import negocio.Usuario;

public class MatriculaDAO {

    public void inserir(Matricula matricula) throws SQLException {
        String sql = "INSERT INTO matricula (usuario_id, curso_id, data_matricula) VALUES (?, ?, ?)";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setObject(1, matricula.getUsuario().getId());
            stmt.setObject(2, matricula.getCurso().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(matricula.getDataMatricula()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    matricula.setId(rs.getInt(1));
                    System.out.println("Matrícula inserida. ID: " + matricula.getId());
                } else {
                    System.out.println("Matrícula inserida, mas o ID gerado não foi recuperado.");
                }
            }
        }
    }
    
    public boolean existeMatricula(UUID usuarioId, UUID cursoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM matricula WHERE usuario_id = ? AND curso_id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, usuarioId);
            stmt.setObject(2, cursoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; 
                }
            }
        }
        return false; 
    }

    public void inserirMatriculaCompleta(UUID idUsuario, UUID idCurso) throws SQLException {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);

        if (usuario == null) {
            System.out.println("Usuário com ID " + idUsuario + " não encontrado. Matrícula não pode ser criada.");
            return;
        }

        CursoDAO cursoDAO = new CursoDAO();
        Curso curso = cursoDAO.buscarPorId(idCurso);

        if (curso == null) {
            System.out.println("Curso com ID " + idCurso + " não encontrado. Matrícula não pode ser criada.");
            return;
        }
        
        if (existeMatricula(idUsuario, idCurso)) {
            System.out.println("Erro: O usuário " + usuario.getNome() + " já está matriculado no curso " + curso.getTitulo() + ".");
            return;
        }

        Matricula matricula = new Matricula();
        matricula.setUsuario(usuario);
        matricula.setCurso(curso);
        matricula.setDataMatricula(LocalDateTime.now());
        inserir(matricula);
    }


    public Matricula buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM matricula WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Matricula matricula = new Matricula();
                    Usuario usuario = new Usuario();
                    Curso curso = new Curso();

                    matricula.setId(rs.getInt("id"));
                    usuario.setId((UUID) rs.getObject("usuario_id"));
                    curso.setId((UUID) rs.getObject("curso_id"));
                    matricula.setUsuario(usuario);
                    matricula.setCurso(curso);
                    matricula.setDataMatricula(rs.getTimestamp("data_matricula").toLocalDateTime());

                    return matricula;
                }
            }
        }
        return null;
    }

    public void listarTodas() throws SQLException {
        String sql = "SELECT * FROM matricula";
        List<Matricula> lista = new ArrayList<>();

        System.out.println("\n--- LISTA DE MATRÍCULAS ---");
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Matricula matricula = new Matricula();
                Usuario usuario = new Usuario();
                Curso curso = new Curso();

                matricula.setId(rs.getInt("id"));
                Object usuarioIdObj = rs.getObject("usuario_id");
                if (usuarioIdObj instanceof UUID) {
                    usuario.setId((UUID) usuarioIdObj);
                } else {
                    usuario.setId(null);
                }

                Object cursoIdObj = rs.getObject("curso_id");
                if (cursoIdObj instanceof UUID) {
                    curso.setId((UUID) cursoIdObj);
                } else {
                    curso.setId(null);
                }
                
                matricula.setUsuario(usuario);
                matricula.setCurso(curso);
                matricula.setDataMatricula(rs.getTimestamp("data_matricula").toLocalDateTime());

                lista.add(matricula);
            }

            if (lista.isEmpty()) {
                System.out.println("Nenhuma matrícula cadastrada.");
            } else {
                for (Matricula m : lista) {
                    System.out.println("ID: " + m.getId() +
                                       ", Usuário ID: " + (m.getUsuario() != null && m.getUsuario().getId() != null ? m.getUsuario().getId() : "N/A") +
                                       ", Curso ID: " + (m.getCurso() != null && m.getCurso().getId() != null ? m.getCurso().getId() : "N/A") +
                                       ", Data Matrícula: " + m.getDataMatricula());
                }
            }
        }
    }

    public void atualizar(Matricula matricula) throws SQLException {
        String sql = "UPDATE matricula SET usuario_id = ?, curso_id = ?, data_matricula = ? WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, matricula.getUsuario() != null ? matricula.getUsuario().getId() : null);
            stmt.setObject(2, matricula.getCurso() != null ? matricula.getCurso().getId() : null);
            stmt.setTimestamp(3, Timestamp.valueOf(matricula.getDataMatricula()));
            stmt.setInt(4, matricula.getId());
            stmt.executeUpdate();
        }
    }

    public void editarMatriculaCompleta(int idMatricula, UUID novoIdUsuario, UUID novoIdCurso) throws SQLException {
        Matricula matricula = buscarPorId(idMatricula);

        if (matricula != null) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            CursoDAO cursoDAO = new CursoDAO();

            UUID usuarioIdAtual = matricula.getUsuario() != null ? matricula.getUsuario().getId() : null;
            UUID cursoIdAtual = matricula.getCurso() != null ? matricula.getCurso().getId() : null;

            boolean mudouUsuario = novoIdUsuario != null && !novoIdUsuario.toString().equals("00000000-0000-0000-0000-000000000000") && !novoIdUsuario.equals(usuarioIdAtual);
            boolean mudouCurso = novoIdCurso != null && !novoIdCurso.toString().equals("00000000-0000-0000-0000-000000000000") && !novoIdCurso.equals(cursoIdAtual);
            
            if (mudouUsuario || mudouCurso) {
                UUID idUsuarioParaVerificar = mudouUsuario ? novoIdUsuario : usuarioIdAtual;
                UUID idCursoParaVerificar = mudouCurso ? novoIdCurso : cursoIdAtual;

                if (idUsuarioParaVerificar != null && idCursoParaVerificar != null && existeMatricula(idUsuarioParaVerificar, idCursoParaVerificar)) {
                    System.out.println("Erro: A nova combinação de usuário e curso já existe em outra matrícula.");
                    return; 
                }
            }


            if (novoIdUsuario != null && !novoIdUsuario.toString().equals("00000000-0000-0000-0000-000000000000")) {
                Usuario novoUsuario = usuarioDAO.buscarPorId(novoIdUsuario);
                if (novoUsuario != null) {
                    matricula.setUsuario(novoUsuario);
                } else {
                    System.out.println("Novo usuário com ID " + novoIdUsuario + " não encontrado. Mantendo o usuário atual.");
                }
            }

            if (novoIdCurso != null && !novoIdCurso.toString().equals("00000000-0000-0000-0000-000000000000")) {
                Curso novoCurso = cursoDAO.buscarPorId(novoIdCurso);
                if (novoCurso != null) {
                    matricula.setCurso(novoCurso);
                } else {
                    System.out.println("Novo curso com ID " + novoIdCurso + " não encontrado. Mantendo o curso atual.");
                }
            }

            atualizar(matricula);
            System.out.println("Matrícula atualizada com sucesso!");
        } else {
            System.out.println("Matrícula com ID " + idMatricula + " não encontrada.");
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM matricula WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("Matrícula deletada.");
            } else {
                System.out.println("Nenhuma matrícula encontrada para deletar com o ID: " + id);
            }
        }
    }
}