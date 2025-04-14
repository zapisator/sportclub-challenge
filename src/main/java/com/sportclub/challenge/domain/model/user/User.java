package com.sportclub.challenge.domain.model.user;

import com.sportclub.challenge.domain.model.branch.Branch;
import lombok.Builder;

@Builder
public record User(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String dni,
        UserState state,
        Branch branch
) {
    public boolean isAuthorized() {
        return UserState.AUTHORIZED.equals(this.state);
    }

    public User withState(UserState newState) {
        return new User(
                this.id, this.firstName, this.lastName, this.email, this.phone, this.dni,
                newState, this.branch
        );
    }

    public User withBranch(Branch newBranch) {
        return new User(
                this.id, this.firstName, this.lastName, this.email, this.phone, this.dni,
                this.state, newBranch
        );
    }
}
