package ru.megateam.lab.service;

import java.util.HashMap;
import java.util.Map;

    public class SampleService {
        private final Map<Long, String> samples = new HashMap<>(); // id -> name
        private long nextId = 1;

        public long add(String name) {
            long id = nextId++;
            samples.put(id, name);
            return id;
        }

        public boolean exists(long id) {
            return samples.containsKey(id);
        }

        public String getName(long id) {
            return samples.get(id);
        }
    }