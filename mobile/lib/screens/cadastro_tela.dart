import 'package:material_ui/material_ui.dart';

import '../services/api_service.dart';
import '../util/tema.dart';
import '../widgets/estados.dart';

/// Tela 2 da documentação: CADASTRO.
///
/// Envia os dados para POST /api/clientes. As validações são as MESMAS do
/// site (feitas no servidor Java): se algo estiver errado, a API devolve a
/// mensagem de cada campo em "campos" e a tela mostra embaixo do campo.
class CadastroTela extends StatefulWidget {
  const CadastroTela({super.key});

  @override
  State<CadastroTela> createState() => _CadastroTelaState();
}

class _CadastroTelaState extends State<CadastroTela> {
  static const _ufs = [
    'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
    'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
  ];

  // Um controlador por campo; a chave é o MESMO nome usado pela API.
  final _campos = {
    for (final c in [
      'nome', 'email', 'cpf', 'telefone', 'senha', 'confirmacao',
      'cep', 'logradouro', 'numero', 'complemento', 'bairro', 'cidade',
    ])
      c: TextEditingController(),
  };
  String? _estado;
  Map<String, String> _erros = {};
  bool _enviando = false;

  @override
  void dispose() {
    for (final c in _campos.values) {
      c.dispose();
    }
    super.dispose();
  }

  String _v(String campo) => _campos[campo]!.text.trim();

  Future<void> _cadastrar() async {
    FocusScope.of(context).unfocus();
    setState(() {
      _enviando = true;
      _erros = {};
    });
    try {
      await ApiService.cadastrar({
        'nome': _v('nome'),
        'email': _v('email'),
        'cpf': _v('cpf'),
        'telefone': _v('telefone'),
        'senha': _campos['senha']!.text,
        'confirmacao': _campos['confirmacao']!.text,
        'endereco': {
          'cep': _v('cep'),
          'logradouro': _v('logradouro'),
          'numero': _v('numero'),
          'complemento': _v('complemento'),
          'bairro': _v('bairro'),
          'cidade': _v('cidade'),
          'estado': _estado ?? '',
        },
      });
      // Sucesso: já entra logado. Fecha o cadastro; a raiz mostra a loja.
      if (mounted) Navigator.of(context).pop();
    } on ApiException catch (e) {
      if (!mounted) return;
      setState(() => _erros = e.campos);
      mostrarMensagem(context, e.mensagem, erro: true);
    } finally {
      if (mounted) setState(() => _enviando = false);
    }
  }

  Widget _campo(
    String chave,
    String rotulo, {
    TextInputType? teclado,
    bool senha = false,
    String? dica,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: TextField(
        controller: _campos[chave],
        keyboardType: teclado,
        obscureText: senha,
        autocorrect: false,
        textInputAction: TextInputAction.next,
        decoration: InputDecoration(labelText: rotulo, helperText: dica, errorText: _erros[chave]),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Criar conta')),
      body: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          const _Secao('Seus dados'),
          _campo('nome', 'Nome completo'),
          _campo('email', 'E-mail', teclado: TextInputType.emailAddress),
          _campo('cpf', 'CPF', teclado: TextInputType.number),
          _campo('telefone', 'Telefone com DDD', teclado: TextInputType.phone),
          _campo('senha', 'Senha', senha: true, dica: 'Mínimo de 8 caracteres, com letras e números'),
          _campo('confirmacao', 'Confirme a senha', senha: true),
          const SizedBox(height: 8),
          const _Secao('Endereço de entrega'),
          _campo('cep', 'CEP', teclado: TextInputType.number),
          _campo('logradouro', 'Rua / Avenida'),
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(child: _campo('numero', 'Número')),
              const SizedBox(width: 12),
              Expanded(flex: 2, child: _campo('complemento', 'Complemento')),
            ],
          ),
          _campo('bairro', 'Bairro'),
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(flex: 3, child: _campo('cidade', 'Cidade')),
              const SizedBox(width: 12),
              Expanded(
                flex: 2,
                child: DropdownButtonFormField<String>(
                  initialValue: _estado,
                  decoration: InputDecoration(labelText: 'UF', errorText: _erros['estado']),
                  items: _ufs.map((uf) => DropdownMenuItem(value: uf, child: Text(uf))).toList(),
                  onChanged: (uf) => setState(() => _estado = uf),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          FilledButton(
            onPressed: _enviando ? null : _cadastrar,
            child: _enviando
                ? const SizedBox.square(dimension: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Cores.branco))
                : const Text('CRIAR CONTA'),
          ),
        ],
      ),
    );
  }
}

class _Secao extends StatelessWidget {
  final String titulo;

  const _Secao(this.titulo);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: Text(titulo, style: const TextStyle(fontFamily: fonteTitulo, fontSize: 19)),
    );
  }
}
