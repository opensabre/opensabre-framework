package io.github.opensabre.governance.dictionary;

import io.github.opensabre.governance.config.GovernanceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * 应用就绪后预热配置指定的字典；单个字典失败不影响其他字典和应用启动。
 */
@Slf4j
@RequiredArgsConstructor
public class DictionaryPreloadListener {

    private final DictionaryService dictionaryService;
    private final GovernanceProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    public void preload() {
        for (String code : properties.getDictionary().getPreloadCodes()) {
            try {
                dictionaryService.items(code);
            } catch (Exception exception) {
                log.warn("Failed to preload dictionary {}", code, exception);
            }
        }
    }
}
