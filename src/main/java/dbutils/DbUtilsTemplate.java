/*
 * Created on 13-6-5
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright @2013 the original author or authors.
 */
package dbutils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 调用Apache Commons DBUtil组件的数据库操作类
 * 采用c3p0作为数据源，数据源在Spring中已经配置好
 * 本类已经在Spring中配置好，在需要的地方，set注入后即可调用
 * <code>
 * private DbUtilsTemplate dbUtilsTemplate;
 * public void setDbUtilsTemplate(DbUtilsTemplate dbUtilsTemplate) {
 * this.dbUtilsTemplate = dbUtilsTemplate;
 * }
 *
 * @author XiongNeng
 * @version 1.0
 * @since 13-6-5
 */
public class DbUtilsTemplate {
    private DataSource dataSource;
    private QueryRunner queryRunner;
    private boolean pmdKnownBroken = false;
    private static final Logger LOG = LoggerFactory.getLogger(DbUtilsTemplate.class);

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 执行sql语句
     *
     * @param sql sql语句
     * @return 受影响的行数
     */
    public int update(String sql) throws SQLException {
        return update(sql, null);
    }

    /**
     * 执行sql语句
     * <code>
     * executeUpdate("update user set username = 'kitty' where username = ?", "hello kitty");
     * </code>
     *
     * @param sql   sql语句
     * @param param 参数
     * @return 受影响的行数
     */
    public int update(String sql, Object param) throws SQLException {
        return update(sql, new Object[]{param});
    }

    /**
     * 执行sql语句,无法保证事务不推荐使用
     *
     * @param sql    sql语句
     * @param params 参数数组
     * @return 受影响的行数
     */
    public int update(String sql, Object[] params) throws SQLException {
        queryRunner = new QueryRunner();
        int affectedRows = 0;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            if (params == null) {
                affectedRows = queryRunner.update(conn, sql);
            } else {
                affectedRows = queryRunner.update(conn, sql, params);
            }

        } catch (SQLException e) {
            LOG.error("Error occured while attempting to update data", e);
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null)
                DbUtils.commitAndClose(conn);
        }
        return affectedRows;
    }

    /**
     * 执行sql语句
     *
     * @param sql sql语句
     * @return 受影响的行数
     */
    public long insert(String sql) throws SQLException {
        return insert(sql, null);
    }

    /**
     * 执行sql语句
     * <code>
     * executeUpdate("insert user(name, age) values(?,?));
     * </code>
     *
     * @param sql   sql语句
     * @param param 参数
     * @return 受影响的行数
     */
    public long insert(String sql, Object param) throws SQLException {
        return insert(sql, new Object[]{param});
    }

    /**
     * 插入一条记录，并返回自增主键
     *
     * @param sql    sql语句
     * @param params 参数数组
     * @return 自增主键(如果没有更新成功, 返回-1或跑出异常)
     * @throws SQLException
     */
    public long insert(String sql, Object[] params) throws SQLException {
        long result = -1L;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql);
            fillStatement(stmt, params);
            int affectCount = stmt.executeUpdate();
            if (affectCount <= 0) return -1L;
            rs = stmt.getGeneratedKeys();
            result = rs.next() ? rs.getLong(1) : -1;
            conn.commit();
        } catch (SQLException e) {
            LOG.error("Error occured while attempting to insert data", e);
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            DbUtils.closeQuietly(conn, stmt, rs);
        }
        return result;
    }

    public void fillStatement(PreparedStatement stmt, Object... params) throws SQLException {

        // check the parameter count, if we can
        ParameterMetaData pmd = null;
        if (!pmdKnownBroken) {
            pmd = stmt.getParameterMetaData();
            int stmtCount = pmd.getParameterCount();
            int paramsCount = params == null ? 0 : params.length;

            if (stmtCount != paramsCount) {
                throw new SQLException("Wrong number of parameters: expected "
                        + stmtCount + ", was given " + paramsCount);
            }
        }

        // nothing to do here
        if (params == null) {
            return;
        }

        for (int i = 0; i < params.length; i++) {
            if (params[i] != null) {
                stmt.setObject(i + 1, params[i]);
            } else {
                // VARCHAR works with many drivers regardless
                // of the actual column type. Oddly, NULL and
                // OTHER don't work with Oracle's drivers.
                int sqlType = Types.VARCHAR;
                if (!pmdKnownBroken) {
                    try {
                        /*
                         * It's not possible for pmdKnownBroken to change from
                         * true to false, (once true, always true) so pmd cannot
                         * be null here.
                         */
                        sqlType = pmd.getParameterType(i + 1);
                    } catch (SQLException e) {
                        pmdKnownBroken = true;
                    }
                }
                stmt.setNull(i + 1, sqlType);
            }
        }
    }

    /**
     * 执行批量sql语句
     *
     * @param sql    sql语句
     * @param params 二维参数数组
     * @return 受影响的行数的数组
     */
    public int[] batchUpdate(String sql, Object[][] params) throws SQLException {
        queryRunner = new QueryRunner();
        int[] affectedRows = new int[0];
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            affectedRows = queryRunner.batch(conn, sql, params);
        } catch (SQLException e) {
            LOG.error("Error occured while attempting to batch update data", e);
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                DbUtils.commitAndClose(conn);
            }
        }
        return affectedRows;
    }

    /**
     * 执行查询，将每行的结果保存到一个Map对象中，然后将所有Map对象保存到List中
     *
     * @param sql sql语句
     * @return 查询结果
     */
    public List<Map<String, Object>> find(String sql) {
        return find(sql, null);
    }

    /**
     * 执行查询，将每行的结果保存到一个Map对象中，然后将所有Map对象保存到List中
     *
     * @param sql   sql语句
     * @param param 参数
     * @return 查询结果
     */
    public List<Map<String, Object>> find(String sql, Object param) {
        return find(sql, new Object[]{param});
    }

    /**
     * 执行查询，将每行的结果保存到一个Map对象中，然后将所有Map对象保存到List中
     *
     * @param sql    sql语句
     * @param params 参数数组
     * @return 查询结果
     */
    public List<Map<String, Object>> find(String sql, Object[] params) {
        queryRunner = new QueryRunner();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            if (params == null) {
                list = queryRunner.query(conn, sql, new MapListHandler());
            } else {
                list = queryRunner.query(conn, sql, new MapListHandler(), params);
            }
        } catch (SQLException e) {
            LOG.error("Error occured while attempting to query data", e);
        } finally {
            if (conn != null) {
                DbUtils.closeQuietly(conn);
            }
        }
        return list;
    }

    /**
     * 执行查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @return 查询结果
     */
    public <T> List<T> find(Class<T> entityClass, String sql) {
        return find(entityClass, sql, null);
    }

    /**
     * 执行查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @param param       参数
     * @return 查询结果
     */
    public <T> List<T> find(Class<T> entityClass, String sql, Object param) {
        return find(entityClass, sql, new Object[]{param});
    }

    /**
     * 执行查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @param params      参数数组
     * @return 查询结果
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> find(Class<T> entityClass, String sql, Object[] params) {
        queryRunner = new QueryRunner();
        Connection conn = null;
        List<T> list = new ArrayList<T>();
        try {
            conn = dataSource.getConnection();
            if (params == null) {
                list = (List<T>) queryRunner.query(conn, sql, new BeanListHandler(entityClass));
            } else {
                list = (List<T>) queryRunner.query(conn, sql, new BeanListHandler(entityClass), params);
            }
        } catch (SQLException e) {
            LOG.error("Error occured while attempting to query data", e);
        } finally {
            if (conn != null) {
                DbUtils.closeQuietly(conn);
            }
        }
        return list;
    }

    /**
     * 执行分页查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @param page        页号
     * @param pageSize    每页记录条数
     * @return 查询结果
     */
    public <T> List<T> find(Class<T> entityClass, String sql, int page, int pageSize) {
        return find(entityClass, sql, null, page, pageSize);
    }

    /**
     * 执行分页查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @param param       参数
     * @param page        页号
     * @param pageSize    每页记录条数
     * @return 查询结果
     */
    public <T> List<T> find(Class<T> entityClass, String sql, Object param, int page, int pageSize) {
        return find(entityClass, sql, new Object[]{param}, page, pageSize);
    }

    /**
     * 执行分页查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @param params      参数数组
     * @param page        页号
     * @param pageSize    每页记录条数
     * @return 查询结果
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> find(Class<T> entityClass, String sql, Object[] params, int page, int pageSize) {
        queryRunner = new QueryRunner();
        Connection conn = null;
        List<T> list = new ArrayList<T>();
        int startFlag = (((page < 1 ? 1 : page) - 1) * pageSize);
        String pageSql = " limit " + startFlag + " , " + startFlag + pageSize;
        try {
            conn = dataSource.getConnection();
            if (params == null) {
                list = (List<T>) queryRunner.query(conn, sql + pageSql, new BeanListHandler(entityClass));
            } else {
                list = (List<T>) queryRunner.query(conn, sql + pageSql, new BeanListHandler(entityClass), params);
            }
        } catch (SQLException e) {
            LOG.error("Error occured while attempting to query data", e);
        } finally {
            if (conn != null) {
                DbUtils.closeQuietly(conn);
            }
        }
        return list;
    }

    /**
     * 执行分页查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中,然后装List封装成PageResult对象
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @param page        页号
     * @param pageSize    每页记录条数
     * @return PageResult对象
     */
    public <T> PageResult findPageResult(Class<T> entityClass, String sql, int page, int pageSize) {
        return findPageResult(entityClass, sql, null, page, pageSize);
    }

    /**
     * 执行分页查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中,然后装List封装成PageResult对象
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @param param       参数
     * @param page        页号
     * @param pageSize    每页记录条数
     * @return PageResult对象
     */
    public <T> PageResult findPageResult(Class<T> entityClass, String sql, Object param, int page, int pageSize) {
        return findPageResult(entityClass, sql, new Object[]{param}, page, pageSize);
    }

    /**
     * 执行分页查询，将每行的结果保存到Bean中，然后将所有Bean保存到List中,然后装List封装成PageResult对象
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @param params      参数数组
     * @param page        页号
     * @param pageSize    每页记录条数
     * @return PageResult对象
     */
    @SuppressWarnings("unchecked")
    public <T> PageResult findPageResult(Class<T> entityClass, String sql, Object[] params, int page, int pageSize) {
        queryRunner = new QueryRunner();
        Connection conn = null;
        List<T> list = new ArrayList<T>();
        int startPage = page < 1 ? 1 : page;
        int startFlag = ((startPage - 1) * pageSize);
        String pageSql = " limit " + startFlag + " , " + startFlag + pageSize;
        try {
            conn = dataSource.getConnection();
            if (params == null) {
                list = (List<T>) queryRunner.query(conn, sql + pageSql, new BeanListHandler(entityClass));
            } else {
                list = (List<T>) queryRunner.query(conn, sql + pageSql, new BeanListHandler(entityClass), params);
            }
        } catch (SQLException e) {
            LOG.error("Error occured while attempting to query data", e);
        } finally {
            if (conn != null) {
                DbUtils.closeQuietly(conn);
            }
        }
        // 计算总行数
        int count = getCount(sql, params);
        // 计算当前页号
        int currentPage = getBeginPage(startPage, pageSize, count);

        return new PageResult(currentPage, pageSize, list, count);
    }

    /**
     * 查询出结果集中的第一条记录，并封装成对象
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @return 对象
     */
    public <T> T findFirst(Class<T> entityClass, String sql) {
        return findFirst(entityClass, sql, null);
    }

    /**
     * 查询出结果集中的第一条记录，并封装成对象
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @param param       参数
     * @return 对象
     */
    public <T> T findFirst(Class<T> entityClass, String sql, Object param) {
        return findFirst(entityClass, sql, new Object[]{param});
    }

    /**
     * 查询出结果集中的第一条记录，并封装成对象
     *
     * @param entityClass 类名
     * @param sql         sql语句
     * @param params      参数数组
     * @return 对象
     */
    @SuppressWarnings("unchecked")
    public <T> T findFirst(Class<T> entityClass, String sql, Object[] params) {
        queryRunner = new QueryRunner();
        Connection conn = null;
        Object object = null;
        try {
            conn = dataSource.getConnection();
            if (params == null) {
                object = queryRunner.query(conn, sql, new BeanHandler(entityClass));
            } else {
                object = queryRunner.query(conn, sql, new BeanHandler(entityClass), params);
            }
        } catch (SQLException e) {
            LOG.error("Error occured while attempting to query data", e);
        } finally {
            if (conn != null) {
                DbUtils.closeQuietly(conn);
            }
        }
        return (T) object;
    }

    /**
     * 查询出结果集中的第一条记录，并封装成Map对象
     *
     * @param sql sql语句
     * @return 封装为Map的对象
     */
    public Map<String, Object> findFirst(String sql) {
        return findFirst(sql, null);
    }

    /**
     * 查询出结果集中的第一条记录，并封装成Map对象
     *
     * @param sql   sql语句
     * @param param 参数
     * @return 封装为Map的对象
     */
    public Map<String, Object> findFirst(String sql, Object param) {
        return findFirst(sql, new Object[]{param});
    }

    /**
     * 查询出结果集中的第一条记录，并封装成Map对象
     *
     * @param sql    sql语句
     * @param params 参数数组
     * @return 封装为Map的对象
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> findFirst(String sql, Object[] params) {
        queryRunner = new QueryRunner();
        Connection conn = null;
        Map<String, Object> map = null;
        try {
            conn = dataSource.getConnection();
            if (params == null) {
                map = queryRunner.query(conn, sql, new MapHandler());
            } else {
                map = queryRunner.query(conn, sql, new MapHandler(), params);
            }
        } catch (SQLException e) {
            LOG.error("Error occured while attempting to query data", e);
        } finally {
            if (conn != null) {
                DbUtils.closeQuietly(conn);
            }
        }
        return map;
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     *
     * @param sql        sql语句
     * @param columnName 列名
     * @return 结果对象
     */
    public Object findBy(String sql, String columnName) {
        return findBy(sql, columnName, null);
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     *
     * @param sql        sql语句
     * @param columnName 列名
     * @param param      参数
     * @return 结果对象
     */
    public Object findBy(String sql, String columnName, Object param) {
        return findBy(sql, columnName, new Object[]{param});
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     *
     * @param sql        sql语句
     * @param columnName 列名
     * @param params     参数数组
     * @return 结果对象
     */
    public Object findBy(String sql, String columnName, Object[] params) {
        queryRunner = new QueryRunner();
        Connection conn = null;
        Object object = null;
        try {
            conn = dataSource.getConnection();
            if (params == null) {
                object = queryRunner.query(conn, sql, new ScalarHandler(columnName));
            } else {
                object = queryRunner.query(conn, sql, new ScalarHandler(columnName), params);
            }
        } catch (SQLException e) {
            LOG.error("Error occured while attempting to query data", e);
        } finally {
            if (conn != null) {
                DbUtils.closeQuietly(conn);
            }
        }
        return object;
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     *
     * @param sql         sql语句
     * @param columnIndex 列索引
     * @return 结果对象
     */
    public Object findBy(String sql, int columnIndex) {
        return findBy(sql, columnIndex, null);
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     *
     * @param sql         sql语句
     * @param columnIndex 列索引
     * @param param       参数
     * @return 结果对象
     */
    public Object findBy(String sql, int columnIndex, Object param) {
        return findBy(sql, columnIndex, new Object[]{param});
    }

    /**
     * 查询某一条记录，并将指定列的数据转换为Object
     *
     * @param sql         sql语句
     * @param columnIndex 列索引
     * @param params      参数数组
     * @return 结果对象
     */
    public Object findBy(String sql, int columnIndex, Object[] params) {
        queryRunner = new QueryRunner();
        Connection conn = null;
        Object object = null;
        try {
            conn = dataSource.getConnection();
            if (params == null) {
                object = queryRunner.query(conn, sql, new ScalarHandler(columnIndex));
            } else {
                object = queryRunner.query(conn, sql, new ScalarHandler(columnIndex), params);
            }
        } catch (SQLException e) {
            LOG.error("Error occured while attempting to query data", e);
        } finally {
            if (conn != null) {
                DbUtils.closeQuietly(conn);
            }
        }
        return object;
    }

    /**
     * 查询记录总条数
     *
     * @param sql sql语句
     * @return 记录总数
     */
    public int getCount(String sql) {
        return getCount(sql, null);
    }

    /**
     * 查询记录总条数
     *
     * @param sql   sql语句
     * @param param 参数
     * @return 记录总数
     */
    public int getCount(String sql, Object param) {
        return getCount(sql, new Object[]{param});
    }

    /**
     * 查询记录总条数
     *
     * @param sql    sql语句
     * @param params 参数数组
     * @return 记录总数
     */
    public int getCount(String sql, Object[] params) {
        String newSql = "select count(1) from (" + sql + ") _c";
        if (params == null) {
            return ((Long) findBy(newSql, 1)).intValue();
        } else {
            return ((Long) findBy(newSql, 1, params)).intValue();
        }
    }

    private int getBeginPage(int beginPage, int pageSize, int count) {
        if (count == 0) {
            return 1;
        }
        int newCurrentPage = beginPage;
        if (beginPage > 1) {
            if ((beginPage - 1) * pageSize >= count) {
                newCurrentPage = (int) (Math.ceil((count * 1.0) / pageSize));
            }
        }
        return newCurrentPage;
    }
}
