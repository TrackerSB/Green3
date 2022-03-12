package bayern.steinbrecher.green3.data;

import bayern.steinbrecher.dbConnector.scheme.ColumnParser;
import bayern.steinbrecher.dbConnector.scheme.RegexColumnPattern;
import bayern.steinbrecher.dbConnector.scheme.SimpleColumnPattern;
import bayern.steinbrecher.dbConnector.scheme.TableScheme;
import bayern.steinbrecher.sepaxmlgenerator.AccountHolderBuilder;
import bayern.steinbrecher.sepaxmlgenerator.BIC;
import bayern.steinbrecher.sepaxmlgenerator.DirectDebitMandateBuilder;
import bayern.steinbrecher.sepaxmlgenerator.IBAN;

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
                            (m, i) -> m.withMandate(m.mandate().withId(String.valueOf(i))),
                            m -> Integer.parseInt(m.mandate().id()),
                            Optional.empty(), true, false),
                    new SimpleColumnPattern<>("Vorname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, f) -> m.withMember(m.member().withFirstname(f)),
                            m -> m.member().firstname()),
                    new SimpleColumnPattern<>("Nachname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, l) -> m.withMember(m.member().withLastname(l)),
                            m -> m.member().lastname()),
                    new SimpleColumnPattern<>("Titel",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, t) -> m.withMember(m.member().withTitle(t)),
                            m -> m.member().title()),
                    new SimpleColumnPattern<>("IstMaennlich",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, g) -> m.withMember(m.member().withGender(g ? Gender.MALE : Gender.FEMALE)),
                            m -> m.member().gender() == Gender.MALE),
                    new SimpleColumnPattern<>("Geburtstag",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (m, b) -> m.withMember(m.member().withBirthday(b)),
                            m -> m.member().birthday()),
                    new SimpleColumnPattern<>("MitgliedSeit",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            MembershipBuilder.With::withEntryDate,
                            Membership::entryDate),
                    new SimpleColumnPattern<>("Strasse",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, s) -> m.withMember(m.member().withHome(m.member().home().withStreet(s))),
                            m -> m.member().home().street()),
                    new SimpleColumnPattern<>("Hausnummer",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, h) -> m.withMember(m.member().withHome(m.member().home().withHouseNumber(h))),
                            m -> m.member().home().houseNumber()),
                    new SimpleColumnPattern<>("PLZ",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, c) -> m.withMember(m.member().withHome(m.member().home().withCityCode(c))),
                            m -> m.member().home().cityCode()),
                    new SimpleColumnPattern<>("Ort",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, c) -> m.withMember(m.member().withHome(m.member().home().withCity(c))),
                            m -> m.member().home().city()),
                    new SimpleColumnPattern<>("IstBeitragsfrei",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            MembershipBuilder.With::withContributionFree,
                            Membership::contributionFree,
                            Optional.of(Optional.of(false)), false, false),
                    new SimpleColumnPattern<>("Iban",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, i) -> m.withMandate(m.mandate().withOwner(m.mandate().owner().withIban(new IBAN(i)))),
                            m -> m.mandate().owner().iban().value()),
                    new SimpleColumnPattern<>("Bic",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, b) -> m.withMandate(m.mandate().withOwner(m.mandate().owner().withBic(new BIC(b)))),
                            m -> m.mandate().owner().bic().value()),
                    new SimpleColumnPattern<>("KontoinhaberVorname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, f) -> m.withMandate(m.mandate().withOwner(m.mandate().owner().withFirstname(f))),
                            m -> m.mandate().owner().firstname()),
                    new SimpleColumnPattern<>("KontoinhaberNachname",
                            ColumnParser.STRING_COLUMN_PARSER,
                            (m, l) -> m.withMandate(m.mandate().withOwner(m.mandate().owner().withLastname(l))),
                            m -> m.mandate().owner().lastname()),
                    new SimpleColumnPattern<>("MandatErstellt",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (m, s) -> m.withMandate(m.mandate().withSigned(s)),
                            m -> m.mandate().signed())
            ),
            List.of(
                    new SimpleColumnPattern<>("Beitrag",
                            ColumnParser.DOUBLE_COLUMN_PARSER,
                            (m, c) -> m.withContribution(Optional.ofNullable(c)),
                            membership -> membership.contribution().orElse(0d)),
                    new SimpleColumnPattern<>("IstAktiv",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, a) -> m.withState(Optional.of(a ? ActiveState.ACTIVE : ActiveState.PASSIVE)),
                            m -> m.state().isPresent() && m.state().get() == ActiveState.ACTIVE),
                    new SimpleColumnPattern<>("IstEhrenmitglied",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, h) -> m.withHonoraryMember(Optional.of(h)),
                            m -> m.honoraryMember().get()),
                    new SimpleColumnPattern<>("IstGründungsmitglied",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, h) -> m.withFounderMember(Optional.of(h)),
                            m -> m.founderMember().get()),
                    new SimpleColumnPattern<>("hatGauEhrenzeichen",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, h) -> m.withGauDistinction(Optional.of(h)),
                            m -> m.gauDistinction().get()),
                    new SimpleColumnPattern<>("IstEhrenschriftführer",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, h) -> m.withHonorarySecretary(Optional.of(h)),
                            m -> m.honorarySecretary().get()),
                    new SimpleColumnPattern<>("IstEhrenvorstand",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, h) -> m.withHonoraryPrincipal(Optional.of(h)),
                            m -> m.honoraryPrincipal().get()),
                    new SimpleColumnPattern<>("IstInVorstandschaft",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, h) -> m.withManagementBoardMember(Optional.of(h)),
                            m -> m.managementBoardMember().get()),
                    new SimpleColumnPattern<>("AusgetretenSeit",
                            ColumnParser.LOCALDATE_COLUMN_PARSER,
                            (m, ld) -> m.withLeavingDate(Optional.ofNullable(ld)),
                            m -> m.leavingDate().orElse(null),
                            Optional.of(Optional.empty()), false, true),
                    new SimpleColumnPattern<>("MitwirkendSeit",
                            ColumnParser.INTEGER_COLUMN_PARSER,
                            (m, cy) -> m.withFirstContributionYear(Optional.ofNullable(cy)),
                            m -> m.firstContributionYear().orElse(null),
                            Optional.of(Optional.empty()), false, true),
                    new RegexColumnPattern<>("^\\d+MitgliedGeehrt$",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, year, honored) -> {
                                if (honored) {
                                    m.memberDistinctions().add(year);
                                }
                                return m;
                            },
                            cn -> Integer.parseInt(cn.substring(0, cn.length() - "MitgliedGeehrt".length())),
                            (m, k) -> m.memberDistinctions().contains(k)),
                    new RegexColumnPattern<>("^\\d+MitwirkendGeehrt$",
                            ColumnParser.BOOLEAN_COLUMN_PARSER,
                            (m, year, honored) -> {
                                if (honored) {
                                    m.contributionDistinctions().add(year);
                                }
                                return m;
                            },
                            cn -> Integer.parseInt(cn.substring(0, cn.length() - "MitwirkendGeehrt".length())),
                            (m, k) -> m.contributionDistinctions().contains(k)),
                    new SimpleColumnPattern<>("Telefonnummer",
                            ColumnParser.STRING_COLUMN_PARSER,
                            MembershipBuilder.With::withPhoneNumber,
                            Membership::phoneNumber),
                    new SimpleColumnPattern<>("Handynummer",
                            ColumnParser.STRING_COLUMN_PARSER,
                            MembershipBuilder.With::withMobileNumber,
                            Membership::mobileNumber),
                    new SimpleColumnPattern<>("E-Mail",
                            ColumnParser.STRING_COLUMN_PARSER,
                            MembershipBuilder.With::withEmail,
                            Membership::email)
            ),
            /* Initialize membership while ensuring nested builders are not null and fields have appropriate default
             * values
             */
            () -> MembershipBuilder.builder()
                    .contribution(Optional.empty())
                    .contributionDistinctions(new HashSet<>())
                    .memberDistinctions(new HashSet<>())
                    .firstContributionYear(Optional.empty())
                    .founderMember(Optional.empty())
                    .honoraryMember(Optional.empty())
                    .leavingDate(Optional.empty())
                    .mandate(DirectDebitMandateBuilder.builder()
                            .owner(AccountHolderBuilder.builder().build())
                            .build())
                    .member(PersonBuilder.builder()
                            .home(AddressBuilder.builder().build())
                            .build())
                    .state(Optional.empty())
                    .build(),
            ms -> ms.collect(Collectors.toSet()) // FIXME Check validity of built members
    );
}
