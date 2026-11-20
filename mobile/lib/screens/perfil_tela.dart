import 'package:material_ui/material_ui.dart';

import '../controllers/carrinho_controller.dart';
import '../controllers/sessao_controller.dart';
import '../models/cliente.dart';
import '../services/api_service.dart';
import '../util/formato.dart';
import '../util/tema.dart';
import '../widgets/estados.dart';
import 'servidor_tela.dart';

/// Tela 10 da documentação: PERFIL DO CLIENTE.
///
/// GET /api/perfil (com o token): dados do cliente e endereços.
/// Também dá acesso à configuração do servidor e ao "Sair".
class PerfilTela extends StatefulWidget {
  const PerfilTela({super.key});

  @override
  State<PerfilTela> createState() => _PerfilTelaState();
}

class _PerfilTelaState extends State<PerfilTela> {
  late Future<Cliente> _cliente = ApiService.perfil();

  Future<void> _sair() async {
    final confirmar = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Sair da conta?'),
        content: const Text('Você precisará entrar de novo para comprar.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Cancelar')),
          TextButton(onPressed: () => Navigator.pop(context, true), child: const Text('Sair')),
        ],
      ),
    );
    if (confirmar == true) {
      carrinho.limpar();
      await ApiService.logout();
    }
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _cliente,
      builder: (context, snap) {
        if (snap.connectionState != ConnectionState.done) return const Carregando();
        if (snap.hasError) {
          return Erro(snap.error!, aoTentarDeNovo: () => setState(() => _cliente = ApiService.perfil()));
        }
        final c = snap.data!;
        return ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Row(
              children: [
                CircleAvatar(
                  radius: 30,
                  backgroundColor: Cores.preto,
                  child: Text(
                    c.primeiroNome.isEmpty ? '?' : c.primeiroNome[0].toUpperCase(),
                    style: const TextStyle(color: Cores.branco, fontFamily: fonteTitulo, fontSize: 26),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(c.nome, style: const TextStyle(fontFamily: fonteTitulo, fontSize: 21)),
                      Text(c.email, style: const TextStyle(color: Cores.cinza500)),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            _Bloco(children: [
              _Linha('CPF', Formato.cpf(c.cpf)),
              _Linha('Telefone', c.telefone == null || c.telefone!.isEmpty ? '—' : Formato.telefone(c.telefone)),
            ]),
            const SizedBox(height: 20),
            const Text('Endereços', style: TextStyle(fontFamily: fonteTitulo, fontSize: 19)),
            const SizedBox(height: 10),
            if (c.enderecos.isEmpty)
              const Text('Nenhum endereço cadastrado.', style: TextStyle(color: Cores.cinza500))
            else
              for (final e in c.enderecos)
                _Bloco(children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Icon(Icons.location_on_outlined, color: Cores.cinza500),
                      const SizedBox(width: 10),
                      Expanded(child: Text(e.formatado, style: const TextStyle(height: 1.4))),
                    ],
                  ),
                ]),
            const SizedBox(height: 20),
            _Bloco(children: [
              ListTile(
                contentPadding: EdgeInsets.zero,
                leading: const Icon(Icons.dns_outlined),
                title: const Text('Servidor'),
                subtitle: ListenableBuilder(
                  listenable: sessao,
                  builder: (context, _) => Text(sessao.servidor, style: const TextStyle(fontSize: 12)),
                ),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => ServidorTela.abrir(context),
              ),
            ]),
            const SizedBox(height: 20),
            OutlinedButton.icon(
              onPressed: _sair,
              icon: const Icon(Icons.logout),
              label: const Text('SAIR DA CONTA'),
            ),
          ],
        );
      },
    );
  }
}

class _Bloco extends StatelessWidget {
  final List<Widget> children;

  const _Bloco({required this.children});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(color: Cores.branco, borderRadius: BorderRadius.circular(6)),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: children),
    );
  }
}

class _Linha extends StatelessWidget {
  final String rotulo;
  final String valor;

  const _Linha(this.rotulo, this.valor);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        children: [
          Text(rotulo, style: const TextStyle(color: Cores.cinza500)),
          const Spacer(),
          Text(valor, style: const TextStyle(fontWeight: FontWeight.w500)),
        ],
      ),
    );
  }
}
