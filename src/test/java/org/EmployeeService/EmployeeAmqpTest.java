package org.EmployeeService;

import org.EmployeeService.entity.Employee;
import org.EmployeeService.repository.EmployeeRepository;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.EmployeeService.mq.RabbitEmployee.*;
import static org.EmployeeService.mq.RabbitMessage.createMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EmployeeApiApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@AutoConfigureMockMvc
public class EmployeeAmqpTest {

    private HttpMessageConverter mappingJackson2HttpMessageConverter;


    private static boolean isCreated=false;
    private static boolean isUpdated=false;
    private static boolean isRemoved=false;

    //
    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }



    @Test
    public void test1Create() throws Exception {
        String jsonObj = json(new Employee("Дима"));

        Message msg = createMessage(EMPLOYEE_CREATE_EVENT, jsonObj);
        rabbitTemplate.setExchange(TO_SERVICE_EMPLOYEE_FANOUT_EXCHANGE);
        rabbitTemplate.send(msg);

        Thread.sleep(100);

        List<Employee> employees = employeeRepository.findByFullName("Дима");
        assertThat(employees != null).isTrue();
        assertThat(employees.size() == 1).isTrue();
        assertThat(employees.get(0).getFullName().equals("Дима")).isTrue();



    }

    @Test
    public void test2Update() throws Exception {
        Employee oldEmployee = employeeRepository.findByFullName("Дима").get(0);
        //String oldJsonObj = json(oldEmployee);

        Employee newEmployee = new Employee(oldEmployee);
        newEmployee.setFullName("Диман");
        List<Employee> employees = new ArrayList<Employee>();
        employees.add(oldEmployee);
        employees.add(newEmployee);
        //String newJsonObj = json(newEmployee);
        String jsonObjs = json(employees);


        Message msg = createMessage(EMPLOYEE_UPDATE_EVENT, jsonObjs);
        rabbitTemplate.setExchange(TO_SERVICE_EMPLOYEE_FANOUT_EXCHANGE);
        rabbitTemplate.send(msg);

        Thread.sleep(100);

        List<Employee> employeesFromDB = employeeRepository.findByFullName("Диман");
        assertThat(employeesFromDB != null).isTrue();
        assertThat(employeesFromDB.size() == 1).isTrue();
        assertThat(employeesFromDB.get(0).getFullName().equals("Диман")).isTrue();


    }


    @Test
    public void test3Remove() throws Exception {
        String jsonObj = json(new Employee("Диман"));

        Message msg = createMessage(EMPLOYEE_DELETE_EVENT, jsonObj);
        rabbitTemplate.setExchange(TO_SERVICE_EMPLOYEE_FANOUT_EXCHANGE);
        rabbitTemplate.send(msg);

        Thread.sleep(100);

        List<Employee> employees = employeeRepository.findByFullName("Дима");
        //assertThat(employees == null).isTrue();
        assertThat(employees.size() == 0).isTrue();
        //assertThat(employees.get(0).getFullName().equals("Диман")).isTrue();


    }







    @RabbitListener(queues = FROM_SERVICE_EMPLOYEE_EVENT_QUEUE)
    public void fromServiceEmployee(Message message){
        System.out.println("get message "+message);
        String action = (String) message.getMessageProperties().getHeaders().get("action");
        String body = null;
        try {
            body = new String(message.getBody(), "UTF-8");
            if (EMPLOYEE_CREATED_EVENT.equals(action)) {
                isCreated=true;

            }else if (EMPLOYEE_UPDATED_EVENT.equals(action)) {
                isUpdated=true;

            }else if (EMPLOYEE_DELETED_EVENT.equals(action)) {
                isRemoved=true;

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void test4CheckListenResults(){

        assertThat(isCreated).isTrue();
        assertThat(isUpdated).isTrue();
        assertThat(isRemoved).isTrue();



    }


}
