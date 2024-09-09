package com.br.firesa.vpn.exception;

import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.br.firesa.vpn.exception.errors.ImeiAndroidIdJaCadastradoException;
import com.br.firesa.vpn.exception.errors.QuantidadeMaxUsersException;
import com.br.firesa.vpn.exception.errors.RegistroNaoEncontradoException;
import com.br.firesa.vpn.exception.errors.SerialNumberAlreadyAssociatedException;
import com.br.firesa.vpn.exception.errors.ValidacaoException;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;

import jakarta.validation.ConstraintViolationException;

// Classe que implementa os erros de "ErroDaApi" trazendo mensagens para cada um dos erros.

@ControllerAdvice
@RestControllerAdvice
public class HandlerErrorDefault {

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Map<String, Object> handle(HttpMessageNotReadableException ex) {
		String rootCauseMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage()
				: "O corpo da requisição possui erros ou não existe";
		if (rootCauseMessage.contains("No enum constant")) {
			String[] parts = rootCauseMessage.split(" ");
			if (parts.length > 2) {
				String enumName = parts[parts.length - 1];
				String fieldName = extractFieldNameFromEnum(enumName);
				rootCauseMessage = "Valor inválido para o campo '" + fieldName;
			}
		}
		return criarMapDeErro(ErroDaApi.BODY_INVALIDO, "Erro ao processar o corpo da requisição: " + rootCauseMessage);
	}

	private String extractFieldNameFromEnum(String enumName) {
		String[] enumParts = enumName.split("\\.");
		return enumParts[enumParts.length - 1];
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(InvalidDefinitionException.class)
	public Map<String, Object> handle(InvalidDefinitionException ide) {
		String atributo = ide.getPath().get(ide.getPath().size() - 1).getFieldName();
		String msgDeErro = "O atributo '" + atributo + "' possui formato inválido.";
		if (ide.getCause() != null) {
			msgDeErro += " Detalhes: " + ide.getCause().getMessage();
		} else {
			msgDeErro += " Verifique o tipo de dado enviado na requisição.";
		}
		return criarMapDeErro(ErroDaApi.FORMATO_INVALIDO, msgDeErro);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ConstraintViolationException.class)
	public Map<String, Object> handle(ConstraintViolationException cve) {
		JSONArray errosArray = new JSONArray();

		cve.getConstraintViolations().forEach((error) -> {
			JSONObject erroDetalhe = new JSONObject();
			String[] paths = error.getPropertyPath().toString().split("\\.");
			String atributo = paths[paths.length - 1];

			erroDetalhe.put("atributo", atributo);
			erroDetalhe.put("mensagem", error.getMessage());
			erroDetalhe.put("valor inválido", error.getInvalidValue());
			erroDetalhe.put("codigo do erro", ErroDaApi.CONDICAO_VIOLADA.getCodigo());

			errosArray.put(erroDetalhe);
		});

		JSONObject body = new JSONObject();
		body.put("erros", errosArray);

		return body.toMap();
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(IllegalArgumentException.class)
	public Map<String, Object> handle(IllegalArgumentException ie) {
		return criarMapDeErro(ErroDaApi.PARAMETRO_INVALIDO, ie.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MissingPathVariableException.class)
	public Map<String, Object> handle(MissingPathVariableException mpve) {
		return criarMapDeErro(ErroDaApi.PRECONDICAO_REQUERIDA, mpve.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public Map<String, Object> handle(MethodArgumentTypeMismatchException matme) {
		return criarMapDeErro(ErroDaApi.TIPO_PARAMETRO_INVALIDO, matme.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public Map<String, Object> handle(HttpRequestMethodNotSupportedException hrmnse) {
		return criarMapDeErro(ErroDaApi.METODO_HTTP_NAO_SUPORTADO, hrmnse.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public Map<String, Object> handle(MissingServletRequestParameterException mrpe) {
		return criarMapDeErro(ErroDaApi.PARAMETRO_OBRIGATORIO, mrpe.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(RegistroNaoEncontradoException.class)
	public Map<String, Object> handle(RegistroNaoEncontradoException rnee) {
		return criarMapDeErro(ErroDaApi.REGISTRO_NAO_ENCONTRADO, rnee.getMessage());
	}

	@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public Map<String, Object> handle(MaxUploadSizeExceededException musee) {
		return criarMapDeErro(ErroDaApi.TAMANHO_MAX_UPLOAD_EXEDIDO, "Tamanho Maximo de Upload Exedido");
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ValidacaoException.class)
	public Map<String, Object> handleValidacaoException(ValidacaoException ex) {
		return criarMapDeErro(ErroDaApi.FALHA_NA_VALIDACAO_DOS_DADOS, ex.getMensagensDeErro());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(QuantidadeMaxUsersException.class)
	public Map<String, Object> handle(QuantidadeMaxUsersException qmue) {
		return criarMapDeErro(ErroDaApi.QUANTIDADE_MAX_USERS_ATINGIDO, qmue.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ImeiAndroidIdJaCadastradoException.class)
	public Map<String, Object> handle(ImeiAndroidIdJaCadastradoException iaijce) {
		return criarMapDeErro(ErroDaApi.USUARIO_JA_CADASTRADO, iaijce.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(SerialNumberAlreadyAssociatedException.class)
	public Map<String, Object> handle(SerialNumberAlreadyAssociatedException snaae) {
		return criarMapDeErro(ErroDaApi.SERIAL_NUMBER_JA_CADASTRADO, snaae.getMessage());
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(DataIntegrityViolationException.class)
	public Map<String, Object> handlePSQLExceptions(DataIntegrityViolationException dve) {
		return criarMapDeErro(ErroDaApi.PARAMETRO_INVALIDO,
				"Ocorreu um erro de integridade referencial na base de dados");
	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(BadCredentialsException.class)
	public Map<String, Object> handle(BadCredentialsException bce) {
		return criarMapDeErro(ErroDaApi.CREDENCIAIS_INVALIDAS, "Login ou senha inválidos");
	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(InternalAuthenticationServiceException.class)
	public Map<String, Object> handle(InternalAuthenticationServiceException iase) {
		return criarMapDeErro(ErroDaApi.ERRO_INTERNO_AUTENTICACAO, iase.getMessage());
	}

	private Map<String, Object> criarMapDeErro(ErroDaApi erroDaApi, String msgDeErro) {

		JSONObject body = new JSONObject();

		JSONObject detalhe = new JSONObject();
		detalhe.put("mensagem", msgDeErro);
		detalhe.put("codigo do erro", erroDaApi.getCodigo());

		JSONArray detalhes = new JSONArray();
		detalhes.put(detalhe);

		body.put("erros", detalhes);

		return body.toMap();

	}

	private Map<String, Object> criarMapDeErro(ErroDaApi erroDaApi, Set<String> msgDeErro) {

		JSONObject body = new JSONObject();

		JSONObject detalhe = new JSONObject();
		detalhe.put("mensagems", msgDeErro);
		detalhe.put("codigo do erro", erroDaApi.getCodigo());

		JSONArray detalhes = new JSONArray();
		detalhes.put(detalhe);

		body.put("erros", detalhes);

		return body.toMap();

	}

}
