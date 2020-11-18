package org.example.spring.homework1.aspectJ;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

/**
 * ClassName:School
 * Package:org.example.spring.homework1
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/18 4:56
 */
@Data
public class School implements ISchool {

    @Autowired(required = true)
    private Klass klass;

    @Resource(name = "student001")
    private Student stu;

    @Override
    public void ding() {
        System.out.println("Class1 have " + this.klass.getStudents().size() + " students and one is " + this.stu);
    }
}
