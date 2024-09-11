package ${configuration.servicePackage};

import java.util.List;
import ${configuration.domainPackage}.${javaName};
import ${configuration.voPackage}.${javaName}Vo;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import com.efeichong.common.PageData;

/**
* @author ${configuration.author}
* @date ${configuration.dateTime}
* @description ${tableComment}
*/
public interface ${javaName}Service {
    /**
     * 通过id查询
     * 
     * @param ${pkColumnName} 主键
     * @return
     */
    public ${javaName}Vo getInfo(${pkColumnType} ${pkColumnName});

    /**
    * 分页查询
    *
    * @param ${instanceName}Vo
    * @return
    */
    public PageData<${javaName}Vo> list(${javaName}Vo ${instanceName}Vo);

    /**
     * 新增
     * 
     * @param ${instanceName}Vo
     * @return
     */
    public void insert(${javaName}Vo ${instanceName}Vo);

    /**
     * 修改
     * 
     * @param ${instanceName}Vo
     * @return
     */
    public void update(${javaName}Vo ${instanceName}Vo);

    /**
     * 批量删除
     * 
     * @param${pkColumnName}s
     * @return
     */
    public void delete(List<${pkColumnType}> ${pkColumnName}s);

<#if hasExcel>
    /**
    * 导入
    *
    * @param inputStream
    * @return
    */
    public void importExcel(InputStream inputStream);

    /**
    * 导出
    *
    * @param ${instanceName}Vo
    * @return
    */
    public void exportExcel(${javaName}Vo ${instanceName}Vo,HttpServletResponse response);
</#if >
}
