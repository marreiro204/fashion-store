import 'package:material_ui/material_ui.dart';

import '../models/pedido.dart';
import '../services/api_service.dart';
import '../util/formato.dart';
import '../util/tema.dart';
import '../widgets/estados.dart';
import 'pedido_tela.dart';

/// Tela 8 da documentação: MEUS PEDIDOS (histórico).
///
/// GET /api/pedidos devolve só os pedidos do cliente logado (o servidor
/// descobre quem é pelo token). Quando o administrador muda o status no
/// site, aqui aparece o novo status ao puxar a tela para baixo.
class PedidosTela extends StatefulWidget {
  const PedidosTela({super.key});

  @override
  State<PedidosTela> createState() => _PedidosTelaState();
}

class _PedidosTelaState extends State<PedidosTela> {
  late Future<List<Pedido>> _pedidos = ApiService.pedidos();

  Future<void> _atualizar() async {
    final novo = ApiService.pedidos();
    setState(() => _pedidos = novo);
    await novo.catchError((_) => <Pedido>[]);
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _pedidos,
      builder: (context, snap) {
        if (snap.connectionState != ConnectionState.done) return const Carregando();
        if (snap.hasError) return Erro(snap.error!, aoTentarDeNovo: _atualizar);
        final pedidos = snap.data!;
        return RefreshIndicator(
          onRefresh: _atualizar,
          child: pedidos.isEmpty
              ? ListView(children: const [
                  SizedBox(height: 80),
                  Vazio(
                    icone: Icons.receipt_long_outlined,
                    titulo: 'Nenhum pedido ainda',
                    texto: 'Seus pedidos aparecem aqui depois da compra.',
                  ),
                ])
              : ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: pedidos.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 10),
                  itemBuilder: (context, i) => _CartaoPedido(
                    pedidos[i],
                    aoVoltar: _atualizar,
                  ),
                ),
        );
      },
    );
  }
}

class _CartaoPedido extends StatelessWidget {
  final Pedido pedido;
  final VoidCallback aoVoltar;

  const _CartaoPedido(this.pedido, {required this.aoVoltar});

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Cores.branco,
      borderRadius: BorderRadius.circular(6),
      child: InkWell(
        borderRadius: BorderRadius.circular(6),
        onTap: () async {
          await Navigator.of(context).push(
            MaterialPageRoute(builder: (_) => PedidoTela(idPedido: pedido.id)),
          );
          aoVoltar();
        },
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Pedido #${pedido.id}', style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
                    const SizedBox(height: 4),
                    Text(Formato.dataHora(pedido.data), style: const TextStyle(color: Cores.cinza500, fontSize: 13)),
                    const SizedBox(height: 10),
                    StatusPedido(pedido.status, pedido.statusDescricao),
                  ],
                ),
              ),
              Text(Formato.dinheiro(pedido.valorTotal), style: const TextStyle(fontWeight: FontWeight.w700)),
              const SizedBox(width: 6),
              const Icon(Icons.chevron_right, color: Cores.cinza500),
            ],
          ),
        ),
      ),
    );
  }
}

/// Etiqueta colorida do status (as mesmas cores do site).
class StatusPedido extends StatelessWidget {
  final String status;
  final String descricao;

  const StatusPedido(this.status, this.descricao, {super.key});

  @override
  Widget build(BuildContext context) {
    final (cor, fundo) = switch (status) {
      'PENDENTE' => (Cores.aviso, Cores.avisoFundo),
      'PAGO' || 'ENVIADO' => (Cores.info, Cores.infoFundo),
      'ENTREGUE' => (Cores.sucesso, Cores.sucessoFundo),
      'CANCELADO' => (Cores.erro, Cores.erroFundo),
      _ => (Cores.cinza700, Cores.cinza100),
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(color: fundo, borderRadius: BorderRadius.circular(20)),
      child: Text(descricao, style: TextStyle(color: cor, fontSize: 12, fontWeight: FontWeight.w600)),
    );
  }
}
