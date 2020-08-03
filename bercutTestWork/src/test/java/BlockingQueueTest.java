import org.junit.Assert;
import org.junit.Before;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class BlockingQueueTest {
    private int limit = 10;

    @Before
    public void setUp () {
    }

    @Test
    public void add_FirstElementToQueue_ShouldBeAddedAsHead() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        Assert.assertTrue(blockingQueue.add("TestString"));
        Assert.assertEquals("TestString", blockingQueue.element());
    }

    @Test(expected = NullPointerException.class)
    public void add_ElementWithNullValue_ShouldThrowNullPointer() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.add(null);
    }

    @Test(expected = IllegalStateException.class)
    public void add_ElementToFullQueue_ShouldThrowIllegalState() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        blockingQueue.add("TestString");
    }

    @Test
    public void offer_FirstElementToQueue_ShouldReturnTrueAndAddElement() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        Assert.assertTrue(blockingQueue.offer("TestString"));
        Assert.assertEquals("TestString", blockingQueue.element());
    }

    @Test(expected = NullPointerException.class)
    public void offer_FirstElementWithNullValue_ShouldThrowNullPointer() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.offer(null);
    }

    @Test
    public void offer_ElementToFullQueue_ShouldReturnFalse() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        Assert.assertFalse(blockingQueue.offer("TestString"));//TODO
    }

    @Test
    public void remove_HeadFromFullQueue_ShouldReturnHeadAndRemove() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.add("Head");
        fillQueue(blockingQueue, limit-1);
        Assert.assertEquals("Head", blockingQueue.remove());
        Assert.assertEquals("Test string 0", blockingQueue.element());
    }

    @Test(expected = NoSuchElementException.class)
    public void remove_HeadFromEmptyQueue_ShouldThrowNoSuchElement() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.remove();
    }

    @Test
    public void poll_HeadFromFullQueue_ShouldReturnHeadAndRemove() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.add("Head");
        fillQueue(blockingQueue, limit-1);
        Assert.assertEquals("Head", blockingQueue.poll());
        Assert.assertEquals("Test string 0", blockingQueue.element());
    }

    @Test
    public void poll_HeadFromEmptyQueue_ShouldReturnNull() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        Assert.assertEquals(null, blockingQueue.poll());
    }

    @Test
    public void element_GetHeadElementFromFullQueue_ShouldReturnHeadAndNotRemove() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.add("Head");
        fillQueue(blockingQueue, limit-1);
        Assert.assertEquals("Head", blockingQueue.element());
        Assert.assertEquals("Head", blockingQueue.element()); //Second launch to make sure it's not deleted
    }

    @Test(expected = NoSuchElementException.class)
    public void element_GetHeadElementFromQueue_ShouldThrowNoSuchElement() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.element();
    }

    @Test
    public void peek_GetHeadElementFromFullQueue_ShouldReturnHeadAndNotRemove() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.add("Head");
        fillQueue(blockingQueue, limit-1);
        Assert.assertEquals("Head", blockingQueue.peek());
        Assert.assertEquals("Head", blockingQueue.element());
    }

    @Test
    public void peek_GetHeadElementFromEmptyQueue_ShouldReturnNull() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        Assert.assertEquals(null, blockingQueue.peek());
    }

    @Test
    public void offer_ElementToEmptyQueue_ShouldAddElement() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.offer("Item In Queue", 1, TimeUnit.SECONDS);
        Assert.assertEquals("Item In Queue", blockingQueue.element());
    }

    @Test
    public void offer_ElementToFullQueue_ShouldReturnFalseAfterTimeout() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        Assert.assertFalse(blockingQueue.offer("Item In Queue", 1, TimeUnit.SECONDS));
    }

    @Test
    public void offer_ElementToFullQueueThen_take_ElementAnd_offer_WhenPlaceVacated() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        List<String> stringsThatWereTaken = new ArrayList<>();
        var elementToOffer = "This Is Offer";
        fillQueue(blockingQueue, limit);
        getOfferThread(blockingQueue, 9, TimeUnit.SECONDS, elementToOffer).start();
        Thread.sleep(100);
        getTakeThread(blockingQueue, stringsThatWereTaken).start();
        Thread.sleep(2000);
        Assert.assertEquals(elementToOffer, stringsThatWereTaken.get(stringsThatWereTaken.size()-1));
    }

    @Test
    public void offer_ElementToFullQueueThen_poll_ElementAnd_offer_WhenPlaceVacated() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        var elementToOffer = "This Is Offer";
        fillQueue(blockingQueue, limit);
        getOfferThread(blockingQueue, 9, TimeUnit.SECONDS, elementToOffer).start();
        Thread.sleep(100);
        blockingQueue.poll();
        Thread.sleep(2000);
        Assert.assertTrue(blockingQueue.contains(elementToOffer));
    }

    @Test
    public void offer_ElementToFullQueueThen_clear_ElementAnd_offer_WhenPlaceVacated() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        var elementToOffer = "This Is Offer";
        fillQueue(blockingQueue, limit);
        getOfferThread(blockingQueue, 9, TimeUnit.SECONDS, elementToOffer).start();
        Thread.sleep(100);
        blockingQueue.clear();
        Thread.sleep(2000);
        Assert.assertTrue(blockingQueue.contains(elementToOffer));
    }

    @Test
    public void offer_ElementToFullQueueThen_retainAll_ElementsWithNoMatchesToFullQueueAnd_offer_WhenPlaceVacated() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        var elementToOffer = "This Is Offer";
        fillQueue(blockingQueue, limit);
        List<String> collection = new ArrayList<>();
        for(int i = 0; i < limit; i++ ) {
            collection.add("Retain string" + " " + i);
        }
        getOfferThread(blockingQueue, 9, TimeUnit.SECONDS, elementToOffer).start();
        blockingQueue.retainAll(collection);
        Thread.sleep(2000);
        Assert.assertTrue(blockingQueue.contains(elementToOffer));
    }

    @Test
    public void put_ElementToEmptyQueueThen_remove_ElementAnd_put_WhenPlaceVacated() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, 1);
        String stringToPut = "String to put";
        List<String> stringsToPut = new ArrayList<>();
        stringsToPut.add(stringToPut);
        getPutThread(blockingQueue, stringsToPut).start();
        Thread.sleep(200);
        blockingQueue.remove();
        Thread.sleep(1000);
        Assert.assertTrue(blockingQueue.contains(stringToPut));
    }

    @Test
    public void put_ElementToFullQueueThen_clear_ElementsOfFullQueueThen_delete_ElementsAnd_put_WhenPlaceVacated() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        String stringToPut = "String to put";
        List<String> stringsToPut = new ArrayList<>();
        stringsToPut.add(stringToPut);
        getPutThread(blockingQueue, stringsToPut).start();
        Thread.sleep(200);
        blockingQueue.clear();
        Thread.sleep(1000);
        Assert.assertTrue(blockingQueue.contains(stringToPut));
    }

    @Test
    public void put_ElementToFullQueueThen_retainAll_ElementsWithNoMatchesToFullQueueAnd_put_WhenPlaceVacated() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        List<String> collection = new ArrayList<>();
        for(int i = 0; i < limit; i++ ) {
            collection.add("Retain string" + " " + i);
        }
        String stringToPut = "String to put";
        List<String> stringsToPut = new ArrayList<>();
        stringsToPut.add(stringToPut);
        getPutThread(blockingQueue, stringsToPut).start();
        blockingQueue.retainAll(collection);
        Thread.sleep(1000);
        Assert.assertTrue(stringToPut, blockingQueue.contains(stringToPut));
    }

    @Test
    public void put_ElementToFullQueueThen_removeAll_ElementsFromFullQueueAnd_put_WhenPlaceVacated() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        List<String> collection = new ArrayList<>();
        for(int i = 0; i < limit; i++ ) {
            collection.add("Test string" + " " + i);
        }
        String stringToPut = "String to put";
        List<String> stringsToPut = new ArrayList<>();
        stringsToPut.add(stringToPut);
        getPutThread(blockingQueue, stringsToPut).start();
        blockingQueue.removeAll(collection);
        Thread.sleep(1000);
        Assert.assertTrue(stringToPut, blockingQueue.contains(stringToPut));
    }

    @Test
    public void take_ElementFromFullQueue_ShouldReturnHeadElement() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        Assert.assertEquals("Test string 0", blockingQueue.take());
    }

    @Test(expected = NullPointerException.class)
    public void put_ElementWithNullValue_ShouldThrowNullPointer() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.put(null);
    }

    @Test
    public void put_ElementToQueueAndTakeItemAfter_ShouldPutElementToQueueAndThenPutItTo_stringsThatWereTaken() throws InterruptedException {
        BlockingQueue<String> queue = new BlockingQueue(limit);
        List<String> stringsThatWereTaken = new ArrayList<>();
        List<String> stringsToPut = new ArrayList<>();
        String testString = "karamelka";
        boolean res = false;
        getTakeThread(queue, stringsThatWereTaken).start();
        Thread.sleep(1000);
        stringsToPut.add(testString);
        getPutThread(queue, stringsToPut).start();
        while (!res) {
            if(stringsThatWereTaken.size() == 1) {
                break;
            }
        }
        Assert.assertEquals(testString, stringsThatWereTaken.get(0));
    }

    @Test
    public void put_ManyElementsToQueueAndTakeItemAfter() throws InterruptedException
    {
        BlockingQueue<String> queue = new BlockingQueue(limit);
        List<String> stringsThatWereTaken = new ArrayList<>();
        List<String> stringsToPut = new ArrayList<>();
        boolean res = false;
        getTakeThread(queue, stringsThatWereTaken).start();
        Thread.sleep(1000);

        for(int i = 0; i < 1000; i++ ) {
            stringsToPut.add("Test string" + " " + i);
        }

        getPutThread(queue, stringsToPut).start();
        while (!res) {
            if(stringsThatWereTaken.size() == 1000) {
                break;
            }
        }
        Assert.assertEquals("Test string 999", stringsThatWereTaken.get(0));
    }

    @Test
    public void poll_ItemFromEmptyQueue_ShouldReturnNullAfterTimeout() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        Assert.assertEquals(null, blockingQueue.poll(1, TimeUnit.SECONDS));
    }

    @Test
    public void poll_ItemFromEmptyQueue_ShouldReturnHeadElementAndDeleteHeadElement() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        Assert.assertEquals("Test string 0", blockingQueue.poll(1, TimeUnit.SECONDS));
    }

    @Test
    public void poll_ElementFromEmptyQueueAndDeleteElementAfterAddingElement() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        List<String> stringsToPut = new ArrayList<>();
        for(int i = 0; i < 10; i++ ) {
            stringsToPut.add("Test string" + " " + i);
        }
        pollThread(blockingQueue, 5, TimeUnit.SECONDS).start();
        Thread.sleep(1000);
        getPutThread(blockingQueue, stringsToPut).start();
        Thread.sleep(1000);
        Assert.assertEquals("Test string 8", blockingQueue.element());
    }

    @Test
    public void get_RemainingCapacityWithEmptyQueue_ShouldReturnLimit() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        Assert.assertEquals(limit, blockingQueue.remainingCapacity());
    }

    @Test
    public void get_RemainingCapacityWithFullQueue_ShouldReturn0() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        Assert.assertEquals(0, blockingQueue.remainingCapacity());
    }

    @Test
    public void remove_TheExistingElement_ShouldRemoveElementAndReturnTrue() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        Assert.assertTrue(blockingQueue.remove("Test string 0"));
    }

    @Test
    public void remove_NotExistingObject_ShouldReturnFalse() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        Assert.assertFalse(blockingQueue.remove("Test string 0"));
    }

    @Test(expected = NullPointerException.class)
    public void remove_NullValueObject_ShouldThrowNullPointer() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.remove(null);
    }

    @Test
    public void addAll_MaxCountElementsFromCollectionToEmptyQueue_ShouldAddAllElementsFromCollectionToQueueAndReturnTrue() {
        List<String> collection = new ArrayList<>();
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        for(int i = 0; i < limit; i++ ) {
            collection.add("Test string" + " " + i);
        }
        Assert.assertTrue(blockingQueue.addAll(collection));
        Assert.assertTrue(blockingQueue.size() == collection.size());
    }

    @Test
    public void addAll_ExceedingCountElementsFromCollectionToEmptyQueue_ShouldReturnFalse() {
        List<String> collection = new ArrayList<>();
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        for(int i = 0; i < limit+1; i++ ) {
            collection.add("Test string" + " " + i);
        }
        Assert.assertFalse(blockingQueue.addAll(collection));
    }

    @Test(expected = NullPointerException.class)
    public void addAll_NullValueElementFromCollectionToEmptyQueue_ShouldThrowNullPointer() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.addAll(null);
    }

    @Test
    public void clear_EmptyQueue_ShouldReturnTrue() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.clear();
        Assert.assertTrue(blockingQueue.size() == 0);
    }

    @Test
    public void clear_EmptyQueue_ShouldClearQueueAndReturnTrue() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        blockingQueue.clear();
        Assert.assertTrue(blockingQueue.size() == 0);
    }

    @Test(expected = NullPointerException.class)
    public void retainAll_WithNullValue_ShouldThrowNullPointer() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.retainAll(null);
    }

    @Test
    public void retainAll_ElementsInQueue_ShouldRetainAllElementsAndReturnTrue() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        List<String> collection = new ArrayList<>();
        for(int i = 0; i < 5; i++ ) {
            collection.add("Test string to retain" + " " + i);
        }
        for(int i = 0; i < 5; i++ ) {
            blockingQueue.add("Test string to retain" + " " + i);
        }
        for(int i = 0; i < 5; i++ ) {
            blockingQueue.add("Test string to delete" + " " + i);
        }
        Assert.assertTrue(blockingQueue.retainAll(collection));
    }

    @Test
    public void retainAll_ExceedingCountElementsInQueue_ShouldReturnFalse() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        List<String> collection = new ArrayList<>();
        for(int i = 0; i < limit+1; i++ ) {
            collection.add("Test string to retain" + " " + i);
        }
        Assert.assertFalse(blockingQueue.retainAll(collection));
    }

    @Test(expected = NullPointerException.class)
    public void removeAll_WithNullValueCollection_ShouldThrowNullPointer() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.removeAll(null);
    }

    @Test
    public void removeAll_WithEmptyQueue_ShouldReturnFalse() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        List<String> collection = new ArrayList<>();
        for(int i = 0; i < limit; i++ ) {
            collection.add("Test string to remove" + " " + i);
        }
        Assert.assertFalse(blockingQueue.removeAll(collection));
    }

    @Test
    public void removeAll_WithFullQueueWithMatches_ShouldRemoveAllMatchesInQueue() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        List<String> collection = new ArrayList<>();
        for(int i = 0; i < limit; i++ ) {
            collection.add("Test string to remove" + " " + i);
        }
        for(int i = 0; i < limit; i++ ) {
            blockingQueue.add("Test string to remove" + " " + i);
        }
        Assert.assertTrue(blockingQueue.removeAll(collection));
    }

    @Test
    public void removeAll_WithFullQueueWithNoMatches_ShouldNotRemoveAnyElements() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        List<String> collection = new ArrayList<>();
        for(int i = 0; i < limit; i++ ) {
            collection.add("Test string to remove" + " " + i);
        }
        fillQueue(blockingQueue, limit);
        Assert.assertFalse(blockingQueue.removeAll(collection));
    }

    @Test(expected = NullPointerException.class)
    public void containsAll_WithNullValueCollection_ShouldThrowNullPointer() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.containsAll(null);
    }

    @Test
    public void containsAll_WithEmptyCollection_ShouldReturnFalse() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        List<String> collection = new ArrayList<>();
        Assert.assertFalse(blockingQueue.containsAll(collection));
    }

    @Test
    public void containsAll_WithMatches_ShouldReturnTrue() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        List<String> collection = new ArrayList<>();
        for(int i = 0; i < limit; i++ ) {
            collection.add("Test string to match" + " " + i);
        }
        for(int i = 0; i < limit; i++ ) {
            blockingQueue.add("Test string to match" + " " + i);
        }
        Assert.assertTrue(blockingQueue.containsAll(collection));
    }

    @Test
    public void containsAll_WithNoMatches_ShouldReturnFalse() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        List<String> collection = new ArrayList<>();
        for(int i = 0; i < limit; i++ ) {
            collection.add("Test string to match" + " " + i);
        }
        fillQueue(blockingQueue, limit);
        Assert.assertFalse(blockingQueue.containsAll(collection));
    }

    @Test
    public void containsAll_WithExceedingCollection_ShouldReturnFalse() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        List<String> collection = new ArrayList<>();
        for(int i = 0; i < limit+1; i++ ) {
            collection.add("Test string to match" + " " + i);
        }
        Assert.assertFalse(blockingQueue.containsAll(collection));
    }

    @Test
    public void size_OfEmptyQueue_ShouldReturn0() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        Assert.assertTrue(blockingQueue.size() == 0);
    }

    @Test
    public void sizeOfFullQueue_ShouldReturnLimit() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        Assert.assertTrue(blockingQueue.size() == limit);
    }

    @Test
    public void isEmpty_WithEmptyQueue_ShouldReturnTrue() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        Assert.assertTrue(blockingQueue.isEmpty());
    }

    @Test
    public void isEmpty_WithFullQueue_ShouldReturnFalse() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        Assert.assertFalse(blockingQueue.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void contains_WithNullValue_ShouldThrowNullPointer() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        blockingQueue.contains(null);
    }

    @Test
    public void contains_WithMatches_ShouldReturnTrue() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        String match =  "Test to match";
        blockingQueue.add("Test to match");
        Assert.assertTrue(blockingQueue.contains(match));
    }

    @Test
    public void contains_WithNoMatches_ShouldReturnFalse() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        String match =  "Test to match";
        blockingQueue.add("Test");
        Assert.assertFalse(blockingQueue.contains(match));
    }

    @Test
    public void toArray_ShouldReturnQueueAsArray() {
        BlockingQueue<String> blockingQueue = new BlockingQueue(limit);
        fillQueue(blockingQueue, limit);
        Object[] test = blockingQueue.toArray();
        Assert.assertTrue(test.length == 10);
    }

    private void fillQueue(BlockingQueue<String> blockingQueue, int count){
        for(int i = 0; i < count; i++ ) {
            blockingQueue.add("Test string" + " " + i);
        }
    }

    public Thread getPutThread(BlockingQueue blockingQueue, List<String> stringsToPut) {
        Runnable taskToPut = () -> {
            while(stringsToPut.size() != 0)
                try {
                    blockingQueue.put(stringsToPut.get(stringsToPut.size()-1));
                    stringsToPut.remove(stringsToPut.size()-1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        };
        Thread threadPut = new Thread(taskToPut);
        return threadPut;
    }

    public Thread getTakeThread(BlockingQueue blockingQueue, List<String> stringsThatWereTaken) {
        Runnable taskToTake = () -> {
            while(stringsThatWereTaken.size() < 1000)
                try {
                    Object res = blockingQueue.take();
                    stringsThatWereTaken.add((String) res);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        };
        Thread threadTake = new Thread(taskToTake);
        return threadTake;
    }

    public Thread getOfferThread(BlockingQueue blockingQueue, long timeout, TimeUnit unit, String elementToOffer) {
        Runnable taskToTake = () -> {
            try {
                blockingQueue.offer(elementToOffer, timeout, unit);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread threadTake = new Thread(taskToTake);
        return threadTake;
    }

    public Thread pollThread(BlockingQueue blockingQueue, long timeout, TimeUnit unit) {
        Runnable taskToTake = () -> {
            try {
                blockingQueue.poll( timeout, unit);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread threadTake = new Thread(taskToTake);
        return threadTake;
    }
}

