import 'package:material_ui/material_ui.dart';

import '../controllers/carrinho_controller.dart';
import '../models/produto.dart';
import '../services/api_service.dart';
import '../util/formato.dart';
import '../util/tema.dart';
import '../widgets/estados.dart';
import '../widgets/imagem_produto.dart';
import 'carrinho_tela.dart';

/// Tela 5 da documentação: DETALHES DO PRODUTO.
///
/// GET /api/produtos/{id}: fotos (principal + galeria), descrição, preço e
/// estoque por tamanho. Sem escolher tamanho, não entra no carrinho.
class ProdutoTela extends StatefulWidget {
  final int idProduto;

  const ProdutoTela({super.key, required this.idProduto});

  @override
  State<ProdutoTela> createState() => _ProdutoTelaState();
}

class _ProdutoTelaState extends State<ProdutoTela> {
  late Future<Produto> _produto = ApiService.produto(widget.idProduto);
  Tamanho? _tamanho;
  int _quantidade = 1;
  int _foto = 0;

  void _recarregar() => setState(() => _produto = ApiService.produto(widget.idProduto));

  void _adicionar(Produto produto) {
    final erro = carrinho.adicionar(produto, _tamanho!, _quantidade);
    if (erro != null) {
      mostrarMensagem(context, erro, erro: true);
      return;
    }
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(SnackBar(
        content: Text('${produto.nome} (${_tamanho!.nome}) adicionado ao carrinho.'),
        backgroundColor: Cores.grafite,
        action: SnackBarAction(
          label: 'VER',
          textColor: Cores.branco,
          onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const CarrinhoTela())),
        ),
      ));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Cores.branco,
      appBar: AppBar(title: const Text('Produto')),
      body: FutureBuilder(
        future: _produto,
        builder: (context, snap) {
          if (snap.connectionState != ConnectionState.done) return const Carregando();
          if (snap.hasError) return Erro(snap.error!, aoTentarDeNovo: _recarregar);
          final p = snap.data!;
          final maximo = _tamanho == null
              ? 1
              : (_tamanho!.quantidade < CarrinhoController.maximoPorItem
                  ? _tamanho!.quantidade
                  : CarrinhoController.maximoPorItem);

          return Column(
            children: [
              Expanded(
                child: ListView(
                  children: [
                    _galeria(p),
                    Padding(
                      padding: const EdgeInsets.fromLTRB(20, 18, 20, 24),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          if (p.categoria != null)
                            Text(
                              p.categoria!.nome.toUpperCase(),
                              style: const TextStyle(fontSize: 12, letterSpacing: 1.2, color: Cores.cinza500),
                            ),
                          const SizedBox(height: 4),
                          Text(p.nome, style: const TextStyle(fontFamily: fonteTitulo, fontSize: 26)),
                          const SizedBox(height: 8),
                          Text(
                            Formato.dinheiro(p.preco),
                            style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w700),
                          ),
                          const SizedBox(height: 22),
                          const Text('Tamanho', style: TextStyle(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 10),
                          if (p.tamanhos.isEmpty)
                            const Text('Sem tamanhos disponíveis.', style: TextStyle(color: Cores.cinza500))
                          else
                            Wrap(
                              spacing: 8,
                              runSpacing: 8,
                              children: p.tamanhos.map(_botaoTamanho).toList(),
                            ),
                          if (_tamanho != null && _tamanho!.ultimas)
                            Padding(
                              padding: const EdgeInsets.only(top: 10),
                              child: Text(
                                'Últimas ${_tamanho!.quantidade} unidade(s) no tamanho ${_tamanho!.nome}!',
                                style: const TextStyle(color: Cores.aviso, fontWeight: FontWeight.w500),
                              ),
                            ),
                          if (_tamanho != null) ...[
                            const SizedBox(height: 20),
                            const Text('Quantidade', style: TextStyle(fontWeight: FontWeight.w600)),
                            const SizedBox(height: 8),
                            SeletorQuantidade(
                              valor: _quantidade,
                              maximo: maximo,
                              aoMudar: (q) => setState(() => _quantidade = q),
                            ),
                          ],
                          if (p.descricao != null && p.descricao!.isNotEmpty) ...[
                            const SizedBox(height: 24),
                            const Text('Descrição', style: TextStyle(fontWeight: FontWeight.w600)),
                            const SizedBox(height: 8),
                            Text(p.descricao!, style: const TextStyle(color: Cores.cinza700, height: 1.5)),
                          ],
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              SafeArea(
                top: false,
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(20, 8, 20, 12),
                  child: FilledButton.icon(
                    onPressed: p.esgotado || _tamanho == null ? null : () => _adicionar(p),
                    icon: const Icon(Icons.shopping_bag_outlined),
                    label: Text(p.esgotado
                        ? 'ESGOTADO'
                        : _tamanho == null
                            ? 'ESCOLHA UM TAMANHO'
                            : 'ADICIONAR AO CARRINHO'),
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  /// Fotos em carrossel (deslizar para o lado), na proporção 3:4.
  Widget _galeria(Produto p) {
    final fotos = p.imagens.isEmpty ? <String?>[null] : p.imagens;
    return Stack(
      children: [
        AspectRatio(
          aspectRatio: 3 / 4,
          child: PageView.builder(
            itemCount: fotos.length,
            onPageChanged: (i) => setState(() => _foto = i),
            itemBuilder: (context, i) => ImagemProduto(fotos[i]),
          ),
        ),
        if (fotos.length > 1)
          Positioned(
            bottom: 12,
            left: 0,
            right: 0,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(
                fotos.length,
                (i) => AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  margin: const EdgeInsets.symmetric(horizontal: 3),
                  width: i == _foto ? 18 : 7,
                  height: 7,
                  decoration: BoxDecoration(
                    color: i == _foto ? Cores.preto : Cores.branco,
                    borderRadius: BorderRadius.circular(4),
                    border: Border.all(color: Cores.preto.withValues(alpha: .3)),
                  ),
                ),
              ),
            ),
          ),
      ],
    );
  }

  Widget _botaoTamanho(Tamanho t) {
    final selecionado = _tamanho?.nome == t.nome;
    return ChoiceChip(
      label: Text(t.nome),
      selected: selecionado,
      showCheckmark: false,
      labelStyle: TextStyle(
        color: !t.disponivel ? Cores.cinza300 : (selecionado ? Cores.branco : Cores.preto),
        decoration: t.disponivel ? null : TextDecoration.lineThrough,
        fontWeight: FontWeight.w600,
      ),
      selectedColor: Cores.preto,
      backgroundColor: Cores.branco,
      side: BorderSide(color: selecionado ? Cores.preto : Cores.cinza300),
      onSelected: t.disponivel
          ? (_) => setState(() {
                _tamanho = t;
                _quantidade = 1;
              })
          : null,
    );
  }
}

/// Botões  –  2  +  (usado aqui e no carrinho).
class SeletorQuantidade extends StatelessWidget {
  final int valor;
  final int maximo;
  final ValueChanged<int> aoMudar;

  const SeletorQuantidade({super.key, required this.valor, required this.maximo, required this.aoMudar});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        border: Border.all(color: Cores.cinza300),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          IconButton(
            visualDensity: VisualDensity.compact,
            icon: const Icon(Icons.remove, size: 18),
            onPressed: valor > 1 ? () => aoMudar(valor - 1) : null,
          ),
          SizedBox(
            width: 28,
            child: Text('$valor', textAlign: TextAlign.center, style: const TextStyle(fontWeight: FontWeight.w600)),
          ),
          IconButton(
            visualDensity: VisualDensity.compact,
            icon: const Icon(Icons.add, size: 18),
            onPressed: valor < maximo ? () => aoMudar(valor + 1) : null,
          ),
        ],
      ),
    );
  }
}
