package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import negocio.Curso;
import negocio.Usuario;

public class CursoDAO {

    public void inserir(Curso curso) throws SQLException {
        String sql = "INSERT INTO curso (id, titulo, descricao, data_criacao, avaliacao) VALUES (?, ?, ?, ?, ?::jsonb)";
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, curso.getId());
            stmt.setString(2, curso.getTitulo());
            stmt.setString(3, curso.getDescricao());
            stmt.setDate(4, java.sql.Date.valueOf(curso.getDataCriacao()));
            stmt.setString(5, curso.getAvaliacao().toString());
            int linhaAfetada = stmt.executeUpdate();

            if (linhaAfetada > 0) {
                System.out.println("Curso inserido: " + curso.getTitulo() + ". ID: " + curso.getId());
            } else {
                System.out.println("Nenhum curso foi inserido.");
            }
        }
    }

    public void inserirCursoCompleto(String titulo, String descricao) throws SQLException {
        Curso curso = new Curso();
        curso.setId(UUID.randomUUID());
        curso.setTitulo(titulo);
        curso.setDescricao(descricao);
        curso.setDataCriacao(LocalDate.now());

        JSONObject avaliacaoInicial = new JSONObject();
        avaliacaoInicial.put("media", 0.0);
        avaliacaoInicial.put("comentarios", new JSONArray());
        curso.setAvaliacao(avaliacaoInicial);

        inserir(curso);
    }

    public Curso buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT * FROM curso WHERE id = ?";
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Curso curso = new Curso();
                    curso.setId((UUID) rs.getObject("id"));
                    curso.setTitulo(rs.getString("titulo"));
                    curso.setDescricao(rs.getString("descricao"));
                    curso.setDataCriacao(rs.getDate("data_criacao").toLocalDate());
                    curso.setAvaliacao(new JSONObject(rs.getString("avaliacao")));
                    return curso;
                }
                return null;
            }
        }
    }

    public void listarTodos() throws SQLException {
        String sql = "SELECT c.id, c.titulo, c.descricao, c.data_criacao, c.avaliacao, " +
                     "COUNT(m.id) AS total_matriculados " +
                     "FROM curso c " +
                     "LEFT JOIN matricula m ON c.id = m.curso_id " +
                     "GROUP BY c.id, c.titulo, c.descricao, c.data_criacao, c.avaliacao " +
                     "ORDER BY c.titulo";
        
        System.out.println("\n--- LISTA DE CURSOS ---");
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("Nenhum curso cadastrado.");
                return;
            }

            while (rs.next()) {
                Curso curso = new Curso();
                curso.setId((UUID) rs.getObject("id"));
                curso.setTitulo(rs.getString("titulo"));
                curso.setDescricao(rs.getString("descricao"));
                curso.setDataCriacao(rs.getDate("data_criacao").toLocalDate());
                
                String avaliacaoJsonString = rs.getString("avaliacao");
                JSONObject avaliacao = new JSONObject(avaliacaoJsonString);
                curso.setAvaliacao(avaliacao);

                double mediaNota = avaliacao.optDouble("media", 0.0);
                int totalMatriculados = rs.getInt("total_matriculados");

                System.out.println("ID: " + curso.getId() +
                                   ", Título: " + curso.getTitulo() +
                                   ", Descrição: " + curso.getDescricao() +
                                   ", Data Criação: " + curso.getDataCriacao() +
                                   ", Média de Nota: " + String.format("%.2f", mediaNota) +
                                   ", Matriculados: " + totalMatriculados);
                
                System.out.println("--------------------------------------------------");
            }
        }
    }

    public void atualizar(Curso curso) throws SQLException {
        String sql = "UPDATE curso SET titulo = ?, descricao = ?, data_criacao = ?, avaliacao = ?::jsonb WHERE id = ?";
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, curso.getTitulo());
            stmt.setString(2, curso.getDescricao());
            stmt.setDate(3, java.sql.Date.valueOf(curso.getDataCriacao()));
            stmt.setString(4, curso.getAvaliacao().toString());
            stmt.setObject(5, curso.getId());
            stmt.executeUpdate();
        }
    }

    public void editarCursoCompleto(UUID idCurso, String novoTitulo, String novaDescricao) throws SQLException {
        Curso curso = buscarPorId(idCurso);

        if (curso != null) {
            if (!novoTitulo.isEmpty()) curso.setTitulo(novoTitulo);
            if (!novaDescricao.isEmpty()) curso.setDescricao(novaDescricao);

            atualizar(curso);
            System.out.println("Curso atualizado com sucesso!");
        } else {
            System.out.println("Curso com ID " + idCurso + " não encontrado.");
        }
    }

    public void deletar(UUID id) throws SQLException {
        String sql = "DELETE FROM curso WHERE id = ?";
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Curso deletado.");
            } else {
                System.out.println("Nenhum curso encontrado com o ID: " + id);
            }
        }
    }

    // MÉTODO AJUSTADO: Agora aceita Integer para nota (pode ser null)
    public void adicionarAvaliacaoComentario(UUID idCurso, UUID idUsuarioAvaliador, Integer nota, String comentarioTexto) throws SQLException {
        Curso curso = buscarPorId(idCurso);
        if (curso == null) {
            System.out.println("Curso com ID " + idCurso + " não encontrado.");
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Usuario usuario = usuarioDAO.buscarPorId(idUsuarioAvaliador);
        if (usuario == null) {
            System.out.println("Usuário avaliador com ID " + idUsuarioAvaliador + " não encontrado.");
            return;
        }

        JSONObject avaliacaoCurso = curso.getAvaliacao();
        if (avaliacaoCurso == null) {
            avaliacaoCurso = new JSONObject();
            avaliacaoCurso.put("media", 0.0);
            avaliacaoCurso.put("comentarios", new JSONArray());
        }

        JSONArray comentarios = avaliacaoCurso.getJSONArray("comentarios");

        JSONObject novoComentario = new JSONObject();
        novoComentario.put("usuario_id", usuario.getId().toString());
        
        // Inclui a nota APENAS se ela não for null
        if (nota != null) {
            novoComentario.put("nota", nota);
        }
        
        // Inclui o comentário APENAS se não for null ou vazio
        if (comentarioTexto != null && !comentarioTexto.isEmpty()) {
            novoComentario.put("comentario", comentarioTexto);
        }
        
        novoComentario.put("data", LocalDateTime.now().toString());
        comentarios.put(novoComentario);

        double somaNotas = 0;
        int contadorNotasValidas = 0;
        for (int i = 0; i < comentarios.length(); i++) {
            JSONObject comentarioExistente = comentarios.getJSONObject(i);
            // Verifica se o campo "nota" existe e não é nulo no JSON
            if (comentarioExistente.has("nota") && !comentarioExistente.isNull("nota")) {
                somaNotas += comentarioExistente.getInt("nota");
                contadorNotasValidas++;
            }
        }
        
        double novaMedia = (contadorNotasValidas > 0) ? (somaNotas / contadorNotasValidas) : 0.0;
        avaliacaoCurso.put("media", novaMedia);

        curso.setAvaliacao(avaliacaoCurso);
        atualizar(curso);
        System.out.println("Avaliação (nota e/ou comentário) adicionada com sucesso!");
    }

    public void mostrarAvaliacaoDoCurso(UUID idCurso) throws SQLException {
        String sql = "SELECT avaliacao FROM curso WHERE id = ?";
        System.out.println("\n--- AVALIAÇÃO E COMENTÁRIOS DO CURSO (ID: " + idCurso + ") ---");

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, idCurso);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String avaliacaoJsonString = rs.getString("avaliacao");
                    if (avaliacaoJsonString != null && !avaliacaoJsonString.isEmpty()) {
                        JSONObject avaliacaoCurso = new JSONObject(avaliacaoJsonString);

                        if (avaliacaoCurso.has("media")) {
                            System.out.println("  Média: " + String.format("%.2f", avaliacaoCurso.getDouble("media")));
                        } else {
                            System.out.println("  Média: Não disponível");
                        }

                        if (avaliacaoCurso.has("comentarios")) {
                            JSONArray comentariosArray = avaliacaoCurso.getJSONArray("comentarios");
                            if (comentariosArray.length() > 0) {
                                System.out.println("  Comentários:");
                                for (int i = 0; i < comentariosArray.length(); i++) {
                                    JSONObject comentario = comentariosArray.getJSONObject(i);
                                    System.out.println("    - Usuário ID: " + comentario.getString("usuario_id"));
                                    // Verifica se a nota existe antes de tentar exibi-la
                                    if (comentario.has("nota") && !comentario.isNull("nota")) {
                                        System.out.println("      Nota: " + comentario.getInt("nota"));
                                    } else {
                                        System.out.println("      Nota: N/A");
                                    }
                                    // Verifica se o comentário existe antes de tentar exibi-lo
                                    if (comentario.has("comentario") && !comentario.isNull("comentario")) {
                                        System.out.println("      Comentário: " + comentario.getString("comentario"));
                                    } else {
                                        System.out.println("      Comentário: N/A");
                                    }
                                    System.out.println("      Data: " + comentario.getString("data"));
                                }
                            } else {
                                System.out.println("  Nenhum comentário disponível.");
                            }
                        } else {
                            System.out.println("  Seção de comentários não encontrada.");
                        }
                    } else {
                        System.out.println("O curso não possui dados de avaliação.");
                    }
                } else {
                    System.out.println("Curso com ID " + idCurso + " não encontrado.");
                }
            }
        }
    }
}