package ${configuration.domainPackage};
import javax.persistence.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
<#if hasLombok>
import lombok.Getter;
import lombok.Setter;
</#if>
<#if baseEntityPkg>
import ${baseEntityPkg};
</#if>
<#list importList as importclazz>
import ${importclazz};
</#list>
import com.efeichong.audit.InsertDefault;
/**
* @author ${configuration.author}
* @date ${configuration.dateTime}
* @description ${tableComment}
*/
@org.hibernate.annotations.Table(appliesTo = "`${tableName}`", comment = "${tableComment}")
<#if hasLombok>
@Setter
@Getter
</#if>
@Entity(name = "`${tableName}`")
<#if tableIndex??>
${tableIndex}
</#if>
<#if logicDelete??>
${logicDelete}
</#if>
@DynamicInsert
@DynamicUpdate
public class ${javaName} <#if baseEntityName?? >extends ${baseEntityName}</#if>{
<#list tableColumns as column>
    <#if column.columnComment?length gt 0>
    /**${column.columnComment}**/
    </#if>
    <#if column.joinTable??>
    ${column.joinTable}
    </#if>
    <#if column.mappedType??>
    ${column.mappedType}
    </#if>
    <#if column.columnAnn>
    ${column.columnAnn}
    </#if>
    <#if column.hasPrimaryKey==1>
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    <#if column.columnDefault?length gt 0>
    @InsertDefault("${column.columnDefault}")
    </#if>
    private ${column.fieldType} ${column.fieldName};

</#list>

<#if !hasLombok>
<#list tableColumns as column>
    public ${column.fieldType} get${column.firstUpperFieldName}(){
    return ${column.fieldName};
    }

    public void set${column.firstUpperFieldName}(${column.fieldType} ${column.fieldName}){
    this.${column.fieldName} = ${column.fieldName};
    }

</#list>
</#if>


}
