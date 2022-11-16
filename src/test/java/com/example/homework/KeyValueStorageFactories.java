package com.example.homework;

public abstract class KeyValueStorageFactories {
    protected abstract KeyValueStorage<String, String> buildStringsStorage() throws MalformedDataException;

    protected abstract KeyValueStorage<Integer, Double> buildNumbersStorage() throws MalformedDataException;

    protected abstract KeyValueStorage<StudentKey, Student> buildPojoStorage() throws MalformedDataException;
}

