package cn.zcn.rpc.test.simple.service;

import cn.zcn.rpc.test.student.Student;
import cn.zcn.rpc.test.student.StudentService;

import java.util.concurrent.ThreadLocalRandom;

public class StudentServiceImpl implements StudentService {

    @Override
    public Student getStudentByName(String name) {
        //throw new IllegalArgumentException("sdfdaf");
        return new Student(name, ThreadLocalRandom.current().nextInt(10, 30));
    }
}
