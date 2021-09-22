package br.com.jpamappergen.domain.vo;

import java.util.stream.Stream;

import br.com.jpamappergen.domain.errors.TypeException;

public enum Type {
	VARCHAR,
	DECIMAL;
	
	public static Type getType(String value) {
		return Stream.of(Type.values())
				.filter(type -> type.toString().equals(value))
				.findFirst()
				.orElseThrow(() -> new TypeException(value));
	}
}