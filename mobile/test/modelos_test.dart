import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:fashionstore_app/controllers/sessao_controller.dart';
import 'package:fashionstore_app/models/pedido.dart';
import 'package:fashionstore_app/models/produto.dart';
import 'package:fashionstore_app/util/formato.dart';

/// Testes sem servidor: o JSON abaixo é uma resposta REAL de GET /api/produtos/1.
void main() {
  const produtoJson = '''
{"idProduto":1,"nome":"Blusa Bege","descricao":"Blusa feminina em tecido leve, cor bege.","preco":139.90,
 "categoria":{"idCategoria":3,"nome":"Blusas","descricao":"Blusas e camisas femininas"},
 "imagens":["http://localhost:8080/FashionStore/assets/images/produtos/feminino/blusa-bege.jpg",
            "http://localhost:8080/FashionStore/assets/images/produtos/feminino/blusa-bege-2.jpg"],
 "tamanhos":[{"tamanho":"P","quantidade":0,"ultimas":false},{"tamanho":"M","quantidade":15,"ultimas":false}],
 "esgotado":false}''';

  test('Produto lê o JSON da API', () {
    final p = Produto.fromJson(jsonDecode(produtoJson) as Map<String, dynamic>);
    expect(p.id, 1);
    expect(p.nome, 'Blusa Bege');
    expect(p.preco, 139.90);
    expect(p.categoria!.nome, 'Blusas');
    expect(p.imagens.length, 2);
    expect(p.imagemPrincipal, endsWith('blusa-bege.jpg'));
    expect(p.tamanhos.first.disponivel, isFalse);
    expect(p.tamanhos.last.quantidade, 15);
    expect(p.esgotado, isFalse);
  });

  test('Pedido lê o JSON da API', () {
    final pedido = Pedido.fromJson(jsonDecode('''
{"idPedido":12,"dataPedido":"2026-11-18T20:30","status":"PENDENTE","statusDescricao":"Pendente","valorTotal":499.80,
 "endereco":{"idEndereco":10,"cep":"01000000","logradouro":"Rua das Flores","numero":"100","complemento":null,
             "bairro":"Centro","cidade":"São Paulo","estado":"SP","enderecoFormatado":"Rua das Flores, 100"},
 "itens":[{"produto":{"idProduto":1,"nome":"Vestido","imagem":null},"tamanho":"M","quantidade":2,
           "precoUnitario":249.90,"subtotal":499.80}]}''') as Map<String, dynamic>);
    expect(pedido.id, 12);
    expect(pedido.data, DateTime(2026, 11, 18, 20, 30));
    expect(pedido.endereco!.cidade, 'São Paulo');
    expect(pedido.itens.single.subtotal, 499.80);
  });

  test('Formatações', () {
    expect(Formato.dinheiro(139.9), 'R\$ 139,90');
    expect(Formato.dinheiro(1249.5), 'R\$ 1.249,50');
    expect(Formato.cpf('52998224725'), '529.982.247-25');
    expect(Formato.telefone('11988887777'), '(11) 98888-7777');
    expect(Formato.dataHora(DateTime(2026, 1, 5, 9, 7)), '05/01/2026 às 09:07');
  });

  test('Endereço do servidor aceita só o IP', () {
    expect(SessaoController.normalizarServidor('192.168.1.109'), 'http://192.168.1.109:8080/FashionStore');
    expect(SessaoController.normalizarServidor(' http://10.0.2.2:8080/FashionStore/ '),
        'http://10.0.2.2:8080/FashionStore');
    expect(SessaoController.normalizarServidor('192.168.137.1:9090'), 'http://192.168.137.1:9090/FashionStore');
  });
}
