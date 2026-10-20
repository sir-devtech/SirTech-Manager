package br.com.sirtech.manager.acesso.dto;

import br.com.sirtech.manager.acesso.Usuario;

/** O que a tela precisa saber de quem entrou. Nunca leva o hash da senha. */
public record UsuarioLogado(String nome, String email) {

    public static UsuarioLogado de(Usuario usuario) {
        return new UsuarioLogado(usuario.getNome(), usuario.getEmail());
    }
}
