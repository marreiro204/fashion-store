import 'package:material_ui/material_ui.dart';

import '../screens/servidor_tela.dart';
import '../services/api_service.dart';
import '../util/tema.dart';

/// Indicador de "carregando" centralizado.
class Carregando extends StatelessWidget {
  const Carregando({super.key});

  @override
  Widget build(BuildContext context) => const Center(child: CircularProgressIndicator());
}

/// Tela de erro com botão "Tentar novamente".
/// Se o problema for falta de conexão, oferece também "Configurar servidor".
class Erro extends StatelessWidget {
  final Object erro;
  final VoidCallback aoTentarDeNovo;

  const Erro(this.erro, {super.key, required this.aoTentarDeNovo});

  @override
  Widget build(BuildContext context) {
    final semConexao = erro is ApiException && (erro as ApiException).semConexao;
    final mensagem = erro is ApiException ? erro.toString() : 'Algo deu errado. Tente novamente.';
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(semConexao ? Icons.wifi_off_rounded : Icons.error_outline, size: 48, color: Cores.cinza500),
            const SizedBox(height: 16),
            Text(mensagem, textAlign: TextAlign.center, style: const TextStyle(color: Cores.cinza700, height: 1.4)),
            const SizedBox(height: 24),
            OutlinedButton(onPressed: aoTentarDeNovo, child: const Text('Tentar novamente')),
            if (semConexao) ...[
              const SizedBox(height: 8),
              TextButton.icon(
                onPressed: () => ServidorTela.abrir(context),
                icon: const Icon(Icons.settings_outlined, size: 18),
                label: const Text('Configurar servidor'),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

/// Mensagem para lista vazia (ex.: "Nenhum pedido ainda").
class Vazio extends StatelessWidget {
  final IconData icone;
  final String titulo;
  final String? texto;

  const Vazio({super.key, required this.icone, required this.titulo, this.texto});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icone, size: 52, color: Cores.cinza300),
            const SizedBox(height: 16),
            Text(titulo, style: const TextStyle(fontFamily: fonteTitulo, fontSize: 20)),
            if (texto != null) ...[
              const SizedBox(height: 8),
              Text(texto!, textAlign: TextAlign.center, style: const TextStyle(color: Cores.cinza500)),
            ],
          ],
        ),
      ),
    );
  }
}

/// Mostra uma mensagem rápida no rodapé.
void mostrarMensagem(BuildContext context, String texto, {bool erro = false}) {
  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(SnackBar(
      content: Text(texto),
      backgroundColor: erro ? Cores.erro : Cores.grafite,
    ));
}
