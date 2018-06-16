package org.EmployeeService.entity;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class Employee implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String fullName;

    public Employee(Employee employee) {
        this.id = employee.getId();
        this.fullName=employee.getFullName();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Employee() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Employee employee = (Employee) o;

        if (id != employee.id) return false;
        return fullName != null ? fullName.equals(employee.fullName) : employee.fullName == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
        return result;
    }

    public Employee(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {

        return "Employee{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
