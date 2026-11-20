import 'package:material_ui/material_ui.dart';

import '../controllers/sessao_controller.dart';
import '../models/categoria.dart';
import '../models/produto.dart';
import '../services/api_service.dart';
import '../util/tema.dart';
import '../widgets/card_produto.dart';
import '../widgets/estados.dart';

/// Tela 3 da documentação: TELA INICIAL.
///
/// Busca na API as categorias e as novidades. Tudo vem do banco: o que o
/// administrador cadastrar ou alterar no site aparece aqui ao puxar a tela
/// para baixo (atualizar).
class InicioTela extends StatefulWidget {
  final void Function(Categoria? categoria) aoEscolherCategoria;

  const InicioTela({super.key, required this.aoEscolherCategoria});

  @override
  State<InicioTela> createState() => _InicioTelaState();
}

class _InicioTelaState extends State<InicioTela> {
  late Future<(List<Categoria>, List<Produto>)> _dados = _carregar();

  Future<(List<Categoria>, List<Produto>)> _carregar() async {
    // As duas chamadas acontecem ao mesmo tempo.
    final categorias = ApiService.categorias();
    final novidades = ApiService.produtos(ordem: 'novidades');
    return (await categorias, await novidades);
  }

  Future<void> _atualizar() async {
    final novo = _carregar();
    setState(() => _dados = novo);
    await novo.catchError((_) => (<Categoria>[], <Produto>[]));
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _dados,
      builder: (context, snap) {
        if (snap.connectionState != ConnectionState.done) return const Carregando();
        if (snap.hasError) return Erro(snap.error!, aoTentarDeNovo: _atualizar);

        final (categorias, produtos) = snap.data!;
        final novidades = produtos.take(8).toList();

        return RefreshIndicator(
          onRefresh: _atualizar,
          child: CustomScrollView(
            slivers: [
              SliverToBoxAdapter(child: _Boasvindas(nome: sessao.cliente?.primeiroNome)),
              SliverToBoxAdapter(
                child: _Categorias(categorias: categorias, aoEscolher: widget.aoEscolherCategoria),
              ),
              const SliverToBoxAdapter(
                child: Padding(
                  padding: EdgeInsets.fromLTRB(20, 28, 20, 14),
                  child: Text('Novidades', style: TextStyle(fontFamily: fonteTitulo, fontSize: 22)),
                ),
              ),
              if (novidades.isEmpty)
                const SliverToBoxAdapter(
                  child: Vazio(icone: Icons.checkroom_outlined, titulo: 'Nenhum produto ainda'),
                )
              else
                SliverPadding(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  sliver: gradeProdutos(novidades),
                ),
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
                  child: OutlinedButton(
                    onPressed: () => widget.aoEscolherCategoria(null),
                    child: const Text('VER TODO O CATÁLOGO'),
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

class _Boasvindas extends StatelessWidget {
  final String? nome;

  const _Boasvindas({this.nome});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.fromLTRB(20, 16, 20, 0),
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(color: Cores.preto, borderRadius: BorderRadius.circular(6)),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  nome == null ? 'Olá!' : 'Olá, $nome!',
                  style: const TextStyle(color: Cores.branco, fontFamily: fonteTitulo, fontSize: 22),
                ),
                const SizedBox(height: 6),
                const Text(
                  'Elegância em cada detalhe.',
                  style: TextStyle(color: Cores.cinza300),
                ),
              ],
            ),
          ),
          Image.asset('assets/monograma-branco.png', height: 54),
        ],
      ),
    );
  }
}

class _Categorias extends StatelessWidget {
  final List<Categoria> categorias;
  final void Function(Categoria? categoria) aoEscolher;

  const _Categorias({required this.categorias, required this.aoEscolher});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.fromLTRB(20, 28, 20, 12),
          child: Text('Categorias', style: TextStyle(fontFamily: fonteTitulo, fontSize: 22)),
        ),
        SizedBox(
          height: 42,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 20),
            itemCount: categorias.length,
            separatorBuilder: (_, _) => const SizedBox(width: 8),
            itemBuilder: (context, i) => ActionChip(
              label: Text(categorias[i].nome),
              backgroundColor: Cores.branco,
              side: const BorderSide(color: Cores.cinza300),
              onPressed: () => aoEscolher(categorias[i]),
            ),
          ),
        ),
      ],
    );
  }
}
