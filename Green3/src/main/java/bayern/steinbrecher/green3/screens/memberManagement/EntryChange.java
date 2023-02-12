package bayern.steinbrecher.green3.screens.memberManagement;

import bayern.steinbrecher.green3.data.Membership;

import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 3u00
 */
record EntryChange(
        Optional<Membership> entryToRemove,
        Optional<Membership> entryToAdd
) {
}
