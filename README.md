#Fast-Jpa

## 简介
    Fast-Jpa是一个jpa增强工具,除保留jpa的特性外还做了简化开发提供工作效率的处理
    
    
## 特性
* 提供了更为便捷的查询方式,使得我们再也不用为了频繁写sql或者hql而烦恼,使用Example.java可以解决您大部分sql使用场景,单表连表查询都不是问题
* 提供了脱离hibernate钩子的批量操作(增,删,改),使批量操作更为快捷且不用再担心内存溢出的问题 
* 支持 Lambda 形式调用,通过 Lambda 表达式,方便的编写各类查询条件，无需再担心字段写错
* 内置代码生成器,通过fast-generator组件可迅速生成entity,dao,service,controller,vo,灵活配置,使得开发更为便捷
* 支持 MySQL、MariaDB、Oracle、DB2、H2、HSQL、SQLite、Postgre、SQLServer 等多种数据库
* 多租户支持
* 逻辑删除支持
* 乐观锁/悲观锁
* vo和po映射工具,内部封装了类似于mapstruct的映射工具,使得vo和po的转换更加方便
* 查询直接返回vo,常规的jpa查询返回都是持久化类,查询字段为全表,此功能可以查询指定字段并返回vo
* 分页查询
* 多数据源支持

## 快速开始

```text 
提示: 1.建表依然沿用jpa(hibernate)规范,合理使用外键,对于代码生成也会有很大帮助
     2.仅支持springboot项目,普通spring项目不支持
```

### 安装
#### Maven
当前最新版本为 2.1.2
```xml
<!-- fast-jpa -->
<dependency>
    <groupId>com.efeichong</groupId>
    <artifactId>fast-jpa-boot-starter</artifactId>
    <version>${last version}</version>
</dependency>
```

#### Gradle
```groovy
    compile group: 'com.efeichong', name: 'fastjpa', version: 'last version', ext: 'pom'
```

### 配置
#### 配置EnableFastJpa注解
* 在启动类增加注解@EnableFastJpa开启fast-jpa支持
* 自己的dao需继承FastJpaRepository.java

```java
    @SpringBootApplication
    //开启fast-jpa支持并扫描dao(repositories)所在目录
    @EnableFastJpa("com.fastjpa.dao")
    //实体类所在的目录
    @EntityScan("com.fastjpa.domain")
    public class Application {
    
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    
    }

    public interface UserDao extends FastJpaRepository<User, Long> {

    }
```

### 注解
#### @Entity
* 描述: 用于指定当前注解的类代表一个实体类型,并且它们的状态由底层持久性上下文管理,name字段为表名,默认为实体类名转下划线,例如HelloWorld.class对应表名为hello_world
* 使用位置: 实体类
* entity规范,详细请参考Java Persistence 2.1 规范
  * 实体类必须有一个无参数的构造函数，它可以是公共的、受保护的或包可见性。它也可以定义额外的构造函数。
  * 实体类不必是顶级类。
  * 从技术上讲，Hibernate 可以持久化最终类或具有最终持久状态访问器（getter/setter）方法的类。但是，这通常不是一个好主意，因为这样做会阻止 Hibernate 生成用于延迟加载实体的代理。
  * Hibernate 不限制应用程序开发人员公开实例变量并从实体类本身外部引用它们。然而，这种范式的有效性充其量是值得商榷的。
#### @InsertDefault
指定insert时的默认值,{@code #value()}和{@link #val()}二选一,如果两个都填则以{@code #val()}为准

| 属性  | 类型  |  必须指定  | 描述  |
| ---  |--- | ---   | --- |
| value |String| 否 | 默认值|
| val|DefaultValueProvider| 否 |通过实现DefaultValueProvider指定默认值|

#### @UpdateDefault
指定update时的默认值,{@code #value()}和{@link #val()}二选一,如果两个都填则以{@code #val()}为准

| 属性  | 类型  |  必须指定  | 描述  |
| ---  |--- | ---   | --- |
| value |String| 否 | 默认值|
| val|DefaultValueProvider| 否 |通过实现DefaultValueProvider指定默认值|

#### @LogicDelete
* 描述: 逻辑删除,作用于实体类上, LogicDelete#column()用于指定逻辑删除依据字段
* 该字段值必须为`long,Long,String`三种类型
* 未删除字段值为0或者'0',删除后为一串雪花随机数
* 有逻辑删除的表加唯一约束时请使用uniqueColumn+logicDeleteColumn,这样可避免逻辑删除完成的数据再次新增产生的唯一约束告警的问题
* 通过JExample#ignoreLogicDelete()可以查询到已逻辑删除的数据(即忽略逻辑删除)

#### @EnableMultiTenant
* 描述: 开启多租户功能,作用于实体类上, EnableMultiTenant#column()用于指定租户标识(tenantId)
* 建议在请求进来时(拦截器/过滤器/切面)通过MultiTenantIdHolder#setTenantId()设置租户id
* MultiTenantIdHolder#setTenantId()可以灵活使用,在FastJpaRepository#save()和查询时多租户会立即生效
* 开启多租户后,insert会从MultiTenantIdHolder取出租户id并存于EnableMultiTenant#column()字段中,查询也会从MultiTenantIdHolder取出租户id
作为查询条件(查询条件为tenantId=#{tenantId} or tenantId is null)
* 执行JExample#ignoreMultiTenant()查询条件将不附带租户标识
* 可以手动设置租户标识字段的值进行更新操作,更新有效

#### @Mapping
* 描述: 用于vo/dto和po的映射
* 连表用'.'隔开 例如 学校>班级>学生 在学生的vo(StudentVo)中需要返回学校名称 在学生的vo中增加字段schoolName,并增加注解@Mapping(poProperty = "clazz.school.name", useMode = UseMode.TO_VO)
```text
    @Mapping(poProperty = "clazz.school.name", useMode = UseMode.TO_VO)
    private String clazzName;
```
* 使用场景
   1. 执行方法TransformUtils#toVo,TransformUtils#toPo,TransformUtils#toVos,TransformUtils#toPos手动进行对象的互转 
   2. 查询结果直接为vo时 例如FastJpaRepository#selectById(id,UserVo.class)
   
| 属性  | 类型  |  必须指定  | 描述  |
| ---  |--- | ---   | --- |
| poProperty |String| 是 | 映射po的字段|
| useMode|UseMode| 是 | 定在哪种时候生效 |
```text
UseMode指定在哪种时候生效
UseMode.ALL 都生效(默认)
UseMode.TO_VO po转vo时生效  
    例如查询学生时需要返回他的学校信息班级信息
    @Setter
    @Getter
    public class StudentVo {
        private Long id;
        /**学生名**/
        private String name;
        /**年龄**/
        private Integer age;
        @Mapping(poProperty = "clazz.id", useMode = UseMode.QUERY)
        private Long clazzId;
        @Mapping(poProperty = "clazz.name", useMode = UseMode.TO_VO)
        private String clazzName;
        @Mapping(poProperty = "clazz.teachers.name", useMode = UseMode.TO_VO)
        private List<String> teacherNames;
    }

UseMode.TO_PO vo转po时生效  
    例如添加学生时需要指定他是哪个班级的,TransformUtils#toPo/toPos(saveStudentVo)后会将SaveStudentVo转为Student对象,
    并自动给Student中的Class对象赋值,然后执行FastJpaRepository#save(student)就可以了
    @Setter
    @Getter
    public class SaveStudentVo {
        /**学生名**/
        private String name;
        /**年龄**/
        private Integer age;
        @Mapping(poProperty = "clazz.id", useMode = UseMode.TO_PO)
        private Long clazzId;
    }


UseMode.QUERY 查询时生效 执行JExample#initExample(V vo)时,vo中的字段会生成查询条件,
    String类型字段 包含'*'为模糊查询 以'*'开头 例如'*ab' 表示查询以ab开头,以'*'包裹 例如'*ab*' 表示查询包含ab的数据,没有'*'则为查询相等
    Collection/Array类型字段 为in查询
    字段为begin开头(从url上获取参数)  例如 beginCreateDate 则为 create_date >=''
    字段为end开头(从url上获取参数)  例如 endCreateDate 则为 create_date <='', 字段类型如果为Date类型value会自动拼接" 23:59:59"
    字段命名为sortOptions(从url上获取参数) 例如 '{"age":"asc","count":"desc"}' 则为 按age升序 按count降序
    其余类型均查询相等,字段值为空则不拼接此条件
    
    @Mapping(poProperty = "clazz.id", useMode = UseMode.QUERY)
    private Long clazzId;
```


#### @CreationTimestamp /  @UpdateTimestamp
用于指定当前注解的时间类型必须用当前的 JVM 时间戳值初始化
```text
java.util.Date
java.util.Calendar
java.sql.Date
java.sql.Time
java.sql.Timestamp
java.time.Instant
java.time.LocalDate
java.time.LocalDateTime
java.time.LocalTime
java.time.MonthDay
java.time.OffsetDateTime
java.time.OffsetTime
java.time.Year
java.time.YearMonth
java.time.ZonedDateTime
```
### 快速测试
示例工程: https://gitee.com/efeichong/fast-jpa-demo.git


### 核心功能
#### CURD接口
##### Save
`saveAll()和saveByExample()在执行更新时为先查询后更新,数据量过大会导致内存溢出问题,考虑使用batchUpdate方法或者合理使用
FastJpaRepository#flush()将数据分批进行flush`
```text
    //新增/更新  id不在则新增，存在则更新(默认所有字段不能更改为空)
    <S extends T> S save(@NonNull S entity, UpdateGlobalConfig... config);
    //批量新增/更新  id不在则新增，存在则更新(默认所有字段不能更改为空)
    <S extends T> List<S> saveAll(@NonNull Collection<S> entities, UpdateGlobalConfig... config);
    //根据条件进行更新  (默认所有字段不能更改为空)
    <S extends T> List<S> saveByExample(@NonNull S entity, JExample<T> example, UpdateGlobalConfig... config);
```
| 参数名  | 类型  |  必须指定  | 描述  |
| ---  |--- | ---   | --- |
| entity |S| 是 | 实体对象|
| entities |Collection`<S>` | 是 | 实体对象集合 |
| example |JExample | 是 | 查询条件 |
| config |UpdateGlobalConfig | 否 | 配置忽略更新的字段和可以更新为空的字段 {@link UpdateGlobalConfig#ALL 包含所有字段  UpdateGlobalConfig#NONE 不好含任何字段}|

##### Remove
`deleteAll()deleteAllByIds()在执行更新时为先查询后更新,数据量过大会导致内存溢出问题,考虑使用batchDelete方法或者合理使用
FastJpaRepository#flush()将数据分批进行flush`
```text
当实体类有@LogicDelete注解时触发逻辑删除,@LogicDelete中指定的逻辑删除字段默认值为0或者'0',当执行逻辑删除后该字段的值为雪花随机数,详细请查看@LogicDelete
相关说明
```
```text
    //删除 
    void delete(@NonNull T entity);
    //通过id删除
    void deleteById(@NonNull ID id);
    //删除全部
    void deleteAll();
    //批量删除
    void deleteAll(@NonNull Iterable<? extends T> entities);
    //根据id批量删除
    void deleteAllByIds(@NonNull Iterable<ID> ids);
    //根据条件删除
    void deleteAll(@NonNull JExample<T> example);
```
| 参数名  | 类型  |  必须指定  | 描述  |
| ---  |--- | ---   | --- |
| entity |S| 是 | 实体对象|
| id |ID| 是 | 主键|
| entities |Collection`<S>` | 是 | 实体对象集合 |
| ids |ID| Iterable<? extends T> | 主键集合|
| example |JExample | 是 | 删除where条件 |
##### GET
```text
    /**
      * 查询结果只有一条
      * @param example 查询条件  无法使用分页
      * @return 如果不存在将会返回{@link Optional#empty()}
      * @throws javax.persistence.NonUniqueResultException 如果返回对象数超过一个将会抛这个异常
      */
     Optional<T> selectOne(@NonNull JExample<T> example);
     /**
      * 查询结果只有一条
      * @param example          查询条件  无法使用分页
      * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
      * @param ignoreProperties 不需要查到vo的字段
      * @return 如果不存在将会返回{@link Optional#empty()}
      * @throws javax.persistence.NonUniqueResultException 如果返回对象数超过一个将会抛这个异常
      */
     <V> Optional<V> selectOne(@NonNull JExample<T> example, Class<? extends V> voClazz, String... ignoreProperties);
     /**
      * 通过主键查询
      *
      * @param id 主键
      * @return 如果不存在将会返回{@link Optional#empty()}
      */
     Optional<T> selectById(@NonNull ID id);
     /**
      * 通过主键查询返回vo
      * @param id
      * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
      * @param ignoreProperties 不需要查到vo的字段
      * @param <V>
      * @return
      */
     <V> Optional<V> selectById(@NonNull ID id, Class<? extends V> voClazz, String... ignoreProperties);
     /**
```
##### List
```text
     //查询全表,如果没有结果将返回空集合并非null
     List<T> selectAll();
     /**
      * 查询全表返回Vo,如果没有结果将返回空集合并非null
      * @param voClazz          返回VO
      * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
      * @param ignoreProperties 不需要查到vo的字段
      */
     <V> List<V> selectAll(Class<? extends V> voClazz, String... ignoreProperties);
     /**
      * 条件查询
      * @param example 查询条件  开启分页后仍返回list对象
      * @return 如果没有结果将返回空集合并非null
      */
     List<T> selectAll(@Nullable JExample<T> example);
     /**
      * 条件查询返回Vo
      * @param example          查询条件  开启分页后仍返回list对象
      * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
      * @param ignoreProperties 不需要查到vo的字段
      */
     <V> List<V> selectAll(@Nullable JExample<T> example, Class<? extends V> voClazz, String... ignoreProperties);
   
      * 通过id查询列表
      * @param ids
      */
     List<T> selectByIds(@NonNull Collection<ID> ids);
 
     /**
      * 通过id查询列表返回Vo
      * @param ids
      * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
      * @param ignoreProperties 不需要查到vo的字段
      */
     <V> List<V> selectByIds(@NonNull Collection<ID> ids, Class<? extends V> voClazz, String... ignoreProperties);
```
##### Page查询
```text
  /**
      * 分页查询，默认使用前端传的参数 @params{pageIndex,pageSize},如果前端没有传这个参数
      * 也可以通过{@link JExample#startPage(int, int)}开启，如果同时存在优先使用手动开启的
      * 不指定则默认 pageIndex=1 pageSize=10
      * @param example 查询条件
      * @return 返回分页对象
      */
     PageData<T> selectByPage(@NonNull JExample<T> example);
     /**
      * 分页查询，默认使用前端传的参数 @params{pageIndex,pageSize},如果前端没有传这个参数
      * 也可以通过{@link JExample#startPage(int, int)}开启，如果同时存在优先使用手动开启的
      * 不指定则默认 pageIndex=1 pageSize=10
      * @param example          查询条件
      * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
      * @param ignoreProperties 不需要查到vo的字段
      * @return 返回分页对象
      */
     <V> PageData<V> selectByPage(@NonNull JExample<T> example, Class<? extends V> voClazz, String... ignoreProperties);
     /**
      * 分页查询，默认使用前端传的参数 @params{pageIndex,pageSize},如果前端没有传这个参数
      * 也可以通过{@link JExample#startPage(int, int)}开启，如果同时存在优先使用手动开启的
      * 不指定则默认 pageIndex=1 pageSize=10
      * @return 返回分页对象
      */
     PageData<T> selectByPage();
     /**
      * 分页查询，默认使用前端传的参数 @params{pageIndex,pageSize},如果前端没有传这个参数
      * 也可以通过{@link JExample#startPage(int, int)}开启，如果同时存在优先使用手动开启的
      * 不指定则默认 pageIndex=1 pageSize=10
      * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
      * @param ignoreProperties 不需要查到vo的字段
      * @return 返回分页对象
      */
     <V> PageData<V> selectByPage(Class<? extends V> voClazz, String... ignoreProperties);
```
##### Count/Exist
```text
     /**
      * 根据条件查询库中的行数
      * @param example 查询条件
      * @return 行数
      */
     long count(@Nullable JExample<T> example);
     /**
      * 是否存在满足该条件的记录
      * @param example 查询条件
      * @return false不存在  true存在
      */
     boolean exist(@Nullable JExample<T> example);
     /**
      * 是否存在该id
      * @param id 主键
      * @return false不存在  true存在
      */
     boolean existsById(@NonNull ID id);
```
##### native查询(通过sql查询)
```text
  /**
     * native查询
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     * @param params      查询参数
     */
    <V> List<V> selectBySql(@NonNull String sql, @NonNull Class<V> resultClazz, @NonNull Map<String, Object> params);
    /**
     * native查询
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     */
    <V> List<V> selectBySql(@NonNull String sql, @NonNull Class<V> resultClazz);
    /**
     * native查询
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     * @param params      查询参数
     */
    <V> V selectOneBySql(@NonNull String sql, @NonNull Class<V> resultClazz, @NonNull Map<String, Object> params);
    /**
     * native查询
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     */
    <V> V selectOneBySql(@NonNull String sql, @NonNull Class<V> resultClazz);
```
##### 批量操作 脱离hibernate/jpa的批量操作
```text
    /**
     * 批量新增 直接执行sql
     * 这不会调用您可能拥有的任何 JPA/Hibernate 生命周期钩子
     * 它不会级联到其他实体
     * 不会操作持久性上下文
     * @param entities
     */
    void batchSave(@NonNull Collection<T> entities);
    /**
     * 批量更新 直接执行sql
     * 这不会调用您可能拥有的任何 JPA/Hibernate 生命周期钩子
     * 它不会级联到其他实体
     * 会自动清除持久性上下文
     * @param entities
     */
    void batchUpdate(@NonNull Collection<T> entities);
    /**
     * 批量删除 直接执行sql
     * 这不会调用您可能拥有的任何 JPA/Hibernate 生命周期钩子
     * 它不会级联到其他实体
     * 不会操作持久性上下文
     */
    void batchDelete();
    /**
     * 批量删除 直接执行sql
     * 这不会调用您可能拥有的任何 JPA/Hibernate 生命周期钩子
     * 它不会级联到其他实体
     * 会自动清除持久性上下文
     * @param entities
     */
    void batchDelete(@NonNull Collection<T> entities);
    /**
     * 批量删除 直接执行sql
     * 这不会调用您可能拥有的任何 JPA/Hibernate 生命周期钩子
     * 它不会级联到其他实体
     * 不会操作持久性上下文
     * @param idCollection
     */
    void batchDeleteByIds(@NonNull Collection<ID> idCollection);
```
##### 其它方法
```text
    /**
     * 检查实例是否是属于当前持久性上下文的托管实体实例
     */
    boolean contains(Object entity);
    /**
     * 清除持久性上下文，导致所有托管实体分离。 对尚未刷新到数据库的实体所做的更改将不会被持久化
     */
    void clear();
    /**
     * 从持久化上下文中删除给定的实体，导致托管实体分离。 对实体所做的未刷新更改（包括删除实体）将不会同步到数据库。 先前引用分离实体的实体将继续引用它。
     * @param entity
     */
    void detach(T entity);
    /**
     * 从持久化上下文中删除给定的实体，导致托管实体分离。 对实体所做的未刷新更改（包括删除实体）将不会同步到数据库。 先前引用分离实体的实体将继续引用它。
     * @param entities
     */
    void detach(Collection<T> entities);
```


### 条件构造器

`以下示例均以学校>班级>学生表结构为样例, 查询主体为学生表`

#### 快捷创建
````text
快捷创建eqaul条件查询 
JExample.of()  例如 
JExample.of(id,1)  学生id=1
JExample.of(id,1,name,2)  学生id=1 and name=2
JExample.of(clazz.name,'22') 学生班级id=1  select s.* from student s left join clazz z on s.clazz_id=z.id where z.name='22'
````
JExample example = new JExample();
#### 排序
```text
example.orderBy().asc("id");
example.orderBy().desc("clazz.name"); select s.* from student s left join clazz z on s.clazz_id=z.id order by z.name desc
```
#### 分组
```text
example.groupBy("id");
example.groupBy("clazz.name");
```
#### 分页
```text
//分页
example.startPage(1,10);
//关闭分页
example.stopPage();
```
#### 去重
```text
example.setDistinct(true)
```
#### 悲观锁
```text
example.setForUpdate(true)  select * from school where id = 1 for update
```
#### 忽略多租户和逻辑删除
```text
//查询不携带逻辑删除的条件
example.ignoreLogicDelete();
//查询不区分多租户
example.ignoreMultiTenant();
```
#### 根据对象初始化条件
```text
example.initExample(studentVo)
    String类型字段 包含'*'为模糊查询 以'*'开头 例如'*ab' 表示查询以ab开头,以'*'包裹 例如'*ab*' 表示查询包含ab的数据,没有'*'则为查询相等
    Collection/Array类型字段 为in查询
    字段为begin开头(从url上获取参数)  例如 beginCreateDate 则为 create_date >=''
    字段为end开头(从url上获取参数)  例如 endCreateDate 则为 create_date <='', 字段类型如果为Date类型value会自动拼接" 23:59:59"
    字段命名为sortOptions(从url上获取参数) 例如 '{"age":"asc","count":"desc"}' 则为 按age升序 按count降序
    其余类型均查询相等,字段值为空则不拼接此条件
```
#### 一个括号 
```text
example.and()    and ()
example.or()     or ()
```
#### 条件生成
```text
//创建一个条件
Criteria criteria = example.createCriteria()
```
```text
1. 最后一个参数为removeEmptyValueCondition,默认为false  为true时 value为空时条件自动移除  in一个空集合条件不生效  between区间开始和结束任意一个为空不生效
                                                    为false时会校验value非空  value为空会报错 in一个空集合报错 between区间开始和结束任意一个为空报错
2. value 为字段时使用 {@link Column#of(String)} 这种时查询columnA=columnB
```
##### Equal
```text
andEqualTo();
andNotEqualTo();
orEqualTo();
orNotEqualTo();

criteria.andEqualTo("clazz.name","11");  select s.* from student s left join clazz z on s.clazz_id=z.id where z.name='22'
criteria.andEqualTo(Student::getName,"11");  select s.* from student s where name='11'
criteria.andEqualTo("name","11", true);  
criteria.andEqualTo("name",null, true); 这个条件会被自动移除  
criteria.andEqualTo("name",null); 这个会返回JpaException("value不能为空")
criteria.andEqualTo("clazz.name",Column.of("name")); select s.* from student s left join clazz z on s.clazz_id=z.id where z.name=s.name
```
##### GreaterThan
```text
andGreaterThan();  >
andGreaterThanEqualTo(); >=
orGreaterThan();
orGreaterThanEqualTo();
```
##### LessThan
```text
andLessThan(); <
andLessThanEqualTo <=
orLessThan();
orLessThanEqualTo
```
##### Between
```text
andBetween();   between ... and ...
andNotBetween();
orBetween();
orNotBetween();
```
##### In
```text
andIn();   in(1,2,3)
andNotIn();
orIn();
orNotIn();
```
##### Like
```text
参数 [String property, String value, LikeType likeType, boolean... removeEmptyValueCondition]
    likeType 不传默认为LikeType.CONTAINS(以%开头以%结尾)  like '%param%'
    LikeType.LEFT(以%开头)  like '%param'
    LikeType.RIGHT(以%结尾)  like 'param%'
andLike();   
andNotLike();
orLike();
orNotLike();
```
##### IsNull
```text
andIsNull();   
andIsNotNull();
```


##### 自定义表达式Expression
```text
criteria.andExpression(new ExecExpression() {
                         @Override
                         public Predicate genExpression(Root root, CriteriaBuilder criteriaBuilder) {
                             return criteriaBuilder.gt(root.get("age"),criteriaBuilder.literal(1));
                         }
                     });
select s.* from student s where s.age>1;
```


### 锁
- 乐观锁 在指定版本号的字段上加@Version

```xslt
@Version
private Long version;
```
- 悲观锁  example.setForUpdate(true);

### 多数据源支持
#### 注意事项
1. 需在yml中配置spring.jpa.open-in-view=false(如果为true在接口请求进来时就会创建一个session,当请求结束后才会关闭,数据源只会使用默认数据源,无法进行切换)
2. 需在yml中配置spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true(解决no session异常,设置open-in-view=false后,在没有事务的方法
中获取持久化类的对象属性时会提示no session)
3. 开启事务后则只会使用开启事务时指定的第一个数据源,切换数据源无效(开启事务后session会跟事务绑定)
   1. 每个数据源独立开启一个事务@Transactional(propagation = Propagation.REQUIRES_NEW)或者使用TransactionTemplate自行维护
   2. 不使用事务
4. 每个数据源的事务要单独维护,即每个数据源要开一个新的事务
#### 配置

```yaml
spring:
  datasource:
    #动态数据源配置
    dynamic:
      #第一个数据源定义的名称(第一个为默认数据源)
      oneName:
        url: #数据库连接
        username: #数据库用户名
        password: #数据库密码
        type: #连接池clazz
       #第二个数据源定义的名称
      twoName:
        url: 
        username: 
        password:
        type:
```
#### 使用
1. 在类或者方法上加注解@Ds(value= "数据源定义的名称")
2. 使用DynamicDataSourceContextHolder进行动态切换
3. 配置spring.datasource.dynamic自动开启多数据源配置,否则不开启
4. 多数据配置中的第一个为默认数据源,当没指定数据源或者数据源指定错误时默认使用第一个

### 代码生成器
#### 安装
```text
<dependency>
    <groupId>com.efeichong</groupId>
    <artifactId>fast-generator-boot-starter</artifactId>
     <version>${last version}</version>
</dependency>
```
#### 生成po，dao，vo，service,serviceImpl，controller文件
1. 一对多/多对多等关联关系的生成需要在数据库表中指定外键关联
2. 一对一关系需指定两张表的外键关联且关联字段有唯一约束
3. 对多对中间表定义：只有两个字段且都有外键约束


#### 参数

| 参数  | 类型  |  说明  | 是否必须  | 默认值|
| ---  |--- | ---   | --- | --- |
| genConfiguration  |GenConfiguration | 配置   | 否|GenConfiguration|
| requireTableNames  |Set | 指定哪些表需要进行代码生成   |否|全库|
| ignoreTableNames  |Set | 不需要生成的表   |否|空|
| hasExcel  |boolean | 是否生成excel相关代码   |否|false|
| hasSwagger  |boolean | 是否生成excel相关代码   |否|false|
| hasLombok  |boolean | 是否生成excel相关代码   |否|true|
| manyToManyMappedTableMap  |Map<String, String> | 指定多对多映射的表维护的一方 key:多对多中间表  value：维护者 表名字   |否|空|
| dataSource  |DataSource | 数据库连接配置   |是|空|
| generateModel  |int | 指定生成的文件类型,默认生成全部   |否| GenConstant.ALL |

`GenConstant.ALL=DOMAIN_TYPE|DAO_TYPE|SERVICE_TYPE|SERVICE_IMPL_TYPE|CONTROLLER_TYPE|VO_TYPE|VUE_TYPE`

#####  代码生成器样例
```$xslt
         Set<String> tables = Sets.newLinkedHashSet();
                tables.add("clazz");
                tables.add("student");
                tables.add("teacher");
                DataSource dataSource = new DataSource();
                dataSource.setDriver("com.mysql.cj.jdbc.Driver");
                dataSource.setUrl("jdbc:mysql://localhost:3306/fast-jpa?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&allowMultiQueries=true&useSSL=false");
                dataSource.setUsername("root");
                dataSource.setPassword("123456");
                //自定义配置
                GenConfiguration configuration = new GenConfiguration();
                //po统一父类
        //        configuration.setBaseEntity(BaseEntity.class);
                configuration.setDomainPackage("com.fastjpa.entity");
                configuration.setDaoPackage("com.fastjpa.dao");
                configuration.setServicePackage("com.fastjpa.service");
                configuration.setServiceImplPackage("com.fastjpa.service.impl");
                configuration.setControllerPackage("com.fastjpa.controller");
                configuration.setVoPackage("com.fastjpa.vo");
                configuration.setResponseClazz(ResponseData.class);
                //指定多对多中间表的维护者, 班级和老师多对多关系, 指定了关系表的数据是在对班级对象中的老师字段设置值并新增班级时生成
                Map map = ImmutableMap.of("clazz_teacher_ship", "teacher");
                Generator generator = Generator.builder().addGenConfiguration(configuration)
                        .addRequireTableNames(tables)
        //                .addHasExcel(true)
                        .addDataSource(dataSource)
                        .addGenerateModel(GenConstant.ALL)
                        .addHasSwagger(false)
                        .addManyToManyMappedTableMap(map)
                        .build();
                generator.generate();
```



