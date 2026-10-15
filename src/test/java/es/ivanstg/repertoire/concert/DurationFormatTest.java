package es.ivanstg.repertoire.concert;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DurationFormatTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0:00",
            "59, 0:59",
            "1140, 19:00",
            "3599, 59:59",
            "3600, 1:00:00",
            "3725, 1:02:05"
    })
    void formatsSecondsAsMinutesOrHours(int seconds, String expected) {
        assertThat(DurationFormat.format(seconds)).isEqualTo(expected);
    }
}
