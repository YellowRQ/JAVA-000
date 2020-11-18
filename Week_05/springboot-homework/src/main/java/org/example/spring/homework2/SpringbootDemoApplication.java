package org.example.spring.homework2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * ClassName:SpringbootApplication
 * Package:org.example.spring.homework2
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/17 23:31
 */
@SpringBootApplication
@Slf4j
public class SpringbootDemoApplication  implements CommandLineRunner {

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(SpringbootDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        showConnection();
        studentDao.batchInsert();
        studentDao.getData();
    }

    public void showConnection() throws SQLException {
        log.info(dataSource.toString());
        Connection conn = dataSource.getConnection();
        log.info(conn.toString());
        conn.close();
    }
}
