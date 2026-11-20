import 'package:material_ui/material_ui.dart';

import '../util/tema.dart';

/// Foto do produto baixada do SERVIDOR (Image.network).
///
/// Sem foto, ou se ela não carregar, mostra a imagem provisória
/// com o monograma da marca (igual ao site).
class ImagemProduto extends StatelessWidget {
  final String? url;
  final BoxFit ajuste;

  const ImagemProduto(this.url, {super.key, this.ajuste = BoxFit.cover});

  @override
  Widget build(BuildContext context) {
    if (url == null) return const _Provisoria();
    return Image.network(
      url!,
      fit: ajuste,
      width: double.infinity,
      height: double.infinity,
      loadingBuilder: (context, filho, progresso) => progresso == null
          ? filho
          : const ColoredBox(
              color: Cores.cinza100,
              child: Center(child: SizedBox.square(dimension: 22, child: CircularProgressIndicator(strokeWidth: 2))),
            ),
      errorBuilder: (context, erro, pilha) => const _Provisoria(),
    );
  }
}

class _Provisoria extends StatelessWidget {
  const _Provisoria();

  @override
  Widget build(BuildContext context) {
    return ColoredBox(
      color: Cores.cinza100,
      child: Center(
        child: FractionallySizedBox(
          widthFactor: .35,
          child: Opacity(opacity: .35, child: Image.asset('assets/monograma.png')),
        ),
      ),
    );
  }
}
