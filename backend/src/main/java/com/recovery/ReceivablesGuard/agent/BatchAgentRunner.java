package com.recovery.ReceivablesGuard.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.recovery.ReceivablesGuard.domain.AgentRun;

@Service
public class BatchAgentRunner {

    private final AgentRunManager agentRunManager;

    /*
     * REST/async executions are identified by AgentRun ID.
     *
     * This allows:
     *
     * agentRunId -> exact Future
     *
     * so cancellation cannot accidentally target another run
     * for the same invoice.
     */
    private final Map<Long, Future<AgentRunResult>> activeTasks =
            new ConcurrentHashMap<>();

    /*
     * Only one execution for a given invoice may execute at a time.
     *
     * Different invoices can execute concurrently.
     */
    private final ConcurrentMap<String, InvoiceLock> invoiceLocks =
            new ConcurrentHashMap<>();

    public BatchAgentRunner(
            AgentRunManager agentRunManager) {

        this.agentRunManager = agentRunManager;
    }

    /*
     * Existing batch execution path.
     *
     * This remains synchronous from the caller's perspective.
     * Each invoice is submitted to the configured executor.
     */
    public BatchRunResult runBatch(
            List<String> invoiceReferences,
            int maxRounds,
            int maxConcurrency) {

        if (invoiceReferences == null ||
                invoiceReferences.isEmpty()) {

            throw new IllegalArgumentException(
                    "Invoice references must not be empty");
        }

        if (maxRounds <= 0) {

            throw new IllegalArgumentException(
                    "maxRounds must be greater than zero");
        }

        if (maxConcurrency <= 0) {

            throw new IllegalArgumentException(
                    "maxConcurrency must be greater than zero");
        }

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        maxConcurrency
                );

        Map<String, Future<AgentRunResult>> futures =
                new ConcurrentHashMap<>();

        try {

            for (String invoiceReference :
                    invoiceReferences) {

                Future<AgentRunResult> future =
                        executor.submit(() ->
                                runInvoice(
                                        invoiceReference,
                                        maxRounds
                                )
                        );

                futures.put(
                        invoiceReference,
                        future
                );
            }

            List<BatchRunItem> results =
                    new ArrayList<>();

            for (String invoiceReference :
                    invoiceReferences) {

                Future<AgentRunResult> future =
                        futures.get(invoiceReference);

                try {

                    AgentRunResult result =
                            future.get();

                    results.add(
                            BatchRunItem.success(
                                    invoiceReference,
                                    result
                            )
                    );

                } catch (InterruptedException exception) {

                    Thread.currentThread().interrupt();

                    results.add(
                            BatchRunItem.failure(
                                    invoiceReference,
                                    exception
                            )
                    );

                } catch (ExecutionException exception) {

                    results.add(
                            BatchRunItem.failure(
                                    invoiceReference,
                                    exception.getCause()
                            )
                    );
                }
            }

            return new BatchRunResult(results);

        } finally {

            executor.shutdown();

            try {

                if (!executor.awaitTermination(
                        30,
                        TimeUnit.SECONDS)) {

                    executor.shutdownNow();
                }

            } catch (InterruptedException exception) {

                executor.shutdownNow();

                Thread.currentThread().interrupt();
            }
        }
    }

    /*
     * Start execution for an already-created AgentRun.
     *
     * The AgentRun must already be persisted and have an ID.
     *
     * The returned REST request does not wait for execution to finish.
     */
    public void startAsync(AgentRun agentRun) {
    if (agentRun == null) {
        throw new IllegalArgumentException("Agent run must not be null");
    }

    if (agentRun.getId() == null) {
        throw new IllegalArgumentException("Agent run ID must not be null");
    }

    if (agentRun.getInvoice() == null
            || agentRun.getInvoice().getExternalRef() == null) {
        throw new IllegalArgumentException("Agent run invoice reference must not be null");
    }

    Long agentRunId = agentRun.getId();
    String invoiceReference = agentRun.getInvoice().getExternalRef();

    ExecutorService executor = Executors.newSingleThreadExecutor();

    Future<AgentRunResult> future;

    System.out.println(
        "[DEBUG] startAsync entered: runId=" + agentRunId
                + ", invoice=" + invoiceReference
);

try {
    future = executor.submit(() -> {
        System.out.println(
                "[DEBUG] worker started: runId=" + agentRunId
        );

        try {
            AgentRunResult result =
                    runCancellableInvoice(
                            agentRunId,
                            invoiceReference
                    );

            System.out.println(
                    "[DEBUG] worker completed: runId="
                            + agentRunId
            );

            return result;

        } catch (Exception exception) {
            System.out.println(
                    "[DEBUG] worker failed: runId="
                            + agentRunId
                            + ", error="
                            + exception
            );
            exception.printStackTrace();
            throw exception;
        }
    });
}catch (RuntimeException exception) {
        executor.shutdownNow();
        throw exception;
    }

    activeTasks.put(agentRunId, future);

    Thread cleanupThread = new Thread(() -> {
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            // AgentRunManager owns FAILED status handling.
        } catch (java.util.concurrent.CancellationException e) {
            // Expected when cancel(agentRunId) successfully cancels the Future.
        } finally {
            activeTasks.remove(agentRunId, future);

            executor.shutdown();

            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    });

    cleanupThread.start();
}

    /*
     * Execute an already-created AgentRun while respecting the
     * per-invoice lock.
     */
    private AgentRunResult runCancellableInvoice(
        Long agentRunId,
        String invoiceReference)
        throws InterruptedException {

        InvoiceLock invoiceLock =
                invoiceLocks.compute(
                        invoiceReference,
                        (reference, existingLock) -> {

                            InvoiceLock lock =
                                    existingLock == null
                                            ? new InvoiceLock()
                                            : existingLock;

                            lock.users++;

                            return lock;
                        }
                );

        boolean locked = false;

        try {

            /*
             * Interruptible lock acquisition is important.
             *
             * If cancellation happens while waiting for another
             * run of the same invoice to finish, this task can
             * stop without entering the critical section.
             */
            invoiceLock.lock.lockInterruptibly();

            locked = true;

            return agentRunManager.executeRun(
        agentRunManager.loadRun(agentRunId)
);

        } finally {

            if (locked) {

                invoiceLock.lock.unlock();
            }

            invoiceLocks.computeIfPresent(
                    invoiceReference,
                    (reference, currentLock) -> {

                        currentLock.users--;

                        return currentLock.users == 0
                                ? null
                                : currentLock;
                    }
            );
        }
    }

    /*
     * Cancel one exact AgentRun.
     *
     * The Future is looked up using AgentRun ID rather than
     * invoice reference.
     */
    public boolean cancel(
            Long agentRunId) {

        if (agentRunId == null) {

            return false;
        }

        Future<AgentRunResult> future =
                activeTasks.get(agentRunId);

        if (future == null) {

            return false;
        }

        return future.cancel(true);
    }

    private static final class InvoiceLock {

        private final ReentrantLock lock =
                new ReentrantLock();

        private int users;
    }
    private AgentRunResult runInvoice(String invoiceReference, int maxRounds)
        throws InterruptedException {

    InvoiceLock invoiceLock = invoiceLocks.compute(
            invoiceReference,
            (reference, existingLock) -> {
                InvoiceLock lock = existingLock != null
                        ? existingLock
                        : new InvoiceLock();
                lock.users++;
                return lock;
            }
    );

    boolean locked = false;

    try {
        invoiceLock.lock.lockInterruptibly();
        locked = true;

        return agentRunManager.run(invoiceReference, maxRounds);

    } finally {
        if (locked) {
            invoiceLock.lock.unlock();
        }

        invoiceLocks.computeIfPresent(
                invoiceReference,
                (reference, lock) -> {
                    lock.users--;
                    return lock.users == 0 ? null : lock;
                }
        );
    }
}
}