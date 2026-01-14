package top.cjf_rb.mp.type;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import org.apache.ibatis.session.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import top.cjf_rb.core.util.Actions;

import java.util.List;

/**
 扩展方法

 @author cjf */
public class ExtendedSqlInjector extends DefaultSqlInjector {
    @Autowired(required = false)
    private List<AbstractMethod> customMethods;

    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(configuration, mapperClass, tableInfo);
        // 仅MySQL有效
        methodList.add(new InsertBatchSomeColumn(t -> !t.isLogicDelete()));
        methodList.add(new UpdateBatchSomeColumn());
        methodList.add(new InsertIgnore());
        // 自定义扩展
        Actions.nonEmptyIfPresent(customMethods, methodList::addAll);
        return methodList;
    }

}
