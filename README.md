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
    │   ├── model/      entidades: Cliente, Endereco, Categoria, Produto, Estoque, Pedido, ItemPedido (+ Carrinho, na sessão)
    │   ├── dao/        acesso ao banco (JDBC + PreparedStatement + transações)
    │   ├── service/    regras de negócio e validações
    │   ├── servlet/    controllers (Home, Login, Cliente, Produto, Categoria, Estoque, Carrinho, Pedido, Admin)
    │   ├── api/        API JSON para o app Flutter (ApiServlet + endpoints, conversor, carrinho do app)
    │   ├── filter/     ControleAcessoFilter (login e perfil ADMIN), MenuFilter (categorias do menu)
    │   └── util/       ConnectionFactory, SenhaUtil (BCrypt), Validador, SessaoUtil, ImagemUtil
    ├── main/resources/
    │   └── db.properties.example   modelo da configuração do banco
    ├── main/webapp/
    │   ├── assets/
    │   │   ├── css/    base, layout, componentes, paginas, admin
    │   │   ├── js/     site.js (carrossel, galeria, busca, quantidade, confirmações)
    │   │   └── images/ marca/, banners/, categorias/, produtos/ (ver "Imagens")
    │   └── WEB-INF/
    │       ├── web.xml
    │       ├── tags/   componentes: cardProduto, imagemProduto, icone
    │       └── views/  JSPs: index.jsp, cliente/, produto/, carrinho/, pedido/, admin/, erro/
    │                   includes/ (head, cabecalho, rodape, conta-menu, admin-menu)
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
| Teste: produtos e categorias | Não | 48 |
| Teste: estoque | Não | 30 |
| Teste: carrinho e pedidos | Não | 47 |
| Teste: administração | Não | 41 |
| Teste: API | Não | 51 |
| Teste: conexão com o banco | **Sim** | conexão |
| Teste: DAOs (CRUD) | **Sim** | 41 (apaga os dados de teste no final) |

Os testes sem banco usam DAOs "falsos" em memória para validar as regras de negócio.

---

## Funcionalidades e URLs

| URL | Acesso | Descrição |
|---|---|---|
| `/` | Público | Página inicial: banners, categorias e novidades |
| `/produtos?busca=&categoria=&ordem=` | Público | Catálogo com pesquisa, filtro por categoria e ordenação (`nome`, `novidades`, `menor-preco`, `maior-preco`) |
| `/produto?id=` | Público | Detalhes, galeria de imagens, tamanhos e "você também pode gostar" |
| `/cadastro`, `/login` | Público | Cadastro (com endereço) e login |
| `/logout` (POST) | Logado | Sair |
| `/carrinho` | Público | Carrinho (fica na sessão; adicionar, alterar, remover, esvaziar) |
| `/pedido` | Cliente logado | Finalização: resumo atualizado e endereço de entrega |
| `/pedidos`, `/pedidos?id=` | Cliente logado | Histórico de compras e detalhes (só os próprios pedidos) |
| `/perfil` | Cliente logado | Dados do cliente e endereços |
| `/admin` | ADMIN | Painel: pedidos a processar, faturamento, pedidos por status, alertas de estoque |
| `/admin/pedidos?status=`, `/admin/pedidos?id=` | ADMIN | Consulta de pedidos e atualização de status |
| `/admin/clientes?busca=`, `/admin/clientes?id=` | ADMIN | Consulta de clientes (dados, endereços e pedidos) |
| `/admin/produtos` | ADMIN | CRUD de produtos |
| `/admin/categorias` | ADMIN | CRUD de categorias |
| `/admin/estoque` | ADMIN | Estoque por tamanho |

Não há como virar administrador pelo site: o cadastro público sempre cria o perfil `CLIENTE`.
O perfil `ADMIN` é definido direto no banco.

**Status dos pedidos** (o admin não pode pular nem voltar etapas):

```
PENDENTE ──> PAGO ──> ENVIADO ──> ENTREGUE
   │           │
   └───────────┴──> CANCELADO   (as peças voltam automaticamente para o estoque)
```

---

## Imagens

Todas as imagens ficam em `src/main/webapp/assets/images/`. **Basta colocar o arquivo na
pasta certa, com o nome certo**: nenhum HTML, CSS ou tabela precisa mudar. Enquanto não
houver foto, o site mostra uma imagem provisória no mesmo lugar e no mesmo tamanho.

| Pasta | O que vai nela | Nome do arquivo | Tamanho recomendado |
|---|---|---|---|
| `marca/` | Logo (já está pronta) | `logo.png`, `logo-texto.png`, `monograma.png` e versões `-branca` | — |
| `banners/` | Fotos do banner da página inicial (cada foto = 1 slide) | `banner-1.jpg`, `banner-2.jpg`... (ordem alfabética) | 1920 × 700 px, horizontal |
| `categorias/` | Foto do card de cada categoria | o nome da categoria sem acento, minúsculo, com hífens: `masculino.jpg`, `calcas-jeans.jpg` | 1400 × 480 px, horizontal |
| `produtos/masculino/`, `produtos/feminino/`, `produtos/unissex/` | Fotos dos produtos | livre, sem espaços nem acentos: `camisa-social-branca.jpg` | 900 × 1200 px, vertical (3:4) |

Formatos aceitos: **.jpg, .jpeg, .png e .webp**.

### Como associar uma foto a um produto

1. Copie a foto para a pasta, por exemplo `produtos/masculino/camisa-social-branca.jpg`.
2. No painel admin, em **Produtos → Editar**, preencha o campo **Imagem** com
   `masculino/camisa-social-branca.jpg` (o caminho a partir de `produtos/`) e salve.
3. Gere e publique o projeto (`Ctrl+Shift+B`).

### Imagens adicionais de um produto (galeria)

Use o **mesmo nome** da imagem principal com `-2`, `-3`... no final, até `-8`:

```
produtos/masculino/camisa-social-branca.jpg     <- principal (a do cadastro)
produtos/masculino/camisa-social-branca-2.jpg   <- aparece na galeria
produtos/masculino/camisa-social-branca-3.jpg   <- aparece na galeria
```

A página do produto encontra as adicionais sozinha. Não é preciso cadastrar nada.

### Substituir uma imagem provisória

Não há imagens provisórias fixas no código. Os espaços vazios mostram `sem-imagem.svg`
(produtos) ou um fundo neutro com o monograma (banner e categorias). Assim que o arquivo
com o nome certo é colocado na pasta, ele aparece no lugar.

### Para não comprometer o layout

- Todas as molduras têm **proporção fixa** e recortam a foto **sem deformar** (`object-fit: cover`).
  Uma foto em outro formato só é recortada nas bordas.
- Prefira as proporções da tabela e deixe o assunto principal **no centro/topo** da foto.
- Use nomes **sem espaços, acentos ou maiúsculas** (o Linux diferencia `Foto.JPG` de `foto.jpg`).
- Fotos de até ~300 KB deixam o site rápido (exporte em qualidade 80% no editor de imagens).

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

## API para o app Flutter

A mesma aplicação Java atende o app Mobile por **endpoints HTTP com JSON** (`/api/...`).
São Servlets separados (pacote `api`) que reutilizam as mesmas classes **Service** e **DAO**
do site: Web e Mobile seguem exatamente as mesmas regras. **O app nunca acessa o MySQL direto.**

**Endereço base:** `http://<servidor>:8080/FashionStore/api`
(emulador Android: `http://10.0.2.2:8080/FashionStore/api`).
Envie e receba **JSON em UTF-8** (`Content-Type: application/json`).

### Endpoints

| Método | Endereço | Login? | Corpo enviado | Resposta |
|---|---|---|---|---|
| GET | `/api/categorias` | — | — | lista de categorias |
| GET | `/api/produtos?busca=&categoria=&ordem=` | — | — | lista de produtos (`ordem`: `nome`, `novidades`, `menor-preco`, `maior-preco`) |
| GET | `/api/produtos/{id}` | — | — | um produto |
| POST | `/api/clientes` | — | dados do cadastro | **201** `{token, validadeDias, cliente}` |
| POST | `/api/login` | — | `{email, senha}` | `{token, validadeDias, cliente}` |
| POST | `/api/logout` | token | — | `{mensagem}` |
| GET | `/api/perfil` | token | — | cliente com endereços |
| POST | `/api/carrinho/validar` | — | `{itens: [...]}` | itens com preço atual, `total`, `avisos` |
| POST | `/api/pedidos` | token | `{idEndereco, itens: [...]}` | **201** pedido criado (status `PENDENTE`) |
| GET | `/api/pedidos` | token | — | histórico (itens vazios) |
| GET | `/api/pedidos/{id}` | token | — | pedido completo (só do próprio cliente) |

**Login:** envie o token em todas as chamadas marcadas com "token":
`Authorization: Bearer <token>`. O token vale 7 dias ou até o logout. Se o servidor
for reiniciado, os tokens são perdidos e a API responde **401**: o app deve voltar ao login.

### Exemplos

```jsonc
// POST /api/clientes
{ "nome": "Ana Souza", "email": "ana@email.com", "cpf": "529.982.247-25", "telefone": "11988887777",
  "senha": "Segredo123", "confirmacao": "Segredo123",
  "endereco": { "cep": "01000-000", "logradouro": "Rua das Flores", "numero": "100", "complemento": "Apto 2",
                "bairro": "Centro", "cidade": "São Paulo", "estado": "SP" } }

// GET /api/produtos/1
{ "idProduto": 1, "nome": "Vestido Midi Preto", "descricao": "...", "preco": 249.90,
  "categoria": { "idCategoria": 2, "nome": "Feminino", "descricao": null },
  "imagens": [ "http://10.0.2.2:8080/FashionStore/assets/images/produtos/feminino/vestido-preto.jpg",
               "http://10.0.2.2:8080/FashionStore/assets/images/produtos/feminino/vestido-preto-2.jpg" ],
  "tamanhos": [ { "tamanho": "P", "quantidade": 5, "ultimas": false },
                { "tamanho": "M", "quantidade": 2, "ultimas": true } ],
  "esgotado": false }

// POST /api/pedidos   (Authorization: Bearer <token>)
{ "idEndereco": 10, "itens": [ { "idProduto": 1, "tamanho": "M", "quantidade": 2 } ] }

// Resposta 201
{ "idPedido": 12, "dataPedido": "2026-11-18T20:30", "status": "PENDENTE", "statusDescricao": "Pendente",
  "valorTotal": 499.80, "endereco": { "idEndereco": 10, "...": "...", "enderecoFormatado": "Rua das Flores, 100 ..." },
  "itens": [ { "produto": { "idProduto": 1, "nome": "Vestido Midi Preto", "imagem": "http://..." },
               "tamanho": "M", "quantidade": 2, "precoUnitario": 249.90, "subtotal": 499.80 } ] }
```

Observações:
- `imagens` vazia = produto sem foto (o app mostra a imagem provisória).
- O **preço nunca vem do app**: o servidor busca no banco e recalcula o total.
- Dinheiro sai com 2 casas (`249.90`); datas em ISO (`2026-11-18T20:30`).
- O cliente nunca recebe a senha.

### Erros (sempre no mesmo formato)

```json
{ "erro": "Corrija os campos destacados.", "campos": { "email": "Este e-mail já está cadastrado." } }
```

| Código | Quando |
|---|---|
| 400 | JSON inválido, dados inválidos (`campos` traz a mensagem de cada campo), endereço inválido |
| 401 | Sem token, token inválido/expirado, ou e-mail/senha errados no login |
| 404 | Produto ou pedido não encontrado (inclusive pedido de outro cliente) |
| 405 | Método errado (ex.: GET em `/api/carrinho/validar`) |
| 409 | Estoque mudou durante a compra (esgotou, quantidade insuficiente, produto removido) |
| 413 | Corpo maior que 64 KB |
| 500 | Banco fora do ar ou erro no servidor |

CORS liberado (inclusive para Flutter Web). Teste rápido pelo navegador:
`http://localhost:8080/FashionStore/api/produtos`.

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
| 7 | Carrinho e pedidos | ✅ |
| 8 | Área administrativa (painel, clientes e pedidos) | ✅ |
| 9 | Interface (identidade visual) | ✅ |
| 10 | API JSON para o app Flutter e testes finais | ✅ (aguardando o banco para o teste integrado) |

---

## Equipe

- Matheus da Silva Vitoriano
- Thiago Lima Bulhões
- Caio Vieira Lopes
- Nickolas Gabriel dos Santos

Trabalho Integrador — Uninove, 2026.
