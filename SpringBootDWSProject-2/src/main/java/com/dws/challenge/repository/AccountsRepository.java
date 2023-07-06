package com.dws.challenge.repository;

import java.util.Map;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountRelatedException;

public interface AccountsRepository {

  void createAccount(Account account) throws Exception, AccountRelatedException;

  Account getAccount(String accountId);

  void clearAccounts();
  
  Map<String, Account> getAccounts();
  
}
