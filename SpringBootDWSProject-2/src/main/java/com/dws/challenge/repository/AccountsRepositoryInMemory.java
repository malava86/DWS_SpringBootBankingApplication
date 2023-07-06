package com.dws.challenge.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountRelatedException;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws AccountRelatedException {
    	if(account.getAccountId().isEmpty()) {
			throw new AccountRelatedException("Account Id does not exists..!!!");
		}
		
		String accNumContainsAlphabetic = account.getAccountId().chars().filter(Character::isAlphabetic).mapToObj(Character::toString).collect(Collectors.joining());
		
		if(!accNumContainsAlphabetic.isBlank()) {
			throw new AccountRelatedException("Account Number should not contains Alphabetic chars..!!!");
		}
		
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		
		if (previousAccount != null) {
            throw new AccountRelatedException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

	@Override
	public Map<String, Account> getAccounts() {
		return accounts;
	}
}
