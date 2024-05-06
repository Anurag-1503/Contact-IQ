package com.contact.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.contact.entities.Contact;

import jakarta.transaction.Transactional;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	
	//pagination
	
	
	@Query("from Contact as c where c.user.id =:userId")
	//pageable object will have 2 information :
	//1-current page
	//2-contacts per page
	public Page<Contact> findContactsByUser(@Param("userId") int userId , Pageable pageable);
	
	@Modifying
	@Transactional
	@Query(value="delete from Contact c where c.cid = ?1")
	void deleteByIdCustom(Integer cId);
}
