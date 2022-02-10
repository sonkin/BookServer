package com.luxoft.highperformance.bookserver.measure;

import lombok.Data;

@Data
public class Measurement {
    private String name;
    private long time;
    private long callsCount = 0L;
    private long callsCountMeasured;
    private long latency;
    private double percent;
    private String boost;

}
