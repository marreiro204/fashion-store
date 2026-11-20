# FashionStore — App Mobile (Flutter)

Aplicativo Android do cliente da loja FashionStore. Consome a **API JSON** da aplicação
Java (`/api/...`); nunca acessa o banco de dados direto.

## Telas (documentação do projeto, seção 15)

| # | Tela | Arquivo | Endpoint |
|---|---|---|---|
| 1 | Login | `screens/login_tela.dart` | `POST /api/login` |
| 2 | Cadastro | `screens/cadastro_tela.dart` | `POST /api/clientes` |
| 3 | Tela inicial | `screens/inicio_tela.dart` | `GET /api/categorias`, `GET /api/produtos?ordem=novidades` |
| 4 | Catálogo | `screens/catalogo_tela.dart` | `GET /api/produtos?busca=&categoria=&ordem=` |
| 5 | Detalhes do produto | `screens/produto_tela.dart` | `GET /api/produtos/{id}` |
| 6 | Carrinho | `screens/carrinho_tela.dart` | `POST /api/carrinho/validar` |
| 7 | Finalização | `screens/finalizar_tela.dart` | `POST /api/pedidos` |
| 8 | Meus pedidos | `screens/pedidos_tela.dart` | `GET /api/pedidos` |
| 9 | Detalhes do pedido | `screens/pedido_tela.dart` | `GET /api/pedidos/{id}` |
| 10 | Perfil | `screens/perfil_tela.dart` | `GET /api/perfil`, `POST /api/logout` |

## Organização do código (`lib/`)

| Pasta | Responsabilidade |
|---|---|
| `models/` | Objetos que espelham o JSON da API (`Produto`, `Pedido`, `Cliente`...) |
| `services/api_service.dart` | **Único** lugar que faz HTTP; trata erros e falta de conexão |
| `controllers/` | Estado compartilhado: sessão (token, servidor) e carrinho, salvos no celular |
| `screens/` | As 10 telas (só desenham e chamam o serviço) |
| `widgets/` | Peças reaproveitadas: card de produto, imagem com placeholder, carregando/erro |
| `util/` | Cores da marca (iguais às do site) e formatação de dinheiro, data e CPF |

Bibliotecas: `http` (chamadas à API) e `shared_preferences` (token, carrinho e endereço
do servidor guardados no aparelho). O resto usa só recursos do Flutter.

## Como rodar

1. Servidor ligado: MySQL + Tomcat com o `FashionStore.war` publicado.
2. Celular na **mesma rede Wi-Fi** do computador; porta 8080 liberada no firewall
   (rede marcada como **Privada** no Windows).
3. Gerar o APK e instalar no celular:

```
flutter build apk --release
```

Arquivo: `build/app/outputs/flutter-apk/app-release.apk`.

4. No app, toque na **engrenagem** da tela de login (ou Perfil › Servidor), digite o IP do
   computador (`ipconfig` › "Endereço IPv4" do Wi-Fi) e toque em **Testar e salvar**.

Testes: `flutter test` (modelos e formatações) e `flutter analyze`.
