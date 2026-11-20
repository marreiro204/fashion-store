import 'package:material_ui/material_ui.dart';

import '../controllers/carrinho_controller.dart';
import '../models/item_carrinho.dart';
import '../services/api_service.dart';
import '../util/formato.dart';
import '../util/tema.dart';
import '../widgets/estados.dart';
import '../widgets/imagem_produto.dart';
import 'finalizar_tela.dart';
import 'produto_tela.dart';

/// Tela 6 da documentação: CARRINHO.
///
/// O carrinho fica no celular, mas os PREÇOS e o TOTAL vêm do servidor:
/// a cada abertura ou mudança, o app chama POST /api/carrinho/validar, que
/// confere estoque e preço atual no banco (se o admin mudou o preço no site,
/// o carrinho já mostra o novo valor).
class CarrinhoTela extends StatefulWidget {
  const CarrinhoTela({super.key});

  @override
  State<CarrinhoTela> createState() => _CarrinhoTelaState();
}

class _CarrinhoTelaState extends State<CarrinhoTela> {
  CarrinhoConferido? _conferido;
  bool _conferindo = false;
  Object? _erro;

  @override
  void initState() {
    super.initState();
    _conferir();
  }

  Future<void> _conferir() async {
    if (carrinho.vazio) {
      setState(() => _conferido = null);
      return;
    }
    setState(() {
      _conferindo = true;
      _erro = null;
    });
    try {
      final resultado = await ApiService.validarCarrinho(carrinho.itens);
      carrinho.substituirPeloServidor(resultado.itens);
      _conferido = resultado;
    } catch (e) {
      _erro = e;
    }
    if (mounted) setState(() => _conferindo = false);
  }

  void _mudarQuantidade(ItemCarrinho item, int quantidade) {
    carrinho.alterarQuantidade(item, quantidade);
    _conferir();
  }

  void _remover(ItemCarrinho item) {
    carrinho.remover(item);
    _conferir();
  }

  num? _subtotal(ItemCarrinho item) {
    for (final i in _conferido?.itens ?? const []) {
      if (i.idProduto == item.idProduto && i.tamanho == item.tamanho) return i.subtotal;
    }
    return null;
  }

  Future<void> _finalizar() async {
    await Navigator.of(context).push(MaterialPageRoute(builder: (_) => const FinalizarTela()));
    // Voltou sem finalizar (ex.: estoque mudou): confere de novo.
    if (mounted) _conferir();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Carrinho')),
      body: ListenableBuilder(
        listenable: carrinho,
        builder: (context, _) {
          if (carrinho.vazio) {
            return const Vazio(
              icone: Icons.shopping_bag_outlined,
              titulo: 'Seu carrinho está vazio',
              texto: 'Escolha um produto e um tamanho para começar.',
            );
          }
          if (_erro != null && _conferido == null) {
            return Erro(_erro!, aoTentarDeNovo: _conferir);
          }
          final avisos = _conferido?.avisos ?? const <String>[];
          return Column(
            children: [
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.all(16),
                  children: [
                    for (final aviso in avisos) _Aviso(aviso),
                    for (final item in carrinho.itens) _itemCarrinho(item),
                  ],
                ),
              ),
              _resumo(),
            ],
          );
        },
      ),
    );
  }

  Widget _itemCarrinho(ItemCarrinho item) {
    final subtotal = _subtotal(item);
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(color: Cores.branco, borderRadius: BorderRadius.circular(6)),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          GestureDetector(
            onTap: () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => ProdutoTela(idProduto: item.idProduto)),
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: SizedBox(width: 78, height: 104, child: ImagemProduto(item.imagem)),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(item.nome, style: const TextStyle(fontWeight: FontWeight.w600)),
                    ),
                    IconButton(
                      visualDensity: VisualDensity.compact,
                      tooltip: 'Remover',
                      icon: const Icon(Icons.delete_outline, size: 20, color: Cores.cinza500),
                      onPressed: () => _remover(item),
                    ),
                  ],
                ),
                Text('Tamanho ${item.tamanho} · ${Formato.dinheiro(item.preco)} cada',
                    style: const TextStyle(color: Cores.cinza500, fontSize: 13)),
                const SizedBox(height: 8),
                Row(
                  children: [
                    SeletorQuantidade(
                      valor: item.quantidade,
                      maximo: CarrinhoController.maximoPorItem,
                      aoMudar: (q) => _mudarQuantidade(item, q),
                    ),
                    const Spacer(),
                    Text(
                      subtotal == null ? '—' : Formato.dinheiro(subtotal),
                      style: const TextStyle(fontWeight: FontWeight.w700),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _resumo() {
    final total = _conferido?.total;
    return Container(
      color: Cores.branco,
      padding: const EdgeInsets.fromLTRB(20, 14, 20, 12),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Row(
              children: [
                const Text('Total', style: TextStyle(fontSize: 16)),
                const Spacer(),
                if (_conferindo)
                  const SizedBox.square(dimension: 18, child: CircularProgressIndicator(strokeWidth: 2))
                else
                  Text(
                    total == null ? '—' : Formato.dinheiro(total),
                    style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w700),
                  ),
              ],
            ),
            const SizedBox(height: 4),
            const Align(
              alignment: Alignment.centerLeft,
              child: Text('Preços conferidos com o estoque da loja.',
                  style: TextStyle(color: Cores.cinza500, fontSize: 12)),
            ),
            const SizedBox(height: 12),
            FilledButton(
              onPressed: _conferindo || _conferido == null || carrinho.vazio ? null : _finalizar,
              child: const Text('FINALIZAR PEDIDO'),
            ),
          ],
        ),
      ),
    );
  }
}

class _Aviso extends StatelessWidget {
  final String texto;

  const _Aviso(this.texto);

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: Cores.avisoFundo, borderRadius: BorderRadius.circular(6)),
      child: Row(
        children: [
          const Icon(Icons.info_outline, color: Cores.aviso, size: 20),
          const SizedBox(width: 10),
          Expanded(child: Text(texto, style: const TextStyle(color: Cores.aviso))),
        ],
      ),
    );
  }
}
