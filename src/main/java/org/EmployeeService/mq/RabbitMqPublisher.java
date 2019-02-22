package org.EmployeeService.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.EmployeeService.entity.Employee;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.EmployeeService.mq.RabbitEmployee.*;
import static org.EmployeeService.mq.RabbitMessage.createMessage;

public class RabbitMqPublisher {

    public void sendDeletedMessage(RabbitTemplate rabbitTemplate, Employee employee) throws JsonProcessingException {
        String jsonEmployeeForRemove= new ObjectMapper().writeValueAsString(employee);
        Message msg = createMessage(EMPLOYEE_DELETED_EVENT, jsonEmployeeForRemove);
        rabbitTemplate.setExchange(FROM_SERVICE_EMPLOYEE_FANOUT_EXCHANGE);
        rabbitTemplate.send(msg);
    }

    public void sendUpdatedMessage(RabbitTemplate rabbitTemplate, Employee oldEmployee, Employee newEmployee) throws JsonProcessingException {
        List<Employee> employees = new ArrayList<Employee>();
        employees.add(oldEmployee);
        employees.add(newEmployee);
        String jsonEmployeeForUpdate= new ObjectMapper().writeValueAsString(employees);
        Message msg = createMessage(EMPLOYEE_UPDATED_EVENT, jsonEmployeeForUpdate);
        rabbitTemplate.setExchange(FROM_SERVICE_EMPLOYEE_FANOUT_EXCHANGE);
        rabbitTemplate.send(msg);
    }

    public void sendCreatedMessage(RabbitTemplate rabbitTemplate, Employee employee) throws JsonProcessingException {
        String jsonEmployeeForInsert= new ObjectMapper().writeValueAsString(employee);
        Message msg = createMessage(EMPLOYEE_CREATED_EVENT, jsonEmployeeForInsert);
        rabbitTemplate.setExchange(FROM_SERVICE_EMPLOYEE_FANOUT_EXCHANGE);
        rabbitTemplate.send(msg);
    }
}
