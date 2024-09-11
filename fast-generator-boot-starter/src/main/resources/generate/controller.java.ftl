package ${configuration.controllerPackage};

import java.util.List;
import org.springframework.web.bind.annotation.*;
import ${configuration.voPackage}.${javaName}Vo;
import ${configuration.servicePackage}.${javaName}Service;
<#if hasSwagger>
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
</#if>
<#if hasExcel>
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import com.alibaba.excel.EasyExcel;
</#if>
import ${responsePkg};
import com.efeichong.common.PageData;
<#if hasLombok>
import lombok.RequiredArgsConstructor;
<#else >
import org.springframework.beans.factory.annotation.Autowired;
</#if>

/**
* @author ${configuration.author}
* @date ${configuration.dateTime}
* @description ${tableComment}
*/
<#if hasSwagger>
@Api(tags = "<#if tableComment??>${tableComment}<#else>${instanceName}</#if>接口", value = "${tableComment}接口")
</#if>
@RestController
@RequestMapping("/${instanceName}")
<#if hasLombok>
@RequiredArgsConstructor
</#if>
public class ${javaName}Controller {

<#if hasLombok>
    private final ${javaName}Service ${instanceName}Service;
<#else >
    @Autowired
    private ${javaName}Service ${instanceName}Service;
</#if>

    <#if hasSwagger>
    @ApiOperation(value = "${tableComment}详细")
    </#if>
    @GetMapping(value = "/{id}")
    public ${responseName}<${javaName}Vo> getInfo(@PathVariable("${pkColumnName}") ${pkColumnType} ${pkColumnName}) {
        return ${responseName}.success(${instanceName}Service.getInfo(${pkColumnName}));
    }

    <#if hasSwagger>
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageIndex", value = "第几页开始", paramType = "query", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "pageSize", value = "每页行数", paramType = "query", dataTypeClass = Integer.class)
    })
    @ApiOperation(value = "${tableComment}列表查询")
    </#if>
    @GetMapping("/list")
    public ${responseName}<PageData<${javaName}Vo>> list(${javaName}Vo ${instanceName}Vo) {
        return ${responseName}.success(${instanceName}Service.list(${instanceName}Vo));
    }

    <#if hasSwagger>
    @ApiOperation(value = "${tableComment}新增")
    </#if>
    @PostMapping
    public ${responseName} insert(@RequestBody ${javaName}Vo ${instanceName}Vo) {
        ${instanceName}Service.insert(${instanceName}Vo);
        return ${responseName}.success();
    }

    <#if hasSwagger>
    @ApiOperation(value = "${tableComment}编辑")
    </#if>
    @PutMapping
    public ${responseName} update(@RequestBody ${javaName}Vo ${instanceName}Vo) {
        ${instanceName}Service.update(${instanceName}Vo);
        return ${responseName}.success();
    }

    <#if hasSwagger>
    @ApiOperation(value = "${tableComment}删除")
    </#if>
    @DeleteMapping("/{ids}")
    public ${responseName} delete(@PathVariable("${pkColumnName}s") List<${pkColumnType}> ${pkColumnName}s) {
        ${instanceName}Service.delete(${pkColumnName}s);
        return ${responseName}.success();
    }

<#if hasExcel>
    @SneakyThrows
    <#if hasSwagger>
    @ApiOperation(value = "${tableComment}导入", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    </#if>
    @PostMapping(value = "/importExcel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ${responseName} importExcel(@RequestPart("file") MultipartFile file) {
        ${instanceName}Service.importExcel(file.getInputStream());
        return ${responseName}.success();
    }

    <#if hasSwagger>
    @ApiOperation(value = "${tableComment}导出", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    </#if>
    @GetMapping(value = "/exportExcel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportExcel(${javaName}Vo ${instanceName}Vo,
                                    HttpServletResponse response) {
        ${instanceName}Service.exportExcel(${instanceName}Vo,response);
    }

    <#if hasSwagger>
    @ApiOperation(value = "${tableComment}模板下载", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    </#if>
    @SneakyThrows
    @GetMapping(value = "/downloadModel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadModel(HttpServletResponse response) {
        EasyExcel.write().head(${javaName}Vo.class).file(response.getOutputStream()).sheet().doWrite();
    }
</#if >
}
