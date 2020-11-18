package org.example.spring.homework2.configStart;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

/**
 * ClassName:School
 * Package:org.example.spring.homework2.configStart
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/19 0:27
 */
@Data
public class School implements ISchool {

    Klass klass;

    Student student;

    @Override
    public void ding(){
        System.out.println("Class1 have " + this.klass.getStudents().size() + " students and one is " + this.student);

    }

}
