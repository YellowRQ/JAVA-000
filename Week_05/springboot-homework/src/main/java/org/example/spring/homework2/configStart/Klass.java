package org.example.spring.homework2.configStart;

import lombok.Data;

import java.util.List;

/**
 * ClassName:Klass
 * Package:org.example.spring.homework2.configStart
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/19 0:26
 */
@Data
public class Klass {

    List<Student> students;

    public void dong(){
        System.out.println(this.getStudents());
    }

}
