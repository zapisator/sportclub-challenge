package com.sportclub.challenge.domain.model.user;


import com.sportclub.challenge.domain.model.branch.Branch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Domain Model Tests")
class UserTest {

    private User authorizedUser;
    private User deniedUser;

    @BeforeEach
    void setUp() {
        final Branch testBranch = new Branch("B001", "Central", "123 Main St", "City A");
        authorizedUser = new User(
                "U001", "John", "Doe", "john.doe@mail.com", "123456789", "111222333",
                UserState.AUTHORIZED, testBranch
        );
        deniedUser = new User(
                "U002", "Jane", "Smith", "jane.smith@mail.com", "987654321", "444555666",
                UserState.DENIED, testBranch
        );
    }

    @Nested
    @DisplayName("isAuthorized Method")
    class IsAuthorized {
        @Test
        @DisplayName("Should return true when state is AUTHORIZED")
        void shouldReturnTrueWhenStateIsAuthorized() {
            assertThat(authorizedUser.isAuthorized()).isTrue();
        }

        @Test
        @DisplayName("Should return false when state is DENIED")
        void shouldReturnFalseWhenStateIsDenied() {
            assertThat(deniedUser.isAuthorized()).isFalse();
        }
    }

    @Nested
    @DisplayName("withState Method")
    class WithState {
        @Test
        @DisplayName("Should return a new User instance with the updated state")
        void shouldReturnNewUserWithUpdatedState() {
            final UserState newState = UserState.DENIED;

            final User updatedUser = authorizedUser.withState(newState);

            assertAll(
                    () -> assertThat(updatedUser).isNotNull(),
                    () -> assertThat(updatedUser.state()).isEqualTo(newState),
                    () -> assertThat(updatedUser.id()).isEqualTo(authorizedUser.id()),
                    () -> assertThat(updatedUser.firstName()).isEqualTo(authorizedUser.firstName()),
                    () -> assertThat(updatedUser.branch()).isEqualTo(authorizedUser.branch())
            );
        }

        @Test
        @DisplayName("Should not modify the original User instance")
        void shouldNotModifyOriginalUser() {
            final UserState originalState = authorizedUser.state();

            authorizedUser.withState(UserState.DENIED);

            assertThat(authorizedUser.state()).isEqualTo(originalState);
        }

        @Test
        @DisplayName("Should return a different object instance")
        void shouldReturnDifferentObjectInstance() {
            final UserState newState = UserState.DENIED;

            final User updatedUser = authorizedUser.withState(newState);

            assertThat(updatedUser).isNotSameAs(authorizedUser);
        }
    }

    @Nested
    @DisplayName("withBranch Method")
    class WithBranch {
        private Branch newBranch;

        @BeforeEach
        void setupBranch() {
            newBranch = new Branch("B002", "North", "456 North Ave", "City B");
        }

        @Test
        @DisplayName("Should return a new User instance with the updated branch")
        void shouldReturnNewUserWithUpdatedBranch() {
            final User updatedUser = authorizedUser.withBranch(newBranch);

            assertAll(
                    () -> assertThat(updatedUser.branch()).isEqualTo(newBranch),
                    () -> assertThat(updatedUser.id()).isEqualTo(authorizedUser.id()),
                    () -> assertThat(updatedUser.firstName()).isEqualTo(authorizedUser.firstName()),
                    () -> assertThat(updatedUser.state()).isEqualTo(authorizedUser.state())
            );
        }

        @Test
        @DisplayName("Should not modify the original User instance")
        void shouldNotModifyOriginalUser() {
            final Branch originalBranch = authorizedUser.branch();

            authorizedUser.withBranch(newBranch);

            assertThat(authorizedUser.branch()).isEqualTo(originalBranch);
        }

        @Test
        @DisplayName("Should return a different object instance")
        void shouldReturnDifferentObjectInstance() {
            final User updatedUser = authorizedUser.withBranch(newBranch);

            assertThat(updatedUser).isNotSameAs(authorizedUser);
        }
    }
}