import 'package:material_ui/material_ui.dart';

import 'controllers/carrinho_controller.dart';
import 'controllers/sessao_controller.dart';
import 'screens/login_tela.dart';
import 'screens/principal_tela.dart';
import 'util/tema.dart';

/// Ponto de entrada do app FashionStore.
///
/// 1. Lê do celular o endereço do servidor, o login salvo e o carrinho.
/// 2. Abre o app: se o cliente já estiver logado, vai direto para a loja;
///    senão, mostra a tela de login (fluxo da documentação: LOGIN -> TELA INICIAL).
Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await sessao.carregar();
  await carrinho.carregar();
  runApp(const FashionStoreApp());
}

class FashionStoreApp extends StatelessWidget {
  const FashionStoreApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'FashionStore',
      debugShowCheckedModeBanner: false,
      theme: temaFashionStore(),
      navigatorKey: navegador,
      // Sempre que a sessão muda (login, logout, token vencido), a raiz é redesenhada.
      home: ListenableBuilder(
        listenable: sessao,
        builder: (context, _) => sessao.logado ? const PrincipalTela() : const LoginTela(),
      ),
    );
  }
}
