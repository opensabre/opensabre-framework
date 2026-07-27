package io.github.opensabre.governance.dictionary;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.governance.client.SysadminGovernanceClient;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JetCacheDictionaryServiceTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void loadsOnceAndUsesJetCacheAfterward() {
        Map<String, List<DictionaryItem>> values = new HashMap<>();
        Cache<String, List<DictionaryItem>> cache = (Cache<String, List<DictionaryItem>>) Proxy.newProxyInstance(
                Cache.class.getClassLoader(), new Class[]{Cache.class}, (proxy, method, args) -> {
                    return switch (method.getName()) {
                        case "get" -> values.get(args[0]);
                        case "put" -> {
                            values.put((String) args[0], (List<DictionaryItem>) args[1]);
                            yield null;
                        }
                        case "remove" -> values.remove(args[0]) != null;
                        default -> null;
                    };
                });
        CacheManager cacheManager = (CacheManager) Proxy.newProxyInstance(
                CacheManager.class.getClassLoader(), new Class[]{CacheManager.class},
                (proxy, method, args) -> method.getName().equals("getOrCreateCache") ? cache : null);
        AtomicInteger loads = new AtomicInteger();
        SysadminGovernanceClient client = (SysadminGovernanceClient) Proxy.newProxyInstance(
                SysadminGovernanceClient.class.getClassLoader(), new Class[]{SysadminGovernanceClient.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getDictionaryItems")) {
                        loads.incrementAndGet();
                        return Result.success(List.of(new DictionaryItem("1", "正常", 1, "S", 1)));
                    }
                    return null;
                });
        JetCacheDictionaryService service = new JetCacheDictionaryService(cacheManager, client);
        service.init();

        assertEquals("正常", service.items("sample_status").getFirst().label());
        assertEquals("正常", service.items("sample_status").getFirst().label());
        assertEquals(1, loads.get());
        assertEquals(1, values.size());
    }

    @Test
    void distinguishesRemoteFailureFromAnEmptyDictionary() {
        CacheManager cacheManager = proxy(CacheManager.class,
                (cacheManagerProxy, method, args) -> method.getName().equals("getOrCreateCache")
                        ? proxy(Cache.class, (cacheProxy, cacheMethod, cacheArgs) -> null)
                        : null);
        SysadminGovernanceClient client = proxy(SysadminGovernanceClient.class,
                (clientProxy, method, args) -> {
                    throw new IllegalStateException("sysadmin unavailable");
                });
        JetCacheDictionaryService service = new JetCacheDictionaryService(cacheManager, client);
        service.init();

        assertThrows(DictionaryUnavailableException.class, () -> service.items("sample_status"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
    }
}
