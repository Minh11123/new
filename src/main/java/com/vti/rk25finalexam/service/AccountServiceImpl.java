package com.vti.rk25finalexam.service;

import com.vti.rk25finalexam.common.Constants;
import com.vti.rk25finalexam.common.Constants.ACCOUNT;
import com.vti.rk25finalexam.common.Constants.IS_DELETED;
import com.vti.rk25finalexam.entity.Account;
import com.vti.rk25finalexam.entity.criteria.AccountCriteria;
import com.vti.rk25finalexam.entity.dto.AccountDTO;
import com.vti.rk25finalexam.repository.AccountRepository;
import com.vti.rk25finalexam.spec.AccountSpec;
import com.vti.rk25finalexam.spec.Expression;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;
    private final QueryService<Account> queryService;

    public AccountServiceImpl(AccountRepository accountRepository,
        ModelMapper modelMapper,
        QueryService<Account> queryService) {
        this.accountRepository = accountRepository;
        this.modelMapper = modelMapper;
        this.queryService = queryService;
    }

    @Override
    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    @Override
    public Optional<Account> getOne(Integer id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<AccountDTO> getOneReturnDTO(Integer id) {
        return getOne(id).map(account -> modelMapper.map(account, AccountDTO.class));
    }

    @Override
    public Account create(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Account update(Integer id, Account account)
        throws NotFoundException {
        getOne(id)
            .map(acc -> {
                account.id(id);
                accountRepository.save(account);
                return acc;
            })
            .orElseThrow(NotFoundException::new);
        return account;
    }

    @Override
    public Account delete(Integer id) throws NotFoundException {
        return getOne(id)
            .map(account -> {
                account.id(id);
                account.isDeleted(IS_DELETED.TRUE);
                accountRepository.save(account);
                return account;
            })
            .orElseThrow(NotFoundException::new);
    }

    @Override
    public Page<AccountDTO> getAllReturnDTO(Pageable pageable) {

        Page<Account> page = accountRepository
            .findAll(pageable);

        List<AccountDTO> accountDtoList = page.getContent()
            .stream()
            .map(account -> modelMapper.map(account, AccountDTO.class))
            .collect(Collectors.toList());

        return new PageImpl<>(accountDtoList, pageable, page.getTotalElements());
    }

    @Override
    public List<AccountDTO> findByUsernameContains(String username) {
        return accountRepository.findAllByUsernameContains(username)
            .stream()
            .map(account -> modelMapper.map(account, AccountDTO.class))
            .collect(Collectors.toList());
    }

    @Override
    public List<AccountDTO> timTheoFirstnameLastname(String firstname, String lastname) {
        return accountRepository.timTheoFirstnameLastname(firstname, lastname)
            .stream()
            .map(account -> modelMapper.map(account, AccountDTO.class))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<AccountDTO> findByUsernameEquals(String username) {

        return accountRepository.findByUsername(username)
            .map(account -> modelMapper.map(account, AccountDTO.class));
    }

    @Override
    public Page<AccountDTO> findAllByCriteria(
        AccountCriteria criteria,
        Pageable pageable) {

        Specification<Account> spec = buildWhere(criteria);

        Page<Account> page = accountRepository.findAll(spec, pageable);

        List<AccountDTO> accountDtoList = page.getContent()
            .stream()
            .map(account -> modelMapper.map(account, AccountDTO.class))
            .collect(Collectors.toList());

        return new PageImpl<>(accountDtoList, pageable, page.getTotalElements());
    }

    @Override
    public List<AccountDTO> getAll(Expression expression) throws Exception {

        validateExpression(expression);

        AccountSpec accountSpec = new AccountSpec(expression);
        Specification<Account> where = Specification.where(accountSpec);

        return accountRepository.findAll(where)
            .stream()
            .map(account -> modelMapper.map(account, AccountDTO.class))
            .collect(Collectors.toList());

    }

    private void validateExpression(Expression expression) throws Exception {
        validateField(expression.getField());
        validateOperator(expression.getOperator());
        validateValue(expression.getValue());
    }

    private void validateValue(Object value) throws Exception {
        if (value == null || value.equals("")) {
            throw new Exception("value is not null");
        }
    }

    private void validateOperator(String operator) throws Exception {
        if (operator == null || operator.equals("")) {
            throw new Exception("operator is not null");
        }
    }

    private void validateField(String field) throws Exception {
        if (field == null || field.equals("")) {
            throw new Exception("field is not null");
        }
    }

    private Specification<Account> buildWhere(AccountCriteria criteria) {
        Specification<Account> spec = Specification.where(null);

        if (criteria.getId() != null) {
            spec.and(queryService.buildIntegerFilter(ACCOUNT.ID, criteria.getId()));
        }
        if (criteria.getUsername() != null) {
            spec.and(queryService.buildStringFilter(ACCOUNT.USERNAME, criteria.getUsername()));
        }
        if (criteria.getFirstName() != null) {
            spec.and(queryService.buildStringFilter(ACCOUNT.FIRST_NAME, criteria.getFirstName()));
        }
        if (criteria.getLastName() != null) {
            spec.and(queryService.buildStringFilter(ACCOUNT.LAST_NAME, criteria.getLastName()));
        }
        if (criteria.getRole() != null) {
            spec.and(queryService.buildStringFilter(ACCOUNT.ROLE, criteria.getRole()));
        }
        return spec;
    }
}
