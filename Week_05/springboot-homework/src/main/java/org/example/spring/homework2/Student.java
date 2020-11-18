package org.example.spring.homework2;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * ClassName:Studnt
 * Package:org.example.spring.homework2
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/18 23:59
 */
@Data
@ToString
@Builder
public class Student {
    private int id;
    private String name;
    private int age;
}
