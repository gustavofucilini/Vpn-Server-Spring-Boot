package com.br.firesa.vpn.controller.converter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.br.firesa.vpn.controller.converter.enums.EnumMenssageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class MapConverter implements Serializable {

	private static final long serialVersionUID = 1L;

	@Autowired
	private ObjectMapper conversor;

	public List<Map<String, Object>> toJsonList(List<?> list, String... exclusoes) {
		List<Map<String, Object>> listagem = new ArrayList<>();

		for (Object obj : list) {
			listagem.add(toJsonMap(obj, exclusoes));
		}

		return listagem;
	}
	
	public List<Map<String, Object>> toJsonList(List<?> list, EnumMenssageResponse menssagemResponse, String... exclusoes) {
		List<Map<String, Object>> listagem = new ArrayList<>();

		for (Object obj : list) {
			listagem.add(toJsonMap(obj, menssagemResponse, exclusoes));
		}

		return listagem;
	}

	public Set<Map<String, Object>> toJsonSet(Set<?> list, String... exclusoes) {

		Set<Map<String, Object>> listagem = new HashSet<Map<String, Object>>();

		for (Object obj : list) {
			listagem.add(toJsonMap(obj, exclusoes));
		}

		return listagem;
	}
	
	public Map<String, Object> toJsonPage(Page<?> page, String... exclusoes) {
        Map<String, Object> pageMap = new LinkedHashMap<>();
        pageMap.put("content", toJsonList(page.getContent(), exclusoes));
        pageMap.put("totalPages", page.getTotalPages());
        pageMap.put("totalElements", page.getTotalElements());
        pageMap.put("size", page.getSize());
        pageMap.put("number", page.getNumber());
        pageMap.put("numberOfElements", page.getNumberOfElements());
        pageMap.put("first", page.isFirst());
        pageMap.put("last", page.isLast());

        return pageMap;
    }

	@Transactional
	@SuppressWarnings("unchecked")
	public Map<String, Object> toJsonMap(Object obj, String... exclusoes) {
		Map<String, Object> map = new LinkedHashMap<>();

		try {
			String json = conversor.writeValueAsString(obj);
			map = conversor.readValue(json, LinkedHashMap.class);
			removeEmptyAndNullFields(map, exclusoes);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> toJsonMap(Object obj, EnumMenssageResponse menssagemResponse, String... exclusoes) {
		Map<String, Object> map = new LinkedHashMap<>();

		try {
			String json = conversor.writeValueAsString(obj);
			map = conversor.readValue(json, LinkedHashMap.class);
			map.put("resposta", menssagemResponse.getEnumValue());
			removeEmptyAndNullFields(map, exclusoes);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return map;
	}
	
	@Transactional
	@SuppressWarnings("unchecked")
	public Map<String, Object> StringtoJsonMap(Object obj, String... exclusoes) {
	    Map<String, Object> map = new LinkedHashMap<>();

	    try {
	        if (obj instanceof String) {
	            String str = (String) obj;
	            if (isProbablyJson(str)) {
	                try {
	                    map = conversor.readValue(str, LinkedHashMap.class);
	                } catch (JsonProcessingException e) {
	                    map.put("response", str);
	                }
	            } else {
	                map.put("response", str);
	            }
	        } else {
	            String json = conversor.writeValueAsString(obj);
	            map = conversor.readValue(json, LinkedHashMap.class);
	        }
	        removeEmptyAndNullFields(map, exclusoes);
	    } catch (JsonProcessingException e) {
	        e.printStackTrace();
	    }

	    return map;
	}

	private boolean isProbablyJson(String str) {
	    str = str.trim();
	    return (str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"));
	}
	
	public String toJsonString(Object obj, String... exclusoes) {
	    Map<String, Object> map = StringtoJsonMap(obj, exclusoes);
	    try {
	        return conversor.writeValueAsString(map);
	    } catch (JsonProcessingException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	private void removeEmptyAndNullFields(Object object, String... exclusoes) {

	    if (object == null) {
	        return;
	    }

	    if (object instanceof Map) {
	        Map<?, ?> map = (Map<?, ?>) object;
	        map.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().equals(JSONObject.NULL));
	        map.forEach((key, value) -> {
	            if (value instanceof Collection || value instanceof Map) {
	                removeEmptyAndNullFields(value, exclusoes);
	            }
	        });
	    } else if (object instanceof List) {
	        List<?> list = (List<?>) object;
	        list.removeIf(Objects::isNull);
	        list.forEach(item -> {
	            if (item instanceof Collection || item instanceof Map) {
	                removeEmptyAndNullFields(item, exclusoes);
	            }
	        });
	    } else if (object instanceof JSONArray) {
	        JSONArray array = (JSONArray) object;
	        for (int i = 0; i < array.length(); ++i) {
	            removeEmptyAndNullFields(array.get(i), exclusoes);
	        }
	    } else if (object instanceof JSONObject) {
	        JSONObject json = (JSONObject) object;
	        Iterator<String> keys = json.keys();
	        while (keys.hasNext()) {
	            String key = keys.next();
	            Object value = json.get(key);
	            if (value == null || value.equals(JSONObject.NULL)) {
	                keys.remove();
	            } else if (value instanceof Collection || value instanceof Map) {
	                removeEmptyAndNullFields(value, exclusoes);
	            }
	        }
	    }
	}

	/*private boolean isLiberadoParaRemocao(String key, String... exclusoes) {
		for (String outraKey : exclusoes) {
			if (key.equals(outraKey)) {
				return true;
			}
		}
		return false;
	}*/

}
