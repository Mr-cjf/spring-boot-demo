package top.cjf_rb.mp.type.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import top.cjf_rb.core.util.Nones;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 逗号分隔的字符串转List&lt;String&gt;

 @author cjf
 @since 1.0 */
@MappedTypes(List.class)
@MappedJdbcTypes({JdbcType.CHAR, JdbcType.VARCHAR})
public class StringListCommaTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter,
                                    JdbcType jdbcType) throws SQLException {
        ps.setString(i, String.join(",", parameter));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String string = rs.getString(columnName);
        return this.analysis(string);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String string = rs.getString(columnIndex);
        return this.analysis(string);
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String string = cs.getString(columnIndex);
        return this.analysis(string);
    }

    private List<String> analysis(String value) {
        return Nones.isBlank(value) ? Collections.emptyList() : List.of(value.split(","));
    }
}
