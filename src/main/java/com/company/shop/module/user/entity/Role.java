package com.company.shop.module.user.entity;

import com.company.shop.common.model.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

	@Column(name = "name", nullable = false, unique = true, length = 50)
	private String name;

	protected Role() {
		// JPA
	}

	public Role(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
