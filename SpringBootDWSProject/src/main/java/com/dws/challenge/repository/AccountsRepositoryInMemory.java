package com.dws.challenge.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountRelatedException;
import com.dws.challenge.exception.MoneyRelatedException;
import com.dws.challenge.service.EmailNotificationService;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Autowired
	private EmailNotificationService emailNotificationService;

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

	/**
	 * listAllAccounts(..) method is used to list all the available accounts
	 */
	@Override
	public List<Account> listAllAccounts() {

		List<Account> listAccounts = new ArrayList<Account>();
		for (Map.Entry<String, Account> entry : accounts.entrySet()) {
			listAccounts.add(entry.getValue());
		}
		return listAccounts;
	}

	/**
	 * transferMoney(...) method is used to transfer the money from account to Account.
	 */
	@Override
	public void transferMoney(String fromAccId, String toAccId, BigDecimal amount) throws Exception {

		Account accountFrom = getAccount(fromAccId);
		Account accountTo = getAccount(toAccId);
		
		if(fromAccId.compareTo(toAccId) !=0 ) {
			
		Object lock1 = fromAccId.compareTo(toAccId) < 0  ? accountFrom : accountTo;
		Object lock2 = fromAccId.compareTo(toAccId) > 0  ? accountFrom : accountTo;

		//To prevent a deadlock, always have to lock the Objects in the following order
		synchronized (lock1) {
			synchronized (lock2) {
				deposit(toAccId, amount);
				withdraw(fromAccId, amount);
			}
		}
		
		}else {
			throw new MoneyRelatedException("Both Account Should not be same...!!!");
		}

	}

	/**
	 * deposit(..) method is used deposit the amount into To Account.
	 */
	@Override
	public Account deposit(String accountId, BigDecimal amount) throws Exception {
		Account actualAccount = accounts.get(accountId);
		String message = "Deposit or Money value should not be negative...!!!";
		checkValidations(actualAccount,amount,message);
		actualAccount.setBalance(actualAccount.getBalance().add(amount));
		accounts.putIfAbsent(actualAccount.getAccountId(), actualAccount);
		emailNotificationService.notifyAboutTransfer(actualAccount, " " + amount + " transfer to " + accountId);

		return actualAccount;
	}

	/**
	 * withdraw(..) method is used to withdraw the amount from the account
	 */
	@Override
	public Account withdraw(String accountId, BigDecimal amount) throws Exception {

		Account actualAccount = accounts.get(accountId);
		String message = "Acc Balance or Money value should not be negative...!!!";
		checkValidations(actualAccount,amount,message);
		withdrawValidations(actualAccount,amount);
		actualAccount.setBalance(actualAccount.getBalance().subtract(amount));
		accounts.putIfAbsent(actualAccount.getAccountId(), actualAccount);
		emailNotificationService.notifyAboutTransfer(actualAccount, " " + amount + " transfer from " + accountId);

		return actualAccount;
	}
	
	private void checkValidations(Account actualAccount, BigDecimal amount,String message) {
		
		if (actualAccount.getBalance().signum() == -1 || ( amount!=null && amount.signum() == -1 )) {
			throw new MoneyRelatedException(message);
		}
		
	}
	
	private void withdrawValidations(Account actualAccount, BigDecimal amount) {
		
		if (amount.compareTo(actualAccount.getBalance()) > 0) {
			throw new MoneyRelatedException("Money value should not be greater then the Account Balance...!!!");
		}
	}

}
