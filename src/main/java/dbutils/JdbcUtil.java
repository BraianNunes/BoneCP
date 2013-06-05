package dbutils;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-7-11
 * Time: 上午10:20
 * JdbcUtil
 */
public class JdbcUtil {

    private static Logger log = LoggerFactory.getLogger(JdbcUtil.class);
    private static ComboPooledDataSource dataSource;

    public synchronized static ComboPooledDataSource initDataSourcePool() {
        if (dataSource == null) {
            log.info("The first time to init origin pool");
            dataSource = new ComboPooledDataSource("dataSource");
        }
        return dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     * @throws Exception
     */
    public static Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }

    /**
     * 关闭数据库连接池
     *
     * @throws Exception
     */
    public static void closeDataSource() throws Exception {
        dataSource.close();
    }
}