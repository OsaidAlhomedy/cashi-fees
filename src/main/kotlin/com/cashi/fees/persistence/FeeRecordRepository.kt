package com.cashi.fees.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface FeeRecordRepository : JpaRepository<FeeRecord, String>