package es.ivanstg.repertoire.concert;

/** Formats a number of seconds as "M:SS", or "H:MM:SS" from one hour up. */
public final class DurationFormat {

    private DurationFormat() {
    }

    public static String format(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = totalSeconds % 3600 / 60;
        int seconds = totalSeconds % 60;
        return hours > 0
                ? "%d:%02d:%02d".formatted(hours, minutes, seconds)
                : "%d:%02d".formatted(minutes, seconds);
    }
}
