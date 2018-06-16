package org.EmployeeService.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.EmployeeService.entity.Employee;
import org.EmployeeService.mq.RabbitMqPublisher;
import org.EmployeeService.repository.EmployeeRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.EmployeeService.mq.RabbitEmployee.*;
import static org.EmployeeService.mq.RabbitMessage.createMessage;


@RestController
@RequestMapping("/api")
public class AppController {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @RequestMapping(value = "/findBy", method=RequestMethod.GET)
    public List<Employee> findByFullName(@RequestParam(value="fullName") String fullName){
        return employeeRepository.findByFullName(fullName);
    }

    @RequestMapping(value = "/findAll", method=RequestMethod.GET)
    public List<Employee> findAll(){
        return employeeRepository.findAll();
    }

    @RequestMapping(value = "/init", method=RequestMethod.GET)
    public void init(){
        Stream.of(new Employee("Alexander"), new Employee("Nikita"), new Employee("Alesya"))
                .forEach(employee -> {
                    String jsonEmployee= null;
                    try {
                        jsonEmployee = new ObjectMapper().writeValueAsString(employeeRepository.save(employee));
                        System.out.println("create message with employee "+jsonEmployee);
                        Message msg = createMessage(EMPLOYEE_CREATED_EVENT, jsonEmployee);
                        System.out.println("Send message "+msg+" using rabbit template "+rabbitTemplate);
                        rabbitTemplate.setExchange(FROM_SERVICE_EMPLOYEE_FANOUT_EXCHANGE);
                        rabbitTemplate.send(msg);
                        //rabbitTemplate.convertAndSend(jsonEmployee);
                        //new RabbitMqPublisher().send("employee-event-fanout-exchange", FROM_SERVICE_EMPLOYEE_EVENT_QUEUE,jsonEmployee );
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                });
        
    }


    @RequestMapping(value = "/addEmployee", method = RequestMethod.POST)
    public ResponseEntity<?> createEmployee(@RequestBody Employee employee){
        if (employee.getFullName() == null) return ResponseEntity.unprocessableEntity().build();

        Employee newEmployee = employeeRepository.save(employee);
        //template.setExchange("exchange-example-3");

/*
        try {
            String jsonEmployee= new ObjectMapper().writeValueAsString(newEmployee);
            template.setExchange("employee-created-fanout");
            template.convertAndSend(EMPLOYEE_CREATED_QUEUE, jsonEmployee);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

*/
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEmployee)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @RequestMapping(value = "/changeEmployee/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> changeEmployee(@PathVariable long id, @RequestBody Employee employee){
        if (employee.getFullName() == null) return ResponseEntity.unprocessableEntity().build();

        Optional<Employee> employeeForChange = employeeRepository.findById(id);
        if (employeeForChange.isPresent()){
            Employee oldEmpl = new Employee(employeeForChange.get());
            //oldEmpl = employeeForChange.get();
            employeeForChange.get().setFullName(employee.getFullName());
            Employee changedEmployee = employeeRepository.save(employeeForChange.get());
            /*
            try {
                String jsonEmployeeForChange= new ObjectMapper().writeValueAsString(oldEmpl);
                String jsonEmployeeChanged= new ObjectMapper().writeValueAsString(changedEmployee);
                template.setExchange("employee-updated-fanout");
                template.convertAndSend(EMPLOYEE_UPDATED_QUEUE, jsonEmployeeForChange + " to " +jsonEmployeeChanged);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
*/
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .buildAndExpand(employeeForChange.get().getId()).toUri();

            return ResponseEntity.created(location).build();
        }else{
            return ResponseEntity.notFound().build();

        }

    }

    @RequestMapping(value = "/deleteEmployee/{id}", method=RequestMethod.DELETE)
    public ResponseEntity<?> deleteEmployee(@PathVariable long id){
        Optional<Employee> employeeForRemove = employeeRepository.findById(id);
        if (employeeForRemove.isPresent()){
            employeeRepository.delete(employeeForRemove.get());

            if (!employeeRepository.findById(employeeForRemove.get().getId()).isPresent()){
                try {
                    new RabbitMqPublisher().sendDeletedMessage(rabbitTemplate,employeeForRemove.get());

                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.notFound().build();
        }

    }




}
