package boncp;

import com.jolbox.bonecp.BoneCPDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-7-26
 * Time: 下午4:43
 * 测试BoneCP连接池的速度，插入10000条数据
 */
public class ExampleJDBC {
    private JdbcTemplate jdbcTemplate;
    private static Logger log = LoggerFactory.getLogger(ExampleJDBC.class);

    public static void main(String[] args) {
        new ExampleJDBC().run();
    }

    /**
     * 测试BonCP插入10000条数据时间：174秒
     */
    public void run() {
        long start = System.currentTimeMillis();
        log.info("=================BonCP开始插入10000条数据测试================ at "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(start)));
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        jdbcTemplate = (JdbcTemplate) ctx.getBean("jdbcBoneCPTemplate");
        final String sql = "INSERT INTO record(description,content) VALUES(?,?)";
        // 批量更新
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return 10000;
            }

            public void setValues(PreparedStatement ps, int index) throws SQLException {
                ps.setString(1, "BonCP测试description" + index);
                ps.setString(2, "BonCP测试content" + index);
            }
        });
        // 关闭连接池
        ((BoneCPDataSource)jdbcTemplate.getDataSource()).close();
        long end = System.currentTimeMillis();
        log.info("=================BonCP结束插入10000条数据测试=============== at "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(end)));
        log.info("BonCP spend time total ： " + (end - start)/ 1000 + "秒");
    }
}

