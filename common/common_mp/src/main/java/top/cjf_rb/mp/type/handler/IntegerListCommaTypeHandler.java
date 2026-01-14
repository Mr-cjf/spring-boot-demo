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
import java.util.stream.Stream;

/**
 逗号分隔的字符串转List&lt;Integer&gt;

 @author cjf
 @since 1.0 */
@MappedTypes(List.class)
@MappedJdbcTypes({JdbcType.CHAR, JdbcType.VARCHAR})
public class IntegerListCommaTypeHandler extends BaseTypeHandler<List<Integer>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Integer> parameter,
                                    JdbcType jdbcType) throws SQLException {
        List<String> list = parameter.stream()
                                     .map(Object::toString)
                                     .toList();
        ps.setString(i, String.join(",", list));
    }

    @Override
    public List<Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String string = rs.getString(columnName);
        return this.analysis(string);
    }

    @Override
    public List<Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String string = rs.getString(columnIndex);
        return this.analysis(string);
    }

    @Override
    public List<Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String string = cs.getString(columnIndex);
        return this.analysis(string);
    }

    private List<Integer> analysis(String value) {
        return Nones.isBlank(value) ? Collections.emptyList() : Stream.of(value.split(","))
                                                                      .map(Integer::valueOf)
                                                                      .toList();
    }
}
