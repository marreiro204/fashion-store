import 'package:material_ui/material_ui.dart';

/// Cores da marca: as MESMAS do site (assets/css/base.css).
class Cores {
  Cores._();

  static const preto = Color(0xFF111111);
  static const grafite = Color(0xFF2A2A2A);
  static const cinza700 = Color(0xFF4D4D4D);
  static const cinza500 = Color(0xFF7D7A75);
  static const cinza300 = Color(0xFFCFCBC4);
  static const cinza100 = Color(0xFFEEECE8);
  static const fundo = Color(0xFFF7F6F3);
  static const branco = Color(0xFFFFFFFF);
  static const sucesso = Color(0xFF2F6B45);
  static const sucessoFundo = Color(0xFFE7F0E9);
  static const aviso = Color(0xFF85601A);
  static const avisoFundo = Color(0xFFF5EDDC);
  static const erro = Color(0xFFA3332B);
  static const erroFundo = Color(0xFFF7E8E6);
  static const info = Color(0xFF3C5A7A);
  static const infoFundo = Color(0xFFE8EDF2);
}

/// Fonte com serifa nos títulos, como a "Playfair Display" do site.
const fonteTitulo = 'serif';

ThemeData temaFashionStore() {
  final esquema = ColorScheme.fromSeed(
    seedColor: Cores.preto,
    primary: Cores.preto,
    onPrimary: Cores.branco,
    secondary: Cores.cinza700,
    surface: Cores.fundo,
    error: Cores.erro,
  );
  final borda = OutlineInputBorder(
    borderRadius: BorderRadius.circular(6),
    borderSide: const BorderSide(color: Cores.cinza300),
  );

  return ThemeData(
    colorScheme: esquema,
    scaffoldBackgroundColor: Cores.fundo,
    appBarTheme: const AppBarTheme(
      backgroundColor: Cores.branco,
      foregroundColor: Cores.preto,
      elevation: 0,
      scrolledUnderElevation: 0.5,
      centerTitle: true,
      titleTextStyle: TextStyle(
        fontFamily: fonteTitulo,
        fontSize: 20,
        color: Cores.preto,
        fontWeight: FontWeight.w600,
      ),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        backgroundColor: Cores.preto,
        foregroundColor: Cores.branco,
        minimumSize: const Size.fromHeight(50),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(6)),
        textStyle: const TextStyle(fontSize: 15, fontWeight: FontWeight.w600, letterSpacing: .5),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: Cores.preto,
        minimumSize: const Size.fromHeight(50),
        side: const BorderSide(color: Cores.preto),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(6)),
      ),
    ),
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: Cores.branco,
      border: borda,
      enabledBorder: borda,
      focusedBorder: borda.copyWith(borderSide: const BorderSide(color: Cores.preto, width: 1.5)),
      contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
    ),
    navigationBarTheme: const NavigationBarThemeData(
      backgroundColor: Cores.branco,
      indicatorColor: Cores.cinza100,
    ),
    snackBarTheme: const SnackBarThemeData(behavior: SnackBarBehavior.floating),
  );
}
