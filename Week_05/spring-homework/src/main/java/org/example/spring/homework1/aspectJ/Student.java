package org.example.spring.homework1.aspectJ;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;

/**
 * ClassName:Student
 * Package:org.example.spring.homework1.aspectJ
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/18 4:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Student implements Serializable {

    @Value(value = "100")
    private int id;

    @Value(value = "吴宣仪")
    private String name;

    public void init(){
        System.out.println("hello...........");
    }
}
