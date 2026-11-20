import 'package:material_ui/material_ui.dart';

import '../models/categoria.dart';
import '../models/produto.dart';
import '../services/api_service.dart';
import '../util/tema.dart';
import '../widgets/card_produto.dart';
import '../widgets/estados.dart';

/// Tela 4 da documentação: CATÁLOGO DE PRODUTOS.
///
/// Pesquisa, filtro por categoria e ordenação são feitos pelo SERVIDOR:
/// GET /api/produtos?busca=...&categoria=...&ordem=...
class CatalogoTela extends StatefulWidget {
  final Categoria? categoriaInicial;

  const CatalogoTela({super.key, this.categoriaInicial});

  @override
  State<CatalogoTela> createState() => _CatalogoTelaState();
}

class _CatalogoTelaState extends State<CatalogoTela> {
  static const _ordens = {
    'nome': 'Nome (A–Z)',
    'novidades': 'Novidades',
    'menor-preco': 'Menor preço',
    'maior-preco': 'Maior preço',
  };

  final _busca = TextEditingController();
  late int? _idCategoria = widget.categoriaInicial?.id;
  String _ordem = 'nome';

  late Future<List<Categoria>> _categorias = ApiService.categorias();
  late Future<List<Produto>> _produtos = _buscar();

  Future<List<Produto>> _buscar() =>
      ApiService.produtos(busca: _busca.text, idCategoria: _idCategoria, ordem: _ordem);

  void _pesquisar() => setState(() => _produtos = _buscar());

  Future<void> _atualizar() async {
    setState(() {
      _categorias = ApiService.categorias();
      _produtos = _buscar();
    });
    await _produtos.catchError((_) => <Produto>[]);
  }

  @override
  void dispose() {
    _busca.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          color: Cores.branco,
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 10),
          child: Column(
            children: [
              TextField(
                controller: _busca,
                textInputAction: TextInputAction.search,
                onSubmitted: (_) => _pesquisar(),
                decoration: InputDecoration(
                  hintText: 'Pesquisar produtos',
                  prefixIcon: const Icon(Icons.search),
                  suffixIcon: IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: () {
                      _busca.clear();
                      _pesquisar();
                    },
                  ),
                  isDense: true,
                ),
              ),
              const SizedBox(height: 10),
              Row(
                children: [
                  Expanded(child: _filtroCategoria()),
                  const SizedBox(width: 10),
                  Expanded(
                    child: DropdownButtonFormField<String>(
                      initialValue: _ordem,
                      isExpanded: true,
                      decoration: const InputDecoration(labelText: 'Ordenar', isDense: true),
                      items: _ordens.entries
                          .map((o) => DropdownMenuItem(value: o.key, child: Text(o.value)))
                          .toList(),
                      onChanged: (ordem) {
                        _ordem = ordem ?? 'nome';
                        _pesquisar();
                      },
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        Expanded(
          child: FutureBuilder(
            future: _produtos,
            builder: (context, snap) {
              if (snap.connectionState != ConnectionState.done) return const Carregando();
              if (snap.hasError) return Erro(snap.error!, aoTentarDeNovo: _atualizar);
              final produtos = snap.data!;
              return RefreshIndicator(
                onRefresh: _atualizar,
                child: produtos.isEmpty
                    ? ListView(children: const [
                        SizedBox(height: 60),
                        Vazio(
                          icone: Icons.search_off,
                          titulo: 'Nenhum produto encontrado',
                          texto: 'Tente outra pesquisa ou categoria.',
                        ),
                      ])
                    : CustomScrollView(
                        slivers: [
                          SliverPadding(
                            padding: const EdgeInsets.fromLTRB(16, 16, 16, 4),
                            sliver: SliverToBoxAdapter(
                              child: Text(
                                '${produtos.length} produto(s)',
                                style: const TextStyle(color: Cores.cinza500, fontSize: 13),
                              ),
                            ),
                          ),
                          SliverPadding(
                            padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
                            sliver: gradeProdutos(produtos),
                          ),
                        ],
                      ),
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _filtroCategoria() {
    return FutureBuilder(
      future: _categorias,
      builder: (context, snap) {
        final categorias = snap.data ?? const <Categoria>[];
        // Evita erro se a categoria escolhida não estiver (ainda) na lista.
        final valor = categorias.any((c) => c.id == _idCategoria) ? _idCategoria : null;
        return DropdownButtonFormField<int?>(
          key: ValueKey('categorias-${categorias.length}'),
          initialValue: valor,
          isExpanded: true,
          decoration: const InputDecoration(labelText: 'Categoria', isDense: true),
          items: [
            const DropdownMenuItem<int?>(value: null, child: Text('Todas')),
            ...categorias.map((c) => DropdownMenuItem<int?>(value: c.id, child: Text(c.nome))),
          ],
          onChanged: (id) {
            _idCategoria = id;
            _pesquisar();
          },
        );
      },
    );
  }
}
