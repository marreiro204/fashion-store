/// Item guardado no carrinho DO CELULAR (o banco não tem tabela de carrinho).
///
/// Para o servidor só interessam idProduto + tamanho + quantidade.
/// Nome, imagem e preço servem apenas para mostrar na tela; o preço
/// verdadeiro é sempre conferido pela API (POST /api/carrinho/validar).
class ItemCarrinho {
  final int idProduto;
  final String nome;
  final String? imagem;
  final String tamanho;
  int quantidade;
  num preco;

  ItemCarrinho({
    required this.idProduto,
    required this.nome,
    this.imagem,
    required this.tamanho,
    required this.quantidade,
    required this.preco,
  });

  /// Formato enviado à API.
  Map<String, dynamic> paraApi() => {
        'idProduto': idProduto,
        'tamanho': tamanho,
        'quantidade': quantidade,
      };

  /// Formato salvo no celular (shared_preferences).
  Map<String, dynamic> toJson() => {
        'idProduto': idProduto,
        'nome': nome,
        'imagem': imagem,
        'tamanho': tamanho,
        'quantidade': quantidade,
        'preco': preco,
      };

  factory ItemCarrinho.fromJson(Map<String, dynamic> json) {
    return ItemCarrinho(
      idProduto: json['idProduto'] as int,
      nome: json['nome'] as String,
      imagem: json['imagem'] as String?,
      tamanho: json['tamanho'] as String,
      quantidade: json['quantidade'] as int,
      preco: json['preco'] as num,
    );
  }
}
