package ai.yue.library.data.jdbc.client;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.alibaba.fastjson.JSONObject;

import ai.yue.library.base.exception.DBException;
import ai.yue.library.base.util.MapUtils;
import ai.yue.library.base.util.StringUtils;
import ai.yue.library.base.view.ResultErrorPrompt;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>SQL优化型数据库操作</h2>
 * Created by sunJinChuan on 2016/6/6
 * @since 0.0.1
 */
@Slf4j
class DBBase {
	
	protected JdbcTemplate jdbcTemplate;
	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
    // 方法
    
	/**
     * 判断更新所影响的行数是否 <b>等于</b> 预期值
     * <p>
     * 若不是预期值，那么将会抛出一个{@linkplain DBException}
     * @param updateRowsNumber	更新所影响的行数
     * @param expectedValue		预期值
     */
	public void updateAndExpectedEqual(long updateRowsNumber, int expectedValue) {
        if (updateRowsNumber != expectedValue) {
    		String msg = ResultErrorPrompt.dataStructure(expectedValue, updateRowsNumber);
        	throw new DBException(msg);
        }
	}
    
    /**
     * 判断更新所影响的行数是否 <b>等于</b> 预期值
     * <p>
     * 若不是预期值，同时 updateRowsNumber > 0 那么将会抛出一个{@linkplain DBException}
     * @param updateRowsNumber	更新所影响的行数
     * @param expectedValue		预期值
     * @return
     */
	public boolean isUpdateAndExpectedEqual(long updateRowsNumber, int expectedValue) {
        if (updateRowsNumber == expectedValue) {
        	return true;
        }
        if (updateRowsNumber == 0) {
        	return false;
        }
        
        String msg = ResultErrorPrompt.dataStructure(expectedValue, updateRowsNumber);
        throw new DBException(msg);
	}
	
    /**
     * 判断更新所影响的行数是否 <b>大于等于</b> 预期值
     * <p>
     * 若不是预期结果，那么将会抛出一个{@linkplain DBException}
     * @param updateRowsNumber	更新所影响的行数
     * @param expectedValue		预期值
     */
	public void updateAndExpectedGreaterThanEqual(long updateRowsNumber, int expectedValue) {
        if (!(updateRowsNumber >= expectedValue)) {
        	String msg = ResultErrorPrompt.dataStructure(">= " + expectedValue, updateRowsNumber);
            throw new DBException(msg);
        }
	}
	
    /**
     * 判断更新所影响的行数是否 <b>大于等于</b> 预期值
     * <p>
     * 若不是预期结果，同时 updateRowsNumber < expectedValue 那么将会抛出一个{@linkplain DBException}
     * @param updateRowsNumber	更新所影响的行数
     * @param expectedValue		预期值
     * @return
     */
	public boolean isUpdateAndExpectedGreaterThanEqual(long updateRowsNumber, int expectedValue) {
        if (updateRowsNumber >= expectedValue) {
        	return true;
        }
        if (updateRowsNumber == 0) {
        	return false;
        }
        
        String msg = ResultErrorPrompt.dataStructure(">= " + expectedValue, updateRowsNumber);
        throw new DBException(msg);
	}

    /**
     * 同 {@linkplain #queryForMap(String, Map)} 的安全查询结果获取
     * @param list {@linkplain #queryForList(String, Map)} 查询结果
     * @return
     */
    public JSONObject resultToJSON(List<Map<String, Object>> list) {
    	int size = list.size();
    	int expectedValue = 1;
    	if (size != expectedValue) {
    		if (size > expectedValue) {
    			String msg = ResultErrorPrompt.dataStructure(expectedValue, size);
    			log.warn(msg);
    		}
    		
    		return null;
    	}
    	
    	return new JSONObject(list.get(0));
	}
    
    /**
     * 同 {@linkplain #queryForObject(String, Map, Class)} 的安全查询结果获取
     * @param <T>
     * @param list {@linkplain #queryForList(String, Map, Class)} 查询结果
     * @return
     */
    public <T> T resultToObject(List<T> list) {
    	int size = list.size();
    	int expectedValue = 1;
    	if (size != expectedValue) {
    		if (size > expectedValue) {
    			String msg = ResultErrorPrompt.dataStructure(expectedValue, size);
    			log.warn(msg);
    		}
    		
    		return null;
    	}
    	
    	return list.get(0);
	}
    
    // WHERE SQL
    
	private synchronized void paramToWhereSql(StringBuffer whereSql, final Map<String, Object> paramMap,
			final String condition) {
		whereSql.append(" AND ");
		whereSql.append(condition);
		var value = paramMap.get(condition);
		if (null == value) {
			whereSql.append(" IS :");
			whereSql.append(condition);
		} else if (value instanceof List) {
			whereSql.append(" IN (:");
			whereSql.append(condition);
			whereSql.append(") ");
		} else {
			whereSql.append(" = :");
			whereSql.append(condition);
		}
	}
    
    /**
     * <b>绝对条件查询参数whereSql化</b>
     * <p>
     * <i>已对 {@link NULL} 值进行特殊处理（IS NULL）</i><br><br>
     * <i>已对 {@linkplain List} 类型值进行特殊处理（IN (?, ?)）</i><br><br>
     * 
     * <b>结果示例：</b><br>
     * <blockquote>
     * <pre>
     * <code>WHERE 1 = 1</code><br>
     * <code>AND</code><br>
     * <code>param1 = :param1</code><br>
     * <code>AND</code><br>
     * <code>param2 IS NULL :param2</code><br>
     * <code>AND</code><br>
     * <code>param3 IN :param3</code><br>
     * <code>AND ...</code>
     * </pre>
     * </blockquote>
     * 
     * @param paramMap
     * @param conditions
     * @return
     */
	protected String paramToWhereSql(Map<String, Object> paramMap, String... conditions) {
		StringBuffer whereSql = new StringBuffer();
		whereSql.append(" WHERE 1 = 1 ");
		for (String condition : conditions) {
			paramToWhereSql(whereSql, paramMap, condition);
		}
		
		return whereSql.toString();
	}
    
    /**
     * <b>绝对条件查询参数whereSql化</b>
     * <p>
     * <i>已对 {@link NULL} 值进行特殊处理（IS NULL）</i><br><br>
     * <i>已对 {@linkplain List} 类型值进行特殊处理（IN (?, ?)）</i><br><br>
     * 
     * <b>结果示例：</b><br>
     * <blockquote>
     * <pre>
     * <code>WHERE 1 = 1</code><br>
     * <code>AND</code><br>
     * <code>param1 = :param1</code><br>
     * <code>AND</code><br>
     * <code>param2 IS NULL :param2</code><br>
     * <code>AND</code><br>
     * <code>param3 IN :param3</code><br>
     * <code>AND ...</code>
     * </pre>
     * </blockquote>
     * 
     * @param paramMap
     * @return
     */
    public String paramToWhereSql(Map<String, Object> paramMap) {
    	StringBuffer whereSql = new StringBuffer();
    	whereSql.append(" WHERE 1 = 1 ");
		paramMap.keySet().forEach(condition -> {
			paramToWhereSql(whereSql, paramMap, condition);
		});
		return whereSql.toString();
    }
    
    // ParamValidate
    
	/**
	 * 参数验证
	 * @param tableName
	 */
    protected void paramValidate(String tableName) {
		if (StringUtils.isEmpty(tableName)) {
			throw new DBException("表名不能为空");
		}
	}
	
	/**
	 * 参数验证
	 * @param tableName
	 * @param whereSql
	 */
    protected void paramValidate(String tableName, String whereSql) {
		if (StringUtils.isEmpty(tableName)) {
			throw new DBException("表名不能为空");
		}
		if (StringUtils.isEmpty(tableName)) {
			throw new DBException("whereSql不能为空");
		}
	}
    
	/**
	 * 参数验证
	 * @param tableName
	 * @param id
	 */
    protected void paramValidate(String tableName, Long id) {
		if (StringUtils.isEmpty(tableName)) {
			throw new DBException("表名不能为空");
		}
		if (null == id) {
			throw new DBException("参数id不能为空");
		}
	}
    
	/**
	 * 参数验证
	 * @param tableName
	 * @param id
	 * @param fieldName
	 */
    protected void paramValidate(String tableName, Long id, String[] fieldName) {
		if (StringUtils.isEmpty(tableName)) {
			throw new DBException("表名不能为空");
		}
		if (null == id) {
			throw new DBException("参数id不能为空");
		}
		if (StringUtils.isEmptys(fieldName)) {
			throw new DBException("fieldName不能为空");
		}
	}
	
	/**
	 * 参数验证
	 * @param tableName
	 * @param paramMap
	 */
    protected void paramValidate(String tableName, Map<String, Object> paramMap) {
		if (StringUtils.isEmpty(tableName)) {
			throw new DBException("表名不能为空");
		}
		if (paramMap.isEmpty()) {
			throw new DBException("参数不能为空");
		}
	}
	
	/**
	 * 参数验证
	 * @param tableName
	 * @param paramMaps
	 */
    protected void paramValidate(String tableName, Map<String, Object>[] paramMaps) {
		if (StringUtils.isEmpty(tableName)) {
			throw new DBException("表名不能为空");
		}
		if (MapUtils.isEmptys(paramMaps)) {
			throw new DBException("参数不能为空");
		}
	}
	
	/**
	 * 参数验证
	 * @param tableName
	 * @param paramMap
	 * @param conditions
	 */
    protected void paramValidate(String tableName, Map<String, Object> paramMap, String[] conditions) {
		if (StringUtils.isEmpty(tableName)) {
			throw new DBException("表名不能为空");
		}
		if (paramMap.isEmpty()) {
			throw new DBException("参数不能为空");
		}
        if (StringUtils.isEmptys(conditions) || !MapUtils.isKeys(paramMap, conditions)) {
        	throw new DBException("更新条件不能为空");
        }
	}
	
}
