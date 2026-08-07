package io.github.opensabre.governance.dictionary.validation;

import io.github.opensabre.governance.dictionary.DictionaryService;
import io.github.opensabre.governance.dictionary.DictionaryUnavailableException;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DictionaryValueValidatorTest {

    private DictionaryService dictionaryService;
    private DictionaryValueValidator validator;

    @BeforeEach
    void setUp() {
        dictionaryService = mock(DictionaryService.class);
        validator = new DictionaryValueValidator(dictionaryService);
        validator.initialize(TestRequest.class.getDeclaredFields()[0].getAnnotation(DictionaryValue.class));
    }

    @Test
    void acceptsNullAndBlankValuesForStandardConstraintsToHandle() {
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("  ", null)).isTrue();
        verifyNoInteractions(dictionaryService);
    }

    @Test
    void acceptsEnabledDictionaryValue() {
        when(dictionaryService.contains("gender", "F")).thenReturn(true);

        assertThat(validator.isValid("F", null)).isTrue();
    }

    @Test
    void rejectsValueOutsideEnabledDictionaryItems() {
        when(dictionaryService.contains("gender", "UNKNOWN")).thenReturn(false);

        assertThat(validator.isValid("UNKNOWN", null)).isFalse();
    }

    @Test
    void preservesDictionaryUnavailableFailure() {
        DictionaryUnavailableException unavailable =
                new DictionaryUnavailableException("gender", new IllegalStateException("sysadmin unavailable"));
        when(dictionaryService.contains("gender", "F")).thenThrow(unavailable);

        assertThatThrownBy(() -> validator.isValid("F", null)).isSameAs(unavailable);
    }

    @Test
    void integratesWithSpringConstraintValidatorFactory() {
        when(dictionaryService.contains("gender", "UNKNOWN")).thenReturn(false);
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("dictionaryService", dictionaryService);
        LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.setConstraintValidatorFactory(new SpringConstraintValidatorFactory(beanFactory));
        validatorFactory.afterPropertiesSet();

        Set<ConstraintViolation<TestRequest>> violations =
                validatorFactory.validate(new TestRequest("UNKNOWN"));

        assertThat(violations).singleElement()
                .extracting(ConstraintViolation::getMessage)
                .isEqualTo("必须是字典 gender 中的启用项");
        validatorFactory.close();
    }

    @Test
    void springValidationWrapsDictionaryUnavailableForGlobalHandling() {
        DictionaryUnavailableException unavailable =
                new DictionaryUnavailableException("gender", new IllegalStateException("sysadmin unavailable"));
        when(dictionaryService.contains("gender", "F")).thenThrow(unavailable);
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("dictionaryService", dictionaryService);
        LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.setConstraintValidatorFactory(new SpringConstraintValidatorFactory(beanFactory));
        validatorFactory.afterPropertiesSet();

        assertThatThrownBy(() -> validatorFactory.validate(new TestRequest("F")))
                .isInstanceOf(jakarta.validation.ValidationException.class)
                .hasCauseInstanceOf(DictionaryUnavailableException.class);
        validatorFactory.close();
    }

    private static class TestRequest {
        @DictionaryValue("gender")
        private String gender;

        private TestRequest() {
        }

        private TestRequest(String gender) {
            this.gender = gender;
        }
    }
}
