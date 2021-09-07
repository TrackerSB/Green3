package bayern.steinbrecher.green3.data;

import bayern.steinbrecher.sepaxmlgenerator.DirectDebitMandate;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Stefan Huber
 * @since 3u00
 */
@RecordBuilder
public record Membership(
        Person member,
        LocalDate entryDate,
        boolean contributionFree,
        DirectDebitMandate mandate,
        Optional<Double> contribution,
        Optional<ActiveState> state,
        Optional<LocalDate> leavingDate,
        Set<Integer> distinctions
) implements MembershipBuilder.With {
}
