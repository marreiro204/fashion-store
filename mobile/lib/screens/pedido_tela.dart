import 'package:material_ui/material_ui.dart';

import '../models/pedido.dart';
import '../services/api_service.dart';
import '../util/formato.dart';
import '../util/tema.dart';
import '../widgets/estados.dart';
import '../widgets/imagem_produto.dart';
import 'pedidos_tela.dart';

/// Tela 9 da documentação: DETALHES DO PEDIDO.
///
/// GET /api/pedidos/{id}. O servidor só devolve se o pedido for do cliente
/// logado (pedido de outra pessoa = 404, sem revelar que existe).
class PedidoTela extends StatefulWidget {
  final int idPedido;

  /// true logo após a compra: mostra a mensagem de sucesso.
  final bool recemCriado;

  const PedidoTela({super.key, required this.idPedido, this.recemCriado = false});

  @override
  State<PedidoTela> createState() => _PedidoTelaState();
}

class _PedidoTelaState extends State<PedidoTela> {
  late Future<Pedido> _pedido = ApiService.pedido(widget.idPedido);

  Future<void> _atualizar() async {
    final novo = ApiService.pedido(widget.idPedido);
    setState(() => _pedido = novo);
    try {
      await novo;
    } catch (_) {
      // O erro já aparece na tela pelo FutureBuilder.
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Pedido #${widget.idPedido}')),
      body: FutureBuilder(
        future: _pedido,
        builder: (context, snap) {
          if (snap.connectionState != ConnectionState.done) return const Carregando();
          if (snap.hasError) return Erro(snap.error!, aoTentarDeNovo: _atualizar);
          final p = snap.data!;
          return RefreshIndicator(
            onRefresh: _atualizar,
            child: ListView(
              padding: const EdgeInsets.all(20),
              children: [
                if (widget.recemCriado) const _Sucesso(),
                Row(
                  children: [
                    StatusPedido(p.status, p.statusDescricao),
                    const Spacer(),
                    Text(Formato.dataHora(p.data), style: const TextStyle(color: Cores.cinza500, fontSize: 13)),
                  ],
                ),
                const SizedBox(height: 20),
                const _Titulo('Itens'),
                for (final i in p.itens)
                  Container(
                    margin: const EdgeInsets.only(bottom: 10),
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(color: Cores.branco, borderRadius: BorderRadius.circular(6)),
                    child: Row(
                      children: [
                        ClipRRect(
                          borderRadius: BorderRadius.circular(4),
                          child: SizedBox(width: 56, height: 74, child: ImagemProduto(i.imagem)),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(i.nomeProduto, style: const TextStyle(fontWeight: FontWeight.w600)),
                              const SizedBox(height: 4),
                              Text(
                                'Tamanho ${i.tamanho} · ${i.quantidade}× ${Formato.dinheiro(i.precoUnitario)}',
                                style: const TextStyle(color: Cores.cinza500, fontSize: 13),
                              ),
                            ],
                          ),
                        ),
                        Text(Formato.dinheiro(i.subtotal), style: const TextStyle(fontWeight: FontWeight.w600)),
                      ],
                    ),
                  ),
                Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(color: Cores.branco, borderRadius: BorderRadius.circular(6)),
                  child: Row(
                    children: [
                      const Text('Total', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
                      const Spacer(),
                      Text(Formato.dinheiro(p.valorTotal),
                          style: const TextStyle(fontSize: 19, fontWeight: FontWeight.w700)),
                    ],
                  ),
                ),
                if (p.endereco != null) ...[
                  const SizedBox(height: 20),
                  const _Titulo('Entrega'),
                  Container(
                    padding: const EdgeInsets.all(14),
                    decoration: BoxDecoration(color: Cores.branco, borderRadius: BorderRadius.circular(6)),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Icon(Icons.location_on_outlined, color: Cores.cinza500),
                        const SizedBox(width: 10),
                        Expanded(child: Text(p.endereco!.formatado, style: const TextStyle(height: 1.4))),
                      ],
                    ),
                  ),
                ],
              ],
            ),
          );
        },
      ),
    );
  }
}

class _Sucesso extends StatelessWidget {
  const _Sucesso();

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 20),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(color: Cores.sucessoFundo, borderRadius: BorderRadius.circular(6)),
      child: const Row(
        children: [
          Icon(Icons.check_circle_outline, color: Cores.sucesso, size: 30),
          SizedBox(width: 12),
          Expanded(
            child: Text(
              'Pedido realizado com sucesso! Acompanhe o status por aqui.',
              style: TextStyle(color: Cores.sucesso, fontWeight: FontWeight.w500),
            ),
          ),
        ],
      ),
    );
  }
}

class _Titulo extends StatelessWidget {
  final String texto;

  const _Titulo(this.texto);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Text(texto, style: const TextStyle(fontFamily: fonteTitulo, fontSize: 19)),
    );
  }
}
