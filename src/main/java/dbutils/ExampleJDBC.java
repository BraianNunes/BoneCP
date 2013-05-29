/*
 * Created on 13-5-25
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

import model.Student;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ProxyFactory;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.wrappers.SqlNullCheckedResultSet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * 利用DBUtils测试数据库的修改
 * <p/>
 * (2)org.apache.commons.dbutils.handlers
 * ArrayHandler ：将ResultSet中第一行的数据转化成对象数组
 * ArrayListHandler将ResultSet中所有的数据转化成List，List中存放的是Object[]
 * BeanHandler ：将ResultSet中第一行的数据转化成类对象
 * BeanListHandler ：将ResultSet中所有的数据转化成List，List中存放的是类对象
 * ColumnListHandler ：将ResultSet中某一列的数据存成List，List中存放的是Object对象
 * KeyedHandler ：将ResultSet中存成映射，key为某一列对应为Map。Map中存放的是数据
 * MapHandler ：将ResultSet中第一行的数据存成Map映射
 * MapListHandler ：将ResultSet中所有的数据存成List。List中存放的是Map
 * ScalarHandler ：将ResultSet中一条记录的其中某一列的数据存成Object
 * <p/>
 * (3)org.apache.commons.dbutils.wrappers
 * SqlNullCheckedResultSet ：该类是用来对sql语句执行完成之后的的数值进行null的替换。
 * StringTrimmedResultSet ：去除ResultSet中中字段的左右空格。Trim()
 *
 * @author XiongNeng
 * @version 1.0
 * @since 13-5-25
 */
public class ExampleJDBC {
    private static final Logger log = LoggerFactory.getLogger(ExampleJDBC.class);

    public static void main(String[] args) {
        JdbcUtil.initDataSoucePool();
        getBeanListData();
    }

    /**
     * BeanListHandler ：将ResultSet中所有的数据转化成List，List中存放的是类对象
     */
    public static void getBeanListData() {
        Connection conn = getConnection();
        QueryRunner qr = new QueryRunner();
        try {
            ResultSetHandler<Student> rsh = new BeanHandler(Student.class);
            Student usr = qr.query(conn, "SELECT id, name, gender, age, team_id as teamId FROM test_student WHERE id=1", rsh);
            System.out.println(StringUtils.center("findById", 50, '*'));
            System.out.println("id=" + usr.getId() + " name=" + usr.getName() + " gender=" + usr.getGender());

            List<Student> results = (List<Student>) qr.query(conn, "SELECT id, name, gender, age, team_id as teamId FROM test_student LIMIT 10",
                    new BeanListHandler(Student.class));
            System.out.println(StringUtils.center("findAll", 50, '*'));
            for (Student result : results) {
                System.out.println("id=" + result.getId() + "  name=" + result.getName() + "  gender=" + result.getGender());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    /**
     * MapListHandler ：将ResultSet中所有的数据存成List。List中存放的是Map
     */
    public static void getMapListData() {
        Connection conn = getConnection();
        QueryRunner qr = new QueryRunner();
        try {
            List results = (List) qr.query(conn, "SELECT id, name, gender, age, team_id FROM test_student", new MapListHandler());
            for (Object result : results) {
                Map map = (Map) result;
                System.out.println("id=" + map.get("id") + " name=" + map.get("name") + " gender=" + map.get("gender"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    /**
     * 新增和更新例子
     */
    public static void insertAndUpdateData() {
        Connection conn = getConnection();
        QueryRunner qr = new QueryRunner();
        try {
            //创建一个数组来存要insert的数据
            Object[] insertParams = {"John Doe", "男", 12, 3};
            int inserts = qr.update(conn, "INSERT INTO test_student(name,gender,age,team_id) VALUES (?,?,?,?)", insertParams);
            System.out.println("inserted " + inserts + " data");

            Object[] updateParams = {"John Doe Update", "John Doe"};
            int updates = qr.update(conn, "UPDATE test_student SET name=? WHERE name=?", updateParams);
            System.out.println("updated " + updates + " data");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    /**
     * Unlike some other classes in DbUtils, this class(SqlNullCheckedResultSet) is NOT thread-safe.
     */
    public static void findUseSqlNullCheckedResultSet() {
        Connection conn = getConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name, gender, age, team_id as teamId FROM test_student");
            SqlNullCheckedResultSet wrapper = new SqlNullCheckedResultSet(rs);
            wrapper.setNullString("N/A"); // Set null string
            rs = ProxyFactory.instance().createResultSet(wrapper);

            while (rs.next()) {
                System.out.println("id=" + rs.getInt("id") + " username=" + rs.getString("name")
                        + " gender=" + rs.getString("gender"));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    /**
     * *数据库连接***
     */
    public static Connection getConnection() {
        try {
            return JdbcUtil.getConnection();
        } catch (Exception e) {
            log.error("获取数据库连接错误", e);
            return null;
        }
    }
}
