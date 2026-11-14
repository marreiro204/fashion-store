# FashionStore — Loja Virtual de Roupas

Aplicação **Java Web** do Trabalho Integrador (Uninove): uma loja virtual de roupas com
**área do cliente** (catálogo, carrinho e pedidos) e **área administrativa** (produtos,
categorias, estoque, clientes e pedidos).

A solução completa tem três partes:

| Parte | Tecnologia | Neste repositório |
|---|---|---|
| Aplicação Web | Java 21, JSP, Servlets, HTML e CSS | ✅ |
| Banco de dados | MySQL 8 | Criado por outro integrante (ver [Banco de dados](#banco-de-dados)) |
| Aplicativo Mobile | Flutter | Desenvolvido por outro integrante |

---

## Tecnologias

| Item | Versão |
|---|---|
| Java (JDK) | 21 |
| Apache Tomcat | 10.1 (APIs **Jakarta**: `jakarta.servlet.*`) |
| Maven | 3.9 (empacotamento **WAR**) |
| Banco | MySQL 8.0.19+ (recomendado 8.4 LTS) via **JDBC** |
| Bibliotecas | JSTL 3.0, MySQL Connector/J 8.4, jBCrypt 0.4 |

Arquitetura **MVC** com uma camada de serviço:

```
Navegador → Filter (controle de acesso) → Servlet (controller) → Service (regras) → DAO (SQL/JDBC) → MySQL
                                               └→ JSP (view)
```

---

## Estrutura do projeto

```
FashionStore/
├── pom.xml                         dependências e build (gera target/FashionStore.war)
├── docs/ajustes-modelagem.md       mudanças aprovadas em relação ao PDF do projeto
├── .vscode/tasks.json              tarefas do VS Code (build, deploy, Tomcat, testes)
└── src/
    ├── main/java/br/com/fashionstore/
    │   ├── model/      entidades: Cliente, Endereco, Categoria, Produto, Estoque, Pedido, ItemPedido
    │   ├── dao/        acesso ao banco (JDBC + PreparedStatement + transações)
    │   ├── service/    regras de negócio e validações
    │   ├── servlet/    controllers (Login, Cliente, Produto, Categoria, Estoque, Admin)
    │   ├── filter/     ControleAcessoFilter (login e perfil ADMIN)
    │   └── util/       ConnectionFactory, SenhaUtil (BCrypt), Validador, SessaoUtil
    ├── main/resources/
    │   └── db.properties.example   modelo da configuração do banco
    ├── main/webapp/
    │   ├── index.jsp
    │   ├── assets/     css, images (images/produtos/ = fotos dos produtos), js
    │   └── WEB-INF/
    │       ├── web.xml
    │       ├── tags/   tag reutilizável da imagem do produto
    │       └── views/  JSPs (cliente/, produto/, admin/, includes/, erro/)
    └── test/java/      testes executáveis (não vão para o WAR)
```

As JSPs ficam em `WEB-INF/views` para não poderem ser abertas direto pelo navegador: toda
página passa primeiro por um Servlet.

---

## Como rodar

### 1. Pré-requisitos

- JDK 21, Maven 3.9 e Tomcat 10.1 instalados.
- Variáveis de ambiente `JAVA_HOME`, `MAVEN_HOME`, `CATALINA_HOME` e `CATALINA_BASE`
  configuradas, e o `bin` do Java e do Maven no `PATH`.
- VS Code com a extensão **Extension Pack for Java**.

### 2. Configurar o banco

Copie o modelo e preencha com os dados recebidos de quem criou o banco:

```
src/main/resources/db.properties.example  →  src/main/resources/db.properties
```

```properties
db.host=localhost
db.port=3306
db.name=fashionstore
db.user=fashionstore_app
db.password=SUA_SENHA
```

> ⚠️ O `db.properties` está no `.gitignore` e **nunca deve ir para o GitHub**.
> Sem ele a aplicação abre normalmente, mas as telas que usam o banco mostram
> "Não foi possível acessar o banco de dados agora".

### 3. Executar pelo VS Code

Use **Ctrl+Shift+P → Tasks: Run Task**:

| Tarefa | O que faz |
|---|---|
| **Tomcat: iniciar** | Liga o Tomcat (deixe o terminal aberto) |
| **Build + Deploy** (`Ctrl+Shift+B`) | `mvn clean package` + copia o WAR para o Tomcat |
| **Tomcat: parar** | Desliga o Tomcat |

Depois acesse: **http://localhost:8080/FashionStore/**

### 4. Executar pelo terminal (alternativa)

```bash
mvn clean package
copy target\FashionStore.war %CATALINA_BASE%\webapps\
%CATALINA_HOME%\bin\startup.bat
```

---

## Testes

Cada teste é uma classe com `main` em `src/test/java`, executada pelas tarefas do VS Code.

| Tarefa | Precisa de banco? | Verificações |
|---|---|---|
| Teste: classes Model | Não | 30 |
| Teste: autenticação | Não | 37 |
| Teste: produtos e categorias | Não | 37 |
| Teste: estoque | Não | 30 |
| Teste: conexão com o banco | **Sim** | conexão |
| Teste: DAOs (CRUD) | **Sim** | 41 (apaga os dados de teste no final) |

Os testes sem banco usam DAOs "falsos" em memória para validar as regras de negócio.

---

## Funcionalidades e URLs

| URL | Acesso | Descrição |
|---|---|---|
| `/` | Público | Página inicial |
| `/produtos?busca=&categoria=` | Público | Catálogo com pesquisa e filtro por categoria |
| `/produto?id=` | Público | Detalhes e tamanhos do produto |
| `/cadastro`, `/login` | Público | Cadastro (com endereço) e login |
| `/logout` (POST) | Logado | Sair |
| `/perfil` | Cliente logado | Dados do cliente e endereços |
| `/admin` | ADMIN | Painel administrativo |
| `/admin/produtos` | ADMIN | CRUD de produtos |
| `/admin/categorias` | ADMIN | CRUD de categorias |
| `/admin/estoque` | ADMIN | Estoque por tamanho |

Não há como virar administrador pelo site: o cadastro público sempre cria o perfil `CLIENTE`.
O perfil `ADMIN` é definido direto no banco.

---

## Segurança

- Senhas com **hash BCrypt** (nunca em texto puro, nem na sessão).
- **PreparedStatement** em todo SQL (proteção contra SQL Injection).
- Saída das JSPs com `<c:out>` (proteção contra XSS).
- **Controle de acesso por perfil** com Filter (`/admin/*` só para ADMIN → senão 403).
- Sessão: troca de ID no login (*session fixation*), cookie `HttpOnly` e `SameSite=Lax` (CSRF),
  expiração em 30 minutos, logout só por POST.
- Validação no servidor (CPF, e-mail, senha forte, CEP, preço, imagem, tamanhos).
- Credenciais do banco fora do código e do Git.
- Páginas de erro amigáveis; detalhes técnicos só no log do Tomcat.

---

## Banco de dados

O banco é criado por outro integrante do grupo, seguindo o **Guia do Banco de Dados**
(entregue em PDF, com diagrama, dicionário de dados e scripts SQL).

Principais ajustes em relação ao PDF do projeto (detalhes em [docs/ajustes-modelagem.md](docs/ajustes-modelagem.md)):

- **Estoque por tamanho**: `tamanho` saiu de `produto` e foi para `estoque` e `item_pedido`;
- coluna `perfil` (`CLIENTE`/`ADMIN`) em `cliente`;
- `email` e `cpf` únicos; `data_pedido` como `DATETIME`; `status` com valores fixos
  (`PENDENTE`, `PAGO`, `ENVIADO`, `ENTREGUE`, `CANCELADO`).

---

## Integração com o Flutter (planejada)

A mesma aplicação Java vai atender o app Mobile por **endpoints HTTP com JSON** (`/api/...`),
criados como Servlets separados que reutilizam as camadas **Service** e **DAO** já existentes.
Assim, Web e Mobile seguem exatamente as mesmas regras de negócio.

---

## Andamento

| Etapa | Conteúdo | Situação |
|---|---|---|
| 1 | Configuração (Maven, Tomcat, estrutura) | ✅ |
| 2 | Classes Model | ✅ |
| 3 | Conexão JDBC e DAOs | ✅ (aguardando o banco para teste real) |
| 4 | Cadastro, login, sessão e proteção de páginas | ✅ |
| 5 | Produtos, categorias, catálogo, pesquisa e filtro | ✅ |
| 6 | Estoque por tamanho | ✅ |
| 7 | Carrinho e pedidos | ⏳ |
| 8 | Área administrativa (clientes e pedidos) | ⏳ |
| 9 | Interface (identidade visual) | ⏳ |
| 10 | Testes finais e preparação para o Flutter | ⏳ |

---

## Equipe

- Matheus da Silva Vitoriano
- Thiago Lima Bulhões
- Caio Vieira Lopes
- Nickolas Gabriel dos Santos

Trabalho Integrador — Uninove, 2026.
