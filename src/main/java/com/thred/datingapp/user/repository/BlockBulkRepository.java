package com.thred.datingapp.user.repository;

import com.thred.datingapp.common.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class BlockBulkRepository {

  private final JdbcTemplate jdbcTemplate;

  @Transactional
  public void bulkInsert(final User blocker, final List<User> blockedUsers) {
    final String saveQuery = "INSERT INTO block(blocker_id, blocked_user_id, created_date, last_modified_date) VALUES(?, ?, ?, ?)";
    jdbcTemplate.batchUpdate(saveQuery, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(final PreparedStatement ps, final int i) throws SQLException {
          ps.setLong(1, blocker.getId());
          ps.setLong(2, blockedUsers.get(i).getId());
          LocalDateTime now = LocalDateTime.now();
          ps.setTimestamp(3, Timestamp.valueOf(now));
          ps.setTimestamp(4, Timestamp.valueOf(now));
      }
      @Override
      public int getBatchSize() {
        return blockedUsers.size();
      }
    });
  }
}
