package org.stirlitz52.temperatureplot.measurements;

import java.util.Date;

public class MeasureEvent {

    public enum MeasureEventType {
        TEMPERATURE,
    }

    public MeasureEvent(final MeasureEventType type, final String source, final double data[],
                        final Date measured_at, final String unit) {
        this.type = type;
        this.source = source;
        this.data = data;
        this.measured_at = measured_at;
        this.unit = unit;
    }

    public MeasureEventType type;
    public String source;
    public double data[];
    public Date measured_at;
    public String unit;
}
