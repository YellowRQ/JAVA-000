package org.example.spring.homework1.aspectJ;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * ClassName:Klass
 * Package:org.example.spring.homework1.aspectJ
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/18 4:59
 */
@Data
@ToString
public class Klass {

    private int id;
    private List<Student> students;

    public void study() {
        System.out.println(getStudents() + "在学习...");
    }
}
