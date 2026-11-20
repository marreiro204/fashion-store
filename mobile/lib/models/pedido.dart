import 'cliente.dart';

/// Um item do pedido (ou do carrinho conferido pelo servidor).
/// Os preços sempre vêm do servidor: o app nunca define preço.
class ItemPedido {
  final int idProduto;
  final String nomeProduto;
  final String? imagem;
  final String tamanho;
  final int quantidade;
  final num precoUnitario;
  final num subtotal;

  const ItemPedido({
    required this.idProduto,
    required this.nomeProduto,
    this.imagem,
    required this.tamanho,
    required this.quantidade,
    required this.precoUnitario,
    required this.subtotal,
  });

  factory ItemPedido.fromJson(Map<String, dynamic> json) {
    final produto = json['produto'] as Map<String, dynamic>;
    return ItemPedido(
      idProduto: produto['idProduto'] as int,
      nomeProduto: produto['nome'] as String,
      imagem: produto['imagem'] as String?,
      tamanho: json['tamanho'] as String,
      quantidade: json['quantidade'] as int,
      precoUnitario: json['precoUnitario'] as num,
      subtotal: json['subtotal'] as num,
    );
  }
}

/// Pedido do cliente. Espelha GET /api/pedidos e /api/pedidos/{id}.
/// Na lista (histórico) os itens vêm vazios; nos detalhes, completos.
class Pedido {
  final int id;
  final DateTime data;

  /// PENDENTE, PAGO, ENVIADO, ENTREGUE ou CANCELADO.
  final String status;

  /// Texto para a tela (ex.: "Pendente").
  final String statusDescricao;
  final num valorTotal;
  final Endereco? endereco;
  final List<ItemPedido> itens;

  const Pedido({
    required this.id,
    required this.data,
    required this.status,
    required this.statusDescricao,
    required this.valorTotal,
    this.endereco,
    required this.itens,
  });

  factory Pedido.fromJson(Map<String, dynamic> json) {
    return Pedido(
      id: json['idPedido'] as int,
      data: DateTime.parse(json['dataPedido'] as String),
      status: json['status'] as String,
      statusDescricao: json['statusDescricao'] as String? ?? json['status'] as String,
      valorTotal: json['valorTotal'] as num,
      endereco: json['endereco'] == null
          ? null
          : Endereco.fromJson(json['endereco'] as Map<String, dynamic>),
      itens: (json['itens'] as List? ?? const [])
          .map((i) => ItemPedido.fromJson(i as Map<String, dynamic>))
          .toList(),
    );
  }
}
