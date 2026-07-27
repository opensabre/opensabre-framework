package io.github.opensabre.governance.dictionary;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.governance.client.SysadminGovernanceClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * 使用 opensabre-starter-cache 本地 shortTime 区域缓存字典。
 */
@Slf4j
@RequiredArgsConstructor
public class JetCacheDictionaryService implements DictionaryService {

    private static final String CACHE_NAME = "governance:dictionary:";

    private final CacheManager cacheManager;
    private final SysadminGovernanceClient client;
    private Cache<String, List<DictionaryItem>> cache;

    @PostConstruct
    public void init() {
        QuickConfig config = QuickConfig.newBuilder("shortTime", CACHE_NAME)
                .cacheType(CacheType.LOCAL)
                .penetrationProtect(true)
                .build();
        cache = cacheManager.getOrCreateCache(config);
    }

    @Override
    public List<DictionaryItem> items(String dictCode) {
        if (dictCode == null || dictCode.isBlank()) {
            return List.of();
        }
        List<DictionaryItem> cached = cache.get(dictCode);
        if (cached != null) {
            return cached.stream().filter(DictionaryItem::enabled).toList();
        }
        try {
            Result<List<DictionaryItem>> response = client.getDictionaryItems(dictCode);
            List<DictionaryItem> loaded = response == null || response.getData() == null
                    ? List.of() : List.copyOf(response.getData());
            cache.put(dictCode, loaded);
            return loaded.stream().filter(DictionaryItem::enabled).toList();
        } catch (Exception exception) {
            log.warn("Failed to load dictionary {} from sysadmin", dictCode, exception);
            throw new DictionaryUnavailableException(dictCode, exception);
        }
    }

    @Override
    public Optional<String> labelOf(String dictCode, Object value) {
        String expected = String.valueOf(value);
        List<DictionaryItem> all = cache.get(dictCode);
        if (all == null) {
            items(dictCode);
            all = cache.get(dictCode);
        }
        return Optional.ofNullable(all).orElseGet(List::of).stream()
                .filter(item -> item.value().equals(expected))
                .map(DictionaryItem::label)
                .findFirst();
    }

    @Override
    public boolean contains(String dictCode, Object value) {
        String expected = String.valueOf(value);
        return items(dictCode).stream().anyMatch(item -> item.value().equals(expected));
    }

    @Override
    public void refresh(String dictCode) {
        if (dictCode != null) {
            cache.remove(dictCode);
        }
    }
}
