package com.company.shop.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

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
