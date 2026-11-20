import 'categoria.dart';

/// Quantidade disponível de um tamanho (ex.: M = 3).
class Tamanho {
  final String nome;
  final int quantidade;

  /// true quando restam poucas unidades (o site mostra "Últimas").
  final bool ultimas;

  const Tamanho({required this.nome, required this.quantidade, required this.ultimas});

  bool get disponivel => quantidade > 0;

  factory Tamanho.fromJson(Map<String, dynamic> json) {
    return Tamanho(
      nome: json['tamanho'] as String,
      quantidade: json['quantidade'] as int,
      ultimas: json['ultimas'] as bool? ?? false,
    );
  }
}

/// Produto da loja. Espelha o JSON de GET /api/produtos e /api/produtos/{id}.
///
/// As imagens chegam com o endereço COMPLETO do servidor
/// (http://IP:8080/FashionStore/assets/...), por isso o app não guarda fotos.
class Produto {
  final int id;
  final String nome;
  final String? descricao;
  final num preco;
  final Categoria? categoria;
  final List<String> imagens;
  final List<Tamanho> tamanhos;
  final bool esgotado;

  const Produto({
    required this.id,
    required this.nome,
    this.descricao,
    required this.preco,
    this.categoria,
    required this.imagens,
    required this.tamanhos,
    required this.esgotado,
  });

  /// Primeira foto (ou null = mostrar imagem provisória).
  String? get imagemPrincipal => imagens.isEmpty ? null : imagens.first;

  factory Produto.fromJson(Map<String, dynamic> json) {
    return Produto(
      id: json['idProduto'] as int,
      nome: json['nome'] as String,
      descricao: json['descricao'] as String?,
      preco: json['preco'] as num,
      categoria: json['categoria'] == null
          ? null
          : Categoria.fromJson(json['categoria'] as Map<String, dynamic>),
      imagens: List<String>.from(json['imagens'] as List? ?? const []),
      tamanhos: (json['tamanhos'] as List? ?? const [])
          .map((t) => Tamanho.fromJson(t as Map<String, dynamic>))
          .toList(),
      esgotado: json['esgotado'] as bool? ?? false,
    );
  }
}
