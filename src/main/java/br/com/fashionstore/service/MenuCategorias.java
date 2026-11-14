package br.com.fashionstore.service;

import java.util.Collections;
import java.util.List;

import br.com.fashionstore.dao.CategoriaDAO;
import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Categoria;

/**
 * Categorias exibidas no menu do cabeçalho de TODAS as páginas.
 *
 * Para não consultar o banco a cada página aberta, a lista fica guardada
 * em memória por 60 segundos (cache). Quando o admin cadastra, altera ou
 * exclui uma categoria, o CategoriaService chama limpar() e o menu é
 * atualizado na hora.
 */
public final class MenuCategorias {

    private static final long VALIDADE_MS = 60_000;

    private static volatile List<Categoria> categorias = Collections.emptyList();
    private static volatile long carregadoEm = 0;

    private MenuCategorias() {
    }

    /** Categorias para o menu. Se o banco falhar, devolve lista vazia (o menu só não mostra categorias). */
    public static List<Categoria> listar() {
        long agora = System.currentTimeMillis();
        if (agora - carregadoEm > VALIDADE_MS) {
            synchronized (MenuCategorias.class) {
                if (agora - carregadoEm > VALIDADE_MS) {
                    try {
                        categorias = Collections.unmodifiableList(new CategoriaDAO().listarTodas());
                    } catch (DAOException e) {
                        categorias = Collections.emptyList(); // tenta de novo em 60 s
                    }
                    carregadoEm = agora;
                }
            }
        }
        return categorias;
    }

    /** Força recarregar na próxima página (chamado ao alterar categorias). */
    public static void limpar() {
        carregadoEm = 0;
    }
}
