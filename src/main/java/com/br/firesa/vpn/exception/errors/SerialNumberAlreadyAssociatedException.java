package com.br.firesa.vpn.exception.errors;

public class SerialNumberAlreadyAssociatedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SerialNumberAlreadyAssociatedException(String mensagem) {
		super(mensagem);
	}
	
}
