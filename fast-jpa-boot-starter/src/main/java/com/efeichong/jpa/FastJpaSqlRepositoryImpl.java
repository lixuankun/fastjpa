package com.efeichong.jpa;

import com.efeichong.common.PageData;
import com.efeichong.util.EntityUtils;
import com.efeichong.util.FastJpaSpringUtils;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lxk
 * @date 2024/5/30
 * @description
 */
@Repository
public class FastJpaSqlRepositoryImpl implements FastJpaSqlRepository {

    private NamedParameterJdbcTemplate jdbcTemplate ;

    @Autowired
    public void FastJpaSqlRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public <T> List<T> selectBySql(String sql, Class<T> resultClazz, Map<String, Object> params) {
        boolean hasNoParamConstructor = false;
        Constructor<?>[] constructors = resultClazz.getDeclaredConstructors();
        if (EntityUtils.isNotEmpty(constructors)) {
            for (Constructor<?> constructor : constructors) {
                if (constructor.getModifiers() == 1 && constructor.getParameterCount() == 0) {
                    hasNoParamConstructor = true;
                    break;
                }
            }
        }
        if (Map.class.isAssignableFrom(resultClazz)) {
            return (List<T>) jdbcTemplate.queryForList(sql, params);
        } else {
            if (hasNoParamConstructor) {
                return jdbcTemplate.query(sql, params, BeanPropertyRowMapper.newInstance(resultClazz));
            } else {
                return jdbcTemplate.query(sql, params, SingleColumnRowMapper.newInstance(resultClazz));
            }
        }
    }

    @Override
    public <T> List<T> selectBySql(String sql, Class<T> resultClazz) {
        return selectBySql(sql, resultClazz, null);
    }

    @Override
    public <T> T selectOneBySql(String sql, Class<T> resultClazz, Map<String, Object> params) {
        boolean hasNoParamConstructor = false;
        Constructor<?>[] constructors = resultClazz.getDeclaredConstructors();
        if (EntityUtils.isNotEmpty(constructors)) {
            for (Constructor<?> constructor : constructors) {
                if (constructor.getModifiers() == 1 && constructor.getParameterCount() == 0) {
                    hasNoParamConstructor = true;
                    break;
                }
            }
        }
        if (Map.class.isAssignableFrom(resultClazz)) {
            return (T) jdbcTemplate.queryForMap(sql, params);
        } else {
            if (hasNoParamConstructor) {
                return jdbcTemplate.queryForObject(sql, params, BeanPropertyRowMapper.newInstance(resultClazz));
            } else {
                return jdbcTemplate.queryForObject(sql, params, resultClazz);
            }
        }
    }

    @Override
    public <T> T selectOneBySql(String sql, Class<T> resultClazz) {
        return selectOneBySql(sql, resultClazz, null);
    }

    @Override
    public <T> PageData<T> selectByPage(@NonNull String sql, @NonNull Pageable page, @NonNull Class<T> resultClazz, Map<String, Object> params) {
        Long total = getCount(sql, params);
        sql = sql + getPageLimit(page);
        List<T> list = selectBySql(sql, resultClazz, params);
        PageData<T> pageData = new PageData<>();
        pageData.setList(list);
        pageData.setPageIndex(page.getPageNumber());
        pageData.setPageSize(page.getPageSize());
        pageData.setTotalRows(total);
        pageData.setTotalPage((int) Math.ceil(total / (double) page.getPageSize()));
        return pageData;
    }

    @Override
    public <T> PageData<T> selectByPage(@NonNull String sql, @NonNull Pageable page, @NonNull Class<T> resultClazz) {
        return selectByPage(sql, startPage(page), resultClazz, null);
    }

    @Override
    public <T> PageData<T> selectByPage(@NonNull String sql, @NonNull Class<T> resultClazz) {
        return selectByPage(sql, startPage(null), resultClazz, null);
    }

    @Override
    public <T> PageData<T> selectByPage(@NonNull String sql, @NonNull Class<T> resultClazz, Map<String, Object> params) {
        return selectByPage(sql, startPage(null), resultClazz, params);
    }

    @Override
    public void batchSave(String sql, Collection<Map<String, Object>> entities, int batchSize) {
        SqlParameterSource[] batchArgs = SqlParameterSourceUtils
                .createBatch(entities.toArray());
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public void save(String sql, Map<String, Object> entity) {
        jdbcTemplate.update(sql, entity);
    }

    @Override
    public <T> void batchSaveEntities(String sql, Collection<T> entities, int batchSize) {
        SqlParameterSource[] batchArgs = SqlParameterSourceUtils
                .createBatch(entities.toArray());
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public <T> void saveEntity(String sql, T t) {
        jdbcTemplate.update(sql, new BeanPropertySqlParameterSource(t));
    }

    @Override
    public void batchDelete(String tableName) {
        jdbcTemplate.update("delete from " + tableName, EmptySqlParameterSource.INSTANCE);
    }

    @Override
    public <ID> void batchDelete(String tableName, @NonNull Collection<ID> ids) {
        jdbcTemplate.update("delete from " + tableName + " where id in (:ids)", new MapSqlParameterSource("ids", ids));
    }

    /**
     * 专门为?的自定义sql获取总数
     *
     * @param sql
     * @param params
     * @return
     */
    private Long getCount(String sql, Map<String, Object> params) {
        String countSql = "select count(*) from ( "
                .concat(sql)
                .concat(" ) ct ");
        return selectOneBySql(countSql, Long.class, params);
    }

    /**
     * 为自定义的sql添加Limit
     *
     * @param page
     * @return
     */
    private String getPageLimit(Pageable page) {

        int pageNum = page.getPageNumber();
        int pageSize = page.getPageSize();


        int pageOffset = (pageNum - 1) * pageSize;

        StringBuilder builder = new StringBuilder(" limit ");
        builder.append(pageOffset);
        builder.append(",");
        builder.append(pageSize);
        return builder.toString();
    }

    /**
     * 分页查询，默认使用前端传的参数 @params{pageIndex,pageSize},如果前端没有传这个参数
     * 不指定则默认 pageIndex=1 pageSize=10
     *
     * @param page
     */
    private Pageable startPage(Pageable page) {
        if (page != null) {
            return page;
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            String pageIndex = request.getParameter("pageIndex");
            String pageSize = request.getParameter("pageSize");
            if (EntityUtils.isNotEmpty(pageIndex) && EntityUtils.isNotEmpty(pageSize)) {
                page = PageRequest.of(Integer.parseInt(pageIndex), Integer.parseInt(pageSize));
            }
        }
        if (page == null) {
            page = PageRequest.of(1, 10);
        }
        return page;
    }

}
