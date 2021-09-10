package bayern.steinbrecher.green3.data;

import bayern.steinbrecher.dbConnector.scheme.ColumnParser;
import bayern.steinbrecher.dbConnector.scheme.RegexColumnPattern;
import bayern.steinbrecher.dbConnector.scheme.SimpleColumnPattern;
import bayern.steinbrecher.dbConnector.scheme.TableScheme;
import bayern.steinbrecher.sepaxmlgenerator.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 3u00
 */
public final class Tables {
    public static final TableScheme<Set<Membership>, Membership> MEMBERS = new TableScheme<>(
            "Mitglieder",
            List.of(
                    new SimpleColumnPattern<>("Mitgliedsnummer",
                            ColumnParser.INTEGER_COLUMN_PARSER,
                            (m, i) -> m.withMandate(m.mandate().withId(String.valueOf(i)))),
                    new SimpleColumnPattern<>("Vorname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, f) -> m.withMember(m.member().withFirstname(f))),
                    new SimpleColumnPattern<>("Nachname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, l) -> m.withMember(m.member().withLastname(l))),
                    new SimpleColumnPattern<>("Titel",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, t) -> m.withMember(m.member().withTitle(t))),
                    new SimpleColumnPattern<>("IstMaennlich",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, g) -> m.withMember(m.member().withGender(g ? Gender.MALE : Gender.FEMALE))),
                    new SimpleColumnPattern<>("Geburtstag",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (m, b) -> m.withMember(m.member().withBirthday(b))),
                    new SimpleColumnPattern<>("MitgliedSeit",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            MembershipBuilder.With::withEntryDate),
                    new SimpleColumnPattern<>("Strasse",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, s) -> m.withMember(m.member().withHome(m.member().home().withStreet(s)))),
                    new SimpleColumnPattern<>("Hausnummer",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, h) -> m.withMember(m.member().withHome(m.member().home().withHouseNumber(h)))),
                    new SimpleColumnPattern<>("PLZ",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, c) -> m.withMember(m.member().withHome(m.member().home().withCityCode(c)))),
                    new SimpleColumnPattern<>("Ort",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, c) -> m.withMember(m.member().withHome(m.member().home().withCity(c)))),
                    new SimpleColumnPattern<>("IstBeitragsfrei",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            MembershipBuilder.With::withContributionFree,
                            Optional.of(Optional.of(false)), false, false),
                    new SimpleColumnPattern<>("Iban",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, i) -> m.withMandate(m.mandate().withOwner(m.mandate().owner().withIban(new IBAN(i))))),
                    new SimpleColumnPattern<>("Bic",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, b) -> m.withMandate(m.mandate().withOwner(m.mandate().owner().withBic(new BIC(b))))),
                    new SimpleColumnPattern<>("KontoinhaberVorname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, f) -> m.withMandate(m.mandate().withOwner(m.mandate().owner().withFirstname(f)))),
                    new SimpleColumnPattern<>("KontoinhaberNachname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, l) -> m.withMandate(m.mandate().withOwner(m.mandate().owner().withLastname(l)))),
                    new SimpleColumnPattern<>("MandatErstellt",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (m, s) -> m.withMandate(m.mandate().withSigned(s)))
            ),
            List.of(
                    new SimpleColumnPattern<>("Beitrag",
                            ColumnParser.DOUBLE_COLUMN_PARSER,
                            (m, c) -> m.withContribution(Optional.ofNullable(c))),
                    new SimpleColumnPattern<>("IstAktiv",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, a) -> m.withState(Optional.of(a ? ActiveState.ACTIVE : ActiveState.PASSIVE))),
                    new SimpleColumnPattern<>("AusgetretenSeit",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (m, ld) -> m.withLeavingDate(Optional.ofNullable(ld)),
                            Optional.of(Optional.empty()), false, true),
                    new RegexColumnPattern<>("^\\d+MitgliedGeehrt$",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, year, honored) -> {
                                if (honored) {
                                    m.distinctions().add(year);
                                }
                                return m;
                            },
                            cn -> Integer.parseInt(cn.substring(0, cn.length() - "MitgliedGeehrt".length())))
            ),
            // Initialize membership while ensuring nested builders are not null
            () -> MembershipBuilder.builder()
                    .distinctions(new HashSet<>())
                    .mandate(DirectDebitMandateBuilder.builder()
                            .owner(AccountHolderBuilder.builder().build())
                            .build())
                    .member(PersonBuilder.builder()
                            .home(AddressBuilder.builder().build())
                            .build())
                    .build(),
            ms -> ms.collect(Collectors.toSet()) // FIXME Check validity of built members
    );
}
