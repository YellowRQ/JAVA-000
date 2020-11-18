package org.example.spring.homework2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * ClassName:StudentDao
 * Package:org.example.spring.homework2
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/19 0:00
 */
@Slf4j
@Repository
public class StudentDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查
     */
    public void getData(){
        Long aLong = jdbcTemplate.queryForObject("select count(*) from t_student ", Long.class);
        log.info("总数共有： {}", aLong);

        List<Student> studentList = jdbcTemplate.query("select * from t_student", new RowMapper<Student>() {
            @Override
            public Student mapRow(ResultSet resultSet, int i) throws SQLException {
                return Student.builder()
                        .id(resultSet.getInt(1))
                        .name(resultSet.getString(2))
                        .age(resultSet.getInt(3))
                        .build();
            }
        });
        log.info("学生如下： {}", studentList);
    }

    /**
     * 批量插入
     */
    public void batchInsert() {
        jdbcTemplate.batchUpdate("insert into t_student(name, age) values (?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, "张三"+i);
                        ps.setString(2, "18");
                    }

                    @Override
                    public int getBatchSize() {
                        return 3;
                    }
                });
    }

}
