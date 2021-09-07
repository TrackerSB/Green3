package bayern.steinbrecher.green3.data;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.LocalDate;

/**
 * @author Stefan Huber
 * @since 3u00
 */
@RecordBuilder
public record Person(
        String firstname,
        String lastname,
        String title,
        Gender gender,
        LocalDate birthday,
        Address home
) implements PersonBuilder.With {
}
