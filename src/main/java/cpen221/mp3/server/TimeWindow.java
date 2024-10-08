package cpen221.mp3.server;

public class TimeWindow {
    public final double startTime;
    public final double endTime;

    public TimeWindow(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "TimeWindow," + getStartTime() + "," + getEndTime();
        // Example: TimeWindow,1,5
    }

    public static TimeWindow toTimeWindow(String str) {
        String[] split = str.split(",");
        return new TimeWindow(Double.valueOf(split[1]), Double.valueOf(split[2]));
    }
}
