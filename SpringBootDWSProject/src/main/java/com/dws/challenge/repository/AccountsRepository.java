package com.dws.challenge.repository;

import java.math.BigDecimal;
import java.util.List;
import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountRelatedException;

public interface AccountsRepository {

  void createAccount(Account account) throws Exception, AccountRelatedException;

  Account getAccount(String accountId);

  void clearAccounts();
  
  List<Account> listAllAccounts();
  
  void transferMoney(String fromAccId, String toAccId, BigDecimal amoun) throws Exception;
  
  Account deposit(String accountId, BigDecimal amount)throws Exception;
  
  Account withdraw(String accountId, BigDecimal amount)throws Exception;
  
}
