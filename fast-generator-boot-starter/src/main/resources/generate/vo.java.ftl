package ${configuration.voPackage};
import lombok.Getter;
import lombok.Setter;
<#if hasSwagger>
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModel;
</#if>
<#if baseEntityPkg>
import ${baseEntityPkg};
</#if>
<#if hasExcel>
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.enums.BooleanEnum;
</#if>
<#list voFieldTypes as importclazz>
import ${importclazz};
</#list>

/**
* @author ${configuration.author}
* @date ${configuration.dateTime}
* @description ${tableComment}
*/
@Setter
@Getter
<#if hasExcel>
@ExcelIgnoreUnannotated
@ContentRowHeight(60)
@ContentFontStyle(italic = BooleanEnum.FALSE,fontName = "Arial")
@HeadRowHeight(40)
@ContentStyle(locked=BooleanEnum.TRUE,wrapped=BooleanEnum.TRUE)
</#if>
<#if hasSwagger>
@ApiModel("${tableComment}")
</#if>
public class ${javaName}Vo <#if baseEntityName?? >extends ${baseEntityName}</#if>{
<#list tableColumns as column>
    <#if column.hasNeedSerialize==1>
    <#if hasExcel>
    @ExcelProperty(value = "${column.columnComment}")
    </#if>
    <#if hasSwagger>
    @ApiModelProperty(value = "${column.columnComment}")
    <#else>
    /**${column.columnComment}**/
    </#if>
    private ${column.fieldType} ${column.fieldName};
    </#if>

</#list>
}
