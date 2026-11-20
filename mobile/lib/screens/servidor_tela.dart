import 'package:material_ui/material_ui.dart';

import '../controllers/sessao_controller.dart';
import '../services/api_service.dart';
import '../util/tema.dart';

/// Configuração do endereço do servidor (computador com Tomcat + MySQL).
///
/// O IP muda conforme a rede Wi-Fi. Por isso ele NÃO fica fixo no código:
/// no dia da apresentação basta digitar o IP do computador aqui.
class ServidorTela extends StatefulWidget {
  const ServidorTela({super.key});

  static Future<void> abrir(BuildContext context) {
    return Navigator.of(context).push(MaterialPageRoute(builder: (_) => const ServidorTela()));
  }

  @override
  State<ServidorTela> createState() => _ServidorTelaState();
}

class _ServidorTelaState extends State<ServidorTela> {
  late final _campo = TextEditingController(text: sessao.servidor);
  bool _testando = false;
  String? _resultado;
  bool _ok = false;

  @override
  void dispose() {
    _campo.dispose();
    super.dispose();
  }

  Future<void> _testarESalvar() async {
    final endereco = SessaoController.normalizarServidor(_campo.text);
    setState(() {
      _testando = true;
      _resultado = null;
    });
    try {
      await ApiService.testarServidor(endereco);
      await sessao.definirServidor(endereco);
      _campo.text = endereco;
      _ok = true;
      _resultado = 'Conectado! Endereço salvo.';
    } on ApiException catch (e) {
      _ok = false;
      _resultado = e.mensagem;
    }
    if (mounted) setState(() => _testando = false);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Servidor')),
      body: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          const Text(
            'Endereço do computador onde estão o Tomcat e o MySQL. '
            'O celular precisa estar na mesma rede Wi-Fi.',
            style: TextStyle(color: Cores.cinza700, height: 1.4),
          ),
          const SizedBox(height: 20),
          TextField(
            controller: _campo,
            keyboardType: TextInputType.url,
            autocorrect: false,
            decoration: const InputDecoration(
              labelText: 'Endereço do servidor',
              helperText: 'Pode digitar só o IP. Ex.: 192.168.1.109',
            ),
            onSubmitted: (_) => _testarESalvar(),
          ),
          const SizedBox(height: 20),
          FilledButton(
            onPressed: _testando ? null : _testarESalvar,
            child: _testando
                ? const SizedBox.square(dimension: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Cores.branco))
                : const Text('TESTAR E SALVAR'),
          ),
          if (_resultado != null) ...[
            const SizedBox(height: 20),
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: _ok ? Cores.sucessoFundo : Cores.erroFundo,
                borderRadius: BorderRadius.circular(6),
              ),
              child: Row(
                children: [
                  Icon(_ok ? Icons.check_circle_outline : Icons.error_outline, color: _ok ? Cores.sucesso : Cores.erro),
                  const SizedBox(width: 10),
                  Expanded(child: Text(_resultado!, style: TextStyle(color: _ok ? Cores.sucesso : Cores.erro))),
                ],
              ),
            ),
          ],
          const SizedBox(height: 28),
          const Text('Como descobrir o IP', style: TextStyle(fontWeight: FontWeight.w600)),
          const SizedBox(height: 6),
          const Text(
            'No computador, abra o terminal e digite "ipconfig". '
            'Use o "Endereço IPv4" do adaptador Wi-Fi.',
            style: TextStyle(color: Cores.cinza500, height: 1.4),
          ),
        ],
      ),
    );
  }
}
