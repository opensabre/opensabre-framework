package io.github.opensabre.governance.dictionary;

import org.springframework.core.io.ResourceLoader;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 从应用包中发现标准字典枚举并转换为现有 {@link DictionaryProvider} 模型。
 */
public final class ClasspathDictionaryEnumProvider implements DictionaryProvider {

    private final ResourceLoader resourceLoader;
    private final List<String> basePackages;

    public ClasspathDictionaryEnumProvider(ResourceLoader resourceLoader, Collection<String> basePackages) {
        this.resourceLoader = resourceLoader;
        this.basePackages = basePackages == null
                ? List.of()
                : basePackages.stream()
                        .filter(packageName -> packageName != null && !packageName.isBlank())
                        .map(String::trim)
                        .distinct()
                        .sorted()
                        .toList();
    }

    @Override
    public Collection<DictionaryDefinition> dictionaries() {
        if (basePackages.isEmpty()) {
            return List.of();
        }

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(OpenSabreDictionary.class));
        Set<String> classNames = new LinkedHashSet<>();
        for (String basePackage : basePackages) {
            scanner.findCandidateComponents(basePackage).stream()
                    .map(beanDefinition -> beanDefinition.getBeanClassName())
                    .filter(className -> className != null)
                    .forEach(classNames::add);
        }

        return classNames.stream()
                .sorted()
                .map(this::toDictionaryDefinition)
                .toList();
    }

    private DictionaryDefinition toDictionaryDefinition(String className) {
        try {
            Class<?> enumType = ClassUtils.forName(className, resourceLoader.getClassLoader());
            OpenSabreDictionary metadata = enumType.getAnnotation(OpenSabreDictionary.class);
            if (!enumType.isEnum() || !DictionaryEnum.class.isAssignableFrom(enumType)) {
                throw new IllegalStateException(
                        "@OpenSabreDictionary requires an enum implementing DictionaryEnum: " + className);
            }
            return createDefinition(metadata, enumType);
        } catch (ClassNotFoundException | LinkageError exception) {
            throw new IllegalStateException("Failed to load dictionary enum " + className, exception);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private DictionaryDefinition createDefinition(OpenSabreDictionary metadata, Class<?> enumType) {
        return DictionaryDefinition.of(metadata.code(), metadata.name(), (Class) enumType);
    }
}
