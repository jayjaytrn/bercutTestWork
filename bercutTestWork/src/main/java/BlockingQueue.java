import java.util.*;
import java.util.concurrent.TimeUnit;

public class BlockingQueue<T> implements java.util.concurrent.BlockingQueue<T> {

    private LinkedList<T> list = new LinkedList<T>();
    private int limit;

    /** Добавляет элемент в очередь сразу, если она не заполнена и
     *  возвращает true. Бросает IllegalStateException, если места нет.
     */
    public synchronized boolean add(T item) {
        if(item == null) throw new NullPointerException();
        if(list.size() == limit) throw new IllegalStateException();
        return offer(item);
    }

    /** Добавляет элемент в очередь сразу, если она не заполнена
     *   и возвращает true. Возвращает false, если нет места.
     */
    public synchronized boolean offer(T item)  {
        if(item == null) throw new NullPointerException();
        if(list.size() == limit) {
            return false;
        }
        else {
            list.add(item);
            if(list.size() == limit)
                notifyAll();
            return true;
        }
    }

    /** Удаляет головной элемент очереди (тот что первым вошел) и
     *  возвращает его. Возвращает NoSuchElementException, если очередь пуста.
     */
    public synchronized T remove() {
        if(list.isEmpty()) throw new NoSuchElementException();
        T head = list.getFirst();
        list.remove(head);
        if (list.size() == 0)
            notifyAll();
        return head;
    }

    /** Удаляет головной элемент очереди (тот что первым вошел) и
     *  возвращает его. Возвращает null, если очередь пуста.
     */
    public synchronized T poll() {
        if(list.isEmpty()) {
            return null;
        }
        T head = list.getFirst();
        list.remove(head);
        if (list.size() < limit)
            notifyAll();
        return head;
    }

    /** Возвращает головной элемент очереди, но не удаляет его.
     *  Возвращает NoSuchElementException, если очередь пуста.
     */
    public synchronized T element() {
        if(list.isEmpty()) throw new NoSuchElementException();
        T head = list.getFirst();
        return head;
    }

    /** Возвращает головной элемент очереди, но не удаляет его.
     *  Возвращает null, если очередь пуста.
     */
    public synchronized T peek() {
        if(list.isEmpty()) {
            return null;
        }
        T item = list.getFirst();
        return item;
    }

    /** Добавляет элемент в очередь сразу, если она не заполнена.
     *  Возвращает null, если очередь пуста. Ожидает до освобождения
     *  очереди, если она заполнена и пытается поместить туда элемент.
     */
    public synchronized void put(T item) throws InterruptedException {
        if(item == null) throw new NullPointerException();
        while (list.size() == this.limit) {
            wait();
        }
        if (list.size() == 0) {
            notifyAll();
        }
        list.add(item);
    }

    /** Добавляет элемент в очередь сразу, если она не заполнена.
     *  Ожидает до освобождения очереди определенное время и
     *  возвращает false если за это время очередь не освободилась.
     */
    public synchronized boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
        TimeUnit time = TimeUnit.MILLISECONDS;
        long waitingTime = time.convert(timeout, unit);
        if(list.size() == limit) {
            wait(waitingTime);
        }
        if(list.size() == limit) {
            return false;
        } else {
            list.add(item);
            if(list.size() == limit)
                notifyAll();
            return true;
        }
    }

    /** Удаляет головной элемент очереди (тот что первым вошел) и возвращает его.
     *  Ожидает пока такой элемент появится если его нет.
     */
    public synchronized T take() throws InterruptedException {
        while (list.size() == 0){
            wait();
        }
        if (list.size() == limit){
            notifyAll();
        }
        T item = list.remove(0);
        return item;
    }

    /** Удаляет головной элемент очереди (тот что первым вошел) и возвращает его.
     *  Ожидает опеределнное время пока такой элемент появится и возвращает null если он не появился.
     */
    public synchronized T poll(long timeout, TimeUnit unit) throws InterruptedException { //
        TimeUnit time = TimeUnit.MILLISECONDS;
        long waitingTime = time.convert(timeout, unit);
        if(list.size() == 0) {
            wait(waitingTime);
        }
        if (list.size() == 0) {
            return null;
        }
        else {
            T item = list.remove(0);//TODO check
            if (list.size() == 0)
                notifyAll();
            return item;
        }
    }

    /** Возвращает текущий лимит очереди
     */
    public synchronized int remainingCapacity() { //
        return limit - list.size();
    }

    /** Удаление объекта из очереди, если он в ней присутствует,
     *  возвращает true в случае успеха и false если объект не найден
     */
    public synchronized boolean remove(Object o) {
        if(o == null) throw new NullPointerException();
        if (list.contains(o)) {
            return list.remove(o);
        } else {
            if (list.size() == 0)
                notifyAll();
            return false;
        }
    }

    /** Добавляет все элементы коллекции в список, возвращает true в случае успеха.
     *  Бросает IllegalStateException если в очереди не хватает места.
     */
    public synchronized boolean addAll(Collection c) {
        if(c == null) throw new NullPointerException();
        if(c.size() > remainingCapacity()) {
            return false;
        }
        if(list.size() == limit)
            notifyAll();
        return list.addAll(c);
    }

    /** Удаляет все элементы из очереди
     */
    public synchronized void clear() {
        list.clear();
        if(list.size() == 0)
            notifyAll();
    }

    /** Оставляет в очереди только те элементы, которые содержатся в коллекции
     */
    public synchronized boolean retainAll(Collection c) { //
        if(c == null) throw new NullPointerException();
        if(c.size() > limit) {
            return false;
        } else {
            if(list.size() == 0)
                notifyAll();
            return list.retainAll(c);
        }
    }

    /** Удаляет из очереди все элементы, которые содержатся в коллекции
     */
    public synchronized boolean removeAll(Collection c) { //
        if(c == null) throw new NullPointerException();
        if(c.size() > limit) {
            return false;
        } else {
            if(list.size() == 0)
                notifyAll();
           return list.removeAll(c);
        }
    }

    /** Возвращает true, если очередь содержит все элементы коллекции
     */
    public synchronized boolean containsAll(Collection c) {
        if(c == null) throw new NullPointerException();
        if(c.size() > limit || c.size() == 0) {
            return false;
        } else {
            return list.containsAll(c);
        }
    }

    /** Возвращает количество элементов в очереди
     */
    public synchronized int size() {
        return list.size();
    }

    /** Возвращает true, если очередь не содержит элементов
     */
    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }

    /** Возвращает true, если очередь содержит элемент
     */
    public synchronized boolean contains(Object o) {
        if(o == null) throw new NullPointerException();
        return list.contains(o);
    }

    /** Возвращает итератор для элементов очереди
     */
    public synchronized Iterator iterator() {
        return list.iterator();
    }

    /** Возвращает массив, содержащий все элементы очереди
     */
    public synchronized Object[] toArray() {
        return list.toArray();
    }

    public synchronized Object[] toArray(Object[] a) {
        return list.toArray(a);
    }

    /** Удаляет все доступные элементы из очереди и добавляет их к данной коллекции, возвращает количество добавленных элементов
     */
    public synchronized int drainTo(Collection c) {
        c.addAll(list);
        list.clear();
        return c.size();
    }

    /** Удаляет из очереди указанное количество элеменов их к данной коллекции, возвращает количество перемещенных элементов
     */
    public int drainTo(Collection c, int maxElements) {
        drainTo(c,maxElements);
        return maxElements;
    }

    public BlockingQueue(int limit){
        if (limit <= 0) throw new IllegalArgumentException();
        this.limit = limit;
    }
}
