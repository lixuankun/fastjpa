<template>
    <div class="app-container">
        <SearchForm :formOptions="formOptions" @onSearch="onSearch" @onReset='onReset'/>
        <DefaultButton :url='url' :single='single' :multiple='multiple' :handleAdd='handleAdd' :handleDelete='handleDelete'
                       :downloadModel='downloadModel' :handleImport='handleImport'
                       :handleExport='handleExport'></DefaultButton>

        <free-table
                v-loading="loading"
                style="min-height: 50vh"
                border
                pagination
                :data="dataList"
                :column="tableColumns"
                @pagination="getList"
                @selection-change="handleSelectionChange"
                pagination
                :auto-scroll="false"
                :total="total"
                :page.sync="queryParams.pageIndex"
                :limit.sync="queryParams.pageSize"
        >
            <template v-slot:action="{ row }">
                <el-button v-hasPermi="[url.edit]" icon="el-icon-edit-outline" type="text" size="mini"
                           @click="handleCommonUpdate(row.id)">编辑
                </el-button>
                <el-button v-hasPermi="[url.deletes]" icon="el-icon-delete" type="text" size="mini"
                           @click="handleDelete(row.id)">删除
                </el-button>
            </template>
        </free-table>

        <!-- 添加或修改 -->
        <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
            <Form :config="formConfig" @submitForm="submitCommonForm" ref="commonForm"/>
        </el-dialog>
        <#if hasExcel>
        <el-dialog title="上传文件" :visible.sync="upload.open" width="500px" append-to-body>
            <Upload :url='url' :downloadModel='downloadModel' :accept='accept' :upload='upload' :getList='getList'></Upload>
        </el-dialog>
        </#if>
    </div>
</template>

<script>
    import DefaultButton from "@/components/DefaultButton"
    import SearchForm from '@/components/Search'
    import FreeTable from '@/components/Table/FreeTable'
    import Form from '@/components/Form'
    import Upload from "@/components/Upload"
    import {commonMixin,LikeType} from '@/mixins'

    export default {
        name: "${javaName}",
        mixins: [commonMixin],
        components: {
            DefaultButton,
            SearchForm,
            FreeTable,
            Form,
            Upload
        },
        data() {
            return {
                //查询条件 例如 {label: '名称', prop: 'name', element: 'el-input',like: LikeType.LEFT_AND_RIGHT},
                // like 指定模糊查询
                formOptions: [

                ],
                //排序 例如: sortOptions:{id:'desc',name:'asc' },
                sortOptions:{

                },
                url: {
                    list: '/${instanceName}/page',
                    deletes: '/${instanceName}/deleteByIds',
                    delete: '/${instanceName}/deleteById',
                    edit: '/${instanceName}/update',
                    add: '/${instanceName}/insert',
                    getInfo: "/${instanceName}/getInfo",
                    downloadModel: '/${instanceName}/downloadModel',
                    export: '/${instanceName}/exportExcel',
                    import: "/${instanceName}/importExcel"
                },
                tableColumns: [
                    <#list tableColumns as column>
                    {prop: '${column.fieldName}', label: "${column.columnComment}", align: 'center'},
                    </#list>
                ],
                //Form
                formConfig: {
                    columns: [
                        <#list tableColumns as column>
                        {prop: '${column.fieldName}', label: "${column.columnComment}",},
                        </#list>
                    ],
                    data: {},
                    rowSize: 1,   //一行可以展示几列表单
                    labelWidth: '80px', //labeld宽度
                },
                //上传格式
                accept: '.xlsx, .xls'
            };
        }
    };
</script>
