package apresentacao;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.UUID;

import negocio.Conteudo;
import negocio.Curso;
import negocio.Matricula;
import negocio.Usuario;
import persistencia.ConteudoDAO;
import persistencia.CursoDAO;
import persistencia.MatriculaDAO;
import persistencia.UsuarioDAO;

public class MenuPrincipal {
    private Scanner scanner = new Scanner(System.in);
    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private CursoDAO cursoDAO = new CursoDAO();
    private ConteudoDAO conteudoDAO = new ConteudoDAO();
    private MatriculaDAO matriculaDAO = new MatriculaDAO();

    public void iniciar() {
        int opcao;
        do {
            exibirMenu();
            opcao = getIntInput("Escolha uma opção: ");

            try {
                switch (opcao) {
                    case 1:
                        operacaoInserirUsuario();
                        break;
                    case 2:
                        operacaoEditarUsuario();
                        break;
                    case 3:
                        operacaoDeletarUsuario();
                        break;
                    case 4:
                        usuarioDAO.listarTodos();
                        break;
                    case 5:
                        operacaoInserirCurso();
                        break;
                    case 6:
                        operacaoEditarCurso();
                        break;
                    case 7:
                        operacaoDeletarCurso();
                        break;
                    case 8:
                        cursoDAO.listarTodos();
                        break;
                    case 9:
                        operacaoAdicionarSomenteNotaAoCurso();
                        break;
                    case 10:
                        operacaoAdicionarSomenteComentarioAoCurso();
                        break;
                    case 11:
                        operacaoMostrarAvaliacaoCurso();
                        break;
                    case 12:
                        operacaoInserirConteudo();
                        break;
                    case 13:
                        operacaoEditarConteudo();
                        break;
                    case 14:
                        operacaoDeletarConteudo();
                        break;
                    case 15:
                        operacaoMostrarConteudosDeUmCurso();
                        break;
                    case 16:
                        conteudoDAO.listarTodos();
                        break;
                    case 17:
                        operacaoDownloadArquivoConteudo();
                        break;
                    case 18:
                        operacaoAbrirArquivoConteudo();
                        break;
                    case 19: 
                        operacaoInserirMatricula();
                        break;
                    case 20:
                        operacaoEditarMatricula();
                        break;
                    case 21:
                        operacaoDeletarMatricula();
                        break;
                    case 22:
                        matriculaDAO.listarTodas();
                        break;
                    case 0:
                        System.out.println("Saindo...");
                        break;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            } catch (SQLException e) {
                System.err.println("Erro de banco de dados: " + e.getMessage());
                // e.printStackTrace(); // depuração
            } catch (Exception e) {
                System.err.println("Ocorreu um erro inesperado: " + e.getMessage());
                // e.printStackTrace(); // depuração
            }
            System.out.println("\nPressione Enter para continuar...");
            scanner.nextLine(); 
        } while (opcao != 0);

        scanner.close();
    }

    private void exibirMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("USUÁRIOS:");
        System.out.println("  1. Inserir novo usuário");
        System.out.println("  2. Editar usuário existente");
        System.out.println("  3. Deletar usuário");
        System.out.println("  4. Listar todos os usuários");
        System.out.println("CURSOS:");
        System.out.println("  5. Inserir novo curso");
        System.out.println("  6. Editar curso existente");
        System.out.println("  7. Deletar curso");
        System.out.println("  8. Listar todos os cursos");
        System.out.println("  9. Adicionar nota de avaliação à um curso");
        System.out.println(" 10. Adicionar comentário à um curso");
        System.out.println(" 11. Mostrar avaliação e comentários de um curso");
        System.out.println("CONTEÚDOS:");
        System.out.println(" 12. Inserir novo conteúdo");
        System.out.println(" 13. Editar conteúdo existente");
        System.out.println(" 14. Deletar conteúdo");
        System.out.println(" 15. Listar conteúdos de um curso");
        System.out.println(" 16. Listar todos os conteúdos");
        System.out.println(" 17. Baixar arquivo de conteúdo");
        System.out.println(" 18. Mostrar arquivo de conteúdo"); 
        System.out.println("MATRÍCULAS:");
        System.out.println(" 19. Inserir nova matrícula");
        System.out.println(" 20. Editar matrícula existente");
        System.out.println(" 21. Deletar matrícula");
        System.out.println(" 22. Listar todas as matrículas");
        System.out.println("0. Sair");
        System.out.print("----------------------------------------\n");
    }

    // Entrada do usuário
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, digite um número inteiro.");
                scanner.next(); 
            } finally {
                scanner.nextLine(); 
            }
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private UUID getUUIDInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return UUID.fromString(scanner.nextLine());
            } catch (IllegalArgumentException e) {
                System.out.println("UUID inválido. Por favor, digite um UUID válido.");
            }
        }
    }

    // Leitura de arquivo em um array de bytes
    private byte[] readFileToBytes(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Arquivo não encontrado em: " + filePath);
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
            return null;
        }
    }

    // Métodos do menu

    // USUÁRIO   
    private void operacaoInserirUsuario() throws SQLException {
        System.out.println("\n--- INSERIR NOVO USUÁRIO ---");
        String nome = getStringInput("Nome do usuário: ");
        String email = getStringInput("Email do usuário: ");
        String senha = getStringInput("Senha do usuário: ");
        String perfilStr = getStringInput("Perfil do usuário (aluno, professor, admin): "); // Pega a String do perfil
        usuarioDAO.inserirUsuarioCompleto(nome, email, senha, perfilStr);
    }

    private void operacaoEditarUsuario() throws SQLException {
        System.out.println("\n--- EDITAR USUÁRIO ---");
        UUID id = getUUIDInput("ID do usuário a ser editado: ");
        Usuario usuarioExistente = usuarioDAO.buscarPorId(id);

        if (usuarioExistente != null) {
            System.out.println("Usuário encontrado. Digite os novos dados (deixe em branco para manter o atual):");
            String novoNome = getStringInput("Novo nome (" + usuarioExistente.getNome() + "): ");
            String novoEmail = getStringInput("Novo email (" + usuarioExistente.getEmail() + "): ");
            String novaSenha = getStringInput("Nova senha (******): ");
            String novoPerfilStr = getStringInput("Novo perfil (" + usuarioExistente.getPerfil() + "): ");

            usuarioDAO.editarUsuarioCompleto(id, novoNome, novoEmail, novaSenha, novoPerfilStr);
        } else {
            System.out.println("Usuário com ID " + id + " não encontrado.");
        }
    }

    private void operacaoDeletarUsuario() throws SQLException {
        System.out.println("\n--- DELETAR USUÁRIO ---");
        UUID id = getUUIDInput("ID do usuário a ser deletado: ");
        usuarioDAO.deletar(id);
    }

    // CURSO
    private void operacaoInserirCurso() throws SQLException {
        System.out.println("\n--- INSERIR NOVO CURSO ---");
        String titulo = getStringInput("Título do curso: ");
        String descricao = getStringInput("Descrição do curso: ");
        cursoDAO.inserirCursoCompleto(titulo, descricao);
    }

    private void operacaoEditarCurso() throws SQLException {
        System.out.println("\n--- EDITAR CURSO ---");
        UUID id = getUUIDInput("ID do curso a ser editado: ");
        Curso cursoExistente = cursoDAO.buscarPorId(id);

        if (cursoExistente != null) {
            System.out.println("Curso encontrado. Digite os novos dados (deixe em branco para manter o atual):");
            String novoTitulo = getStringInput("Novo título (" + cursoExistente.getTitulo() + "): ");
            String novaDescricao = getStringInput("Nova descrição (" + cursoExistente.getDescricao() + "): ");

            cursoDAO.editarCursoCompleto(id, novoTitulo, novaDescricao);
        } else {
            System.out.println("Curso com ID " + id + " não encontrado.");
        }
    }

    private void operacaoDeletarCurso() throws SQLException {
        System.out.println("\n--- DELETAR CURSO ---");
        UUID id = getUUIDInput("ID do curso a ser deletado: ");
        cursoDAO.deletar(id);
    }

    private void operacaoAdicionarAvaliacaoComentarioCompleta() throws SQLException {
        System.out.println("\n--- ADICIONAR AVALIAÇÃO (NOTA E COMENTÁRIO) AO CURSO ---");
        UUID idCurso = getUUIDInput("ID do curso para adicionar avaliação/comentário: ");
        UUID idUsuario = getUUIDInput("ID do usuário avaliador: ");
        int nota = getIntInput("Nota para o curso (1-5): ");

        if (nota < 1 || nota > 5) {
            System.out.println("Nota inválida. Deve ser entre 1 e 5.");
            return;
        }
        String comentarioTexto = getStringInput("Comentário: ");

        // Null se cometario em branco
        cursoDAO.adicionarAvaliacaoComentario(idCurso, idUsuario, nota, comentarioTexto.isEmpty() ? null : comentarioTexto);
    }

    private void operacaoAdicionarSomenteNotaAoCurso() throws SQLException {
        System.out.println("\n--- ADICIONAR SOMENTE NOTA AO CURSO ---");
        UUID idCurso = getUUIDInput("ID do curso para adicionar nota: ");
        UUID idUsuario = getUUIDInput("ID do usuário avaliador: ");
        int nota = getIntInput("Nota para o curso (1-5): ");

        if (nota < 1 || nota > 5) {
            System.out.println("Nota inválida. Deve ser entre 1 e 5.");
            return;
        }
        
        cursoDAO.adicionarAvaliacaoComentario(idCurso, idUsuario, nota, null);
        System.out.println("Nota adicionada com sucesso! (Sem comentário)");
    }

    private void operacaoAdicionarSomenteComentarioAoCurso() throws SQLException {
        System.out.println("\n--- ADICIONAR SOMENTE COMENTÁRIO AO CURSO ---");
        UUID idCurso = getUUIDInput("ID do curso para adicionar comentário: ");
        UUID idUsuario = getUUIDInput("ID do usuário avaliador: ");
        String comentarioTexto = getStringInput("Comentário: ");

        
        cursoDAO.adicionarAvaliacaoComentario(idCurso, idUsuario, null, comentarioTexto);
        System.out.println("Comentário adicionado com sucesso! (Sem nota)");
    }

    private void operacaoMostrarAvaliacaoCurso() throws SQLException {
        System.out.println("\n--- MOSTRAR AVALIAÇÃO E COMENTÁRIOS DO CURSO ---");
        UUID idCurso = getUUIDInput("ID do curso para mostrar avaliação: ");
        cursoDAO.mostrarAvaliacaoDoCurso(idCurso);
    }

    // CONTEÚDO
    private void operacaoInserirConteudo() throws SQLException {
        System.out.println("\n--- INSERIR NOVO CONTEÚDO ---");
        UUID idCurso = getUUIDInput("ID do curso: ");
        String titulo = getStringInput("Título do conteúdo (ex: 'Notas da Aula 1', ou Nome do Arquivo: 'documento.pdf'): ");
        String descricao = getStringInput("Descrição do conteúdo: ");
        String tipo = getStringInput("Tipo do conteúdo (ex: pdf, txt, jpg): ");
        
        System.out.print("Digite o caminho completo para o arquivo a ser enviado (ex: /home/usuario/documentos/arquivo.pdf): ");
        String filePath = scanner.nextLine();
        byte[] arquivoBytes = readFileToBytes(filePath);

        if (arquivoBytes != null) {
            conteudoDAO.inserirConteudoCompleto(idCurso, titulo, descricao, tipo, arquivoBytes);
        } else {
            System.out.println("Arquivo não pôde ser lido ou não foi selecionado. Conteúdo não inserido.");
        }
    }

    private void operacaoEditarConteudo() throws SQLException {
        System.out.println("\n--- EDITAR CONTEÚDO ---");
        int idConteudo = getIntInput("ID do conteúdo a ser editado: ");
        Conteudo conteudoExistente = conteudoDAO.buscarPorId(idConteudo);

        if (conteudoExistente != null) {
            System.out.println("Conteúdo encontrado. Digite os novos dados (deixe em branco para manter o atual):");
            
            UUID novoIdCurso = getUUIDInput("Novo ID do curso (" + (conteudoExistente.getCurso() != null ? conteudoExistente.getCurso().getId() : "N/A") + ", digite 00000000-0000-0000-0000-000000000000 para manter): ");
            
            String novoTitulo = getStringInput("Novo título (Atual: " + conteudoExistente.getTitulo() + "): ");
            String novaDescricao = getStringInput("Nova descrição (Atual: " + conteudoExistente.getDescricao() + "): ");
            String novoTipo = getStringInput("Novo tipo (Atual: " + conteudoExistente.getTipo() + "): ");
            
            byte[] novoArquivo = null;
            System.out.print("Digite o caminho completo para o NOVO arquivo a ser enviado (deixe em branco para manter o arquivo atual): ");
            String novoArquivoPath = scanner.nextLine();
            if (!novoArquivoPath.isEmpty()) {
                novoArquivo = readFileToBytes(novoArquivoPath);
            } else {
                novoArquivo = conteudoExistente.getArquivo();
            }

            conteudoDAO.editarConteudoCompleto(idConteudo, novoIdCurso, novoTitulo, novaDescricao, novoTipo, novoArquivo);
        } else {
            System.out.println("Conteúdo com ID " + idConteudo + " não encontrado.");
        }
    }

    private void operacaoDeletarConteudo() throws SQLException {
        System.out.println("\n--- DELETAR CONTEÚDO ---");
        int id = getIntInput("ID do conteúdo a ser deletado: ");
        conteudoDAO.deletar(id);
    }

    // Mostrar conteúdos de um curso
    private void operacaoMostrarConteudosDeUmCurso() throws SQLException {
        System.out.println("\n--- MOSTRAR CONTEÚDOS DE UM CURSO ESPECÍFICO ---");
        UUID idCurso = getUUIDInput("Digite o ID do Curso para listar seus conteúdos: ");

        Curso curso = cursoDAO.buscarPorId(idCurso);
        if (curso == null) {
            System.out.println("Curso com ID " + idCurso + " não encontrado.");
            return;
        }
        conteudoDAO.listarConteudosPorCurso(idCurso);
    }

    // DOWNLOAD DE UM ARQUIVO
    private void operacaoDownloadArquivoConteudo() throws SQLException {
        System.out.println("\n--- DOWNLOAD DE ARQUIVO DE CONTEÚDO ---");
        int idConteudo = getIntInput("Digite o ID do Conteúdo cujo arquivo você deseja baixar: ");

        Conteudo conteudo = conteudoDAO.buscarPorId(idConteudo);

        if (conteudo != null) {
            byte[] arquivoBytes = conteudo.getArquivo();
            if (arquivoBytes != null && arquivoBytes.length > 0) {
                String nomeArquivoSugerido = conteudo.getTitulo(); 
                if (conteudo.getTipo() != null && !conteudo.getTipo().isEmpty() && !nomeArquivoSugerido.toLowerCase().endsWith("." + conteudo.getTipo().toLowerCase())) {
                    nomeArquivoSugerido += "." + conteudo.getTipo(); 
                }
                
                System.out.println("Conteúdo encontrado. Nome de arquivo sugerido: " + nomeArquivoSugerido);
                String caminhoSalvar = getStringInput("Digite o caminho completo para salvar o arquivo (ex: /home/usuario/downloads/" + nomeArquivoSugerido + "): ");

                File arquivoDestino = new File(caminhoSalvar);

                try (FileOutputStream fos = new FileOutputStream(arquivoDestino)) {
                    fos.write(arquivoBytes);
                    System.out.println("Arquivo salvo com sucesso em: " + arquivoDestino.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Erro ao salvar o arquivo: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("O conteúdo com ID " + idConteudo + " não possui um arquivo associado ou o arquivo está vazio.");
            }
        } else {
            System.out.println("Conteúdo com ID " + idConteudo + " não encontrado.");
        }
    }

    // VISUALIZA ARQUIVO DE CONTEÚDO
    private void operacaoAbrirArquivoConteudo() throws SQLException {
        System.out.println("\n--- ABRIR ARQUIVO DE CONTEÚDO ---");
        int idConteudo = getIntInput("Digite o ID do Conteúdo cujo arquivo você deseja abrir: ");

        Conteudo conteudo = conteudoDAO.buscarPorId(idConteudo);

        if (conteudo != null) {
            byte[] arquivoBytes = conteudo.getArquivo();
            if (arquivoBytes != null && arquivoBytes.length > 0) {
                String nomeArquivoTemporario = "temp_" + UUID.randomUUID().toString();
                if (conteudo.getTipo() != null && !conteudo.getTipo().isEmpty()) {
                    nomeArquivoTemporario += "." + conteudo.getTipo();
                }
                
                File arquivoTemporario = null;
                try {
                    arquivoTemporario = File.createTempFile(nomeArquivoTemporario, null);
                    arquivoTemporario.deleteOnExit(); 

                    try (FileOutputStream fos = new FileOutputStream(arquivoTemporario)) {
                        fos.write(arquivoBytes);
                    }
                    
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(arquivoTemporario);
                        System.out.println("Arquivo aberto com sucesso. Caminho temporário: " + arquivoTemporario.getAbsolutePath());
                        System.out.println("Pode ser necessário fechar o visualizador externo para continuar no menu.");
                    } else {
                        System.out.println("Funcionalidade de abrir arquivos não suportada neste ambiente (requer ambiente gráfico).");
                        System.out.println("Arquivo temporário salvo em: " + arquivoTemporario.getAbsolutePath());
                    }

                } catch (IOException e) {
                    System.err.println("Erro ao criar/escrever/abrir o arquivo temporário: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Opão para deletar após a tentativa de abertura:
                    // if (arquivoTemporario != null) {
                    //    arquivoTemporario.delete();
                    // }
                }
            } else {
                System.out.println("O conteúdo com ID " + idConteudo + " não possui um arquivo associado ou o arquivo está vazio.");
            }
        } else {
            System.out.println("Conteúdo com ID " + idConteudo + " não encontrado.");
        }
    }


    // MATRÍCULA
    private void operacaoInserirMatricula() throws SQLException {
        System.out.println("\n--- INSERIR NOVA MATRÍCULA ---");
        UUID idUsuario = getUUIDInput("ID do usuário para a matrícula: ");
        UUID idCurso = getUUIDInput("ID do curso para a matrícula: ");

        matriculaDAO.inserirMatriculaCompleta(idUsuario, idCurso);
    }

    private void operacaoEditarMatricula() throws SQLException {
        System.out.println("\n--- EDITAR MATRÍCULA ---");
        int idMatricula = getIntInput("ID da matrícula a ser editada: ");
        Matricula matriculaExistente = matriculaDAO.buscarPorId(idMatricula);

        if (matriculaExistente != null) {
            System.out.println("Matrícula encontrada. Digite os novos dados (deixe em branco para manter o atual):");
            UUID novoIdUsuario = getUUIDInput("Novo ID do usuário (" + (matriculaExistente.getUsuario() != null ? matriculaExistente.getUsuario().getId() : "N/A") + ", digite 00000000-0000-0000-0000-000000000000 para manter): ");
            UUID novoIdCurso = getUUIDInput("Novo ID do curso (" + (matriculaExistente.getCurso() != null ? matriculaExistente.getCurso().getId() : "N/A") + ", digite 00000000-0000-0000-0000-000000000000 para manter): ");

            matriculaDAO.editarMatriculaCompleta(idMatricula, novoIdUsuario, novoIdCurso);
        } else {
            System.out.println("Matrícula com ID " + idMatricula + " não encontrada.");
        }
    }

    private void operacaoDeletarMatricula() throws SQLException {
        System.out.println("\n--- DELETAR MATRÍCULA ---");
        int id = getIntInput("ID da matrícula a ser deletada: ");
        matriculaDAO.deletar(id);
    }
}