import 'package:material_ui/material_ui.dart';

import '../models/produto.dart';
import '../screens/produto_tela.dart';
import '../util/formato.dart';
import '../util/tema.dart';
import 'imagem_produto.dart';

/// Card de produto usado na tela inicial e no catálogo.
/// Ao tocar, abre os detalhes do produto.
class CardProduto extends StatelessWidget {
  final Produto produto;

  const CardProduto(this.produto, {super.key});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () => Navigator.of(context).push(
        MaterialPageRoute(builder: (_) => ProdutoTela(idProduto: produto.id)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: ClipRRect(
              borderRadius: BorderRadius.circular(6),
              child: Stack(
                fit: StackFit.expand,
                children: [
                  ImagemProduto(produto.imagemPrincipal),
                  if (produto.esgotado)
                    const Positioned(left: 8, top: 8, child: _Etiqueta('ESGOTADO')),
                ],
              ),
            ),
          ),
          const SizedBox(height: 8),
          if (produto.categoria != null)
            Text(
              produto.categoria!.nome.toUpperCase(),
              style: const TextStyle(fontSize: 10.5, letterSpacing: 1, color: Cores.cinza500),
            ),
          Text(
            produto.nome,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500),
          ),
          const SizedBox(height: 2),
          Text(Formato.dinheiro(produto.preco), style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w700)),
        ],
      ),
    );
  }
}

class _Etiqueta extends StatelessWidget {
  final String texto;

  const _Etiqueta(this.texto);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(color: Cores.preto, borderRadius: BorderRadius.circular(4)),
      child: Text(texto, style: const TextStyle(color: Cores.branco, fontSize: 10, letterSpacing: 1)),
    );
  }
}

/// Grade de produtos em 2 colunas (proporção das fotos: 3:4).
SliverGrid gradeProdutos(List<Produto> produtos) {
  return SliverGrid(
    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
      crossAxisCount: 2,
      mainAxisSpacing: 20,
      crossAxisSpacing: 14,
      childAspectRatio: .56,
    ),
    delegate: SliverChildBuilderDelegate(
      (context, i) => CardProduto(produtos[i]),
      childCount: produtos.length,
    ),
  );
}
