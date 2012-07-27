package boncp;

import com.jolbox.bonecp.BoneCPDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-7-26
 * Time: 下午4:46
 * To change this template use File | Settings | File Templates
 */
public class JdbcUtil {

    private static Logger log = LoggerFactory.getLogger(JdbcUtil.class);
    private static BoneCPDataSource dataSource;

    /**
     * 初始化连接池(Spring方式)
     *
     * @throws Exception
     */
    public static void initDataSource() throws Exception {
        if (dataSource == null) {
            log.info("The first time to init origin pool");
            dataSource = getSpringConnection();
        }
    }

    /**
     * 初始化连接池(手动方式)
     *
     * @throws Exception
     */
    public static void initManualDataSource() throws Exception {
        if (dataSource == null) {
            log.info("The first time to init origin pool");
            // load the DB driver
            Class.forName("com.mysql.jdbc.Driver");
            // create a new datasource object
            dataSource = new BoneCPDataSource();
            // set the JDBC url
            dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8");
            // set the username
            dataSource.setUsername("root");
            // set the password
            dataSource.setPassword("123456");
            // (other config options here)
            dataSource.setAcquireIncrement(10);
        }
    }

    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     * @throws Exception
     */
    public static Connection getConnection() throws Exception {
        return (dataSource != null) ? dataSource.getConnection() : null;
    }

    /**
     * 关闭数据库连接池
     *
     * @throws Exception
     */
    public static void closeDataSource() throws Exception {
        dataSource.close();
    }

    private static BoneCPDataSource getSpringConnection() throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        return (BoneCPDataSource) ctx.getBean("dataSource");
    }
}
