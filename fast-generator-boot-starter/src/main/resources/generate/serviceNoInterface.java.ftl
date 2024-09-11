package ${configuration.servicePackage};

import java.util.List;
import org.springframework.stereotype.Service;
import ${configuration.daoPackage}.${javaName}Dao;
import ${configuration.domainPackage}.${javaName};
import ${configuration.voPackage}.${javaName}Vo;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.efeichong.common.PageData;
import com.efeichong.util.TransformUtils;
import com.efeichong.jpa.JExample;
import ${exceptionPkg};
<#if hasExcel>
import lombok.SneakyThrows;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import java.util.ArrayList;
import com.efeichong.util.EntityUtils;
</#if>

/**
* @author ${configuration.author}
* @date ${configuration.dateTime}
* @description ${tableComment}
*/
@RequiredArgsConstructor
@Service
public class ${javaName}Service {

    private final ${javaName}Dao ${instanceName}Dao;

    /**
     * 通过id查询
     *
     * @param ${pkColumnName} 主键
     * @return
     */
    public ${javaName}Vo getInfo(${pkColumnType} ${pkColumnName}){
        return ${instanceName}Dao.selectById(${pkColumnName}, ${javaName}Vo.class).orElseThrow(() -> new ${exceptionName}("数据不存在！"));
    }

    /**
    * 分页查询
    *
    * @param ${instanceName}Vo
    * @return
    */
    public PageData<${javaName}Vo> list(${javaName}Vo ${instanceName}Vo){
        JExample<${javaName}> example = new JExample();
        example.initExample(${instanceName}Vo);
        return ${instanceName}Dao.selectByPage(example, ${javaName}Vo.class);
    }

    /**
     * 新增
     *
     * @param ${instanceName}Vo
     * @return
     */
    @Transactional
    public void insert(${javaName}Vo ${instanceName}Vo){
        ${instanceName}Dao.save(TransformUtils.toPo(${instanceName}Vo,${javaName}.class));
    }

    /**
     * 修改
     *
     * @param ${instanceName}Vo
     * @return
     */
    @Transactional
    public void update(${javaName}Vo ${instanceName}Vo){
        if(${instanceName}Vo.get${firstUpperPkColumnName}() == null){
            throw new ${exceptionName}("主键为必传参数");
        }
        ${instanceName}Dao.save(TransformUtils.toPo(${instanceName}Vo,${javaName}.class));
    }

    /**
     * 批量删除
     *
     * @param ${pkColumnName}s
     * @return
     */
    @Transactional
    public void delete(List<${pkColumnType}> ${pkColumnName}s){
        ${instanceName}Dao.deleteAllByIds(${pkColumnName}s);
    }

<#if hasExcel>

    /**
    * 导入
    *
    * @param inputStream
    * @return
    */
    @Transactional
    public void importExcel(InputStream inputStream){
        List<${javaName}> list = new ArrayList<>();
        int batchSize = 100;
        EasyExcel.read()
                .file(inputStream)
                .head(${javaName}Vo.class)
                .registerReadListener(new ReadListener<${javaName}Vo>() {
                        @Override
                        public void invoke(${javaName}Vo ${instanceName}Vo, AnalysisContext context) {
                            list.add(TransformUtils.toPo(${instanceName}Vo, ${javaName}.class));
                            if (list.size() >= batchSize){
                                ${instanceName}Dao.saveAll(list);
                                list.clear();
                            }
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            if (EntityUtils.isNotEmpty(list)){
                                ${instanceName}Dao.saveAll(list);
                                list.clear();
                            }
                        }
                    }).doReadAll();
    }

    /**
    * 导出
    *
    * @param ${instanceName}Vo
    * @return
    */
    @SneakyThrows
    public void exportExcel(${javaName}Vo ${instanceName}Vo,HttpServletResponse response){
        JExample<${javaName}> example = new JExample();
        example.initExample(${instanceName}Vo);
        List<${javaName}Vo> list = ${instanceName}Dao.selectAll(example,${javaName}Vo.class);
        EasyExcel.write()
                .file(response)
                .head(${javaName}Vo.class)
                .sheet()
                .doWrite(list);
    }
</#if >
}
