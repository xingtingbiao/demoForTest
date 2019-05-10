package com.test;

import com.xingtb.Person;
import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestThreadLocal {
    Lock lock = new ReentrantLock();

    @Test
    public void test() {
        ThreadLocal<Person> local = ThreadLocal.withInitial(Person::new);
    }

    @Test
    public void testThread() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> say(Thread.currentThread().getName())).start();
        }
        Thread.sleep(100);
    }

    private void say(String name) {
        lock.lock();
        for (int i = 0; i < 5; i++) {
            System.out.println("I am " + name);
        }
        lock.unlock();
    }
}
