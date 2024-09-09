package com.br.firesa.vpn.exception.errors;

public class QuantidadeMaxUsersException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public QuantidadeMaxUsersException(String mensagem) {
		super(mensagem);
	}

}
