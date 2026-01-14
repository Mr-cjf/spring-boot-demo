package top.cjf_rb.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.commons.collections4.ListUtils;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;

/**
 @author cjf
 @since 1.0 */
public interface AppBasicMapper<T> extends BaseMapper<T> {

    int BATCH_EXECUTE_SIZE = 1024;

    /**
     新增数据
     <p>
     存在则忽略

     @return 改变的行数
     */
    int insertIgnore(T t);

    /**
     批量插入数据

     @param entities 插入的新数据
     @return 操作条数
     */
    int insertBatchSomeColumn(Collection<T> entities);

    /**
     批量分片插入数据, 默认最大数据量为1024

     @param entities 插入的新数据
     @return 操作条数
     */
    default int insertPartition(List<T> entities) {
        return this.insertPartition(entities, this.getBatchExecuteSize());
    }

    /**
     批量分片插入数据

     @param entities  插入的新数据
     @param batchSize 批量插入最大量
     @return 操作条数
     */
    default int insertPartition(@NonNull List<T> entities, int batchSize) {
        if (entities.isEmpty()) {
            return 0;
        }

        if (entities.size() <= this.getBatchExecuteSize()) {
            return this.insertBatchSomeColumn(entities);
        }

        List<List<T>> partition = ListUtils.partition(entities, batchSize);
        return partition.stream()
                        .mapToInt(this::insertBatchSomeColumn)
                        .sum();
    }

    /**
     批量更新

     @param entities 需要更新的数据
     @return 操作条数
     */
    int updateBatchSomeColumn(Collection<T> entities);

    /**
     分段批量更新

     @param entities 需要更新的数据
     @return 操作条数
     */
    default int updateBatchPartition(List<T> entities) {
        return updateBatchPartition(entities, this.getBatchExecuteSize());
    }

    /**
     分段批量更新

     @param entities  需要更新的数据
     @param batchSize 批量插入最大量
     @return 操作条数
     */
    default int updateBatchPartition(List<T> entities, int batchSize) {
        if (entities.isEmpty()) {
            return 0;
        }

        if (entities.size() <= this.getBatchExecuteSize()) {
            return this.updateBatchSomeColumn(entities);
        }

        List<List<T>> partition = ListUtils.partition(entities, batchSize);
        return partition.stream()
                        .mapToInt(this::updateBatchSomeColumn)
                        .sum();
    }

    /**
     获取分片插入数据的最大数据量

     @return 分片插入数据的最大量
     */
    default int getBatchExecuteSize() {
        return BATCH_EXECUTE_SIZE;
    }

}
