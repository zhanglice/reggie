package com.zhang;

import org.junit.jupiter.api.Test;

public class UploadFileTest {

    @Test
    public void test1(){  //后缀截取测试
        String fileName = "asasdada.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);
    }
}
