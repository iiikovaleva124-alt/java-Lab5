package ru.megateam.lab.service;

import ru.megateam.lab.domain.Sample;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleService {
    private final Map<Long, Sample> samples = new HashMap<>();
    private long nextId = 1;

    public long SampleAdd(String name) {
        long id = nextId++;
        Sample sample = new Sample(id, name);
        samples.put(id, sample);
        return id;
    }

    public boolean exists(long id) {
        return samples.containsKey(id);
    }

    public String getName(long id) {
        Sample sample = samples.get(id);
        return sample != null ? sample.getName() : null;
    }

    public Long getId(String name) {
        if (name == null) return null;

        for (Map.Entry<Long, Sample> entry : samples.entrySet()) { //entrySet возвращает набор всех пар ключ-значение
            //Map.Entry - возвращает пары id - образец
            if (entry.getValue().getName().equalsIgnoreCase(name)) { //сравнивает имя образца с искомым
                return entry.getKey(); //возвращает id
            }
        }
        return null;
    }

    public List<Sample> getAll() {    //list чтобы хранить все свойства
        return new ArrayList<>(samples.values());
    }

    public Sample getById(long id) {
        return samples.get(id);
    }

    public void replaceAll(List<Sample> newSamples) { //заменяет все образцы на новые
        samples.clear();  //очищаем старые
        for (Sample sample : newSamples) {
            samples.put(sample.getId(), sample); //сохраняет в хранилище по id
            if (sample.getId() >= nextId) { //проверяет если id образца из файла больше следующего по порядку
                nextId = sample.getId() + 1; //сдвигаем счетчик
            }
        }
    }
}