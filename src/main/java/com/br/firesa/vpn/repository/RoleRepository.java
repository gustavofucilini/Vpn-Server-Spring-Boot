package com.br.firesa.vpn.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.br.firesa.vpn.entity.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long>{

	Optional<Role> findByAuthority(String authority);
	
	boolean existsByAuthority(String authority);
	
}

