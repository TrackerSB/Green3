package bayern.steinbrecher.green3.data;

import io.soabase.recordbuilder.core.RecordBuilder;

/**
 * @author Stefan Huber
 * @since 3u00
 */
@RecordBuilder
public record Address(
    String street,
    String houseNumber,
    String cityCode,
    String city
) implements AddressBuilder.With {
}
