package com.company.shop.common.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SoftDeleteEntity extends AuditableEntity {

	@Column(name = "deleted", nullable = false)
	private boolean deleted = false;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public boolean isDeleted() {
		return deleted;
	}

	public void markDeleted() {
		this.deleted = true;
		this.deletedAt = LocalDateTime.now();
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}
}
