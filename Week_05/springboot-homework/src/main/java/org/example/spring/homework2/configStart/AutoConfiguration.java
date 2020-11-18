package org.example.spring.homework2.configStart;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName:AutoConfiguration
 * Package:org.example.spring.homework2.configStart
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/11/19 0:28
 */
@Configuration
@AutoConfigureBefore(Student.class)
@ConditionalOnProperty(prefix = "student", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnResource(resources = "META-INF/spring.factories")
@EnableConfigurationProperties(Student.class)
public class AutoConfiguration {

    @Bean
    public ISchool createSchool() {
        School school = new School();
        List<Klass> klassList = new ArrayList<>();
        school.setKlass(createKlass());
        school.setStudent(createStudent());
        return school;
    }

    @Bean
    public Student createStudent() {
        return new Student();
    }

    @Bean
    public Klass createKlass() {
        Klass klass = new Klass();
        List<Student> students = new ArrayList<>();
        students.add(createStudent());
        klass.setStudents(students);
        return klass;
    }
}
