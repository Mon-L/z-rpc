package cn.zcn.rpc.example;

import java.util.concurrent.ThreadLocalRandom;

public class StudentServiceImpl implements StudentService {

    @Override
    public Student getStudentByName(String name) {
        return new Student(name, ThreadLocalRandom.current().nextInt(10, 30));
    }
}
