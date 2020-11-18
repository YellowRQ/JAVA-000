package org.example.spring.homework2.configStart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * ClassName:Student
 * Package:org.example.spring.homework2.configStart
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/19 0:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "student")
public class Student implements Serializable {

    private int id;
    private String name;
}