package com.example.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import com.example.userservice.client.PermissionServiceClient;

@SpringBootTest(properties = {"seata.enabled=false"})
class UserServiceApplicationTests {

    @MockBean
    private RocketMQTemplate rocketMQTemplate;

    @MockBean
    private PermissionServiceClient permissionServiceClient;

    @Test
    void contextLoads() {
    }

}
