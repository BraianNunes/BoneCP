package c3p0;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import model.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-7-26
 * Time: 下午4:43
 * To change this template use File | Settings | File Templates
 */
public class ExampleJDBC {
    private JdbcTemplate jdbcTemplate;
    private static Logger log = LoggerFactory.getLogger(ExampleJDBC.class);

    public static void main(String[] args) {
        new ExampleJDBC().run();
    }

    /**
     * 测试C3P0插入10000条数据时间：168秒
     */
    public void run() {
        long start = System.currentTimeMillis();
        log.info("=================C3P0开始插入10000条数据测试================ at "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(start)));
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        jdbcTemplate = (JdbcTemplate) ctx.getBean("jdbcC3P0Template");
        final String sql = "INSERT INTO record(description,content) VALUES(?,?)";
        final Record[] paramList = new Record[10000];
        for (int i = 0; i < 10000; i++) {
            paramList[i] = new Record("C3P0测试description" + i, "C3P0测试content" + i);
        }
        // 批量更新
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return 10000;
            }

            public void setValues(PreparedStatement ps, int index) throws SQLException {
                ps.setString(1, paramList[index].getDescription());
                ps.setString(2, paramList[index].getContent());
            }
        });
        // 关闭连接池
        ((ComboPooledDataSource)jdbcTemplate.getDataSource()).close();
        long end = System.currentTimeMillis();
        log.info("=================C3P0结束插入10000条数据测试=============== at "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(end)));
        log.info("C3P0 spend time total ： " + (end - start)/ 1000 + "秒");
    }
}
