package org.EmployeeService;

import org.EmployeeService.entity.Employee;
import org.EmployeeService.repository.EmployeeRepository;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EmployeeApiApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmployeeRepositoryTests {
    @Autowired
    EmployeeRepository employeeRepository;

    @Test
    public void test1JpaCreate() {
        //Insert
        Stream.of(new Employee("Alexander"), new Employee("Nikita"), new Employee("Alesya"))
                .forEach(employee -> {
                    employeeRepository.save(employee);
                });
        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees != null).isTrue();
        assertThat(employees.size() == 3).isTrue();
    }

    @Test
    public void test2JpaFind() {
        //Select
        Employee employee = employeeRepository.findByFullName("Alexander").get(0);
        assertThat(employee.getFullName()).isEqualTo("Alexander");
    }


    @Test
    public void test3JpaChange() {
        //Update
        Employee employee = employeeRepository.findByFullName("Alexander").get(0);
        employee.setFullName("AlexanderMv");
        employeeRepository.save(employee);
        Employee changedEmployee = employeeRepository.findByFullName("AlexanderMv").get(0);
        assertThat(changedEmployee).isEqualTo(employee);
    }

    @Test
    public void test4JpaDelete() {
        List<Employee> chatUsersForDelete = employeeRepository.findAll();
        employeeRepository.deleteAll(chatUsersForDelete);
        assertThat(employeeRepository.findAll().size() == 0).isTrue();
    }
}
