import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;

import '../controllers/sessao_controller.dart';
import '../models/categoria.dart';
import '../models/cliente.dart';
import '../models/item_carrinho.dart';
import '../models/pedido.dart';
import '../models/produto.dart';

/// Erro devolvido pela API (ou falta de conexão).
///
/// [status] = código HTTP (400, 401, 404, 409...). 0 = sem conexão com o servidor.
/// [campos] = mensagens por campo do formulário (ex.: {"email": "Este e-mail já..."}).
class ApiException implements Exception {
  final int status;
  final String mensagem;
  final Map<String, String> campos;

  const ApiException(this.status, this.mensagem, [this.campos = const {}]);

  bool get semConexao => status == 0;

  @override
  String toString() => mensagem;
}

/// Resposta do POST /api/carrinho/validar: itens com preço ATUAL do banco.
class CarrinhoConferido {
  final List<ItemPedido> itens;
  final num total;
  final List<String> avisos;

  const CarrinhoConferido(this.itens, this.total, this.avisos);
}

/// ÚNICO lugar do app que conversa com a API Java.
///
/// As telas nunca fazem HTTP direto: pedem ao ApiService.
/// Fluxo: Tela -> ApiService -> (HTTP/JSON) -> Servlet Java -> Service -> DAO -> MySQL.
class ApiService {
  ApiService._();

  static const _tempoLimite = Duration(seconds: 12);

  // ======================= Catálogo (sem login) =======================

  static Future<List<Categoria>> categorias() async {
    final lista = await _get('/categorias') as List;
    return lista.map((j) => Categoria.fromJson(j as Map<String, dynamic>)).toList();
  }

  /// [ordem]: nome, novidades, menor-preco ou maior-preco.
  static Future<List<Produto>> produtos({String? busca, int? idCategoria, String? ordem}) async {
    final parametros = <String, String>{
      if (busca != null && busca.trim().isNotEmpty) 'busca': busca.trim(),
      'categoria': ?idCategoria?.toString(),
      'ordem': ?ordem,
    };
    final lista = await _get('/produtos', parametros: parametros) as List;
    return lista.map((j) => Produto.fromJson(j as Map<String, dynamic>)).toList();
  }

  static Future<Produto> produto(int id) async {
    return Produto.fromJson(await _get('/produtos/$id') as Map<String, dynamic>);
  }

  // ======================= Conta =======================

  /// Login: devolve o token e guarda a sessão no celular.
  static Future<void> login(String email, String senha) async {
    final resposta = await _post('/login', {'email': email.trim(), 'senha': senha});
    await _guardarSessao(resposta as Map<String, dynamic>);
  }

  /// Cadastro do cliente + primeiro endereço (mesmas regras do site).
  static Future<void> cadastrar(Map<String, dynamic> dados) async {
    final resposta = await _post('/clientes', dados);
    await _guardarSessao(resposta as Map<String, dynamic>);
  }

  static Future<void> logout() async {
    try {
      await _post('/logout', const {}, autenticado: true);
    } catch (_) {
      // Mesmo sem rede, o app esquece o token.
    }
    await sessao.sair();
  }

  /// Dados do cliente logado (com endereços).
  static Future<Cliente> perfil() async {
    final cliente = Cliente.fromJson(await _get('/perfil', autenticado: true) as Map<String, dynamic>);
    await sessao.atualizarCliente(cliente);
    return cliente;
  }

  // ======================= Carrinho e pedidos =======================

  /// O servidor confere estoque e devolve os preços atuais do banco.
  static Future<CarrinhoConferido> validarCarrinho(List<ItemCarrinho> itens) async {
    final json = await _post('/carrinho/validar', {
      'itens': itens.map((i) => i.paraApi()).toList(),
    }) as Map<String, dynamic>;
    return CarrinhoConferido(
      (json['itens'] as List).map((i) => ItemPedido.fromJson(i as Map<String, dynamic>)).toList(),
      json['total'] as num,
      List<String>.from(json['avisos'] as List? ?? const []),
    );
  }

  /// Finaliza a compra. O app envia só produto + tamanho + quantidade;
  /// o PREÇO e o TOTAL são calculados pelo servidor.
  static Future<Pedido> finalizarPedido(int idEndereco, List<ItemCarrinho> itens) async {
    final json = await _post('/pedidos', {
      'idEndereco': idEndereco,
      'itens': itens.map((i) => i.paraApi()).toList(),
    }, autenticado: true);
    return Pedido.fromJson(json as Map<String, dynamic>);
  }

  static Future<List<Pedido>> pedidos() async {
    final lista = await _get('/pedidos', autenticado: true) as List;
    return lista.map((j) => Pedido.fromJson(j as Map<String, dynamic>)).toList();
  }

  static Future<Pedido> pedido(int id) async {
    return Pedido.fromJson(await _get('/pedidos/$id', autenticado: true) as Map<String, dynamic>);
  }

  /// Usado na tela "Servidor": confirma se o endereço digitado responde.
  static Future<void> testarServidor(String servidor) async {
    await _enviar('GET', '/categorias', servidor: servidor);
  }

  // ======================= HTTP (parte interna) =======================

  static Future<void> _guardarSessao(Map<String, dynamic> json) async {
    await sessao.entrar(
      json['token'] as String,
      Cliente.fromJson(json['cliente'] as Map<String, dynamic>),
    );
  }

  static Future<dynamic> _get(String caminho, {Map<String, String>? parametros, bool autenticado = false}) {
    return _enviar('GET', caminho, parametros: parametros, autenticado: autenticado);
  }

  static Future<dynamic> _post(String caminho, Object corpo, {bool autenticado = false}) {
    return _enviar('POST', caminho, corpo: corpo, autenticado: autenticado);
  }

  static Future<dynamic> _enviar(
    String metodo,
    String caminho, {
    Map<String, String>? parametros,
    Object? corpo,
    bool autenticado = false,
    String? servidor,
  }) async {
    final base = servidor ?? sessao.servidor;
    final url = Uri.parse('$base/api$caminho').replace(
      queryParameters: (parametros == null || parametros.isEmpty) ? null : parametros,
    );
    final cabecalhos = <String, String>{
      'Accept': 'application/json',
      if (corpo != null) 'Content-Type': 'application/json; charset=utf-8',
      // Login por token: "Authorization: Bearer <token>"
      if (autenticado && sessao.token != null) 'Authorization': 'Bearer ${sessao.token}',
    };

    final http.Response resposta;
    try {
      resposta = metodo == 'GET'
          ? await http.get(url, headers: cabecalhos).timeout(_tempoLimite)
          : await http
              .post(url, headers: cabecalhos, body: jsonEncode(corpo))
              .timeout(_tempoLimite);
    } on TimeoutException {
      throw const ApiException(0, 'O servidor demorou para responder. Verifique a rede e o endereço do servidor.');
    } on SocketException {
      throw const ApiException(0, 'Não foi possível conectar ao servidor. Verifique se o celular está na mesma rede Wi-Fi do computador.');
    } on http.ClientException {
      throw const ApiException(0, 'Não foi possível conectar ao servidor. Verifique o endereço do servidor.');
    } on FormatException {
      throw const ApiException(0, 'Endereço do servidor inválido.');
    }

    // utf8.decode: garante acentos corretos ("Calça", e não "CalÃ§a").
    final texto = utf8.decode(resposta.bodyBytes);
    final dynamic json = texto.isEmpty ? null : _lerJson(texto);

    if (resposta.statusCode >= 200 && resposta.statusCode < 300) {
      return json;
    }

    // Erro padronizado da API: {"erro": "...", "campos": {...}}
    final mensagem = json is Map && json['erro'] is String
        ? json['erro'] as String
        : 'Erro inesperado no servidor (${resposta.statusCode}).';
    final campos = json is Map && json['campos'] is Map
        ? Map<String, String>.from(json['campos'] as Map)
        : const <String, String>{};

    // Token vencido (ou servidor reiniciado): volta para o login.
    if (resposta.statusCode == 401 && autenticado) {
      await sessao.expirou();
    }
    throw ApiException(resposta.statusCode, mensagem, campos);
  }

  static dynamic _lerJson(String texto) {
    try {
      return jsonDecode(texto);
    } on FormatException {
      throw const ApiException(0, 'O endereço informado não é o servidor do FashionStore.');
    }
  }
}
