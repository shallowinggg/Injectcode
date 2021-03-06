package com.wushunda.injectcode.generate.impl;

import com.wushunda.injectcode.entity.TableFile;
import com.wushunda.injectcode.enums.ColumnKey;
import com.wushunda.injectcode.generate.Generate;
import com.wushunda.injectcode.generate.crudMethod;

import static com.wushunda.injectcode.util.Util.firstCapitalLineToHump;
import static com.wushunda.injectcode.util.Util.lineToHump;

/**
 * @author wsd
 */
public class MapperGenerate implements Generate, crudMethod {

    private static final String COMMA = ",";
    private static final String AND = "AND";

    private EntityGenerate entityGenerate;

    private String primaryKey;

    /**
     * 默认dao存的包
     */
    private String daoPackageName = "dao";
    /**
     * 类名称
     */
    private String className;
    /**
     * 类变量名
     */
    private String file;


    public MapperGenerate(EntityGenerate entityGenerate) {
        this.entityGenerate = entityGenerate;
        initiPrimaryKey();
        this.className = firstCapitalLineToHump(entityGenerate.getTableName());
        this.file = lineToHump(entityGenerate.getTableName());
    }

    public MapperGenerate(EntityGenerate entityGenerate, String daoPackageName) {
        this.entityGenerate = entityGenerate;
        this.daoPackageName = daoPackageName;
        initiPrimaryKey();
        this.className = firstCapitalLineToHump(entityGenerate.getTableName());
        this.file = lineToHump(entityGenerate.getTableName());
    }

    @Override
    public String create() {
        return String.format("    <insert id=\"create%s\" useGeneratedKeys=\"true\" keyProperty=\"%s\">\n" +
                "        INSERT INTO %s (%s) VALUES(%s)\n" +
                "    </insert>", className, lineToHump(primaryKey), entityGenerate.getTableName(), getColumn(), getValues());
    }

    @Override
    public String retrieve() {
        return String.format("    <select id=\"get%sById\" resultType=\"%s.%s.%s\">\n" +
                        "        SELECT %s\n" +
                        "        FROM %s\n" +
                        "        WHERE %s = #{%s}\n" +
                        "    </select>", className, entityGenerate.getPackageName(), entityGenerate.getEntityPackageName(), className,
                getColumn(), entityGenerate.getTableName(), primaryKey, file);
    }

    @Override
    public String update() {
        return String.format("    <update id=\"update%sById\">\n" +
                        "        UPDATE %s SET %s\n" +
                        "        WHERE %s = #{%s}\n" +
                        "    </update>", className, entityGenerate.getTableName(),
                getUpdateColumn(), primaryKey, file + "." + lineToHump(primaryKey));
    }

    @Override
    public String deleteById() {
        return String.format("    <delete id=\"delete%sById\">\n" +
                        "        DELETE FROM %s\n" +
                        "        WHERE %s=#{%s}\n" +
                        "    </delete>", className,
                entityGenerate.getTableName(), primaryKey, file);
    }

    @Override
    public String delete() {
        return String.format("    <delete id=\"delete%s\">\n" +
                "        DELETE FROM %s\n" +
                "        WHERE %s\n" +
                "    </delete>", className, entityGenerate.getTableName(), getDeleteCondition());
    }

    @Override
    public String build() {
        StringBuilder str = new StringBuilder();
        String head = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "\n" +
                "<mapper namespace=\"%s.%s.%s\">", entityGenerate.getPackageName(), daoPackageName, className + "Dao");
        str.append(head).append("\n")
                .append(create()).append("\n")
                .append(retrieve()).append("\n")
                .append(update()).append("\n")
                .append(deleteById()).append("\n")
                .append(delete()).append("\n")
                .append("</mapper>");
        return str.toString();
    }

    @Override
    public String tableName() {
        return this.entityGenerate.tableName();
    }

    /**
     * 获取主键
     */
    private void initiPrimaryKey() {
        for (TableFile tableFile : entityGenerate.getTableFiles()) {
            if (ColumnKey.PRI.getKey().equals(tableFile.getColumnKey())) {
                primaryKey = tableFile.getColumnName();
                break;
            }
        }
    }

    /**
     * 插入查询的列名
     *
     * @return
     */
    private String getColumn() {
        StringBuilder str = new StringBuilder();
        for (TableFile tableFile : entityGenerate.getTableFiles()) {
            str.append(lineToHump(tableFile.getColumnName())).append(",");
        }
        if (str.length() > 0 && COMMA.equals(String.valueOf(str.charAt(str.length() - 1)))) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    /**
     * 更新时的列内容
     *
     * @return
     */
    private String getUpdateColumn() {
        StringBuilder str = new StringBuilder();
        for (TableFile tableFile : entityGenerate.getTableFiles()) {
            str.append(tableFile.getColumnName()).append("=")
                    .append("#{").append(file)
                    .append(".").append(lineToHump(tableFile.getColumnName())).append("}").append(",");
        }
        if (str.length() > 0 && COMMA.equals(String.valueOf(str.charAt(str.length() - 1)))) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    private String getValues() {
        StringBuilder str = new StringBuilder();
        for (TableFile tableFile : entityGenerate.getTableFiles()) {
            str.append("#{").append(file).append(".").append(lineToHump(tableFile.getColumnName())).append("}").append(",");
        }
        if (str.length() > 0 && COMMA.equals(String.valueOf(str.charAt(str.length() - 1)))) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    private String getDeleteCondition() {
        StringBuilder str = new StringBuilder();
        for (TableFile tableFile : entityGenerate.getTableFiles()) {
            str.append(" ").append(tableFile.getColumnName()).append(" = ")
                    .append("#{").append(file).append(".")
                    .append(lineToHump(tableFile.getColumnName())).append("}\n").append("               AND");
        }
        if (str.length() >= AND.length() && AND.equals(str.substring(str.length() - AND.length(), str.length()))) {
            str.delete(str.length() - AND.length(), str.length());
        }
        return str.toString();
    }
}
