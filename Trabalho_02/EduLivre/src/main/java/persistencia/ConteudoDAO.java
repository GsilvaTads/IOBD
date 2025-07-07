package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import negocio.Conteudo;
import negocio.Curso;

public class ConteudoDAO {

    public void inserir(Conteudo conteudo) throws SQLException {
        String sql = "INSERT INTO conteudo (curso_id, titulo, descricao, tipo, arquivo) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setObject(1, conteudo.getCurso().getId());
            stmt.setString(2, conteudo.getTitulo());
            stmt.setString(3, conteudo.getDescricao());
            stmt.setString(4, conteudo.getTipo());
            stmt.setBytes(5, conteudo.getArquivo());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    conteudo.setId(rs.getInt(1));
                    System.out.println("Conteúdo inserido. ID: " + conteudo.getId());
                }
            }
        }
    }

    public void inserirConteudoCompleto(UUID idCurso, String titulo, String descricao, String tipo, byte[] arquivoBytes) throws SQLException {
        CursoDAO cursoDAO = new CursoDAO();
        Curso curso = cursoDAO.buscarPorId(idCurso);

        if (curso != null) {
            Conteudo conteudo = new Conteudo();
            conteudo.setCurso(curso);
            conteudo.setTitulo(titulo);
            conteudo.setDescricao(descricao);
            conteudo.setTipo(tipo);
            conteudo.setArquivo(arquivoBytes);
            inserir(conteudo);
        } else {
            System.out.println("Curso com ID " + idCurso + " não encontrado. Conteúdo não pode ser inserido.");
        }
    }

    public Conteudo buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM conteudo WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Conteudo conteudo = new Conteudo();
                    Curso curso = new Curso();

                    conteudo.setId(rs.getInt("id"));
                    Object cursoIdObj = rs.getObject("curso_id");
                    if (cursoIdObj instanceof UUID) {
                        curso.setId((UUID) cursoIdObj);
                    } else if (cursoIdObj != null) {
                        System.err.println("Atenção: ID do curso não é um UUID para o conteúdo ID: " + conteudo.getId());
                        curso.setId(null);
                    } else {
                        curso.setId(null);
                    }
                    conteudo.setCurso(curso);
                    conteudo.setTitulo(rs.getString("titulo"));
                    conteudo.setDescricao(rs.getString("descricao"));
                    conteudo.setTipo(rs.getString("tipo"));
                    conteudo.setArquivo(rs.getBytes("arquivo")); 

                    return conteudo;
                }
            }
        }
        return null;
    }

    public void listarTodos() throws SQLException {
        String sql = "SELECT id, curso_id, titulo, descricao, tipo, OCTET_LENGTH(arquivo) AS tamanho_arquivo FROM conteudo ORDER BY titulo";
        
        System.out.println("\n--- LISTA DE CONTEÚDOS ---");
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("Nenhum conteúdo cadastrado.");
                return;
            }

            while (rs.next()) {
                int id = rs.getInt("id");
                String titulo = rs.getString("titulo");
                UUID curso_id = (UUID) rs.getObject("curso_id");
                String tipo = rs.getString("tipo");
                long tamanhoArquivo = rs.getLong("tamanho_arquivo"); 

                System.out.println("ID: " + id +
                                   ", Nome do Arquivo: " + titulo + 
                                   ", Curso ID: " + curso_id +
                                   ", Tipo: " + tipo +
                                   ", Tamanho do Arquivo: " + tamanhoArquivo + " bytes");
            }
        }
    }

    public void listarConteudosPorCurso(UUID idCurso) throws SQLException {
        String sql = "SELECT id, titulo, tipo, OCTET_LENGTH(arquivo) AS tamanho_arquivo " +
                     "FROM conteudo WHERE curso_id = ? ORDER BY titulo";
        
        System.out.println("\n--- CONTEÚDOS PARA O CURSO ID: " + idCurso + " ---");

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idCurso);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("Nenhum conteúdo encontrado para este curso.");
                    return;
                }

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String titulo = rs.getString("titulo");
                    String tipo = rs.getString("tipo");
                    long tamanhoArquivo = rs.getLong("tamanho_arquivo");

                    System.out.println("ID: " + id +
                                       ", Nome do Arquivo: " + titulo + 
                                       ", Tipo: " + tipo +
                                       ", Tamanho do Arquivo: " + tamanhoArquivo + " bytes");
                }
            }
        }
    }

    public void atualizar(Conteudo conteudo) throws SQLException {
        String sql = "UPDATE conteudo SET curso_id = ?, titulo = ?, descricao = ?, tipo = ?, arquivo = ? WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, conteudo.getCurso() != null ? conteudo.getCurso().getId() : null);
            stmt.setString(2, conteudo.getTitulo());
            stmt.setString(3, conteudo.getDescricao());
            stmt.setString(4, conteudo.getTipo());
            stmt.setBytes(5, conteudo.getArquivo());
            stmt.setInt(6, conteudo.getId());
            stmt.executeUpdate();
        }
    }

    public void editarConteudoCompleto(int idConteudo, UUID novoIdCurso, String novoTitulo, String novaDescricao, String novoTipo, byte[] novoArquivoBytes) throws SQLException {
        Conteudo conteudo = buscarPorId(idConteudo);

        if (conteudo != null) {
            if (novoIdCurso != null && !novoIdCurso.toString().equals("00000000-0000-0000-0000-000000000000")) {
                CursoDAO cursoDAO = new CursoDAO();
                Curso novoCurso = cursoDAO.buscarPorId(novoIdCurso);
                if (novoCurso != null) {
                    conteudo.setCurso(novoCurso);
                } else {
                    System.out.println("Novo curso com ID " + novoIdCurso + " não encontrado. Mantendo o curso atual.");
                }
            } else if (novoIdCurso != null && novoIdCurso.toString().equals("00000000-0000-0000-0000-000000000000")) {
                
            }

            if (!novoTitulo.isEmpty()) conteudo.setTitulo(novoTitulo);
            if (!novaDescricao.isEmpty()) conteudo.setDescricao(novaDescricao);
            if (!novoTipo.isEmpty()) conteudo.setTipo(novoTipo);            
            
            if (novoArquivoBytes != null) {
                conteudo.setArquivo(novoArquivoBytes); 
            }

            atualizar(conteudo);
            System.out.println("Conteúdo atualizado com sucesso!");
        } else {
            System.out.println("Conteúdo com ID " + idConteudo + " não encontrado.");
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM conteudo WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("Conteúdo deletado.");
            } else {
                System.out.println("Nenhum conteúdo encontrado no ID: " + id);
            }
        }
    }
}