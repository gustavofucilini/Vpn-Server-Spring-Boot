package com.br.firesa.vpn.controller.converter.enums;

import java.util.HashMap;
import java.util.Map;

public enum EnumMenssageResponse {

//	Enum que demarca o tipo de inserção JSon, útil tanto para debug quanto para
//	passar  a mensagem de sucesso ou erro para o front.
	
	INSERIR("Inserido Com Sucesso"),
	ALTERAR("Alterado com Sucesso"),
	DELETAR("Deletado com Sucesso");

	private final String description;
    private static Map<String, String> enumMap;

    private EnumMenssageResponse(String description) {
        this.description = description;
    }

    public String getEnumValue() {
        return description;
    }

    public static String getEnumKey(String name) {
        if (enumMap == null) {
            initializeMap();
        }
        return enumMap.get(name);
    }

    private static Map<String, String> initializeMap() {
        enumMap = new HashMap<String, String>();
        for (EnumMenssageResponse access : EnumMenssageResponse.values()) {
            enumMap.put(access.getEnumValue(), access.toString());
        }
        return enumMap;
    }

}