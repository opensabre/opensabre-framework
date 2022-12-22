package io.github.opensabre.common.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrivateHelperTest {

    private final PrivateHelper instance = PrivateHelper.getInstance();
    private PrivateObject privateObject;

    @BeforeEach
    public void before() {
        this.privateObject = new PrivateObject();
    }

    @Test
    public void testSetPrivateField_假如对象有一个私有成员变量_当通过该方法给私有成员变量赋值_那么可以赋值成功() throws Exception {
        this.instance.setPrivateField(privateObject, "code", "123456");
        assertEquals("123456", privateObject.getCode());
    }

    @Test
    public void testInvokeMethod_假如对象有一个有参私有方法_当通过该方法调用私有方法_那么可以调用成功并返回结果() {
        Method method = this.instance.findMethod(privateObject, "changeCode", String.class);
        String code = (String) this.instance.invokePrivateMethod(privateObject, method, "abcef");
        assertEquals("abcef", code);
    }

    @Test
    public void testInvokeMethod_假如对象有一个无参私有方法_当通过该方法调用私有方法_那么可以调用成功() {
        Method method = this.instance.findMethod(privateObject, "changeCode");
        this.instance.invokePrivateMethod(privateObject, method);
        assertEquals("aaaaa", privateObject.getCode());
    }

    @Test
    void testFindMethod_假如对象有一个无参私有方法_当通过该方法获取私有方法的信息_那么可以调用成功并返回结果() {
        Method method = this.instance.findMethod(privateObject, "changeCode");
        assertEquals("changeCode", method.getName());
        assertEquals(void.class, method.getReturnType());
    }

    @Test
    void testFindMethodWithArgs_假如对象有一个有参私有方法_当通过该方法获取私有方法信息_那么可以调用成功并返回结果() {
        Method method = this.instance.findMethod(privateObject, "changeCode", String.class);
        assertEquals("changeCode", method.getName());
        assertEquals(String.class, method.getReturnType());
    }
}