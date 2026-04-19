package ru.megateam.lab.service;

import java.util.HashMap;
import java.util.Map;

    public class SampleService {
        private final Map<Long, String> samples = new HashMap<>();
        private long nextId = 1;

        public long SampleAdd(String name) {
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

        public Map<Long, String> getAll() {
            return new HashMap<>(samples);
        }

        public void replaceAll(Map<Long, String> newSamples) {
            samples.clear();

            long maxId = 0;
            for (Map.Entry<Long, String> entry : newSamples.entrySet()) {
                samples.put(entry.getKey(), entry.getValue());
                if (entry.getKey() > maxId) {
                    maxId = entry.getKey();
                }
            }

            nextId = maxId + 1;
        }
    }

