package io.github.opensabre.common.core.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class UserContextHolderTest {

    @Test
    void getInstance() {
        assertNotNull(UserContextHolder.getInstance());
        //单例，两次获取的同一个实例
        assertEquals(UserContextHolder.getInstance(), UserContextHolder.getInstance());
    }

    @Test
    void setContext() {
        UserContextHolder.getInstance().setContext(new HashMap<>());
    }

    @Test
    void getContext() {
        HashMap<String, String> map = new HashMap<>();
        UserContextHolder.getInstance().setContext(map);
        assertEquals(UserContextHolder.getInstance().getContext(), UserContextHolder.getInstance().getContext());
    }

    @Test
    void getContextNotSet() {
        assertNull(UserContextHolder.getInstance().getContext());
    }

    @Test
    void getUsername() {
        HashMap<String, String> map = new HashMap<>();
        map.put(UserContextHolder.getInstance().KEY_USERNAME, "zhangsan");
        UserContextHolder.getInstance().setContext(map);
        assertEquals(UserContextHolder.getInstance().getUsername(), "zhangsan");
    }

    @Test
    void clear() {
        HashMap<String, String> map = new HashMap<>();
        map.put(UserContextHolder.getInstance().KEY_USERNAME, "zhangsan");
        UserContextHolder.getInstance().setContext(map);
        UserContextHolder.getInstance().clear();
        assertNull(UserContextHolder.getInstance().getContext());
    }
}