package com.recovery.ReceivablesGuard.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import org.mockito.junit.jupiter.MockitoExtension;
import com.recovery.ReceivablesGuard.domain.AgentRun;
import com.recovery.ReceivablesGuard.domain.Customer;
import com.recovery.ReceivablesGuard.domain.Invoice;

@ExtendWith(MockitoExtension.class)
class BatchAgentRunnerTest {

    @Mock
    private AgentRunManager agentRunManager;

    private AgentRunResult successfulResult() {
        return new AgentRunResult(
                1L,
                1L,
                1L,
                "COMPLETED",
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    @Timeout(10)
    void sameInvoiceRunsSequentially() throws Exception {

        BatchAgentRunner runner =
                new BatchAgentRunner(agentRunManager);

        AtomicBoolean running =
                new AtomicBoolean(false);

        AtomicBoolean overlapped =
                new AtomicBoolean(false);

        CountDownLatch firstStarted =
                new CountDownLatch(1);

        CountDownLatch releaseFirst =
                new CountDownLatch(1);

        AtomicInteger invocationCount =
                new AtomicInteger(0);

        doAnswer(invocation -> {

            int invocationNumber =
                    invocationCount.incrementAndGet();

            if (!running.compareAndSet(false, true)) {
                overlapped.set(true);
            }

            if (invocationNumber == 1) {

                firstStarted.countDown();

                releaseFirst.await(
                        5,
                        TimeUnit.SECONDS
                );
            }

            running.set(false);

            return successfulResult();

        }).when(agentRunManager)
                .run(eq("INV-001"), eq(1));

        Thread first =
                new Thread(() ->
                        runner.runBatch(
                                List.of("INV-001"),
                                1,
                                2));

        Thread second =
                new Thread(() ->
                        runner.runBatch(
                                List.of("INV-001"),
                                1,
                                2));

        first.start();

        assertTrue(
                firstStarted.await(
                        5,
                        TimeUnit.SECONDS
                ),
                "First invocation did not start"
        );

        second.start();

        Thread.sleep(200);

        assertEquals(
                1,
                invocationCount.get(),
                "Second execution started before first completed"
        );

        releaseFirst.countDown();

        first.join(5000);
        second.join(5000);

        assertFalse(
                first.isAlive(),
                "First batch thread did not finish"
        );

        assertFalse(
                second.isAlive(),
                "Second batch thread did not finish"
        );

        assertFalse(
                overlapped.get(),
                "Same invoice executions overlapped"
        );

        verify(
                agentRunManager,
                timeout(5000).times(2)
        ).run("INV-001", 1);
    }

    @Test
    @Timeout(10)
    void differentInvoicesRunConcurrently() throws Exception {

        BatchAgentRunner runner =
                new BatchAgentRunner(agentRunManager);

        CountDownLatch invoiceOneStarted =
                new CountDownLatch(1);

        CountDownLatch invoiceTwoStarted =
                new CountDownLatch(1);

        CountDownLatch release =
                new CountDownLatch(1);

        doAnswer(invocation -> {

            String invoice =
                    invocation.getArgument(0);

            if ("INV-001".equals(invoice)) {
                invoiceOneStarted.countDown();
            }

            if ("INV-002".equals(invoice)) {
                invoiceTwoStarted.countDown();
            }

            release.await(
                    5,
                    TimeUnit.SECONDS
            );

            return successfulResult();

        }).when(agentRunManager)
                .run(anyString(), anyInt());

        Thread batch =
                new Thread(() ->
                        runner.runBatch(
                                List.of(
                                        "INV-001",
                                        "INV-002"
                                ),
                                1,
                                2));

        batch.start();

        assertTrue(
                invoiceOneStarted.await(
                        5,
                        TimeUnit.SECONDS
                ),
                "INV-001 did not start"
        );

        assertTrue(
                invoiceTwoStarted.await(
                        5,
                        TimeUnit.SECONDS
                ),
                "INV-002 did not start concurrently"
        );

        release.countDown();

        batch.join(5000);

        assertFalse(
                batch.isAlive(),
                "Batch did not finish"
        );

        verify(
                agentRunManager,
                timeout(5000)
        ).run("INV-001", 1);

        verify(
                agentRunManager,
                timeout(5000)
        ).run("INV-002", 1);
    }

    @Test
    @Timeout(10)
    void maximumConcurrencyIsBounded() {

        BatchAgentRunner runner =
                new BatchAgentRunner(agentRunManager);

        int maxConcurrency = 2;

        AtomicInteger currentConcurrency =
                new AtomicInteger(0);

        AtomicInteger observedMaximum =
                new AtomicInteger(0);

        doAnswer(invocation -> {

            int current =
                    currentConcurrency.incrementAndGet();

            observedMaximum.updateAndGet(
                    maximum ->
                            Math.max(
                                    maximum,
                                    current
                            )
            );

            try {

                Thread.sleep(100);

                return successfulResult();

            } finally {

                currentConcurrency.decrementAndGet();
            }

        }).when(agentRunManager)
                .run(anyString(), anyInt());

        List<String> invoices =
                List.of(
                        "INV-001",
                        "INV-002",
                        "INV-003",
                        "INV-004",
                        "INV-005"
                );

        BatchRunResult result =
                runner.runBatch(
                        invoices,
                        1,
                        maxConcurrency
                );

        assertEquals(
                invoices.size(),
                result.results().size()
        );

        assertTrue(
                observedMaximum.get() <= maxConcurrency,
                "Observed concurrency exceeded configured maximum"
        );
    }

    @Test
    @Timeout(10)
    void cancellationStopsTask() throws Exception {

        BatchAgentRunner runner =
                new BatchAgentRunner(agentRunManager);

        CountDownLatch taskStarted =
                new CountDownLatch(1);

        CountDownLatch interrupted =
                new CountDownLatch(1);

        AgentRun agentRun =
                createAgentRun(
                        100L,
                        "INV-CANCEL"
                );
        doReturn(agentRun)
        .when(agentRunManager)
        .loadRun(100L);

        doAnswer(invocation -> {

            taskStarted.countDown();

            try {

                Thread.sleep(30_000);

            } catch (InterruptedException exception) {

                interrupted.countDown();

                throw exception;
            }

            return successfulResult();

        }).when(agentRunManager)
                .executeRun(agentRun);

        runner.startAsync(agentRun);

        assertTrue(
                taskStarted.await(
                        5,
                        TimeUnit.SECONDS
                ),
                "Task did not start"
        );

        boolean cancelled =
                waitForCancellation(
                        runner,
                        100L
                );

        assertTrue(
                cancelled,
                "Cancellation should return true"
        );

        assertTrue(
                interrupted.await(
                        5,
                        TimeUnit.SECONDS
                ),
                "Running task was not interrupted"
        );
    }

    @Test
    @Timeout(10)
    void cancellationUsesAgentRunId() throws Exception {

        BatchAgentRunner runner =
                new BatchAgentRunner(agentRunManager);

        CountDownLatch firstStarted =
                new CountDownLatch(1);

        CountDownLatch secondStarted =
                new CountDownLatch(1);

        CountDownLatch firstInterrupted =
                new CountDownLatch(1);

        CountDownLatch secondRelease =
                new CountDownLatch(1);

        AgentRun firstRun = createAgentRun(101L, "INV-FIRST");
        AgentRun secondRun = createAgentRun(102L, "INV-SECOND");
       doReturn(firstRun)
        .when(agentRunManager)
        .loadRun(101L);

doReturn(secondRun)
        .when(agentRunManager)
        .loadRun(102L);
        doAnswer(invocation -> {

            firstStarted.countDown();

            try {

                Thread.sleep(30_000);

            } catch (InterruptedException exception) {

                firstInterrupted.countDown();

                throw exception;
            }

            return successfulResult();

        }).when(agentRunManager)
                .executeRun(firstRun);

        doAnswer(invocation -> {

            secondStarted.countDown();

            secondRelease.await(
                    5,
                    TimeUnit.SECONDS
            );

            return successfulResult();

        }).when(agentRunManager)
                .executeRun(secondRun);

        runner.startAsync(firstRun);
        runner.startAsync(secondRun);

        assertTrue(
                firstStarted.await(
                        5,
                        TimeUnit.SECONDS
                ),
                "First run did not start"
        );

        assertTrue(
                secondStarted.await(
                        5,
                        TimeUnit.SECONDS
                ),
                "Second run did not start"
        );

        assertTrue(
                runner.cancel(101L),
                "First run should be cancellable"
        );

        assertTrue(
                firstInterrupted.await(
                        5,
                        TimeUnit.SECONDS
                ),
                "First run was not interrupted"
        );

        /*
         * Cancelling AgentRun 101 must not cancel AgentRun 102,
         * even though both runs use the same invoice reference.
         */
        assertFalse(
                runner.cancel(101L),
                "First run should no longer be active"
        );

        assertTrue(
                runner.cancel(102L),
                "Second run should still be independently cancellable"
        );

        secondRelease.countDown();
    }

    @Test
    @Timeout(10)
    void failureOfOneInvoiceDoesNotAffectOthers() {

        BatchAgentRunner runner =
                new BatchAgentRunner(agentRunManager);

        doThrow(new IllegalStateException(
                "Simulated failure"
        )).when(agentRunManager)
                .run("INV-FAIL", 1);

        doAnswer(invocation ->
                successfulResult()
        ).when(agentRunManager)
                .run("INV-OK", 1);

        BatchRunResult result =
                runner.runBatch(
                        List.of(
                                "INV-FAIL",
                                "INV-OK"
                        ),
                        1,
                        2
                );

        assertEquals(
                2,
                result.results().size()
        );

        verify(
                agentRunManager,
                timeout(5000)
        ).run("INV-FAIL", 1);

        verify(
                agentRunManager,
                timeout(5000)
        ).run("INV-OK", 1);
    }

    @Test
    @Timeout(10)
    void concurrentRunsDoNotCauseRaceConditions()
            throws Exception {

        BatchAgentRunner runner =
                new BatchAgentRunner(agentRunManager);

        int numberOfInvoices = 20;

        AtomicInteger active =
                new AtomicInteger(0);

        AtomicInteger maximumActive =
                new AtomicInteger(0);

        List<String> invoices =
                new ArrayList<>();

        for (int i = 0; i < numberOfInvoices; i++) {

            invoices.add(
                    "INV-" + i
            );
        }

        doAnswer(invocation -> {

            int current =
                    active.incrementAndGet();

            maximumActive.updateAndGet(
                    maximum ->
                            Math.max(
                                    maximum,
                                    current
                            )
            );

            try {

                Thread.sleep(20);

                return successfulResult();

            } finally {

                active.decrementAndGet();
            }

        }).when(agentRunManager)
                .run(anyString(), anyInt());

        BatchRunResult result =
                runner.runBatch(
                        invoices,
                        1,
                        4
                );

        assertEquals(
                numberOfInvoices,
                result.results().size()
        );

        assertEquals(
                0,
                active.get(),
                "All tasks should have completed"
        );

        assertTrue(
                maximumActive.get() <= 4,
                "Concurrency exceeded configured limit"
        );

        verify(
                agentRunManager,
                timeout(5000).times(numberOfInvoices)
        ).run(anyString(), eq(1));
    }

    private AgentRun createAgentRun(
            Long id,
            String invoiceReference) {

        Customer customer =
        new Customer(
                "CUST-" + id,
                "Test Customer",
                "test@example.com",
                "+911234567890"
        );

        Invoice invoice =
                new Invoice(
                        invoiceReference,
                        customer,
                        java.math.BigDecimal.valueOf(10000),
                        "INR",
                        java.time.LocalDate.now().minusDays(30),
                        java.time.LocalDate.now().minusDays(10)
                );

        AgentRun agentRun =
                new AgentRun(
                        invoice,
                        1
                );

        setAgentRunId(
                agentRun,
                id
        );

        return agentRun;
    }

    private boolean waitForCancellation(
            BatchAgentRunner runner,
            Long agentRunId)
            throws InterruptedException {

        long deadline =
                System.currentTimeMillis() + 5000;

        while (System.currentTimeMillis() < deadline) {

            if (runner.cancel(agentRunId)) {
                return true;
            }

            Thread.sleep(25);
        }

        return false;
    }

    private static void setAgentRunId(
            AgentRun agentRun,
            Long id) {

        try {

            java.lang.reflect.Field field =
                    AgentRun.class.getDeclaredField("id");

            field.setAccessible(true);

            field.set(
                    agentRun,
                    id
            );

        } catch (ReflectiveOperationException exception) {

            throw new AssertionError(
                    "Unable to set agent run ID",
                    exception
            );
        }
    }
}