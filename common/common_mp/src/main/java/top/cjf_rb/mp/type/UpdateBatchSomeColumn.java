package top.cjf_rb.mp.type;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;
import java.util.Objects;

/**
 MySQL 语法的批量更新
 */
public class UpdateBatchSomeColumn extends AbstractMethod {

    public UpdateBatchSomeColumn() {
        super("updateBatchSomeColumn");
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String sql = "<script>UPDATE %s %s\n WHERE\n %s IN %s</script>";
        sql = String.format(sql, tableInfo.getTableName(), sqlSet(tableInfo), tableInfo.getKeyColumn(),
                            sqlIn(tableInfo.getKeyProperty()));
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);

        return this.addUpdateMappedStatement(mapperClass, modelClass, methodName, sqlSource);

    }

    /**
     生成更新语句set部分
     */
    private String sqlSet(TableInfo tableInfo) {
        List<TableFieldInfo> fieldList = tableInfo.getFieldList();
        StringBuilder sb = new StringBuilder();
        for (TableFieldInfo fieldInfo : fieldList) {
            sb.append("<trim prefix=\"")
              .append(fieldInfo.getColumn())
              .append("=case ")
              .append(tableInfo.getKeyColumn())
              .append("\" suffix=\"end,\"> ")
              .append("<foreach collection=\"list\" item=\"item\" > ")
              .append("<if test=\"item.")
              .append(fieldInfo.getProperty())
              .append("!=null\"> ")
              .append("WHEN #{item.")
              .append(tableInfo.getKeyProperty())
              .append("} THEN #{item.")
              .append(fieldInfo.getProperty())
              .append(Objects.nonNull(fieldInfo.getTypeHandler()) ? "," + fieldInfo.getMapping() : "")
              .append("} ")
              .append("</if> ")
              .append("</foreach> ")
              .append("</trim>");
        }
        return "<set>\n" + sb + "</set>";
    }

    /**
     生成更新语句主键in部分
     */
    private String sqlIn(String keyProperty) {
        return "<foreach collection=\"list\" separator=\", \" item=\"item\" open=\"(\" close=\")\">\n" + "#{item." +
                keyProperty + "}\n" + "</foreach>\n";
    }

}
