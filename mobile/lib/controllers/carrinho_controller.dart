import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/item_carrinho.dart';
import '../models/pedido.dart';
import '../models/produto.dart';

/// Carrinho do cliente, guardado NO CELULAR (o banco não tem tabela de carrinho).
///
/// Mesmas regras do site: um item por produto + tamanho, no máximo
/// 10 unidades, e nunca mais do que o estoque do tamanho.
/// Quando muda, avisa as telas (ex.: o número no ícone do carrinho).
class CarrinhoController extends ChangeNotifier {
  static const maximoPorItem = 10;
  static const _chave = 'carrinho';

  final List<ItemCarrinho> _itens = [];

  List<ItemCarrinho> get itens => List.unmodifiable(_itens);

  bool get vazio => _itens.isEmpty;

  /// Total de peças (para o número no ícone).
  int get quantidadeTotal => _itens.fold(0, (soma, i) => soma + i.quantidade);

  Future<void> carregar() async {
    final prefs = await SharedPreferences.getInstance();
    final texto = prefs.getString(_chave);
    _itens.clear();
    if (texto != null) {
      final lista = jsonDecode(texto) as List;
      _itens.addAll(lista.map((j) => ItemCarrinho.fromJson(j as Map<String, dynamic>)));
    }
    notifyListeners();
  }

  /// Adiciona ao carrinho. Devolve uma mensagem de erro, ou null se deu certo.
  String? adicionar(Produto produto, Tamanho tamanho, int quantidade) {
    final existente = _buscar(produto.id, tamanho.nome);
    final total = quantidade + (existente?.quantidade ?? 0);
    if (total > maximoPorItem) {
      return 'Máximo de $maximoPorItem unidades por produto e tamanho.';
    }
    if (total > tamanho.quantidade) {
      return 'Só temos ${tamanho.quantidade} unidade(s) no tamanho ${tamanho.nome}.';
    }
    if (existente != null) {
      existente.quantidade = total;
    } else {
      _itens.add(ItemCarrinho(
        idProduto: produto.id,
        nome: produto.nome,
        imagem: produto.imagemPrincipal,
        tamanho: tamanho.nome,
        quantidade: quantidade,
        preco: produto.preco,
      ));
    }
    _salvar();
    return null;
  }

  void alterarQuantidade(ItemCarrinho item, int quantidade) {
    if (quantidade < 1 || quantidade > maximoPorItem) return;
    item.quantidade = quantidade;
    _salvar();
  }

  void remover(ItemCarrinho item) {
    _itens.remove(item);
    _salvar();
  }

  void limpar() {
    _itens.clear();
    _salvar();
  }

  /// Troca o carrinho pelo que o SERVIDOR conferiu (preços e quantidades atuais).
  void substituirPeloServidor(List<ItemPedido> conferidos) {
    _itens
      ..clear()
      ..addAll(conferidos.map((i) => ItemCarrinho(
            idProduto: i.idProduto,
            nome: i.nomeProduto,
            imagem: i.imagem,
            tamanho: i.tamanho,
            quantidade: i.quantidade,
            preco: i.precoUnitario,
          )));
    _salvar();
  }

  ItemCarrinho? _buscar(int idProduto, String tamanho) {
    for (final i in _itens) {
      if (i.idProduto == idProduto && i.tamanho == tamanho) return i;
    }
    return null;
  }

  Future<void> _salvar() async {
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_chave, jsonEncode(_itens.map((i) => i.toJson()).toList()));
  }
}

/// Instância única usada pelo app inteiro.
final carrinho = CarrinhoController();
