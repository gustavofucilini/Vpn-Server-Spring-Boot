package com.br.firesa.vpn.exception;

// Classe para tratamento de erros que retorna o erro que aconteceu na API.

public enum ErroDaApi {

	BODY_INVALIDO("001"), FORMATO_INVALIDO("002"), CONDICAO_VIOLADA("003"), PARAMETRO_INVALIDO("004"),
	REGRA_VIOLADA("005"), PRECONDICAO_REQUERIDA("006"), TIPO_PARAMETRO_INVALIDO("007"),
	METODO_HTTP_NAO_SUPORTADO("008"), PARAMETRO_OBRIGATORIO("009"), REGISTRO_NAO_ENCONTRADO("010"),
	TOKEN_INVALIDO("011"), CONVERSAO_INVALIDA("012"), INTEGRACAO_INVALIDA("013"), ACESSO_NAO_PERMITIDO("014"),
	CREDENCIAIS_INVALIDAS("015"), ERRO_INTERNO_AUTENTICACAO("016"), TOKEN_EXPIRADO("017"),
	ERRO_TOKEN_DESCONHECIDO("018"), QUANTIDADE_MAX_USERS_ATINGIDO("019"), TAMANHO_MAX_UPLOAD_EXEDIDO("020"), FALHA_NA_VALIDACAO_DOS_DADOS("021"),
	USUARIO_JA_CADASTRADO("022"), SERIAL_NUMBER_JA_CADASTRADO("023"), ERRO_NAO_ENCONTRADO("999");

	private String codigo;

	private ErroDaApi(String codigo) {
		this.codigo = codigo;
	}

	public String getCodigo() {
		return codigo;
	}

}
