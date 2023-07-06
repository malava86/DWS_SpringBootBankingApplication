package com.dws.challenge.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepositoryInMemory;
import lombok.Getter;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepositoryInMemory accountsRepositoryInMemory;
  
  @Autowired
  public AccountsService(AccountsRepositoryInMemory accountsRepositoryInMemory) {
    this.accountsRepositoryInMemory = accountsRepositoryInMemory;
  }

  public void createAccount(Account account)throws Exception {
    this.accountsRepositoryInMemory.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepositoryInMemory.getAccount(accountId);
  }
  
  public List<Account> listAllAccounts(){
	  return this.accountsRepositoryInMemory.listAllAccounts();
  }
  
  public void transferMoney(String fromAccId,String toAccId, BigDecimal amount)throws Exception {
	  this.accountsRepositoryInMemory.transferMoney(fromAccId, toAccId, amount);
  }
  
  public void deposit(String accountId, BigDecimal amount)throws Exception {
	  this.accountsRepositoryInMemory.deposit(accountId,amount);
  }
  
  public void withdraw(String accountId, BigDecimal amount)throws Exception {
	  this.accountsRepositoryInMemory.withdraw(accountId,amount);
  }
}
