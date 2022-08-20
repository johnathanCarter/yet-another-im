package com.method.im;

import static org.mockito.ArgumentMatchers.booleanThat;

import com.method.im.utils.MD5Utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ImApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("hello world");
    }

    @Test
    public void Md5() {
        boolean result = MD5Utils.verify("root", "c9ec1a457180442c1769ed8ee3e71f51315e213c19324208");
        System.out.println("result: " + result);
    }

    public boolean isPass(int n) {
        return n % 2 == 0;
    }

    @Test
    public void test01() {
        System.out.println(isPass(14));
    }

}
