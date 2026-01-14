package top.cjf_rb.mp.type;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;

public class InsertIgnore extends AbstractMethod {

    public InsertIgnore() {
        super("insertIgnore");
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String columnScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlColumnMaybeIf(null), LEFT_BRACKET,
                                                         RIGHT_BRACKET, null, COMMA);
        String valuesScript = LEFT_BRACKET + NEWLINE + SqlScriptUtils.convertTrim(
                tableInfo.getAllInsertSqlPropertyMaybeIf(null), null, null, null, COMMA) + NEWLINE + RIGHT_BRACKET;

        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
        String keyProperty = null;
        String keyColumn = null;
        // 表包含主键处理逻辑,如果不包含主键当普通字段处理
        if (tableInfo.havePK()) {
            if (tableInfo.getIdType() == IdType.AUTO) {
                /* 自增主键 */
                keyGenerator = Jdbc3KeyGenerator.INSTANCE;
                keyProperty = tableInfo.getKeyProperty();
                keyColumn = tableInfo.getKeyColumn();
            } else {
                if (tableInfo.getKeySequence() != null) {
                    keyGenerator = TableInfoHelper.genKeyGenerator(this.methodName, tableInfo, builderAssistant);
                    keyProperty = tableInfo.getKeyProperty();
                    keyColumn = tableInfo.getKeyColumn();
                }
            }
        }

        var sql = "<script>\nINSERT IGNORE INTO " + tableInfo.getTableName() + " " + columnScript + " VALUES " +
                valuesScript + "\n</script>";
        var sqlSource = createSqlSource(configuration, sql, mapperClass);

        return addInsertMappedStatement(mapperClass, modelClass, methodName, sqlSource, keyGenerator, keyProperty,
                                        keyColumn);
    }
}