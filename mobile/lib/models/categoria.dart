/// Categoria do catálogo (ex.: Camisetas, Vestidos).
/// Espelha o JSON de GET /api/categorias.
class Categoria {
  final int id;
  final String nome;
  final String? descricao;

  const Categoria({required this.id, required this.nome, this.descricao});

  factory Categoria.fromJson(Map<String, dynamic> json) {
    return Categoria(
      id: json['idCategoria'] as int,
      nome: json['nome'] as String,
      descricao: json['descricao'] as String?,
    );
  }
}
