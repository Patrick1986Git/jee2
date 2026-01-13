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

	public void delete() {
		this.deleted = true;
		this.deletedAt = LocalDateTime.now();
	}

	public void markDeleted() {
		this.delete();
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}
}