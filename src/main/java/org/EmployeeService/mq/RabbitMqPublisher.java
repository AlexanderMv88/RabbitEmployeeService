package org.EmployeeService.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.EmployeeService.entity.Employee;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.EmployeeService.mq.RabbitEmployee.EMPLOYEE_DELETED_EVENT;
import static org.EmployeeService.mq.RabbitEmployee.FROM_SERVICE_EMPLOYEE_FANOUT_EXCHANGE;
import static org.EmployeeService.mq.RabbitMessage.createMessage;

public class RabbitMqPublisher {


    public void sendDeletedMessage(RabbitTemplate rabbitTemplate, Employee employee) throws JsonProcessingException {
        String jsonEmployeeForRemove= new ObjectMapper().writeValueAsString(employee);
        Message msg = createMessage(EMPLOYEE_DELETED_EVENT, jsonEmployeeForRemove);
        rabbitTemplate.setExchange(FROM_SERVICE_EMPLOYEE_FANOUT_EXCHANGE);
        rabbitTemplate.send(msg);
    }
}
