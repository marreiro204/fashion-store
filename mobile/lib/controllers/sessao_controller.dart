import 'dart:convert';

import 'package:material_ui/material_ui.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/cliente.dart';

/// Chave do navegador do app: usada para voltar ao início quando a sessão expira.
final navegador = GlobalKey<NavigatorState>();

/// Sessão do cliente: endereço do servidor, token de login e dados do cliente.
///
/// Fica guardada no celular (shared_preferences), então o cliente continua
/// logado ao fechar e abrir o app. Quando muda, avisa as telas (ChangeNotifier).
class SessaoController extends ChangeNotifier {
  static const servidorPadrao = 'http://192.168.1.109:8080/FashionStore';

  static const _chaveServidor = 'servidor';
  static const _chaveToken = 'token';
  static const _chaveCliente = 'cliente';

  String servidor = servidorPadrao;
  String? token;
  Cliente? cliente;

  /// Mensagem mostrada na tela de login (ex.: "Sua sessão expirou").
  String? aviso;

  bool get logado => token != null;

  /// Lê o que ficou salvo no celular (chamado ao abrir o app).
  Future<void> carregar() async {
    final prefs = await SharedPreferences.getInstance();
    servidor = prefs.getString(_chaveServidor) ?? servidorPadrao;
    token = prefs.getString(_chaveToken);
    final clienteJson = prefs.getString(_chaveCliente);
    if (token != null && clienteJson != null) {
      cliente = Cliente.fromJson(jsonDecode(clienteJson) as Map<String, dynamic>);
    } else {
      token = null;
    }
    notifyListeners();
  }

  Future<void> definirServidor(String endereco) async {
    servidor = normalizarServidor(endereco);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_chaveServidor, servidor);
    notifyListeners();
  }

  Future<void> entrar(String novoToken, Cliente dados) async {
    token = novoToken;
    aviso = null;
    await atualizarCliente(dados);
  }

  Future<void> atualizarCliente(Cliente dados) async {
    cliente = dados;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_chaveToken, token!);
    await prefs.setString(_chaveCliente, jsonEncode(_clienteParaJson(dados)));
    notifyListeners();
  }

  Future<void> sair({String? mensagem}) async {
    token = null;
    cliente = null;
    aviso = mensagem;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_chaveToken);
    await prefs.remove(_chaveCliente);
    // Fecha as telas abertas e volta para a raiz (que mostrará o login).
    navegador.currentState?.popUntil((rota) => rota.isFirst);
    notifyListeners();
  }

  /// A API respondeu 401: token vencido ou servidor reiniciado.
  Future<void> expirou() => sair(mensagem: 'Sua sessão expirou. Entre novamente.');

  /// Aceita só o IP ("192.168.1.109") ou o endereço completo.
  /// "192.168.1.109" -> "http://192.168.1.109:8080/FashionStore"
  static String normalizarServidor(String entrada) {
    var s = entrada.trim();
    if (s.endsWith('/')) s = s.substring(0, s.length - 1);
    if (!s.startsWith('http://') && !s.startsWith('https://')) s = 'http://$s';
    final uri = Uri.tryParse(s);
    if (uri == null || uri.host.isEmpty) return s;
    final porta = uri.hasPort ? uri.port : 8080;
    final caminho = uri.path.isEmpty ? '/FashionStore' : uri.path;
    return '${uri.scheme}://${uri.host}:$porta$caminho';
  }

  static Map<String, dynamic> _clienteParaJson(Cliente c) => {
        'idCliente': c.id,
        'nome': c.nome,
        'email': c.email,
        'cpf': c.cpf,
        'telefone': c.telefone,
        'enderecos': c.enderecos
            .map((e) => {
                  'idEndereco': e.id,
                  'cep': e.cep,
                  'logradouro': e.logradouro,
                  'numero': e.numero,
                  'complemento': e.complemento,
                  'bairro': e.bairro,
                  'cidade': e.cidade,
                  'estado': e.estado,
                  'enderecoFormatado': e.formatado,
                })
            .toList(),
      };
}

/// Instância única usada pelo app inteiro.
final sessao = SessaoController();
