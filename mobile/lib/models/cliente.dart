/// Endereço de entrega do cliente.
class Endereco {
  final int id;
  final String cep;
  final String logradouro;
  final String numero;
  final String? complemento;
  final String bairro;
  final String cidade;
  final String estado;

  /// Texto pronto vindo do servidor (ex.: "Rua das Flores, 100 - Centro...").
  final String formatado;

  const Endereco({
    required this.id,
    required this.cep,
    required this.logradouro,
    required this.numero,
    this.complemento,
    required this.bairro,
    required this.cidade,
    required this.estado,
    required this.formatado,
  });

  factory Endereco.fromJson(Map<String, dynamic> json) {
    return Endereco(
      id: json['idEndereco'] as int,
      cep: json['cep'] as String? ?? '',
      logradouro: json['logradouro'] as String? ?? '',
      numero: json['numero'] as String? ?? '',
      complemento: json['complemento'] as String?,
      bairro: json['bairro'] as String? ?? '',
      cidade: json['cidade'] as String? ?? '',
      estado: json['estado'] as String? ?? '',
      formatado: json['enderecoFormatado'] as String? ?? '',
    );
  }
}

/// Cliente logado. A API NUNCA envia a senha.
class Cliente {
  final int id;
  final String nome;
  final String email;
  final String cpf;
  final String? telefone;
  final List<Endereco> enderecos;

  const Cliente({
    required this.id,
    required this.nome,
    required this.email,
    required this.cpf,
    this.telefone,
    required this.enderecos,
  });

  /// "Ana Souza" -> "Ana"
  String get primeiroNome => nome.trim().split(' ').first;

  factory Cliente.fromJson(Map<String, dynamic> json) {
    return Cliente(
      id: json['idCliente'] as int,
      nome: json['nome'] as String,
      email: json['email'] as String,
      cpf: json['cpf'] as String? ?? '',
      telefone: json['telefone'] as String?,
      enderecos: (json['enderecos'] as List? ?? const [])
          .map((e) => Endereco.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }
}
