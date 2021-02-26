/*
 * Copyright Chris2018998
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.beecp.concurrent;

import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import static java.util.concurrent.atomic.AtomicReferenceFieldUpdater.newUpdater;

/**
 * Concurrent Queue Implementation mock class:{@link java.util.concurrent.ConcurrentLinkedQueue}
 *
 * @author Chris.Liao
 */
public class BeeConcurrentQueue<E> extends AbstractQueue<E> implements Queue<E> {
    private final static AtomicReferenceFieldUpdater<Node, Node> nextUpd =
            newUpdater(Node.class, Node.class, "next");

    private static final class Node<E> {
        volatile Node<E> next;
        private E v;

        public Node(E v) {
            this.v = v;
        }
    }

    private final Node<E> head;
    private volatile Node<E> tail;
    public BeeConcurrentQueue() {
        this.head = new Node<E>(null);
        this.tail = head;
    }

    public final boolean isEmpty() {
        return head == tail;
    }

    public E peek() {
        Node node = head.next;
        return (node != null) ? (E) node.v : null;
    }

    public E poll() {
        Node node = head.next;
        if (node != null && nextUpd.compareAndSet(head, node, node.next))
            return (E) node.v;
        return null;
    }

    public int size() {
        int size = 0;
        Node<E> curNode = head.next;
        while (curNode != null) {
            size++;
            curNode = curNode.next;
        }
        return size;
    }

    public boolean offer(E v) {
        Node<E> node = new Node<E>(v);
        while (true) {
            Node<E> t = tail;
            if (nextUpd.compareAndSet(t, null, node)) {
                this.tail = node;
                return true;
            }
        }
    }

    public boolean remove(Object o) {
        Node preNode = head;
        Node curNode = head.next;
        while (curNode != null) {
            if (o.equals(curNode.v)) {
                if (nextUpd.compareAndSet(preNode, curNode, curNode.next)) {//removed from chain
                    if (curNode == tail) tail = preNode;
                    return true;
                } else {
                    return false;
                }
            }
            preNode = curNode;
            curNode = preNode.next;
        }
        return false;
    }

    public boolean contains(Object o) {
        Node node = head.next;
        while (node != null) {
            if (o.equals(node.v)) return true;
            Node next = node.next;
        }
        return false;
    }

    public Object[] toArray() {
        LinkedList<E> al = new LinkedList<E>();
        Node node = head.next;
        while (node != null) {
            al.add((E) node.v);
            Node next = node.next;
        }
        return al.toArray();
    }

    public <T> T[] toArray(T[] a) {
        LinkedList<E> al = new LinkedList<E>();
        Node node = head.next;
        while (node != null) {
            al.add((E) node.v);
            Node next = node.next;
        }
        return al.toArray(a);
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        private Node preNode = head;
        private Node curNode = head;

        public boolean hasNext() {
            return curNode != null && curNode.next != null;
        }

        public E next() {
            if (curNode == null) throw new NoSuchElementException();

            preNode = curNode;
            curNode = preNode.next;
            return (curNode != null) ? (E) curNode.v : null;
        }

        public void remove() {
            if (curNode == null || preNode == curNode) throw new IllegalStateException();
            Node nextNode = curNode.next;
            if (preNode.next == curNode && nextUpd.compareAndSet(preNode, curNode, nextNode)) {//removed from chain
                curNode = nextNode;
                if (curNode == tail) tail = preNode;
            }
        }
    }//Iterator


//    public static void main(String[] args) {
//        int size = 9000000;
//        ConcurrentLinkedQueue<Integer> queue1 = new ConcurrentLinkedQueue<Integer>();
//        testConcurrentQueue("ConcurrentLinkedQueue", queue1, size);
//        BeeConcurrentQueue<Integer> queue2 = new BeeConcurrentQueue<Integer>();
//        testConcurrentQueue("ConcurrentLinkedQueue2", queue2, size);
//    }
//
//    private static void testConcurrentQueue(String queueName, Queue queue, int size) {
//        long time1 = System.currentTimeMillis();
//        for (int i = 0; i < size; i++) {
//            queue.offer(i);
//        }
//        long time2 = System.currentTimeMillis();
//        for (int i = 0; i < size; i++) {
//            queue.remove(i);
//        }
//        long time3 = System.currentTimeMillis();
//
//        System.out.println("<" + queueName + ">offer time:" + (time2 - time1) + "ms");
//        System.out.println("<" + queueName + ">remove time:" + (time2 - time1) + "ms");
//        System.out.println("<" + queueName + ">is empty:" + queue.isEmpty());
//    }
}
