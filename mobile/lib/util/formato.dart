/// Formatações para a tela (sem bibliotecas extras).
class Formato {
  Formato._();

  /// 1249.9 -> "R$ 1.249,90"
  static String dinheiro(num valor) {
    final partes = valor.toStringAsFixed(2).split('.');
    final inteiro = partes[0].replaceAllMapped(
      RegExp(r'(\d)(?=(\d{3})+$)'),
      (m) => '${m[1]}.',
    );
    return 'R\$ $inteiro,${partes[1]}';
  }

  /// 2026-11-18T20:30 -> "18/11/2026 às 20:30"
  static String dataHora(DateTime d) {
    String dois(int n) => n.toString().padLeft(2, '0');
    return '${dois(d.day)}/${dois(d.month)}/${d.year} às ${dois(d.hour)}:${dois(d.minute)}';
  }

  /// "52998224725" -> "529.982.247-25"
  static String cpf(String digitos) {
    final d = digitos.replaceAll(RegExp(r'\D'), '');
    if (d.length != 11) return digitos;
    return '${d.substring(0, 3)}.${d.substring(3, 6)}.${d.substring(6, 9)}-${d.substring(9)}';
  }

  /// "11988887777" -> "(11) 98888-7777"
  static String telefone(String? digitos) {
    if (digitos == null) return '';
    final d = digitos.replaceAll(RegExp(r'\D'), '');
    if (d.length == 11) return '(${d.substring(0, 2)}) ${d.substring(2, 7)}-${d.substring(7)}';
    if (d.length == 10) return '(${d.substring(0, 2)}) ${d.substring(2, 6)}-${d.substring(6)}';
    return digitos;
  }
}
