package com.example.homework;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Оснастка для функционального тестирования {@link KeyValueStorage}.
 * Для запуска нужно завести конкретный класс-потомок и определить соответствующие фабричные методы.
 */
public abstract class AbstractSingleFileStorageTest extends KeyValueStorageFactories {

    public static final StudentKey KEY_1 = new StudentKey(591, "Vasya Pukin");
    public static final Student VALUE_1 = new Student(
            591,
            "Vasya Pukin",
            "Vasyuki",
            StorageTestUtils.date(1996, 4, 14),
            true,
            7.8);

    public static final StudentKey KEY_2 = new StudentKey(591, "Ahmad Ben Hafiz");
    public static final Student VALUE_2 = new Student(
            591,
            "Ahmad Ben Hafiz",
            "Cairo",
            StorageTestUtils.date(1432, 9, 2),
            false,
            3.3);

    public static final StudentKey KEY_3 = new StudentKey(599, "John Smith");
    public static final Student VALUE_3 = new Student(
            599,
            "John Smith",
            "Glasgow",
            StorageTestUtils.date(1874, 3, 8),
            true,
            9.1);

    @Test
    public void testReadWrite() throws Exception {
        doWithStrings(storage -> {
            storage.write("foo", "bar");
            Assertions.assertEquals("bar", storage.read("foo"));
            Assertions.assertEquals(1, storage.size());
            StorageTestUtils.assertFullyMatch(storage.readKeys(), "foo");
        });
    }

    @Test
    public void testPersistence() throws Exception {
        doWithPojo(storage -> storage.write(KEY_1, VALUE_1));

        doWithPojo(storage -> {
            Assertions.assertEquals(VALUE_1, storage.read(KEY_1));
            Assertions.assertEquals(1, storage.size());
            StorageTestUtils.assertFullyMatch(storage.readKeys(), KEY_1);
        });

        doWithPojo(storage -> Assertions.assertNotNull(storage.read(KEY_1)));
    }

    @Test
    public void testMissingKey() throws Exception {
        doWithNumbers(storage -> {
            storage.write(4, 3.0);
            Assertions.assertEquals((Object) storage.read(4), 3.0);
            Assertions.assertNull(storage.read(5));
            Assertions.assertEquals(1, storage.size());
            StorageTestUtils.assertFullyMatch(storage.readKeys(), 4);
        });
    }

    @Test
    public void testMultipleModifications() throws Exception {
        doWithStrings(storage -> {
                storage.write("foo", "bar");
                storage.write("bar", "foo");
                storage.write("yammy", "nooo");
                Assertions.assertEquals("bar", storage.read("foo"));
                Assertions.assertEquals("foo", storage.read("bar"));
                Assertions.assertEquals("nooo", storage.read("yammy"));
                Assertions.assertTrue(storage.exists("foo"));
                Assertions.assertEquals(3, storage.size());
                StorageTestUtils.assertFullyMatch(storage.readKeys(), "bar", "foo", "yammy");
            });

        doWithStrings(storage -> {
            Assertions.assertEquals("bar", storage.read("foo"));
            Assertions.assertEquals("foo", storage.read("bar"));
            Assertions.assertEquals("nooo", storage.read("yammy"));
            Assertions.assertTrue(storage.exists("bar"));
            Assertions.assertFalse(storage.exists("yep"));
            Assertions.assertEquals(3, storage.size());
            StorageTestUtils.assertFullyMatch(storage.readKeys(), "bar", "foo", "yammy");
        });

        doWithStrings(storage -> {
            storage.delete("bar");
            storage.write("yammy", "yeahs");
            Assertions.assertFalse(storage.exists("bar"));
            Assertions.assertFalse(storage.exists("yep"));
            Assertions.assertEquals(2, storage.size());
            StorageTestUtils.assertFullyMatch(storage.readKeys(), "foo", "yammy");
        });

        doWithStrings(storage -> {
            Assertions.assertEquals("bar", storage.read("foo"));
            Assertions.assertNull(storage.read("bar"));
            Assertions.assertEquals("yeahs", storage.read("yammy"));
            Assertions.assertEquals(2, storage.size());
            StorageTestUtils.assertFullyMatch(storage.readKeys(), "foo", "yammy");
        });
    }

    @Test
    public void testPersistAndCopy() throws Exception {
        doWithPojo(storage -> {
            storage.write(KEY_1, VALUE_1);
            storage.write(KEY_2, VALUE_2);
        });

        /* File from = new File(path1);
        String path2ext = path2 + File.separator + "trololo/";
        File to = new File(path2ext);
        FileUtils.copyDirectory(from, to); */

        doWithPojo(storage -> {
            Assertions.assertEquals(VALUE_1, storage.read(KEY_1));
            Assertions.assertEquals(VALUE_2, storage.read(KEY_2));
            Assertions.assertEquals(2, storage.size());
            StorageTestUtils.assertFullyMatch(storage.readKeys(), KEY_1, KEY_2);
        });
    }

    @Test
    @Disabled
    public void testNonEquality() throws Exception {
        Assertions.assertNotSame(doWithPojo(null), doWithPojo(null));
        Assertions.assertNotSame(doWithStrings(null), doWithStrings(null));
        Assertions.assertNotSame(doWithNumbers(null), doWithNumbers(null));
    }

    @Test
    public void testIteratorWithConcurrentKeysModification() throws Exception {
        doWithPojo(storage -> {
                storage.write(KEY_1, VALUE_1);
                storage.write(KEY_2, VALUE_2);
                storage.write(KEY_3, VALUE_3);
                Iterator<StudentKey> iterator = storage.readKeys();
                Assertions.assertTrue(iterator.hasNext());
                Assertions.assertTrue(Arrays.asList(KEY_1, KEY_2, KEY_3).contains(iterator.next()));
                Assertions.assertTrue(iterator.hasNext());
                Assertions.assertTrue(Arrays.asList(KEY_1, KEY_2, KEY_3).contains(iterator.next()));
                storage.delete(KEY_2);
                Assertions.assertTrue(iterator.hasNext());
                iterator.next();
            });
    }

    @Test
    public void testIteratorWithConcurrentValuesModification() throws Exception {
        doWithPojo(storage -> {
            storage.write(KEY_1, VALUE_1);
            storage.write(KEY_2, VALUE_2);
            storage.write(KEY_3, VALUE_3);
            Iterator<StudentKey> iterator = storage.readKeys();
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertTrue(Arrays.asList(KEY_1, KEY_2, KEY_3).contains(iterator.next()));
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertTrue(Arrays.asList(KEY_1, KEY_2, KEY_3).contains(iterator.next()));
            storage.write(KEY_3, VALUE_2);
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertTrue(Arrays.asList(KEY_1, KEY_2, KEY_3).contains(iterator.next()));
        });
    }

    @Test
    public void testDoNotWriteInClosedState() {
        assertThrows(
                Exception.class,
                () -> doWithNumbers(storage -> {
                    Assertions.assertNull(storage.read(4));
                    storage.write(4, 5.0);
                    Assertions.assertEquals((Object) 5.0, storage.read(4));
                    storage.close();
                    storage.write(3, 5.0);
                    throw new AssertionError("Storage should not accept writes in closed state");
                })
        );
    }

    @Test
    public void testDoNotReadInClosedState() {
        assertThrows(
                Exception.class,
                () -> doWithStrings(storage -> {
                    Assertions.assertNull(storage.read("trololo"));
                    storage.write("trololo", "yarr");
                    Assertions.assertEquals("yarr", storage.read("trololo"));
                    storage.close();
                    storage.readKeys();
                    throw new AssertionError("Storage should not allow read anything in closed state");
                })
        );
    }

    protected final <K extends Comparable<K>, V> KeyValueStorage<K, V> storageCallback(
            StorageTestUtils.Callback<KeyValueStorage<K, V>> callback,
            Supplier<KeyValueStorage<K, V>> supplier) throws Exception {
        KeyValueStorage<K, V> storage = supplier.get();
        //try (storage) {
            if (callback != null) {
                callback.callback(storage);
            }
        //}
        return storage;
    }

    protected final KeyValueStorage<String, String> doWithStrings(
            StorageTestUtils.Callback<KeyValueStorage<String, String>> callback) throws Exception {
        return storageCallback(callback, this::buildStringsStorage);
    }

    protected final KeyValueStorage<Integer, Double> doWithNumbers(
            StorageTestUtils.Callback<KeyValueStorage<Integer, Double>> callback) throws Exception {
        return storageCallback(callback, this::buildNumbersStorage);
    }

    protected final KeyValueStorage<StudentKey, Student> doWithPojo(
            StorageTestUtils.Callback<KeyValueStorage<StudentKey, Student>> callback) throws Exception {
        return storageCallback(callback, this::buildPojoStorage);
    }
}
