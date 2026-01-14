package top.cjf_rb.mp.type.handler;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.io.IOException;

/**
 Long[] 的JSON转换

 @author cjf */
@Slf4j
@MappedTypes({Long[].class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class LongArrayJsonTypeHandler extends AbstractJsonTypeHandler<Long[]> {
    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final TypeReference<Long[]> typeReference = new TypeReference<>() {
    };

    public LongArrayJsonTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public Long[] parse(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toJson(Long[] obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
