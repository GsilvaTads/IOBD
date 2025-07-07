DROP DATABASE IF EXISTS edulivre;

CREATE DATABASE edulivre;

\c edulivre;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(100) NOT NULL,
    perfil VARCHAR(50) NOT NULL
);

CREATE TABLE curso (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    data_criacao DATE NOT NULL DEFAULT CURRENT_DATE,
    avaliacao JSONB
);

CREATE TABLE matricula (
    id SERIAL PRIMARY KEY,
    usuario_id UUID REFERENCES usuario(id) ON DELETE CASCADE NOT NULL,
    curso_id UUID REFERENCES curso(id) ON DELETE CASCADE NOT NULL,
    data_matricula TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (usuario_id, curso_id)
);

CREATE TABLE conteudo (
    id SERIAL PRIMARY KEY,
    curso_id UUID REFERENCES curso(id) ON DELETE CASCADE NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    tipo VARCHAR(50),
    arquivo BYTEA
);

-- Inserindo usuarios
INSERT INTO usuario (nome, email, senha, perfil) VALUES
('Gustavo Silva', 'gustavo.silva@ifrs.br', 'gugu#dada', 'ALUNO'),
('Igor Avila', 'igor.avila@ifrs.br', 'o#melhor', 'PROFESSOR'),
('Frank Zappa', 'frank.zappa@ifrs.br', 'muito#louco', 'ADMIN');

-- Inserindo cursos
INSERT INTO curso (titulo, descricao, avaliacao) VALUES
('Programacao Java', 'Aprenda os fundamentos da programação Java.',
 '{"media": 0.0, "comentarios": []}'::jsonb),

('Implementacao e Operacao de Banco de Dados', 'Domine SQL com tópicos avançados.',
 '{"media": 0.0, "comentarios": []}'::jsonb),

('JDBC', 'Conecte e interaja sua aplicação java com bancos de dados.',
 '{"media": 0.0, "comentarios": [{"usuario_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef", "nota": 1, "comentario": "Nao entendi nada.", "data": "2023-01-15T10:30:00"}, {"usuario_id": "b1c2d3e4-f5a6-7890-1234-567890abcdef", "nota": 4, "comentario": "Top curso.", "data": "2023-01-20T14:00:00"}]}'::jsonb);

-- Inserindo matricula
DO $$
DECLARE
    gugu_id UUID;
    igor_id UUID;
    curso_java_id UUID;
    curso_iobd_id UUID;
    curso_jdbc_id UUID;
BEGIN
    SELECT id INTO gugu_id FROM usuario WHERE email = 'gustavo.silva@ifrs.br';
    SELECT id INTO igor_id FROM usuario WHERE email = 'igor.avila@ifrs.br';
    SELECT id INTO curso_java_id FROM curso WHERE titulo = 'Programacao Java';
    SELECT id INTO curso_iobd_id FROM curso WHERE titulo = 'Implementacao e Operacao de Banco de Dados';
    SELECT id INTO curso_jdbc_id FROM curso WHERE titulo = 'JDBC';

    INSERT INTO matricula (usuario_id, curso_id) VALUES
    (gugu_id, curso_java_id),
    (igor_id, curso_iobd_id),
    (gugu_id, curso_jdbc_id);
END $$;


