package cn.zcn.rpc.test.student;

import java.io.Serializable;

public class Student implements Serializable {

	private final String name;
	private final int age;

	public Student(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}
}
