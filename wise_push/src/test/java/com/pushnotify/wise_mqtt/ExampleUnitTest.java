package com.pushnotify.wise_mqtt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() {
        String s1 = "1234\\1111\\111";
        String json = "\\";
        boolean b = s1.contains(json);
        String replace = s1.replace("\\", "");
        boolean b1 = b;
    }

    public String obj1 = "obj1";
    public String obj2 = "obj2";


    class Lock implements Runnable {

        @Override
        public void run() {

        }
    }

}