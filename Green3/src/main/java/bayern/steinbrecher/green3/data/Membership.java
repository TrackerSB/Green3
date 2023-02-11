package bayern.steinbrecher.green3.data;

import bayern.steinbrecher.sepaxmlgenerator.DirectDebitMandate;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.time.LocalDate;
import java.util.Objects;
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
        Set<Integer> memberDistinctions,
        Set<Integer> contributionDistinctions,
        Optional<Boolean> honoraryMember,
        Optional<Boolean> founderMember,
        Optional<Integer> firstContributionYear,
        String phoneNumber,
        String mobileNumber,
        String email,
        Optional<Boolean> gauDistinction,
        Optional<Boolean> honorarySecretary,
        Optional<Boolean> honoraryPrincipal,
        Optional<Boolean> managementBoardMember
) implements MembershipBuilder.With {
    /**
     * @return {@code true} iff two memberships are considered equal based on the primary key of {@link Tables#MEMBERS}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Membership that = (Membership) o;
        return Objects.equals(mandate.id(), that.mandate.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(mandate.id());
    }
}
