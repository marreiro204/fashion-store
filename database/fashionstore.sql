-- =====================================================================
--  FashionStore - script completo do banco de dados (MySQL 8.0.16+)
--  Estrutura: idêntica à do banco criado pelo colega (archive.zip),
--  conferida coluna por coluna com os DAOs da aplicação Java.
--
--  Como executar:
--   - MySQL Workbench: File > Open SQL Script... > Execute (raio).
--   - Terminal:  mysql -u root -p < fashionstore.sql
--
--  Ordem: 1) banco  2) tabelas  3) dados obrigatórios
--         4) dados de exemplo (opcional)  5) usuário da aplicação
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1) BANCO
-- ---------------------------------------------------------------------
-- ATENÇÃO: se o banco já existir (ex.: você importou o ZIP do colega),
-- a linha abaixo dá erro de propósito, para não apagar nada.
-- Para recriar do zero, tire o "-- " da linha DROP (apaga TODOS os dados).
-- DROP DATABASE IF EXISTS fashionstore;

CREATE DATABASE fashionstore
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE fashionstore;

-- ---------------------------------------------------------------------
-- 2) TABELAS (na ordem das dependências: "pai" antes do "filho")
-- ---------------------------------------------------------------------

-- Clientes e administradores (perfil diferencia os dois).
CREATE TABLE cliente (
    id_cliente  INT          NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    senha       VARCHAR(255) NOT NULL,              -- hash BCrypt, nunca a senha pura
    cpf         VARCHAR(14)  NOT NULL,              -- a aplicação grava só os 11 dígitos
    telefone    VARCHAR(20)  NULL,
    perfil      VARCHAR(10)  NOT NULL DEFAULT 'CLIENTE',
    CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
    CONSTRAINT uk_cliente_email UNIQUE (email),
    CONSTRAINT uk_cliente_cpf   UNIQUE (cpf),
    CONSTRAINT ck_cliente_perfil CHECK (perfil IN ('CLIENTE', 'ADMIN'))
) ENGINE = InnoDB;

-- Endereços de entrega (um cliente pode ter vários).
CREATE TABLE endereco (
    id_endereco INT          NOT NULL AUTO_INCREMENT,
    id_cliente  INT          NOT NULL,
    cep         VARCHAR(9)   NOT NULL,
    logradouro  VARCHAR(150) NOT NULL,
    numero      VARCHAR(10)  NOT NULL,
    complemento VARCHAR(100) NULL,
    bairro      VARCHAR(100) NOT NULL,
    cidade      VARCHAR(100) NOT NULL,
    estado      CHAR(2)      NOT NULL,
    CONSTRAINT pk_endereco PRIMARY KEY (id_endereco),
    CONSTRAINT fk_endereco_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

-- Categorias do catálogo (menu do site).
CREATE TABLE categoria (
    id_categoria INT          NOT NULL AUTO_INCREMENT,
    nome         VARCHAR(50)  NOT NULL,
    descricao    VARCHAR(255) NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (id_categoria),
    CONSTRAINT uk_categoria_nome UNIQUE (nome)
) ENGINE = InnoDB;

-- Produtos. "imagem" guarda o caminho relativo dentro de
-- assets/images/produtos/ (ex.: 'feminino/vestido-preto.jpg').
CREATE TABLE produto (
    id_produto   INT           NOT NULL AUTO_INCREMENT,
    id_categoria INT           NOT NULL,
    nome         VARCHAR(100)  NOT NULL,
    descricao    VARCHAR(1000) NULL,
    preco        DECIMAL(10,2) NOT NULL,
    imagem       VARCHAR(255)  NULL,
    CONSTRAINT pk_produto PRIMARY KEY (id_produto),
    CONSTRAINT fk_produto_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_produto_preco CHECK (preco >= 0)
) ENGINE = InnoDB;

-- Estoque por produto E tamanho (uma linha por tamanho).
CREATE TABLE estoque (
    id_estoque INT         NOT NULL AUTO_INCREMENT,
    id_produto INT         NOT NULL,
    tamanho    VARCHAR(10) NOT NULL,
    quantidade INT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_estoque PRIMARY KEY (id_estoque),
    CONSTRAINT uk_estoque_produto_tamanho UNIQUE (id_produto, tamanho),
    CONSTRAINT fk_estoque_produto FOREIGN KEY (id_produto)
        REFERENCES produto (id_produto)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_estoque_quantidade CHECK (quantidade >= 0)
) ENGINE = InnoDB;

-- Pedidos.
CREATE TABLE pedido (
    id_pedido   INT           NOT NULL AUTO_INCREMENT,
    id_cliente  INT           NOT NULL,
    id_endereco INT           NOT NULL,
    data_pedido DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status      VARCHAR(20)   NOT NULL DEFAULT 'PENDENTE',
    CONSTRAINT pk_pedido PRIMARY KEY (id_pedido),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_pedido_endereco FOREIGN KEY (id_endereco)
        REFERENCES endereco (id_endereco)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_pedido_status
        CHECK (status IN ('PENDENTE', 'PAGO', 'ENVIADO', 'ENTREGUE', 'CANCELADO')),
    CONSTRAINT ck_pedido_valor CHECK (valor_total >= 0)
) ENGINE = InnoDB;

-- Itens de cada pedido (preço "congelado" no momento da compra).
CREATE TABLE item_pedido (
    id_item_pedido INT           NOT NULL AUTO_INCREMENT,
    id_pedido      INT           NOT NULL,
    id_produto     INT           NOT NULL,
    tamanho        VARCHAR(10)   NOT NULL,
    quantidade     INT           NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    subtotal       DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_item_pedido PRIMARY KEY (id_item_pedido),
    CONSTRAINT fk_item_pedido_pedido FOREIGN KEY (id_pedido)
        REFERENCES pedido (id_pedido)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_item_pedido_produto FOREIGN KEY (id_produto)
        REFERENCES produto (id_produto)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_item_preco      CHECK (preco_unitario >= 0),
    CONSTRAINT ck_item_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_item_subtotal   CHECK (subtotal >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- 3) DADOS OBRIGATÓRIOS
-- ---------------------------------------------------------------------

-- Administrador inicial. Sem ele ninguém entra em /admin, porque o
-- cadastro do site sempre cria perfil CLIENTE.
-- LOGIN DO ADMIN NO SITE (somente para testes locais):
--     email: admin@fashionstore.com
--     senha: (combinada com a equipe; nunca fica no GitHub)
-- Na coluna senha vai o HASH BCrypt dessa senha, nunca a senha pura.
-- Troque depois do primeiro acesso em um ambiente real.
-- CPF 529.982.247-25: número de exemplo válido (passa na validação do site).
INSERT INTO cliente (nome, email, senha, cpf, telefone, perfil) VALUES
    ('Administrador', 'admin@fashionstore.com',
     '$2a$10$b/egNgRCsXAw4UVmZ3QKL.aR.3UZFa/Gklkd29VGda5/n4Eyq65cq',
     '52998224725', '11900000000', 'ADMIN');

-- Categorias pensadas a partir das fotos que já estão no projeto.
-- Podem ser editadas/excluídas depois pelo painel /admin/categorias.
INSERT INTO categoria (nome, descricao) VALUES
    ('Camisetas', 'Camisetas básicas e estampadas'),
    ('Calças',    'Jeans, alfaiataria e sarja'),
    ('Blusas',    'Blusas e camisas femininas'),
    ('Vestidos',  'Vestidos para o dia a dia e ocasiões especiais'),
    ('Conjuntos', 'Conjuntos coordenados'),
    ('Malhas',    'Suéteres, cardigãs e tricôs');

-- ---------------------------------------------------------------------
-- 4) DADOS DE EXEMPLO (OPCIONAL)
--    Produtos usando as fotos já salvas em assets/images/produtos/.
--    Preços e estoques são fictícios. Se não quiser, apague esta seção
--    e cadastre tudo pelo painel /admin/produtos.
-- ---------------------------------------------------------------------

INSERT INTO produto (id_categoria, nome, descricao, preco, imagem)
SELECT c.id_categoria, p.nome, p.descricao, p.preco, p.imagem
FROM (
    SELECT 'Blusas'    AS categoria, 'Blusa Bege'           AS nome, 'Blusa feminina em tecido leve, cor bege.'              AS descricao, 119.90 AS preco, 'feminino/blusa-bege.jpg'           AS imagem
    UNION ALL SELECT 'Conjuntos', 'Conjunto Alfaiataria', 'Blazer e calça de alfaiataria, caimento reto.',               349.90, 'feminino/conjunto-alfaiataria.jpg'
    UNION ALL SELECT 'Vestidos',  'Vestido Preto',        'Vestido preto midi, básico e versátil.',                      189.90, 'feminino/vestido-preto.jpg'
    UNION ALL SELECT 'Calças',    'Calça Jeans',          'Calça jeans masculina, lavagem média, modelagem reta.',       159.90, 'masculino/calca-jeans.jpg'
    UNION ALL SELECT 'Camisetas', 'Camiseta Preta',       'Camiseta masculina 100% algodão, gola careca.',                69.90, 'masculino/camiseta-preta.jpg'
    UNION ALL SELECT 'Malhas',    'Suéter Bege',          'Suéter masculino de tricô, cor bege.',                        199.90, 'masculino/sueter-bege.jpg'
) AS p
JOIN categoria c ON c.nome = p.categoria;

-- Estoque por tamanho (roupas P a GG; calça na numeração 38 a 44).
INSERT INTO estoque (id_produto, tamanho, quantidade)
SELECT pr.id_produto, t.tamanho, t.quantidade
FROM produto pr
JOIN (
    SELECT 'letras' AS grade, 'P' AS tamanho, 10 AS quantidade
    UNION ALL SELECT 'letras',  'M',  15
    UNION ALL SELECT 'letras',  'G',  12
    UNION ALL SELECT 'letras',  'GG',  4
    UNION ALL SELECT 'numeros', '38',  8
    UNION ALL SELECT 'numeros', '40', 12
    UNION ALL SELECT 'numeros', '42', 10
    UNION ALL SELECT 'numeros', '44',  3
) AS t ON t.grade = IF(pr.nome = 'Calça Jeans', 'numeros', 'letras');

-- ---------------------------------------------------------------------
-- 5) USUÁRIO DA APLICAÇÃO (recomendado)
--    A aplicação Java NÃO deve usar o root. Este usuário só pode
--    ler/gravar dados do banco fashionstore (não cria nem apaga tabelas).
--    Troque <DEFINA_UMA_SENHA> por uma senha sua. Essa senha vai SOMENTE
--    no arquivo src/main/resources/db.properties (que não vai pro GitHub).
-- ---------------------------------------------------------------------

CREATE USER IF NOT EXISTS 'fashionstore_app'@'localhost' IDENTIFIED BY '<DEFINA_UMA_SENHA>';
GRANT SELECT, INSERT, UPDATE, DELETE ON fashionstore.* TO 'fashionstore_app'@'localhost';

-- ---------------------------------------------------------------------
-- Conferência rápida (deve mostrar 7 tabelas, 1 admin, 6 categorias,
-- 6 produtos e 24 linhas de estoque, se a seção 4 foi mantida).
-- ---------------------------------------------------------------------
SHOW TABLES;
SELECT 'clientes'   AS tabela, COUNT(*) AS linhas FROM cliente
UNION ALL SELECT 'categorias', COUNT(*) FROM categoria
UNION ALL SELECT 'produtos',   COUNT(*) FROM produto
UNION ALL SELECT 'estoque',    COUNT(*) FROM estoque;
