package com.br.firesa.vpn.exception.errors;

import java.util.Set;

public class ValidacaoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

    private final Set<String> mensagensDeErro;

    public ValidacaoException(Set<String> mensagensDeErro) {
        super("Erro(s) de validação encontrados.");
        this.mensagensDeErro = mensagensDeErro;
    }

    public Set<String> getMensagensDeErro() {
        return mensagensDeErro;
    }

    @Override
    public String getMessage() {
        return String.join("\n", mensagensDeErro);
    }
}