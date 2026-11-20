import 'package:material_ui/material_ui.dart';

import '../controllers/carrinho_controller.dart';
import '../controllers/sessao_controller.dart';
import '../models/cliente.dart';
import '../services/api_service.dart';
import '../util/formato.dart';
import '../util/tema.dart';
import '../widgets/estados.dart';
import 'pedido_tela.dart';

/// Tela 7 da documentação: FINALIZAÇÃO DO PEDIDO.
///
/// O cliente escolhe o endereço e confirma. O app envia SÓ:
///   { idEndereco, itens: [ {idProduto, tamanho, quantidade} ] }
/// O servidor busca os preços no banco, confere o estoque de novo, baixa o
/// estoque e grava o pedido como PENDENTE, tudo numa transação.
class FinalizarTela extends StatefulWidget {
  const FinalizarTela({super.key});

  @override
  State<FinalizarTela> createState() => _FinalizarTelaState();
}

class _FinalizarTelaState extends State<FinalizarTela> {
  late Future<(Cliente, CarrinhoConferido)> _dados = _carregar();
  int? _idEndereco;
  bool _enviando = false;

  Future<(Cliente, CarrinhoConferido)> _carregar() async {
    final cliente = ApiService.perfil();
    final conferido = ApiService.validarCarrinho(carrinho.itens);
    final resultado = (await cliente, await conferido);
    if (resultado.$1.enderecos.isNotEmpty) {
      _idEndereco ??= resultado.$1.enderecos.first.id;
    }
    return resultado;
  }

  Future<void> _confirmar() async {
    setState(() => _enviando = true);
    try {
      final pedido = await ApiService.finalizarPedido(_idEndereco!, carrinho.itens);
      carrinho.limpar();
      // Volta para a loja e abre o pedido recém-criado.
      final nav = navegador.currentState!;
      nav.popUntil((rota) => rota.isFirst);
      nav.push(MaterialPageRoute(builder: (_) => PedidoTela(idPedido: pedido.id, recemCriado: true)));
    } on ApiException catch (e) {
      if (!mounted) return;
      setState(() => _enviando = false);
      mostrarMensagem(context, e.mensagem, erro: true);
      // 409 = estoque mudou: volta ao carrinho, que confere tudo de novo.
      if (e.status == 409) Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Finalizar pedido')),
      body: FutureBuilder(
        future: _dados,
        builder: (context, snap) {
          if (snap.connectionState != ConnectionState.done) return const Carregando();
          if (snap.hasError) {
            return Erro(snap.error!, aoTentarDeNovo: () => setState(() => _dados = _carregar()));
          }
          final (cliente, conferido) = snap.data!;
          return Column(
            children: [
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.all(20),
                  children: [
                    const _Titulo('Endereço de entrega'),
                    if (cliente.enderecos.isEmpty)
                      const Text(
                        'Você não tem endereço cadastrado. Cadastre um endereço no site para continuar.',
                        style: TextStyle(color: Cores.erro),
                      )
                    else
                      for (final e in cliente.enderecos) _opcaoEndereco(e),
                    const SizedBox(height: 20),
                    const _Titulo('Resumo'),
                    Container(
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(color: Cores.branco, borderRadius: BorderRadius.circular(6)),
                      child: Column(
                        children: [
                          for (final i in conferido.itens)
                            Padding(
                              padding: const EdgeInsets.symmetric(vertical: 5),
                              child: Row(
                                children: [
                                  Expanded(child: Text('${i.quantidade}× ${i.nomeProduto} (${i.tamanho})')),
                                  Text(Formato.dinheiro(i.subtotal)),
                                ],
                              ),
                            ),
                          const Divider(height: 22),
                          Row(
                            children: [
                              const Text('Total', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
                              const Spacer(),
                              Text(Formato.dinheiro(conferido.total),
                                  style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 18)),
                            ],
                          ),
                        ],
                      ),
                    ),
                    for (final aviso in conferido.avisos)
                      Padding(
                        padding: const EdgeInsets.only(top: 10),
                        child: Text(aviso, style: const TextStyle(color: Cores.aviso)),
                      ),
                    const SizedBox(height: 14),
                    const Text(
                      'O pagamento é combinado com a loja. Seu pedido começa como "Pendente" e você '
                      'acompanha o status em Meus pedidos.',
                      style: TextStyle(color: Cores.cinza500, fontSize: 13, height: 1.4),
                    ),
                  ],
                ),
              ),
              SafeArea(
                top: false,
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(20, 8, 20, 12),
                  child: FilledButton(
                    onPressed: _enviando || _idEndereco == null || conferido.itens.isEmpty ? null : _confirmar,
                    child: _enviando
                        ? const SizedBox.square(
                            dimension: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Cores.branco))
                        : Text('CONFIRMAR PEDIDO · ${Formato.dinheiro(conferido.total)}'),
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _opcaoEndereco(Endereco e) {
    final selecionado = e.id == _idEndereco;
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      decoration: BoxDecoration(
        color: Cores.branco,
        borderRadius: BorderRadius.circular(6),
        border: Border.all(color: selecionado ? Cores.preto : Cores.cinza300, width: selecionado ? 1.5 : 1),
      ),
      child: ListTile(
        onTap: () => setState(() => _idEndereco = e.id),
        leading: Icon(selecionado ? Icons.radio_button_checked : Icons.radio_button_unchecked,
            color: selecionado ? Cores.preto : Cores.cinza500),
        title: Text('${e.logradouro}, ${e.numero}'),
        subtitle: Text(e.formatado, style: const TextStyle(fontSize: 12)),
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
