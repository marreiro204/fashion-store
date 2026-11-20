import 'package:material_ui/material_ui.dart';

import '../controllers/carrinho_controller.dart';
import '../models/categoria.dart';
import '../services/api_service.dart';
import 'carrinho_tela.dart';
import 'catalogo_tela.dart';
import 'inicio_tela.dart';
import 'pedidos_tela.dart';
import 'perfil_tela.dart';

/// Estrutura da loja depois do login: barra de navegação com
/// Início, Catálogo, Pedidos e Perfil (fluxo da documentação, seção 16).
/// O carrinho fica no ícone do topo, em todas as abas.
class PrincipalTela extends StatefulWidget {
  const PrincipalTela({super.key});

  @override
  State<PrincipalTela> createState() => _PrincipalTelaState();
}

class _PrincipalTelaState extends State<PrincipalTela> {
  int _aba = 0;
  Categoria? _categoriaCatalogo;

  @override
  void initState() {
    super.initState();
    // Confere o login salvo: se o token venceu (ou o servidor reiniciou),
    // a API responde 401 e o app volta sozinho para o login.
    ApiService.perfil().ignore();
  }

  /// Chamado pela tela inicial ao tocar numa categoria.
  void _abrirCatalogo(Categoria? categoria) {
    setState(() {
      _categoriaCatalogo = categoria;
      _aba = 1;
    });
  }

  @override
  Widget build(BuildContext context) {
    final paginas = [
      InicioTela(aoEscolherCategoria: _abrirCatalogo),
      // A chave muda junto com a categoria: o catálogo recarrega já filtrado.
      CatalogoTela(key: ValueKey(_categoriaCatalogo?.id), categoriaInicial: _categoriaCatalogo),
      const PedidosTela(),
      const PerfilTela(),
    ];
    const titulos = ['FashionStore', 'Catálogo', 'Meus pedidos', 'Perfil'];

    return Scaffold(
      appBar: AppBar(
        title: _aba == 0
            ? Image.asset('assets/logo-texto.png', height: 26)
            : Text(titulos[_aba]),
        actions: const [BotaoCarrinho()],
      ),
      body: paginas[_aba],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _aba,
        onDestinationSelected: (i) => setState(() {
          if (i == 1 && _aba != 1) _categoriaCatalogo = null;
          _aba = i;
        }),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: 'Início'),
          NavigationDestination(icon: Icon(Icons.grid_view_outlined), selectedIcon: Icon(Icons.grid_view), label: 'Catálogo'),
          NavigationDestination(icon: Icon(Icons.receipt_long_outlined), selectedIcon: Icon(Icons.receipt_long), label: 'Pedidos'),
          NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person), label: 'Perfil'),
        ],
      ),
    );
  }
}

/// Ícone do carrinho com o número de peças (atualiza sozinho).
class BotaoCarrinho extends StatelessWidget {
  const BotaoCarrinho({super.key});

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: carrinho,
      builder: (context, _) => IconButton(
        tooltip: 'Carrinho',
        onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const CarrinhoTela())),
        icon: Badge(
          isLabelVisible: carrinho.quantidadeTotal > 0,
          label: Text('${carrinho.quantidadeTotal}'),
          child: const Icon(Icons.shopping_bag_outlined),
        ),
      ),
    );
  }
}
