package com.recovery.ReceivablesGuard.diagnosis;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

class DiagnosisArchitectureTest {

    @Test
    void diagnosisClientShouldNotDependOnExecutionComponents() {

        Class<?> clazz =
                GeminiDiagnosisClient.class;

        Field[] fields =
                clazz.getDeclaredFields();

        boolean hasForbiddenDependency =
                Arrays.stream(fields)
                        .map(field ->
                                field.getType()
                                        .getSimpleName())
                        .anyMatch(name ->
                                name.contains("Repository")
                                || name.contains("Payment")
                                || name.contains("Execution")
                                || name.contains("InvoiceService")
                        );

        assertFalse(
                hasForbiddenDependency,
                "Gemini diagnosis must not depend on execution/state components"
        );
    }

    @Test
    void diagnosisResultShouldBePureData() {

        Field[] fields =
                DiagnosisResult.class
                        .getDeclaredFields();

        for (Field field : fields) {

            String name =
                    field.getType().getSimpleName();

            assertFalse(
                    name.contains("Repository"),
                    "DiagnosisResult must not contain repositories"
            );

            assertFalse(
                    name.contains("Service"),
                    "DiagnosisResult must not contain services"
            );
        }
    }
}