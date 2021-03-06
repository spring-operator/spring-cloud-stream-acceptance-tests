package sample.acceptance.tests;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.fail;

public class PartitioningKafkaAcceptanceTests extends AbstractAcceptanceTests {


    @Test
    public void testPartitioningWith3ConsumersKafka() throws Exception {

        Thread.sleep(10_000);

        String prodUrl = System.getProperty("partitioning.producer.route");

        boolean foundLogs = waitForLogEntry("Partitioning producer", prodUrl, "Started PartProducerApplication in");
        if(!foundLogs) {
            fail("Did not find the logging messages.");
        }

        String consumer1Url = System.getProperty("partitioning.consumer1.route");
        String consumer2Url = System.getProperty("partitioning.consumer2.route");
        String consumer3Url = System.getProperty("partitioning.consumer3.route");

        Future<?> future1 = verifyPartitions("Partitioning Consumer-1", consumer1Url,
                "f received from partition 0",
                "g received from partition 0",
                "h received from partition 0");
        Future<?> future2 = verifyPartitions("Partitioning Consumer-2", consumer2Url,
                "fo received from partition 1",
                "go received from partition 1",
                "ho received from partition 1");
        Future<?> future3 = verifyPartitions("Partitioning Consumer-3", consumer3Url,
                "foo received from partition 2",
                "goo received from partition 2",
                "hoo received from partition 2");

        verifyResults(future1, future2, future3);
    }

    private Future<?> verifyPartitions(String consumerMsg, String consumerRoute,
                                       String... entries) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Future<?> submit = executorService.submit(() -> {
            boolean found = waitForLogEntry(consumerMsg, consumerRoute, entries);
            if (!found) {
                fail("Could not find the test data in the logs");
            }
        });

        executorService.shutdown();
        return submit;
    }

    private void verifyResults(Future<?>... futures) throws Exception {
        for (Future<?> future : futures) {
            try {
                future.get();
            }
            catch (Exception e) {
                throw e;
            }
        }
    }
}
