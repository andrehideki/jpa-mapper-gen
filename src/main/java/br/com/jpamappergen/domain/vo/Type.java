package br.com.jpamappergen.domain.vo;

import java.util.stream.Stream;

import br.com.jpamappergen.domain.errors.type.TypeUnsuportedException;
import lombok.Getter;

@Getter
public enum Type {
	VARCHAR,
	INT,
	INTEGER,
	BOOLEAN,
	TINYINT,
	SMALLINT,
	BIGINT,
	IDENTITY,
	DECIMAL,
	DOUBLE,
	REAL,
	TIME,
	DATE,
	TIMESTAMP,
	CHAR;
	
	
	public static Type getType(String value) {
		return Stream.of(Type.values())
				.filter(type -> type.toString().equals(value))
				.findFirst()
				.orElseThrow(() -> new TypeUnsuportedException(value));
	}
}
