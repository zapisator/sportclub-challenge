package com.sportclub.challenge.domain.port.datasource;

import com.sportclub.challenge.domain.model.Branch;
import com.sportclub.challenge.domain.model.User;

import java.util.List;

public interface Db1DataSourcePort {
    List<User> findAllUsersFromDb1();
    List<Branch> findAllBranchesFromDb1();
}
