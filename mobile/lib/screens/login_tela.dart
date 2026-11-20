import 'package:material_ui/material_ui.dart';

import '../controllers/sessao_controller.dart';
import '../services/api_service.dart';
import '../util/tema.dart';
import 'cadastro_tela.dart';
import 'servidor_tela.dart';

/// Tela 1 da documentação: LOGIN.
///
/// Envia e-mail e senha para POST /api/login. O servidor confere o hash
/// BCrypt (o mesmo código do site) e devolve um TOKEN, que fica salvo no
/// celular. A partir daí o main.dart mostra a loja automaticamente.
class LoginTela extends StatefulWidget {
  const LoginTela({super.key});

  @override
  State<LoginTela> createState() => _LoginTelaState();
}

class _LoginTelaState extends State<LoginTela> {
  final _email = TextEditingController();
  final _senha = TextEditingController();
  bool _ocultarSenha = true;
  bool _enviando = false;
  String? _erro;

  @override
  void initState() {
    super.initState();
    _erro = sessao.aviso; // ex.: "Sua sessão expirou."
  }

  @override
  void dispose() {
    _email.dispose();
    _senha.dispose();
    super.dispose();
  }

  Future<void> _entrar() async {
    if (_email.text.trim().isEmpty || _senha.text.isEmpty) {
      setState(() => _erro = 'Informe e-mail e senha.');
      return;
    }
    setState(() {
      _enviando = true;
      _erro = null;
    });
    try {
      await ApiService.login(_email.text, _senha.text);
      // Sucesso: a sessão muda e o main.dart troca para a loja sozinho.
    } on ApiException catch (e) {
      if (mounted) setState(() => _erro = e.mensagem);
    } finally {
      if (mounted) setState(() => _enviando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Cores.branco,
      appBar: AppBar(
        backgroundColor: Cores.branco,
        actions: [
          IconButton(
            tooltip: 'Servidor',
            icon: const Icon(Icons.settings_outlined),
            onPressed: () => ServidorTela.abrir(context),
          ),
        ],
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(28, 0, 28, 28),
          children: [
            Center(child: Image.asset('assets/logo.png', height: 120)),
            const SizedBox(height: 28),
            const Text(
              'Entrar',
              textAlign: TextAlign.center,
              style: TextStyle(fontFamily: fonteTitulo, fontSize: 28),
            ),
            const SizedBox(height: 6),
            const Text(
              'Acesse sua conta para comprar e acompanhar seus pedidos.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Cores.cinza500),
            ),
            const SizedBox(height: 28),
            if (_erro != null) ...[
              _Alerta(_erro!),
              const SizedBox(height: 16),
            ],
            TextField(
              controller: _email,
              keyboardType: TextInputType.emailAddress,
              autocorrect: false,
              textInputAction: TextInputAction.next,
              decoration: const InputDecoration(labelText: 'E-mail', prefixIcon: Icon(Icons.mail_outline)),
            ),
            const SizedBox(height: 14),
            TextField(
              controller: _senha,
              obscureText: _ocultarSenha,
              textInputAction: TextInputAction.done,
              onSubmitted: (_) => _entrar(),
              decoration: InputDecoration(
                labelText: 'Senha',
                prefixIcon: const Icon(Icons.lock_outline),
                suffixIcon: IconButton(
                  icon: Icon(_ocultarSenha ? Icons.visibility_outlined : Icons.visibility_off_outlined),
                  onPressed: () => setState(() => _ocultarSenha = !_ocultarSenha),
                ),
              ),
            ),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: _enviando ? null : _entrar,
              child: _enviando
                  ? const SizedBox.square(dimension: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Cores.branco))
                  : const Text('ENTRAR'),
            ),
            const SizedBox(height: 12),
            OutlinedButton(
              onPressed: _enviando
                  ? null
                  : () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const CadastroTela())),
              child: const Text('CRIAR CONTA'),
            ),
          ],
        ),
      ),
    );
  }
}

class _Alerta extends StatelessWidget {
  final String texto;

  const _Alerta(this.texto);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: Cores.erroFundo, borderRadius: BorderRadius.circular(6)),
      child: Row(
        children: [
          const Icon(Icons.error_outline, color: Cores.erro, size: 20),
          const SizedBox(width: 10),
          Expanded(child: Text(texto, style: const TextStyle(color: Cores.erro))),
          if (texto.contains('conectar') || texto.contains('servidor'))
            TextButton(
              onPressed: () => ServidorTela.abrir(context),
              child: const Text('Servidor'),
            ),
        ],
      ),
    );
  }
}
