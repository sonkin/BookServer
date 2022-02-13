package com.luxoft.highperformance.bookserver.measure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Data
public class Measurement {
    private String name;
    private long time;
    private long callsCount = 0L;
    private long callsCountMeasured;
    private long latency;
    private double percent;
    private String boost;

    @JsonIgnore
    private SortedSet<Long> times = new TreeSet<>();

    public long getPercentile50() {
        return getPercentile(50);
    }

    public long getPercentile90() {
        return getPercentile(90);
    }

    public long getPercentile99() {
        return getPercentile(99);
    }

    public Long getPercentile(int percentile) {
        int indexAtPercentile = (int) Math.floor(times.size() * percentile / 100d);
        List<Long> list = new ArrayList<>(times);
        return list.get(indexAtPercentile);
    }

}
