package br.com.mapper_gen.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import br.com.mapper_gen.dto.DatabaseObject;


@Service
public class DatabaseService {
    
    private JdbcTemplate jdbc;

    @Value("${database_schema}")
    private String schema;
    
    public DatabaseService(
        JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    public boolean exists(String name) {
        return !jdbc.query("""
           SELECT 1 FROM all_objects 
           WHERE owner = '{owner}' AND 
           object_name like '%{name}%'      
        """
        .replace("{owner}", schema)
        .replace("{name}", name.toUpperCase())
        , (rs, row) -> rs.getInt(1))
        .isEmpty();
    }

    public String getType(String name) {
        var result = jdbc.query("""
           SELECT object_type FROM all_objects 
           WHERE owner = '{owner}' AND 
           object_name like '%{name}%'      
        """
        .replace("{owner}", schema)
        .replace("{name}", name.toUpperCase())
        , (rs, row) -> rs.getString("object_type"));
        
        if (result.isEmpty()) throw new RuntimeException("Failed to find: " + name);

        return result.get(0);
    }

    public List<String> getTables(String name) {
        return jdbc.query("""
            SELECT table_name, owner
            FROM all_tables  
            WHERE owner = '{owner}' AND 
            table_name like '%{name}%'
            ORDER BY table_name
        """
        .replace("{owner}", schema)
        .replace("{name}", name.toUpperCase())
        , (rs, row) -> rs.getString("table_name"));
    }

    public List<String> getViews(String name) {
        return jdbc.query("""
            SELECT view_name, owner
            FROM all_views  
            WHERE owner = '{owner}' AND 
            view_name like '%{view_name}%'
            ORDER BY view_name
        """
        .replace("{owner}", schema)
        .replace("{view_name}", name.toUpperCase())
        , (rs, row) -> rs.getString("view_name"));
    }

    public List<String> getProcedures(String name) {
        return jdbc.query("""
            SELECT OBJECT_NAME, owner
            FROM all_objects   
            WHERE owner = '{owner}' AND 
            object_name like '%{object_name}%'
            AND object_type IN ('PROCEDURE', 'FUNCTION')
            ORDER BY OBJECT_NAME
        """
        .replace("{owner}", schema)
        .replace("{object_name}", name.toUpperCase())
        , (rs, row) -> rs.getString("object_name"));
    }

    public DatabaseObject getDatabaseObject(String name) {
        if (!exists(name)) throw new RuntimeException("Failed to find: " + name.toUpperCase());
        String type = getType(name);
        if (Arrays.asList("VIEW", "TABLE").contains(type)) {
            String sql = """
                SELECT c.column_name,
                c.data_type,
                c.data_length,
                c.data_precision,
                c.nullable,
                CASE 
                    WHEN pk.constraint_type = 'P' THEN 'YES'
                    ELSE 'NO'
                END AS is_primary_key
                FROM all_tab_columns c
                LEFT JOIN (
                    SELECT cc.column_name, ac.table_name, ac.owner, ac.constraint_type
                    FROM all_constraints ac
                    INNER JOIN all_cons_columns cc
                    ON ac.constraint_name = cc.constraint_name AND ac.owner = cc.owner
                    WHERE ac.constraint_type = 'P'
                ) pk
                ON c.table_name = pk.table_name 
                AND c.owner = pk.owner 
                AND c.column_name = pk.column_name
                WHERE c.table_name = '{name}'
                AND c.owner = '{owner}'
                ORDER BY c.column_name
            """
            .replace("{owner}", schema)
            .replace("{name}", name.toUpperCase());
                        
            var attrs = jdbc.query(sql, (rs, row) -> {
                return new DatabaseObject.Attr(
                    rs.getString("column_name"),
                    rs.getString("data_type"),
                    rs.getInt("data_length"),
                    rs.getInt("data_precision"),
                    "Y".equals(rs.getString("nullable")),
                    "YES".equals(rs.getString("is_primary_key"))
                );
            });

            return new DatabaseObject(
                name.toUpperCase(),
                schema,
                type,
                attrs,
                new ArrayList<>()
            );
        }

        String sql = """
            SELECT ARGUMENT_NAME, DATA_TYPE, IN_OUT, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, SEQUENCE
            FROM all_arguments 
            WHERE 
            owner = '{owner}'
            AND object_name = '{name}'
            ORDER BY sequence
        """
        .replace("{owner}", schema)
        .replace("{name}", name.toUpperCase());

        var args = jdbc.query(sql, (rs, row) -> {
            return new DatabaseObject.Arg(
                rs.getString("ARGUMENT_NAME"),
                rs.getString("DATA_TYPE"),
                rs.getInt("data_length"),
                rs.getInt("data_precision"),
                "IN".equals(rs.getString("IN_OUT")),
                "OUT".equals(rs.getString("IN_OUT"))
            );
        });

        return new DatabaseObject(
            name.toUpperCase(),
            schema,
            type,
            new ArrayList<>(),
            args
        );
    }
}
